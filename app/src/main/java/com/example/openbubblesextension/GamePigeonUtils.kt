package com.example.openbubblesextension

import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.math.pow
import kotlin.math.floor

object GamePigeonUtils {
    fun decodeFromUrl(url: String): String {
        return decrypt(URLDecoder.decode(url.split("&data=")[1], "UTF-8"));
    }

    fun encodeToUrl(gpData: String): String {
        return "data:?ver=51&data=" + URLEncoder.encode(encrypt(gpData), "UTF-8");
    }

    fun extractReplay(gpData: String): String {
        return gpData.split("&replay=")[1].split("&")[0]
    }

    fun decrypt(encrypted: String): String {
        val rand = Rand48(0L)
        rand.srand(encrypted.length * 0xefL)

        val offsets: ArrayList<Int> = ArrayList()
        var modifier = 0
        for (i in encrypted.indices) {
            offsets.add(
                floor(rand.drand() * (modifier + encrypted.length)).toInt()
            )
            modifier -= 1
        }

        var output = ""
        for (i in offsets.indices.reversed()) {
            val offset = offsets[i]
            output = output.substring(0, offset) + encrypted[i] + output.substring(offset)
        }

        return output
    }

    fun encrypt(plaintext: String): String {
        var plaintext = plaintext
        val rand = Rand48(0)
        rand.srand(plaintext.length * 0xefL)

        var output = ""
        for (i in plaintext.indices) {
            val idx = floor(rand.drand() * plaintext.length).toInt()
            output += plaintext[idx]
            plaintext = plaintext.substring(0, idx) + plaintext.substring(idx + 1)
        }

        return output
    }

    private class Rand48(var n: Long) {
        fun srand(seed: Long) {
            this.n = (seed shl 16) + 0x330e
        }

        fun next(): Long {
            this.n = (25214903917L * this.n + 11) and (2.0.pow(48.0).toLong() - 1)
            return this.n
        }

        fun drand(): Double {
            return this.next() / 2.0.pow(48.0)
        }

        fun lrand(): Long {
            return this.next() shr 17
        }

        fun mrand(): Long {
            var n = this.next() shr 16
            if ((n and (1L shl 31)) != 0L) {
                n -= 1L shl 32
            }
            return n
        }
    }
}