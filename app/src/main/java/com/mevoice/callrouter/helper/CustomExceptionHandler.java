package com.mevoice.callrouter.helper;

import android.content.Context;

import com.mevoice.callrouter.CRApp;
import com.mevoice.callrouter.R;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Admin on 9/9/2016.
 */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    Context mContext;
    private Thread.UncaughtExceptionHandler defaultUEH;
    public CustomExceptionHandler(Context context) {
        this.mContext = context;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        //String fileName = Utils.saveStringToTempFile(".stacktrace", stacktrace);
        int buildNumber = Utils.getBuildNumber();

        Utils.startSendEmailWithAttachment(mContext,
                new String[] {CRApp.context.getString(R.string.literal_stacktrace_email_recipient)},
                String.format(CRApp.context.getString(R.string.literal_stacktrace_email_subject), CRApp.context.getString(R.string.app_name), buildNumber),
                stacktrace, null,
                CRApp.context.getString(R.string.message_stacktrace_chooser_title));
        defaultUEH.uncaughtException(t, e);
    }
}

