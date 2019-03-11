package isc.crsc.com.mytestcase.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @ClassName: RecorderUtil
 * @Description: 录音工具
 */
public class RecorderUtil {

	/** 录音成功 **/
	public static final int RECORDER_OK = 1;
	/** 录音失败 **/
	public static final int RECORDER_NG = 2;

	/** Raw录Mp3格式 **/
	public static final int RECORDING_MODE_MP3 = 1;
	/** Raw录wav格式 **/
	public static final int RECORDING_MODE_WAV = 2;
	/** Raw录media格式 **/
	public static final int RECORDING_MODE_MEDIA = 3;
	/** Raw暂存档名 **/
	public static final String RAW_FILE_NAME = "Recorder.raw";

	/** 线程池 **/
	private ExecutorService fixedThreadPool;
	//44100是目前的标准，但是某些设备仍然支持22050，16000，11025，低质量的音频就可以使用4000、8000等低采样率
	/** 采样频率 */
	private static final int SAMPLE_RATE = 8000;
	//设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_IN_MONO为单声道
	/** 声道 **/
	private static int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
	// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
	/** 样本数 **/
	private static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

	/** AudioRecord录音类 **/
	private AudioRecord mAudioRecorder;
	/** MediaRecorder录音类 **/
	private MediaRecorder mMediaRecorder;
	/** 缓冲区大小 **/
	private int bufferSize;
	/** raw档所在文件夹 **/
	private String folder;
	/** Raw暂存路径 **/
	private String rawFilePath;

	/** AudioRecord的录音储存模式(mp3用shout, wav用byte) **/
	private int recordingMode = RECORDING_MODE_WAV;
	private byte[] wavBuffer = new byte[1];
	private short[] mp3Buffer = new short[1];

	/** AudioRecord的录音结果保存路径 **/
	private String resultPath;
	/** 录音状态 **/
	private boolean isRecording = false;

	private Handler handler;

	/** 初始化路径资料夹 **/
	public RecorderUtil(String folderName, Handler handler){
		folder = getSDPath(folderName);
		rawFilePath = folder + File.separator + RAW_FILE_NAME;
		fixedThreadPool = Executors.newFixedThreadPool(1);
		this.handler = handler;
	}

	public String getFloderPath(){
		return folder;
	}

