package com.library.callback;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.library.utils.Debug;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
public class FLLocationAjaxCallback extends FLAbstractAjaxCallback<Location, FLLocationAjaxCallback> {

	private LocationManager lm;
	private long timeout = 30000;
	private long interval = 1000;
	private float tolerance = 10;
	private float accuracy = 1000;
	private int iteration = 3;
	private int n = 0;
	private boolean networkEnabled = false;
	private boolean gpsEnabled = false;
	
	//private long expire = 0;
	private Listener networkListener;
	private Listener gpsListener;
	private long initTime;
	
	public FLLocationAjaxCallback(){
		type(Location.class).url("device");
	}
	
	
	@Override
	public void async(Context context){
		lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);		
		gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		work();
	}
	
	public FLLocationAjaxCallback timeout(long timeout){
		this.timeout = timeout;
		return this;
	}
	
	public FLLocationAjaxCallback accuracy(float accuracy){
		this.accuracy = accuracy;
		return this;
	}
	
	public FLLocationAjaxCallback tolerance(float tolerance){
		this.tolerance = tolerance;
		return this;
	}
	
	public FLLocationAjaxCallback iteration(int iteration){
		this.iteration = iteration;
		return this;
	}
	
	private void check(Location loc){
	
		if(loc != null){	
			
			if(isBetter(loc)){

				n++;				
				boolean last = n >= iteration;
				
				boolean accurate = isAccurate(loc);
				boolean diff = isDiff(loc);
				
				boolean best = !gpsEnabled || LocationManager.GPS_PROVIDER.equals(loc.getProvider());
				
				Debug.Log(n+"--"+iteration);
				Debug.Log("acc"+"--"+accurate);
				Debug.Log("best"+"--"+best);
				
				
				if(diff){
					if(last){
						if(accurate && best){
							stop();
							callback(loc);
						}
					}else{
						if(accurate && best){
							stop();
						}
						callback(loc);
					}
					
				}
				
			}
		}
		
	}
	
	private void callback(Location loc){
		result = loc;					
		status(loc, 200);
		callback();
	}
	
	
	
	private void status(Location loc, int code){
		
		if(status == null){
			status = new FLAjaxStatus();
		}
		
		if(loc != null){
			status.time(new Date(loc.getTime()));
		}
		
		status.code(code).done().source(FLAjaxStatus.DEVICE);
		
	}
	
	private boolean isAccurate(Location loc){
		
		return loc.getAccuracy() < accuracy;
		
	}
	
	
	private boolean isDiff(Location loc){

		if(result == null) return true;
		
		float diff = distFrom(result.getLatitude(), result.getLongitude(), loc.getLatitude(), loc.getLongitude());
		
		if(diff < tolerance){
			Debug.Log("duplicate location");
			return false;
		}else{
			return true;
		}
	}
	
	
	private boolean isBetter(Location loc){
		
		if(result == null) return true;
		
		// if this loc is network and there's already an recent async gps update
		if(result.getTime() > initTime && result.getProvider().equals(LocationManager.GPS_PROVIDER) && loc.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
			Debug.Log("inferior location");
			return false;
		}
		
		return true;
		
		
	}
	
	private void failure(){
		
		if(gpsListener == null && networkListener == null) return;

		Debug.Log("fail");
		
		result = null;
		status(null, FLAjaxStatus.TRANSFORM_ERROR);
		stop();
		callback();
	}
	
	public void stop(){

		Debug.Log("stop");
		
		Listener gListener = gpsListener;
		
		if(gListener != null){
			lm.removeUpdates(gListener);
			gListener.cancel();
		}
		
		Listener nListener = networkListener;
		
		if(nListener != null){
			lm.removeUpdates(nListener);
			nListener.cancel();
		}
		
		gpsListener = null;
		networkListener = null;
	}
	
	private void work(){
		
		Location loc = getBestLocation();		
		
		Timer timer = new Timer(false);
		
		if(networkEnabled){
			Debug.Log("register net");
			networkListener = new Listener();
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, interval, 0, networkListener, Looper.getMainLooper()); 
			timer.schedule(networkListener, timeout);
		}
		
		
		if(gpsEnabled){
			Debug.Log("register gps");
			gpsListener = new Listener();
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, gpsListener, Looper.getMainLooper());  
			timer.schedule(gpsListener, timeout);
		}
		
		if(iteration > 1 && loc != null){
			n++;
			callback(loc);
		}
		
		initTime = System.currentTimeMillis();
		
	}
	
	
	private Location getBestLocation(){
		
		Location loc1 = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location loc2 = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);	
		
		if(loc2 == null) return loc1;
		if(loc1 == null) return loc2;
		
		if(loc1.getTime() > loc2.getTime()){
			return loc1;
		}else{
			return loc2;
		}
		
		
	}
	
	private static float distFrom(double lat1, double lng1, double lat2, double lng2) {
	    
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
           Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
           Math.sin(dLng/2) * Math.sin(dLng/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		int meterConversion = 1609;
		return (float) dist * meterConversion;
		 
		 
	}
	
	private class Listener extends TimerTask implements LocationListener {
		
	    public void onLocationChanged(Location location) {

			Debug.Log("changed"+"--"+location);
	      	check(location);
	      	
	      	
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Debug.Log("onStatusChanged");
		}
		
		public void onProviderEnabled(String provider) {
			Debug.Log("onProviderEnabled");
		  	check(getBestLocation());
		  	lm.removeUpdates(this);
		}
		
		public void onProviderDisabled(String provider) {
			Debug.Log("onProviderDisabled");
		}

		@Override
		public void run() {			
			failure();
		}
		
	}
	
	
}
