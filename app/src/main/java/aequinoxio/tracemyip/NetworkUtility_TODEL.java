package aequinoxio.tracemyip;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

// TODO: CANCELLARE - INTEGRATA IN NetworkState

/**
 * Created by utente on 11/02/2018.
 */

public class NetworkUtility_TODEL {
    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("ERROR**", ex.toString());
        } // for now eat exceptions
        return "";
    }

    public String getNetworkState(Context context){
        String connType;
       // EditText edtLog = (EditText) context.findViewById(R.id.edtLog);
        String ip2="";
        WifiManager wm;

        ConnectivityManager cm =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected){
            connType = "No connection";
        }else {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_BLUETOOTH:
                    connType = "BlueTooth";
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    connType = "Ethernet";
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    connType = "Mobile";
                    break;
                case ConnectivityManager.TYPE_MOBILE_DUN:
                    connType = "Mobile DUN";
                    break;
                case ConnectivityManager.TYPE_VPN:
                    connType = "VPN";
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    connType = "WIFI";
                    wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
                    if (wm.isWifiEnabled()) {
                        int ipAddress = wm.getConnectionInfo().getIpAddress();
                        //   String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                        ip2 = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                    } else {
                        ip2 = "Disabled";
                    }
                    break;
                case ConnectivityManager.TYPE_WIMAX:
                    connType = "WIMAX";
                    break;
                case ConnectivityManager.TYPE_DUMMY:
                    connType = "Dummy";
                    break;
                default:
                    connType = "Unknown";
            }
        }

        return connType;
    }

//
//@NonNull
//private String getPublicIPAddress() {
//StringBuilder sb = new StringBuilder();
//String line;
//try {
//URL url = new URL("http://wtfismyip.com/text");
//HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//httpURLConnection.getResponseCode();
//InputStream is= httpURLConnection.getInputStream();
//BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
//while ((line = bufferedReader.readLine()) != null) {
//sb.append(line);
//}
//httpURLConnection.disconnect();
//} catch (MalformedURLException e) {
//e.printStackTrace();
//} catch (IOException e) {
//e.printStackTrace();
//}
//
//return sb.toString();
//
//}
}
