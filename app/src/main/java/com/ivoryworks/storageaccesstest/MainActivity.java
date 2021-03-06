package com.ivoryworks.storageaccesstest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int STORE_TYPE_PRIMARY = 1000;
    private static final int STORE_TYPE_SECONDARY = 2000;
    private static final String PREF_KEY_PRIMARY_URI = "pref_key_primary_uri";
    private static final String PREF_KEY_SECONDARY_URI = "pref_key_secondary_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {
        viewStorageVolumes();
        super.onResume();
    }

    /**
     * getStorageVolumes()の取得データを表示する
     */
    private void viewStorageVolumes () {
        Context context = getApplicationContext();
        StorageManager storageManager = (StorageManager) getSystemService(this.STORAGE_SERVICE);

        TextView svText = findViewById(R.id.text_storage_volume);
        List<StorageVolume> volumes =  storageManager.getStorageVolumes();
        StringBuilder strBuilder = new StringBuilder();
        for (StorageVolume volume : volumes) {
            strBuilder.append(String.format("%s:%s\n", "Description", volume.getDescription(context)));
            strBuilder.append(String.format("%s:%s\n", "isRemovable", volume.isRemovable()));
            strBuilder.append(String.format("%s:%s\n", "State", volume.getState()));
            strBuilder.append(String.format("%s:%s\n", "isEmulated", volume.isEmulated()));
            strBuilder.append(String.format("%s:%s\n", "isPrimary", volume.isPrimary()));
            strBuilder.append("\n");
        }
        svText.setText(strBuilder.toString());
    }

    /**
     * 内部ストレージに書き込む
     */
    public void onPrimaryWrite(View view) {
        Uri uri = getUri(PREF_KEY_PRIMARY_URI);
        if (uri == null) {
            // 権限取得なし
            StorageManager storageManager = (StorageManager) getSystemService(this.STORAGE_SERVICE);
            StorageVolume volume = storageManager.getPrimaryStorageVolume();
            Intent intent = volume.createAccessIntent(Environment.DIRECTORY_MUSIC);
            startActivityForResult(intent, STORE_TYPE_PRIMARY);
        } else {
            // 権限取得済み
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
            saveFile(pickedDir);
        }
    }

    /**
     * SDカードに書き込む
     */
    public void onSecondaryWrite(View view) {
        Uri uri = getUri(PREF_KEY_SECONDARY_URI);
        if (uri == null) {
            // 権限取得なし
            StorageManager storageManager = (StorageManager) getSystemService(this.STORAGE_SERVICE);
            List<StorageVolume> volumes =  storageManager.getStorageVolumes();
            for (StorageVolume volume : volumes) {
                if (!volume.isRemovable()) {
                    continue;
                }
                Intent intent = volume.createAccessIntent(Environment.DIRECTORY_MUSIC);
                startActivityForResult(intent, STORE_TYPE_PRIMARY);
                break;
            }
        } else {
            // 権限取得済み
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
            saveFile(pickedDir);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri = data.getData();
        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);

        switch (requestCode) {
            case STORE_TYPE_PRIMARY:
            case STORE_TYPE_SECONDARY:
                saveUri(uri, requestCode);
                saveFile(pickedDir);
                break;
        }
    }

    /**
     * ファイルを保存する
     */
    private void saveFile(DocumentFile rootDir) {
        DocumentFile pkgDir = rootDir.findFile(getPackageName());
        if (pkgDir == null) {
            // パッケージ名のディレクトリが存在しなければ作成する
            pkgDir = rootDir.createDirectory(getPackageName());
        }
        DocumentFile newFile = pkgDir.createFile("text/plain", "test");
        try {
            OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
            out.write("This file is not audio.".getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uriを保存する
     */
    private void saveUri(Uri uri, int type) {
        SharedPreferences pref = getSharedPreferences(getPackageName(), 0);
        SharedPreferences.Editor prefEditor = pref.edit();
        switch (type) {
            case STORE_TYPE_PRIMARY:
                prefEditor.putString(PREF_KEY_PRIMARY_URI, uri.toString());
                break;
            case STORE_TYPE_SECONDARY:
                prefEditor.putString(PREF_KEY_SECONDARY_URI, uri.toString());
                break;
        }
        prefEditor.apply();
    }

    /**
     * Uriを返却する
     */
    private Uri getUri(String key) {
        SharedPreferences pref = getSharedPreferences(getPackageName(), 0);
        String uriStr =  pref.getString(key, "");
        return TextUtils.isEmpty(uriStr) ? null : Uri.parse(uriStr);
    }
}
