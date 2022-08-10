package com.example.watchApp.pizzawatchface;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.watchApp.pizzawatchface.databinding.ActivityEmergencyBinding;
import com.example.watchApp.pizzawatchface.mqtt.MqttActivity;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.concurrent.atomic.AtomicInteger;

public class EmergencyActivity extends Activity  {
    private TextView mTextView, countText;
    private ImageButton mSosicon;
    private CountDownTimer countDownTimer = null;

    private ActivityEmergencyBinding binding;
    private GoogleApiClient googleApiClient;
    private boolean wearableConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEmergencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.text;
        mSosicon = binding.sosIcon;
        countText = binding.countText;

        mTextView.setText("SOS 호출 실행");
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.shake);
        anim.setRepeatCount(Animation.INFINITE);
        final AtomicInteger atomicCount = new AtomicInteger(0);

     /*   mSosicon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    v.setBackgroundResource(R.drawable.btn_friendalarm_main_alarm_on);
                    v.startAnimation(anim);
                    int count =3;

                    atomicCount.set(count + 1);
                    countText.setText(String.valueOf(atomicCount.get()));
//                    countText.animate().translationX(mSosicon.getHeight());
                    countText.setVisibility(View.VISIBLE);
                    vibrator.vibrate(500);
                    countDownTimer = new CountDownTimer(count * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            vibrator.vibrate(50);
                            countText.setText(String.valueOf(atomicCount.decrementAndGet()));
                        }

                        @Override
                        public void onFinish() {
                            System.out.println(">>>>>>>>>>>>>>>>>>>> SOS startup!!! ");
//                            //TODO: 알람을 보낼 사람이 없을경우 알람 실행을 막는다
//                            if (DatabaseHandleUtils.getDBLogoutAndDeleteUser(mainActivity) != 0) {
//                                LocalBroadcastManager.getInstance(mainActivity).sendBroadcast(new Intent(UserMainService.ACTION_EXECUTE_EMERGENCY_CALL));
//                                cancelEmergencyCallBtn.setVisibility(View.VISIBLE);
//                            } else {
//                                UiUtils.showDialog(mainActivity, getString(R.string.src_s_um_failed_no_destination), getString(R.string.src_s_um_failed_no_destination_message));
//                            }
                            vibrator.vibrate(300);
                            countText.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "SOS 호출이 실행되었습니다", Toast.LENGTH_SHORT).show();
                        }
                    };
                    countDownTimer.start();

                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//                    if (cancelEmergencyCallBtn.getVisibility() == View.GONE) {
//                        v.setBackgroundResource(R.drawable.btn_friendalarm_main_alarm);
//                    }
                    v.clearAnimation();
                    countText.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }

                return false;
            }
        });
*/

        mSosicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EmergencyActivity.this, MqttActivity.class);
                startActivity(i);
            }
        });


        if (!hasGps()) {
            Log.d("MyWatch", ">>>>>>>>> This hardware doesn't have GPS.");
            // Fall back to functionality that does not use location or
            // warn the user that location function is not available.
        }else
            Log.d("MyWatch", ">>>>>>>>> This hardware have GPS.");

    }

    private boolean hasGps() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    // 바로 워피페이스 설정화면으로 이동한다
    public static void pickYourWatchFace(Context context) {
        ComponentName yourWatchFace = new ComponentName(context, MyWatchFace.class);
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, yourWatchFace)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


}