package uz.toshmatov.otg_usb_camera.app

import android.app.Application
import android.util.Log
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import uz.toshmatov.otg_usb_camera.di.appModule

class UvcApp : Application() {
    override fun onCreate() {
        super.onCreate()
        installGlThreadCrashGuard()
        startKoin {
            androidContext(this@UvcApp)
            modules(appModule)
        }
    }

    /**
     * Pedro library OpenGlView GL thread crash prevention.
     *
     * When Camera2 session is invalidated (e.g. USB camera connects/disconnects,
     * device-specific cancelRequest bug), the GL thread calls updateTexImage()
     * on an abandoned SurfaceTexture → IllegalStateException: "Unable to update texture contents".
     * Pedro library doesn't have a try-catch there, so it crashes the app.
     *
     * We intercept ONLY that specific crash on the GL thread and suppress it.
     * All other crashes still go to the default handler.
     */
    private fun installGlThreadCrashGuard() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val isGlTextureCrash =
                thread.name.contains("gl", ignoreCase = true) &&
                    throwable is IllegalStateException &&
                    throwable.message?.contains("texture", ignoreCase = true) == true

            if (isGlTextureCrash) {
                // Known pedro library issue: camera disconnect while GL thread is rendering.
                // The GL thread terminates (exception exits run()), but the app stays alive.
                Log.w("UvcApp", "GL texture crash suppressed (camera disconnect): ${throwable.message}")
            } else {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}