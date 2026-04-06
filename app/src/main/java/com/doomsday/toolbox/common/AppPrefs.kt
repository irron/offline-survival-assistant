package com.doomsday.toolbox.common

import android.content.Context

class AppPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("doomsday_toolbox", Context.MODE_PRIVATE)

    var disclaimerAccepted: Boolean
        get() = prefs.getBoolean("disclaimer_accepted", false)
        set(value) = prefs.edit().putBoolean("disclaimer_accepted", value).apply()

    var activeModelConfigPath: String?
        get() = prefs.getString("active_model_config_path", null)
        set(value) = prefs.edit().putString("active_model_config_path", value).apply()
}
