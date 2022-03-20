package com.is1423.socialmedia.notifications;

public class Token {
    /*An FCM Token ~ registrationToken
    * An ID issued by GCM connection servers to the client app that allows it to receive message*/
    String token;

    public Token() {
    }

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
