#include <jni.h>
#include <string>
#include <stdio.h>
#include <android/log.h>
#include "./noise_suppression.h"
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <errno.h>

#include <unistd.h>
#include <string.h>
//#include <linux/delay.h>
#include <linux/time.h>
#include <errno.h>
#include <assert.h>
#include <string.h>

#include <stdlib.h>
#include <unistd.h>
#include <linux/i2c.h>
#include <linux/i2c-dev.h>

#define CHIP_ADDR  0xa0   //mcu i2c addr
#define I2C_DEV   "/dev/i2c-1"    // register i2c B bus

#define LOG_TAG "i2c"        //android logcat

static const char *TAG = "NdkGpio";
#define LOG_Info(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOG_Debug(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOG_Error(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

NsHandle *nsHandle = NULL;
enum nsLevel {
    kLow,
    kModerate,
    kHigh,
    kVeryHigh
};
#ifndef MIN
#define MIN(A, B)        ((A) < (B) ? (A) : (B))
#endif

#define   LOG_TAG    "NATIVE_LOG"
#define   LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define   LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define   LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


unsigned char i2c_buf[64];
unsigned int cat5132_spk_addr = 0x28; //喇叭通路的地址
unsigned int cat5132_ear_addr = 0x2B; //听筒通路的地址

//return the volume(1~127) on success
//return -1 when error happens
int cat5132_Get_SPK_vol_level(int fd) {
    //check i2c device works fine or not;
    if (ioctl(fd, I2C_SLAVE_FORCE, cat5132_spk_addr) < 0) {
        LOGD("Error->Cant find i2c SPK chip !!!\n");
        return -1;
    } else {
        LOGD("PASS->find SPK cat5132 chip.\n");
    }

    //WCR Write Operation
    i2c_buf[0] = 0x02;  //AR Address
    i2c_buf[1] = 0x80;  //DR(00h) Selection
    if (write(fd, &i2c_buf, 2) < 0) {
        LOGD("write i2c error!!!\n");
        return -1;
    }

    //DR  READ Operation
    i2c_buf[0] = 0x00;  //DR(00h) Selection
    if (write(fd, &i2c_buf, 1) < 0) {
        LOGD("write i2c error!!!\n");
        return -1;
    }
    //read DR data
    if (read(fd, i2c_buf, 1) != 1) {
        LOGD("write i2c error!!!\n");
        return -1;
    } else {
        LOGD("read i2c ok 0x%02X \n", i2c_buf[0]);//tofix
        return (int) i2c_buf[0];
    }

}

//Set spk volume 0~127
//return the volume(1~127) on success
//return -1 when error happens
int cat5132_Set_SPK_volume(char vol_spk, int fd) {
    if (vol_spk > 127) {
        vol_spk = 127;
    } else if (vol_spk < 0) {
        vol_spk = 0;
    }
    //check i2c device works fine or not;
    if (ioctl(fd, I2C_SLAVE_FORCE, cat5132_spk_addr) < 0) {
        LOGD("Error->Cant find i2c SPK chip !!!\n");
        return -1;
    } else {
        LOGD("PASS->find SPK cat5132 chip.\n");
    }

    //WCR Write Operation
    i2c_buf[0] = 0x02;  //AR Address
    i2c_buf[1] = 0x80;  //DR(00h) Selection
    if (write(fd, &i2c_buf, 2) < 0) {
        LOGD("write i2c error!!!\n");
        return -1;
    }

    //DR  READ Operation
    i2c_buf[0] = 0x00;  //DR(00h) Selection
    i2c_buf[1] = vol_spk;
    if (write(fd, &i2c_buf, 2) < 0) {
        LOGD("write i2c error!!!\n");
        return -1;
    }

    return cat5132_Get_SPK_vol_level(fd);
}

