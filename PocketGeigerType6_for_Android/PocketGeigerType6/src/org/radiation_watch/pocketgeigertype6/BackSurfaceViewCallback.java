package org.radiation_watch.pocketgeigertype6;

import java.text.DecimalFormat;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BackSurfaceViewCallback extends SurfaceView implements SurfaceHolder.Callback, Runnable {

	private final static int WAIT_TIME = 50;
	
	private Globals mGlobals = null;

	private Bitmap imgHazardsymbolLoad;
	private Bitmap imgHazardsymbolDraw;
	
	private int mScrWidth;
    private int mScrHeight; 
    private SurfaceHolder mHolder;
    private boolean redraw = true;
    private float mDegree = 0;
    private int rotate_x = 0;
    private int rotate_y = 0;
    
    private Thread mThreadDraw;
    
    public void SetGlobals(Globals globals) {
    	mGlobals = globals;
    }
    public boolean ReDraw() {
    	return redraw;
    }
    public void ReDraw(boolean value) {
    	redraw = value;
    }

	public BackSurfaceViewCallback(Context context) {
		// TODO
        super(context);
        
        imgHazardsymbolLoad = BitmapFactory.decodeResource(getResources(), R.drawable.radiation);

        mHolder = this.getHolder();
        mHolder.addCallback(this);
	}

	public BackSurfaceViewCallback(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        imgHazardsymbolLoad = BitmapFactory.decodeResource(getResources(), R.drawable.radiation);

        mHolder = this.getHolder();
        mHolder.addCallback(this);
    }

	public BackSurfaceViewCallback(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        imgHazardsymbolLoad = BitmapFactory.decodeResource(getResources(), R.drawable.radiation);

        mHolder = this.getHolder();
        mHolder.addCallback(this);
    }
    
	@Override
	public void run() {
		// TODO

		Canvas canvas;

        long beginTime;
        long pastTick;
        int sleep = 0;

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setTextSize(60);
  
        while (this.mThreadDraw != null) {
            canvas = null;

            beginTime = System.currentTimeMillis();

            try {
                synchronized (mHolder) {
                    canvas = mHolder.lockCanvas();
                    if (canvas == null)
                        continue;
                    
                    if (redraw) {
                        //canvas.drawColor(0, Mode.CLEAR);
	                    canvas.save();
	                    draw(canvas);
	                    canvas.restore();
                    }
                }
            } finally {
                if (canvas != null) {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }

            pastTick = System.currentTimeMillis() - beginTime;
            sleep = (int)(WAIT_TIME - pastTick);
            
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (Exception e) {}
            }
        }		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO
		mThreadDraw = new Thread(this);
		mThreadDraw.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		// TODO
        mScrWidth = width;
        mScrHeight = height;
        
        Matrix matrix = new Matrix();

        float iw = (float) imgHazardsymbolLoad.getWidth();
        float rsz_ratio_w = 1.0f;
        float rsz_ratio_h = 1.0f;
        
        float w3 = mScrWidth / 2.0f;
        float w4 = (float) mGlobals.Y1p20 * 5.5f;
        if (w3 > w4) 
        	w3 = w4;
        if (iw >= w3) {
        	rsz_ratio_w = w3 / iw;
        }else{
        	rsz_ratio_w = iw / w3;        	
        }
        rsz_ratio_h = rsz_ratio_w;
        
        matrix.postScale(rsz_ratio_w, rsz_ratio_h);

        imgHazardsymbolDraw = Bitmap.createBitmap(imgHazardsymbolLoad, 0, 0, imgHazardsymbolLoad.getWidth(), imgHazardsymbolLoad.getHeight(), matrix, true);

        rotate_x = width / 2;
        rotate_y = imgHazardsymbolDraw.getHeight() / 2 + (int) (10.0f * mGlobals.Y_Scale);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO
		mThreadDraw = null;
	}

    public void draw(Canvas canvas) {
    	
    	int width = mScrWidth;
    	//int height = mScrHeight;

		Paint pBack = new Paint();
    	int c1 = Color.rgb(240, 240, 240);
    	int c2 = Color.rgb(224, 128, 0);
        Shader shader = new LinearGradient(mScrWidth / 2, 0, mScrWidth / 2, mScrHeight, c1, c2,  Shader.TileMode.CLAMP);  
        pBack.setShader(shader);
    	canvas.drawRect(new Rect(0, 0, mScrWidth, mScrHeight), pBack);


    	float textWidth;
        DecimalFormat df0 = new DecimalFormat("00");
        DecimalFormat df2 = new DecimalFormat("0.00");
        //DecimalFormat df3 = new DecimalFormat("0.000");

        Paint pText = new Paint();
        pText.setAntiAlias(true);
        pText.setTypeface(Typeface.DEFAULT_BOLD);
        pText.setColor(Color.BLACK);
        
        //pText.setTextSize(35);
        pText.setTextSize(18 * mGlobals.X_Scale);
        FontMetrics fMT1 = pText.getFontMetrics();
        String version = "Lite 1.00 Type6 USB";
        canvas.drawText(version, 5 * mGlobals.X_Scale, fMT1.bottom * 3, pText);
        
        //modelName
        textWidth = pText.measureText(mGlobals.modelName);
        canvas.drawText(mGlobals.modelName, width - textWidth - 20 * mGlobals.X_Scale, fMT1.bottom * 3, pText);
        
		Paint pSerial = new Paint();
		pSerial.setAntiAlias(true);
		pSerial.setAlpha(50);
		if (mGlobals.openSerial) {
			pSerial.setColor(Color.GREEN);
		}else{
			pSerial.setColor(Color.RED);
		}
		canvas.drawCircle(width - 10 * mGlobals.X_Scale, (fMT1.bottom * 4) / 2 , 5 * mGlobals.X_Scale, pSerial);		
        
        //Elapsed Time
        pText.setTextSize(30 * mGlobals.X_Scale);
        String ElapsedTime = "Elapsed Time = ";
    	long sec = (int) mGlobals.second;
    	long mm = sec / 60;
    	long ss = sec - mm * 60;
    	
        ElapsedTime += df0.format(mm) + ":";
        ElapsedTime += df0.format(ss);
        textWidth = pText.measureText(ElapsedTime);
        canvas.drawText(ElapsedTime, (width - textWidth) / 2,  mGlobals.Y1p20 * 7, pText);
        
        //Progreess
        if (mGlobals.measure) {
            Paint pText2 = new Paint();
            pText2.setColor(Color.BLUE);
            pText2.setAntiAlias(true);
            pText2.setTypeface(Typeface.DEFAULT_BOLD);
            pText2.setTextSize(20 * mGlobals.X_Scale);
            FontMetrics fMT2 = pText2.getFontMetrics();
            int percent = 0;
            if (mGlobals.measurementTime > 0) {
            	percent = (int) ((float)sec / (float)mGlobals.measurementTime * 100); 
            }
            String progress = "( " + Integer.toString(mGlobals.measurementTime / 60) + " min : ";
            progress += Integer.toString(percent) + "% )";
            textWidth = pText2.measureText(progress);
            canvas.drawText(progress, (width - textWidth) / 2, fMT2.bottom * 3 + mGlobals.progress_top, pText2);
        }
        
        //cpm
        pText.setTextSize(45 * mGlobals.X_Scale);
        String cpm_literal = "[ cpm ]";
        canvas.drawText(cpm_literal, mGlobals.X1p20 * 12, mGlobals.Y1p20 * 10, pText);
        String cpm = df2.format(mGlobals.cpm);
        textWidth = pText.measureText(cpm);
        canvas.drawText(cpm, mGlobals.X1p20 * 12 - textWidth - 10, mGlobals.Y1p20 * 10, pText);

        Paint pSignal = new Paint();
        if (mGlobals.NoisePulse > 0) {
            pSignal.setColor(Color.RED);
    		canvas.drawCircle(mGlobals.X1p20 * 4, mGlobals.Y1p20 * 10 - 18 * mGlobals.Y_Scale, 18 * mGlobals.X_Scale, pSignal);		
        	mGlobals.NoisePulse -= 1;
        }else{
        	if (mGlobals.SignalPulse > 0) {
                pSignal.setColor(Color.YELLOW);
        		canvas.drawCircle(mGlobals.X1p20 * 4, mGlobals.Y1p20 * 10 - 18 * mGlobals.Y_Scale, 18 * mGlobals.X_Scale, pSignal);		
        		//canvas.drawCircle(w10 * 3, h10 * 6 - 18 * mGlobals.Y_Scale, 18 * mGlobals.X_Scale, pSignal);		
        		//canvas.drawCircle(270, h10 * 6 - 35, 30, pSignal);		
            	mGlobals.SignalPulse -= 1;
        	}
        } 
        
        //uSv
        String uSvh_literal = "[ É Sv/h ]";
        canvas.drawText(uSvh_literal, mGlobals.X1p20 * 12, mGlobals.Y1p20 * 12, pText);
        String uSvh = df2.format(mGlobals.doseRate);
        uSvh += " Å} ";
        uSvh += df2.format(mGlobals.doseRate_theta);
        textWidth = pText.measureText(uSvh);
        canvas.drawText(uSvh, mGlobals.X1p20 * 12 - textWidth - 10, mGlobals.Y1p20 * 12, pText);
        
        if (mGlobals != null) {
        	if (mGlobals.second < 1) {
        		mDegree = 0;
        		mGlobals.SignalPulse = 0;
        		mGlobals.NoisePulse = 0;
        	}
        	if (mGlobals.measure) {
        		if (mGlobals.doseRate > 60.0) {
                	mDegree += 60.0;
        		}else{
            		if (mGlobals.doseRate >= 0.01) {
                    	mDegree += mGlobals.doseRate * 1;
            		}
        		}
        	}
    	}
    	
    	if (mDegree > 360) {
    		mDegree -= 360;
    	}
    	
    	if (imgHazardsymbolDraw != null) {
        	int ix = rotate_x - imgHazardsymbolDraw.getWidth() / 2;
        	int iy = rotate_y - imgHazardsymbolDraw.getHeight() / 2;
        	
            Paint pImage = new Paint();
            pImage.setAntiAlias(true);

            //int imgHeight = mGlobals.image002_green.getHeight();
            canvas.rotate(mDegree, rotate_x, rotate_y);
            
            canvas.drawBitmap(imgHazardsymbolDraw, ix, iy, pImage);
    	}
    }
}
