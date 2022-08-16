package com.example.watchApp.pizzawatchface.work;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NetWorkCheckWorker extends Worker {
    public NetWorkCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        Log.d(TAG , ">>>>>>>>>>>>> NetWorkCheckWorker  <<<<<<<<<<<");
    }

    @Override
    public Result doWork() {
        Log.d(TAG , ">>>>>>>>>>>>> NetWorkCheckWorker dowork <<<<<<<<<<<");
        // Do the work here--in this case, upload the images.
        uploadImages();

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    private void uploadImages() {
        Log.d(TAG , ">>>>>>>>>>>>> Worker uploadImages method <<<<<<<<<<<");
    }


}