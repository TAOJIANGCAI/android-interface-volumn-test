package isc.crsc.com.mytestcase.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class AudioRecorder {

    private AudioRecord audioRecord = null;  // 声明 AudioRecord 对象

    /**
     * 录音状态
     **/
    private boolean isRecording = false;

    /**
     * 采样频率
     */
    private static final int SAMPLE_RATE = 8000;
    //设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_IN_MONO为单声道
    /**
     * 声道
     **/
    private static int CHANNEL = AudioFormat.CHANNEL_IN_MONO;

    /**
     * 样本数
     **/
    private static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 缓冲区大小
     **/
    private int bufferSize;

    private String folder;
    private String resultPath;

    /**
     * 初始化AudioRecord
     */
    static {
        System.loadLibrary("native-lib");
    }

    public void initAudioRecorder(String folderName) {
        folder = getSDPath(folderName);
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, AUDIO_FORMAT, bufferSize);
    }

    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    /**
     * 开始Raw录音
     **/
    public boolean startRawRecording(String fileName) {

        byte[] data = new byte[bufferSize];
        // 如果正在录音，则返回
        if (isRecording) {
            return false;
        }
        if (getAudioRecord() == null) {
            initAudioRecorder("AudioRecoder");
        }
        resultPath = getFloderPath(fileName);
        if (!checkSD()) {
            Log.i("Tag", "-------无可用的SD卡");
            return false;
        }
        audioRecord.startRecording();
        isRecording = true;
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(resultPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (null != os) {
            ns_init();
            while (isRecording) {
                int read = audioRecord.read(data, 0, bufferSize);
                ns_process(data, read);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void stopRecord() {
        if (!isRecording) {
            return;
        }
        if (null != audioRecord) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    /**
     * 获取SD完整路径
     **/
    public static String getSDPath(String path) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + path;
    }

    /**
     * 确认SD是否可以用
     **/
    public static boolean checkSD() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public String getFloderPath(String fileName) {
        return getPathFile(folder) + File.separator + fileName;
    }

    /**
     * 获取路径并创建资料夹
     **/
    public static File getPathFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public native void ns_init();

    public native void ns_process(byte[] buffer, int len);

}
