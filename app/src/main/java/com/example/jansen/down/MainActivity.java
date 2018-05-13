package com.example.jansen.down;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.example.jansen.downloader.BaseAPKDownloader;
import com.example.jansen.downloader.FileData;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.download_btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseAPKDownloader.startDownload(MainActivity.this, fakeData1());
            }
        });

        findViewById(R.id.download_btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseAPKDownloader.startDownload(MainActivity.this, fakeData2());
            }
        });

        findViewById(R.id.download_btn_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseAPKDownloader.startDownload(MainActivity.this, fakeData3());
            }
        });
    }

    private FileData fakeData1() {
        FileData fileData = new FileData("http://p.gdown.baidu.com/67fd6894f15d45159e468a8e1c7351e449da1fefc867966d7466e356132615d1a8fa9b7a19410bb91a6a29922951cd3ec4eddb1a4e4833f7c8685a514e98cb4372dadf6e1e02f67997a79968f9dfcc5e0fddcf907766f9696233e1b3f2c76a70d878098fe3e2a4395e9812d6a841e683c22b1d613712a4ae9fe8edf24611b91288511e081bd77e1277d4bb31d2d0ee26e71d4700501bf50ca13939a8d6a02e7e6ca236667ab4b99640c5c2f0a8fc3af8aab7f1ca28926577b3d0b5cdc16641224cbd5986122805c9141cf0cf4368df61518b2c827de59fe1a8c3a9bff7758b659c1298124109e2a0e1bcb4777364b89aaeef451004ba696d2ce800195d45cd10ec1adc54c08269f19feab2b23968e4d7351b3e87e77d0994b8420f96f1a4d67c",
        "下载备份", "backup");
        return fileData;
    }

    private FileData fakeData2() {
        FileData fileData = new FileData("http://p.gdown.baidu.com/25f0974219adcc81c6a1ab974297b9896d629a2890f2ab13254d69bca7e454ea9e9430a10192bf14f5fd758a9602512160e9d61016061d6609737881c37af35e755fd39775a7f251b4c51a721ca28525dd8ff8517c71744efda34779058e5fdbc706350f6345e836f1543ca30695d9025df20d6b9cce483228f4152b5af2eed71e784050450cbead2b222fbcc480cc1477d0d09620b66c8eb3e9dd9ffd5e2d0daf790a27fc79caa16db2b91a81a1fbc64dd4e14dc8d7931074f84fafb784df850696933e9067b0f24103db80665408ba934d8d940622b13418ef12b7c77effb606a8bca801d1bafcc7e6635637e5a4332a54ca7b4ac9188a3418dcd4a845ff06295a8d1b34669aca91ad2cfacc388d3d",
                "下载枪战", "PUBG");
        return fileData;
    }

    private FileData fakeData3() {
        FileData fileData = new FileData("http://p.gdown.baidu.com/259adcc81c6a1ab974297b9896d629a2890f2ab13254d69bca7e454ea9e9430a10192bf14f5fd758a9602512160e9d61016061d6609737881c37af35e755fd39775a7f251b4c51a721ca28525dd8ff8517c71744efda34779058e5fdbc706350f6345e836f1543ca30695d9025df20d6b9cce483228f4152b5af2eed71e784050450cbead2b222fbcc480cc1477d0d09620b66c8eb3e9dd9ffd5e2d0daf790a27fc79caa16db2b91a81a1fbc64dd4e14dc8d7931074f84fafb784df850696933e9067b0f24103db80665408ba934d8d940622b13418ef12b7c77effb606a8bca801d1bafcc7e6635637e5a4332a54ca7b4ac9188a3418dcd4a845ff06295a8d1b34669aca91ad2cfacc388d3d",
                "失败的下载", "PUBG");
        return fileData;
    }
}
