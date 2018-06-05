package ru.findcarwash.ru.helpers.json;

public class JsonCatalogReceive {
    public String id, short_name, description, rating, minPrice, washAddress, phone, latitude, longitude, image, loginWash;

    public JsonCatalogReceive(
            String id, String short_name, String phone, String description,
                              String rating, String minPrice, String washAddress,
                              String image, String latitude, String longitude, String loginWash){
        // for catalog
        this.id = id;
        this.short_name = short_name;
        this.phone = phone;
        this.description = description;
        this.rating = rating;
        this.minPrice = minPrice;
        this.washAddress = washAddress;
        this.image = image;
        // for all info
        this.latitude = latitude;
        this.longitude = longitude;
        this.loginWash = loginWash;
    }
}