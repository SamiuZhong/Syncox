package io.github.samiuzhong.syncox.compiler

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import java.io.OutputStream

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
class SyncoxProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private var isInvoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isInvoked) return emptyList()
        isInvoked = true

        val annotationName = "io.github.samiuzhong.syncox.OfflineSync"
        val symbols = resolver.getSymbolsWithAnnotation(annotationName)

        val validFunctions =
            symbols
                .filterIsInstance<KSFunctionDeclaration>()
                .filter { it.validate() }

        val routes = mutableListOf<RouteInfo>()

        for (function in validFunctions) {
            if (!function.modifiers.contains(Modifier.SUSPEND)) {
                logger.error("Syncox 编译错误：@OfflineSync 只能用于标记 suspend 函数！", function)
                continue
            }

            val annotation =
                function.annotations.first {
                    it.annotationType
                        .resolve()
                        .declaration.qualifiedName
                        ?.asString() == annotationName
                }
            val actionArgument =
                annotation.arguments.firstOrNull { it.name?.asString() == "action" }
            val actionType = actionArgument?.value?.toString() ?: continue

            val packageName = function.packageName.toString()
            val functionName = function.simpleName.asString()

            routes.add(RouteInfo(actionType, packageName, functionName))
        }

        generateRouterClass(routes)
        generateExtensionProperty()
        return emptyList()
    }

    private fun generateRouterClass(routes: List<RouteInfo>) {
        val packageName = "io.github.samiuzhong.syncox.generated"
        val className = "GeneratedSyncoxRouter"

        val file: OutputStream =
            codeGenerator.createNewFile(
                dependencies = Dependencies(false),
                packageName = packageName,
                fileName = className,
            )

        val imports =
            routes
                .map { "import ${it.packageName}.${it.functionName}" }
                .distinct()
                .joinToString("\n")

        val branches =
            routes.joinToString("\n                            ") { route ->
                "\"${route.actionType}\" -> ${route.functionName}(payloadJson)"
            }

        val code =
            """
            package $packageName

            import io.github.samiuzhong.syncox.SyncoxNetworkHandler
            import io.github.samiuzhong.syncox.NetworkResult
            $imports

            /**
             * ⚠️ 警告：此类由 Syncox KSP 编译器自动生成，请勿手动修改！
             */
            public object $className : SyncoxNetworkHandler {
                override suspend fun execute(actionType: String, payloadJson: String): NetworkResult {
                    return try {
                        when (actionType) {
                            $branches
                            else -> NetworkResult.Failure(isFatal = true, error = Exception("未知的离线动作: ${'$'}actionType"))
                        }
                    } catch (e: Exception) {
                        NetworkResult.Failure(isFatal = false, error = e)
                    }
                }
            }
            """.trimIndent()

        file.write(code.toByteArray())
        file.close()
    }

    private fun generateExtensionProperty() {
        val packageName = "io.github.samiuzhong.syncox"
        val fileName = "SyncoxAutoRouterExt"

        val file: OutputStream =
            codeGenerator.createNewFile(
                dependencies = Dependencies(false),
                packageName = packageName,
                fileName = fileName,
            )

        val code =
            """
            package $packageName

            import io.github.samiuzhong.syncox.Syncox
            import io.github.samiuzhong.syncox.SyncoxNetworkHandler
            import io.github.samiuzhong.syncox.generated.GeneratedSyncoxRouter

            /**
             * ⚠️ 警告：此扩展属性由 Syncox KSP 自动生成，请勿手动修改。
             * * 业务方初始化引擎时，直接传入 [Syncox.autoRouter] 即可实现零侵入路由挂载。
             */
            public val Syncox.autoRouter: SyncoxNetworkHandler
                get() = GeneratedSyncoxRouter
            """.trimIndent()

        file.write(code.toByteArray())
        file.close()
    }

    private data class RouteInfo(
        val actionType: String,
        val packageName: String,
        val functionName: String,
    )
}

class SyncoxProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        SyncoxProcessor(environment.codeGenerator, environment.logger)
}
