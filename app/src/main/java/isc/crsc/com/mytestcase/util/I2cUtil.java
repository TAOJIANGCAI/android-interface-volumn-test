package isc.crsc.com.mytestcase.util;

public class I2cUtil {

    static {
        System.loadLibrary("native-lib");
    }

    public native int open(String nodeName);

    public native int read(int fileHander, int i2c_adr, int buf[], int Length);

    public native int write(int fileHander, int i2c_adr, int sub_adr, int buf[], int Length);

    public native void close(int fileHander);

}
