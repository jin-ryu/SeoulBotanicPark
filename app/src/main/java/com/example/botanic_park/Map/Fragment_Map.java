package com.example.botanic_park.Map;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.example.botanic_park.MainActivity;
import com.example.botanic_park.R;
import com.github.clans.fab.FloatingActionMenu;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;
import com.naver.maps.map.widget.ZoomControlView;

import java.util.Arrays;


public class Fragment_Map extends Fragment implements OnMapReadyCallback{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private MapView mapView;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private InfoWindow infoWindow;
    private MainActivity mainActivity;
    private LocationButtonView locationButtonView;
    private ZoomControlView zoomControlView;
    private FrameLayout frame;

    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mainActivity = (MainActivity) getActivity();
        Toast.makeText(getContext(), "생성", Toast.LENGTH_LONG).show();

        return view;
    }


    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        frame = view.findViewById(R.id.background);

        mapView.getMapAsync(naverMap -> {

            locationButtonView = getActivity().findViewById(R.id.location);
            locationButtonView.setMap(naverMap);
            zoomControlView = getView().findViewById(R.id.zoom);
            zoomControlView.setMap(naverMap);

        });

        mapView.getMapAsync(this);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        materialDesignFAM = (FloatingActionMenu) view.findViewById(R.id.menu);

        materialDesignFAM.setIconAnimated(false);

        materialDesignFAM.setOnMenuButtonClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(materialDesignFAM.isOpened()) closeFloatingMenu();
                else openFloatingMenu();
            }

        });

        frame.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
               if (materialDesignFAM.isOpened())
               {
                   closeFloatingMenu();
                   return true;
               }

               return false;
            }
        });

        floatingActionButton1 = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.material_design_floating_action_menu_item3);
