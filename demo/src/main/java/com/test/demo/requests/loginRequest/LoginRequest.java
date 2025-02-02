package com.test.demo.requests.loginRequest;

public class LoginRequest {
    private String username;
    private String password;

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public String setUsername(String username){
        return this.username = username;
    }

    public String setPassword(String password){
        return this.password = password;
    }

}
