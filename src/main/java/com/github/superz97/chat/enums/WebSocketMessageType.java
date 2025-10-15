package com.github.superz97.chat.enums;

import lombok.Builder;

import java.time.LocalDateTime;

public enum WebSocketMessageType {

    // Chat messages
    MESSAGE,
    MESSAGE_EDIT,
    MESSAGE_DELETE,
    MESSAGE_READ,

    // Typing indicators
    TYPING_START,
    TYPING_STOP,

    // User status
    USER_ONLINE,
    USER_OFFLINE,
    USER_STATUS_CHANGE,

    // Channel/Group events
    CHANNEL_CREATED,
    CHANNEL_UPDATED,
    CHANNEL_DELETED,
    USER_JOINED_CHANNEL,
    USER_LEFT_CHANNEL,

    GROUP_CREATED,
    GROUP_UPDATED,
    GROUP_DELETED,
    USER_JOINED_GROUP,
    USER_LEFT_GROUP,

    // File events
    FILE_UPLOADED,

    // Notifications
    NOTIFICATION,

    // System
    ERROR,
    PING,
    PONG

}
