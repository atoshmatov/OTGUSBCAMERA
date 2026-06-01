package uz.toshmatov.otg_usb_camera.di

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import uz.toshmatov.otg_usb_camera.ui.main.MainViewModel
import uz.toshmatov.otg_usb_camera.ui.rtsp.RtspViewModel
import uz.toshmatov.otg_usb_camera.ui.settings.SettingsViewModel

val appModule = module {
    viewModel { MainViewModel(androidApplication()) }
    viewModel { RtspViewModel() }
    viewModel { SettingsViewModel() }
}
