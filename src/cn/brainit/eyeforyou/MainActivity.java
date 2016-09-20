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

	// �������
	private SurfaceView mSurfaceview = null; // SurfaceView����(��ͼ���)��Ƶ��ʾ
	private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder����(����ӿ�)SurfaceView֧����
	private Camera mCamera = null; // Camera�������Ԥ��
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
						camera.cancelAutoFocus();// ֻ�м�������һ�䣬�Ż��Զ��Խ���
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
			// data.length);// data���ֽ����ݣ����������λͼ
			// if (mBitmap != null) {
			// Bitmap newBit = Bitmap.createBitmap(mBitmap, 100, 100,
			// 100, 100);
			// cutPreview.setImageBitmap(newBit);
			// }
			//
			// } else {
			// System.out.println("�յ�bitmap");
			// }
			//
			// // mCamera.stopPreview();
			// }
			// bufferedImage --> bitmap
			// // ����FOCUS_MODE_CONTINUOUS_VIDEO)֮��myParam.set("rotation",
			// // 90)ʧЧ��ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��
			// Matrix matrix = new Matrix();
			// matrix.postRotate((float) 90.0);
			// Bitmap rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
			// mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
			//
			// // ��ת��rotaBitmap��960��1280.Ԥ��surfaview�Ĵ�С��540��800
			// // ��960��1280���ŵ�540��800
			// Bitmap sizeBitmap = Bitmap.createScaledBitmap(rotaBitmap, 540,
			// 800,
			// true);
			// Bitmap rectBitmap = Bitmap.createBitmap(sizeBitmap, 100, 200,
			// 300,
			// 300);// ��ȡ
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

		// ���캯��
		ImageTask(byte[] data) {
			this.mData = data;
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Size size = mCamera.getParameters().getPreviewSize(); // ��ȡԤ����С
			final int w = size.width; // ���
			final int h = size.height;

			final YuvImage image = new YuvImage(mData, ImageFormat.NV21, w, h,
					null);
			ByteArrayOutputStream os = new ByteArrayOutputStream(mData.length);
			if (!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)) {
				return null;
			}
			byte[] tmp = os.toByteArray();
			Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
			previewImage(bmp); // �Լ������ʵʱ����Ԥ��֡��Ƶ���㷨
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
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ��������
		setContentView(R.layout.activity_main);
		initSurfaceView();
	}

	// InitSurfaceView
	@SuppressWarnings({ "deprecation" })
	private void initSurfaceView() {

		mSurfaceview = (SurfaceView) this.findViewById(R.id.surfaceView);
		mSurfaceHolder = mSurfaceview.getHolder(); // ��SurfaceView��ȡ��SurfaceHolder����
		mSurfaceHolder.addCallback(MainActivity.this); // SurfaceHolder����ص��ӿ�
		mSurfaceview.setZOrderOnTop(false);
		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);// translucent��͸��
															// transparent͸��
		// mSurfaceHolder.setFixedSize(176, 144); // Ԥ����С�O��
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// �O���@ʾ����ͣ�setType��������
		holdButton = (Button) findViewById(R.id.holdButton);
		holdButton.setOnTouchListener(new HoldListener());
		cutPreview = (ImageView) findViewById(R.id.cutPreview);
	}

	/* ��SurfaceHolder.Callback �ص������� */
	public void surfaceCreated(SurfaceHolder holder) {
		// SurfaceView����ʱ/����ʵ������Ԥ�����汻����ʱ���÷��������á�
		mCamera = Camera.open();// ��������ͷ��2.3�汾��֧�ֶ�����ͷ,�贫�������
		try {
			Log.i(TAG, "SurfaceHolder.Callback��surface Created");
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
		// ��SurfaceView/Ԥ������ĸ�ʽ�ʹ�С�����ı�ʱ���÷���������
		Log.i(TAG, "SurfaceHolder.Callback��Surface Changed");
		mPreviewHeight = height;
		mPreviewWidth = width;
		initCamera();
		mCamera.cancelAutoFocus();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// SurfaceView����ʱ���÷���������
		Log.i(TAG, "SurfaceHolder.Callback��Surface Destroyed");
		if (null != mCamera) {
			mCamera.setPreviewCallback(null); // �������������ǰ����Ȼ�˳�����
			mCamera.stopPreview();
			bIfPreview = false;
			mCamera.release();
			mCamera = null;
		}
	}

	/* ��2�������Ԥ���� */
	private void initCamera() {
		// surfaceChanged�е���
		Log.i(TAG, "going into initCamera");
		if (bIfPreview) {
			mCamera.stopPreview();// stopCamera();
		}
		if (null != mCamera) {
			try {
				// /* Camera Service settings */
				Camera.Parameters parameters = mCamera.getParameters();
				// parameters.setFlashMode("off"); // �������
				// parameters.setPictureFormat(PixelFormat.JPEG); // Sets the
				// image
				// // format for
				// // picture
				// // �趨��Ƭ��ʽΪJPEG��Ĭ��ΪNV21
				// parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP); //
				// Sets
				// // the
				// // image
				// // format
				// // for
				// // preview
				// // picture��Ĭ��ΪNV21
				// /*
				// * ��ImageFormat��JPEG/NV16(YCrCb format��used for
				// * Video)/NV21(YCrCb format��used for Image)/RGB_565/YUY2/YU12
				// */
				//
				// // �����ԡ���ȡcamera֧�ֵ�PictrueSize�������ܷ����ã���
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
				// // �������պ�Ԥ��ͼƬ��С
				pictureWidth = pictureSizes.get(pictureSizes.size() - 1).width;
				pictureHeight = pictureSizes.get(pictureSizes.size() - 1).height;
				previewWidth = previewSizes.get(previewSizes.size() - 1).width;
				previewHeight = previewSizes.get(previewSizes.size() - 1).height;
				parameters.setPictureSize(pictureWidth, pictureHeight); // ָ������ͼƬ�Ĵ�С
				parameters.setPreviewSize(previewWidth, previewHeight); //

				// parameters.setPreviewSize(mPreviewWidth, mPreviewHeight); //
				// ָ��preview�Ĵ�С
				// // ���������� ����������������õĺ���ʵ�ֻ��Ĳ�һ��ʱ���ͻᱨ��
				//
				// // ��������ͷ�Զ�����
				if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
					parameters.set("orientation", "portrait"); //
					parameters.set("rotation", 90); // ��ͷ�Ƕ�ת90�ȣ�Ĭ������ͷ�Ǻ��ģ�
					mCamera.setDisplayOrientation(90); // ��2.2���Ͽ���ʹ��
				} else// ����Ǻ���
				{
					parameters.set("orientation", "landscape"); //
					mCamera.setDisplayOrientation(0); // ��2.2���Ͽ���ʹ��
				}

				parameters
						.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1�����Խ�

				/* ��Ƶ�����봦�� */
				// ��Ӷ���Ƶ��������

				// �趨���ò���������Ԥ��
				Log.d(TAG, "�����������");
				mCamera.setParameters(parameters); // TODO �ı��˲�������
				// ��Camera.Parameters�趨��Camera
				Log.d(TAG, "�����ʼԤ��");
				mCamera.startPreview(); // ��Ԥ������
				bIfPreview = true;

				// �����ԡ����ú��ͼƬ��С��Ԥ����С�Լ�֡��
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
	 * ��ȡ�ؼ��ĸ߶Ȼ��߿�� isHeight=true��Ϊ�����ÿؼ��ĸ߶ȣ�isHeight=false��Ϊ�����ÿؼ��Ŀ��
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