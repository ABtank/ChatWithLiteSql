package ru.abramov.chat.common;


public class Library {
    /*
    /auth_request±login±password       запрс авторизации
    /auth_accept±nickname        подтверждение авторизации
    /auth_error         ошибка авторизации
    /broadcast±msg          общие сообщения
    /msg_format_error±msg   аналог Exceptions
    /user_list±user1±user2.....
    * */
    public static final String DELIMITER = "±";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENIED = "/auth_denied";
    public static final String MSG_FORMAT_ERROR = "/msg_format_error";
    public static final String TYPE_BROADCAST = "/bcast";
    public static final String TYPE_BCAST_CLIENT = "/client_msg";
    public static final String USER_LIST = "/user_list";
    public static final String AUTH_NEW_CLIENT_REQUEST = "/auth_new_client_request";
    public static final String REGISTRATION_DENIED = "/registration_denied";

    public static String getTypeBcastClient(String msg) {
        return TYPE_BCAST_CLIENT + DELIMITER + msg;
    }

    public static String getUserList(String users) {
        return USER_LIST + DELIMITER + users;
    }

    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDenied() {
        return AUTH_DENIED;
    }

    public static String registrationDenied(String nickname, String login) {
        return REGISTRATION_DENIED + DELIMITER + nickname + DELIMITER + login;
    }

    public static String getMsgFormatError(String message) {
        return MSG_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getTypeBroadcast(String src, String message) {
        return TYPE_BROADCAST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }

    public static String getAuthNewClientRequest(String login, String password, String nickName) {
        return AUTH_NEW_CLIENT_REQUEST + DELIMITER + login + DELIMITER + password + DELIMITER + nickName;
    }

}
