package ru.findcarwash.ru.helpers.json;

public class JsonReviewReceive {
    public String error = "0";
    public String review = "";
    public String rating = "";
    public JsonReviewReceive(String error, String review, String rating){
        this.error = error;
        this.review = review;
        this.rating = rating;
    }
}