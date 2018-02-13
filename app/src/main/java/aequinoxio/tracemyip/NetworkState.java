package aequinoxio.tracemyip;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import aequinoxio.tracemyip.DatabaseUtils.DataAdapter;
import aequinoxio.tracemyip.DatabaseUtils.DataRow;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by utente on 11/02/2018.
 */

class NetworkState implements NetworkUpdateCallback {
    private static final NetworkState ourInstance = new NetworkState();
    private static Timestamp timestamp;
    private boolean isConnected = false;      // Internet connection
    private boolean isAirplaneMode = false;
    private Context context;
    DataAdapter dataAdapter;

    final String INSERT_VALUES="INSERT INTO IP (ip,interface) values (?,?)";

    // Imposto il timestamp in ora locale
    final String SELECT_VALUES="SELECT datetime(timestamp, 'localtime'),ip, interface FROM IP WHERE interface='External' ORDER BY timestamp DESC";

   // private List<Interface> allInterfaces;
    private List<NetworkInterface> networkInterfaces;
    private String ExternalIP;

    static NetworkState getInstance(Context context) {
        ourInstance.context = context;
        // Aggiorno il DB creandolo se necessario
        ourInstance.dataAdapter = new DataAdapter(context);
        ourInstance.dataAdapter.createDatabase();

        return ourInstance;
    }

    private NetworkState() {
        try {
            networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public List<DataRow> getData(){
        dataAdapter.open();
        List<DataRow> temp=dataAdapter.getValues(SELECT_VALUES);
        dataAdapter.close();
        return temp;
    }

    /**
     * Aggiorna lo stato interrogando le interfacce e cercando l'ip esterno
     */
    public void updateState() {
        try {
            //networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            // updateIPAddresses();

            ExternalIpAsyncTask externalIpAsyncTask = new ExternalIpAsyncTask(this);
            externalIpAsyncTask.execute();
            String ip = externalIpAsyncTask.get();

            //callback(ip);
            Log.e("IP:", ip); // TODO: Eliminare dopo il debug

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void callback(Object o) {
        ExternalIP = o.toString();
        timestamp = new Timestamp(System.currentTimeMillis());

        ArrayList<String> ifaces = new ArrayList<>();
        ArrayList<String> ifacesIP = new ArrayList<>();
        Intent networkStateIntent = new Intent(Constants.NETWORK_AVAILABLE_INTENT);

        networkStateIntent.putExtra(Constants.INTENT_OBJECTS_LENGTH, 1); // TODO: FIXIT
        networkStateIntent.putExtra(Constants.IS_NETWORK_AVAILABLE_DETAIL_INTENT, true); // TODO: FIXIT
        networkStateIntent.putExtra(Constants.INTENT_TIMESTAMP, timestamp.toString()); // TODO: FIXIT
        for (NetworkInterface networkInterface : networkInterfaces) {
            // TODO: Ogni interfaccia ha una lista di indirizzi. filtrare gli IPV4
            ifaces.add(networkInterface.getDisplayName());
            Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();
            InetAddress inetAddress;
            String sAddr = "";
            try {
                if (networkInterface.isUp()) {
                    while (addressEnumeration.hasMoreElements()) {
                        inetAddress = addressEnumeration.nextElement();
                        sAddr = inetAddress.getHostAddress();
                        if (sAddr.indexOf(':') < 0) {
                            break;// Mi fermo al primno indirizzo IPv4 che trovo
                        }
                    }
                } else {
                    sAddr = "Unconnected";
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            ifacesIP.add(sAddr); // Comunque aggiungo un indirizzo anche vuoto
        }

        ifaces.add("External");
        ifacesIP.add(ExternalIP);

        dataAdapter.open();
        // Scrivo tutto sul DB. Il timestamp è quello della scrittura e non quello dell'avio del recupero dell'external ip
        for (int i=0;i<ifaces.size();i++){
//            String insert_query=String.format("INSERT INTO IP (ip,interface) values ('%s','%s')",ifacesIP.get(i),ifaces.get(i));
//            dataAdapter.insertQuery(insert_query);
            dataAdapter.insertValues(INSERT_VALUES,ifacesIP.get(i),ifaces.get(i));
        }
        dataAdapter.close();

        networkStateIntent.putStringArrayListExtra(Constants.NETWORK_INTERFACE_NAME_DETAIL_INTENT, ifaces); // TODO: FIXIT
        networkStateIntent.putStringArrayListExtra(Constants.NETWORK_INTERFACE_IP_DETAIL_INTENT, ifacesIP); // TODO: FIXIT
        LocalBroadcastManager.getInstance(context).sendBroadcast(networkStateIntent);

    }
//
//    private void updateIPAddresses() {
//        allInterfaces = new ArrayList<>();
//        try {
//            for (NetworkInterface intf : networkInterfaces) {
//                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
//                for (InetAddress addr : addrs) {
//                    if (!addr.isLoopbackAddress()) {
//                        String sAddr = addr.getHostAddress();
//                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
//                        boolean isIPv4 = sAddr.indexOf(':') < 0;
//                        if (isIPv4) {
//                            Interface anInterface = new Interface();
//                            anInterface.STATE = intf.isUp() ? NetworkStates.CONNECTED : NetworkStates.DISCONNECTED;
//                            anInterface.NAME = intf.getName();
//                            anInterface.ADDRESS = sAddr;
//                            allInterfaces.add(anInterface);
//                        }
//                    }
//                }
//            }
//        } catch (SocketException ex) {
//            Log.e("ERROR**", ex.toString());
//        } // for now eat exceptions
//
//    }


    public String getNetworkState() {
        String connType;
        // EditText edtLog = (EditText) context.findViewById(R.id.edtLog);
        String ip2 = "";
        WifiManager wm;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            connType = "No connection";
        } else {
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

//    enum NetworkStates {
//        CONNECTED, CONNECTING, DISCONNECTED
//    }
//
//    class Interface {
//        String NAME;
//        NetworkStates STATE;
//        String ADDRESS;
//    }
}
