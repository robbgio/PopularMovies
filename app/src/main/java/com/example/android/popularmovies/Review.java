package com.example.android.popularmovies;

public class Review {

    private String reviewID;
    private String reviewAuthor;
    private String reviewContent;
    private String reviewURL;
    private String reviewContentTruncated;

    public Review (String ID, String author, String content, String truncated, String URL){
        reviewID = ID;
        reviewAuthor = author;
        reviewContent = content;
        reviewContentTruncated = truncated;
        reviewURL = URL;
    }

    public void setReviewID(String ID){
        reviewID = ID;
    }
    public void setReviewAuthor(String author){
        reviewAuthor = author;
    }
    public void setReviewContent(String content){
        reviewContent = content;
    }
    public void setReviewContentTruncated(String truncated) {reviewContentTruncated = truncated; }
    public void setReviewURL(String URL){
        reviewURL = URL;
    }

    public String getReviewID (){
        return reviewID;
    }
    public String getReviewAuthor(){
        return reviewAuthor;
    }
    public String getReviewContent(){
        return reviewContent;
    }
    public String getReviewURL(){
        return reviewURL;
    }
    public String getReviewContentTruncated() {return reviewContentTruncated; }
}
