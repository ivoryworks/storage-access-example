package com.ivoryworks.storageaccesstest;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

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
}
