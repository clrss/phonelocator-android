/**
 * 
 *  Copyright 2011, 2012 Birkett Enterprise Ltd
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

package com.birkettenterprise.phonelocator.broadcastreceiver;

import com.birkettenterprise.phonelocator.settings.SettingsHelper;
import com.birkettenterprise.phonelocator.utility.UpdateUtility;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class PollLocationAndSendUpdateBroadcastReceiver extends BroadcastReceiver {
	
	public static final String ACTION = "com.birkettenterprise.phonelocator.POLL_LOCATION_AND_SEND_UPDATE";
	@Override
	public void onReceive(Context context, Intent i) {	
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SettingsHelper.setLastUpdateTimeStamp(preferences, System.currentTimeMillis());
		UpdateUtility.pollLocationAndSendUpdate(context);
	}
	
}