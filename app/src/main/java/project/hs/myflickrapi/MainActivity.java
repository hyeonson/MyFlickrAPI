package project.hs.myflickrapi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    EditText search_text;
    Button search_btn;
    ListViewAdapter mAdapter;
    Bitmap bm;
    public GridView mGridView;
    private ProgressDialog progressDialog;

    private static final String TAG = "imagesearchexample";
    public static final int LOAD_SUCCESS = 101;

    private String SEARCH_URL = "https://secure.flickr.com/services/rest/?method=flickr.photos.search";
    private String API_KEY = "&api_key=63b7e36db22d9f7ec0cd3f36b2179053";
    private String PER_PAGE = "&per_page=20"; //일단 20개로
    private String SORT = "&sort=interestingness-desc";
    private String FORMAT = "&format=json";
    private String CONTECT_TYPE = "&content_type=1";
    private String SEARCH_TEXT = "&text='cat'";
    private String REQUEST_URL = SEARCH_URL + API_KEY + PER_PAGE + SORT + FORMAT + CONTECT_TYPE + SEARCH_TEXT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search_text = (EditText)findViewById(R.id.search_text);
        search_text.setMovementMethod(new ScrollingMovementMethod());
        search_btn = (Button)findViewById(R.id.search_btn);
        mGridView = (GridView)findViewById(R.id.mGridView);

        search_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String validStr = search_text.getText().toString().trim();
                int validLeng = validStr.length();
                if(validLeng < 2)
                {
                    Toast.makeText(getApplicationContext(), "검색어를 두글자 이상 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                SEARCH_TEXT = "&text='" + validStr + "'";
                REQUEST_URL = SEARCH_URL + API_KEY + PER_PAGE + SORT + FORMAT + CONTECT_TYPE + SEARCH_TEXT;
                progressDialog = new ProgressDialog( MainActivity.this );
                progressDialog.setMessage("Please wait.....");
                progressDialog.show();
                getJSON();
            }
        });

    }
    public void  getJSON(){

        Thread thread = new Thread(new Runnable() {

            public void run() {
                try {
                    String result;
                    Log.d(TAG, REQUEST_URL);
                    URL url = null;
                    try {
                        url = new URL(REQUEST_URL);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.connect();

                    int responseStatusCode = httpURLConnection.getResponseCode();

                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {

                        inputStream = httpURLConnection.getInputStream();
                    } else {
                        inputStream = httpURLConnection.getErrorStream();

                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;


                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    httpURLConnection.disconnect();

                    result = sb.toString().trim();

                    Log.d("result show me", result);

                    if (jsonParser(result)) {
                        Message message = mHandler.obtainMessage(LOAD_SUCCESS);
                        mHandler.sendMessage(message);
                    }

                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> weakReference;

        public MyHandler(MainActivity mainactivity) {
            weakReference = new WeakReference<MainActivity>(mainactivity);
        }

        @Override
        public void handleMessage(Message msg) {

            MainActivity mainactivity = weakReference.get();

            if (mainactivity != null) {
                switch (msg.what) {

                    case LOAD_SUCCESS:
                        mainactivity.mGridView.setAdapter(mainactivity.mAdapter);
                        mainactivity.progressDialog.dismiss();

                        String jsonString = (String) msg.obj;
                        break;
                }
            }
        }
    }

    public boolean jsonParser(String jsonString){
        mAdapter = new ListViewAdapter(this);
        if (jsonString == null ) return false;

        jsonString = jsonString.replace("jsonFlickrApi(", "");
        jsonString = jsonString.replace(")", "");

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject photos = jsonObject.getJSONObject("photos");
            JSONArray photo = photos.getJSONArray("photo");

            for (int i = 0; i < photo.length(); i++) {
                JSONObject photoInfo = photo.getJSONObject(i);

                String id = photoInfo.getString("id");
                String secret = photoInfo.getString("secret");
                String server = photoInfo.getString("server");
                String farm = photoInfo.getString("farm");
                String title = photoInfo.getString("title");

                mAdapter.addItem(title, farm, server, id, secret);
                mAdapter.dataChange();
            }
            //mGridView.setAdapter(mAdapter);
            return true;
        } catch (JSONException e) {

            Log.d(TAG, e.toString() );
        }

        return false;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<imgData> mImgList = new ArrayList<>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mImgList.size();
        }

        @Override
        public Object getItem(int position) {
            return mImgList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            //imgData mData = mImgList.get(position);
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.one_image, null);
                //holder.mIcon = (ImageView) convertView.findViewById(R.id.mImage);
                holder.imageView = (ImageView) convertView.findViewById(R.id.one_imageView);
                holder.url = null;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            imgData mData = mImgList.get(position);
            final String imgurl = "http://farm" + mData.getFarm() + ".static.flickr.com/" + mData.getServer()
                    + "/" + mData.getId() + "_" + mData.getSecret() + ".jpg";

            Log.d("imgurl", "http://farm" + mData.getFarm() + ".static.flickr.com/" + mData.getServer()
            + "/" + mData.getId() + "_" + mData.getSecret() + ".jpg");

            Thread thread = new Thread(){
                @Override
                public void run(){
                    try{
                        URL url = new URL(imgurl);
                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();

                        InputStream is = conn.getInputStream();
                        bm = BitmapFactory.decodeStream(is);
                    }catch (MalformedURLException e){
                        e.printStackTrace();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            try{
                thread.join();
                holder.url = imgurl;
                holder.id = mData.getId();
                holder.title = mData.getTitle();
                Log.d("holder's url : ", holder.url);
                holder.imageView.setImageBitmap(bm);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            holder.imageView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    //hoder.url을 인텐트로 넘긴다.
                    Intent intent = new Intent(MainActivity.this, ShowImage.class);
                    intent.putExtra("id", holder.id);
                    intent.putExtra("title", holder.title);
                    intent.putExtra("url", holder.url);
                    startActivity(intent);
                    finish();
                }
            });

            return convertView;
        }

        public void addItem(String title, String farm, String server, String id, String secret) {
            imgData addInfo = null;
            addInfo = new imgData(title, farm, server, id, secret);
            mImgList.add(addInfo);
        }
        /*
        public void remove(int position) {
            mListData.remove(position);
            dataChange();
        }
        */
            /*
            public void sort(){
                Collections.sort(mListData, ListData.ALPHA_COMPARATOR);
                dataChange();
            }
            */

        public void dataChange() {
            mAdapter.notifyDataSetChanged();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder alertDig = new AlertDialog.Builder(this);

            alertDig.setMessage("종료 하시겠습니까??");
            alertDig.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    System.exit(0);
                }
            });

            alertDig.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            });

            AlertDialog alert = alertDig.create();
            alert.setTitle("정말..");
            //alert.section(R.draw.ic_launcher);
            alert.show();
        }
        return true;
    }

}
