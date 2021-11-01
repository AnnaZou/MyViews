package com.annazou.myviews.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtils {

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int requireWidth, int requireHeight) {
        int realWidth = options.outWidth;
        int realHeight = options.outHeight;

        int inSampleSize = 1;
        if (realWidth > requireWidth || realHeight > requireHeight) {
            int widthRatio = Math.round((float) realWidth
                    / (float) requireWidth);
            int heightRatio = Math.round((float) realHeight
                    / (float) requireHeight);
            inSampleSize = widthRatio < heightRatio ? widthRatio : heightRatio;
        }
        return inSampleSize;
    }

    public static Bitmap getCompressedBitmap(String filePath, int requireWidth,
                                             int requireHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, requireWidth,
                requireHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }
}
