package aequinoxio.tracemyip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkState networkState = NetworkState.getInstance(context);
        networkState.updateState();
        Log.v("Broadcast***:","Ricevuto"); // TODO: Eliminare dopo il debug
    }
}
