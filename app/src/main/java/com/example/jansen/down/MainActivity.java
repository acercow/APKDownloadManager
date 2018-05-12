package com.example.jansen.down;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.jansen.downloader.FileData;
import com.example.jansen.downloader.TestDownloader;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.download_btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestDownloader.class);
                intent.putExtra("filedata", fakeData1());
                startService(intent);
            }
        });

        findViewById(R.id.download_btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestDownloader.class);
                intent.putExtra("filedata", fakeData2());
                startService(intent);
            }
        });
    }

    private FileData fakeData1() {
        FileData fileData = new FileData("http://p.gdown.baidu.com/67fd6894f15d45159e468a8e1c7351e449da1fefc867966d7466e356132615d1a8fa9b7a19410bb91a6a29922951cd3ec4eddb1a4e4833f7c8685a514e98cb4372dadf6e1e02f67997a79968f9dfcc5e0fddcf907766f9696233e1b3f2c76a70d878098fe3e2a4395e9812d6a841e683c22b1d613712a4ae9fe8edf24611b91288511e081bd77e1277d4bb31d2d0ee26e71d4700501bf50ca13939a8d6a02e7e6ca236667ab4b99640c5c2f0a8fc3af8aab7f1ca28926577b3d0b5cdc16641224cbd5986122805c9141cf0cf4368df61518b2c827de59fe1a8c3a9bff7758b659c1298124109e2a0e1bcb4777364b89aaeef451004ba696d2ce800195d45cd10ec1adc54c08269f19feab2b23968e4d7351b3e87e77d0994b8420f96f1a4d67c", "cleaner.apk", "cleaner.apk");
        return fileData;
    }

    private FileData fakeData2() {
        FileData fileData = new FileData("http://p.gdown.baidu.com/cf160bb4a5d97364c1319565cb7c3cc105e89c7a32c90887bdb1dca3bea7f341197e52667937b6c3c6f17432986f3f98f3c043be42b6c78a018ad50f960a92a070413b574fd13a5f529f1b2d6e1f16a5863e1f352309957b874d9e3d6d31a9ea66b240484bcc8b50da8a50f286aafd0058337ee3f8a08da6b2c559373c040ebfc58f29b5df2eeb11e751cff1d1a9d6d1989d6244acb62f0b7f325b5dd57c57c72bdcf97ad035f0600e87611101eb3bf40dda3dd32d4fef7d17560828fe988c8c8f475c3a377bfbab54d4d3ecf7b32c817e7f206b515177ca7a3a326c2f4e3ee7", "mail.apk", "mail.apk");
        return fileData;
    }
}
