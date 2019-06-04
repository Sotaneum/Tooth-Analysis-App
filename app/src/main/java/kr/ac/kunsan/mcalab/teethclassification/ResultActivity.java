package kr.ac.kunsan.mcalab.teethclassification;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

import java.net.URL;

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        Intent intent = getIntent();
        int bad = intent.getExtras().getInt("bad");
        int all = intent.getExtras().getInt("all");
        String uri = "http://duration.u-gis.net/detect/"+intent.getExtras().getString("uri")+"?model=teeth";
        int good = all-bad;
        int result = 0;
        if(good>0 && all !=0){
            Log.v("222test",((good* 100)/all)+"");
            result = 101-((good* 100)/all);
        }

        Log.v("22test",uri);


        TextView rs = findViewById(R.id.result);
        if(result == 0){
            rs.setText("결과 : 측정 불가!");
        }else {
            rs.setText("결과 : 상위 " + result + "%");
        }
        LoadImageFromWebOperations(uri);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });
    }

    public void LoadImageFromWebOperations(final String url) {
        Thread thread = new Thread(new Runnable() {

            public void run() {
                try {
                    InputStream is = (InputStream) new URL(url).getContent();
                    final Drawable d = Drawable.createFromStream(is, "src name");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView im = findViewById(R.id.imageView);
                            im.setImageDrawable(d);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
    }
}