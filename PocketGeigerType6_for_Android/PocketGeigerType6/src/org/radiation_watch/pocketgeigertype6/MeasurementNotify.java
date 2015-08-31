package org.radiation_watch.pocketgeigertype6;

public interface MeasurementNotify {

	public void onTimer(double cpm, double cpm_theta, double second, double minute, long noiseCount);

}
