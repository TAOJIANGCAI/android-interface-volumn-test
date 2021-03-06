package isc.crsc.com.mytestcase.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Environment;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class AudioTrackManager {

    public static final String TAG = "AudioTrackManager";
    private AudioTrack audioTrack;
    private DataInputStream dis;
    private Thread recordThread;
    private boolean isStart = false;
    private static AudioTrackManager mInstance;
    private int bufferSize;
    private String FOLDER = "AudioRecoder";
    private AudioRecorder audioRecorder = null;

    public AudioTrackManager() {
        bufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2, AudioTrack.MODE_STREAM);
    }

    /**
     * 获取单例引用
     *
     * @return
     */
    public static AudioTrackManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioTrackManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioTrackManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 销毁线程方法
     */
    private void destroyThread() {
        try {
            isStart = false;
            if (null != recordThread && Thread.State.RUNNABLE == recordThread.getState()) {
                try {
                    Thread.sleep(500);
                    recordThread.interrupt();
                } catch (Exception e) {
                    recordThread = null;
                }
            }
            recordThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            recordThread = null;
        }
    }

    /**
     * 启动播放线程
     */
    private void startThread() {
        destroyThread();
        isStart = true;
        if (recordThread == null) {
            audioRecorder = new AudioRecorder();
            recordThread = new Thread(recordRunnable);
            recordThread.start();
        }
    }

    /**
     * 播放线程
     */
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
//                audioRecorder.ns_init();
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                byte[] tempBuffer = new byte[bufferSize];
                int readCount = 0;
                while (dis.available() > 0) {
                    readCount = dis.read(tempBuffer);
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
//                    audioRecorder.ns_process(tempBuffer, readCount);
                    if (readCount != 0 && readCount != -1) {
                        audioTrack.play();
                        audioTrack.write(tempBuffer, 0, readCount);
                    }
                }
                // stopPlay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    /**
     * 播放文件
     *
     * @param path
     * @throws Exception
     */
    private void setPath(String path) throws Exception {
        File file = new File(path);
        dis = new DataInputStream(new FileInputStream(file));
    }

    /**
     * 启动播放
     *
     * @param path
     */
    public void startPlay(String path) {
        try {
            setPath(path);
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        try {
            destroyThread();
            if (audioTrack != null) {
                if (audioTrack.getState() == AudioRecord.STATE_INITIALIZED) {
                    audioTrack.stop();
                }
                if (audioTrack != null) {
                    audioTrack.release();
                }
            }
            if (dis != null) {
                dis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取SD完整路径
     **/
    public static String getSDPath(String path) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + path;
    }

    public String getFloderPath(String fileName) {
        return getPathFile(getSDPath(FOLDER)) + File.separator + fileName;
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

}
