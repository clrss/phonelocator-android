/**
 * 
 *  Copyright 2011 Birkett Enterprise Ltd
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

package com.birkettenterprise.phonelocator.activity;

import com.birkettenterprise.phonelocator.R;
import com.birkettenterprise.phonelocator.settings.SettingsManager;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SettingsActivity extends PreferenceActivity {
	
	private SettingsManager mSettingsManager;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	protected void onResume() {
		super.onResume();
		mSettingsManager = SettingsManager.getInstance(this, this);
		
		// If the shared preferences are updated elsewhere, i.e. from the network. The view does not automatically refresh to reflect the new values.
		// as a temporary work around, we add the settings every time the activity is resumed and remove them when it is destroyed
		addPreferencesFromResource(R.xml.preferences);
		
		 Preference passcodePreference = findPreference("passcode");
		 passcodePreference.getOnPreferenceClickListener();
		passcodePreference.setOnPreferenceClickListener(mPreferencesClickListener);
		    
	}
	
	protected void onPause() {
		super.onPause();
		mSettingsManager.releaseInstance(this);
		mSettingsManager = null;
		getPreferenceScreen().removeAll();
	}
	
	public void setPreferenceScreen (PreferenceScreen preferenceScreen) {
		super.setPreferenceScreen(preferenceScreen);
	}
	
	public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen, Preference preference) {
		return false;
		//return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	public OnPreferenceClickListener mPreferencesClickListener = new OnPreferenceClickListener() {

	    public boolean onPreferenceClick(Preference pref) {


	        return true;
	    }

	};

}
