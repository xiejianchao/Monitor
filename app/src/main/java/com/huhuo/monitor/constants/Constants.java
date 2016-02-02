package com.huhuo.monitor.constants;

/**
 * Created by xiejianchao on 15/10/27.
 */
public class Constants {

    public static final String BASE_URL = "";
    public static final String HTTP_PREFIX = "http";

    public static final String PREFERENCES_NAME = "monitor";

    public static final String LIST = "list";
    public static final String ADD = "add";
    public static final String DEL = "del";
    public static final String STATUS = "status";
    public static final String UPDATE = "update";
    public static final String CLEAR = "clear";

    public static final int OPEN = 1;
    public static final int RECEIVE_MESSAGE = 2;
    public static final int CLOSE = 3;
    public static final int ERROR = 4;
    public static final int RESTART = 5;

    public static final int ONLINE = 0;
    public static final int FAULT = 1;
    public static final int ALARM = 2;
    public static final int OFFLINE = 3;

    public static final String DEFAULT_USER_ID = "13466608793";
    public static final String IM_APP_ID = "20150314000000110000000000000010";
    public static final String IM_APP_TOKEN = "17E24E5AFDB6D0C1EF32F3533494502B";

    public static class Key {
        public static final String VALUE = "value";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String TIME = "time";
        public static final String DESC = "desc";
        public static final String CONTENT = "content";
        public static final String MOBILE = "mobile";

    }

    public static final String getLoginUrl(String name) {
        return LOGIN_URL + name;
    }
//    private static final String LOGIN_URL = "http://xiaofang.northchinatech.com:18087/shop/index.php/Pc/User/login?login_name=15810440412";
    private static final String LOGIN_URL = "http://xiaofang.northchinatech.com:18087/shop/index.php/Pc/User/login?login_name=";

}
