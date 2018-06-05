package ru.findcarwash.ru.helpers.json;

public class JsonReviewItemsReceive {
    public String login;
    public String review;
    public String rating;

    public JsonReviewItemsReceive(String login, String review, String rating){
        this.login = login;
        this.review = review;
        this.rating = rating;
    }
}
