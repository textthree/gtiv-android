package com.dqd2022.dto;

public class UserApiJavaDto {
    public class VideoMasterResponse {
        public int ApiCode;
        public String ApiMessage;
        public String Endpoint;
        public String Nickname;
        public String Avatar;
        public String Username;
        public int SupportNum;
        public int FollowNum;
        public int FansNum;
        public int CreateVideoNum;
        public int SupportVideoNum;
        public String Intro;
        public boolean IsFollow;
        public boolean IsFriend;
        
    }

    public static class Userinfo {
        public int UserId;
        public int UserRole;
        public String Nickname;
        public String Username;
        public String Avatar;
        public int Gender;
        public long LastLoginTime;
        public int Version;
        public int ContactsVersion;
        public String Token;
        public int FansNum;
        public int SupportNum;
        public int CreateVideoNum;
        public int CollectVideoNum;
        public int FollowNum;
    }

    public class LoginRegisterRes extends CommonResDto {
        public Userinfo Userinfo;
    }

    public static class LoginReq {
        public String Username;
        public String Password;
    }

    public static class UserinfoEditReq {

        public String Field;
        public String Value;

        public UserinfoEditReq(String field, String value) {
            Field = field;
            Value = value;
        }

    }


}
