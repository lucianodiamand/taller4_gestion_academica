package com.ips.gestion_academica.dto.auth;

public class LoginRequest {

    private String legajo;
    private String password;

    public LoginRequest() {}

    public String getLegajo() {
        return legajo;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
