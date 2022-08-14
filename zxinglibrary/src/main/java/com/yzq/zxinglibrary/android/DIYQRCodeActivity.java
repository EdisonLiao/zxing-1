package com.yzq.zxinglibrary.android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.nativead.NativeAd;
import com.yzq.zxinglibrary.BuildConfig;
import com.yzq.zxinglibrary.R;
import com.yzq.zxinglibrary.encode.CodeCreator;
import com.yzq.zxinglibrary.income.AdMgr;
import com.yzq.zxinglibrary.income.IAdmobRequestListener;

public class DIYQRCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText contentEt;
    private ImageView contentIv;
    private NativeAd mNativeAD;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        contentEt = findViewById(R.id.contentEt);
        contentIv = findViewById(R.id.contentIvWithLogo);
        findViewById(R.id.backIv).setOnClickListener(this);
        findViewById(R.id.encodeBtnWithLogo).setOnClickListener(this);
        initAD();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.encodeBtnWithLogo){
            String contentEtString = contentEt.getText().toString().trim();
            if (TextUtils.isEmpty(contentEtString)) {
                Toast.makeText(this, getString(R.string.str_qr_code_input_hint), Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = CodeCreator.createQRCode(contentEtString, 400, 400, null);
            if (bitmap != null) {
                contentIv.setImageBitmap(bitmap);
            }
        }else if (view.getId() == R.id.backIv){
            finish();
        }
    }

    private void initAD(){
        String adId = "ca-app-pub-7094078041880308/8708769382";

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
}
