package name.peterbukhal.android.youtrack.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import name.peterbukhal.android.youtrack.proto.Ping;
import name.peterbukhal.android.youtrack.util.impl.LocationListenerImpl;

import static android.provider.Settings.Secure.ANDROID_ID;

/**
 * Created on 12/09/16 by
 *
 * @author Peter Bukhal (petr@taxik.ru)
 */
public final class YouTrackService extends Service {

    private static final String LOG_TAG = "YouTrackService";

    private class YouTrackThread extends Thread {

        private InetSocketAddress mInetSocketAddress;
        private DatagramSocket mSocket;

        public YouTrackThread() throws IOException {
            super("YouTrackThread");

            mInetSocketAddress = new InetSocketAddress(mServer, mPort);
            mSocket = new DatagramSocket();
        }

        @Override
        public void run() {
            while (true) {
                if (mLocation.equals(DEFAULT_LOCATION)) continue;

                try {
                    final byte[] data;

                    if (mOutput.equals(OUTPUT_PROTOBUF)) {
                         data = new Ping.Builder()
                                .provider(mLocation.getProvider())
                                .time(mLocation.getTime())
                                .latitude(mLocation.getLatitude())
                                .longitude(mLocation.getLongitude())
                                .altitude(mLocation.getAltitude())
                                .speed(mLocation.getSpeed())
                                .bearing(mLocation.getBearing())
                                .accuracy(mLocation.getAccuracy())
                                .uid(mAndroidId)
                                .build()
                                .encode();
                    } else if (mOutput.equals(OUTPUT_JSON)) {
                        name.peterbukhal.android.youtrack.json.Ping ping =
                                new name.peterbukhal.android.youtrack.json.Ping(
                                        mLocation.getProvider(), mLocation.getTime(),
                                        mLocation.getLatitude(), mLocation.getLongitude(),
                                        (float) mLocation.getAltitude(), mLocation.getSpeed(),
                                        mLocation.getBearing(), mLocation.getAccuracy(),
                                        mAndroidId, false);

                        data = new Gson()
                                .toJson(ping)
                                .getBytes();
                    } else {
                        stopSelf();
                        interrupt();

                        return;
                    }

                    final DatagramPacket sendPacket = new DatagramPacket(
                            data, data.length, mInetSocketAddress);
                    mSocket.send(sendPacket);

                    TimeUnit.SECONDS.sleep(mSendInterval);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "", e);
                }
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private LocationManager mLocationManager;
    private ExecutorService mExecutor;

    private String mAndroidId;
    private String mServer;
    private int mPort;
    private int mSendInterval;
    private int mUpdateInterval;
    private boolean mUseGps;
    private boolean mUseNetwork;
    private String mOutput;

    public static final String EXTRA_SERVER_NAME = "extra_server_name";
    public static final String EXTRA_SERVER_PORT = "extra_server_port";
    public static final String EXTRA_SEND_INTERVAL = "extra_send_interval";
    public static final String EXTRA_UPDATE_INTERVAL = "extra_update_interval";
    public static final String EXTRA_USE_GPS = "extra_use_gps";
    public static final String EXTRA_USE_NETWORK = "extra_use_network";
    public static final String EXTRA_OUTPUT = "extra_output";

    public static final String OUTPUT_PROTOBUF = "protobuf";
    public static final String OUTPUT_JSON = "json";

    @Override
    public void onCreate() {
        super.onCreate();

        mAndroidId = Secure.getString(getContentResolver(), ANDROID_ID);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mExecutor = Executors.newSingleThreadExecutor();
    }

    private static final Location DEFAULT_LOCATION = new Location("none");
    private volatile Location mLocation = DEFAULT_LOCATION;

    private LocationListener mGpsLocationListener = new LocationListenerImpl() {

        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;
        }

    };

    private LocationListener mNetworkLocationListener = new LocationListenerImpl() {

        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;
        }

    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras().containsKey(EXTRA_SERVER_NAME)
                && intent.getExtras().containsKey(EXTRA_SERVER_PORT)
                && intent.getExtras().containsKey(EXTRA_SEND_INTERVAL)
                && intent.getExtras().containsKey(EXTRA_UPDATE_INTERVAL)
                && intent.getExtras().containsKey(EXTRA_USE_GPS)
                && intent.getExtras().containsKey(EXTRA_USE_NETWORK)
                && intent.getExtras().containsKey(EXTRA_OUTPUT)) {
            mServer = intent.getExtras().getString(EXTRA_SERVER_NAME);
            mPort = intent.getExtras().getInt(EXTRA_SERVER_PORT);
            mSendInterval = intent.getExtras().getInt(EXTRA_SEND_INTERVAL);
            mUpdateInterval = intent.getExtras().getInt(EXTRA_UPDATE_INTERVAL);
            mUseGps = intent.getExtras().getBoolean(EXTRA_USE_GPS);
            mUseNetwork = intent.getExtras().getBoolean(EXTRA_USE_NETWORK);
            mOutput = intent.getExtras().getString(EXTRA_OUTPUT);
        } else {
            stopSelf();

            return 0;
        }

        try {
            if (mUseGps) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        mUpdateInterval * 1000, 0.0F, mGpsLocationListener);
            }

            if (mUseNetwork) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        mUpdateInterval * 1000, 0.0F, mNetworkLocationListener);
            }

            mExecutor.execute(new YouTrackThread());
        } catch (Exception e) {
            //
        }

        Log.i(LOG_TAG, "YouTrackService has started.");

        return 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            mExecutor.shutdownNow();
            mLocationManager.removeUpdates(mGpsLocationListener);
            mLocationManager.removeUpdates(mNetworkLocationListener);
        } catch (Exception e) {
            //
        }

        Log.i(LOG_TAG, "YouTrackService has stopped.");
    }

}
