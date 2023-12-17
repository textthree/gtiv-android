package com.dqd2022.dto;

import java.util.ArrayList;

public class VideoApiDto {
    public class UserVideoItem {
        public int VideoId;
        public String Title;
        public String Cover;
        public int SupportNum;
    }

    public class UserVideosResponse {
        public int ApiCode;
        public String ApiMessage;
        public String Endpoint;
        public ArrayList<UserVideoItem> List;
    }

    public class VideoInfoResDto {
        public int MasterUid;
        public String MasterAvatar;
        public boolean IsFollow;
        public int SupportNum;
        public int CollectNum;
        public int ShareNum;
        public String VideoCover;
        public int VideoWidth;
        public int VideoHeight;
    }


}
