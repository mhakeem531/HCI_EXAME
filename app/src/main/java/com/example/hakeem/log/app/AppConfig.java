package com.example.hakeem.log.app;

/**
 * Created by hakeem on 3/2/18.
 */

public class AppConfig {


    private  static final String serverAddress = "192.168.1.5";
    // Server user login url
    public static String URL_LOGIN = "http://" + serverAddress + "/login_try/login.php";

    // Server user register url
    public static String URL_REGISTER = "http://" + serverAddress + "/login_try/register.php";

    // Server user upload profile photo url
    public static String URL_UPLOAD_PROFILE_PHOTO = "http://" + serverAddress + "/login_try/include/upload_profile_image.php";


    public static String URL_INVOKE_PROFILE_PHOTO = "http://" + serverAddress + "/login_try/include/invoke_profile_photo.php";

}