//return the volume(1~127) on success
//return -1 when error happens
int cat5132_Get_EAR_vol_level(int fd) {
    //check i2c device works fine or not;
    if (ioctl(fd, I2C_SLAVE_FORCE, cat5132_ear_addr) < 0) {
        LOGD("Error->Cant find i2c ear chip !!!\n");
        return -1;
    } else {
        LOGD("PASS->find ear cat5132 chip.\n");//tofix
    }

    //WCR Write Operation
    i2c_buf[0] = 0x02;  //AR Address
    i2c_buf[1] = 0x80;  //DR(00h) Selection
    if (write(fd, &i2c_buf, 2) < 0) {
        LOGD("write i2c error!!!\n");
        return -1;
    }

    //DR  READ Operation
    i2c_buf[0] = 0x00;  //DR(00h) Selection
    if (write(fd, &i2c_buf, 1) < 0) {
        LOGD("write i2c error!!!\n");
        return -1;
    }
    //read DR data
    if (read(fd, i2c_buf, 1) != 1) {
        LOGD("write i2c error!!!\n");
        return -1;
    } else {
        LOGD("read i2c ok 0x%02X \n", i2c_buf[0]);//tofix
        return (int) i2c_buf[0];
    }

}

//Set ear volume 0~127
//return the volume(1~127) on success
//return -1 when error happens
int cat5132_Set_EAR_volume(char vol_ear, int fd) {
    if (vol_ear > 127) {
        vol_ear = 127;
    } else if (vol_ear < 0) {
        vol_ear = 0;
    }

    //check i2c device works fine or not;
    if (ioctl(fd, I2C_SLAVE_FORCE, cat5132_ear_addr) < 0) {
        LOGD("Error->Cant find i2c ear chip !!!\n");
        return -1;
    } else {
        LOGD("PASS->find ear cat5132 chip.\n");//tofix
    }

    //WCR Write Operation
    i2c_buf[0] = 0x02;  //AR Address
    i2c_buf[1] = 0x80;  //DR(00h) Selection
    if (write(fd, &i2c_buf, 2) < 0) {
        LOGD("write i2c error!!!\n");
        return -1;
    }

    //DR  READ Operation
    i2c_buf[0] = 0x00;  //DR(00h) Selection
    i2c_buf[1] = vol_ear;
    if (write(fd, &i2c_buf, 2) < 0) {
        LOGD("write i2c error!!!\n");
        return -1;
    }

    return cat5132_Get_EAR_vol_level(fd);
}


//读取ADC电压值
float ads7828_Read_ADC_Result(int ch, int fd) {
    unsigned int temp;
    unsigned int ads7828_AP;
    float adc_result[4];

    switch (ch) {
        case 0:
            i2c_buf[0] = 0x01;  //AP
            i2c_buf[1] = 0xC5;  //Config MSB
            i2c_buf[2] = 0x83;  //Config LSB
            break;
        case 1:
            i2c_buf[0] = 0x01;  //AP
            i2c_buf[1] = 0xD5;  //Config MSB
            i2c_buf[2] = 0x83;  //Config LSB
            break;
        case 2:
            i2c_buf[0] = 0x01;  //AP
            i2c_buf[1] = 0xE5;  //Config MSB
            i2c_buf[2] = 0x83;  //Config LSB
            break;
        case 3:
            i2c_buf[0] = 0x01;  //AP
            i2c_buf[1] = 0xF5;  //Config MSB
            i2c_buf[2] = 0x83;  //Config LSB
            break;
        default:
            break;
    }

    if (write(fd, &i2c_buf, 3) < 0) LOGD("write i2c error!!!\n");
    //else  printf("write i2c ok!!!\n");

    usleep(1000);       //Wait for ADC convert finish

    ads7828_AP = 0x00;

    if (write(fd, &ads7828_AP, 1) < 0) LOGD("write i2c error!!!\n");
    //else  printf("write i2c ok!!!\n");

    if (read(fd, i2c_buf, 2) != 2) LOGD("read i2c error!!!\n");
    //else printf("read i2c ok!!!\n");

    temp = (i2c_buf[0] << 8) + i2c_buf[1];

    temp = temp >> 4;

    adc_result[ch] = ((float) temp / 2048) * 2.048;     //12bit including 1 bit sign;

//    printf("i2c read ADC CH[%d]:%f", ch, adc_result[ch]);

    return adc_result[ch];
}

