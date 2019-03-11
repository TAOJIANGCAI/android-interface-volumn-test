package isc.crsc.com.mytestcase.util;


public class GpioControl {

    // JNI
    public native int nativeReadGpio(String path);
    public native int nativeWriteGpio(String path, String value);

    private static final String  gpioExport     = "/sys/class/gpio/export";
    private static final String  gpioPath      = "/sys/class/gpio/gpio";
    private static final String  gpioDirection = "/direction";
    private static final String  gpioValue     = "/value";

    private String buildGpioPath(int num)                   //private 函数
    {
        String  numstr;
        numstr = Integer.toString(num);     //将gpio端口数字转换成对应的数字字符
        return gpioPath.concat(numstr);     //用concat转换成绝对gpio操作路径，如 /sys/class/gpio/gpio22
    }

    private int chmodGpio(int num)                   //读取gpio的输入输出状态，num为gpio口序列号，返回0为输入，返回1为输出
    {
        String dataPath = buildGpioPath(num);

        int result = new ShellCommandExecutor()
                .addCommand("chmod 777 ".concat(dataPath).concat("/direction"))
                .addCommand("chmod 777 ".concat(dataPath).concat("/value"))
                .execute();

        return result;
    }

    public int exportGpio(int num)                          //export gpio 端口
    {
        nativeWriteGpio(gpioExport, Integer.toString(num));
        return chmodGpio(num);
    }

    public int writeGpioValue(int num, int value)           //设置gpio输出电平，value=0为低电平，value=1为高电平，num为gpio口序列号
    {
        String dataPath = buildGpioPath(num).concat(gpioValue);     //转换为 /sys/class/gpio/gpio22/value

        return nativeWriteGpio(dataPath, Integer.toString(value));  //传递 /sys/class/gpio/gpio22/value 和 0/1的字符串值到 nativeWriteGpio
    }

    public int readGpioValue(int num)                       //读取gpio输入的高低电平值，num为gpio口序列号
    {
        String dataPath = buildGpioPath(num).concat(gpioValue);

        return nativeReadGpio(dataPath);
    }

    public int writeGpioDirection(int num, int value)       //设置gpio口输入输出状态，value=0为输入，value=1为输出，num为gpio口序列号
    {
        String dataPath = buildGpioPath(num).concat("/direction");
        String direct;
        if(value == 0)
        {
            direct = "in";
        }
        else if(value == 1)
        {
            direct = "out";
        }
        else
        {
            return -1;
        }
        return nativeWriteGpio(dataPath, direct);
    }

    public int readGpioDirection(int num)                   //读取gpio的输入输出状态，num为gpio口序列号，返回0为输入，返回1为输出
    {
        String dataPath = buildGpioPath(num).concat(gpioDirection);

        return nativeReadGpio(dataPath);
    }


}


