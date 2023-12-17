package com.dqd2022.page.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dqd2022.R;
import com.dqd2022.api.ImbizApi;
import com.dqd2022.constant.MMKVkey;
import com.dqd2022.databinding.LoginFragmentBinding;
import com.dqd2022.dto.UserApiJavaDto;
import com.dqd2022.helpers.AlertUtils;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.CacheHelpers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kit.AppKit;
import kit.LogKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    LoginFragmentBinding binding;
    Activity activity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LoginFragmentBinding.inflate(inflater, container, false);
        activity = getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.register.setOnClickListener(l -> {
            App.switchFragmentWithAnim(getParentFragmentManager(), new RegisterFragment(), R.id.login_activity_fragment);
        });
        binding.header.commonHeaderBack.setOnClickListener(l -> {
            activity.finish();
        });
        binding.username.requestFocus();
        binding.container.setOnClickListener(v -> {
            AppKit.hideKeyboard(getActivity());
        });
        binding.login.setOnClickListener(l -> {
            binding.loading.setVisibility(View.VISIBLE);
            AppKit.hideKeyboard(activity);
            UserApiJavaDto.LoginReq req = new UserApiJavaDto.LoginReq();
            req.Username = binding.username.getText().toString();
            req.Password = binding.password.getText().toString();
            ImbizApi.getInstance().login(req).enqueue(new Callback<UserApiJavaDto.LoginRegisterRes>() {
                @Override
                public void onResponse(Call<UserApiJavaDto.LoginRegisterRes> call, Response<UserApiJavaDto.LoginRegisterRes> response) {
                    binding.loading.setVisibility(View.GONE);
                    LogKit.p(response);
                    UserApiJavaDto.LoginRegisterRes res = response.body();
                    if (res == null) {
                        AlertUtils.toast(getString(R.string.login_fail));
                        return;
                    }
                    if (res.ApiCode != 0) {
                        AlertUtils.toast(res.ApiMessage);
                        return;
                    }
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        CacheHelpers cache = new CacheHelpers();
                        if (!res.Userinfo.Avatar.equals("")) {
                            res.Userinfo.Avatar = cache.downloadAvatar(activity, res.Userinfo.Avatar, true).localFileUri;
                        }
                        String jsonString = objectMapper.writeValueAsString(res.Userinfo);
                        App.mmkv.putString(MMKVkey.userinfo.name(), jsonString);
                        App.loadUserinfo();
                        App.init();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        LogKit.p("登录结果序列化失败");
                    }
                    activity.finish();
                }

                @Override
                public void onFailure(Call<UserApiJavaDto.LoginRegisterRes> call, Throwable t) {
                    AlertUtils.toast(getString(R.string.login_fail));
                    binding.loading.setVisibility(View.GONE);
                }
            });
        });
    }


}