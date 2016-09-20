package cn.brainit.eyeforyou;

import java.io.ByteArrayOutputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import cn.brainit.eyeforyou.util.DensityUtil;
import cn.brainit.image.BinaryImage;
import cn.brainit.image.GrayImage;
import cn.brainit.image.Image;

@SuppressLint("WrongCall")
public class MainActivity extends Activity implements SurfaceHolder.Callback {

	// 定义对象
	private SurfaceView mSurfaceview = null; // SurfaceView对象：(视图组件)视频显示
	private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder对象：(抽象接口)SurfaceView支持类
	private Camera mCamera = null; // Camera对象，相机预览
	private Button holdButton;
	private ImageView cutPreview;

	private static final String TAG = "MainActivity";
	private static final int UPDATA_PREVIEW_IMAGE = 1;

	private boolean bIfPreview;
	@SuppressWarnings("unused")
	private int mPreviewWidth, mPreviewHeight;

	private int previewWidth, previewHeight, pictureWidth, pictureHeight;
	PreviewCallback mJpegPreviewCallback = new Camera.PreviewCallback() {
		@Override
		public void onPreviewFrame(final byte[] data, Camera camera) {
			camera.autoFocus(new AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					if (success) {
						camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
						if (null != imageTask) {
							switch (imageTask.getStatus()) {
							case RUNNING:
								return;
							case PENDING:
								imageTask.cancel(false);
								break;
							default:
								break;
							}
						}
						imageTask = new ImageTask(data);
						imageTask.execute((Void) null);
					}
				}

			});

