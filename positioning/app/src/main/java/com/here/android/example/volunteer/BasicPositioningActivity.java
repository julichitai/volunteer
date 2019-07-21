/*
 * Copyright (c) 2011-2019 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.here.android.example.volunteer;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.here.android.mpa.cluster.ClusterLayer;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.LocationDataSourceHERE;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.SupportMapFragment;
import com.here.android.mpa.mapping.MapState;
import com.here.android.positioning.StatusListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BasicPositioningActivity extends AppCompatActivity implements PositioningManager.OnPositionChangedListener, Map.OnTransformListener {

    // permissions request code
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    // map embedded in the map fragment
    private Map map;

    // map fragment embedded in this activity
    private SupportMapFragment mapFragment;

    // positioning manager instance
    private PositioningManager mPositioningManager;

    // HERE location data source instance
    private LocationDataSourceHERE mHereLocation;

    // flag that indicates whether maps is being transformed
    private boolean mTransforming;

    // callback that is called when transforming ends
    private Runnable mPendingUpdate;

    // text view instance for showing location information
    private TextView mLocationInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasPermissions(this, RUNTIME_PERMISSIONS)) {
            initializeMapsAndPositioning();
        } else {
            ActivityCompat
                    .requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPositioningManager != null) {
            mPositioningManager.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPositioningManager != null) {
            mPositioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR);
        }
    }

    @Override
    public void onPositionUpdated(final PositioningManager.LocationMethod locationMethod, final GeoPosition geoPosition, final boolean mapMatched) {
        final GeoCoordinate coordinate = geoPosition.getCoordinate();
        if (mTransforming) {
            mPendingUpdate = new Runnable() {
                @Override
                public void run() {
                    onPositionUpdated(locationMethod, geoPosition, mapMatched);
                }
            };
        } else {
            map.setCenter(coordinate, Map.Animation.BOW);
        }
    }

    @Override
    public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {
        // ignored
    }

    @Override
    public void onMapTransformStart() {
        mTransforming = true;
    }

    @Override
    public void onMapTransformEnd(MapState mapState) {
        mTransforming = false;
        if (mPendingUpdate != null) {
            mPendingUpdate.run();
            mPendingUpdate = null;
        }
    }

    /**
     * Only when the app's target SDK is 23 or higher, it requests each dangerous permissions it
     * needs when the app is running.
     */
    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                for (int index = 0; index < permissions.length; index++) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {

                        /*
                         * If the user turned down the permission request in the past and chose the
                         * Don't ask again option in the permission request system dialog.
                         */
                        if (!ActivityCompat
                                .shouldShowRequestPermissionRationale(this, permissions[index])) {
                            Toast.makeText(this, "Required permission " + permissions[index]
                                            + " not granted. "
                                            + "Please go to settings and turn on for sample app",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Required permission " + permissions[index]
                                    + " not granted", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                initializeMapsAndPositioning();
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private SupportMapFragment getMapFragment() {
        return (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }

    /**
     * Initializes HERE Maps and HERE Positioning. Called after permission check.
     */
    private void initializeMapsAndPositioning() {
        setContentView(R.layout.activity_main);
        mapFragment = getMapFragment();
        mapFragment.setRetainInstance(false);

        // Set path of isolated disk cache
        String diskCacheRoot = Environment.getExternalStorageDirectory().getPath()
                + File.separator + ".isolated-here-maps";
        // Retrieve intent name from manifest
        String intentName = "";
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            intentName = bundle.getString("INTENT_NAME");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(this.getClass().toString(), "Failed to find intent name, NameNotFound: " + e.getMessage());
        }

        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(diskCacheRoot, intentName);
        if (!success) {
            // Setting the isolated disk cache was not successful, please check if the path is valid and
            // ensure that it does not match the default location
            // (getExternalStorageDirectory()/.here-maps).
            // Also, ensure the provided intent name does not match the default intent name.
        } else {
            mapFragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                    if (error == OnEngineInitListener.Error.NONE) {
                        map = mapFragment.getMap();
                        map.setCenter(new GeoCoordinate(55.164440, 61.436844, 0.0), Map.Animation.NONE);
                        map.setZoomLevel(map.getMaxZoomLevel() - 1);
                        map.addTransformListener(BasicPositioningActivity.this);
                        ClusterLayer cl = new ClusterLayer();
                        ArrayList<GeoCoordinate> list = new ArrayList<GeoCoordinate>();
                        list.add(new GeoCoordinate(55.11227, 61.62441));
                        list.add(new GeoCoordinate(55.17775, 61.48313));
                        list.add(new GeoCoordinate(55.15108, 61.36839));
                        list.add(new GeoCoordinate(55.14661, 61.39378));
                        list.add(new GeoCoordinate(55.17172, 61.45715));
                        list.add(new GeoCoordinate(55.16996, 61.46522));
                        list.add(new GeoCoordinate(55.15314, 61.37366));
                        list.add(new GeoCoordinate(53.40018, 58.97266));
                        list.add(new GeoCoordinate(53.42052, 58.99443));
                        list.add(new GeoCoordinate(53.41759, 58.99385));
                        list.add(new GeoCoordinate(53.41114, 58.95427));
                        list.add(new GeoCoordinate(53.43677, 58.98395));
                        list.add(new GeoCoordinate(54.90321, 61.45227));
                        list.add(new GeoCoordinate(55.11686, 59.71145));
                        list.add(new GeoCoordinate(54.98998, 57.29084));
                        list.add(new GeoCoordinate(55.18256, 61.48292));
                        list.add(new GeoCoordinate(55.30891, 61.2596));
                       // list.add(new GeoCoordinate(57.31376, 65.66981));
                        for(int i = 0; i < list.size(); i++){
                            MapMarker mm = new MapMarker();
                            mm.setCoordinate(list.get(i));
                            cl.addMarker(mm);
                        }
                        map.addClusterLayer(cl);
                        mPositioningManager = PositioningManager.getInstance();
                        mHereLocation = LocationDataSourceHERE.getInstance(
                                new StatusListener() {
                                    @Override
                                    public void onOfflineModeChanged(boolean offline) {
                                        // called when offline mode changes
                                    }

                                    @Override
                                    public void onAirplaneModeEnabled() {
                                        // called when airplane mode is enabled
                                    }

                                    @Override
                                    public void onWifiScansDisabled() {
                                        // called when Wi-Fi scans are disabled
                                    }

                                    @Override
                                    public void onBluetoothDisabled() {
                                        // called when Bluetooth is disabled
                                    }

                                    @Override
                                    public void onCellDisabled() {
                                        // called when Cell radios are switch off
                                    }

                                    @Override
                                    public void onGnssLocationDisabled() {
                                        // called when GPS positioning is disabled
                                    }

                                    @Override
                                    public void onNetworkLocationDisabled() {
                                        // called when network positioning is disabled
                                    }

                                    @Override
                                    public void onServiceError(ServiceError serviceError) {
                                        // called on HERE service error
                                    }

                                    @Override
                                    public void onPositioningError(PositioningError positioningError) {
                                        // called when positioning fails
                                    }

                                    @Override
                                    @SuppressWarnings("deprecation")
                                    public void onWifiIndoorPositioningNotAvailable() {
                                        // called when running on Android 9.0 (Pie) or newer
                                    }

                                    @Override
                                    public void onWifiIndoorPositioningDegraded() {
                                        // called when running on Android 9.0 (Pie) or newer
                                    }
                                });
                        if (mHereLocation == null) {
                            Toast.makeText(BasicPositioningActivity.this, "LocationDataSourceHERE.getInstance(): failed, exiting", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        mPositioningManager.setDataSource(mHereLocation);
                        mPositioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(
                                BasicPositioningActivity.this));
                        // start position updates, accepting GPS, network or indoor positions
                        try {
                            if (mPositioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR)) {
                                mapFragment.getPositionIndicator().setVisible(true);
                            } else {
                                Toast.makeText(BasicPositioningActivity.this, "PositioningManager.start: failed, exiting", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } catch (Exception e) {
                            Log.e("HERE", "Caught: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(BasicPositioningActivity.this, "onEngineInitializationCompleted: error: " + error + ", exiting", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        }
    }

    private void setLocationMethod() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] names = getResources().getStringArray(R.array.locationMethodNames);
        builder.setTitle(R.string.title_select_location_method)
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            final String[] values = getResources().getStringArray(R.array.locationMethodValues);
                            final PositioningManager.LocationMethod method =
                                    PositioningManager.LocationMethod.valueOf(values[which]);
                            setLocationMethod(method);
                        } catch (IllegalArgumentException ex) {
                            Toast.makeText(BasicPositioningActivity.this, "setLocationMethod failed: "
                                    + ex.getMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            dialog.dismiss();
                        }
                    }
                });
        builder.create().show();
    }

    private void setLocationMethod(PositioningManager.LocationMethod method) {
        if (!mPositioningManager.start(method)) {
            Toast.makeText(BasicPositioningActivity.this, "PositioningManager.start(" + method + "): failed", Toast.LENGTH_LONG).show();
        }
    }
}