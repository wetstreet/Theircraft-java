import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chenyirun on 2017/3/21.
 */

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context){
        super(context, "map.db", null, 5);
    }

    public void onCreate(SQLiteDatabase db){
        String query = "create table if not exists state(" +
                "   x float not null" +
                "   y float not null" +
                "   z float not null" +
                "   pitch float not null" +
                "   yaw float not null" +
                "   roll float not null" +
                ");";
        db.execSQL(query);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
