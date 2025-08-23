package com.rk.terminal

import android.app.Application
import android.os.Build
import android.os.StrictMode
import com.github.anrwatchdog.ANRWatchDog
import com.rk.libcommons.application
import com.rk.settings.SettingsManager
import com.rk.resources.Res
import com.rk.update.UpdateManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors


class App : Application() {

    @OptIn(DelicateCoroutinesApi::class)
    companion object {
        fun getTempDir(): File {
            val tmp = File(application!!.filesDir.parentFile, "tmp")
            if (!tmp.exists()) {
                tmp.mkdir()
            }
            return tmp
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        application = this
        Res.application = this
        
        // Initialize the new settings manager
        SettingsManager.initialize()

        GlobalScope.launch(Dispatchers.IO) {
            getTempDir().apply {
                if (exists() && listFiles().isNullOrEmpty().not()){ deleteRecursively() }
            }
        }

        //Thread.setDefaultUncaughtExceptionHandler(CrashHandler)
        ANRWatchDog().start()

        UpdateManager().onUpdate()

        if (BuildConfig.DEBUG){
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder().apply {
                    detectAll()
                    penaltyLog()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                        penaltyListener(Executors.newSingleThreadExecutor()) { violation ->
                            println(violation.message)
                            violation.printStackTrace()
                            violation.cause?.let { throw it }
                            println("vm policy error")
                        }
                    }
                }.build()
            )
        }





    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

}
