package kr.ac.kunsan.mcalab.teethclassification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
public class MainActivity extends AppCompatActivity {
    AppCompatDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.demo1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { getDataFromURI("demo01.jpg"); }
        });
        findViewById(R.id.demo2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { getDataFromURI("demo02.jpg"); }
        });
        findViewById(R.id.demo3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { getDataFromURI("demo03.jpg"); }
        });

        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    // 선택한 이미지에서 비트맵 생성
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();
                    getDataFromBitmap(img);
                    // 이미지 표시
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void openResult(int all, int bad, String uri){
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("all",all);
        intent.putExtra("bad",bad);
        intent.putExtra("uri",uri);
        startActivity(intent);
        progressOFF();
    }

    private void getDataFromURI(final String uri){
        progressON(this,"loading...");
        Thread thread = new Thread(new Runnable() {

            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet normal_teeth_post = new HttpGet("http://duration.u-gis.net/detect.do?xy=true&image=" + uri);
                    HttpResponse response = client.execute(normal_teeth_post);
                    String normal_teeth_json = EntityUtils.toString(response.getEntity());
                    String file_name = normal_teeth_json.split("ori_file_name\": ")[1].replace("\"","").split(",")[0];
                    String detect_file_name = normal_teeth_json.split("image_path\": ")[1].replace("\"","").replace("}","").replace("]","");
                    HttpGet bad_teeth_get = new HttpGet("http://duration.u-gis.net/detect.do?xy=true&model_name=teeth_ext&image=" + file_name);
                    response = client.execute(bad_teeth_get);
                    String bad_teeth_json = EntityUtils.toString(response.getEntity());

                    int teeth = normal_teeth_json.split("teeth").length;
                    int bad_teeth = bad_teeth_json.split("object_type").length - bad_teeth_json.split("teeth").length;

                    openResult(teeth, bad_teeth,detect_file_name);


                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    private void getDataFromBitmap(final Bitmap bitmap) {
        progressON(this,"loading...");
        Thread thread = new Thread(new Runnable() {

            public void run() {

                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
                byte[] ba = bao.toByteArray();
                String ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
                ArrayList<NameValuePair> nameValuePairs = new ArrayList();
                nameValuePairs.add(new BasicNameValuePair("fileObj", ba1));
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost normal_teeth_post = new HttpPost("http://duration.u-gis.net/detect.do?xy=true");
                    normal_teeth_post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = client.execute(normal_teeth_post);
                    String normal_teeth_json = EntityUtils.toString(response.getEntity());
                    String file_name = normal_teeth_json.split("ori_file_name\": ")[1].replace("\"","").split(",")[0];
                    String detect_file_name = normal_teeth_json.split("image_path\": ")[1].replace("\"","").replace("}","").replace("]","");
                    HttpGet bad_teeth_get = new HttpGet("http://duration.u-gis.net/detect.do?xy=true&model_name=teeth_ext&image=" + file_name);
                    response = client.execute(bad_teeth_get);
                    String bad_teeth_json = EntityUtils.toString(response.getEntity());

                    int teeth = normal_teeth_json.split("teeth").length;
                    int bad_teeth = bad_teeth_json.split("object_type").length - bad_teeth_json.split("teeth").length;

                    openResult(teeth, bad_teeth,detect_file_name);

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }





    public void progressON(AppCompatActivity activity, String message) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressSET(message);
        } else {
            progressDialog = new AppCompatDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.loading);
            progressDialog.show();
        }
        final ImageView img_loading_frame = progressDialog.findViewById(R.id.iv_frame_loading);
        final AnimationDrawable frameAnimation = (AnimationDrawable) img_loading_frame.getBackground();
        img_loading_frame.post(new Runnable() {
            @Override
            public void run() {
                frameAnimation.start();
            }
        });
        TextView tv_progress_message =progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }
    }

    public void progressSET(String message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        TextView tv_progress_message =progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }
    }

    public void progressOFF() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
