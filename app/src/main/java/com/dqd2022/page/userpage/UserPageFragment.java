package com.dqd2022.page.userpage;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.dqd2022.R;
import com.dqd2022.api.UsersApi;
import com.dqd2022.databinding.UserHomeFragmentBinding;
import com.dqd2022.dto.CommonResDto;
import com.dqd2022.helpers.App;
import com.dqd2022.page.im.contacts.AddFriendFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import kit.LogKit;
import kit.NumberKit;
import kit.StatusBar.StatusBarKit;

public class UserPageFragment extends Fragment {
    private UserHomeFragmentBinding binding;
    private UserPageVM vm;
    private UserPageModle model;
    private FragmentActivity activity;
    private int userId;
    List<PageViewFragment> pageViewFragments = new ArrayList<>();
    String avatar, nickname;
    UserPageAction action;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(UserPageVM.class);
        model = new UserPageModle();
        binding = UserHomeFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        activity = getActivity();
        action = new UserPageAction(activity);
        StatusBarKit.translucentStatus(activity, false);
        StatusBarKit.setFontBlack(activity);
        activity.getWindow().setNavigationBarColor(App.bottomNaviBgBlack);
        initView();
        initData();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
//        StatusBarKit.translucentStatus(activity, false);
//        StatusBarKit.setFontBlack(activity);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            StatusBarKit.translucentStatus(activity, false);
            StatusBarKit.setFontWhite(activity);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void initView() {
        // 返回
        binding.back.setOnClickListener(l -> {
            getActivity().finish();
        });

        // 关注 / 取关
        binding.followBtn.setOnClickListener(l -> {
            if (!vm.isFollow) {
                vm.increaseFans();
                binding.followBtn.setText(R.string.unfollow);
            } else {
                vm.reduceFans();
                binding.followBtn.setText(R.string.follow);
            }
            vm.isFollow = !vm.isFollow;
            UsersApi.getInstance().follow(userId).enqueue(CommonResDto.commonCallback);
        });

        // 添加标签
        List<String> tabs = new ArrayList<>();
        tabs.add(activity.getString(R.string.creation));
        tabs.add(activity.getString(R.string.supports));
        //tabs.add(activity.getString(R.string.collect)); // 收藏的内容不公开

        // 设置适配器，绑定 Fragment 到 viewPager
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public Fragment createFragment(int position) {
                PageViewFragment fragment = new PageViewFragment(position);
                Bundle b = new Bundle();
                b.putInt("userId", userId);
                b.putString("avatar", avatar);
                fragment.setArguments(b);
                pageViewFragments.add(fragment);
                return fragment;
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }

        });

        // 将 viewPager 绑定到标签
        new TabLayoutMediator(binding.tablayout, binding.viewPager, (tab, position) -> tab.setText(tabs.get(position))).attach();

        binding.userHomeAppbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            /**
             * 根据百分比改变颜色透明度
             */
            public int changeAlpha(int color, float fraction) {
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                int alpha = (int) (Color.alpha(color) * fraction);
                return Color.argb(alpha, red, green, blue);
            }

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset < -500) {
                    StatusBarKit.setFontBlack(activity);
                } else {
                    StatusBarKit.setFontWhite(activity);
                }
                binding.pinHeader.setBackgroundColor(
                        changeAlpha(getResources().getColor(R.color.white),
                                Math.abs(verticalOffset * 1.0f) / appBarLayout.getTotalScrollRange()));
            }
        });

    }

    void initData() {
        avatar = getArguments().getString("avatar");
        nickname = getArguments().getString("nick");
        userId = getArguments().getInt("userId");
        // nickname
        vm.setNickname(nickname);
        vm.getNickname().observe(getViewLifecycleOwner(), binding.nickname::setText);
        // avatar
        vm.setAvatar(avatar);
        vm.getAvatar().observe(getViewLifecycleOwner(), binding.avatar::setImageURI);
        // 背景 banner（使用头像做背景）
        int url = R.drawable.user_banner;
        RequestOptions options = RequestOptions.bitmapTransform(new BlurTransformation(20, 1));
        RequestManager manager = Glide.with(this);
        RequestBuilder builder;
        builder = manager.load(url);
        builder.apply(options).into(binding.topBackground);
        // 请求后端获取更多用户信息
        model.getVideoMasterInfo((response -> {
            binding.supportNum.setText(NumberKit.formatWithUnit(App.language, response.SupportNum));
            binding.followNum.setText(NumberKit.formatWithUnit(App.language, response.FollowNum));
            binding.personalIntro.setText(response.Intro.replace("\\n", "\n"));
            // fans 数需要双向绑定，访客在这里点关注与取关时需要更新 ui
            vm.setFansNum(response.FansNum);
            vm.getFansNum().observe(getViewLifecycleOwner(), binding.fansNum::setText);
            // nickname
            binding.nickname.setText(response.Nickname);
            // username
            binding.userAccount.setText(getString(R.string.userid) + " " + response.Username);
            // 关注
            if (response.IsFollow) {
                binding.followBtn.setText(R.string.unfollow);
                vm.isFollow = true;
            } else {
                binding.followBtn.setText(R.string.follow);
            }
            // 私聊
            if (response.IsFriend) {
                binding.friendBtn.setText(R.string.send_private_message);
                vm.isFriend = true;
            } else {
                binding.friendBtn.setText(R.string.add_friend);
            }
            // 更新 tableLayout 标签文字
            binding.tablayout.getTabAt(0).setText(activity.getString(R.string.creation) + " (" + response.CreateVideoNum + ")");
            // 加好友 / 发消息
            binding.friendBtn.setOnClickListener(l -> {
                UserPageActivity ac = (UserPageActivity) activity;
                if (response.IsFriend) {
                    ac.needSwitchToMessageList = true;
                    action.goChattion(userId, avatar, nickname);
                } else {
                    ac.gotoAddFriend(String.valueOf(userId), nickname, avatar);
                }
            });
        }), userId);
    }


}