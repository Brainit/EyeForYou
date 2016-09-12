package cn.brainit.eyeforyou;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * use ImageView to draw line for surfaceView
 * 
 * @author v1126
 * 
 */
public class DrawImageView extends ImageView {

	public DrawImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	Paint paint = new Paint();
	{
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2.5f);// 设置线宽
		paint.setAlpha(100);
	};

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawRect(new Rect(0, 0, 400, 200), paint);// 绘制矩形

	}

	protected void onDraw(Canvas canvas, View v) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		v.measure(w, h);
		int height = v.getMeasuredHeight();
		int width = v.getMeasuredWidth();
		System.out.println("width : " + width + "\theight : " + height);
		canvas.drawRect(new Rect(width / 4, height / 4, width / 2, width / 8),
				paint);// 绘制矩形

	}
}