package com.example.watchApp.pizzawatchface.util;

import android.os.AsyncTask;


public class TaskUtils {

    public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task) {
        execute(task, (P[]) null);
    }

    public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task, P... params) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }
}
