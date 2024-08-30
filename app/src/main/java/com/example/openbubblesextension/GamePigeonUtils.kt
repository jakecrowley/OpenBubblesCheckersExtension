package com.example.openbubblesextension

import android.content.BroadcastReceiver
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.bluebubbles.messaging.IKeyboardHandle
import com.bluebubbles.messaging.IMessageViewHandle
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import kotlin.math.pow
import kotlin.math.floor
import kotlin.random.Random

class CheckersData() {
    private val queryParams = arrayOf("sender", "version", "tver", "ios", "game", "id", "size", "player", "player2", "mode", "avatar1", "avatar2", "replay", "num", "build")
    private val data: HashMap<String, String> = java.util.HashMap()

    private val sender: String = "B9A474B9-3D21-4B7B-9331-964079B3A605cHGxEL"

    constructor(gpData: String = "") : this() {
        if (gpData == "") {
            return
        }

        val dataParams = GamePigeonUtils.parseArgsFromUrl(gpData)
        for (param in queryParams) {
            if (dataParams.contains(param)) {
                data[param] = dataParams[param]!!
            }
        }
    }

    fun startGameUrl(mode: String): String {
        val randomBytes = ByteArray(12)
        Random.nextBytes(randomBytes)
        val id = Base64.encode(randomBytes, Base64.DEFAULT)

        return GamePigeonUtils.encodeToUrl(
            "?sender=$sender&version=5&tver=5&ios=18.0&start=&caption=Let's play Checkers!&id=$id&player=2&player2=$sender&avatar2=${GamePigeonUtils.getAvatar()}&game=checkers&game_name=Checkers&mode=$mode&num=1&build=pfvgT"
        )
    }

    fun nextTurnUrl(): String {
        data["num"] = (data["num"]?.toInt()!! + 1).toString()

        var url = "?"
        for (param in queryParams) {
            if (data.contains(param)) {
                url += "${param}=${data[param]}&"
            }
        }
        url = url.substring(0, url.length-1) //strip last &
        return GamePigeonUtils.encodeToUrl(url)
    }

    fun setReplay(replay: String): CheckersData {
        data["replay"] = replay
        return this
    }
}

object GamePigeonUtils {

    fun parseArgsFromUrl(url: String): HashMap<String, String> {
        val data: HashMap<String, String> = java.util.HashMap()
        for (param in url.substring(1).split("&")) {
            val spl = param.split("=")
            data[spl[0]] = spl[1]
        }
        return data
    }

    fun decodeFromUrl(url: String): String {
        return URLDecoder.decode(decrypt(URLDecoder.decode(url.split("&data=")[1], "UTF-8")), "UTF-8")
    }

    fun encodeToUrl(gpData: String): String {
        return "data:?ver=51&data=" + URLEncoder.encode(encrypt(gpData.replace("|", "%7C").replace(" ", "%20")), "UTF-8");
    }

    fun extractReplay(gpData: String): String? {
        if (gpData.contains("&replay=")) {
            return gpData.split("&replay=")[1].split("&")[0]
        }
        return null
    }

    fun getAvatar(): String {
        return "body,5|eyes,5|mouth,2|acc,0|wins,0|bg_color,0.675581,0.779119,0.420136|body_color,1.000000,0.745098,0.600000|glasses,0|stache,0|backdrop,0|hair,12|clothes,2|hair_color,0.000000,0.000000,0.000000|clothes_color,0.552489,0.654599,0.741282"
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