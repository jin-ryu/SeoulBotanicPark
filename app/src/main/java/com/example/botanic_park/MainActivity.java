package com.example.botanic_park;

import android.app.Activity;
import android.content.Intent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;
import com.example.botanic_park.PlantSearch.Fragment_Plant_Book;
import com.example.botanic_park.PlantSearch.PlantBookItem;

import java.util.ArrayList;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    private Fragment_Home fragment_Home;
    private Fragment_Map fragment_Map;
    private Fragment_Plant_Book fragment_Plant_Book;
    private Fragment_Information fragment_Information;

    private ArrayList<PlantBookItem> list;

    private CurveBottomBar curveBottomBar;

    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 로딩 액티비티에서 파싱해온 정보 받음
        final Intent intent = getIntent();
        list = (ArrayList<PlantBookItem>) intent.getSerializableExtra(LoadingActivity.PLANT_LIST_KEY);

        //상태 바 색 바꿔줌
        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        // getWindow().setStatusBarColor(Color.parseColor("#FAFAFA"));
        setContentView(R.layout.activity_main);

        // 프래그먼트 객체 생성
        fragment_Home = new Fragment_Home();
        fragment_Map = new Fragment_Map();
        fragment_Plant_Book = Fragment_Plant_Book.newInstance(list);
        fragment_Information = new Fragment_Information();

        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_container, fragment_Home).commitAllowingStateLoss();

        // 하단 메뉴 설정
        floatingActionButton  = findViewById(R.id.floating_action_button);
        floatingActionButton .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(MainActivity.this, QRPopUpActivity.class);
                startActivity(intent1); // QR 액티비티 띄움
            }
        });

        curveBottomBar = findViewById(R.id.customBottomBar);
        curveBottomBar.inflateMenu(R.menu.navigation);
        curveBottomBar.setOnNavigationItemSelectedListener(new ItemSelectedListener());

        // 지도
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature: info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Fragment_Plant_Book에서 작성된 메소드 사용
        // 프레그먼트에서 이 메소드를 override하면 동작하지 않음
        fragment_Plant_Book.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // 하단 메뉴 선택 리스너
    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener{
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            transaction = fragmentManager.beginTransaction();

            switch(menuItem.getItemId())
            {
                case R.id.home:
                    transaction.replace(R.id.frame_container, fragment_Home).commitAllowingStateLoss();
                    break;
                case R.id.map:
                    transaction.replace(R.id.frame_container, fragment_Map).commitAllowingStateLoss();
                    break;
                case R.id.plant_book:
                    transaction.replace(R.id.frame_container, fragment_Plant_Book).commitAllowingStateLoss();
                    break;
                case R.id.information:
                    transaction.replace(R.id.frame_container, fragment_Information).commitAllowingStateLoss();
                    break;
            }
            return true;
        }
    }

    public void setCurveBottomBarVisibility() {
        if (curveBottomBar.isShown())
        {
            curveBottomBar.setVisibility(View.GONE);
            floatingActionButton.hide();
        }
        else
        {
            curveBottomBar.setVisibility(View.VISIBLE);
            floatingActionButton.show();
        }
    }
}

