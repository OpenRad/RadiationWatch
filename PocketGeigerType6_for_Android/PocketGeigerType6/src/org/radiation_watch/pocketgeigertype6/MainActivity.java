/*
 * Copyright (C) 2014 Radiation-Watch
 *      http://www.radiation-watch.org/
 *
 * Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * [ PocketGeiger Type6 USB for Android ]
 * 
 *  This application project require Physicaloid Library project
 *
 *  Physicaloid Library https://github.com/ksksue/PhysicaloidLibrary
 * 
 *   How to use.
 * 
 *   1. Download PhysicaloidLibrary
 *   2. File -> import and select a PhysicaloidLibrary directory.
 *   3. Right click your project -> Properties -> Android -> click Library's "Add" button -> select PhysicaloidLibrary
 *   4. Select PhysicaloidLibrary project -> Select UsbVidList.java -> Add UsbVidList "MICROCHIP (0x04d8),"
 *
 * Special Thanks.
 *
 *  This code has built in knowledge of Physicaloid. Thanks to all Physicaloid coders.
 *  
 *  http://www.physicaloid.com/
 *
 */

package org.radiation_watch.pocketgeigertype6;

import java.io.IOException;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
//import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
//import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.UartConfig;

import org.radiation_watch.pocketgeigertype6.preference.CalibrationPrefActivity;
import org.radiation_watch.pocketgeigertype6.preference.MeasurementPrefActivity;
import org.radiation_watch.pocketgeigertype6.preference.SerialPrefActivity;

