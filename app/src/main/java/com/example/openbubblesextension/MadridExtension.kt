package com.example.openbubblesextension

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
import com.bluebubbles.messaging.IKeyboardHandle
import com.bluebubbles.messaging.IMadridExtension
import com.bluebubbles.messaging.IMessageViewHandle
import com.bluebubbles.messaging.ITaskCompleteCallback
import com.bluebubbles.messaging.IViewUpdateCallback
import com.bluebubbles.messaging.MadridMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.net.URLDecoder
import java.net.URLEncoder


class MadridExtension(private val context: Context) : IMadridExtension.Stub() {

    companion object {
        var currentKeyboardHandle: IKeyboardHandle? = null
        private var broadcastReceiver: BroadcastReceiver? = null
        private var currentHandle: IMessageViewHandle? = null
    }

    private var callback: IViewUpdateCallback? = null;

    override fun keyboardClosed() {
        currentKeyboardHandle = null
    }

    override fun keyboardOpened(callback: IViewUpdateCallback?, handle: IKeyboardHandle?): RemoteViews {
        this.callback = callback
        var view = RemoteViews(context.packageName, R.layout.keyboard)

        currentKeyboardHandle = handle

        val intentWithData = Intent(
            context,
            KeyboardClickReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(context, 7, intentWithData,
            PendingIntent.FLAG_IMMUTABLE)

        view.setOnClickPendingIntent(R.id.button, pendingIntent)

        return view
    }

    override fun didTapTemplate(message: MadridMessage?, handle: IMessageViewHandle?) {
        handle?.lock()

        val old_url = message!!.url
        Log.i("gamepigeon", old_url);

        val decryptedGPData = GamePigeonUtils.decodeFromUrl(message.url)
        Log.i("gamepigeon", decryptedGPData);

        val replay = GamePigeonUtils.extractReplay(decryptedGPData)
        if (replay != null) {
            Log.i("gamepigeon", replay)
        }

        val intent = Intent(context, CheckersActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("replay", replay)
        context.startActivity(intent)

        Log.d("gamepigeon", "OUTER: " + Thread.currentThread())

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val new_replay = intent.getStringExtra("send_replay") as String
                Log.d("gamepigeon", "I GOT IT $new_replay")

                var dec = GamePigeonUtils.decodeFromUrl(message.url)
                var old_replay = GamePigeonUtils.extractReplay(dec)

                var new_url: String
                if (old_replay != null) {
                    new_url = GamePigeonUtils.encodeToUrl(dec.replace(old_replay, new_replay))
                } else {
                    new_url
                }

                Log.d("gamepigeon", dec)
                Log.d("gamepigeon", new_url)

                message.url = new_url
            }
        }

        val filter = IntentFilter("com.example.openbubblesextension.GAME_DATA");
        registerReceiver(context, broadcastReceiver, filter, RECEIVER_EXPORTED);

        runBlocking {
            while (message.url == old_url) {
                delay(100)
            }
        }

        handle!!.updateMessage(message, object : ITaskCompleteCallback.Stub() {
            override fun complete() {
                Log.i("sent!", "done")
                handle.unlock()
            }
        })
    }

    override fun getLiveView(
        callback: IViewUpdateCallback?,
        message: MadridMessage?,
        handle: IMessageViewHandle?
    ): RemoteViews {
        Log.i("live view", "init")
        var view = RemoteViews(context.packageName, R.layout.livemsg)

        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.my_image)
        view.setImageViewBitmap(R.id.imageView, bitmap)

        return view
    }

    override fun messageUpdated(message: MadridMessage?) {
        Log.i("update", "message");
    }

}