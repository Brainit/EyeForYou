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

	// �������
	private SurfaceView mSurfaceview = null; // SurfaceView����(��ͼ���)��Ƶ��ʾ
	private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder����(����ӿ�)SurfaceView֧����
	private Camera mCamera = null; // Camera�������Ԥ��

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
			// ���ݽ�����data,Ĭ����YUV420SP��
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
				Toast.makeText(
						MainActivity.this,
						"������Ϣ:" + pictureWidth + "\\" + pictureHeight + "\\"
								+ previewWidth + "\\" + previewHeight,
						Toast.LENGTH_SHORT).show();
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