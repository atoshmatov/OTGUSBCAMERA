package uz.toshmatov.otg_usb_camera.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import uz.toshmatov.otg_usb_camera.ui.MainActivity
import uz.toshmatov.otg_usb_camera.ui.MainViewModel
import uz.toshmatov.strem_lib.StreamService

val appModule = module {
    single { MainActivity::class.java }
    viewModel { MainViewModel() }
    factory { StreamService() }
}