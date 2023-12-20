package com.example.Simple2.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.Simple2.database.dao.NoteDao;
import com.example.Simple2.database.entities.Note;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {

    private static NoteDatabase sInstance;
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    @VisibleForTesting
    public static final String DATABASE_NAME = "My-database";

    public abstract NoteDao noteDao();

    public void setNoteDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

    public void updateNoteDatabaseCreated(final Context context){
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setNoteDatabaseCreated();
        }
    }

    public static NoteDatabase buildDatabase(final Context context) {
        return Room.databaseBuilder(context, NoteDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        NoteDatabase database = NoteDatabase.getInstance(context);
                        database.setNoteDatabaseCreated();
                    }
                }).allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }
    public static NoteDatabase getInstance(final Context context) {
        if (sInstance == null){
            synchronized (NoteDatabase.class) {
                if (sInstance == null){
                    sInstance = buildDatabase(context);
                    sInstance.updateNoteDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }


}
