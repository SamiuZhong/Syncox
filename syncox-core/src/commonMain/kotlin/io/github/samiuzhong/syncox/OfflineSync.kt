package io.github.samiuzhong.syncox

/**
 * @author samiu 2026/7/5
 * @email samiuzhong@foxmail.com
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class OfflineSync(
    val action: String,
)
