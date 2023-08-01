package com.proma.promaapp;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {

    public static Bitmap rotateImageIfRequired(Bitmap bitmap, InputStream input) throws IOException {
        ExifInterface exifInterface = new ExifInterface(input);

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotateAngle = 0;

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotateAngle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotateAngle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotateAngle = 270;
                break;
        }

        if (rotateAngle != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotateAngle);

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return rotatedBitmap;
        }

        return bitmap;
    }
}
