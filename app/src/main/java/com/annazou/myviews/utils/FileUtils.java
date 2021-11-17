package com.annazou.myviews.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static boolean writeFile(String filePath, String content){
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(filePath));
            outputStream.write(content.getBytes());
            outputStream.close();
            return true;
        } catch (java.io.FileNotFoundException e) {
            Log.d(TAG, "The File doesn't exist.");
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        return false;
    }


    public static String readFile(String path){
        StringBuilder sb = new StringBuilder("");
        try {
            FileInputStream inputStream = new FileInputStream(path);
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            while(len > 0){
                sb.append(new String(buffer,0,len));
                len = inputStream.read(buffer);
            }
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }

        String result = sb.toString();
        return result;
    }

    public static void deleteFile(String path){
        File file = new File(path);
        if (file.exists()) file.delete();
    }

    public static void renameFile(String dir,String oldName, String newName){
        File file = new File(dir + "/" + oldName);
        if (file.exists()){
            file.renameTo(new File(dir + "/" + newName));
        }
    }

    public static String getFileThumbTitle(String filePath){
        StringBuilder sb = new StringBuilder("");
        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[64];
            int len = inputStream.read(buffer);
            sb.append(new String(buffer,0,len));
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }
        String tmp = sb.toString();
        String[] title = tmp.split("\n");
        return title[0];
    }

    public static String getFileDate(File file){
        String date = "--/--/--";
        if(file.exists()){
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd");
            Date lastModify = new Date(file.lastModified());
            date = sdf.format(lastModify);
        }
        return date;
    }


    public static final int MAX_KEY_WORD_LENGTH = 20;

    private boolean checkTextContains(String path, String key){
        if(key.isEmpty()) return false;
        if(key.length()  > MAX_KEY_WORD_LENGTH) key = key.substring(0,MAX_KEY_WORD_LENGTH);
        boolean matched = false;
        int length = key.length() * 2;
        try {
            FileInputStream inputStream = new FileInputStream(path);
            byte[] buffer = new byte[100];
            String content = "";
            int len = inputStream.read(buffer);
            while(len > 0){
                content += new String(buffer,0,len);
                if(content.contains(key)){
                    matched = true;
                    break;
                }
                content = content.substring(len - length > 0 ? len - length : 0 ,len);
                len = inputStream.read(buffer);
            }
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }
        return matched;
    }

    public static boolean copyFile(String fromFile, String toFile){
        File file = new File(fromFile);
        if(!file.exists() || !file.canRead()) return false;
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0)
            {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
