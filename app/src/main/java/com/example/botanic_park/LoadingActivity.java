package com.example.botanic_park;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.botanic_park.PlantSearch.PlantBookItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class LoadingActivity extends AppCompatActivity {
    public static final String PLANT_LIST_KEY = "plant list";
    public static final int REQUEST_CODE = 4000;
    ArrayList<PlantBookItem> list = null;
    ParsePlantTask parsePlantTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //상태 바 색 바꿔줌
        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.parseColor("#FAFAFA"));
        setContentView(R.layout.activity_loading);
        AppManager.getInstance().setLoadingActivity(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView loading = findViewById(R.id.loading);
        Glide.with(this).load(R.drawable.loading).into(loading);

        list = onSearchData("list");  // 기존 저장 정보 가져옴
        if(list == null)
            list = getListFromFile();  // 초기 파일 가져옴

        // 오늘의 식물 가져옴
        AppManager.getInstance().setPlantsToday(onSearchData("plant today"));

        parsePlantTask = new ParsePlantTask(list);
        parsePlantTask.execute(); // AsyncTask 작동시킴(파싱)
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        parsePlantTask.cancel(true);
    }

    private void onClearData() {
        SharedPreferences sp = getSharedPreferences("Botanic Park", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    // 식물 list 가져옴
    private ArrayList<PlantBookItem> onSearchData(String tag) {
        SharedPreferences sp = getSharedPreferences("Botanic Park", MODE_PRIVATE);
        String strList = sp.getString(tag, "");

        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<ArrayList<PlantBookItem>>() {
        }.getType();

        ArrayList<PlantBookItem> list = gson.fromJson(strList, listType);

        return list;
    }


    private ArrayList<PlantBookItem> getListFromFile(){
        String strList = null;
        try {
            // list text file 읽어옴
            Resources resources = getResources();
            InputStream inputStream = resources.openRawResource(R.raw.list);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            strList = new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<ArrayList<PlantBookItem>>() {
        }.getType();

        ArrayList<PlantBookItem> list = gson.fromJson(strList, listType);

        return list;
    }

    /* 웹에서 식물 정보 긁어오는 클래스 */
    public class ParsePlantTask extends AsyncTask<Void, Void, Void> {
        String PLANT_LIST_URL = "https://botanicpark.seoul.go.kr/front/plants/plantsIntro.do";
        String IMAGE_START_URL = "https://botanicpark.seoul.go.kr";
        String PLANT_DETAIL_URL = "https://botanicpark.seoul.go.kr/front/plants/plantsIntroView.do";
        String ID = "?plt_sn=";     // 식물 식별 id
        String PAGE = "&page=";     // 페이지

        ArrayList<PlantBookItem> list;

        public ParsePlantTask(ArrayList<PlantBookItem> list) {
            this.list = list;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (list == null) {
                // 저장된 정보가 없는 경우 파싱
                list = new ArrayList<>();
                for (int id = 121; id >= 1; id--) {
                    String DETAIL_PAGE_URL = PLANT_DETAIL_URL + ID + id;

                    // 인증서 있는 홈페이지를 인증서 없이도 연결 가능하게 설정
                    SSLConnect sslConnect = new SSLConnect();
                    sslConnect.postHttps(DETAIL_PAGE_URL, 1000, 1000);

                    // 웹에서 정보 읽어옴
                    Document document = null;
                    try {
                        document = Jsoup.connect(DETAIL_PAGE_URL).get();
                        Elements textElements = document.select("div[class=text_area]").select("span");

                        String imgUrl = document.select("div[class=img_area]").select("img").attr("src");

                        String name_ko = textElements.get(0).text();
                        name_ko = name_ko.replace("이 름:", "");

                        String name_sc = textElements.get(1).text();
                        name_sc = name_sc.replace("학 명:", "");

                        String name_en = textElements.get(2).text();
                        name_en = name_en.replace("영 명:", "");

                        String type = textElements.get(3).text();
                        type = type.replace("구 분:", "");

                        String blossom = textElements.get(4).text();
                        blossom = blossom.replace("개화기:", "");

                        String details = document.select("div[class=text_editor]").text();

                        // 리스트에 추가
                        list.add(new PlantBookItem(id, IMAGE_START_URL + imgUrl, name_ko, name_sc, name_en, type, blossom, details));
                        Log.d("테스트", "id: " + id);
                    } catch (IOException e) {
                        continue;   // 7번만 invalid Mark로 안옴
                    }
                }
            } else {
                // 저장된 정보가 있는 경우
                try {
                    Thread.sleep(2100);
                    Log.d("테스트", list.size() + "");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            finish();   // 파싱 끝나면 로딩 액티비티 종료

            int count = 0;
            for(PlantBookItem item: list){
                if(item.isCollected())
                    count++;
            }
            // 앱메니저에 저장
            AppManager.getInstance().collectionCount = count;
            AppManager.getInstance().setList(list);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

            onSaveData(list);
        }

        // 식물 list 저장
        private void onSaveData(ArrayList<PlantBookItem> list) {
            Gson gson = new GsonBuilder().create();
            Type listType = new TypeToken<ArrayList<PlantBookItem>>() {
            }.getType();
            String json = gson.toJson(list, listType);  // arraylist -> json string

            SharedPreferences sp = getSharedPreferences("Botanic Park", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("list", json); // JSON으로 변환한 객체를 저장한다.
            editor.commit(); // 완료한다.
        }
    }
}


