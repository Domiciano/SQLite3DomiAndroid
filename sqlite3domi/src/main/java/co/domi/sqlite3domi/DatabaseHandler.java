package co.domi.sqlite3domi;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler {

    private static DatabaseHandler instance = null;
    private SQLiteOpenHelper connection;
    private Context context;
    private String dbname;
    private int version = -1;

    public static DatabaseHandler getInstance(){
        if(instance == null){
            instance = new DatabaseHandler();
        }
        return instance;
    }

    public void selectDatabase(String dbname, int version){
        this.dbname = dbname;
        this.version = version;
    }

    private DatabaseHandler() {

    }

    public void initialize(Context context){
        this.context = context;
        verifyDBVersion();
    }



    private void verifyDBVersion(){
        if(version == -1){
            Log.e(">>>SQLite", "Version is undefined");
            return;
        }

        create("CREATE TABLE IF NOT EXISTS db_version(id INTEGER PRIMARY KEY)");
        ResultSet results = query("SELECT * FROM db_version WHERE id = (SELECT MAX(id) FROM db_version)");
        if (results.next() ){
            int lastversion = results.getIntAt(0);
            if (version == lastversion) {
                Log.e(">>>SQLite", "You have opened successfully the database at version "+version);
            }else if (version>lastversion){
                onDatabaseUpdate();
                create("INSERT OR IGNORE INTO db_version(id) VALUES ({version})".replace("{version}",""+version));
                Log.e(">>>SQLite","The version has been updated to version "+version);
            }else {
                Log.e(">>>SQLite","Important warning! the version you try to access is no longer available. Current version is "+lastversion);
            }
        }else{
            internalExecute("INSERT OR IGNORE INTO db_version(id) VALUES ({version})".replace("{version}",""+version));
        }

    }

    private void onDatabaseUpdate(){
        ResultSet results = query("SELECT * FROM sqlite_master WHERE type='table'");
        while(results.next()){
            String tablename = results.getStringAt(1);
            if (tablename.equals("sqlite_sequence")){
                continue;
            }
            if (tablename.equals("db_version")){
                continue;
            }
            create("DROP TABLE '{tablename}'".replace("{tablename}",tablename));
            Log.e(">>>SQLite","Table "+tablename+" updated to new version");
        }

    }

    private SQLiteDatabase openDatabase(){
        if(dbname == null){
            Log.e(">>>SQLite", "dbname is undefined");
            return null;
        }

        connection = new SQLiteOpenHelper(this.context, dbname, null, version) {
            @Override
            public void onCreate(SQLiteDatabase db) {}
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
        };
        return connection.getWritableDatabase();
    }



    public void create(String sql){
        SQLiteDatabase db = openDatabase();
        try {
            db.execSQL(sql);
        }catch (SQLException ex){
            ex.printStackTrace();
        }finally {
            db.close();
            connection.close();
        }
    }

    private void internalExecute(String sql){
        SQLiteDatabase db = openDatabase();
        try {
            db.execSQL(sql);
        }catch (SQLException ex){
            Log.e(">>>SQLite","ERROR: "+ex.getLocalizedMessage());
        }finally {
            db.close();
            connection.close();
        }
    }

    public void execute(String sql){
        SQLiteDatabase db = openDatabase();
        try {
            db.execSQL(sql);
            Log.e(">>>SQLite","OK: "+sql);
        }catch (SQLException ex){
            Log.e(">>>SQLite","ERROR: "+ex.getLocalizedMessage());
        }finally {
            db.close();
            connection.close();
        }
    }

    public ResultSet query(String sql){
        ResultSet resultSet = null;
        SQLiteDatabase db = openDatabase();
        try {
            Cursor cursor = db.rawQuery(sql, null);
            int columnNumber = cursor.getColumnCount();
            int rowNumber = cursor.getCount();
            String[][] data = new String[rowNumber][columnNumber];

            int rowPointer = 0;
            if(cursor.moveToFirst()) {
                do{
                    String[] row = new String[columnNumber];
                    for (int i=0 ; i<columnNumber ; i++){
                        String chunk = cursor.getString(i);
                        row[i] = chunk;
                    }
                    data[rowPointer] = row;
                    rowPointer++;
                }while (cursor.moveToNext());
            }
            resultSet = new ResultSet(data);
        }catch (SQLException ex){
            Log.e(">>>SQLite","ERROR: "+ex.getLocalizedMessage());
        }finally {
            db.close();
            connection.close();
        }
        return resultSet;
    }

}
