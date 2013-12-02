package com.codekappa.bitcoinLiveWallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class BitcoinWallpaperService extends WallpaperService {
	
	@Override
	public Engine onCreateEngine() {
		// TODO Auto-generated method stub
		return new BitcoinWallPaperServiceEngine();
	}
	
	public void onCreate() {
		super.onCreate();
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
	
	class BitcoinWallPaperServiceEngine extends Engine {
		
	    static final float FRICTION = 0.3f;
	    static final float SPEED = 5;
	    static final int COIN_SIZE = 400;
	    static final int FRAMERATE_IN_MILISEC = 10;
	    
	    Canvas canvas;
		Bitmap coin;
		SensorManager sensorManager;
		
	    float roll;
	    float pitch;
	    float azimuth;
	    
	    Point coinPosition;
	    Point screenSize;
	    
		private final Handler handler = new Handler();
		private final Runnable drawRunner = new Runnable() {
			@Override
			public void run() {
				draw();
			}
		};
		private boolean visible = true;
			BitcoinWallPaperServiceEngine() {
		}
		
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			
			InitalizeVariables();
			
			getApplicationContext();
			sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
			sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
		    
			WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		    Display display = windowManager.getDefaultDisplay();

		    display.getRealSize(screenSize);
		    
			coin = BitmapFactory.decodeResource(getResources(), R.drawable.bitcoin_shadow_png);
			coin = Bitmap.createScaledBitmap(coin, COIN_SIZE, COIN_SIZE, false);
			
			coinPosition.x = 0;
			coinPosition.y = screenSize.y;
		}
		
		public void InitalizeVariables(){
		    screenSize = new Point();
		    coinPosition = new Point();
		}
		
		@Override
		public void onVisibilityChanged(boolean visible) {
			this.visible = visible;
			if(visible) {
				handler.post(drawRunner);
			} 
			else {
				handler.removeCallbacks(drawRunner);
			}
		}
		
		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			this.visible = false;
			handler.removeCallbacks(drawRunner);
		}
		
		
		private SensorEventListener sensorEventListener = new SensorEventListener() {
			public void onSensorChanged(SensorEvent event){
				switch (event.sensor.getType()) {
	            case Sensor.TYPE_ACCELEROMETER:
	     				 azimuth = event.values[0];
	    				 pitch = event.values[1];
	    				 roll = event.values[2];

	                     break;
				}
			}
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		
		void draw() {
			final SurfaceHolder holder = getSurfaceHolder();
			canvas = null;
			UpdateCoinPosition();
			ConstrainCoinToScreenBoundries();
			try
			{
				canvas = holder.lockCanvas();
				canvas.drawColor(Color.WHITE);
				if(canvas != null) {
					canvas.drawBitmap(coin,coinPosition.x , coinPosition.y,null);
				}
			}
			finally
			{
				if(canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
			handler.removeCallbacks(drawRunner);
			if(visible) {
				handler.postDelayed(drawRunner, FRAMERATE_IN_MILISEC);
			}
		}
		
		void UpdateCoinPosition()
		{
			if(azimuth > FRICTION || azimuth < -FRICTION)
			{
				coinPosition.x += -azimuth*SPEED;
			}
			if(pitch > FRICTION || pitch < -FRICTION)
			{
				coinPosition.y += pitch*SPEED;
			}

		}
		
		void ConstrainCoinToScreenBoundries()
		{
			if(coinPosition.x < 0)
			{
				coinPosition.x = 0;
			}
			else if((coinPosition.x+COIN_SIZE) > screenSize.x)
			{
				coinPosition.x = screenSize.x - COIN_SIZE;
			}
			
			if(coinPosition.y < 0)
			{
				coinPosition.y = 0;
			}
			else if((coinPosition.y + COIN_SIZE) > screenSize.y)
			{
				coinPosition.y = screenSize.y - COIN_SIZE;
			}
		}
	}
}
