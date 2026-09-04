package org.ohdj.nfcaimereader

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass
import org.ohdj.nfcaimereader.data.repository.SettingsRepositoryImpl

lateinit var nfcApp: NfcAimeReaderApplication

class NfcAimeReaderApplication : Application() {

    companion object {
        fun setEnableOnBackInvokedCallback(appInfo: ApplicationInfo, enable: Boolean) {
            runCatching {
                val applicationInfoClass = ApplicationInfo::class.java
                val method = applicationInfoClass.getDeclaredMethod("setEnableOnBackInvokedCallback", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(appInfo, enable)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        nfcApp = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val enable = SettingsRepositoryImpl().enablePredictiveBack
            HiddenApiBypass.addHiddenApiExemptions("Landroid/content/pm/ApplicationInfo;->setEnableOnBackInvokedCallback")
            setEnableOnBackInvokedCallback(applicationInfo, enable)
        }
    }
}
