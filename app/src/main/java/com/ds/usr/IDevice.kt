package com.ds.usr

import java.io.IOException

interface IDevice {
    @Throws(IOException::class)
    fun read(buf: ByteArray): Int
    fun close()
}