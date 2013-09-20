/* Copyright (c) 2013 NITLab, University of Thessaly, Greece
 * This software may be used and distributed solely under the terms of the MIT license (License).
 * You should find a copy of the License in LICENSE.TXT or at http://opensource.org/licenses/MIT.
 * By downloading or using this software you accept the terms and the liability disclaimer in the License.
*/

package com.omf.resourcecontroller;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SettingsActivity extends Activity{
	public static final String TAG = "SettingsActivity";
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
	}
	
	
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG,"onDestroy");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG,"onResume");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG,"onStart");
	}
}
