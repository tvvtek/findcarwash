package ru.findcarwash.ru.helpers.settings;


public final class MySettings {
    // SignInClientSection
    public final static String ERROR_NO = "0";
    public final static String ERROR_LOGIN = "1";
    public final static String ERROR_PWD = "2";
    public final static String ERROR_UNKNOWN = "3";
    public final static String SUCCESS = "success";
    public final static int MIN_LENGTH = 3;
    public final static int MAX_LENGTH = 30;

    public final static String CLIENT_LOGIN_PREFERENCE = "myLogin";
    public final static String IS_WASH = "is_wash";
    public final static String CLIENT_SIGN_LOGIN_REQUEST = "signInLogin";
    public final static String CLIENT_SIGN_PWD_REQUEST = "signInPwd";
    public final static String CLIENT_SIGN_DEVICE_UID_REQUEST = "deviceModel";
    public final static String CLIENT_EMAIL_PREFERENCE = "myEmail";
    public final static String CLIENT_DEVICE_PREFERENCE = "myDevice";

    // ClientKeySection
    public final static String CLIENT_KEY = "myKey";
    public final static int lengthKey = 256;
    public final static String regexPassword = "(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[~`!@#\\$%\\^&\\*\\(\\)\\-_\\+=\\{\\}\\[\\]\\|\\;:\"<>,./\\?]).{8,}";

    // Location settings by default cart work
    public final static double PETROPAVL_LAT = 54.868374;
    public final static double PETROPAVL_LNG = 69.141191;
    // global settings
    public final static String LOG_TAG = "mycar";
    public final static String URL = "http://192.168.1.30"; // config to url address
    public final static String CHAT_URL_PORT = "ws://192.168.1.2:8080"; // config to url address CHAT server

    public final static String CATALOG_WASH_IMAGES = "/wash_photo/";

    public final static String SIGN_IN_CLIENT_SCRIPT = "/enter.php";
    public final static String SIGN_IN_WASH_SCRIPT = "/enterWash.php";
    public final static String REGISTER_CLIENT_SCRIPT = "/register.php";
    public final static String REGISTER_WASH_SCRIPT = "/registerWash.php";
    public final static String REGISTER_WASH_UPLOAD_IMAGE_SCRIPT = "/uploadImage.php";
    public final static String CATALOG_SCRIPT = "/catalog.php";
    public final static String REVIEW_SCRIPT = "/review.php";
    public final static String REVIEW_LIST_SCRIPT = "/reviewList.php";
    public final static String CLIENT_ANDROID_KEY_NAME = "androidKey";
    public final static String ANDROID_KEY = "asvn3f2ocneve20fevmr43-21rjoep4gh2m4-saf3no1nelnwhlwrkmewmepkvmalmf03j1gpm";
    // chat Notification
    public final static String NOTIFICATION_CHAT_CHANNEL = "10";
    public final static String NOTIFICATION_CHAT_CHANNEL_NAME = "chatFindWashCar";
    public final static String NOTIFICATION_SENDER_LOGIN = "senderLogin";
    public final static String NOTIFICATION_MY_LOGIN = "myLogin";
    public final static String NOTIFICATION_WASH_NAME = "washName";
    public final static String NOTIFICATION_WASH_ID = "idWash";
    public final static String NOTIFICATION_EVENT = "event";

    public final static String NOTIFICATION_EVENT_GO_CHAT = "goChat";


    // About Wash section
    public final static String LOGIN = "login";
    public final static String WASH_ID_NAME = "id";
    public final static String WASH_SHORT_NAME_NAME = "short_name";
    public final static String WASH_DESCRIPTION_NAME = "description";
    public final static String WASH_RATING_NAME = "rating";
    public final static String WASH_MIN_PRICE_NAME = "minPrice";
    public final static String WASH_ADDRESS_NAME = "washAddress";
    public final static String WASH_PHONE_NAME = "phone";
    public final static String WASH_IMAGE_NAME = "image";
    public final static String WASH_LATITUDE = "latitude";
    public final static String WASH_LONGITUDE = "longitude";
    public final static String WASH_LOGIN = "loginWash";

    public static int tabClientWorkScreen = 0;


    public final static int NETWORK_TIMEOUT = 5;

    public static String getUrl(){
        return URL;
    }
    public static String getLogTag(){
        return LOG_TAG;
    }
    public static String getAndroidKey(){
        return ANDROID_KEY;
    }
}