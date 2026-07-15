package com.midiaindoor.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val acao = intent.action
        if (acao == Intent.ACTION_BOOT_COMPLETED || acao == "android.intent.action.QUICKBOOT_POWERON") {
            val abrirApp = Intent(context, MainActivity::class.java)
            abrirApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(abrirApp)
        }
    }
}
