package com.example.botanic_park.Help;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.example.botanic_park.R;
import me.relex.circleindicator.CircleIndicator;

public class HelpActivity extends AppCompatActivity {
    public static String HELP_CODE = "help code";
    public static final int HELP_TODAY_PLANT = 0;
    FragmentPagerAdapter adapter;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_tabs);

        ViewPager viewPager = findViewById(R.id.viewPager);
        adapter = new PagerAdapter(getSupportFragmentManager(), 3);
        viewPager.setAdapter(adapter);

        CircleIndicator indicator = findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);

        Intent intent = getIntent();
        int helpCode = intent.getIntExtra(HELP_CODE,0);
        switch (helpCode){
            case HELP_TODAY_PLANT:
                title = "\"오늘의 식물\"";
                break;
        }

        TextView textView = findViewById(R.id.title);
        textView.setText(title);

        Button skipBtn = findViewById(R.id.skip_btn);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public static class PagerAdapter extends FragmentPagerAdapter {
        int count = 0;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public PagerAdapter(FragmentManager fm, int count) {
            super(fm);
            this.count = count;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return HelpFragment.newInstance(1);
                case 1:
                    return HelpFragment.newInstance(2);
                case 2:
                    return HelpFragment.newInstance(3);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return count;
        }
    }
}

