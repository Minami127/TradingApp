package com.example.carrotapp.model;

public class ChatUser {
    private String profileImg; // 프로필 이미지 URL
    private String nickname;    // 사용자 닉네임
    private String location;    // 사용자 위치
    private String time;        // Firebase에서 가져올 시간
    private String lastChat;    // Firebase에서 가져올 마지막 채팅 내용

    // 생성자
    public ChatUser(String profileImg, String nickname, String location) {
        this.profileImg = profileImg;
        this.nickname = nickname;
        this.location = location;
    }

    // Getter 및 Setter 메소드
    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLastChat() {
        return lastChat;
    }

    public void setLastChat(String lastChat) {
        this.lastChat = lastChat;
    }
}