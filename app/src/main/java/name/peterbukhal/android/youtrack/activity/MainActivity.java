package name.peterbukhal.android.youtrack.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import name.peterbukhal.android.youtrack.R;
import name.peterbukhal.android.youtrack.service.YouTrackService;

import static java.lang.String.valueOf;
import static name.peterbukhal.android.youtrack.service.YouTrackService.EXTRA_OUTPUT;
import static name.peterbukhal.android.youtrack.service.YouTrackService.EXTRA_SEND_INTERVAL;
import static name.peterbukhal.android.youtrack.service.YouTrackService.EXTRA_SERVER_NAME;
import static name.peterbukhal.android.youtrack.service.YouTrackService.EXTRA_SERVER_PORT;
import static name.peterbukhal.android.youtrack.service.YouTrackService.EXTRA_UPDATE_INTERVAL;
import static name.peterbukhal.android.youtrack.service.YouTrackService.EXTRA_USE_GPS;
import static name.peterbukhal.android.youtrack.service.YouTrackService.EXTRA_USE_NETWORK;
import static name.peterbukhal.android.youtrack.service.YouTrackService.OUTPUT_JSON;
import static name.peterbukhal.android.youtrack.service.YouTrackService.OUTPUT_PROTOBUF;

/**
 * Created on 13/09/16 by
 *
 * @author Peter Bukhal (petr@taxik.ru)
 */
public final class MainActivity extends AppCompatActivity {

    private EditText mEtServerName;
    private EditText mEtServerPort;
    private EditText mEtSendInterval;
    private EditText mEtUpdateInterval;
    private CheckBox mCbUseGps;
    private CheckBox mCbUseNetwork;
    private RadioGroup mRgOutputType;

    private SharedPreferences mSharedPreferences;

