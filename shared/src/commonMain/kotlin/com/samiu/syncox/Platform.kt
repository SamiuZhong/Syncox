package com.samiu.syncox

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform