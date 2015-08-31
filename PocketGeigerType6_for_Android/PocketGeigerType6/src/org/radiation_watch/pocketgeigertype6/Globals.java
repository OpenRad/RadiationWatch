package org.radiation_watch.pocketgeigertype6;

import android.app.Application;

public class Globals extends Application {
	
	public boolean openSerial = false;
	public boolean reset = true;
	public boolean measure = false;
	
	public double coefficient = 53.032;
	public double offset = 0.0;	
	public double doseRate = 0;
	public double doseRate_theta = 0;
	public double cpm = 0;
	public double second = 0;
	public int measurementTime = 0;
	public int SignalPulse = 0;
	public int NoisePulse = 0;
	public long noiseCount = 0;
	
	public String modelName = "";
	
	//Widh=540 Height=960 base size
	public float X_Scale = 1.0f;
	public float Y_Scale = 1.0f;
	public int X1p20 = 0;
	public int Y1p20 = 0;
	public int progress_top = 0;
	
	public Globals() {
		// TODO
	}

}
