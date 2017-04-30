package com.fxj.flickerprogressbardemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.fxj.flickerprogressbar.FlickerProgressBar;

public class MainActivity extends AppCompatActivity implements Runnable{
    private String tag="MainActivity";

    private FlickerProgressBar pb;
    private Button btn;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            float value=msg.arg1;
            Log.i(tag,"value="+value);
            pb.setProgress(value);
            if(value>=100f){
                pb.finishTask();
            }

        }
    };

    Thread download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pb=(FlickerProgressBar)findViewById(R.id.pb);
        btn=(Button) findViewById(R.id.btn);

        downloadTask();

        pb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.toggle();

                if(pb.isStop()){
                    download.interrupt();
                }else{
                    downloadTask();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.reset();
                download.interrupt();
                downloadTask();
            }
        });

    }

    /**启动下载任务*/
    private void downloadTask() {
        download=new Thread(this);
        download.start();
    }

    @Override
    public void run() {

        try {
            while(!download.isInterrupted()){
                float progress=pb.getProgress();
                progress++;
                Thread.sleep(200);

                Message msg= Message.obtain();
                msg.arg1=(int)progress;
                handler.sendMessage(msg);
                if(progress>=100){
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
