package isc.crsc.com.mytestcase.UsbTest;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;

import isc.crsc.com.mytestcase.R;

public class UsbActivity extends Activity {
    private TextView usbText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usb_main);
        usbText = findViewById(R.id.usbText);
    }

    public void btnUsb(View view) {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        StringBuilder sb = new StringBuilder();
        while (deviceIterator.hasNext()) {
            UsbDevice usbDevice = deviceIterator.next();
            int deviceClass = usbDevice.getDeviceClass();
            if (deviceClass == 0) {
                UsbInterface anInterface = usbDevice.getInterface(0);
                int interfaceClass = anInterface.getInterfaceClass();
                if (interfaceClass == 8) {
                    sb.append("DeviceName=" + usbDevice.getDeviceName() + "\n");
                    sb.append("U 盘已插入");
                    usbText.setText(sb);
                }
            }
        }

    }


}

