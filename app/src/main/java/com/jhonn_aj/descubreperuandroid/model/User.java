package com.jhonn_aj.descubreperuandroid.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by jhonn_aj on 19/03/2017.
 */

@IgnoreExtraProperties
public class User {

    private String token;
    private String name;
    private String email;
    private String password;
    private String image;
    private String phone;
    private String current;

    public User(String tolen, String name, String email, String password, String image, String phone, String current) {
        this.token = tolen;
        this.name = name;
        this.email = email;
        this.password = password;
        this.image = image;
        this.phone = phone;
        this.current = current;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
