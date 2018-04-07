package aequinoxio.tracemyip.DatabaseUtils;

/**
 * Created by utente on 11/02/2018.
 */

public class DataRow {
    String timestamp;
    String ip;
    String networkInterface;

    public String getIp(){return ip;}
    public String getData(){return timestamp==null?null:timestamp.split(" ")[0];}

    @Override
    public String toString() {
        if (timestamp == null && networkInterface == null) {
            return ip;
        } else {
            return String.format("%s: %s - %s", timestamp, networkInterface, ip);
        }
    }

    public String toExportString() {

       // return String.format("%s\t%s\t%s%n", timestamp, networkInterface, ip);
        if (timestamp == null && networkInterface == null) {
            return ip;
        } else {
            return String.format("%s\t%s\t%s%n", timestamp, networkInterface, ip);
        }
    }
}
