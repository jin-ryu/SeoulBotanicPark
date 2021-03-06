package com.example.botanic_park.Information;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.example.botanic_park.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class InconvenienceDetailPostActivity extends Activity {

    private static final String IP_ADDRESS = "106.10.37.13";
    private static final String TAG = "check";

    String myJSON;
    String myCommendJSON=null;

    private static Intent intent;
    private static final String TAG_TITLE = "title";
    private static final String TAG_CONTENT = "content";
    private static final String TAG_VIEWS = "views";
    private static final String TAG_REGISTRATION_DATE = "date";
    private static final String TAG_AMOUNT = "result";
    private static final String TAG_NUMBER = "number";

    JSONArray posts = null;
    JSONArray commendPosts = null;

    private EditText content;           //댓글 추가
    int pard;               //삭제 해도됌 asynctask에서 views값 가져오고 싶은데 안됨;;ㅠ
    TextView add_views;

    ListView commendList;
    ArrayList<HashMap<String, String>> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //상태 바 색 바꿔줌
        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.parseColor("#FAFAFA"));
        setContentView(R.layout.activity_inconvenience_detail_post);
        AppManager.getInstance().setInconvenienceDetailPostActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(NetworkStatus.getConnectivityStatus(InconvenienceDetailPostActivity.this)==3) {
            Toast.makeText(InconvenienceDetailPostActivity.this, "네트워크가 연결되지 않아 초기화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(InconvenienceDetailPostActivity.this, MainActivity.class);
            startActivity(intent);
        }

        intent = getIntent();
        add_views = findViewById(R.id.views);

        getData("http://" + IP_ADDRESS + "/inconvenienceselect.php");   //글에 대한 정보 가져오기

        commendList = (ListView) findViewById(R.id.listviewcommend);
        postList = new ArrayList<HashMap<String, String>>();

        getCommendData("http://" + IP_ADDRESS + "/selectinconveniencecommend.php");         //글에 대한 댓글 가져오기

        add_views.setText(String.valueOf(Integer.parseInt(intent.getStringExtra("views"))+1));

        UpdateData updateTask = new UpdateData();
        updateTask.execute("http://" + IP_ADDRESS + "/updatepostviews.php", intent.getStringExtra("title"), add_views.getText().toString());

        content = (EditText)findViewById(R.id.input_commend_content);

        ImageButton buttonInsert = (ImageButton)findViewById(R.id.add_comment_button);
        buttonInsert.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                if(content.getText().toString().replace(" ", "").equals("")) {
                    Toast.makeText(InconvenienceDetailPostActivity.this, "내용이 공백입니다. 댓글추가가 안됩니다.", Toast.LENGTH_SHORT).show();
                }
                else if(NetworkStatus.getConnectivityStatus(InconvenienceDetailPostActivity.this)!=3) {

                    InsertCommend task = new InsertCommend();
                    task.execute("http://" + IP_ADDRESS + "/insertinconveniencecommend.php", intent.getStringExtra("title"), content.getText().toString());

                    content.setText("");
                    onStart();      //댓글 추가하면 바로 달리게 onStart 사용
                }
                else {
                    Toast.makeText(InconvenienceDetailPostActivity.this, "네트워크가 연결되어야 이용할 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton edit_post = (ImageButton) findViewById(R.id.edit_post);
        edit_post.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                Intent activityRevise = new Intent(getApplicationContext(),RegistrationPostInInconvenienceActivity.class);
                activityRevise.putExtra("Post", "Revise");
                activityRevise.putExtra("number", intent.getStringExtra("title"));
                activityRevise.putExtra("password", intent.getStringExtra("password"));

                if(NetworkStatus.getConnectivityStatus(InconvenienceDetailPostActivity.this)!=3) {
                    startActivity(activityRevise);
                }
                else {
                    Toast.makeText(InconvenienceDetailPostActivity.this, "네트워크가 연결되어야 이용할 수 있습니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        ImageButton delete_post = (ImageButton) findViewById(R.id.delete_post);
        delete_post.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                Intent activityDelete = new Intent(getApplicationContext(),ConfirmPasswordActivity.class);
                activityDelete.putExtra("password", intent.getStringExtra("password"));

                startActivityForResult(activityDelete, 3000);
            }
        });

    }

    //ConfirmPasswordActivity를 통해서 받아오는 값을 통해 작업
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data.getStringExtra("result").equals("delete")) {
            if (NetworkStatus.getConnectivityStatus(InconvenienceDetailPostActivity.this) != 3) {

                DeletePost deletePost = new DeletePost();
                deletePost.execute("http://" + IP_ADDRESS + "/deleteinconveniencepost.php", intent.getStringExtra("title"));

                DeleteCommend deleteCommend = new DeleteCommend();
                deleteCommend.execute("http://" + IP_ADDRESS + "/deleteinconveniencecommend.php", intent.getStringExtra("title"));

                finish();      //글삭제하면 바로 글 목록으로감
            } else {
                Toast.makeText(InconvenienceDetailPostActivity.this, "네트워크가 연결되어야 이용할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            onStart();
        }
    }

    protected void showList() {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            posts = jsonObj.getJSONArray(TAG_AMOUNT);
            for (int i = 0; i < posts.length(); i++) {
                JSONObject c = posts.getJSONObject(i);
                if (c.getString(TAG_NUMBER).equals(intent.getStringExtra("title"))){

                    TextView post_title = findViewById(R.id.title);
                    post_title.setText(c.getString(TAG_TITLE));

                    TextView post_content = findViewById(R.id.content);
                    post_content.setText(c.getString(TAG_CONTENT));

                    TextView post_date = findViewById(R.id.date);
                    post_date.setText(c.getString(TAG_REGISTRATION_DATE));

                    add_views.setText(String.valueOf(Integer.parseInt(c.getString(TAG_VIEWS)) + 1));
                    pard = Integer.parseInt(c.getString(TAG_VIEWS)) + 1;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getData(String url) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();

                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                myJSON = result;
                showList();
            }

        }

        GetDataJSON g = new GetDataJSON();

        g.execute(url);

    }

    protected void showListCommend() {
        try {
            JSONObject jsonObj = new JSONObject(myCommendJSON);
            commendPosts = jsonObj.getJSONArray(TAG_AMOUNT);
            for (int i = 0; i < commendPosts.length(); i++) {
                JSONObject c = commendPosts.getJSONObject(i);
                if(c.getString(TAG_NUMBER).equals(intent.getStringExtra("title"))){

                    String content = c.getString(TAG_CONTENT);
                    String date = c.getString(TAG_REGISTRATION_DATE);

                    HashMap<String, String> posts2 = new HashMap<String, String>();

                    posts2.put(TAG_CONTENT, content);
                    posts2.put(TAG_REGISTRATION_DATE, date);
                    postList.add(posts2);
                }
            }
            ListAdapter adapter = new SimpleAdapter(
                    InconvenienceDetailPostActivity.this, postList, R.layout.item_inconvenience_commend,
                    new String[]{TAG_CONTENT, TAG_REGISTRATION_DATE},
                    new int[]{R.id.commend_content, R.id.commend_date});
            commendList.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getCommendData(String url) {
        class GetCommendDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();

                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                myCommendJSON = result;
                showListCommend();
            }
        }

        GetCommendDataJSON ge = new GetCommendDataJSON();

        ge.execute(url);

    }


    class UpdateData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(InconvenienceDetailPostActivity.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
        }


        @Override
        protected String doInBackground(String... params) {

            String number = (String)params[1];
            String views = (String)params[2];

            Log.d("testad", views);
            String serverURL = (String)params[0];
            String postParameters = "number=" + number + "&views=" + Integer.parseInt(views);

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("texttest", "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {

                Log.d("texttest", "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }

    class InsertCommend extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(InconvenienceDetailPostActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(InconvenienceDetailPostActivity.this, result, Toast.LENGTH_LONG).show();

            progressDialog.dismiss();
        }


        @Override
        protected String doInBackground(String... params) {

            String number = (String)params[1];
            String content = (String)params[2];
            long now = System.currentTimeMillis();
            Date today = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
            String date = sdf.format(today);

            String serverURL = (String)params[0];
            String postParameters = "number=" + number + "&content=" + content + "&date=" +date;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return null;
            }

        }
    }

    class DeletePost extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(InconvenienceDetailPostActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(InconvenienceDetailPostActivity.this, result, Toast.LENGTH_LONG).show();

            progressDialog.dismiss();
        }


        @Override
        protected String doInBackground(String... params) {

            String number = (String)params[1];

            String serverURL = (String)params[0];
            String postParameters = "number=" + number;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return null;
            }

        }
    }

    class DeleteCommend extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(InconvenienceDetailPostActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(InconvenienceDetailPostActivity.this, result, Toast.LENGTH_LONG).show();

            progressDialog.dismiss();
        }

        @Override
        protected String doInBackground(String... params) {

            String number = (String)params[1];

            String serverURL = (String)params[0];
            String postParameters = "number=" + number;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return null;
            }

        }
    }
}




