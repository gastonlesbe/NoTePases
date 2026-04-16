package com.gaston.lesbegueris.notepases;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Tab2Fragment extends Fragment {
    private SimpleCursorAdapter adapter;
    private DataBaseManager manager;
    private Cursor cursor;
    private boolean trakingOn = false;

    private double latitude2;
    private double longitude2;
    private String address;
    private String nota;
    private String str_id;
    private String nombre;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView lista = view.findViewById(R.id.listFavoritos);
        manager = new DataBaseManager(requireContext());

        String[] from = new String[]{manager.CN_NOMBRE, manager.CN_ADDRESS};
        int[] to = new int[]{R.id.txtNombreFav, R.id.txtAddressfav};

        cursor = manager.MostrarFavoritas();
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
        adapter = new SimpleCursorAdapter(requireContext(),
                R.layout.lista_favoritos, cursor, from, to, 0) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                View btnDelete = row.findViewById(R.id.btnDeleteFav);
                View rowContent = row.findViewById(R.id.rowContent);
                Cursor rowCursor = (Cursor) getItem(position);
                String rowId = rowCursor.getString(rowCursor.getColumnIndexOrThrow("_id"));
                btnDelete.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.delete_address_title)
                            .setMessage(R.string.delete_address_message)
                            .setPositiveButton(R.string.delete_confirm, (dialog, which) -> {
                                manager.eliminar(rowId);
                                cursor = manager.MostrarFavoritas();
                                changeCursor(cursor);
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                });
                if (rowContent != null) {
                    rowContent.setOnClickListener(v ->
                            lista.performItemClick(row, position, getItemId(position)));
                }
                return row;
            }
        };
            lista.setAdapter(adapter);
            new LoadDataTask().execute();
        }

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View itemView,
                                    int position, long id) {
                cursor = (Cursor) listView.getItemAtPosition(position);
                str_id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                latitude2 = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("latitude2")));
                longitude2 = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("longitude2")));
                address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                nota = cursor.getString(cursor.getColumnIndexOrThrow("nota"));

                Intent intent = new Intent(requireContext(), MapsActivity.class);
                intent.putExtra("str_id", str_id);
                intent.putExtra("nombre", nombre);
                intent.putExtra("latitude2", latitude2);
                intent.putExtra("longitude2", longitude2);
                intent.putExtra("trakingOn", trakingOn);
                intent.putExtra("nota", nota);
                startActivity(intent);
            }
        });
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(requireContext(), "Loading data...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (adapter != null) {
                adapter.changeCursor(cursor);
            }
        }
    }
}
