package com.tigernum.app

import android.app.Application
import com.tigernum.app.util.security.EmulatorDetector
import com.tigernum.app.util.security.RootDetector
import com.tigernum.app.util.security.TamperDetector

class TigerNumApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // فحوصات أمنية أساسية (يمكن إظهار تحذير أو إنهاء التطبيق في الإصدار النهائي)
        val isRooted = RootDetector.isDeviceRooted()
        val isEmulator = EmulatorDetector.isEmulator()
        val isTampered = TamperDetector.isAppTampered(this)

        if (isRooted || isEmulator || isTampered) {
            // في الإصدار النهائي: عرض رسالة تحذيرية أو إنهاء التطبيق
            // حاليًا: نكتب في السجل فقط
            android.util.Log.w("TigerNumApp", "تحذير أمني: root=$isRooted, emulator=$isEmulator, tampered=$isTampered")
        }
    }

    companion object {
        lateinit var instance: TigerNumApp
            private set
    }
}