int open() {
    int fd;

    fd = open("/dev/i2c-0", O_RDWR);
    LOGI("fd value is %d ", fd);
    if (fd < 0) {
        //LOGD("HELLO");
        LOGD("error to open i2c!!!  %s", strerror(errno));
        return -1;
    }
    return fd;
}

int check(int fd) {
    unsigned int ads1015_addr = 0x49; //0x48
    if (ioctl(fd, I2C_TENBIT, 0) <
        0) {  //对应的arg取值为0：从机地址为7 bit；对应的arg取值为1：从机地址为10bit。用来指定I2C从机地址的位数；
        LOGD("set i2c address error!!!\n");
        return -1;
    }
    //check i2c device works fine or not;
    if (ioctl(fd, I2C_SLAVE_FORCE, ads1015_addr) < 0) { //对应的arg取值为I2C从机地址，用来修改I2C从机地址
        LOGD("i2c slave address check failed!!!\n");
        return -1;
    } else {
        LOGD("i2c slave address check ok!!!\n");
    }

    if (ioctl(fd, I2C_TIMEOUT, 1) < 0) {
        LOGD("set i2c timeout error!!!\n");
        return -1;
    }

    if (ioctl(fd, I2C_RETRIES, 3) < 0) {
        LOGD("set i2c retry error!!!\n");
        return -1;
    }
    return fd;
}


int readData(const char *filePath) {
    int fd;
    int value;
    fd = open(filePath, O_RDONLY);
    if (fd < 0) {
        return -1;
    }
    char valueStr[32];
    memset(valueStr, 0, sizeof(valueStr));
    read(fd, (void *) valueStr, sizeof(valueStr) - 1);
    char *end;
    if (strncmp(valueStr, "in", 2) == 0) {
        value = 0;
    } else if (strncmp(valueStr, "out", 3) == 0) {
        value = 1;
    } else {
        value = strtol(valueStr, &end, 0);
        if (end == valueStr) {
            close(fd);
            return -1;
        }
    }

    close(fd);
    return value;
}

int writeData(const char *data, int count, const char *filePath) {
    int fd;
    fd = open(filePath, O_WRONLY);
    if (fd < 0) {
        return -1;
    }
    int ret = write(fd, data, count);
    close(fd);
    return 0;
}


extern "C" JNIEXPORT jstring
JNICALL
Java_isc_crsc_com_mytestcase_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C"
JNIEXPORT void JNICALL
Java_isc_crsc_com_mytestcase_util_AudioRecorder_ns_1init(JNIEnv *env, jobject instance) {
    // TODO
    LOGD("start!!!!!!!!!!!");
    if (nsHandle != NULL)
        return;
    nsHandle = WebRtcNs_Create();
    int status = WebRtcNs_Init(nsHandle, 8000);
    if (status != 0) {
        LOGD("WebRtcNs_Init fail");
    }
    status = WebRtcNs_set_policy(nsHandle, kModerate);
    if (status != 0) {
        LOGD("WebRtcNs_set_policy fail");
    }
    LOGD("end!!!!!!!!!!!");
}


int ns_process(int16_t *buffer, uint32_t sampleRate, int samplesCount, enum nsLevel level) {
    if (buffer == NULL) return -1;
    if (samplesCount == 0) return -1;
    size_t samples = MIN(160, sampleRate / 100);
    if (samples == 0) return -1;
    uint32_t num_bands = 1;
    int16_t *input = buffer;
    size_t nTotal = (samplesCount / samples);
    int i = 0;
    for (i = 0; i < nTotal; i++) {
        int16_t *nsIn[1] = {input};   //ns input[band][data]
        int16_t *nsOut[1] = {input};  //ns output[band][data]
        WebRtcNs_Analyze(nsHandle, nsIn[0]);
        WebRtcNs_Process(nsHandle, (const int16_t *const *) nsIn, num_bands, nsOut);
        input += samples;
    }
    return 1;
}

