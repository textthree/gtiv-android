package com.dqd2022.page.mine;

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
import com.dqd2022.databinding.MineEditNickFragmentBinding;
import com.dqd2022.dto.CommonResDto;
import com.dqd2022.helpers.AlertUtils;
import com.dqd2022.helpers.App;

import kit.AppKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditNickFragment extends Fragment {
    MineEditNickFragmentBinding binding;
    Activity activity;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MineEditNickFragmentBinding.inflate(inflater, container, false);
        activity = getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        binding.nick.setText(args.getString("nickname"));
        binding.nick.requestFocus();
        AppKit.showKeyBoard(activity, binding.nick);
        // 返回
        binding.header.commonHeaderBack.setOnClickListener(l -> {
            getParentFragmentManager().popBackStack();
        });
        // title
        binding.header.title.setText(getString(R.string.setNickname));
        binding.header.title.setVisibility(View.VISIBLE);
        // 保存
        binding.submit.setOnClickListener(l -> {
            binding.loading.setVisibility(View.VISIBLE);
            String nick = binding.nick.getText().toString();
            App.updateUserinfoField("myNickname", nick);
            getParentFragmentManager().popBackStack();
            ImbizApi.getInstance().userinfoEdit("nickname", nick).enqueue(new Callback<CommonResDto>() {
                @Override
                public void onResponse(Call<CommonResDto> call, Response<CommonResDto> response) {
                    binding.loading.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<CommonResDto> call, Throwable t) {
                    binding.loading.setVisibility(View.GONE);
                    AlertUtils.toast("fail");
                }
            });
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}