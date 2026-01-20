package com.gaston.lesbegueris.notepases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DataBaseManager {

	//le doy un nombre a la tabla, y a cada una de las columnas
	public static final String TABLE_NAME = "tabla";
	public static final String CN_ID = "_id";
	public static final String CN_NOMBRE = "nombre";
	public static final String CN_LATITUDE = "latitude2";
	public static final String CN_LONGITUDE ="longitude2";
	public static final String CN_ADDRESS ="address";
	public static final String CN_FECHA = "fecha";
	public static final String CN_NOTA = "nota";


	
	private DbHelper helper;
	private SQLiteDatabase db;
	 
	
	//creo la tabla
	public static final String CREATE_TABLE ="create table " +TABLE_NAME+ " ("
			+ CN_ID + " integer primary key autoincrement,"
			+ CN_NOMBRE + " text,"
			+ CN_LATITUDE + " text not null,"
			+ CN_LONGITUDE + " text not null,"
			+ CN_ADDRESS + " text,"
			+ CN_FECHA + " text,"
			+ CN_NOTA + " text)";
	

	
	private static final String Where = null;
	
	
	public DataBaseManager(Context context){
		helper = new DbHelper(context);
		db = helper.getWritableDatabase();
	}



	public ContentValues generarContentValues(String _id,String nombre, String latitude2, String longitude2, String address,
			String fecha, String nota){
		ContentValues valores = new ContentValues();
		valores.put(CN_ID, _id);
		valores.put(CN_NOMBRE, nombre);
		valores.put(CN_LATITUDE, latitude2);
		valores.put(CN_LONGITUDE, longitude2);
		valores.put(CN_ADDRESS, address);
		valores.put(CN_FECHA, fecha);
		valores.put(CN_NOTA, nota);

		return valores;
		
	}

	public void insertar(String nombre, String latitude2, String longitude2, String address, String fecha, String nota){
		db.insert(TABLE_NAME, null, generarContentValues(null, nombre, latitude2, longitude2, address, fecha, nota));
		
	}

	public void eliminar(String _id){
		db.delete(TABLE_NAME, CN_ID + "=" + _id, null);
	}
	public void agregarFaborita(String nota, String nombre){

		String strSQL = "UPDATE " + TABLE_NAME +" SET " + CN_NOTA +" ="+ nota+" WHERE "+ CN_NOMBRE +" ='"+ nombre +"'";
		db.execSQL(strSQL);

	}

	public Cursor MostrarUbicaciones(){
		return db.rawQuery("SELECT * FROM "+ TABLE_NAME, null);

	}
	
	public Cursor MostrarFavoritas(){
		return db.rawQuery("SELECT * FROM "+ TABLE_NAME +" WHERE " + CN_NOTA +"='fav'", null);
	}

	public void modificarUbicacion(String _id, String nombre, String latitude2, String longitude2,
								   String address, String fecha, String nota){
		db.update(TABLE_NAME, generarContentValues( _id, nombre, latitude2, longitude2,
				address, fecha, nota),
				CN_ID +"="+ _id, null);
	}

	public Cursor verRepetidos(String latitude, String longitude) {
		return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "+ CN_LATITUDE+"='"
				+ latitude +"' AND "+ CN_LONGITUDE +" ='"+longitude+"'", null);


	}

}
