package com.example.legokp.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.legokp.database.entity.LegoSetEntity;

import java.util.List;

@Dao
public interface LegoSetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LegoSetEntity legoSet);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LegoSetEntity> legoSets);

    @Update
    void update(LegoSetEntity legoSet);

    @Delete
    void delete(LegoSetEntity legoSet);

    @Query("DELETE FROM lego_sets")
    void deleteAll();

    @Query("SELECT * FROM lego_sets ORDER BY name ASC")
    LiveData<List<LegoSetEntity>> getAllSets();

    @Query("SELECT * FROM lego_sets WHERE set_num = :setNum LIMIT 1")
    LegoSetEntity getSetByNum(String setNum);

    @Query("SELECT * FROM lego_sets WHERE set_num = :setNum LIMIT 1")
    LiveData<LegoSetEntity> getSetByNumLive(String setNum);

    // ✅ LiveData версия для автообновления
    @Query("SELECT * FROM lego_sets WHERE is_favorite = 1 ORDER BY name ASC")
    LiveData<List<LegoSetEntity>> getFavoriteSets();

    // ✅ НОВОЕ: Синхронная версия для прямых запросов
    @Query("SELECT * FROM lego_sets WHERE is_favorite = 1 ORDER BY name ASC")
    List<LegoSetEntity> getFavoriteSetsSync();

    @Query("SELECT * FROM lego_sets WHERE theme = :theme ORDER BY name ASC")
    LiveData<List<LegoSetEntity>> getSetsByTheme(String theme);

    @Query("SELECT * FROM lego_sets WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    LiveData<List<LegoSetEntity>> searchSets(String searchQuery);

    @Query("UPDATE lego_sets SET is_favorite = :isFavorite WHERE set_num = :setNum")
    void updateFavoriteStatus(String setNum, boolean isFavorite);

    @Query("SELECT COUNT(*) FROM lego_sets")
    int getSetCount();

    @Query("SELECT COUNT(*) FROM lego_sets WHERE is_favorite = 1")
    int getFavoriteCount();

    @Query("SELECT * FROM lego_sets WHERE last_updated < :timestamp")
    List<LegoSetEntity> getOutdatedSets(long timestamp);

    @Query("SELECT COUNT(*) FROM lego_sets WHERE set_num = :setNum")
    int checkSetExists(String setNum);
}