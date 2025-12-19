package com.example.legokp.utils;

import com.example.legokp.database.entity.LegoSetEntity;
import com.example.legokp.database.entity.ReviewEntity;
import com.example.legokp.models.LegoSet;
import com.example.legokp.models.Review;

import java.util.ArrayList;
import java.util.List;

/**
 * Маппер для конвертации между моделями API и Entity базы данных
 */
public class ModelMapper {

    // ========== LEGO SETS ==========

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

    public static List<LegoSetEntity> toEntityList(List<LegoSet> legoSets) {
        List<LegoSetEntity> entities = new ArrayList<>();
        if (legoSets != null) {
            for (LegoSet legoSet : legoSets) {
                entities.add(toEntity(legoSet));
            }
        }
        return entities;
    }

    public static List<LegoSet> toModelList(List<LegoSetEntity> entities) {
        List<LegoSet> legoSets = new ArrayList<>();
        if (entities != null) {
            for (LegoSetEntity entity : entities) {
                legoSets.add(toModel(entity));
            }
        }
        return legoSets;
    }

    // ========== REVIEWS ✨ НОВОЕ ==========

    /**
     * Конвертировать Review (API) в ReviewEntity (БД)
     */
    public static ReviewEntity reviewToEntity(Review review) {
        ReviewEntity entity = new ReviewEntity(
                review.getSetNum(),
                review.getUserId(),
                review.getUsername(),
                review.getRating(),
                review.getComment()
        );
        entity.setReviewId(review.getReviewId());
        entity.setCreatedAt(review.getCreatedAt());
        entity.setSynced(review.isSynced());
        return entity;
    }

    /**
     * Конвертировать ReviewEntity (БД) в Review (API)
     */
    public static Review entityToReview(ReviewEntity entity) {
        Review review = new Review();
        review.setReviewId(entity.getReviewId());
        review.setSetNum(entity.getSetNum());
        review.setUserId(entity.getUserId());
        review.setUsername(entity.getUsername());
        review.setRating(entity.getRating());
        review.setComment(entity.getComment());
        review.setCreatedAt(entity.getCreatedAt());
        review.setSynced(entity.isSynced());
        return review;
    }

    /**
     * Конвертировать список Review в ReviewEntity
     */
    public static List<ReviewEntity> reviewListToEntityList(List<Review> reviews) {
        List<ReviewEntity> entities = new ArrayList<>();
        if (reviews != null) {
            for (Review review : reviews) {
                entities.add(reviewToEntity(review));
            }
        }
        return entities;
    }

    /**
     * Конвертировать список ReviewEntity в Review
     */
    public static List<Review> entityListToReviewList(List<ReviewEntity> entities) {
        List<Review> reviews = new ArrayList<>();
        if (entities != null) {
            for (ReviewEntity entity : entities) {
                reviews.add(entityToReview(entity));
            }
        }
        return reviews;
    }
}