package com.dqd2022.page.im.setting;

import android.app.Activity
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dqd2022.databinding.ImNoticeListFragmentBinding
import kit.PermissKit

class ImSettingFragment(val activity: Activity) : Fragment() {
    lateinit var binding: ImNoticeListFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ImNoticeListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 返回
        binding.header.commonHeaderBack.setOnClickListener { l -> activity.finish() }
        // 悬浮窗权限
        binding.lockScreenCallSwitch.setOnClickListener {
        binding.lockScreenCallSwitch.isChecked =  binding.lockScreenCallSwitch.isChecked
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.packageName)
            )
            activity.startActivity(intent)
        }
        // 电池权限
        if (checkBatteryOptimizationPermission()) {
            binding.batterySwitch.isChecked = true
        } else {
            binding.batterySwitch.setOnClickListener { l ->
                binding.lockScreenCallSwitch.isChecked = false
                requestBatteryOptimizations()
            }
        }
    }

    // 检查电池权限
    private fun checkBatteryOptimizationPermission(): Boolean {
        val powerManager = activity.getSystemService(POWER_SERVICE) as PowerManager
        val packageName = activity.packageName
        val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)
        return isIgnoringBatteryOptimizations
    }

    // 请求忽略电源优化权限，让电源优化管理系统忽略本应用
    // android 本身是支持这个效果的，但是各手机厂商可能会把此权限阉割，设置了也没效果
    private fun requestBatteryOptimizations() {
        val packageName = activity.packageName
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (PermissKit.checkFloatPermission(activity)) {
            binding.lockScreenCallSwitch.isChecked = true
        }
    }

}