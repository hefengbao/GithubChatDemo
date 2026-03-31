package com.hefengbao.kmp.demo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform