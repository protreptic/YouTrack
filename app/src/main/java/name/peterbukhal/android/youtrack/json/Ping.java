package name.peterbukhal.android.youtrack.json;

/**
 * Created on 13/09/16 by
 *
 * @author Peter Bukhal (petr@taxik.ru)
 */
public final class Ping {

    private String provider;
    private long time;
    private double latitude;
    private double longitude;
    private float altitude;
    private float speed;
    private float bearing;
    private float accuracy;
    private String uid;
    private boolean mock;

    public Ping(String provider, long time, double latitude, double longitude, float altitude, float speed, float bearing, float accuracy, String uid, boolean mock) {
        this.provider = provider;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.bearing = bearing;
        this.accuracy = accuracy;
        this.uid = uid;
        this.mock = mock;
    }

    public String getProvider() {
        return provider;
    }

    public long getTime() {
        return time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getAltitude() {
        return altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public float getBearing() {
        return bearing;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public String getUid() {
        return uid;
    }

    public boolean isMock() {
        return mock;
    }

    @Override
    public String toString() {
        return "Ping{" +
                "provider='" + provider + '\'' +
                ", time=" + time +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", speed=" + speed +
                ", bearing=" + bearing +
                ", accuracy=" + accuracy +
                ", uid='" + uid + '\'' +
                ", mock=" + mock +
                '}';
    }
}
