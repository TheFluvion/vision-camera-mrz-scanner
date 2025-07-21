package com.visioncameramrzscanner

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

import java.util.ArrayList
import java.util.Collections
import java.util.List

import com.mrousavy.camera.frameprocessors.VisionCameraProxy

import android.util.Log
import com.facebook.react.bridge.UiThreadUtil
class MrzScannerInitializerModule(private val reactContext: ReactApplicationContext) : NativeModule {

    private val TAG = "MRZScannerModule"
    override fun getName(): String = "VisionCameraMrzScannerInitializer"

    override fun initialize() {
        Log.d(TAG, "MrzScannerInitializerModule.initialize() called!")
        Log.d(TAG, "Is on UI Thread: ${UiThreadUtil.isOnUiThread()}") // Verifica el hilo
        Log.d(TAG, "ReactContext: $reactContext") // Muestra la instancia del contexto
        Log.d(TAG, "CatalystInstance: ${reactContext.catalystInstance}") // ¡CRÍTICO! Verifica si CatalystInstance es nulo

        if (reactContext.catalystInstance == null) {
            Log.e(TAG, "CatalystInstance is NULL when initializing VisionCameraMrzScannerPluginV4! __scanMRZ will not be available.")
        } else {
            try {
                val proxy = VisionCameraProxy(reactContext)
                Log.d(TAG, "VisionCameraProxy created successfully.")
                VisionCameraMrzScannerPluginV4.install(proxy)
                Log.d(TAG, "VisionCameraMrzScannerPluginV4.install() called successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error installing VisionCameraMrzScannerPluginV4: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    override fun onCatalystInstanceDestroy() {
        Log.d(TAG, "MrzScannerInitializerModule.onCatalystInstanceDestroy() called.")
    }

    override fun invalidate() {
        Log.d(TAG, "MrzScannerInitializerModule.invalidate() called.")
    }

    override fun canOverrideExistingModule(): Boolean = false
}

class VisionCameraMrzScannerPackage : ReactPackage {

    @Suppress("unused")
    override fun createViewManagers(reactContext: ReactApplicationContext): kotlin.collections.List<ViewManager<*, *>?> {
        return Collections.emptyList()
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): kotlin.collections.List<NativeModule?> {
        val modules = ArrayList<NativeModule>()
        modules.add(MrzScannerInitializerModule(reactContext))
        return modules
    }
}