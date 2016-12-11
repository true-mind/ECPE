package com.truemind.ecpe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 현석 on 2016-12-05.
 */
public class LogActivity extends Activity {

    private final int dataLength = 8;
    private final int crcLength = 7;
    TextView textView;
    ImageButton log_btn;
    String log;
    ProgressDialog progressdialog;

    int tempArray[] = new int[crcLength+1];
    int dataIntArray[][] = new int[8][];
    int divisorIntArray[] = new int[crcLength+1];
    int crcIntArray[][] = new int[8][];
    String slidingWindow[] = new String[3];
    String ACK[] = new String[8];
    String receive[] = new String[8];

    String frameCodeword[] = new String[8];
    String frame[] = new String[8];
    int frameScenario[] = new int[8];
    String divisor;
    int crcResult[] = new int[8];
    int error[] = new int[8];
    private Timer timer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        initView();
        initListener();
        for(int i=0;i<8;i++){
            dataIntArray[i]=new int[dataLength+crcLength+1];
        }
        for(int i=0;i<8;i++){
            crcIntArray[i]=new int[crcLength];
        }
        progressdialog = ProgressDialog.show(this, "로딩중", "Loading...please wait", true, false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                log = "";
                Intent intent = getIntent();

                divisor = intent.getStringExtra("divisor");
                for(int i = 0; i < crcLength; i++) {
                    divisorIntArray[i] = Atoi(divisor.charAt(i));
                }

                frame[0] = intent.getStringExtra("frame[0]");
                frame[1] = intent.getStringExtra("frame[1]");
                frame[2] = intent.getStringExtra("frame[2]");
                frame[3] = intent.getStringExtra("frame[3]");
                frame[4] = intent.getStringExtra("frame[4]");
                frame[5] = intent.getStringExtra("frame[5]");
                frame[6] = intent.getStringExtra("frame[6]");
                frame[7] = intent.getStringExtra("frame[7]");
                frameScenario[0] = intent.getIntExtra("frameScenario[0]", 4);
                frameScenario[1] = intent.getIntExtra("frameScenario[1]", 4);
                frameScenario[2] = intent.getIntExtra("frameScenario[2]", 4);
                frameScenario[3] = intent.getIntExtra("frameScenario[3]", 4);
                frameScenario[4] = intent.getIntExtra("frameScenario[4]", 4);
                frameScenario[5] = intent.getIntExtra("frameScenario[5]", 4);
                frameScenario[6] = intent.getIntExtra("frameScenario[6]", 4);
                frameScenario[7] = intent.getIntExtra("frameScenario[7]", 4);
                for(int i = 0; i<8; i++) {
                    receive[i] = "";
                    ACK[i] = "";
                }
                log += "------Receive/ACK initialized------\n";
                for(int i = 0; i<8; i++) {
                    log += "Receive["+i+"]: "+receive[i]+"\n";
                }
                for(int i = 0; i<8; i++) {
                    log += "ACK["+i+"]: "+ACK[i]+"\n";
                }

                log += "------------------------------------\n";
                for(int i = 0; i<8; i++) {
                    log += "frame["+i+"]: "+frame[i]+"\n";
                }


                log += "------------------------------------\n";
                log += "error - (1) for success, (0) for error\n";
                log += "----------------Start---------------\n";
                for(int i = 0; i < 8; i++) {
                    work(frameScenario[i], i);
                }



                threadhandler.sendEmptyMessage(0);
            }
        });
        thread.start();
    }


    public void initView(){

        textView = (TextView) findViewById(R.id.textView);
        log_btn = (ImageButton) findViewById(R.id.log_btn);

    }
    public void initListener(){

        textView.setMovementMethod(new ScrollingMovementMethod());

        log_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private Handler threadhandler = new Handler(){
        public void handleMessage(Message msg){
            textView.setText(log);
            progressdialog.dismiss();
        }
    };

    public void setRightDataIntArray(int n){
        for (int i = 0; i < dataLength+crcLength; i++) {
            dataIntArray[n][i] = (Atoi(frameCodeword[n].charAt(i)));
        }
    }

    public void setErrorDataIntArray(int n){
        for (int i = 0; i < dataLength+crcLength; i++) {
            dataIntArray[n][i] = (Atoi(frameCodeword[n].charAt(i)));
        }
        double randomValue = Math.random();
        int intValue = (int)(randomValue * (dataLength+crcLength-1));

        if(dataIntArray[n][intValue] == 1)
            dataIntArray[n][intValue] = 0;
        else
            dataIntArray[n][intValue] = 1;
    }

    public void setframeCodeword(int n, int error){
        frameCodeword[n] ="";
        for(int i = 0; i<dataLength+crcLength; i++) {
            frameCodeword[n] += frame[n].charAt(i+10);
        }
        if(error==1)
            setErrorDataIntArray(n);
        else
            setRightDataIntArray(n);

    }


    public void work(int frameScenario, int i){

        //need timer
        switch(frameScenario){
            case 1:
                setframeCodeword(i, 0);
                error[i] = errorDetection(i);
                Log.d("MyTag", "error"+ i + error[i]);
                Log.d("MyTag", "frameCodeword"+ i + frameCodeword[i]);
                log += "Frame"+"["+i+"] error : "+ error[i]+"\n";
                break;
            case 2:
                setframeCodeword(i, 1);
                error[i] = errorDetection(i);
                Log.d("MyTag", "error"+ i + error[i]);
                Log.d("MyTag", "frameCodeword"+ i + frameCodeword[i]);
                log += "Frame"+"["+i+"] error : "+ error[i]+"\n";
                break;
            case 3:
                setframeCodeword(i, 0);
                error[i] = errorDetection(i);
                Log.d("MyTag", "error"+ i + error[i]);
                Log.d("MyTag", "frameCodeword"+ i + frameCodeword[i]);
                log += "Frame"+"["+i+"] error : "+ error[i]+"\n";
                break;
            case 4:
                setframeCodeword(i, 0);
                error[i] = errorDetection(i);
                Log.d("MyTag", "error"+ i + error[i]);
                Log.d("MyTag", "frameCodeword"+ i + frameCodeword[i]);
                log += "Frame"+"["+i+"] error : "+ error[i]+"\n";
                break;
        }

        sendError(frameScenario, i);

        log+="\n\n";
        //until here
    }



    private void sendError(int frameScenario, int i) {
        if(error[i]==1) {
            if (frameScenario == 1) {
                receive[i] = "0";
                ACK[i] = "0";
            } else if (frameScenario == 2) {
                receive[i] = frame[i];
                ACK[i] = "01111110" + threeBitDtob(i) + "01111110";
            } else if (frameScenario == 3) {
                receive[i] = frame[i];
                ACK[i] = "0";
            } else if (frameScenario == 4) {
                receive[i] = frame[i];
                ACK[i] = "01111110" + threeBitDtob(i) + "01111110";
            }
        }
        else if(error[i]==0) {
            receive[i] = "01111110" + threeBitDtob(i) + frameCodeword[i] + "01111110";
            ACK[i] = "0";
        }


        log += "\n";
        log += "Receive[" + i + "]: " + receive[i] + "\nACK[" + i + "]: " + ACK[i]+"\n";
        sendError2(i);
    }
//-----------------------------------------------------------------------

    private void sendError2(int i) {
        if(error[i]==1) {
            if (receive[i] == "0" && ACK[i] == "0") {
                log += "Frame[" + i + "]--- frame lost, resending ...\n";
                receive[i] = frame[i];
                ACK[i] = "01111110" + threeBitDtob(i) + "01111110";
            } else if (receive[i] != "0" && ACK[i] == "0") {
                log += "Frame[" + i + "]--- ACK lost, resending ...\n";
                ACK[i] = "01111110" + threeBitDtob(i) + "01111110";
            } else if (receive[i] != "0" && ACK[i] != "0")
                log += "Frame[" + i + "]--- Success..!\n";
        }
        else if(error[i]==0) {
            log += "Frame[" + i + "]--- Single bit error..\n";
            receive[i] = frame[i];
            ACK[i] = "01111110" + threeBitDtob(i) + "01111110";
        }

        log += "Receive[" + i + "]: " + receive[i] + "\nACK[" + i + "]: " + ACK[i]+"\n";
    }


    private int errorDetection(int i) {

        for (int i2 = 0; i2 < crcLength+1; i2++) {
            tempArray[i2] = dataIntArray[i][i2];
        }
        for (int j = 0; j < dataLength; j++) {
            if (tempArray[0] == 1) {//나머지의 시작이 1일 때
                for (int k = 0; k < crcLength; k++) {
                    tempArray[k] = XOR(tempArray[k + 1], divisorIntArray[k+1]);
                }
            } else {//나머지의 시작이 0일 때
                for (int k = 0; k < crcLength; k++) {
                    tempArray[k] = XOR(tempArray[k + 1], 0);
                }
            }
            for (int k = 0; k < crcLength; k++) {
                tempArray[crcLength] = dataIntArray[i][j + (crcLength+1)];
                Log.d("MyTag", "tA" + j + ": " + tempArray[k]);
            }
        }
        for (int j = 0; j < crcLength; j++) {
            crcIntArray[i][j] = tempArray[j];
        }
        for (int j = 0; j < crcLength; j++) {
            crcResult[i] += crcIntArray[i][j];
        }

        Log.d("MyTag", "crcResult["+i+"]"+crcResult[i]);

        if(crcResult[i] == 0)
            return 1;
        else
            return 0;
    }

    private int XOR(int a, int b){
        if(a==b){
            return 0;
        }
        else{
            return 1;
        }
    }

    private int Atoi(char a){
        if(a=='1'){
            return 1;
        }else{
            return 0;
        }
    }

    private String threeBitDtob(int a){

        String binary = "";

        while(a>0){
            binary += a % 2;
            a = a / 2;
        }

        return binary;
    }
}