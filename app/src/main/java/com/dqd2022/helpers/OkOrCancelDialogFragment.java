package com.dqd2022.helpers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

import com.dqd2022.R;

import kit.LogKit;

/* kotlin 使用示例
OkOrCancelDialogFragment.newInstance("标题", "内容") {
    AlertUtils.toast("ok")
}.show(parentFragmentManager, null)  // 第二个参数是 tag
*/
public class OkOrCancelDialogFragment extends DialogFragment {

    public interface DialogClickListener {
        void onClickOk();
    }

    static DialogClickListener dialogClickListener;

    public static OkOrCancelDialogFragment newInstance(String title, String message, DialogClickListener listener) {
        dialogClickListener = listener;
        OkOrCancelDialogFragment fragment = new OkOrCancelDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 获取传递的参数
        Bundle args = getArguments();
        String title = args.getString("title", "");
        String message = args.getString("message", "");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomDialogStyle);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    // 处理点击 OK 按钮的操作
                    if (dialogClickListener != null) {
                        dialogClickListener.onClickOk();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    // 处理点击 Cancel 按钮的操作，90% 的场景基本是不需要做什么
                });
        return builder.create();
    }
}
    