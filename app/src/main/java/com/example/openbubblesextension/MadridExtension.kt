package com.example.openbubblesextension

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.RemoteViews

import com.bluebubbles.messaging.IKeyboardHandle
import com.bluebubbles.messaging.IMadridExtension
import com.bluebubbles.messaging.IMessageViewHandle
import com.bluebubbles.messaging.IViewUpdateCallback
import com.bluebubbles.messaging.MadridMessage


class MadridExtension(private val context: Context) : IMadridExtension.Stub() {

    companion object {
        var currentKeyboardHandle: IKeyboardHandle? = null
        var currentGameData: CheckersData? = null
        var currentMessageHandle: IMessageViewHandle? = null
        var currentMessage: MadridMessage? = null
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
        if (handle == null || message == null) {
            Log.e("gamepigeon", "Uh oh handle or message is null!")
            return
        }

        handle.lock()

        if (message.url == null) {
            Log.e("gamepigeon", "MadridMessage tapped but message url was null?")
            return
        }

        val oldUrl = message.url
        Log.i("gamepigeon", "Message URL: $oldUrl")

        val decryptedGPString = GamePigeonUtils.decodeFromUrl(message.url)
        Log.i("gamepigeon", "Deobfed GP Data URL: $decryptedGPString")

        val gameData = CheckersData(decryptedGPString)

        if (!gameData.isTurn()) {
            Log.i("gamepigeon", "IT IS NOT YOUR TURN")
            return
        }

        val replay = gameData.getReplay()
        Log.i("gamepigeon", "GamePigeon Replay: $replay")

        currentMessage = message
        currentGameData = gameData
        currentMessageHandle = handle

        val intent = Intent(context, CheckersActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("replay", replay)
        intent.putExtra("player", gameData.getPlayer())
        context.startActivity(intent)
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