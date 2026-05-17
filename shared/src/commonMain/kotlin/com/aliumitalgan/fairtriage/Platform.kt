package com.aliumitalgan.fairtriage

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform