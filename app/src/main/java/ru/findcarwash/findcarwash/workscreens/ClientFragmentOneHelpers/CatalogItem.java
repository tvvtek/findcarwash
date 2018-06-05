package ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers;

public class CatalogItem {

    public String id;
    public String shortName;
    public String description;
    public String rating;
    public String minPrice;
    public String washAddress;
    public String phone;
    public String image;
    public String latitude;
    public String longitude;
    public String loginWash;


    public CatalogItem(String id, String shortName, String description, String rating,
                       String minPrice, String washAddress, String phone, String image,
                       String latitude, String longitude, String loginWash) {
        this.id = id;
        this.shortName = shortName;
        this.description = description;
        this.rating = rating;
        this.minPrice = minPrice;
        this.phone = phone;
        this.image = image;
        this.washAddress = washAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.loginWash = loginWash;
    }
}