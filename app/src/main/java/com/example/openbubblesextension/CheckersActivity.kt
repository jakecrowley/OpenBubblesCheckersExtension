package com.example.openbubblesextension

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import org.godotengine.godot.Godot
import org.godotengine.godot.GodotActivity
import org.godotengine.godot.plugin.GodotPlugin
import kotlinx.coroutines.launch


class CheckersActivity : GodotActivity() {
    internal var appPlugin: AppPlugin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkers)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setReplay(intent.getStringExtra("replay")!!)
    }

    private fun getOrCreateAppPlugin() {
        if (appPlugin == null) {
            appPlugin = AppPlugin(godot!!, intent, this)
        }
    }

    private fun setReplay(replay: String) {
        getOrCreateAppPlugin()
        appPlugin!!.setReplay(replay)
    }

    override fun getHostPlugins(godot: Godot): Set<GodotPlugin> {
        getOrCreateAppPlugin()
        return setOf(appPlugin!!)
    }
}