package com.example.botanic_park.PlantSearch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.botanic_park.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/* 식물 검색 결과 액티비티 */
public class SearchResultActivity extends AppCompatActivity {
    public static final String RESULT_TYPE = "result type";
    public static final int TEXT_SEARCH = 0;
    public static final int IMAGE_SEARCH = 1;

    ArrayList<PlantBookItem> list;          // 전체 식물 리스트
    ArrayList<PlantBookItem> searchList;    // 검색결과 저장 리스트
    String searchword;

    PlantExpandableListView resultListView;   // 텍스트 검색 시 결과 리스트뷰
    LinearLayout noResult;  // 결과 없음 메세지
    LinearLayout resultItem;    // 이미지 검색 시 결과 아이템

    int resultType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_list);

        Intent intent = getIntent();
        resultType = intent.getIntExtra(RESULT_TYPE, 0);
        list = (ArrayList<PlantBookItem>) intent.getSerializableExtra(Fragment_Plant_Book.PLANT_LIST_KEY);
        searchword = intent.getStringExtra(Fragment_Plant_Book.SEARCH_WORD_KEY);

        resultListView = findViewById(R.id.listview);
        noResult = findViewById(R.id.no_result_message);
        resultItem = findViewById(R.id.content_search_result);

        getSearchResult();
        showSearchResult();
    }

    private void getSearchResult() {
        // 텍스트 검색 결과 리스트
        searchList = new ArrayList<PlantBookItem>();

        if(searchword.isEmpty())
            return;

        // 단어 분할
        String[] words = searchword.split(" ");
        for (PlantBookItem item : list) {
            for(String word : words) {
                if (item.getName_ko().contains(word)
                        || item.getName_en().contains(word)
                        || item.getName_sc().contains(word)) {
                    searchList.add(item);
                }
            }
        }
    }

    private void showSearchResult() {
        if (searchList.size() <= 0) {
            // 검색 결과 없음 메시지 보여줌
            noResult.setVisibility(View.VISIBLE);
            resultListView.setVisibility(View.GONE);
            resultItem.setVisibility(View.GONE);
            return;
        }

        switch (resultType) {
            case TEXT_SEARCH:
                textSearchResult();
                break;
            case IMAGE_SEARCH:
                imageSearchResult();
                break;
        }
    }

    private void imageSearchResult() {
        resultItem.setVisibility(View.VISIBLE);
        resultListView.setVisibility(View.GONE);
        noResult.setVisibility(View.GONE);

        setData(searchList.get(0)); // 첫번재 결과만 보여줌
    }

    private void setData(PlantBookItem selectedItem) {
        // 이미지 결과 검색 보여줌
       TextView name_ko = findViewById(R.id.content_name);
        name_ko.setText(selectedItem.getName_ko());

        TextView name_sc = findViewById(R.id.content_scientific_name);
        name_sc.setText(selectedItem.getName_sc());

        TextView name_en = findViewById(R.id.content_name_en);
        name_en.setText(selectedItem.getName_en());

        TextView type = findViewById(R.id.content_type);
        type.setText(selectedItem.getType());

        TextView blossom = findViewById(R.id.content_blossom);
        blossom.setText(selectedItem.getBlossom());

        TextView details = findViewById(R.id.details);
        details.setText(selectedItem.getDetails());
    }

    private void textSearchResult() {
        // 텍스트 검색 시 결과 리스트로 보여줌
        resultListView.setVisibility(View.VISIBLE);
        noResult.setVisibility(View.GONE);
        resultItem.setVisibility(View.GONE);

        PlantBookAdapter adapter = new PlantBookAdapter(getApplicationContext(),
                R.layout.item_plant, searchList, PlantBookAdapter.SHOW_ALL_NAME);
        resultListView.setAdapter(adapter);   // 어댑터 연결

        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 아이템 클릭시 상세 페이지로 넘어감
                Intent intent = new Intent(getApplicationContext(), DetailPopUpActivity.class);
                intent.putExtra(Fragment_Plant_Book.SELECTED_ITEM_KEY, searchList.get(i));
                startActivity(intent);
            }
        });
    }



}


