package com.edison.qrcodes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.android.DIYQRCodeActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.income.AdMgr;
import com.yzq.zxinglibrary.income.IAdmobRequestListener;

/**
 * @author: yzq
 * @date: 2017/10/26 15:17
 * @declare :
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scanBtn;
    private TextView result;
    private Toolbar toolbar;
    private int REQUEST_CODE_SCAN = 111;
    private NativeAd mNativeAD;
    private boolean mIsPermit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result = findViewById(R.id.result);
        scanBtn = findViewById(R.id.scanBtn);
        findViewById(R.id.diyBtn).setOnClickListener(this);
        scanBtn.setOnClickListener(this);
        result.setOnClickListener(this);
        MobileAds.initialize(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsPermit) {
            initScanner();
        }
    }

    private void initScanner(){
        AndPermission.with(this)
                .runtime()
                .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                .onGranted(data -> {
                    mIsPermit = true;
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    /*ZxingConfig是配置类
                     *可以设置是否显示底部布局，闪光灯，相册，
                     * 是否播放提示音  震动
                     * 设置扫描框颜色等
                     * 也可以不传这个参数
                     * */
                    ZxingConfig config = new ZxingConfig();
                    // config.setPlayBeep(false);//是否播放扫描声音 默认为true
                    //  config.setShake(false);//是否震动  默认为true
                    // config.setDecodeBarCode(false);//是否扫描条形码 默认为true
//                                config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
//                                config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
//                                config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
                    config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
                    intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                    startActivityForResult(intent, REQUEST_CODE_SCAN);
                })
                .onDenied(data -> {
                    showAlterDialog();
                })
                .start();
    }

    private void showAlterDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.need_permission_toast));
        //点击对话框以外的区域是否让对话框消失
        builder.setCancelable(false);
        //设置正面按钮
        builder.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri packageURI = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        //设置反面按钮
        builder.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        //显示对话框
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scanBtn:
                initScanner();
                break;
            case R.id.result:
                if (!TextUtils.isEmpty(result.getText())) {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("Label", result.getText());
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(this,getString(R.string.str_copy_it),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.diyBtn:
                Intent intent = new Intent(this, DIYQRCodeActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void initAD(){
        String adId = "ca-app-pub-7094078041880308/8708769382";
        if (BuildConfig.DEBUG){
            adId = "ca-app-pub-3940256099942544/2247696110";
        }

        AdMgr.INSTANCE.requestNativeAd(this, adId, new IAdmobRequestListener() {
            @Override
            public void onLoadSuccess(NativeAd adObject) {
                mNativeAD = adObject;
                AdMgr.INSTANCE.populateNativeAdViewFix(mNativeAD,getLayoutInflater(),findViewById(R.id.fl_ad));
            }

            @Override
            public void onLoadFailed(int errorCode, @NonNull String errorMsg) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNativeAD != null){
            mNativeAD.destroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                initAD();
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                result.setText(content);
            }
        }
    }

}
