package com.is1423.socialmedia.common;

public class Constant {
    public static final class MESSAGE_STATUS {
        public static final String SEEN = "Seen";
        public static final String DELIVERED = "Delivered";
    }

    public static final class TABLE {
        public static final String USER = "User";
        public static final String MESSAGE = "Message";
        public static final String TOKEN = "Token";
        public static final String POST = "Post";
        public static final String MESSAGE_LIST = "MessageList";
    }

    public static final class USER_STATUS {
        public static final String ONLINE = "online";
        public static final String OFFLINE = "offline";
    }

    public static final class MESSAGE_COMMON {
        public static final String DELETED = "This message was deleted...";
    }

    public static final class MESSAGE_TABLE_FIELD {
        public static final String SENDER = "sender";
        public static final String RECEIVER = "receiver";
        public static final String MESSAGE = "message";
        public static final String SEND_DATETIME = "sendDatetime";
        public static final String IS_SEEN = "isSeen";
    }

    public static final class USER_TABLE_FIELD {
        public static final String ONLINE_STATUS = "onlineStatus";
        public static final String UID = "uid";
        public static final String EMAIL = "email";
        public static final String NAME = "name";
        public static final String PHONE = "phone";
        public static final String IMAGE = "image";
        public static final String COVER = "cover";
    }

    public static final class POSTING_TABLE_FIELD {
        public static final String UID = "uid";
        public static final String USER_NAME = "user_name";
        public static final String USER_EMAIL = "user_email";
        public static final String USER_IMAGE = "user_image";
        public static final String POST_ID = "post_id";
        public static final String TITLE = "post_title";
        public static final String DESCRIPTION = "post_description";
        public static final String POST_IMAGE = "post_image";
        public static final String POST_DATETIME = "post_datetime";
    }

    public static final class MESSAGE_LIST_TABLE_FIELD {
        public static final String ID = "id";
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

    public static final class IMAGE_SOURCE_OPTIONS {
        public static final String CAMERA = "Camera";
        public static final String GALLERY = "Gallery";
    }

    public static final class REQUEST_CODE {
        public static final int CAMERA_REQUEST_CODE = 100;
        public static final int STORAGE_REQUEST_CODE = 200;
        public static final int IMAGE_PICK_GALLERY_CODE = 300;
        public static final int IMAGE_PICK_CAMERA_CODE = 400;
    }
}
