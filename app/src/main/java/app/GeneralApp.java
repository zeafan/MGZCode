package app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class GeneralApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
       String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().name("realm_data").directory(new File(rootPath+"/realm/")).build();
        Realm.setDefaultConfiguration(realmConfiguration);
        Log.d("opertaion","Init Realm Successfull");
    }
}