    public static final String SHARED_PREFERENCES_KEY__SERVER_NAME = "server_name";
    public static final String SHARED_PREFERENCES_KEY__SERVER_PORT = "server_port";
    public static final String SHARED_PREFERENCES_KEY__SEND_INTERVAL = "send_interval";
    public static final String SHARED_PREFERENCES_KEY__UPDATE_INTERVAL = "update_interval";
    public static final String SHARED_PREFERENCES_KEY__USE_GPS = "use_gps";
    public static final String SHARED_PREFERENCES_KEY__USE_NETWORK = "use_network";
    public static final String SHARED_PREFERENCES_KEY__OUTPUT_TYPE = "output_type";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_main);

        mEtServerName = (EditText) findViewById(R.id.serverName);
        mEtServerPort = (EditText) findViewById(R.id.serverPort);
        mEtSendInterval = (EditText) findViewById(R.id.sendInterval);
        mEtUpdateInterval = (EditText) findViewById(R.id.updateInterval);
        mCbUseGps = (CheckBox) findViewById(R.id.gps_provider);
        mCbUseNetwork = (CheckBox) findViewById(R.id.network_provider);
        mRgOutputType = (RadioGroup) findViewById(R.id.output_type);
        mRgOutputType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.output_type_protobuf:
                        mOutputType = OUTPUT_PROTOBUF;
                        break;
                    case R.id.output_type_json:
                        mOutputType = OUTPUT_JSON;
                        break;
                }
            }
        });

        mSharedPreferences = getSharedPreferences("main", MODE_PRIVATE);

        mEtServerName.setText(mSharedPreferences.getString(SHARED_PREFERENCES_KEY__SERVER_NAME, ""));
        mEtServerPort.setText(mSharedPreferences.getInt(SHARED_PREFERENCES_KEY__SERVER_PORT, 0) == 0
                ? "" : valueOf(mSharedPreferences.getInt(SHARED_PREFERENCES_KEY__SERVER_PORT, 0)));
        mEtSendInterval.setText(mSharedPreferences.getInt(SHARED_PREFERENCES_KEY__SEND_INTERVAL, 0) == 0
                ? "" : valueOf(mSharedPreferences.getInt(SHARED_PREFERENCES_KEY__SEND_INTERVAL, 0)));
        mEtUpdateInterval.setText(mSharedPreferences.getInt(SHARED_PREFERENCES_KEY__UPDATE_INTERVAL, 0) == 0
                ? "" : valueOf(mSharedPreferences.getInt(SHARED_PREFERENCES_KEY__UPDATE_INTERVAL, 0)));
        mCbUseGps.setChecked(mSharedPreferences.getBoolean(SHARED_PREFERENCES_KEY__USE_GPS, false));
        mCbUseNetwork.setChecked(mSharedPreferences.getBoolean(SHARED_PREFERENCES_KEY__USE_NETWORK, true));

        init();
    }

    private void init() {
        /**
         * Проверяем есть ли доступ к точному местоположению, если есть то идем дальше,
         * иначе запрашиваем разрешение у пользователя, если разрешение получено, то
         * идем дальше, иначе выходим из приложения так как для дальнейшей работы
         * обязательно требуется доступ к точному местоположению.
         */
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.attention))
                        .setMessage(getString(R.string.request_permission_message, getString(R.string.location_service)))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION },
                                        REQUEST_PERMISSION_LOCATION);
                            }
                        })
                        .setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION },
                        REQUEST_PERMISSION_LOCATION);
            }
        }
    }

    private static final int REQUEST_PERMISSION_LOCATION = 53212;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    finish();
                }
            } break;
        }
    }

    private String mOutputType = OUTPUT_PROTOBUF;

    public void onLaunchClicked(View view) {
        if (((ToggleButton) view).isChecked()) {
            final String serverName = mEtServerName.getText().toString();
            final int serverPort = Integer.valueOf(mEtServerPort.getText().toString());
            final int sendInterval = Integer.valueOf(mEtSendInterval.getText().toString());
            final int updateInterval = Integer.valueOf(mEtUpdateInterval.getText().toString());
            final boolean useGps = mCbUseGps.isChecked();
            final boolean useNetwork = mCbUseNetwork.isChecked();
            final String outputType = mOutputType;

            if (TextUtils.isEmpty(serverName) || serverPort <= 0) {
                Toast.makeText(this, R.string.set_server_and_port, Toast.LENGTH_LONG).show();

                return;
            }

            mSharedPreferences
                    .edit()
                    .putString(SHARED_PREFERENCES_KEY__SERVER_NAME, serverName)
                    .putInt(SHARED_PREFERENCES_KEY__SERVER_PORT, serverPort)
                    .putInt(SHARED_PREFERENCES_KEY__SEND_INTERVAL, sendInterval)
                    .putInt(SHARED_PREFERENCES_KEY__UPDATE_INTERVAL, updateInterval)
                    .putBoolean(SHARED_PREFERENCES_KEY__USE_GPS, useGps)
                    .putBoolean(SHARED_PREFERENCES_KEY__USE_NETWORK, useNetwork)
                    .putString(SHARED_PREFERENCES_KEY__OUTPUT_TYPE, outputType)
                    .apply();

            startService(new Intent(this, YouTrackService.class)
                    .putExtra(EXTRA_SERVER_NAME, serverName)
                    .putExtra(EXTRA_SERVER_PORT, serverPort)
                    .putExtra(EXTRA_SEND_INTERVAL, sendInterval)
                    .putExtra(EXTRA_UPDATE_INTERVAL, updateInterval)
                    .putExtra(EXTRA_USE_GPS, useGps)
                    .putExtra(EXTRA_USE_NETWORK, useNetwork)
                    .putExtra(EXTRA_OUTPUT, outputType));

            mEtServerName.setEnabled(false);
            mEtServerPort.setEnabled(false);
            mEtSendInterval.setEnabled(false);
            mEtUpdateInterval.setEnabled(false);
            mCbUseGps.setEnabled(false);
            mCbUseNetwork.setEnabled(false);
            mRgOutputType.setEnabled(false);
        } else {
            stopService(new Intent(this, YouTrackService.class));

            mEtServerName.setEnabled(true);
            mEtServerPort.setEnabled(true);
            mEtSendInterval.setEnabled(true);
            mEtUpdateInterval.setEnabled(true);
            mCbUseGps.setEnabled(true);
            mCbUseNetwork.setEnabled(true);
            mRgOutputType.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, YouTrackService.class));
    }

}
