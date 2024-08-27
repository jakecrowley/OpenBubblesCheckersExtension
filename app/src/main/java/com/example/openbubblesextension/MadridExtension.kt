package com.example.openbubblesextension

import android.R.id
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.provider.SyncStateContract.Constants
import android.util.Base64
import android.util.Log
import android.widget.RemoteViews
import com.bluebubbles.messaging.IKeyboardHandle
import com.bluebubbles.messaging.IMadridExtension
import com.bluebubbles.messaging.IMessageViewHandle
import com.bluebubbles.messaging.ITaskCompleteCallback
import com.bluebubbles.messaging.IViewUpdateCallback
import com.bluebubbles.messaging.MadridMessage
import java.net.URLDecoder


class MadridExtension(private val context: Context) : IMadridExtension.Stub() {

    companion object {
        var currentKeyboardHandle: IKeyboardHandle? = null
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
//        val intent = Intent(context, MessageActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        context.startActivity(intent)
        handle?.lock()
        Log.i("here", message!!.caption)

        Log.i("gamepidgeon", message.url);

        var decryptedGPData = GamePidgeon.decodeFromUrl(message.url);
        Log.i("gamepidgeon", decryptedGPData);

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