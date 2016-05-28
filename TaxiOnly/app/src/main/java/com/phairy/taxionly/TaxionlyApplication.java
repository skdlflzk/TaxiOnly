package com.phairy.taxionly;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Environment;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import org.apache.log4j.Level;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by aa on 2016-05-28.
 */
public class TaxionlyApplication extends Application {


    /** onCreate()
     * 액티비티, 리시버, 서비스가 생성되기전 어플리케이션이 시작 중일때
     * Application onCreate() 메서드가 만들어 진다고 나와 있습니다.
     * by. Developer 사이트
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        try {


        LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(Environment.getExternalStorageDirectory() + "/TaxiOnly/Logs/logFile.log");
        logConfigurator.setMaxFileSize(512 * 1024);
        logConfigurator.setUseLogCatAppender(true);
            logConfigurator.setRootLevel(Level.DEBUG);



            logConfigurator.configure();
        } catch (Exception e) {

        }
    }

    /**
     * onConfigurationChanged()
     * 컴포넌트가 실행되는 동안 단말의 화면이 바뀌면 시스템이 실행 한다.
     */

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
