package com.dqd2022.page.userpage;

import android.app.Activity;

import com.dqd2022.constant.ChatType;
import com.dqd2022.helpers.ImHelpers;

public class UserPageAction {
    Activity activity;

    UserPageAction(Activity activity) {
        this.activity = activity;
    }

    void goChattion(int bizId, String avatar, String nickname) {
        ImHelpers.goChatting(activity, ChatType.Private, bizId, nickname);
        activity.finish();
    }


}
