package cn.brainit.eyeforyou;

import android.graphics.Bitmap;
import cn.brainit.image.Image;

public class ImageAdapter4A {

	/**
	 * load the image from bitmap
	 * 
	 * @param bitmap
	 *            bitmap
	 * @return the cn.brainit.image.Image
	 */
	public static Image loadImage(Bitmap bitmap) {
		int[][] pixels = null;
		if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
			return new Image(pixels);
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		pixels = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixels[i][j] = bitmap.getPixel(i, j);
			}
		}
		return new Image(pixels);
	}

	/**
	 * translate the image to fit Bitmap in android
	 * 
	 * @param image
	 *            the class image in cn.brainit.image
	 * @return the bitmap in android package
	 */

	public static Bitmap toBitmap(Image image) {
		if (image.isEmpty()) {
			return null;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				bitmap.setPixel(i, j, image.getRGB(i, j));
			}
		}
		return bitmap;
	}
}
