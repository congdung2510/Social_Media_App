package com.is1423.socialmedia.common;

public class Constant {
    public static final class MESSAGE_STATUS{
        public static final String SEEN = "Seen";
        public static final String DELIVERED = "Delivered";
    }

    public static final class TABLE{
        public static final String USER = "User";
        public static final String MESSAGE = "Message";
        public static final String TOKEN = "Token";
    }

    public static final class USER_STATUS{
        public static final String ONLINE = "online";
        public static final String OFFLINE = "offline";
    }

    public static final class MESSAGE_COMMON{
        public static final String DELETED = "This message was deleted...";
    }

    public static final class MESSAGE_TABLE_FIELD{
        public static final String SENDER = "sender";
        public static final String RECEIVER = "receiver";
        public static final String MESSAGE = "message";
        public static final String SEND_DATETIME = "sendDatetime";
        public static final String IS_SEEN = "isSeen";
    }

    public static final class USER_TABLE_FIELD{
        public static final String ONLINE_STATUS = "onlineStatus";
        public static final String UID = "uid";
        public static final String EMAIL = "email";
        public static final String NAME = "name";
        public static final String PHONE = "phone";
        public static final String IMAGE = "image";
        public static final String COVER = "cover";
    }

    public static final class COMMON_KEY {
        public static final String SHARED_PREFERENCES_SP_USER_NAME = "SP_USER";
        public static final String SHARED_PREFERENCES_CURRENT_USERID_KEY = "Current_USERID";
        public static final String PARTNER_UID_KEY = "partnerUid";
    }

    public static final class REMOTE_MESSAGE {
        public static final String SENT = "sent";
        public static final String USER = "user";
        public static final String ICON = "icon";
        public static final String TITLE = "title";
        public static final String BODY = "body";
    }

    public static final class FCM {
        public static final String URL = "https://fcm.googleapis.com/";
    }
}