extern "C"
JNIEXPORT void JNICALL
Java_isc_crsc_com_mytestcase_util_AudioRecorder_ns_1process(JNIEnv *env, jobject instance,
                                                            jbyteArray buffer_, jint len) {
    jbyte *buffer = env->GetByteArrayElements(buffer_, NULL);

    // TODO

    if (nsHandle != NULL)
        ns_process(reinterpret_cast<int16_t *>(buffer), 8000, 640, kModerate);

    env->ReleaseByteArrayElements(buffer_, buffer, 0);
}

extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_util_GpioControl_nativeReadGpio(JNIEnv *env, jobject instance,
                                                             jstring path) {
//    const char *path = env->GetStringUTFChars(path, 0);
    // TODO
    if (path == NULL) {
        return -1;
    }
    const char *chars = env->GetStringUTFChars(path, NULL);
    int ret = readData(chars);
    env->ReleaseStringUTFChars(path, chars);
    return ret;
//    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_util_GpioControl_nativeWriteGpio(JNIEnv *env, jobject instance,
                                                              jstring path, jstring value) {
//    const char *path = env->GetStringUTFChars(path_, 0);
//    const char *value = env->GetStringUTFChars(value_, 0);
//接收到 /sys/class/gpio/gpio22/value 和 0/1的字符串值到 nativeWriteGpio
    // TODO
    if (path == NULL) {
        return -1;
    }
    const char *chars = env->GetStringUTFChars(path, NULL);
    const char *valueStr = env->GetStringUTFChars(value, NULL);
    int ret = writeData(valueStr, strlen(valueStr), chars);
    env->ReleaseStringUTFChars(path, chars);
    env->ReleaseStringUTFChars(value, valueStr);
    return ret;

//    env->ReleaseStringUTFChars(path_, path);
//    env->ReleaseStringUTFChars(value_, value);
}


static int read_eeprom(int fd, char buff[], int addr, int count) {
    int res;
    int i;

    for (i = 0; i < PAGE_SIZE; i++) {
        buff[i] = 0;
    }


    if (write(fd, &addr, 1) != 1)
        return -1;
    usleep(10000);
    res = read(fd, buff, count);
    LOGI("read %d byte at 0x%.2x\n", res, addr);
    for (i = 0; i < PAGE_SIZE; i++) {
        LOGI("0x%.2x, ", buff[i]);
    }

    return res;
}

static int write_eeprom(int fd, char buff[], int addr, int count) {
    int res;
    int i;
    char sendbuffer[PAGE_SIZE + 1];

    memcpy(sendbuffer + 1, buff, count);
    sendbuffer[0] = addr;

    res = write(fd, sendbuffer, count + 1);
    LOGI("write %d byte at 0x%.2x\n", res, addr);
    usleep(10000);

    for (i = 0; i < PAGE_SIZE; i++) {
        LOGI("0x%.2x, ", buff[i]);
    }
}


extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_util_I2cUtil_open(JNIEnv *env, jobject obj, jstring file) {
//    const char *nodeName = env->GetStringUTFChars(file, 0);

    char fileName[64];
    const char *str;

    str = env->GetStringUTFChars(file, 0);
    if (str == NULL) {
        LOGI("Can't get file name!");
        return -1;
    }
    sprintf(fileName, "%s", str);
    LOGI("will open i2c device node %s", fileName);
    env->ReleaseStringUTFChars(file, str);
    return open(fileName, O_RDWR);

//    env->ReleaseStringUTFChars(nodeName_, nodeName);
}


extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_util_I2cUtil_read(JNIEnv *env, jobject obj, jint fileHander,
                                               jint slaveAddr, jintArray bufArr, jint len) {
//    jint *buf = env->GetIntArrayElements(buf_, NULL);

    jint *bufInt;
    char *bufByte;
    int res = 0, i = 0, j = 0;
    if (len <= 0) {
        LOGE("I2C: buf len <=0");
        goto err0;
    }
    bufInt = (jint *) malloc(len * sizeof(int));
    if (bufInt == 0) {
        LOGE("I2C: nomem");
        goto err0;
    }
    bufByte = (char *) malloc(len);
    if (bufByte == 0) {
        LOGE("I2C: nomem");
        goto err1;
    }

    env->GetIntArrayRegion(bufArr, 0, len, bufInt);


    memset(bufByte, '\0', len);
    if ((j = read(fileHander, bufByte, len)) != len) {
        LOGE("read fail in i2c read jni i = %d buf 4", i);
        goto err2;
    } else {
        for (i = 0; i < j; i++)
            bufInt[i] = bufByte[i];
        LOGI("return %d %d %d %d in i2c read jni", bufByte[0], bufByte[1], bufByte[2], bufByte[3]);
        env->SetIntArrayRegion(bufArr, 0, len, bufInt);
    }

    free(bufByte);
    free(bufInt);
    return j;

    err2:
    free(bufByte);
    err1:
    free(bufInt);
    err0:
    return -1;
//    env->ReleaseIntArrayElements(buf_, buf, 0);
}

extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_util_I2cUtil_write(JNIEnv *env, jobject obj, jint fileHander,
                                                jint slaveAddr, jint mode, jintArray bufArr,
                                                jint len) {
//    jint *buf = env->GetIntArrayElements(buf_, NULL);

#if 1
    jint *bufInt;
    char *bufByte;
    int res = 0, i = 0, j = 0;

    if (len <= 0) {
        LOGE("I2C: buf len <=0");
        goto err0;
    }

    bufInt = (jint *) malloc(len * sizeof(int));
    if (bufInt == 0) {
        LOGE("I2C: nomem");
        goto err0;
    }
    bufByte = (char *) malloc(len + 1);
    if (bufByte == 0) {
        LOGE("I2C: nomem");
        goto err1;
    }

    env->GetIntArrayRegion(bufArr, 0, len, bufInt);
    bufByte[0] = mode;
    for (i = 0; i < len; i++)
        bufByte[i + 1] = bufInt[i];

    if ((j = write(fileHander, bufByte, len + 1)) != len + 1) {
        LOGE("write fail in i2c");
        goto err2;
    }

    LOGI("I2C: write %d byte", j);
    free(bufByte);
    free(bufInt);

    return j - 1;

    err2:
    free(bufByte);
    err1:
    free(bufInt);
    err0:
    return -1;

#endif

//    env->ReleaseIntArrayElements(buf_, buf, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_isc_crsc_com_mytestcase_util_I2cUtil_close(JNIEnv *env, jobject instance, jint fileHander) {
    close(fileHander);
}


extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_MainActivity_open_1i2c(JNIEnv *env, jobject instance) {
    return open();
}

extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_MainActivity_check_1i2c(JNIEnv *env, jobject instance, jint fd) {

    // TODO
    return check(fd);

}

extern "C"
JNIEXPORT void JNICALL
Java_isc_crsc_com_mytestcase_MainActivity_close_1i2c(JNIEnv *env, jobject instance, jint fd) {

    close(fd);

}

extern "C"
JNIEXPORT jfloat JNICALL
Java_isc_crsc_com_mytestcase_MainActivity_readAdcResult(JNIEnv *env, jobject instance, jint i,
                                                        jint fd) {
    return ads7828_Read_ADC_Result(i, fd);

}
extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_MainActivity_setSparkVolume(JNIEnv *env, jobject instance,
                                                         jint vol_spk, jint fd) {

    return cat5132_Set_SPK_volume(vol_spk, fd);

}
extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_MainActivity_getSparkVolume(JNIEnv *env, jobject instance, jint fd) {

    return cat5132_Get_SPK_vol_level(fd);

}
extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_MainActivity_setEarVolume(JNIEnv *env, jobject instance, jint vol_ear,
                                                       jint fd) {

    return cat5132_Set_EAR_volume(vol_ear, fd);

}
extern "C"
JNIEXPORT jint JNICALL
Java_isc_crsc_com_mytestcase_MainActivity_getEarVolume(JNIEnv *env, jobject instance, jint fd) {

    return cat5132_Get_EAR_vol_level(fd);

}