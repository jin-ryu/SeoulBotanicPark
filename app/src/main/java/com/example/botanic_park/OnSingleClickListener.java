package com.example.botanic_park;

import android.os.SystemClock;
import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener {
    // 중복 클릭 방지 시간 설정
    private static final long MIN_CLICK_INTERVAL=600;

    private long mLastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < MIN_CLICK_INTERVAL) {

            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        // 중복 클릭아 아니라면 추상함수 호출
        onSingleClick(v);
    }

}