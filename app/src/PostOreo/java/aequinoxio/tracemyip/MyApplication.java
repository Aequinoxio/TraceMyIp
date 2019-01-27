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

public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
