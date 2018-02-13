package aequinoxio.tracemyip.DatabaseUtils;

/**
 * Created by utente on 11/02/2018.
 */

public class DataRow {
    String timestamp;
    String ip;
    String networkInterface;

    @Override
    public String toString() {
        return String.format("%s: %s - %s",timestamp,networkInterface,ip);
    }
}
