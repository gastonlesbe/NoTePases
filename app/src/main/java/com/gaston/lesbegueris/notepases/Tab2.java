package com.gaston.lesbegueris.notepases;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import static android.text.TextUtils.substring;

/**
 * Created by gaston on 05/12/17.
 */

public class Tab2 extends Activity implements OnClickListener {


    SimpleCursorAdapter adapter;

    double latitude2, longitude2;

    private ListView listFavoritos, lista;
    private DataBaseManager manager;
    private Cursor cursor;
    private boolean trakingOn = false;
    private String name, nota, address, str_id, nombre;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab2);


       // listUltimos = (ListView) findViewById(R.id.listFavoritos);

        lista = (ListView) findViewById(R.id.listFavoritos);

        manager = new DataBaseManager(this);
        String[] direccion= new String[]{manager.CN_ADDRESS};
        String destino = "direccion";



        String[] from = new String[]{manager.CN_NOMBRE, manager.CN_ADDRESS};
        int[] to = new int[]{R.id.txtNombreFav, R.id.txtAddressfav};

        cursor = manager.MostrarFavoritas();
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            destino = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            direccion = destino.split( ",",1);
            adapter = new SimpleCursorAdapter(this,
                    R.layout.lista_favoritos, cursor, from, to, 0);
            lista.setAdapter(adapter);


            new LoadDataTask().execute();
        } else {

            //Toast.makeText(getBaseContext(), primerAviso, Toast.LENGTH_LONG).show();
        }


        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                //obtener el cursor con el id correspondiente
                cursor = (Cursor) listView.getItemAtPosition(position);
                str_id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                latitude2 = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("latitude2")));
                longitude2 = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("longitude2")));
                address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                //name = nombre.substring( 0, nombre.indexOf(","));
                nota = cursor.getString(cursor.getColumnIndexOrThrow("nota"));

                irFavorita();
            }
        });
    }

    @Override
    public void onClick(View v) {
        irFavorita();
    }

    private void irFavorita() {

        //latitude2 = Double.parseDouble(String.valueOf(txtlatitude2));
        //longitude2 = Double.parseDouble(String.valueOf(txtlongitude2));
        //trakingOn = false;


        Intent intent = new Intent(Tab2.this, MapsActivity.class);
        intent.putExtra("str_id", str_id);
        intent.putExtra("nombre", nombre);
        intent.putExtra("latitude2", latitude2);
        intent.putExtra("longitude2", longitude2);
        //X intent.putExtra("address", address);
        intent.putExtra("trakingOn", trakingOn);
        intent.putExtra("nota", nota);


        //Toast.makeText(getApplicationContext(),"lat: "+ latitude2+ " lon: "+longitude2, Toast.LENGTH_LONG).show();
        startActivity(intent);

    }


    private class LoadDataTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "Loading data...", Toast.LENGTH_SHORT).show();
        }

        protected void onPostExecute(Void unused) {
            adapter.changeCursor(cursor);
        }
    }
}