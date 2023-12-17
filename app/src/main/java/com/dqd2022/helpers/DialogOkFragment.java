package com.dqd2022.helpers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.dqd2022.R;

// DialogOkFragment.newInstance("标题", "内容").show(parentFragmentManager, null)
public class DialogOkFragment extends DialogFragment {

    public interface DialogClickListener {
        void onClickOk();
    }

    private DialogClickListener dialogClickListener;

    public void setDialogClickListener(DialogClickListener listener) {
        this.dialogClickListener = listener;
    }


    public static DialogOkFragment newInstance(String title, String message, DialogClickListener... listener) {
        DialogOkFragment fragment = new DialogOkFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        fragment.setArguments(args);
        if (listener.length > 0) fragment.dialogClickListener = listener[0];
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
                });
        return builder.create();
    }
}
    