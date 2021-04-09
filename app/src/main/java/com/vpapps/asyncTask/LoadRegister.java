package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.vpapps.interfaces.SocialLoginListener;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.RequestBody;

public class LoadRegister extends AsyncTask<String, String, String> {

    private RequestBody requestBody;
    private SocialLoginListener socialLoginListener;
    private String success = "0", message = "", user_id = "", user_name = "", email = "", auth_id = "";

    public LoadRegister(SocialLoginListener socialLoginListener, RequestBody requestBody) {
        this.socialLoginListener = socialLoginListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        socialLoginListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = JsonUtils.okhttpPost(Constant.SERVER_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);

                success = c.getString(Constant.TAG_SUCCESS);
                message = c.getString(Constant.TAG_MSG);
                if(c.has("user_id")) {
                    user_id = c.getString("user_id");
                    user_name = c.getString("name");

                    auth_id = c.getString("auth_id");
                    email = c.getString("email");
                }
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        socialLoginListener.onEnd(s, success, message, user_id, user_name, email, auth_id);
        super.onPostExecute(s);
    }
}