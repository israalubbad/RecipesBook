package com.example.recipesbook.Model;

import java.security.Timestamp;

public class Comments {
    private String commentId;
    private String userId;
    private String recipeId;
    private String comment;
    private String timestamp;

    public Comments() {
    }

    public Comments(String commentId, String userId, String recipeId, String comment, String timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.recipeId = recipeId;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}
