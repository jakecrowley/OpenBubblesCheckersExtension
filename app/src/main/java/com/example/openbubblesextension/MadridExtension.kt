package com.example.openbubblesextension

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
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


class MadridExtension(private val context: Context) : IMadridExtension.Stub() {

    companion object {
        var currentKeyboardHandle: IKeyboardHandle? = null
        private var broadcastReceiver: BroadcastReceiver? = null
    }

    private var callback: IViewUpdateCallback? = null

    override fun keyboardClosed() {
        currentKeyboardHandle = null
    }

    override fun keyboardOpened(callback: IViewUpdateCallback?, handle: IKeyboardHandle?): RemoteViews {
        this.callback = callback
        val view = RemoteViews(context.packageName, R.layout.keyboard)

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
        if (message?.url == null) {
            Log.e("gamepigeon", "MadridMessage tapped but message or url was null?")
            return
        }

        val oldUrl = message.url
        Log.i("gamepigeon", "Message URL: $oldUrl")

        val decryptedGPString = GamePigeonUtils.decodeFromUrl(message.url)
        Log.i("gamepigeon", "Deobfed GP Data URL: $decryptedGPString")

        val gameData = CheckersData(decryptedGPString)

        var replay = gameData.getReplay()
        if (replay != null) {
            Log.i("gamepigeon", "GamePigeon Replay: $replay")
        } else {
            Log.i("gamepigeon", "No replay, first move")
            replay = "board:0,2,0,2,0,2,0,2,2,0,2,0,2,0,2,0,0,2,0,2,0,2,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0|move:-1,-1,-1,-1|board:0,2,0,2,0,2,0,2,2,0,2,0,2,0,2,0,0,2,0,2,0,2,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0"
        }

        val intent = Intent(context, CheckersActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("replay", replay)
        context.startActivity(intent)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val newReplay = intent.getStringExtra("send_replay") as String
                Log.d("gamepigeon", "Got new replay: $newReplay")

                gameData.setReplay(newReplay)
                val newUrl = gameData.nextTurnUrl()

                Log.d("gamepigeon", "New GP Data URL: $newUrl")

                message.url = newUrl
            }
        }

        val filter = IntentFilter("com.example.openbubblesextension.GAME_DATA")
        registerReceiver(context, broadcastReceiver, filter, RECEIVER_EXPORTED)

        //TODO: WORKAROUND remove once OpenBubbles fixes multithreading issue
        runBlocking {
            while (message.url == oldUrl) {
                delay(100)
            }
        }

        handle!!.updateMessage(message, object : ITaskCompleteCallback.Stub() {
            override fun complete() {
                Log.i("gamepigeon", "Sent updated game message.")
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
        val view = RemoteViews(context.packageName, R.layout.livemsg)

        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.my_image)
        view.setImageViewBitmap(R.id.imageView, bitmap)

        return view
    }

    override fun messageUpdated(message: MadridMessage?) {
        Log.i("update", "message")
    }

}