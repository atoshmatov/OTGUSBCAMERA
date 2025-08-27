package uz.toshmatov.otg_usb_camera.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import uz.toshmatov.otg_usb_camera.di.appModule

class UvcApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@UvcApp)
            modules(appModule)
        }
    }
}