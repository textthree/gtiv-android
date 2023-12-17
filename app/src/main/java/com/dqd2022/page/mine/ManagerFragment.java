package com.dqd2022.page.mine;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dqd2022.api.ImbizApi;
import com.dqd2022.constant.CachePath;
import com.dqd2022.databinding.MineManagerFragmentBinding;
import com.dqd2022.dto.CommonResDto;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.qiniu.QiniuUtils;
import com.dqd2022.page.im.chatting.GlideEngine;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.io.File;
import java.util.ArrayList;

import kit.CryptoKit;
import kit.LogKit;
import kit.luban.Luban;
import kit.luban.OnCompressListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerFragment extends Fragment {
    MineManagerFragmentBinding binding;
    Activity activity;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MineManagerFragmentBinding.inflate(inflater, container, false);
        activity = getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        // 返回
        binding.header.commonHeaderBack.setOnClickListener(l -> {
            activity.finish();
        });
        // avatar
        binding.avatarBox.setOnClickListener(l -> avatarSelector());
        // nickname
        binding.nick.setOnClickListener(l -> {
            EditNickFragment fragment = new EditNickFragment();
            Bundle args = new Bundle();
            args.putString("nickname", App.myNickname);
            fragment.setArguments(args);
            App.switchFragmentWithAnim(getParentFragmentManager(), fragment);
        });
        // language
        binding.language.setOnClickListener(l -> {
            App.switchFragmentWithAnim(getParentFragmentManager(), new LanguageFragment());
        });
        // exit
        binding.logout.setOnClickListener(l -> {
            App.logout(activity);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    void initData() {
        binding.avatarImage.setImageURI(App.myAvatar);
        binding.nickname.setText(App.myNickname);
    }

    // 从相册选择照片
    private void avatarSelector() {
        PictureSelector.create(activity).
                openGallery(SelectMimeType.ofImage()).
                setSelectionMode(SelectModeConfig.SINGLE).
                setImageEngine(GlideEngine.createGlideEngine()).
                setLanguage(App.selectorLanguage).
                forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        LocalMedia item = result.get(0);
                        //LogKit.p("图片选择结果：", result.get(0).getRealPath(), result.get(0).getMimeType());
                        String targetDir = CachePath.chatPhotoDir(activity);
                        Luban.with(activity).load(new File(item.getRealPath())).ignoreBy(100).setTargetDir(targetDir)
                                .setCompressListener(new OnCompressListener() {
                                    @Override
                                    public void onStart() {
                                    }

                                    @Override
                                    public void onSuccess(File file) {
                                        String path = "file://" + file.getAbsolutePath();
                                        // LogKit.p("图片压缩完成，压缩后文件", path);
                                        // 本地更新
                                        App.updateUserinfoField("myAvatar", path);
                                        binding.avatarImage.setImageURI(path);
                                        // 异步更新服务端
                                        String suffix = "." + item.getMimeType().split("/")[1];
                                        String objectkey = CryptoKit.md5Encrypt("useravatar" + App.myUserId + suffix);
                                        Thread T = new Thread(() -> {
                                            String obsUrl = QiniuUtils.appUpload(path, item.getMimeType(), objectkey);
                                            LogKit.p("头像上传完成 obsUrl：", obsUrl, " local:", path);
                                            ImbizApi.getInstance().userinfoEdit("avatar", obsUrl).enqueue(new Callback<CommonResDto>() {
                                                @Override
                                                public void onResponse(Call<CommonResDto> call, Response<CommonResDto> response) {
                                                    LogKit.p(response, response.body());
                                                }

                                                @Override
                                                public void onFailure(Call<CommonResDto> call, Throwable t) {
                                                }
                                            });
                                        });
                                        T.start();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        LogKit.p("[图片压缩失败]", e);
                                    }
                                }).launch();
                    }

                    @Override
                    public void onCancel() {
                    }
                });

    }


}