public class MainActivity extends Activity implements OnClickListener,
													  MeasurementNotify,
													  Runnable {

	private Globals mGlobals;

    // debug settings
    private static final boolean SHOW_DEBUG                 = false;

    public static final boolean isICSorHigher = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2 );

    // occurs USB packet loss if TEXT_MAX_SIZE is over 6000
    //private static final int TEXT_MAX_SIZE = 8192;

    private static final int MENU_ID_CALIBRATIONCOEFFICIENT = 0;
    private static final int MENU_ID_MEASUREMENTTIME        = 1;
    private static final int MENU_ID_SERIALSETTING          = 2;
    
    // Defines of Display Settings
    //private static final int DISP_CHAR  = 0;
    //private static final int DISP_DEC   = 1;
    //private static final int DISP_HEX   = 2;

    // Linefeed Code Settings
    private static final int LINEFEED_CODE_CR   = 0;
    private static final int LINEFEED_CODE_CRLF = 1;
    private static final int LINEFEED_CODE_LF   = 2;

    // Load Bundle Key (for view switching)
    private static final String BUNDLEKEY_LOADTEXTVIEW = "bundlekey.LoadTextView";
	
    private Physicaloid mSerial;

    //private ScrollView mSvText;
    private TextView mTvSerial;
    //private StringBuilder mText = new StringBuilder();
    private boolean mStop = false;

	private final String TAG = MainActivity.class.getSimpleName();

    //private Handler mHandler = new Handler();

    private BackSurfaceViewCallback mBackSurfaceViewCallback;
    private int mViewWidth;
    private int mViewHeight;
    private LinearLayout controlOverlayView;
    private Switch swMeasure;
    private SeekBar seekBar;
    private ProgressBar progressBar;
    
    // Default settings
    //private Typeface mTextTypeface  = Typeface.MONOSPACE;
    //private int mDisplayType        = DISP_CHAR;
    //private int mReadLinefeedCode   = LINEFEED_CODE_LF;
    private int mWriteLinefeedCode  = LINEFEED_CODE_LF;
    private int mBaudrate           = 38400;
    private int mDataBits           = UartConfig.DATA_BITS8;
    private int mParity             = UartConfig.PARITY_NONE;
    private int mStopBits           = UartConfig.STOP_BITS1;
    private int mFlowControl        = UartConfig.FLOW_CONTROL_OFF;

    private boolean mRunningMainLoop = false;

    private static final String ACTION_USB_PERMISSION =
            "org.radiation_watch.pocketgeigertype6.USB_PERMISSION";

    // Linefeed
    //private final static String BR = System.getProperty("line.separator");

    private Measurement mMeasurement = null;    
    private boolean mDataMark = false;
    private double mCoefficient = 53.032;
    private double mOffset = 0;
    private int mMeasurementTime = 600;
    private double mDoseRate;
    private double mDoseRate_theta;
    private int mSignal = 0;
    private int mNoise = 0;
    private boolean mSeparated = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle("");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        

		setContentView(R.layout.activity_main);
		
		mGlobals = (Globals) getApplication();

        mBackSurfaceViewCallback = (BackSurfaceViewCallback) findViewById(R.id.surface_view_1);
        mBackSurfaceViewCallback.SetGlobals(mGlobals);
		
        mGlobals.modelName =  Build.MODEL.toString();
        
        controlOverlayView = (LinearLayout) getLayoutInflater().inflate(R.layout.control_overlay, null);
        try {
        	LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	        addContentView(controlOverlayView, params);
	    } catch (Exception ex) {
			ex.printStackTrace();
		}
		
        getWindowsSize();
        getControls();
	}
	
	private void getControls() {
		
        //mSvText = (ScrollView) controlOverlayView.findViewById(R.id.svText);
        mTvSerial = (TextView) controlOverlayView.findViewById(R.id.tvSerial);

        swMeasure = (Switch) controlOverlayView.findViewById(R.id.Measure_Switch);
        swMeasure.setScaleX(2.0f);
        swMeasure.setScaleY(2.0f);
        
        progressBar =  (ProgressBar) controlOverlayView.findViewById(R.id.progressBar1);
        progressBar.setScaleY(3);
        progressBar.setMax(mMeasurementTime);

        //DEBUG
        seekBar = (SeekBar) controlOverlayView.findViewById(R.id.seekBar1);
        seekBar.setMax(600);
        seekBar.setProgress(0);
        seekBar.setVisibility(View.INVISIBLE);
        //DEBUG
	}

	private void setControlsBehavior() {
		
		swMeasure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO
				if (isChecked) {
					if (mSerial != null && !mSerial.isOpened()) {
						openUsbSerial();
						if (mSerial.isOpened()) {
							MeasureStart();
		                    writeDataToSerial("S");
						}
					}
				}else{
					if (mSerial != null && mSerial.isOpened()) {
	                    writeDataToSerial("E");
						closeUsbSerial();
						if (!mSerial.isOpened()) {
							MeasureStop();
						}
					}
				}
			}
		});

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	        public void onProgressChanged(SeekBar seekBar,
	                int progress, boolean fromUser) {
                    	mGlobals.doseRate = (double) progress / 10.0;
                    }
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
        });
	}
	
    private void getWindowsSize() {

    	// Get an instance of the window manager
    	WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);

    	// It creates an instance of a display
    	Display disp = wm.getDefaultDisplay();
        Point p = new Point();
        disp.getSize(p);

        mViewWidth = p.x + 0;
        mViewHeight = p.y + 0;
        //if (isPortrait()) {
        //    mViewWidth = p.y + 1;
        //    mViewHeight = p.x + 1;
        //}else{
        //}
        mGlobals.X_Scale = (float) mViewWidth / 540.0f;
        mGlobals.Y_Scale = (float) mViewHeight / 960.0f;
        mGlobals.X1p20 = mViewWidth / 20;
        mGlobals.Y1p20 = mViewHeight / 20;
    }

    //*******************************************************************************************************
	//
	//*******************************************************************************************************
	private void MeasureStart() {
		
		loadCalibrationSettingValues();
		loadMeasurementSettingValues();

		progressBar.setProgress(0);
		progressBar.setMax(mMeasurementTime);
		mGlobals.measurementTime = mMeasurementTime;
		mGlobals.progress_top = progressBar.getTop() + progressBar.getHeight();

		mGlobals.cpm = 0;
		mGlobals.doseRate = 0;
		mGlobals.doseRate_theta = 0;
		mGlobals.second = 0;
		
		mDataMark = false;
		mMeasurement.setListener(this);
		mMeasurement.Clear();
		mMeasurement.Start();
		mGlobals.measure = true;
	}
	
	private void MeasureStop() {
		if (mMeasurement != null) {
			mMeasurement.Stop();
			mMeasurement.removeListener();
			mGlobals.measure = false;
		}
	}

	//*******************************************************************************************************
	//
	//*******************************************************************************************************
    private void writeDataToSerial(String writeData) {
        String strWrite = writeData.toString();
        strWrite = changeEscapeSequence(strWrite);
        if (SHOW_DEBUG) {
            Log.d(TAG, "FTDriver Write(" + strWrite.length() + ") : " + strWrite);
        }
        mSerial.write(strWrite.getBytes(), strWrite.length());
    }

    private String changeEscapeSequence(String in) {
        String out = new String();
        try {
            out = unescapeJava(in);
        } catch (IOException e) {
            return "";
        }
        switch (mWriteLinefeedCode) {
            case LINEFEED_CODE_CR:
                out = out + "\r";
                break;
            case LINEFEED_CODE_CRLF:
                out = out + "\r\n";
                break;
            case LINEFEED_CODE_LF:
                out = out + "\n";
                break;
            default:
        }
        return out;
    }

    // ---------------------------------------------------------------------------------------
    // Option Menu
    // ---------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
    	getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (!mGlobals.measure) {
			switch (item.getItemId()) {
            case R.id.opt_calibration_coefficient:
                Intent itCalibration = new Intent(this, CalibrationPrefActivity.class);
                startActivityForResult(itCalibration, MENU_ID_CALIBRATIONCOEFFICIENT);
                return true;
            case R.id.opt_measurement_time:
                Intent itMeasurement = new Intent(this, MeasurementPrefActivity.class);
                startActivityForResult(itMeasurement, MENU_ID_MEASUREMENTTIME);
                return true;
            case R.id.opt_serial_setting:                
                Intent itSerial = new Intent(this, SerialPrefActivity.class);
                startActivityForResult(itSerial, MENU_ID_SERIALSETTING);
                return true;
            default:
                return false;
			}
		}else{
			return false;
		}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MENU_ID_CALIBRATIONCOEFFICIENT) {
        } else if (requestCode == MENU_ID_MEASUREMENTTIME) {
        } else if (requestCode == MENU_ID_SERIALSETTING) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String res;
            int intRes;

            res = pref.getString("baudrate_list", Integer.toString(9600));
            intRes = Integer.valueOf(res);
            if (mBaudrate != intRes) {
                mBaudrate = intRes;
                mSerial.setBaudrate(mBaudrate);
            }

            res = pref.getString("databits_list", Integer.toString(UartConfig.DATA_BITS8));
            intRes = Integer.valueOf(res);
            if (mDataBits != intRes) {
                mDataBits = Integer.valueOf(res);
                mSerial.setDataBits(mDataBits);
            }

            res = pref.getString("parity_list",
                    Integer.toString(UartConfig.PARITY_NONE));
            intRes = Integer.valueOf(res);
            if (mParity != intRes) {
                mParity = intRes;
                mSerial.setParity(mParity);
            }

            res = pref.getString("stopbits_list",
                    Integer.toString(UartConfig.STOP_BITS1));
            intRes = Integer.valueOf(res);
            if (mStopBits != intRes) {
                mStopBits = intRes;
                mSerial.setStopBits(mStopBits);
            }

            res = pref.getString("flowcontrol_list",
                    Integer.toString(UartConfig.FLOW_CONTROL_OFF));
            intRes = Integer.valueOf(res);
            if (mFlowControl != intRes) {
                mFlowControl = intRes;
                if(mFlowControl == UartConfig.FLOW_CONTROL_ON) {
                    mSerial.setDtrRts(true, true);
                } else {
                    mSerial.setDtrRts(false, false);
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // End of Menu button
    // ---------------------------------------------------------------------------------------

    /**
     * Saves values for view switching
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLEKEY_LOADTEXTVIEW, mTvSerial.getText().toString());
    }

    /**
     * Loads values for view switching
     */

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTvSerial.setText(savedInstanceState.getString(BUNDLEKEY_LOADTEXTVIEW));
    }

    @Override
    public void onDestroy() {
        mSerial.close();
        mStop = true;
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
        if (mMeasurement != null) {
        	mMeasurement.Stop();
        	mMeasurement.removeListener();
        	mMeasurement = null;
        }
    }

    private void mainloop() {
        mStop = false;
        mRunningMainLoop = true;
        if (SHOW_DEBUG) {
            Log.d(TAG, "start mainloop");
        }
        new Thread(mLoop).start();
    }

    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            int len;
            byte[] rbuf = new byte[4096];

            for (;;) {// this is the main loop for transferring

                len = mSerial.read(rbuf);
                rbuf[len] = 0;
                
                if (len > 0) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Read  Length : " + len);
                    }
                    
                    for (int i = 0; i < len; i++) {
                    	if (rbuf[i] == '>') {
                    		mDataMark = true;
                            mSignal = 0;
                            mNoise = 0;
                            mSeparated = false;
                    	}else{
                    		if (mDataMark) {
                    			if (rbuf[i] >= '0' && rbuf[i] <= '9') {
                    				if (mSeparated) {
                    					mNoise = mNoise * 10 + (rbuf[i] - 48);
                    				}else{
                        				mSignal = mSignal * 10 + (rbuf[i] - 48);
                    				}
                    			}
                    			if (rbuf[i] == ',') {
                    				mSeparated = true;
                    			}
                        		if (rbuf[i] == 0x0d) {
                    				if (mMeasurement != null) {
                    					if (mSignal > 0) {
                        					mMeasurement.SignalPulse(mSignal);
                        					mGlobals.SignalPulse += mSignal;
                        					mSignal = 0;
                        				}
                            			if (mNoise > 0) {
	                    					mMeasurement.NoisePulse(mNoise);
                        					int noisePulse = mGlobals.NoisePulse + 10;
	                    					if (noisePulse > 50) {
	                        					mGlobals.NoisePulse = 50;
	                    					}else{
	                        					mGlobals.NoisePulse += 10;
	                    					}
	                    					mNoise = 0;
                            			}                    			
                        			}
                        		}
                    		}
                    		if (rbuf[i] == 0x0d) {
                    			mDataMark = false;
                    		}
                    	}
                    }

                    /*
	                mHandler.post(new Runnable() {
	                    public void run() {
	                        if (mTvSerial.length() > TEXT_MAX_SIZE) {
	                            StringBuilder sb = new StringBuilder();
	                            sb.append(mTvSerial.getText());
	                            sb.delete(0, TEXT_MAX_SIZE / 2);
	                            mTvSerial.setText(sb);
	                        }
	                        mTvSerial.append(mText);
	                        mText.setLength(0);
	                        mSvText.fullScroll(ScrollView.FOCUS_DOWN);
	                    }
	                });
	                */
	            }
	
	            try {
	                Thread.sleep(50);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	
	            if (mStop) {
	                mRunningMainLoop = false;
	                return;
	            }
	        }
	    }
    };

    boolean lastDataIs0x0D = false;

    void loadCalibrationSettingValues() {
    	
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        String res = pref.getString("calibration_coefficient", Double.toString(53.032));
        if ("".equals(res)) {
            mCoefficient = 53.032;
            mGlobals.coefficient = mCoefficient;
        }else{
        	mCoefficient = Double.valueOf(res);
            mGlobals.coefficient = mCoefficient;
        }
        res = pref.getString("calibration_offset", Double.toString(0));
        if ("".equals(res)) {
        	mOffset = 0;
            mGlobals.offset = mOffset;
        }else{
        	mOffset = Double.valueOf(res);
            mGlobals.offset = mOffset;
        }
    }

    void loadMeasurementSettingValues() {
    	
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        String res = pref.getString("measurementtime_list", Integer.toString(600));
    	mMeasurementTime = Integer.valueOf(res);
    }
    
    void loadSerialSettingValues() {
        	
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        
        String res = pref.getString("baudrate_list", Integer.toString(38400));
        mBaudrate = Integer.valueOf(res);

        res = pref.getString("databits_list", Integer.toString(UartConfig.DATA_BITS8));
        mDataBits = Integer.valueOf(res);

        res = pref.getString("parity_list", Integer.toString(UartConfig.PARITY_NONE));
        mParity = Integer.valueOf(res);

        res = pref.getString("stopbits_list", Integer.toString(UartConfig.STOP_BITS1));
        mStopBits = Integer.valueOf(res);

        res = pref.getString("flowcontrol_list", Integer.toString(UartConfig.FLOW_CONTROL_OFF));
        mFlowControl = Integer.valueOf(res);
    }

    // Load default baud rate
    int loadDefaultBaudrate() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String res = pref.getString("baudrate_list", Integer.toString(9600));
        return Integer.valueOf(res);
    }
    
    private void openUsbSerial() {
        if(mSerial == null) {
            mGlobals.openSerial = false;
        	return;
        }

        if (!mSerial.isOpened()) {
            if (SHOW_DEBUG) {
                Log.d(TAG, "onNewIntent begin");
            }
            
            if (!mSerial.open()) {
                mGlobals.openSerial = false;
                return;
            } else {
            	
            	loadSerialSettingValues();

                boolean dtrOn=false;
                boolean rtsOn=false;
                if(mFlowControl == UartConfig.FLOW_CONTROL_ON) {
                    dtrOn = true;
                    rtsOn = true;
                }
                mSerial.setConfig(new UartConfig(mBaudrate, mDataBits, mStopBits, mParity, dtrOn, rtsOn));

                if(SHOW_DEBUG) {
                    Log.d(TAG, "setConfig : baud : "+mBaudrate+", DataBits : "+mDataBits+", StopBits : "+mStopBits+", Parity : "+mParity+", dtr : "+dtrOn+", rts : "+rtsOn);
                }

                mGlobals.openSerial = true;
            }
        }
        
        if (!mRunningMainLoop) {
            mainloop();
        }

    }

    private void closeUsbSerial() {
        detachedUi();
        mStop = true;
        mSerial.close();
        mGlobals.openSerial = false;
    }

    protected void onNewIntent(Intent intent) {
        if (SHOW_DEBUG) {
            Log.d(TAG, "onNewIntent");
        }
        
        openUsbSerial();
    };

    private void detachedUi() {
        //Toast.makeText(this, "disconnect", Toast.LENGTH_SHORT).show();
        mGlobals.measure = false;
        mGlobals.openSerial = false;
    }

    // BroadcastReceiver when insert/remove the device USB plug into/from a USB
    // port
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Device attached");
                }
                if (!mSerial.isOpened()) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Device attached begin");
                    }
                    openUsbSerial();
                }
                if (!mRunningMainLoop) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Device attached mainloop");
                    }
                    mainloop();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Device detached");
                }
                mStop = true;
                detachedUi();
