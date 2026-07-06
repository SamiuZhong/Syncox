import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.AZUL)
    }
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.ksp.symbol.processing)
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates("io.github.samiuzhong", "syncox-compiler", "0.1.0")

    pom {
        name = "Syncox"
        description =
            "The KSP annotation processor for Syncox engine, automatically generating routing tables for offline sync."
        inceptionYear = "2026"
        url = "https://github.com/SamiuZhong/Syncox"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "samiuzhong"
                name = "Samiu Zhong"
                url = "https://github.com/SamiuZhong"
            }
        }
        scm {
            url = "https://github.com/SamiuZhong/Syncox.git"
            connection = "scm:git:git://github.com/SamiuZhong/Syncox.git"
            developerConnection = "scm:git:git@github.com:SamiuZhong/Syncox.git"
        }
    }
}
