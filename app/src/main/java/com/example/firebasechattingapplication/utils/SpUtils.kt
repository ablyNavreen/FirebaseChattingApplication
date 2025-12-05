package com.example.firebasechattingapplication

import android.content.Context

class SpUtils {
    companion object{

            fun cleanPref(context: Context) {
                val settings = context.getSharedPreferences(Constants.SP_Name, Context.MODE_PRIVATE)
                settings.edit().clear().apply()
            }

            fun saveString(context: Context, key: String?, value: String?) {
                val settings = context.getSharedPreferences(Constants.SP_Name, Context.MODE_PRIVATE)
                val editor = settings.edit()
                editor.putString(key, value)
                editor.apply()
            }

            fun getString(context: Context, key: String?): String? {
                return getStrings(context, key, null)
            };

            fun getStrings(context : Context, key: String?, defaultVal: String?): String? {
                val settings = context.getSharedPreferences(Constants.SP_Name, Context.MODE_PRIVATE)
                return settings.getString(key, defaultVal)
            }
        }
    }