			// Log.i(TAG, "going into onPreviewFrame");
			// Bitmap mBitmap = null;
			// if (null != data) {
			// if (data.length != 0) {
			// mBitmap = BitmapFactory.decodeByteArray(data, 0,
			// data.length);// data是字节数据，将其解析成位图
			// if (mBitmap != null) {
			// Bitmap newBit = Bitmap.createBitmap(mBitmap, 100, 100,
			// 100, 100);
			// cutPreview.setImageBitmap(newBit);
			// }
			//
			// } else {
			// System.out.println("空的bitmap");
			// }
			//
			// // mCamera.stopPreview();
			// }
			// bufferedImage --> bitmap
			// // 设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation",
			// // 90)失效。图片竟然不能旋转了，故这里要旋转下
			// Matrix matrix = new Matrix();
			// matrix.postRotate((float) 90.0);
			// Bitmap rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
			// mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
			//
			// // 旋转后rotaBitmap是960×1280.预览surfaview的大小是540×800
			// // 将960×1280缩放到540×800
			// Bitmap sizeBitmap = Bitmap.createScaledBitmap(rotaBitmap, 540,
			// 800,
			// true);
			// Bitmap rectBitmap = Bitmap.createBitmap(sizeBitmap, 100, 200,
			// 300,
			// 300);// 截取
		}
	};
	ImageTask imageTask;

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATA_PREVIEW_IMAGE:
				Bitmap bmp = (Bitmap) msg.obj;
				cutPreview.setImageBitmap(bmp);
				break;
			default:
				break;
			}
		}

	};

	public class ImageTask extends AsyncTask<Void, Void, Void> {

		private byte[] mData;

		// 构造函数
		ImageTask(byte[] data) {
			this.mData = data;
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Size size = mCamera.getParameters().getPreviewSize(); // 获取预览大小
			final int w = size.width; // 宽度
			final int h = size.height;

			final YuvImage image = new YuvImage(mData, ImageFormat.NV21, w, h,
					null);
			ByteArrayOutputStream os = new ByteArrayOutputStream(mData.length);
			if (!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)) {
				return null;
			}
			byte[] tmp = os.toByteArray();
			Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
			previewImage(bmp); // 自己定义的实时分析预览帧视频的算法
			return null;
		}

		private void previewImage(Bitmap bmp) {
			Bitmap ratioBitmap = adjustPhotoRotation(bmp, 90);
			int viewWidth = DensityUtil.dp2px(MainActivity.this, 400);
			int viewHeight = DensityUtil.dp2px(MainActivity.this, 400);
			int cutWidth = DensityUtil.dp2px(MainActivity.this, 200);
			int cutHeight = DensityUtil.dp2px(MainActivity.this, 50);
			System.out.println(viewWidth);
			System.out.println(viewHeight);
			int width = (int) (ratioBitmap.getWidth() * ((double) cutWidth / (double) viewWidth));
			int height = (int) (ratioBitmap.getHeight() * ((double) cutHeight / (double) viewHeight));
			Bitmap newBit = Bitmap.createBitmap(ratioBitmap,
					ratioBitmap.getWidth() / 2 - width / 2,
					ratioBitmap.getHeight() / 2 - height / 2, width, height);
			// int[] pixels = new int[newBit.getWidth() * newBit.getHeight()];
			// newBit.getPixels(pixels, 0, newBit.getWidth(), 0, 0,
			// newBit.getWidth(), newBit.getHeight());

			Image image = ImageAdapter4A.loadImage(newBit);
			GrayImage gray = image.grayWithAverage();
			BinaryImage binary = gray.binaryWithOstu();
			Bitmap bit = ImageAdapter4A.toBitmap(binary);
			Message msg = new Message();
			msg.what = UPDATA_PREVIEW_IMAGE;
			msg.obj = bit;
			handler.sendMessage(msg);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.activity_main);
		initSurfaceView();
	}

	// InitSurfaceView
	@SuppressWarnings({ "deprecation" })
	private void initSurfaceView() {

		mSurfaceview = (SurfaceView) this.findViewById(R.id.surfaceView);
		mSurfaceHolder = mSurfaceview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
		mSurfaceHolder.addCallback(MainActivity.this); // SurfaceHolder加入回调接口
		mSurfaceview.setZOrderOnTop(false);
		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);// translucent半透明
															// transparent透明
		// mSurfaceHolder.setFixedSize(176, 144); // 预览大小設置
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 設置顯示器類型，setType必须设置
		holdButton = (Button) findViewById(R.id.holdButton);
		holdButton.setOnTouchListener(new HoldListener());
		cutPreview = (ImageView) findViewById(R.id.cutPreview);
	}

	/* 【SurfaceHolder.Callback 回调函数】 */
	public void surfaceCreated(SurfaceHolder holder) {
		// SurfaceView启动时/初次实例化，预览界面被创建时，该方法被调用。
		mCamera = Camera.open();// 开启摄像头（2.3版本后支持多摄像头,需传入参数）
		try {
			Log.i(TAG, "SurfaceHolder.Callback：surface Created");
			mCamera.setPreviewDisplay(mSurfaceHolder);// set the surface to be
														// used for live preview

		} catch (Exception ex) {
			if (null != mCamera) {
				mCamera.release();
				mCamera = null;
			}
			Log.i(TAG + "initCamera", ex.getMessage());
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// 当SurfaceView/预览界面的格式和大小发生改变时，该方法被调用
		Log.i(TAG, "SurfaceHolder.Callback：Surface Changed");
		mPreviewHeight = height;
		mPreviewWidth = width;
		initCamera();
		mCamera.cancelAutoFocus();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// SurfaceView销毁时，该方法被调用
		Log.i(TAG, "SurfaceHolder.Callback：Surface Destroyed");
		if (null != mCamera) {
			mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
			mCamera.stopPreview();
			bIfPreview = false;
			mCamera.release();
			mCamera = null;
		}
	}

	/* 【2】【相机预览】 */
	private void initCamera() {
		// surfaceChanged中调用
		Log.i(TAG, "going into initCamera");
		if (bIfPreview) {
			mCamera.stopPreview();// stopCamera();
		}
		if (null != mCamera) {
			try {
				// /* Camera Service settings */
				Camera.Parameters parameters = mCamera.getParameters();
				// parameters.setFlashMode("off"); // 无闪光灯
				// parameters.setPictureFormat(PixelFormat.JPEG); // Sets the
				// image
				// // format for
				// // picture
				// // 设定相片格式为JPEG，默认为NV21
				// parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP); //
				// Sets
				// // the
				// // image
				// // format
				// // for
				// // preview
				// // picture，默认为NV21
				// /*
				// * 【ImageFormat】JPEG/NV16(YCrCb format，used for
				// * Video)/NV21(YCrCb format，used for Image)/RGB_565/YUY2/YU12
				// */
				//
				// // 【调试】获取camera支持的PictrueSize，看看能否设置？？
				List<Size> pictureSizes = mCamera.getParameters()
						.getSupportedPictureSizes();
				List<Size> previewSizes = mCamera.getParameters()
						.getSupportedPreviewSizes();
				@SuppressWarnings("unused")
				Size psize = null;
				// for (int i = 0; i < pictureSizes.size(); i++) {
				// psize = pictureSizes.get(i);
				// }
				// for (int i = 0; i < previewSizes.size(); i++) {
				// psize = previewSizes.get(i);
				// }
				// // 设置拍照和预览图片大小
				pictureWidth = pictureSizes.get(pictureSizes.size() - 1).width;
				pictureHeight = pictureSizes.get(pictureSizes.size() - 1).height;
				previewWidth = previewSizes.get(previewSizes.size() - 1).width;
				previewHeight = previewSizes.get(previewSizes.size() - 1).height;
				parameters.setPictureSize(pictureWidth, pictureHeight); // 指定拍照图片的大小
				parameters.setPreviewSize(previewWidth, previewHeight); //

				// parameters.setPreviewSize(mPreviewWidth, mPreviewHeight); //
				// 指定preview的大小
				// // 这两个属性 如果这两个属性设置的和真实手机的不一样时，就会报错
				//
				// // 横竖屏镜头自动调整
				if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
					parameters.set("orientation", "portrait"); //
					parameters.set("rotation", 90); // 镜头角度转90度（默认摄像头是横拍）
					mCamera.setDisplayOrientation(90); // 在2.2以上可以使用
				} else// 如果是横屏
				{
					parameters.set("orientation", "landscape"); //
					mCamera.setDisplayOrientation(0); // 在2.2以上可以使用
				}

				parameters
						.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦

				/* 视频流编码处理 */
				// 添加对视频流处理函数

				// 设定配置参数并开启预览
				Log.d(TAG, "相机参数设置");
				mCamera.setParameters(parameters); // TODO 改变了参数设置
				// 将Camera.Parameters设定予Camera
				Log.d(TAG, "相机开始预览");
				mCamera.startPreview(); // 打开预览画面
				bIfPreview = true;

				// 【调试】设置后的图片大小和预览大小以及帧率
				Camera.Size csize = mCamera.getParameters().getPreviewSize();
				mPreviewHeight = csize.height; //
				mPreviewWidth = csize.width;
				Log.i(TAG + "initCamera", "after setting, previewSize:width: "
						+ csize.width + " height: " + csize.height);
				csize = mCamera.getParameters().getPictureSize();
				Log.i(TAG + "initCamera", "after setting, pictruesize:width: "
						+ csize.width + " height: " + csize.height);
				Log.i(TAG + "initCamera", "after setting, previewformate is "
						+ mCamera.getParameters().getPreviewFormat());
				Log.i(TAG + "initCamera", "after setting, previewframetate is "
						+ mCamera.getParameters().getPreviewFrameRate());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public class HoldListener implements View.OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (v.getId() == R.id.holdButton) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// cutPreview.setVisibility(View.VISIBLE);
					mCamera.setPreviewCallback(mJpegPreviewCallback);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					// cutPreview.setVisibility(View.INVISIBLE);
					mCamera.setPreviewCallback(null);
				}
			}
			return false;
		}
	}

	public static Bitmap adjustPhotoRotation(Bitmap bm,
			final int orientationDegree) {
		Matrix m = new Matrix();
		m.setRotate(orientationDegree, (float) bm.getWidth() / 2,
				(float) bm.getHeight() / 2);

		try {
			Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
					bm.getHeight(), m, true);
			return bm1;
		} catch (OutOfMemoryError ex) {
		}
		return null;
	}

	/**
	 * 获取控件的高度或者宽度 isHeight=true则为测量该控件的高度，isHeight=false则为测量该控件的宽度
	 * 
	 * @param view
	 * @param isHeight
	 * @return
	 */
	public static int getViewHeight(View view) {
		int result;
		if (view == null)
			return 0;
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		view.measure(h, 0);
		result = view.getMeasuredHeight();

		return result;
	}

	public static int getViewWidth(View view) {
		int result;
		if (view == null)
			return 0;
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		view.measure(0, w);
		result = view.getMeasuredWidth();
		return result;
	}

}