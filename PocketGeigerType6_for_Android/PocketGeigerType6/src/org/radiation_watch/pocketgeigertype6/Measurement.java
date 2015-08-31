package org.radiation_watch.pocketgeigertype6;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

public class Measurement {
	
	private Globals mGlobals;
	
	private Timer mTimer = null;
	private Handler mHandler = null;

	//PocketGeiger
	private boolean mMeasured = false;
    
    private long mTotalMilisec = 0;
    private double mTotalSecond = 0;
    private double mTotalMinute = 0;
    
    private long mTotalCount = 0;
    private double mCpm = 0;
    private double mCpm_theta = 0;
    
    // Buff
    private int[] mBuffaCount = new int[9]; //100ms
    //
    private long mNoiseCount = 0;
    private boolean mNoiseReject = false;
    private long mNoiseMilisec = 1000;
    
	public Measurement(Globals globals) {
		// TODO
		mGlobals = globals;
	}
	
	private MeasurementNotify measurementNotifyListner = null;

	/// <summary>
    /// Add Listener
    /// @param listener
	/// </summary>
    public void setListener(MeasurementNotify listener){
        this.measurementNotifyListner = listener;
    }

	/// <summary>
    /// Delt Listener
	/// </summary>
    public void removeListener(){
        this.measurementNotifyListner = null;
    }

	/// <summary>
    /// Clear
	/// </summary>
	public void Clear() {
		mNoiseCount = 0;
		mTotalMilisec = 0;
		mTotalSecond = 0;
		mTotalCount = 0;
		mNoiseReject = false;
	}

	/// <summary>
    /// Measure Start
	/// </summary>
	public void Start() {
		
		mTimer = new Timer();
		mHandler = new Handler();
		
		mTimer.schedule( new TimerTask() {
	        @Override
	        public void run() {
	        	// 
	        	if (mNoiseReject) {
	        		mNoiseMilisec -= 100;
	        		for (int i = 0; i < mBuffaCount.length; i++)
	        			mBuffaCount[i] = 0;
	        		if (mNoiseMilisec < 100) {
	        			mNoiseReject = false; 
	        		}
	        	}else{
	        		mTotalMilisec += 100;
	        		mTotalCount += mBuffaCount[0];
	        		for (int i = 1; i < mBuffaCount.length; i++)
	        			mBuffaCount[i - 1] = mBuffaCount[i];
        			mBuffaCount[mBuffaCount.length - 1] = 0;
	        	}
	        	
            	if (mTotalMilisec > 0) {
            		mTotalSecond = Math.ceil((double) mTotalMilisec / 1000.0 * 100.0) / 100.0;
            		mTotalMinute = Math.ceil(mTotalSecond / 60.0 * 10000.0) / 10000.0;
            		double offsetCount = mGlobals.offset * mTotalMinute;
            		double totalCount = mTotalCount + offsetCount;
            		if (totalCount < 0) {
            			totalCount = 0.0;
            		}
            		//mCpm = Math.round( (double) mTotalCount / mTotalMinute * 10000.0 ) / 10000.0;
            		//mCpm_theta = Math.round( Math.sqrt( (double) mTotalCount) / mTotalMinute * 10000.0 ) / 10000.0;
            		mCpm = Math.round( (double) totalCount / mTotalMinute * 10000.0 ) / 10000.0;
            		mCpm_theta = Math.round( Math.sqrt( (double) totalCount) / mTotalMinute * 10000.0 ) / 10000.0;
            		
            		if (measurementNotifyListner != null) {
	            		mHandler.post( new Runnable() {
			                public void run() {
	                			measurementNotifyListner.onTimer(mCpm, mCpm_theta, mTotalSecond, mTotalMinute, mNoiseCount);
			                }
			            });
            		}
            	}
	        }
	    }, 100, 100);
		
		mMeasured = true;
	}
	
	public void Stop() {
		if(mTimer != null) {
			mTimer.cancel();
		}
		mTimer = null;
		mHandler = null;
	}
	
	public void SignalPulse(int value) {
		if (mMeasured) {
			mBuffaCount[mBuffaCount.length - 1] += value;
		}
	}

	public void NoisePulse(int value) {
		if (mMeasured) {
			if (mNoiseReject == false) {
        		mTotalMilisec -= 1000;
        		if (mTotalMilisec < 0)
        			mTotalMilisec = 0;
			}
				
			mNoiseReject = true;
			mNoiseMilisec = 1000;
			mNoiseCount += value;
		}
	}

}
