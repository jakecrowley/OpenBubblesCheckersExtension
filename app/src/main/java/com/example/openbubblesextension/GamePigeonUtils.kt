package com.example.openbubblesextension

import android.content.BroadcastReceiver
import android.net.Uri
import com.bluebubbles.messaging.IKeyboardHandle
import com.bluebubbles.messaging.IMessageViewHandle
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.math.pow
import kotlin.math.floor

class CheckersData() {
    val queryParams = arrayOf("sender", "version", "tver", "ios", "game", "id", "size", "player", "player2", "mode", "avatar1", "avatar2", "replay", "num", "build")
    val data: HashMap<String, String> = java.util.HashMap()

    constructor(gpData: String) : this() {
        val uri = Uri.parse(gpData)
        for (param in queryParams) {
            val value = uri.getQueryParameter(param)
            if (value != null) {
                data[param] = value
            }
        }

        data["game"] = "checkers"
    }

    fun buildUrl(): String {
        var uri = "?"
        for (param in queryParams) {
            if (data.containsValue(param)) {
                uri += "${param}=${data[param]}&"
            }
        }
        uri = uri.substring(0, uri.length-1) //strip last &

//        return GamePigeonUtils.encodeToUrl(
//            "?sender=$sender&version=0&tver=$tver&ios=$ios&game=checkers&id=$id&size=4&player=2&player2=$sender&mode=$mode&avatar1=$avatar1&avatar2=$avatar2&replay=$replay&num=$num&build=sYXqxHJGXuxeL"
//        )
    }
}

object GamePigeonUtils {

    fun decodeFromUrl(url: String): String {
        return URLDecoder.decode(decrypt(URLDecoder.decode(url.split("&data=")[1], "UTF-8")), "UTF-8")
    }

    fun encodeToUrl(gpData: String): String {
        return "data:?ver=51&data=" + URLEncoder.encode(encrypt(gpData.replace("|", "%7C")), "UTF-8");
    }

    fun extractReplay(gpData: String): String? {
        if (gpData.contains("&replay=")) {
            return gpData.split("&replay=")[1].split("&")[0]
        }
        return null
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