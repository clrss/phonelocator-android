/**
 * 
 *  Copyright 2011-2012 Birkett Enterprise Ltd
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 */

package com.birkettenterprise.phonelocator.service;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import com.birkettenterprise.phonelocator.protocol.RegistrationResponse;
import com.birkettenterprise.phonelocator.protocol.Session;
import com.birkettenterprise.phonelocator.settings.DefaultSettingsSetter;
import com.birkettenterprise.phonelocator.settings.EnvironmentalSettingsSetter;
import com.birkettenterprise.phonelocator.settings.Setting;
import com.birkettenterprise.phonelocator.settings.SettingSynchronizationHelper;
import com.birkettenterprise.phonelocator.settings.SettingsHelper;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class RegistrationService extends Service {

    private final IBinder mBinder = new RegistrationServiceBinder();
    
    private Handler mHandler;
    private Session mSession;
    private Throwable mException;
    private Vector<Runnable> mObservers;
    private RegistrationResponse mRegistrationResponse;
    
    private RegisrationRunnable mRegisrationRunnable;
    private SynchronizeRunnable mSynchronizeRunnable;
    private Thread mWorkerThread;
    
    private static final String LOG_TAG = "REGISTATION_SERVICE";
    
    private static void synchronizeSettings(Session session, SharedPreferences sharedPreferences) throws IOException {
		Vector<Setting> settings = session.synchronizeSettings(SettingSynchronizationHelper.getSettingsModifiedSinceLastSyncrhonization(sharedPreferences));
		SettingSynchronizationHelper.setSettings(sharedPreferences, settings);
		SettingSynchronizationHelper.updateSettingsSynchronizationTimestamp(sharedPreferences);
    }
    
    private class SynchronizeRunnable implements Runnable {

		public void run() {
		
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RegistrationService.this);
			
			try {
				mSession.connect();
				mSession.authenticate(SettingsHelper.getAuthenticationToken(sharedPreferences));		
				synchronizeSettings(mSession, sharedPreferences);
			} catch (Throwable e) {
				mException = e;
			} finally {
				mSession.close();
			}
			
			synchronized(this) {
				mWorkerThread = null;
			}
			
			updateObservers();
		}
    	
    }
    
    private class RegisrationRunnable implements Runnable {

		public void run() {
			try {
				
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RegistrationService.this);
				DefaultSettingsSetter.setDefaultSettings(sharedPreferences, RegistrationService.this);			
				EnvironmentalSettingsSetter.updateEnvironmentalSettingsIfRequired(sharedPreferences, RegistrationService.this);			
				SettingSynchronizationHelper.resetSettingsSynchronizationTimestamp(sharedPreferences);
				mSession.connect();
				mRegistrationResponse = mSession.register();
				mSession.authenticate(mRegistrationResponse.getAuthenticationToken());		
				SettingsHelper.storeResponse(sharedPreferences, mRegistrationResponse.getAuthenticationToken(), mRegistrationResponse.getRegistrationUrl());
				Log.d(LOG_TAG, "storing authentication token "+ mRegistrationResponse.getAuthenticationToken());
				Log.d(LOG_TAG, "storing registration url "+ mRegistrationResponse.getRegistrationUrl());

				synchronizeSettings(mSession, sharedPreferences);
				
			} catch (Throwable e) {
				mException = e;
			} finally {
				mSession.close();
			}
			
			synchronized(this) {
				mWorkerThread = null;
			}
			
			updateObservers();

		}
    	
    }
    
	public class RegistrationServiceBinder extends Binder {
        public RegistrationService getService() {
            return RegistrationService.this;
        }
    }
	
	private void updateObservers() {
		synchronized (mObservers) {
			Iterator<Runnable> iterator = mObservers.iterator();
			while (iterator.hasNext()) {
				Runnable nextRunnable = iterator.next();
				mHandler.post(nextRunnable);
			}
		}

	}

    @Override
    public void onCreate() {
		//android.os.Debug.waitForDebugger();
    	super.onCreate();
    	mHandler = new Handler();
    	mSession = new Session();
    	mObservers = new Vector<Runnable>();
    	mRegisrationRunnable = new RegisrationRunnable();
    	mSynchronizeRunnable = new SynchronizeRunnable();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    public void register() {
    	startRunnable(mRegisrationRunnable);
    }
    
    public void synchronize() {
    	startRunnable(mSynchronizeRunnable);
    }
    
    private void startRunnable(Runnable runnable) {
    	if (mWorkerThread != null) {
    		throw new RuntimeException();
    	}
    	clearResponse();
    	mWorkerThread = new Thread(runnable);
    	mWorkerThread.start();
    }
    
    public void addObserver(Runnable observer) {
    	synchronized (mObservers) {
    		mObservers.remove(observer);
    		mObservers.add(observer);
    	}
    }
    
    public void clearResponse() {
    	mRegistrationResponse = null;
    	mException = null;
    }
    
    public void removeObserver(Runnable observer) {
    	synchronized (mObservers) {
    		mObservers.remove(observer);
    	}
    }
    
    public boolean isSuccess() {
    	return mRegistrationResponse != null && mException == null;
    }
    
    public RegistrationResponse getResponse() {
    	return mRegistrationResponse;
    }
    
    public Throwable getException() {
    	return mException;
    }
    
    public boolean isErrorOccured() {
    	return mException != null;
    }
    
    public boolean isRunning() {
    	synchronized(this) {
    	return mWorkerThread != null;
    	}
    }
}
