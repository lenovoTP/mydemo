package com.tzutalin.dlibtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;

import com.example.xjl.mydemo.R;

public class facecamera extends Activity {
    private static int OVERLAY_PERMISSION_REQ_CODE = 1;
    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮（虽然moto行不通hhh）
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(!Settings.canDrawOverlays(this.getApplicationContext())){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);//这东西如果是moto，华为，vivo还是不要想了
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }
        if (null == savedInstanceState) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, CameraConnectionFragment.newInstance())
                    .commit();
        }
    }
}
