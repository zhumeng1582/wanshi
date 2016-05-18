package com.wanshi.app.bean;

/**
 * Created by 张超 on 2016/3/5.
 */
public class Room {
    private String id;
    private String roomName;
    private String conversationId;
    private String urlRoomIcon;
    private String urlStreamAddr;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getUrlRoomIcon() {
        return urlRoomIcon;
    }

    public void setUrlRoomIcon(String urlRoomIcon) {
        this.urlRoomIcon = urlRoomIcon;
    }

    public String getUrlStreamAddr() {
        return urlStreamAddr;
    }

    public void setUrlStreamAddr(String urlStreamAddr) {
        this.urlStreamAddr = urlStreamAddr;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
