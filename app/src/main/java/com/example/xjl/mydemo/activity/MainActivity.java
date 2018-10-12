package com.example.xjl.mydemo.activity;
/**
 * ......................我佛慈悲....................
 * ......................_oo0oo_.....................
 * .....................o8888888o....................
 * .....................88" . "88....................
 * .....................(| -_- |)....................
 * .....................0\  =  /0....................
 * ...................___/`---'\___..................
 * ..................' \\|     |// '.................
 * ................./ \\|||  :  |||// \..............
 * .............../ _||||| -卍-|||||- \..............
 * ..............|   | \\\  -  /// |   |.............
 * ..............| \_|  ''\---/''  |_/ |.............
 * ..............\  .-\__  '-'  ___/-. /.............
 * ............___'. .'  /--.--\  `. .'___...........
 * .........."" '<  `.___\_<|>_/___.' >' ""..........
 * ........| | :  `- \`.;`\ _ /`;.`/ - ` : | |.......
 * ........\  \ `_.   \_ __\ /__ _/   .-` /  /.......
 * ....=====`-.____`.___ \_____/___.-`___.-'=====....
 * ......................`=---='.....................
 * ..................佛祖开光 ,永无BUG................
 */
import android.content.Intent;

import android.os.Build;

import android.provider.Settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.WindowManager;
import android.widget.Toast;


import com.example.xjl.mydemo.R;

import com.tzutalin.dlibtest.CameraConnectionFragment;

public class MainActivity extends AppCompatActivity {
    private static int OVERLAY_PERMISSION_REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //申请悬浮窗权限，但是除了Google手机都TM用不了QAQ
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this.getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }

        if (null == savedInstanceState) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, CameraConnectionFragment.newInstance())
                    .commit();
        }

//        circularButton1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(animation == null)
//                    //播放动画
//                    animation = AnimationsContainer.getInstance(R.array.loading_anim_smile, 100).createProgressDialogAnim(imageView);
//                if(true){
//                    animation.start();
//                }else {
//                    animation.stop();
//                }
//            }
//        });
//        circularButton2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this, MyCameraActivity.class));
////                getFragmentManager()
////                        .beginTransaction()
////                        .replace(R.id.mycontainer_mod, CameraConnectionFragment.newInstance())
////                        .commit();
//                //想办法把悬浮窗弄过来
//            }
//        });·

    }
    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this.getApplicationContext())) {
                    Toast.makeText(MainActivity.this, "MainActivity\", \"SYSTEM_ALERT_WINDOW, permission not granted...", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            }
        }
    }

}
