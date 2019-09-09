package com.example.botanic_park;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.botanic_park.Help.HelpActivity;
import com.example.botanic_park.PlantSearch.DetailPopUpActivity;
import com.example.botanic_park.PlantSearch.Fragment_Plant_Book;
import com.example.botanic_park.PlantSearch.PlantBookItem;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class Fragment_Home extends Fragment {
    private ArrayList<PlantBookItem> plantsToday;

    public Fragment_Home() {
    }

    public static Fragment_Home newInstance() {
        Fragment_Home fragment = new Fragment_Home();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        SliderView sliderView = view.findViewById(R.id.imageSlider);
        SliderAdapter sliderAdapter = new SliderAdapter(getContext());
        sliderView.setSliderAdapter(sliderAdapter);

        sliderView.setIndicatorAnimation(IndicatorAnimations.COLOR);
        sliderView.setSliderTransformAnimation(SliderAnimations.FADETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        sliderView.startAutoCycle();

        sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
            @Override
            public void onIndicatorClicked(int position) {
                sliderView.setCurrentPagePosition(position);
            }
        });

        setPlantsToday();   // 오늘의 식물 선정
        GridView gridView = view.findViewById(R.id.gridview_plant_today);
        TextView textView = view.findViewById(R.id.title_plants_today);
        if (isPlantsTodayComplete()) {
            // 바코드 보여줌
            gridView.setVisibility(View.INVISIBLE);
            textView.setText("오늘의 쿠폰");
            LinearLayout linearLayout = view.findViewById(R.id.barcode);
            linearLayout.setVisibility(View.VISIBLE);

        } else {
            // 오늘의 식물 보여줌
            gridView.setVisibility(View.VISIBLE);
            textView.setText("오늘의 식물");
            PlantTodayAdapter plantTodayAdapter = new PlantTodayAdapter(getContext(),
                    R.layout.item_plant_today, plantsToday);
            gridView.setAdapter(plantTodayAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getContext(), DetailPopUpActivity.class);
                    intent.putExtra(Fragment_Plant_Book.SELECTED_ITEM_KEY, plantsToday.get(i));
                    startActivity(intent);
                }
            });
        }


        ImageButton helpBtn = view.findViewById(R.id.help_btn);
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), HelpActivity.class);
                intent.putExtra(HelpActivity.HELP_CODE, HelpActivity.HELP_TODAY_PLANT);
                startActivity(intent);
            }
        });

        return view;
    }

    private void setPlantsToday() {
        // 오늘의 식물 구하는 공식
        // 오늘 날짜에 * 240 곱해서 119로 나눈 나머지를 시작 인덱스로 3개 뽑음
        int index = (240 * getDate()) % 119;

        ArrayList<PlantBookItem> list = AppManager.getInstance().getList();
        plantsToday = new ArrayList<>();
        plantsToday.add(getNewItem(list.get(index + 1)));
        plantsToday.add(getNewItem(list.get(index + 2)));
        plantsToday.add(getNewItem(list.get(index + 3)));

        AppManager.getInstance().setPlantsToday(plantsToday);
    }

    private int getDate() {
        // 오늘 날짜 정보를 숫자로 반환
        TimeZone jst = TimeZone.getTimeZone("Asia/Seoul");
        Calendar calendar = Calendar.getInstance(jst);

        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        Log.d("날짜", month + " " + day);

        return 100 * month + day;
    }

    private boolean isPlantsTodayComplete() {
        for (PlantBookItem item : plantsToday) {
            if (!item.isCollected())
                return false;
        }
        return true;
    }

    private PlantBookItem getNewItem(PlantBookItem item) {
        return new PlantBookItem(item.getId(), item.getImg_url(),
                item.getName_ko(), item.getName_sc(), item.getName_en(),
                item.getType(), item.getBlossom(), item.getDetails());
    }

}

class SliderAdapter extends SliderViewAdapter<SliderAdapter.ViewHolder> {
    private Context context;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_slider, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        switch (position) {
            case 0:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.home_2)
                        .centerCrop()
                        .into(viewHolder.imageView);
                break;
            case 1:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.home_4)
                        .centerCrop()
                        .into(viewHolder.imageView);
                break;
            case 2:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.home_5)
                        .centerCrop()
                        .into(viewHolder.imageView);
                break;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    class ViewHolder extends SliderViewAdapter.ViewHolder {
        View itemView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_home_background);
            this.itemView = itemView;
        }
    }
}

class PlantTodayAdapter extends BaseAdapter {
    private ArrayList<PlantBookItem> itemList;
    private Context context;

    int layout;
    LayoutInflater layoutInflater;

    public PlantTodayAdapter(Context context, int layout, ArrayList<PlantBookItem> itemList) {
        this.itemList = itemList;
        this.layout = layout;
        this.context = context;

        layoutInflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int i) {
        return itemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = layoutInflater.inflate(layout, null);

        PlantBookItem item = itemList.get(i);
        TextView textView = view.findViewById(R.id.name);
        textView.setText(item.getName_ko());

        if (item.isCollected()) {
            view.setBackground(ContextCompat.getDrawable(context, R.drawable.border_oval_active));
            ImageView imageView = view.findViewById(R.id.icon);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            layoutParams.setMargins(5, 5, 5, 5);
            imageView.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_lotus_active));
            textView.setTextColor(view.getResources().getColor(R.color.colorBase));
        }

        return view;
    }

}


