package com.example.recipesbook.Model;

public class RecipeModel {
    private String recipeId;
    private String userId;
    private String title;
    private String ingredients;
    private String steps;
    private String category;
    private int  cookingTime;
    private int  calories;
    private int evaluation;
    private String description;
    private String videUrl;
    private String imageUrl;

    public RecipeModel() {
    }


    public RecipeModel(String recipeId, String userId, String title, String ingredients, String steps, String category, int cookingTime, int calories, int evaluation, String description, String videUrl, String imageUrl) {
        this.recipeId = recipeId;
        this.userId = userId;
        this.title = title;
        this.ingredients = ingredients;
        this.steps = steps;
        this.category = category;
        this.cookingTime = cookingTime;
        this.calories = calories;
        this.evaluation = evaluation;
        this.description = description;
        this.videUrl = videUrl;
        this.imageUrl = imageUrl;
    }

    public int getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(int evaluation) {
        this.evaluation = evaluation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVideUrl() {
        return videUrl;
    }

    public void setVideUrl(String videUrl) {
        this.videUrl = videUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }
}
