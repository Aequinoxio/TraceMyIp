package aequinoxio.tracemyip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: verificare con intent.getAction(); che il filtro sia android.net.conn.CONNECTIVITY_CHANGE
        // TODO: usare GCMNetworkManager per aumentare l'efficienza e ridurre i consumi (best bractice Android Development site)
        NetworkState networkState = NetworkState.getInstance(context);
        networkState.updateState();
        //Log.v("Broadcast***:","Ricevuto"); // Eliminare dopo il debug
    }
}
