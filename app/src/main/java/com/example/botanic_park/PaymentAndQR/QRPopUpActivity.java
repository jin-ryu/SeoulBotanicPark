package com.example.botanic_park.PaymentAndQR;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import com.example.botanic_park.AppManager;
import com.example.botanic_park.OnSingleClickListener;
import com.example.botanic_park.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.*;

public class QRPopUpActivity extends Activity {

    private ImageView iv;
    private WindowManager.LayoutParams params;
    private float brightness;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 타이틀바 없애기
        setContentView(R.layout.activity_qr_pop_up);
        params = getWindow().getAttributes();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        ((TextView)findViewById(R.id.limitTimeTextView)).setText(String.valueOf(AppManager.getInstance().getMainActivity().limitTime) + ":00 입장마감");

        ImageView closeBtn = findViewById(R.id.close);
        closeBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) { finish();   // 액티비티 닫음
            }
        });

        TimeZone jst = TimeZone.getTimeZone("Asia/Seoul");
        Calendar calendar = Calendar.getInstance(jst);

        int limitTime = AppManager.getInstance().getMainActivity().limitTime;
        int hour = calendar.get ( Calendar.HOUR_OF_DAY );

        iv = (ImageView) findViewById(R.id.qrcode);

        TextView date = findViewById(R.id.date);
        Date today = Calendar.getInstance().getTime();
        date.setText(new SimpleDateFormat("MM/dd (EE)", Locale.getDefault()).format(today));


        if(hour < limitTime) {
            try {
                Random rnd = new Random();
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                BitMatrix bitMatrix = multiFormatWriter.encode(String.valueOf(rnd.nextInt()), BarcodeFormat.QR_CODE, 300, 300);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                iv.setImageBitmap(bitmap);

            } catch (Exception e) {
            }

            Toast.makeText(getApplicationContext(), "QR화면은 캡처가 불가합니다.", Toast.LENGTH_SHORT).show();
        }

        else
        {
            iv.setVisibility(View.INVISIBLE);
            ((LinearLayout)findViewById(R.id.close_announcement)).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        brightness = params.screenBrightness;
        // 최대 밝기로 설정
        params.screenBrightness = 1f;
        // 밝기 설정 적용
        getWindow().setAttributes(params);
    }

    @Override protected void onPause() {
        super.onPause();

        // 기존 밝기로 변경
        params.screenBrightness = brightness;
        getWindow().setAttributes(params);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

}