package com.sound.project2.isaac.project_sound;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    Button btnRecord, btnStop, btnExit;

    boolean isRecording = false;
    static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_IN_DEFAULT;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    int recBufSize;
    AudioRecord audioRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("噪音测试机");

        recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                frequency, channelConfiguration,
                audioEncoding, recBufSize);

        btnRecord = (Button) this.findViewById(R.id.startBtn);
        btnRecord.setOnClickListener(new ClickEvent());

        btnStop = (Button) this.findViewById(R.id.stopBtn);
        btnStop.setOnClickListener(new ClickEvent());

        recoderDialog = new AudioRecoderDialog(this);
        recoderDialog.setShowAlpha(0.98f);
//        btnExit = (TextView) this.findViewById(R.id.exitBtn);
//        btnExit.setOnClickListener(new ClickEvent());
    }

    private long downT; //开始录音时间
    private AudioRecoderDialog recoderDialog;


    public void onUpdate(double db) {
        if(null != recoderDialog) {
            recoderDialog.setLevel((int)db);
            recoderDialog.setTime(System.currentTimeMillis() - downT);
        }
    }

    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view == btnRecord) {
                isRecording = true;
                new RecordPlayThread().start();     //开始录音处理
                /**-------------**/
                downT = System.currentTimeMillis(); //记录当前时间
                recoderDialog.showAtLocation(view, Gravity.CENTER,0,0); //显示话筒的对话框
                /**-------------**/

            } else if (view == btnStop) {
                Log.d("暂停", "------------------------");
                isRecording = false;
                recoderDialog.dismiss();
            } else if (view == btnExit) {
                isRecording = false;
                MainActivity.this.finish();
            }

        }
    }




    class RecordPlayThread extends Thread {
        private long startTime;
        private long endTime;
        public void run() {
            byte[] buffer = new byte[recBufSize];
            audioRecord.startRecording(); //开始录制
            startTime = System.currentTimeMillis();
            while (isRecording) {
                //r是实际读取的数据长度，一般而言r会小于buffersize
                int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
                long v = 0;
                // 将 buffer 内容取出，进行平方和运算
                for (int i = 0; i < buffer.length; i++) {
                    v += buffer[i] * buffer[i];
                }
                // 平方和除以数据总长度，得到音量大小。
                double mean = v / (double) bufferReadResult;
                double volume = 20 * Math.log10(mean);
                Log.d("测音", "分贝值:" + volume);
                updateMicStatus(volume);
            }
            audioRecord.stop();
        }

        private void updateMicStatus(double volume){
            if(audioRecord != null){
                    onUpdate(volume);
            }
        }
    }


}