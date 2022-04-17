package com.example.shortvideo.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.libcommon.global.AppGlobals;
import com.example.shortvideo.model.BottomBar;
import com.example.shortvideo.model.Destination;
import com.example.shortvideo.model.SofaTab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class AppConfig {
    private static HashMap<String, Destination> sDestConfig;
    private static BottomBar sBottomBar;
    private static SofaTab tab,findTagConfig;

    /**
     * @return 获取存储在assets目录中的json资源 并加载到hashmap中返回
     */
    public static HashMap<String, Destination> getsDestConfig() {
        if (sDestConfig == null) {
            String content = parseFile("destnation.json");
            sDestConfig = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>() {
            });

        }
        return sDestConfig;
    }

    public static SofaTab getFindTagConfig(){
        if (findTagConfig == null){
            String content = parseFile("find_tabs_config.json");
            findTagConfig = JSON.parseObject(content, SofaTab.class);
            Collections.sort(findTagConfig.getTabs(), new Comparator<SofaTab.Tabs>() {
                @Override
                public int compare(SofaTab.Tabs o1, SofaTab.Tabs o2) {
                    return o1.getIndex() < o2.getIndex() ? -1 : 1;
                }
            });
        }
        return findTagConfig;
    }

    public static BottomBar getsBottomBar() {
        if (sBottomBar == null) {
            String content = parseFile("main_tabs_config.json");
            sBottomBar = JSON.parseObject(content, BottomBar.class);
        }
        return sBottomBar;
    }

    public static SofaTab getSofaTabConfig() {
        if (tab == null) {
            String content = parseFile("sofa_tabs_config.json");
            tab = JSON.parseObject(content, SofaTab.class);
            Collections.sort(tab.getTabs(), new Comparator<SofaTab.Tabs>() {
                @Override
                public int compare(SofaTab.Tabs o1, SofaTab.Tabs o2) {
                    return o1.getIndex() < o2.getIndex() ? -1 : 1;
                }
            });
        }
        return tab;
    }

    /**
     * @param fileName assets目录下的json文件名
     * @return
     */
    private static String parseFile(String fileName) {
        //获取assets对象
        AssetManager asset = AppGlobals.getsApplication().getResources().getAssets();

        InputStream stream = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            stream = asset.open(fileName);
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }
}
