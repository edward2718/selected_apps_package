package com.example.savepkg;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class MainActivity extends AppCompatActivity {

    private static final String DEFAULT_FILE_PATH = "/sdcard/SavePkg/selected_apps_package.txt";
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_FILE_PATH = "file_path";

    private Set<String> selectedApps = new HashSet<>();
    private String filePath;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestStoragePermission(); // 动态申请存储权限
        loadFilePathFromPreferences(); // 加载保存路径
        loadSelectedAppsFromFile(); // 加载已保存的应用

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ApplicationInfo> appList = getInstalledApps();
        AppAdapter adapter = new AppAdapter(appList, selectedApps, getPackageManager());
        recyclerView.setAdapter(adapter);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveSelectedAppsToFile());

        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> showPathSettingsDialog());
    }

    private List<ApplicationInfo> getInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> allApps = pm.getInstalledApplications(0);
        List<ApplicationInfo> userApps = new ArrayList<>();

        for (ApplicationInfo appInfo : allApps) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                userApps.add(appInfo);
            }
        }
        return userApps;
    }

    private void saveSelectedAppsToFile() {
        if (!isExternalStorageWritable()) {
            Toast.makeText(this, "没有文件读取权限", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(filePath);

        try (FileWriter writer = new FileWriter(file)) {
            for (String packageName : selectedApps) {
                writer.write(packageName + "\n");
            }
            Toast.makeText(this, "已保存到 " + filePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSelectedAppsFromFile() {
        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String packageName;
                while ((packageName = reader.readLine()) != null) {
                    selectedApps.add(packageName);
                }
                Toast.makeText(this, "已加载已保存的应用", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFilePathFromPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        filePath = prefs.getString(KEY_FILE_PATH, DEFAULT_FILE_PATH);
    }

    private void saveFilePathToPreferences(String newPath) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_FILE_PATH, newPath);
        editor.apply();
    }

    private void showPathSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置保存路径");

        final EditText input = new EditText(this);
        input.setText(filePath);
        builder.setView(input);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newPath = input.getText().toString().trim();
            if (!newPath.isEmpty()) {
                filePath = newPath;
                saveFilePathToPreferences(newPath);
                Toast.makeText(this, "路径已更新为: " + newPath, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "路径不能为空", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "存储权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