	/** 初始化AudioRecord */
	private void initAudioRecorder() {
		bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, AUDIO_FORMAT);
		mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, AUDIO_FORMAT, bufferSize);
	}

	public AudioRecord getAudioRecord(){
		return mAudioRecorder;
	}

	public MediaRecorder getMediaRecorder(){
		return mMediaRecorder;
	}

	public int getRecordingMode() {
		return recordingMode;
	}

	public byte[] getWavBuffer() {
		return wavBuffer;
	}

	public short[] getMp3Buffer() {
		return mp3Buffer;
	}

	/** 初始化MediaRecorder */
	private void initMediaRecord(){
		/* ①Initial：实例化MediaRecorder对象 */
		mMediaRecorder = new MediaRecorder();

		/* setAudioSource/setVedioSource*/
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置麦克风

		/* 设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default
		 * THREE_GPP(3gp格式，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
		 */
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);

		/* 设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default */
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
	}

	/** 录音wav **/
	public boolean startWavRecording(String filePath){
		return startRawRecording(filePath, RECORDING_MODE_WAV);
	}

	/** 录音mp3 **/
	public boolean startMp3Recording(String filePath){
		return startRawRecording(filePath, RECORDING_MODE_MP3);
	}

	/** 开始Raw录音 **/
	private boolean startRawRecording(String filePath, int recordingMode) {

		// 如果正在录音，则返回
		if (isRecording) {
			return false;
		}
		this.recordingMode = recordingMode;
		resultPath = filePath;

		if(!checkSD()){
			Log.i("Tag","-------无可用的SD卡");
			return false;
		}
		getPathFile(folder);

		// 初始化AudioRecorder
		if (mAudioRecorder == null) {
			initAudioRecorder();
		}
		mAudioRecorder.startRecording();
		fixedThreadPool.execute(new BufferedWriteThread());
		isRecording = true;
		return true;
	}

	/** 开始Media录音 **/
	public boolean startMediaRecording(String filePath){
		// 如果正在录音，则返回
		if (isRecording) {
			return false;
		}
		recordingMode = RECORDING_MODE_MEDIA;
		if(!checkSD()){
			Log.i("Tag","-------无可用的SD卡");
			return false;
		}
		getPathFile(folder);

		// 初始化MediaRecorder
		if(mMediaRecorder == null){
			initMediaRecord();
		}
		mMediaRecorder.setOutputFile(filePath);
		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
			isRecording = true;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isRecording;
	}

	/** 停止录音 **/
	public void stopRawRecording() {
		if (!isRecording) {
			return;
		}
		if(mAudioRecorder != null){
			// 停止
			mAudioRecorder.stop();
			isRecording = false;
		}
	}

	/** 停止录音 **/
	public void stopMediaRecording() {
		if (!isRecording) {
			return;
		}
		if(mMediaRecorder != null){
			// 停止
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
			isRecording = false;
			if(handler != null){
				handler.sendEmptyMessage(RECORDER_OK);
			}
		}
	}

	public void deleteFile(String filePath){
		File file = new File(filePath);
		if(file.exists()){
			file.delete();
		}
	}

	/** 缓冲流写入线程 **/
	class BufferedWriteThread implements Runnable {

		@Override
		public void run() {
			boolean result = writeDateTOFile(rawFilePath);
			boolean result2 = false;
			if(result){
				if(recordingMode == RECORDING_MODE_WAV){
					result2 = convertWavFile(rawFilePath, resultPath);
					// raw可删可不删
//					deleteFile(rawFilePath);
				} else if (recordingMode == RECORDING_MODE_MP3) {
					//result2 = convertMp3File(rawFilePath, resultPath);
					// raw可删可不删
//					deleteFile(rawFilePath);
				} else {
					result2 = true;
				}
			} else {
				result2 = false;
			}
			if(handler != null){
				if(result2){
					handler.sendEmptyMessage(RECORDER_OK);
				} else {
					handler.sendEmptyMessage(RECORDER_NG);
				}
			}
		}

		/** 边录边写入档案 **/
		private boolean writeDateTOFile(String filePath){

			boolean isSuccess = false;
			DataOutputStream output = null;
			try {
				output = new DataOutputStream(new BufferedOutputStream( new FileOutputStream(filePath)));

				// 由于引用了不同人的Demo,转换为wav及转换为mp3的保存raw方式不一样,不然会是杂音
				// 实验证实一个音2byte,wav以byte存储，高位数会在第二个byte,例如讯号1，保存后的值是00010000；用FLame存成mp3，高位数在第一个byte,结果就是00000001
				if(recordingMode == RECORDING_MODE_WAV){
					wavBuffer = new byte[bufferSize];
					while (isRecording) {
						int readSize = mAudioRecorder.read(wavBuffer, 0, bufferSize);
						if(AudioRecord.ERROR_INVALID_OPERATION != readSize){
							output.write(wavBuffer);
						}
					}

				} else if (recordingMode == RECORDING_MODE_MP3) {
					mp3Buffer = new short[bufferSize];
					while (isRecording) {
						int readSize = mAudioRecorder.read(mp3Buffer, 0, bufferSize);
						for (int i = 0; i < readSize; i++) {
							output.writeShort(mp3Buffer[i]);
						}
					}
				}
				output.flush();
				isSuccess = true;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close(output);
			}
			return isSuccess;
		}

		/** 转换为mp3格式 **/
		/*private boolean convertMp3File(String inFilename, String outFilename){
			FLameUtils lameUtils = new FLameUtils(1, SAMPLE_RATE, 96);
			return lameUtils.raw2mp3(inFilename, outFilename);
		}*/

		/** 这里得到可播放的wav音频文件  **/
		private boolean convertWavFile(String inFilename, String outFilename) {
			boolean isSuccess = false;
			FileInputStream in = null;
			FileOutputStream out = null;
			long totalAudioLen = 0;
			long totalDataLen = totalAudioLen + 36;
			long longSampleRate = SAMPLE_RATE;
			int channels = 1;
			if(CHANNEL == AudioFormat.CHANNEL_IN_STEREO){
				channels = 2;
			}
			long byteRate = 16 * SAMPLE_RATE * channels / 8;
			byte[] data = new byte[bufferSize];
			try {
				in = new FileInputStream(inFilename);
				out = new FileOutputStream(outFilename);
				totalAudioLen = in.getChannel().size();
				totalDataLen = totalAudioLen + 36;
				WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
				while (in.read(data) != -1) {
					out.write(data);
				}
				out.flush();
				isSuccess = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close(in);
				close(out);
			}
			return isSuccess;
		}

		/**
		 * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
		 * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
		 * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
		 * 自己特有的头文件。
		 */
		private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
										 long totalDataLen, long longSampleRate, int channels, long byteRate)
				throws IOException {
			byte[] header = new byte[44];
			header[0] = 'R'; // RIFF/WAVE header
			header[1] = 'I';
			header[2] = 'F';
			header[3] = 'F';
			header[4] = (byte) (totalDataLen & 0xff);
			header[5] = (byte) ((totalDataLen >> 8) & 0xff);
			header[6] = (byte) ((totalDataLen >> 16) & 0xff);
			header[7] = (byte) ((totalDataLen >> 24) & 0xff);
			header[8] = 'W';
			header[9] = 'A';
			header[10] = 'V';
			header[11] = 'E';
			header[12] = 'f'; // 'fmt ' chunk
			header[13] = 'm';
			header[14] = 't';
			header[15] = ' ';
			header[16] = 16; // 4 bytes: size of 'fmt ' chunk
			header[17] = 0;
			header[18] = 0;
			header[19] = 0;
			header[20] = 1; // format = 1
			header[21] = 0;
			header[22] = (byte) channels;
			header[23] = 0;
			header[24] = (byte) (longSampleRate & 0xff);
			header[25] = (byte) ((longSampleRate >> 8) & 0xff);
			header[26] = (byte) ((longSampleRate >> 16) & 0xff);
			header[27] = (byte) ((longSampleRate >> 24) & 0xff);
			header[28] = (byte) (byteRate & 0xff);
			header[29] = (byte) ((byteRate >> 8) & 0xff);
			header[30] = (byte) ((byteRate >> 16) & 0xff);
			header[31] = (byte) ((byteRate >> 24) & 0xff);
			header[32] = (byte) (channels * 16 / 8); // block align
			header[33] = 0;
			header[34] = 16; // bits per sample
			header[35] = 0;
			header[36] = 'd';
			header[37] = 'a';
			header[38] = 't';
			header[39] = 'a';
			header[40] = (byte) (totalAudioLen & 0xff);
			header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
			header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
			header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
			out.write(header, 0, 44);
		}
	}

	/**确认SD是否可以用**/
	public static boolean checkSD(){
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**获取SD完整路径**/
	public static String getSDPath(String path){
		return Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + path;
	}

	/**获取路径并创建资料夹**/
	public static File getPathFile(String path) {
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		return file;
	}

	/**关闭流**/
	public static void close(Closeable closeable) {
		if(closeable!= null){
			try {
				closeable.close();
				closeable = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
