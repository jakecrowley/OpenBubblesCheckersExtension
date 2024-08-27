package com.example.openbubblesextension

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.bluebubbles.messaging.MadridMessage
import java.io.ByteArrayOutputStream
import java.util.UUID


class KeyboardClickReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        val bm = BitmapFactory.decodeResource(context.resources, R.drawable.my_image)
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val b = baos.toByteArray()
        val imageEncoded: String = Base64.encodeToString(b, Base64.NO_WRAP)


        val message = MadridMessage().apply {
            messageGuid = UUID.randomUUID().toString()
            ldText = "Checkers"
            url = "data:?ver=51&data=es-Bvcecg0ier30-2v319td%26x%25_-.%2C0%2Crkdarae0m47wt%3De%3Dg%26%3Dd%3Dl7n400l1otlsocf1tl1e%2C%25_ts6%2Cs.0.02y.eg50pr%254B0.Cor7s_0vs005302eC0%3D%2Cla5%26Co%2CC0c5_p7s00%26a1009%250eml009ycCaA%3De0%3D4s59o9A5kh72099y9L%26a7n7%3Fk%2C9%3Dmx.%26hh%3DL%3Duceab725Tacy%254a%265%267M94lD1u%25p0DCt2TiE%25y07200o0r%2C10B6C0Llrm1C8-%270.r%2CG07070o%2C8Co.s%2509h227tCe%26bnp%2642sga%2Cn6lCGo.rlh-2tah1s.B01a6lBa%2CoEoC7rrei89_80B%255.%267Be9Co55.i03aei%252tt.%2Cc37HLch3nR0%2C%25yt2ri%2C66cb%2630%3D%3Dmi-HB47n0ner7e%3DmpdC%2C%2C%2C44nBoo00nc07am007607s0r%2653rc4cpaic701e7o%257.sbe%2Ccdh55Tb4cho6%2CgreCA%2C%3D3c%2C-ulk0Ce%3Ds%2522a4e1%25d0sv9B%21dv1osc%26-r79e9A0A%25oC"
            session = UUID.randomUUID().toString()
            imageBase64 = imageEncoded
            caption = "Let's play Checkers!"
            isLive = true
        }

        MadridExtension.currentKeyboardHandle?.addMessage(message)
    }
}