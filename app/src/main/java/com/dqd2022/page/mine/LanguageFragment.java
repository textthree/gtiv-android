package com.dqd2022.page.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dqd2022.MainActivity;
import com.dqd2022.R;
import com.dqd2022.constant.Language;
import com.dqd2022.databinding.MineLanguageFragmentBinding;
import com.dqd2022.helpers.App;

import kit.AppKit;

public class LanguageFragment extends Fragment {
    MineLanguageFragmentBinding binding;
    Activity activity;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MineLanguageFragmentBinding.inflate(inflater, container, false);
        activity = getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        // 返回
        binding.header.commonHeaderBack.setOnClickListener(l -> {
            getParentFragmentManager().popBackStack();
        });
        // title
        binding.header.title.setText(getString(R.string.language));
        binding.header.title.setVisibility(View.VISIBLE);
        // 保存
        binding.submit.setOnClickListener(l -> {
            int checkedRadioButtonId = binding.radioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = activity.findViewById(checkedRadioButtonId);
            String lang = (String) selectedRadioButton.getTag();
            App.setI18n(getActivity(), lang);
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    void initData() {
        binding.itemEN.setTag(Language.english);
        binding.itemCN.setTag(Language.chinese);
        switch (App.language) {
            case Language.english:
                binding.itemEN.setChecked(true);
                break;
            case Language.chinese:
                binding.itemCN.setChecked(true);
                break;
        }
    }

}