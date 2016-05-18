/*************************************************************************************************
 * ��Ȩ���� (C)2012,  �����п��Ѽ��Źɷ����޹�˾
 * <p/>
 * �ļ���ƣ�FileUtil.java
 * ����ժҪ���ļ�������
 * ��ǰ�汾��
 * ��         �ߣ� hexiaoming
 * ������ڣ�2012-12-26
 * �޸ļ�¼��
 * �޸����ڣ�
 * ��   ��  �ţ�
 * ��   ��  �ˣ�
 * �޸����ݣ�
 ************************************************************************************************/
package com.wanshi.tool.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class FileUtil {

    public static boolean createFile(String dir, String fileName) {
        File updateDir = new File(dir);
        File updateFile = new File(updateDir + "/" + fileName);
        if (!updateDir.exists()) {
            updateDir.mkdirs();
        }
        if (!updateFile.exists()) {
            try {
                updateFile.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 保存图片文件
     *
     * @param bm
     * @param filePath
     * @param fileName
     * @throws IOException
     */
    public static void saveImageToFile(Bitmap bm, String filePath, String fileName) throws IOException {
        createFile(filePath, fileName);

        File myCaptureFile = new File(filePath + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
    }

    //读取图片
    public static Drawable readDrawableFromFile(String filePath, String fileName) {
        File mFile = new File(filePath + fileName);
        //若该文件存在
        if (mFile.exists()) {

            Drawable drawable = Drawable.createFromPath(filePath + fileName);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;

        }
        return null;
    }

    //读取图片
    public static Bitmap readImageFromFile(String filePath, String fileName) {
        File mFile = new File(filePath + fileName);
        //若该文件存在
        if (mFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath + fileName);
            return bitmap;
        }
        return null;
    }

    public static void saveTxtToFile(String content, String filePath, String fileName) throws IOException {

        createFile(filePath, fileName);

        File file = new File(filePath + fileName);
        FileOutputStream outStream = new FileOutputStream(file);
        outStream.write(content.getBytes());
        outStream.close();
    }

    //读取文本文件中的内容
    public static String readTxtFromFile(String filePath, String fileName) {

        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(filePath + fileName);

        if (file.isDirectory()) {
            LogUtil.d("readTxtFromFile", "The File doesn't not exist.");
            return null;
        }
        try {
            String temp;
            InputStream instream = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
            while (br != null && null != (temp = br.readLine())) {
                content += temp;
            }
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    //读取文本文件中的内容
    public static String readAssetsFile(Context mContext, String fileName) {

        String content = "";
        try {
            String temp;
            //读文件
            InputStream fil;
            fil = mContext.getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fil, "UTF-8"));
            while (br != null && null != (temp = br.readLine())) {
                content += temp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
    //判断文件是否存在
    public static boolean isFileExists(String filePath, String fileName) {
        try {
            File f = new File(filePath+fileName);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

//    public static Drawable readImageFromAssets(Context mContext,String fileName)  {
//        InputStream fil = null;
//        try {
//            fil = mContext.getAssets().open(fileName);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return Drawable.createFromStream(fil, null);
//    }
}