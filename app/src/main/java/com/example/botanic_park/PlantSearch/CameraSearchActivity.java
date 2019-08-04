package com.example.botanic_park.PlantSearch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.botanic_park.AppManager;
import com.example.botanic_park.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/* 이미지 식물 검색 액티비티 */
public class CameraSearchActivity extends AppCompatActivity {
    private CameraSurfaceView surfaceView;
    private ImageView galleryBtn;
    private ProgressBar progressBar;
    public static Bitmap bitmap;

    private int cameraFacing;  // 카메라 전면,후면 구분

    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;

    private ArrayList<PlantBookItem> list;          // 전체 식물 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 전체 식물 리스트 받아옴
        list = AppManager.getInstance().getList();

        cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

        init();
    }

    private void init() {
        surfaceView = new CameraSurfaceView(CameraSearchActivity.this, cameraFacing);
        setContentView(surfaceView);
        addContentView(LayoutInflater.from(CameraSearchActivity.this).inflate(R.layout.activity_camera_search, null),
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        FrameLayout frameLayout = findViewById(R.id.camera_preview);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                surfaceView.autoFocus();    // 자동 초점 맞춤
            }
        });

        ImageButton captureBtn = findViewById(R.id.capture_btn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                surfaceView.capture(new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Log.d("테스트", originalBitmap.getWidth() + " " + originalBitmap.getHeight());

                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);  // 회전

                        bitmap = Bitmap.createBitmap(originalBitmap, 0,0,
                                originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
                        /*
                        int remainWidth = originalBitmap.getWidth() - originalBitmap.getHeight();
                        bitmap = Bitmap.createBitmap(originalBitmap, remainWidth/2,0,
                                originalBitmap.getHeight(), originalBitmap.getHeight(), matrix, true); // 정방형
                        */

                        Intent intent = new Intent(getApplicationContext(), ImagePreviewActivity.class);
                        startActivity(intent);  //  카메라 미리보기 화면 보여주기
                    }
                });
            }
        });

        ImageButton closeBtn = findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();   // 액티비티 닫음
            }
        });

        ImageButton switchCameraBtn = findViewById(R.id.switch_camera_btn);
        switchCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 카메라 화면 전환
                cameraFacing = (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) ?
                        Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
                surfaceView = new CameraSurfaceView(CameraSearchActivity.this, cameraFacing);
                init();
            }
        });

        //progressBar = findViewById(R.id.progressbar_camera);

        galleryBtn = findViewById(R.id.gallery_btn);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAlbum();
            }
        });
    }


    private void goToAlbum() {
        // 갤러리로 이동
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /*
         * 예외사항 처리
         * 앨범으로 이동했지만 선택하지 않고 뒤로 간 경우
         */
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {
            Uri photoUri = data.getData();
            Cursor cursor = null;

            try {
                // Uri 스키마를 content:/// 에서 file:/// 로 변경
                String[] proj = {MediaStore.Images.Media.DATA}; // MediaStore 이용한 접근 -> content 스키마

                assert photoUri != null;    // [assert] 값이 참이면 그냥 지나가고, 거짓이면 예외 발생해 강제종료
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  // 이 컬럼 인덱스에 파일 경로가 저장

                cursor.moveToFirst();

                File imageFile = new File(cursor.getString(column_index));    // 파일 가져옴
                BitmapFactory.Options options = new BitmapFactory.Options();
                String imageFilePath = imageFile.getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath, options);  // 비트맵 파일로 변환

                showSearchResult(bitmap);

            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
    }

    private String getBase64EncodedImage(Bitmap bitmap) {
        // 비트맵을 64bit로 인코딩한 string을 반환
        // 서버로 이미지를 보내기 위해서는 64bit 인코딩 필요
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);    // 저장된 이미지를 jpg로 포맷 품질 100으로 하여 출력
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String result = Base64.encodeToString(byteArray, Base64.DEFAULT);  // 이미지 인코딩
        return result;
    }

    private void showSearchResult(Bitmap bitmap) {
       ArrayList<String> result = new ArrayList<>();
        try {
            // request API
            PlantAPITask task = new PlantAPITask(getApplicationContext(), getBase64EncodedImage(bitmap));
            ArrayList<ProbablePlant> plants = task.execute().get();
            for(ProbablePlant plant : plants){
                result.add(plant.name);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // 검색 결과 창 띄우기
        Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
        intent.putExtra(SearchResultActivity.RESULT_TYPE, SearchResultActivity.IMAGE_SEARCH);
        intent.putExtra(Fragment_Plant_Book.SEARCH_WORD_KEY, result);
        /*
        if (result != null) {
            intent.putExtra("search word", result.name);
        } else {
            intent.putExtra("search word", new String());   // 검색 결과가 없으면 빈 스트링 보냄
        }
        */

        startActivity(intent);

        //finish();
    }
}
