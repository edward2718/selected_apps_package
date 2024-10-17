package com.example.savepkg;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;
public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    private List<ApplicationInfo> appList;
    private Set<String> selectedApps;
    private PackageManager packageManager;

    public AppAdapter(List<ApplicationInfo> appList, Set<String> selectedApps, PackageManager pm) {
        this.appList = appList;
        this.selectedApps = selectedApps;
        this.packageManager = pm;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_item, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        ApplicationInfo appInfo = appList.get(position);

        // 设置应用名称、包名和Logo
        holder.appName.setText(packageManager.getApplicationLabel(appInfo));
        holder.packageName.setText(appInfo.packageName);
        holder.appLogo.setImageDrawable(packageManager.getApplicationIcon(appInfo));

        // 确保绑定正确的状态
        holder.appSwitch.setOnCheckedChangeListener(null); // 防止复用时触发事件

        // 初始化开关状态
        holder.appSwitch.setChecked(selectedApps.contains(appInfo.packageName));

        // 切换开关监听器
        holder.appSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedApps.add(appInfo.packageName);
            } else {
                selectedApps.remove(appInfo.packageName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    // ViewHolder 类
    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appLogo;
        TextView appName, packageName;
        Switch appSwitch;

        AppViewHolder(View itemView) {
            super(itemView);
            appLogo = itemView.findViewById(R.id.app_logo);
            appName = itemView.findViewById(R.id.app_name);
            packageName = itemView.findViewById(R.id.package_name);
            appSwitch = itemView.findViewById(R.id.app_switch);
        }
    }
}
