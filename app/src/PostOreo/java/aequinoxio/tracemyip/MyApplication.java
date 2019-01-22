package aequinoxio.tracemyip;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import org.acra.*;
import org.acra.annotation.*;

@AcraMailSender(mailTo = "aequinoxio@gmail.com",reportAsFile = true,reportFileName = "CrashReport.txt")
//@AcraDialog(reportDialogClass = SendLog.class, resTheme = R.style.AppTheme_Dialog)
@AcraToast(resText= R.string.app_name, length = Toast.LENGTH_LONG)
@AcraCore(buildConfigClass = BuildConfig.class)

//@ReportsCrashes(
//        customReportContent = {
//                // Campi obblicatori per acralyzer
//                ReportField.REPORT_ID, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION,ReportField.PACKAGE_NAME,ReportField.BUILD, ReportField.STACK_TRACE,
//
//                // Campo per sapere il cellulare su cui è installato
//                ReportField.INSTALLATION_ID,
//
//                // Campi utili per avere altre info
//                ReportField.PHONE_MODEL,ReportField.CUSTOM_DATA, ReportField.LOGCAT, ReportField.SETTINGS_GLOBAL, ReportField.DEVICE_FEATURES,
//                ReportField.SETTINGS_SECURE, ReportField.SETTINGS_SYSTEM, ReportField.SHARED_PREFERENCES, ReportField.THREAD_DETAILS
//        },
//)


public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