;

    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setOnMapClickListener(new naverMapClick());
        naverMap.setLocationSource(new TrackingModeListener(this,LOCATION_PERMISSION_REQUEST_CODE));

        naverMap.getUiSettings().setZoomControlEnabled(false);
        getNormalMarker(37.5719325, 126.8318979,"화장실");
        setInfowindowMarker(37.5694308, 126.8350116,"온실");
        getNormalMarker(37.5682113, 126.8337287,"주제정원");
        getNormalMarker(37.5662934, 126.8296977,"방문자센터").setSubCaptionText("카페·화장실");
        setGardenMark("VR카페",37.5684276, 126.8345194).setSubCaptionText("VR·카페·화장실");
        setThemaGardenMarker();

        infoWindow = getInfoWindow("정보");
        setPolygone();
    }


    /*----오버레이 생성 메소드----*/

    private Marker setInfowindowMarker(double latitude, double longitude, String caption)
    {
        final Marker marker = getNormalMarker(latitude,longitude,caption);
        marker.setTag(R.array.tema_garden);
        marker.setOnClickListener(new MarkerClick());

        return marker;
    }

    private Marker getNormalMarker(double latitude, double longitude, String caption)
    {
        Marker marker = new Marker();
        marker.setWidth(Marker.SIZE_AUTO);
        marker.setHeight(Marker.SIZE_AUTO);
        marker.setHideCollidedSymbols(true);
        marker.setHideCollidedMarkers(true);
        marker.setPosition(new LatLng(latitude, longitude));
        marker.setMap(naverMap);
        marker.setCaptionText(caption);
        marker.setZIndex(2);
        marker.setOnClickListener(overlay -> {
            Marker mark = (Marker) overlay;
            naverMap.setCameraPosition(new CameraPosition(mark.getPosition(),16));
            return true;
        });

        marker.setSubCaptionColor(Color.BLUE);
        marker.setSubCaptionTextSize(10);

        return marker;
    }

    private Marker setGardenMark(String gardenName, double latitude, double longitude)
    {
        Marker garden = getNormalMarker(latitude,longitude,gardenName);
        garden.setZIndex(0);
        garden.setHideCollidedMarkers(true);
        garden.setForceShowIcon(false);
        garden.setIconTintColor(Color.BLUE);
        garden.setWidth(1);
        garden.setHeight(1);

        garden.setSubCaptionTextSize(9);
        garden.setCaptionColor(Color.GRAY);

        garden.setMinZoom(15);

        return garden;
    }

    private InfoWindow getInfoWindow(final String describe)  // infowindow
    {
        InfoWindow infoWindow = new InfoWindow();
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return describe;
            }

        });

        infoWindow.setOnClickListener(new InfowindowClick());

        return infoWindow;
    }

    private void setPolygone()
    {
        PolylineOverlay polyline = new PolylineOverlay();
        polyline.setCoords(Arrays.asList(
                new LatLng(37.567925, 126.832514),
                new LatLng(37.5674657, 126.832184),
                new LatLng(37.567184, 126.8325716),
                new LatLng(37.5672061, 126.8328912),
                new LatLng(37.5671507, 126.8330588),
                new LatLng(37.5668952, 126.8329167),
                new LatLng(37.5666871, 126.8331705),
                new LatLng(37.5668223, 126.8335741),
                new LatLng(37.5671661, 126.834191),
                new LatLng(37.5673363, 126.8344046),
                new LatLng(37.5677949, 126.8346072),
                new LatLng(37.5680845, 126.8348282),
                new LatLng(37.5684359, 126.835196),
                new LatLng(37.5687838, 126.8353599),
                new LatLng(37.568786, 126.8353514),
                new LatLng(37.5692877, 126.8355989),
                new LatLng(37.5696706, 126.8355672),
                new LatLng(37.5699208, 126.8351941),
                new LatLng(37.5699228, 126.8347411),
                new LatLng(37.5699347, 126.8340503),
                new LatLng(37.5699324, 126.8340446),
                new LatLng(37.5695101, 126.8334878),
                new LatLng(37.5690383, 126.8333335),
                new LatLng(37.5689097, 126.8333064),
                new LatLng(37.567925, 126.832514)));

        polyline.setWidth(6);
        polyline.setJoinType(PolylineOverlay.LineJoin.Round);
        polyline.setMap(naverMap);
        polyline.setColor(Color.RED);
        polyline.setPattern(10, 8);
        polyline.setZIndex(-1);
        polyline.setMinZoom(15);
    }

    /*------------*/

    private void setThemaGardenMarker()  // 주제정원 마크
    {

        /* 주제 정원 */
        setGardenMark("치유의 정원",37.56867930442805,126.83485658315652);
        setGardenMark("숲정원",37.567565404738865,126.83402142477078);
        setGardenMark("바람의 정원",37.5676674540794,126.83291191946778);
        setGardenMark("오늘의 정원",37.568248938032475,126.83315398465993);
        setGardenMark("추억의 정원",37.56876686430842,126.83305095534219);
        setGardenMark("정원사 정원",37.56871846741127 ,126.83387171487031);
        setGardenMark("사색 정원",37.56946197667976 ,126.83400589684094);
        setGardenMark("초대의 정원",37.569061575852345 ,126.83439164445056);

        /* 일반 정원 */
        setGardenMark("초지원",37.5659305, 126.8312749);
        setGardenMark("숲문화원",37.5646662, 126.8324412);
        setGardenMark("둘레숲",37.5649573, 126.8336954);
        setGardenMark("숲문화학교",37.5652731, 126.8329566).setSubCaptionText("편의점·화장실");;
        setGardenMark("재배온실2",37.5658154, 126.8332292);
        setGardenMark("아이리스원",37.5683496, 126.8322809);
        setGardenMark("물가 가로수길",37.5691639, 126.8328341);

        setGardenMark("어린이정원학교",37.5704747, 126.834327).setSubCaptionText("카페·화장실");
        setGardenMark("어린이정원",37.5706668, 126.8344231);
    }

    /*플로팅 버튼 */

    private void closeFloatingMenu()
    {
        materialDesignFAM.close(true);
        materialDesignFAM.getMenuIconView().setImageResource(R.drawable.icon_pin_white);
        frame.setBackgroundColor(Color.parseColor("#00FFFFFF"));

    }
    private void openFloatingMenu()
    {
        materialDesignFAM.open(true);
        materialDesignFAM.getMenuIconView().setImageResource(R.drawable.ic_close);
        frame.setBackgroundColor(Color.parseColor( "#99000000"));

    }

    /*---- 네이버 지도 커스텀 ----*/


    private void setObjectVisibility()
    {
       if( locationButtonView.isShown())
       {
           locationButtonView.setVisibility(View.GONE);
           zoomControlView.setVisibility(View.GONE);
           materialDesignFAM.setVisibility(View.GONE);

       }  else { locationButtonView.setVisibility(View.VISIBLE);
          zoomControlView.setVisibility(View.VISIBLE);
          materialDesignFAM.setVisibility(View.VISIBLE);
       }
    }

    class MarkerClick implements Overlay.OnClickListener{ // 마커 클릭을 위한 리스너

        @Override
        public boolean onClick(@NonNull Overlay overlay) {
            Marker marker = (Marker)overlay;

            if(marker.getInfoWindow() == null){
                infoWindow.open(marker);
                naverMap.setCameraPosition(new CameraPosition(marker.getPosition(),16));

            } else {
                infoWindow.close();
            }

            return true;
        }
    }

    class InfowindowClick implements Overlay.OnClickListener{ // 정보창 클릭을 위한 리스너

        @Override
        public boolean onClick(@NonNull Overlay overlay) {
            InfoWindow infoWindow = (InfoWindow) overlay;
            Marker marker = infoWindow.getMarker();
            Toast.makeText(getContext(), marker.getCaptionText(), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getActivity(), Facilities_information.class);
            startActivity(intent);

            return true;
        }
    }

    class TrackingModeListener extends FusedLocationSource { // 위치 정보 클래스 오버라이드 메소드

        public TrackingModeListener(@NonNull Fragment fragment, int permissionRequestCode) {
            super(fragment, permissionRequestCode);
        }

        @Override
        public void deactivate() {  // 현재 위치 해제 시에 피벗 위치로 카메라 이동
          naverMap.setCameraPosition(new CameraPosition(new LatLng(37.56801290446582,126.83245227349256),15));
        }
    }

    class naverMapClick implements NaverMap.OnMapClickListener{ // 맵 클릭을 위한 리스너

        @Override
        public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
            if(infoWindow.getMarker() != null) infoWindow.close();

            else
            {
                mainActivity.setCurveBottomBarVisibility();
                setObjectVisibility();
            }
        }
    }

}
