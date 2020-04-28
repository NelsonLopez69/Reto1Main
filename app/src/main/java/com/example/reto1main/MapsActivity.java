package com.example.reto1main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {


    private GoogleMap mMap;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private ArrayList<com.google.android.gms.maps.model.Marker> markers;

    private Location locationPerson;
    private MarkerOptions person;
    private LatLng markerLocationDefault;

    private ImageButton addButton;
    private TextView infoText;
    private EditText nameMarkerText;
    private Button saveNameButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        markers = new ArrayList<Marker>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addButton = findViewById(R.id.addButton);
        infoText = findViewById(R.id.infoText);

        listenerAddButton();

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 11);
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
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                System.out.println("here ha entrado prog");
                locationPerson =location;
                updateLocationPerson(locationPerson);
                markerLocationDefault = new LatLng(location.getLatitude()+0.0008, location.getLongitude()-0.0002);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 11) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,1,locationListener);

                if(location ==null) System.out.println("locationNull");
                updateLocationPerson(location);
            }
        }
    }

    public void listenerAddButton(){
        addButton.setOnClickListener(
                (v)-> {
                showDialog();
                }
        );
    }



    public void showDialog(){
        {
            // Build an AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.layout, null);

            builder.setCancelable(false);

            builder.setView(dialogView);

            nameMarkerText = (EditText) dialogView.findViewById(R.id.editName);
            saveNameButton = (Button) dialogView.findViewById(R.id.okBtn);

            // Create the alert dialog
            final AlertDialog dialog = builder.create();

            listenerSaveNameButton(dialog, dialogView);

            // Display the custom alert dialog on interface
            dialog.show();
        }
    }

    public void listenerSaveNameButton(AlertDialog dialog, View dialogView){
        saveNameButton.setOnClickListener((v)-> {
            final EditText editNameMarker = (EditText) dialogView.findViewById(R.id.editName);
            String nameMarker = editNameMarker.getText().toString();

            addMarker(nameMarker);
            updateInfotext();
            dialog.cancel();
        });
    }


    public void updateInfotext(){
        if(markers.size()>0) {
            Marker lugarCercano = lugarMasCercano();
            String nombreLugar = lugarCercano.getTitle();

            Location ubicacionLugarCercano = new Location(LocationManager.GPS_PROVIDER);
            ubicacionLugarCercano.setLatitude(lugarCercano.getPosition().latitude);
            ubicacionLugarCercano.setLongitude(lugarCercano.getPosition().longitude);

            float distancia = ubicacionLugarCercano.distanceTo(locationPerson);



            if (distancia >= 40)
                infoText.setText("El lugar más cercano es " + nombreLugar);
            else
                infoText.setText("Usted está en " + nombreLugar);

            //Hacer que este metodo se ejecute en dos casos, cuando sse cambia de posicion el usuario
            //y cuando se anada un nuevo marker.
        }
    }


    public void addMarker(String name){
        markerLocationDefault = new LatLng(locationPerson.getLatitude()+0.0008, locationPerson.getLongitude()-0.0002);
        markers.add(mMap.addMarker(new MarkerOptions()
                    .position(markerLocationDefault).draggable(true)
                    .title(name)));
    }

    public void updateLocationPerson(Location location){
        locationPerson = location;
        if(person==null) {
            person = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()));
            person.icon(BitmapDescriptorFactory.fromResource(R.drawable.personlocation));
            mMap.addMarker(person);

            float zoomLevel = 16.0f;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationPerson.getLatitude(), locationPerson.getLongitude()), zoomLevel));
        }else
           person.position(new LatLng(location.getLatitude(), location.getLongitude()));
    }


    private void setWindowInfo(String address) {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(MapsActivity.this);
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(MapsActivity.this);
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setText(address);
                    info.addView(title);
                return info;
            }
        });
    }


    private String getAddress(Location location) {
        String completeAddress=null;
        String fullAdd = "";
        String city = "";
        String country = "";


        Geocoder geocoder= new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses.size()>0){
            Address address = addresses.get(0);
            fullAdd = address.getAddressLine(0);
            city = address.getLocality();
            country = address.getCountryName();
        }

        completeAddress = "Usted se encuentra en "+fullAdd;
        return completeAddress;
    }


    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.hideInfoWindow();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        updateInfotext();

        Location locationMarker = new Location(LocationManager.NETWORK_PROVIDER);
        locationMarker.setLatitude(marker.getPosition().latitude);
        locationMarker.setLongitude(marker.getPosition().longitude);

        int distance = (int)locationMarker.distanceTo(locationPerson);

        //Condicionales para controlar el tipo de informacion que se mostrara al usuario cuando este de click en alguno de los marcadores:
        //Si el marcador clickeado es el de la persona se muestra un tipo de informacion diferente a cmparacion de si se da click a otro tipo de marcador
        if(marker.isDraggable()==false)
            setWindowInfo(getAddress(locationPerson));
        else
            setWindowInfo(marker.getTitle()+", usted se encuentra a "+distance+"m del lugar");

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Location locationMarker = new Location(LocationManager.GPS_PROVIDER);
        locationMarker.setLatitude(marker.getPosition().latitude);
        locationMarker.setLongitude(marker.getPosition().longitude);

        int distance = (int)locationMarker.distanceTo(locationPerson);

        //Condicionales para controlar el tipo de informacion que se mostrara al usuario cuando este de click en alguno de los marcadores:
        //Si el marcador clickeado es el de la persona se muestra un tipo de informacion diferente a cmparacion de si se da click a otro tipo de marcador

       if(marker.isDraggable()==false)
           setWindowInfo(getAddress(locationPerson));
       else
           setWindowInfo(marker.getTitle()+", usted se encuentra a "+distance+"m del lugar");

       return false;
    }




   /* public double calcularDsitancia(LatLng loc1, LatLng loc2){
        double distancia = 0;


        double lat = Math.abs(loc1.latitude - loc2.latitude);
        double lon  =  Math.abs(loc1.longitude - loc2.longitude);

        //El ponderado correspondiente a la distancia se calcula sumando la latitud y la longitud en su valor absoluto
        distancia = lat + lon;

        return  distancia;
    }*/


    public Marker lugarMasCercano(){
        Marker lugarMasCercano = null;

        if(markers.size()>0) {

        Location ubicacionMarcador = new Location(LocationManager.GPS_PROVIDER);

        double [][] distancias = new double[markers.size()][2];
        for (int i = 0; i < markers.size(); i++) {

            ubicacionMarcador.setLatitude(markers.get(i).getPosition().latitude);
            ubicacionMarcador.setLongitude(markers.get(i).getPosition().longitude);

            double distancia = ubicacionMarcador.distanceTo(locationPerson);

            distancias[i][0] = distancia;
            distancias[i][1] = i; //Anexo a cada distancia el indice de cada marker de la lista "markers".
        }

            double mayor, menor;
            mayor = menor = distancias [0][0];

            for (int i = 0; i < distancias.length; i++) {
                if(distancias[i][0] > mayor) {
                    mayor = distancias[i][0];
                }
                if(distancias[i][0]<menor) {
                    menor = distancias[i][0];
                }
            }


            //Busco el menor numero en el arreglo y extraigo su respectivo indice del arrayList de markers.
            boolean encontrado =false;
            int indiceMenor= 0;
            for (int i = 0; i < distancias.length && !encontrado; i++) {
                if(distancias[i][0] == menor){
                    indiceMenor = i;
                    encontrado = true;
                }
            }
            lugarMasCercano = markers.get(indiceMenor);
        }
    return lugarMasCercano;
    }
}