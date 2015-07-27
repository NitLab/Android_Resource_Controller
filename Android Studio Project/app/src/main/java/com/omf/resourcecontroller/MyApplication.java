package com.omf.resourcecontroller;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import android.app.Application;

@ReportsCrashes(
	    formUri = "https://nitlab.cloudant.com/acra-omf-rc/_design/acra-storage/_update/report",
	    reportType = HttpSender.Type.JSON,
	    httpMethod = HttpSender.Method.POST,
	    formUriBasicAuthLogin = "ersturnowndeaftedidedson",
	    formUriBasicAuthPassword = "lHNr4CGPCnUApcYAwv35pAnk",
	    formKey = "", // This is required for backward compatibility but not used
	    customReportContent = {
	            ReportField.APP_VERSION_CODE,
	            ReportField.APP_VERSION_NAME,
	            ReportField.ANDROID_VERSION,
	            ReportField.PACKAGE_NAME,
	            ReportField.REPORT_ID,
	            ReportField.BUILD,
	            ReportField.STACK_TRACE
	    },
	    mode = ReportingInteractionMode.TOAST,
	    resToastText = R.string.toast_crash
	)

	public class MyApplication extends Application {

	    @Override
	    public void onCreate() {
	        super.onCreate();
	        // The following line triggers the initialization of ACRA
	        ACRA.init(this);
	    }
	}
