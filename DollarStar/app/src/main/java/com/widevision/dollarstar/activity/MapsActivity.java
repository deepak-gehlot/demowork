package com.widevision.dollarstar.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.widevision.dollarstar.R;
import com.widevision.dollarstar.dao.PostGsonClass;
import com.widevision.dollarstar.model.ImagesModel;
import com.widevision.dollarstar.util.ProgressLoaderHelper;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<ImagesModel> pinList = new ArrayList<>();
    private ArrayList<PostGsonClass.Data> myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        findViewById(R.id.back_BTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera

        myList = (ArrayList<PostGsonClass.Data>) getIntent().getSerializableExtra("post_list");
        if (myList != null && myList.size() != 0) {
            new BitmapTask().execute();
        } else {
            Toast.makeText(MapsActivity.this, "No image to show.", Toast.LENGTH_SHORT).show();
        }
    }

    class BitmapTask extends AsyncTask<Void, Void, Integer> {

        private ProgressLoaderHelper progressLoaderHelper;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressLoaderHelper = ProgressLoaderHelper.getInstance();
            progressLoaderHelper.showProgress(MapsActivity.this);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int a = 0;
            for (int i = 0; i < myList.size(); i++) {
                PostGsonClass.Data item = myList.get(i);
                if (item.lat != null && !item.lat.isEmpty() && item.lng != null && !item.lng.isEmpty() && !item.lat.equals("0") && !item.lng.equals("0")) {
                    generateBitmap(item.post_excerpt, "" + item.lat, "" + item.lng);
                } else {
                    a = a + 1;
                }
            }
            if (a == myList.size()) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            progressLoaderHelper.dismissProgress();
            if (i == 0) {
                Toast.makeText(MapsActivity.this, "No image to show.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateBitmap(String url, final String lat, final String lng) {
        final AjaxCallback<Bitmap> cb = new AjaxCallback<Bitmap>() {
            @Override
            public void callback(String url, Bitmap bm, AjaxStatus status) {
                // do whatever you want with bm (the image)
                ImagesModel item = new ImagesModel();
                item.setBitmap(Bitmap.createScaledBitmap(bm, 75, 75, false));
                item.setLat(lat);
                item.setLng(lng);
                LatLng location = new LatLng(Double.parseDouble(item.getLat()), Double.parseDouble(item.getLng()));
                mMap.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromBitmap(addWhiteBorder(item.getBitmap(), 3))));
                pinList.add(item);
            }
        };
        final AQuery aq = new AQuery(MapsActivity.this);
        aq.ajax(url, Bitmap.class, 0, cb);
    }

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }
}
