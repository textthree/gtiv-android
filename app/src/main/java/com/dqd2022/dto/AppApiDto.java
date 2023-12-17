package com.dqd2022.dto;

public class AppApiDto {
    public static class AzureUpKey extends CommonResDto {
        public String AK;
        public String AN;
        public String CN;
        public String URL;
 
    }

    public static class AzureUpKeyReq {
        public int Type;

        public AzureUpKeyReq(int type) {
            Type = type;
        }
    }

}
