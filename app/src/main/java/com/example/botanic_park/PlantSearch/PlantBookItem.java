package com.example.botanic_park.PlantSearch;


import java.io.Serializable;

/* 식물 정보를 긁어 와서 담는 모델  */
public class PlantBookItem implements Serializable, Comparable {
    private int id;

    private String img_url;
    private String name_ko;     // 이름
    private String name_sc;     // 학명
    private String name_en;     // 영명
    private String type;        // 구분
    private String blossom;     // 개화기
    private String details;     // 상세설명

    private boolean isCollected = false;    // 도감 추가 여부

    public PlantBookItem(int id, String img_url, String name_ko, String name_sc, String name_en, String type, String blossom, String details) {
        this.id = id;
        this.img_url = img_url;
        this.name_ko = name_ko;
        this.name_sc = name_sc;
        this.name_en = name_en;
        this.type = type;
        this.blossom = blossom;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean collected) {
        isCollected = collected;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getName_ko() {
        return name_ko;
    }

    public void setName_ko(String name_ko) {
        this.name_ko = name_ko;
    }

    public String getName_sc() {
        return name_sc;
    }

    public void setName_sc(String name_sc) {
        this.name_sc = name_sc;
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBlossom() {
        return blossom;
    }

    public void setBlossom(String blossom) {
        this.blossom = blossom;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public int compareTo(Object o) {
        PlantBookItem item  = (PlantBookItem) o;
        return name_ko.compareTo(item.getName_ko());
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;

        if(obj != null && obj instanceof PlantBookItem){
            isEqual = (this.name_ko == ((PlantBookItem) obj).getName_ko());
        }
        return isEqual;
    }
}
