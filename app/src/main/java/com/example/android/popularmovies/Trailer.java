package com.example.android.popularmovies;

public class Trailer {
    private String trailerID;
    private String trailerName;
    private String trailerKey;
    private String trailerType;

    public Trailer(String id, String key, String name, String type){
        trailerID = id;
        trailerKey = key;
        trailerName = name;
        trailerType = type;
    }
    public void setTrailerID(String id){
        trailerID = id;
    }
    public void setTrailerKey(String key) {
        trailerKey = key;
    }
    public void setTrailerName(String name){
        trailerName = name;
    }
    public void setTrailerType(String type) {trailerType = type;}
    public String getTrailerID(){
        return trailerID;
    }
    public String getTrailerKey(){
        return trailerKey;
    }
    public String getTrailerName(){
        return trailerName;
    }
    public String getTrailerType() { return trailerType; }
}
