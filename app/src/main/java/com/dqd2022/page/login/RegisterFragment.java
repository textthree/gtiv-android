package com.dqd2022.page.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.dqd2022.R;
import com.dqd2022.api.UsersApi;
import com.dqd2022.constant.MMKVkey;
import com.dqd2022.databinding.RegisterFragmentBinding;
import com.dqd2022.dto.UserApiJavaDto;
import com.dqd2022.helpers.AlertUtils;
import com.dqd2022.helpers.App;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kit.AppKit;
import kit.LogKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {
    RegisterFragmentBinding binding;
    FragmentActivity context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = RegisterFragmentBinding.inflate(inflater, container, false);
        context = getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.header.commonHeaderBack.setOnClickListener(l -> {
            getParentFragmentManager().popBackStack();
        });
        binding.username.requestFocus();
        binding.registerSubmit.setOnClickListener(l -> {
            onSubmit();
        });
        binding.container.setOnClickListener(v -> {
            AppKit.hideKeyboard(getActivity());
        });
    }

    void onSubmit() {
        binding.loading.setVisibility(View.VISIBLE);
        String username = binding.username.getText().toString();
        String password = binding.password.getText().toString();
        String passwordConfirm = binding.confirmPassword.getText().toString();
        if (username.equals("")) {
            AlertUtils.toast(getString(R.string.account_error));
            binding.username.requestFocus();
            binding.loading.setVisibility(View.GONE);
            return;
        }
        if (password.equals("")) {
            AlertUtils.toast(getString(R.string.password_empty));
            binding.password.requestFocus();
            binding.loading.setVisibility(View.GONE);
            return;
        }
        if (!password.equals(passwordConfirm)) {
            AlertUtils.toast(getString(R.string.cofirm_password_error));
            binding.confirmPassword.requestFocus();
            binding.loading.setVisibility(View.GONE);
            return;
        }
        UsersApi.getInstance().registerByUsername(username, password).enqueue(new Callback<UserApiJavaDto.LoginRegisterRes>() {
            @Override
            public void onResponse(Call<UserApiJavaDto.LoginRegisterRes> call, Response<UserApiJavaDto.LoginRegisterRes> response) {
                binding.loading.setVisibility(View.GONE);
                UserApiJavaDto.LoginRegisterRes res = response.body();
                if (res.ApiCode == 1) {
                    AlertUtils.toast(res.ApiMessage);
                    return;
                }
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    String jsonString = objectMapper.writeValueAsString(res.Userinfo);
                    App.mmkv.putString(MMKVkey.userinfo.name(), jsonString);
                    App.loadUserinfo();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    LogKit.p("注册结果序列化失败");
                }
                getActivity().finish();
            }

            @Override
            public void onFailure(Call<UserApiJavaDto.LoginRegisterRes> call, Throwable t) {
                binding.loading.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}