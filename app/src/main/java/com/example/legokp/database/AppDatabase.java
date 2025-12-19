package com.example.legokp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.legokp.database.dao.LegoSetDao;
import com.example.legokp.database.dao.ReviewDao;
import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.database.entity.ReviewEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Главная база данных приложения
 * Версия 2: добавлена таблица отзывов
 */
@Database(
        entities = {
                LegoSetEntity.class,
                ReviewEntity.class  // ✨ НОВОЕ
        },
        version = 2,  // ✨ Увеличили версию
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract LegoSetDao legoSetDao();
    public abstract ReviewDao reviewDao();  // ✨ НОВОЕ

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "lego_database"
                            )
                            .fallbackToDestructiveMigration()  // При изменении версии пересоздать БД
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}