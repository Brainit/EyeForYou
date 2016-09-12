package cn.brainit.eyeforyou;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("WrongCall")
public class MainActivity extends Activity implements SurfaceHolder.Callback {

	// 定义对象
	private SurfaceView mSurfaceview = null; // SurfaceView对象：(视图组件)视频显示
	private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder对象：(抽象接口)SurfaceView支持类
	private Camera mCamera = null; // Camera对象，相机预览

	private Button holdButton;

	private static final String TAG = "MainActivity";

	private boolean bIfPreview;
	@SuppressWarnings("unused")
	private int mPreviewWidth, mPreviewHeight;

	@SuppressWarnings("unused")
	private int previewWidth, previewHeight, pictureWidth, pictureHeight;
	PreviewCallback mJpegPreviewCallback = new Camera.PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// 传递进来的data,默认是YUV420SP的
			try {
				Log.i(TAG, "going into onPreviewFrame");

			} catch (Exception e) {
				Log.v("System.out", e.toString());
			}
		}
	};

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
				Toast.makeText(
						MainActivity.this,
						"参数信息:" + pictureWidth + "\\" + pictureHeight + "\\"
								+ previewWidth + "\\" + previewHeight,
						Toast.LENGTH_SHORT).show();
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
					mCamera.setPreviewCallback(mJpegPreviewCallback);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					mCamera.setPreviewCallback(null);
				}
			}
			return false;
		}
	}
}