//                mSerial.usbDetached(intent);
                mSerial.close();
				MeasureStop();
    			swMeasure.setChecked(false);
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Request permission");
                }
                synchronized (this) {
                    if (!mSerial.isOpened()) {
                        if (SHOW_DEBUG) {
                            Log.d(TAG, "Request permission begin");
                        }
                        openUsbSerial();
                    }
                }
                if (!mRunningMainLoop) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Request permission mainloop");
                    }
                    mainloop();
                }
            }
        }
    };


    /**
     * <p>Unescapes any Java literals found in the <code>String</code> to a
     * <code>Writer</code>.</p>
     *
     * <p>For example, it will turn a sequence of <code>'\'</code> and
     * <code>'n'</code> into a newline character, unless the <code>'\'</code>
     * is preceded by another <code>'\'</code>.</p>
     * 
     * <p>A <code>null</code> string input has no effect.</p>
     * 
     * @param out  the <code>String</code> used to output unescaped characters
     * @param str  the <code>String</code> to unescape, may be null
     * @throws IllegalArgumentException if the Writer is <code>null</code>
     * @throws IOException if error occurs on underlying Writer
     */
    private String unescapeJava(String str) throws IOException {
        if (str == null) {
            return "";
        }
        int sz = str.length();
        StringBuffer unicode = new StringBuffer(4);

        StringBuilder strout = new StringBuilder();
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (inUnicode) {
                // if in unicode, then we're reading unicode
                // values in somehow
                unicode.append(ch);
                if (unicode.length() == 4) {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                    try {
                        int value = Integer.parseInt(unicode.toString(), 16);
                        strout.append((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    } catch (NumberFormatException nfe) {
                        // throw new NestableRuntimeException("Unable to parse unicode value: " + unicode, nfe);
                        throw new IOException("Unable to parse unicode value: " + unicode, nfe);
                    }
                }
                continue;
            }
            if (hadSlash) {
                // handle an escaped value
                hadSlash = false;
                switch (ch) {
                    case '\\':
                        strout.append('\\');
                        break;
                    case '\'':
                        strout.append('\'');
                        break;
                    case '\"':
                        strout.append('"');
                        break;
                    case 'r':
                        strout.append('\r');
                        break;
                    case 'f':
                        strout.append('\f');
                        break;
                    case 't':
                        strout.append('\t');
                        break;
                    case 'n':
                        strout.append('\n');
                        break;
                    case 'b':
                        strout.append('\b');
                        break;
                    case 'u':
                        {
                            // uh-oh, we're in unicode country....
                            inUnicode = true;
                            break;
                        }
                    default :
                        strout.append(ch);
                        break;
                }
                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }
            strout.append(ch);
        }
        if (hadSlash) {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            strout.append('\\');
        }
        return new String(strout.toString());
    }    

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO
	}
	
	@Override 
	public void onWindowFocusChanged(boolean hasFocus) {  
	    super.onWindowFocusChanged(hasFocus);
    	
        try{
            Paint pText = new Paint();
            pText.setTextSize(30 * mGlobals.X_Scale);
            String ElapsedTime = "Elapsed Time = 00:00";
            float textWidth = pText.measureText(ElapsedTime);
    	    float progWidth = progressBar.getWidth();
    	    float progScale = textWidth / progWidth;
            
        	progressBar.setScaleX(progScale);
    	    int w1 = progressBar.getWidth();
        	RelativeLayout.LayoutParams p2 = (RelativeLayout.LayoutParams) progressBar.getLayoutParams();
        	p2.topMargin = (int) (mGlobals.Y1p20 * 7 + 10 * mGlobals.Y_Scale);
        	p2.leftMargin = (int) ((mViewWidth - w1) / 2);

    	    int h2 = swMeasure.getHeight();
    	    int w2 = swMeasure.getWidth();
        	RelativeLayout.LayoutParams p1 = (RelativeLayout.LayoutParams) swMeasure.getLayoutParams();
        	p1.topMargin = (int) ((float)mGlobals.Y1p20 * 13f + (float)h2);
        	p1.leftMargin = (int) ((mViewWidth - w2) / 2);
        	
        }catch (Exception e) {
        	e.printStackTrace();
        }

        // get service
        if (mSerial == null) {
            mSerial = new Physicaloid(this);
            
            if (SHOW_DEBUG) {
                Log.d(TAG, "New instance : " + mSerial);
            }
            
            // listen for new devices
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_USB_PERMISSION);
            registerReceiver(mUsbReceiver, filter);

            if (SHOW_DEBUG) {
                Log.d(TAG, "FTDriver beginning");
            }

            openUsbSerial();
            
    		if (mSerial != null && mSerial.isOpened()) {
    			closeUsbSerial();
    		}
        }
		
		if (mMeasurement == null) {
			mMeasurement = new Measurement(mGlobals);
		}
		
        setControlsBehavior();
	}
	
	@Override
    protected void onStart() {
        super.onStart();

    }

	@Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMeasurement == null) {
    		mMeasurement = new Measurement(mGlobals);
        }
    }

	@Override
	public void run() {
		// TODO
	}

	@Override
	public void onTimer(double cpm, double cpm_theta, double second, double minute, long noiseCount) {
		mDoseRate =  Math.round( cpm / mCoefficient * 100.0 ) / 100.0;
		mDoseRate_theta = Math.round( cpm_theta / mCoefficient * 100.0 ) / 100.0;
		
		mGlobals.cpm = cpm;
		mGlobals.doseRate = mDoseRate;
		mGlobals.doseRate_theta = mDoseRate_theta;
		mGlobals.second = second;
		mGlobals.noiseCount = noiseCount;
		
		//if ((int)second <= 1200) {
		if ((int)second < mMeasurementTime) {
			progressBar.setProgress((int)second);
		}else{
			if ((int)second == mMeasurementTime) {
				progressBar.setProgress((int)second);
			}
			swMeasure.setChecked(false);
		}
	}
}
