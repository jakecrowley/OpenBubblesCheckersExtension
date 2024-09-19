package com.example.openbubblesextension

import android.app.Activity
import android.content.Intent
import android.util.Log
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot


/**
 * Runtime [GodotPlugin] used to enable interaction with the Godot gdscript logic.
 */
class AppPlugin(godot: Godot, val intent: Intent, val _activity: Activity) : GodotPlugin(godot) {

    private var replay = "";
    private var player = -1;

    companion object {
        val SET_REPLAY_SIGNAL = SignalInfo("set_replay", String::class.java)
    }

    override fun getPluginName() = "AppPlugin"

    override fun getPluginSignals() = setOf(SET_REPLAY_SIGNAL)

    override fun onGodotMainLoopStarted() {
        Log.i("gamepigeon", "IM READY IM READY");
        emitSignal(SET_REPLAY_SIGNAL.name, replay)
        super.onGodotMainLoopStarted()
    }

    @UsedByGodot
    fun sendReplay(replay: String) {
        Log.d("gamepigeon", "sendReplay: $replay")

        val intent = Intent("com.example.openbubblesextension.GAME_DATA")
        intent.putExtra("send_replay", replay);
        _activity.sendBroadcast(intent)
        _activity.finish()
    }

    @UsedByGodot
    fun getPlayer(): Int {
        return this.player
    }

    fun setPlayer(player: Int) {
        this.player = player
    }

    /**
     * Used to emit a signal to the gdscript logic to update the gltf being shown.
     *
     * @param glbFilepath Filepath of the gltf asset to be shown
     */
    internal fun setReplay(replay: String) {
        this.replay = replay
        Log.i("gamepigeon", "Set replay: $this.replay")
    }
}