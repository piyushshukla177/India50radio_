package com.vpapps.interfaces;

public interface SocialLoginListener {
    void onStart();
    void onEnd(String success, String registerSuccess, String message, String user_id, String user_name, String email, String auth_id);
}