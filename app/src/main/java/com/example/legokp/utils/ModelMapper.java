package com.example.legokp.utils;

import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.models.LegoSet;

import java.util.ArrayList;
import java.util.List;

public class ModelMapper {

    // Convert LegoSet (API model) to LegoSetEntity (Database model)
    public static LegoSetEntity toEntity(LegoSet legoSet) {
        return new LegoSetEntity(
                legoSet.getSetNum(),
                legoSet.getName(),
                legoSet.getYear(),
                legoSet.getTheme(),
                legoSet.getNumParts(),
                legoSet.getSetImgUrl(),
                legoSet.getPrice(),
                legoSet.getRating(),
                legoSet.getAgeRange(),
                legoSet.isExclusive(),
                legoSet.isInStock(),
                legoSet.isFavorite(),
                legoSet.getDescription()
        );
    }

    // Convert LegoSetEntity (Database model) to LegoSet (API model)
    public static LegoSet toModel(LegoSetEntity entity) {
        LegoSet legoSet = new LegoSet();
        legoSet.setSetNum(entity.getSetNum());
        legoSet.setName(entity.getName());
        legoSet.setYear(entity.getYear());
        legoSet.setTheme(entity.getTheme());
        legoSet.setNumParts(entity.getNumParts());
        legoSet.setSetImgUrl(entity.getSetImgUrl());
        legoSet.setPrice(entity.getPrice());
        legoSet.setRating(entity.getRating());
        legoSet.setAgeRange(entity.getAgeRange());
        legoSet.setExclusive(entity.isExclusive());
        legoSet.setInStock(entity.isInStock());
        legoSet.setFavorite(entity.isFavorite());
        legoSet.setDescription(entity.getDescription());
        return legoSet;
    }

    // Convert list of LegoSet to list of LegoSetEntity
    public static List<LegoSetEntity> toEntityList(List<LegoSet> legoSets) {
        List<LegoSetEntity> entities = new ArrayList<>();
        if (legoSets != null) {
            for (LegoSet legoSet : legoSets) {
                entities.add(toEntity(legoSet));
            }
        }
        return entities;
    }

    // Convert list of LegoSetEntity to list of LegoSet
    public static List<LegoSet> toModelList(List<LegoSetEntity> entities) {
        List<LegoSet> legoSets = new ArrayList<>();
        if (entities != null) {
            for (LegoSetEntity entity : entities) {
                legoSets.add(toModel(entity));
            }
        }
        return legoSets;
    }
}