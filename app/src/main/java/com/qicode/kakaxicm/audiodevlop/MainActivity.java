package com.qicode.kakaxicm.audiodevlop;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
    }


    private final String[] pers = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private void requestPermission() {
        boolean b = checkPermissions();
        if (!b) {
            //权限判断，没有就去请求所需权限，传参 需要申请的权限(可以多个)， requestCode请求码用于结果回调里判断
            ActivityCompat.requestPermissions(this, pers, 1314);
        } else {
            //有权限
        }
    }

    private boolean checkPermissions() {
        boolean b = true;
        for (int i = 0; i < pers.length; i++) {
            if (ContextCompat.checkSelfPermission(this, pers[i]) != PackageManager.PERMISSION_GRANTED) {
                b = false;
                break;
            }
        }
        return b;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (1314 == requestCode) {
            for (int x : grantResults) {
                if (x == PackageManager.PERMISSION_DENIED) {
                    //权限拒绝了
                    return;
                }
            }
        }
    }
}
