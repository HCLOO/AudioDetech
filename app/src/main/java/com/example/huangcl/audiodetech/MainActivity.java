package com.example.huangcl.audiodetech;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    int frequency = 8000;/*44100;*/
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    AudioRecord audioRecord;
    private RealDoubleFFT transformer;
    int blockSize = /*2048;// = */256;
    boolean started = false;
    boolean CANCELLED_FLAG = false;
    double[][] cancelledResult = {{100}};
    int mPeakPos;
    int targetVolume;
    RecordAudio recordTask;
    short[] buffer;
    MediaPlayer mediaPlayer=new MediaPlayer();
    int successNum=0;
    int totalNum=0;
    int width;
    Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startDeteching();
    }

    public void startDeteching() {

        mediaPlayer=MediaPlayer.create(this,R.raw.audio);
        started = true;
        CANCELLED_FLAG = false;
        recordTask = new RecordAudio();
        recordTask.execute();
        mediaPlayer.start();
    }

    private class RecordAudio extends AsyncTask<Void, double[], Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    channelConfiguration, audioEncoding);

            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT, frequency,
                    channelConfiguration, audioEncoding, bufferSize);

            int bufferReadResult;
            buffer = new short[blockSize];
            double[] toTransform = new double[blockSize];
            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {
                Log.e("Recording failed", e.toString());

            }
            while (started) {

                if (isCancelled() || (CANCELLED_FLAG == true)) {

                    started = false;
                    publishProgress(cancelledResult);
                    Log.d("doInBackground", "Cancelling the RecordTask");
                    break;
                } else {
                    bufferReadResult = audioRecord.read(buffer, 0, blockSize);

                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                    }

                    transformer.ft(toTransform);

                    publishProgress(toTransform);

                }
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(double[]...progress) {

            double mMaxFFTSample = 150.0;
            boolean flag=false;

            if(progress[0].length == 1 ){
                Log.d("FFTSpectrumAnalyzer", "onProgressUpdate: Blackening the screen");
            }

            else {
                if (width > 512) {
                    for (int i = 0; i < progress[0].length; i++) {
                        int x = 2 * i;
                        int downy = (int) (150 - (progress[0][i] * 10));//progress[0][i]与该频率对应的分贝值有关
                        int upy = 150;
                        if(downy < mMaxFFTSample)
                        {
                            mMaxFFTSample = downy;
                            mPeakPos = i;
                        }
                        if((((1.0 * frequency) / (1.0 * blockSize)) * i)/2>2950 && (((1.0 * frequency) / (1.0 * blockSize)) * i)/2<3050) {
                            targetVolume=(int)Math.abs(progress[0][i]);
                            Log.d("test1::", "downy1: "+downy+" 频率： "+(((1.0 * frequency) / (1.0 * blockSize)) * i)/2+" i: "+i+" progress[0][i]: "+targetVolume);

                            if(targetVolume>15)
                                flag=true;
                        }
                    }
                    ++totalNum;
                    if(flag)
                        ++successNum;
                    if(mediaPlayer.getCurrentPosition()>=mediaPlayer.getDuration()/2) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer=null;
                        recordTask.cancel(true);
                        audioRecord.stop();
                        audioRecord.release();
                        audioRecord=null;
                        if((100*(successNum-1)/totalNum)>=75)//获取率等于或大于75%则通过测试
                            Toast.makeText(getApplicationContext(), "检测成功率："+100*(successNum-1)/totalNum+" 是否通过：通过！", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "检测成功率："+100*(successNum-1)/totalNum+" 是否通过：不通过！", Toast.LENGTH_LONG).show();

                        Log.d("test1::", "总数： "+totalNum+" 成功数： "+(successNum-1)+" 检测成功率："+100*(successNum-1)/totalNum);
                    }
                }

                else {
                    for (int i = 0; i < progress[0].length; i++) {
                        int x = i;
                        int downy = (int) (150 - (progress[0][i] * 10));//progress[0][i]与该频率对应的分贝值有关
                        int upy = 150;
                        if(downy < mMaxFFTSample)
                        {
                            mMaxFFTSample = downy;
                            //mMag = mMaxFFTSample;
                            mPeakPos = i;
                        }
                        if((((1.0 * frequency) / (1.0 * blockSize)) * i)/2>2950 && (((1.0 * frequency) / (1.0 * blockSize)) * i)/2<3050) {
                            targetVolume=(int)Math.abs(progress[0][i]);
                            Log.d("test2::", "downy2: "+downy+" 频率： "+(((1.0 * frequency) / (1.0 * blockSize)) * i)/2+" i: "+i+" progress[0][i]: "+targetVolume);

                            if(targetVolume>15)
                                flag=true;
                        }
                    }
                    ++totalNum;
                    if(flag)
                        ++successNum;
                    if(mediaPlayer.getCurrentPosition()>=mediaPlayer.getDuration()/2) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer=null;
                        recordTask.cancel(true);
                        audioRecord.stop();
                        audioRecord.release();
                        audioRecord=null;
                        if((100*(successNum-1)/totalNum)>=75)//获取率等于或大于75%则通过测试
                            Toast.makeText(getApplicationContext(), "检测成功率："+100*(successNum-1)/totalNum+" 是否通过：通过！", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "检测成功率："+100*(successNum-1)/totalNum+" 是否通过：不通过！", Toast.LENGTH_LONG).show();

                        Log.d("test2::", "总数： "+totalNum+" 成功数： "+(successNum-1)+" 检测成功率："+100*(successNum-1)/totalNum);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try{
                audioRecord.stop();
            }
            catch(IllegalStateException e){
                Log.e("Stop failed", e.toString());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        recordTask.cancel(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        transformer = new RealDoubleFFT(blockSize);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordTask.cancel(true);
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
