package com.securephone.shared.protocol;

public enum MessageType {
    // Authentication
    LOGIN_REQUEST,
    LOGIN_RESPONSE,
    REGISTER_REQUEST,
    REGISTER_RESPONSE,
    LOGOUT,
    AUTH_ERROR,

    // 2FA
    REQUEST_2FA,
    VERIFY_2FA,

    // User management
    USER_STATUS_UPDATE,
    USER_LIST,
    USER_PROFILE,

    // Messaging
    TEXT_MESSAGE,
    IMAGE_MESSAGE,
    FILE_MESSAGE,
    AUDIO_MESSAGE,
    VIDEO_MESSAGE,
    MESSAGE_DELIVERED,
    MESSAGE_READ,

    // Contacts
    CONTACT_REQUEST,
    CONTACT_ACCEPT,
    CONTACT_REJECT,
    CONTACT_REMOVE,
    CONTACT_LIST,

    // Calls
    CALL_INITIATE,
    CALL_ACCEPT,
    CALL_REJECT,
    CALL_END,
    CALL_STATUS,

    // Rooms
    ROOM_CREATE,
    ROOM_JOIN,
    ROOM_LEAVE,
    ROOM_LIST,
    ROOM_MESSAGE,

    // Presence
    PRESENCE_UPDATE,
    TYPING_INDICATOR,

    // System
    PING,
    PONG,
    ERROR,
    NOTIFICATION,

    // Push
    PUSH_REGISTER,
    PUSH_NOTIFICATION
}
