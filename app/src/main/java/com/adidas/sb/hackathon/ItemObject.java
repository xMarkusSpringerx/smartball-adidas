package com.adidas.sb.hackathon;
public class ItemObject {

    private String name;
    private String score;
    private int photoId;

    public ItemObject(String name, String score, int photoId) {
        this.name = name;
        this.score = score;
        this.photoId = photoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }
}