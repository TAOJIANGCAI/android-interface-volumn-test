package isc.crsc.com.mytestcase.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Android shell 命令执行器，支持无限个命令串型执行（需要有root权限！！）
  *
 *      int result = new ShellCommandExecutor()
 *                  .addCommand("mount -o remount,rw /system")
 *                  .addCommand("cp /sdcard/Download/bootanimation.zip /system/media")
 *                  .addCommand("cd /system/media")
 *                  .addCommand("chmod 777 bootanimation.zip")
 *                  .execute();
 *
 */


public class ShellCommandExecutor {
    private static final String TAG = "ShellCommandExecutor";

    private StringBuilder mCommands;

    public ShellCommandExecutor() {
        mCommands = new StringBuilder();
    }

    public int execute() {
        return execute(mCommands.toString());
    }

    public ShellCommandExecutor addCommand(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            throw new IllegalArgumentException("command can not be null.");
        }
        mCommands.append(cmd);
        mCommands.append("\n");
        return this;
    }

    private static int execute(String command) {
        int result = -1;
        DataOutputStream dos = null;
        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            Log.i(TAG, command);
            dos.writeBytes(command + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
