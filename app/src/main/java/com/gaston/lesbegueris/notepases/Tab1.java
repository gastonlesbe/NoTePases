package com.gaston.lesbegueris.notepases;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.view.View.OnClickListener;

/**
 * Created by gaston on 05/12/17.
 */

public class Tab1 extends Activity implements OnClickListener {


    SimpleCursorAdapter adapter;

    double latitude2, longitude2;

    private ListView listUltimos, lista;
    private DataBaseManager manager;
    private Cursor cursor;
    private boolean trakingOn = false;
    private String name, nota, address, str_id, nombre;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab1);


       // listUltimos = (ListView) findViewById(R.id.listUltimos);

        lista = (ListView) findViewById(R.id.listUltimos);

        manager = new DataBaseManager(this);
       String direccion= "address";
        String destino = "address";

        String[] from = new String[]{manager.CN_ADDRESS};
        //String[] destino = new String[]{ manager.CN_ADDRESS.split("\\,")};

        int[] to = new int[]{R.id.txtDireccion};

        cursor = manager.MostrarUbicaciones();
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            destino = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            direccion = destino.substring(destino.indexOf(","));
           //from = destino.split("\\,");
           //String from1 =  from[0];
            adapter = new SimpleCursorAdapter(this,
                    R.layout.lista_direcciones, cursor, from, to, 0);
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
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);
                str_id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                latitude2 = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("latitude2")));
                longitude2 = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("longitude2")));
                address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                nota = cursor.getString(cursor.getColumnIndexOrThrow("nota"));
                //name = nombre.substring( 0, nombre.indexOf(","));

                //latitude2 = Double.parseDouble(String.valueOf(txtlatitude2));
                //longitude2 = Double.parseDouble(String.valueOf(txtlongitude2));
                //trakingOn = false;


                Intent intent = new Intent(Tab1.this, MapsActivity.class);
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
        });
    }

    @Override
    public void onClick(View v) {

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