package com.microsoft.mimicker.appcore;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.microsoft.mimicker.BuildConfig;
import com.microsoft.mimicker.R;
import com.microsoft.mimicker.model.Alarm;
import com.microsoft.mimicker.onboarding.OnboardingToSFragment;
import com.microsoft.mimicker.onboarding.OnboardingTutorialFragment;
import com.microsoft.mimicker.settings.AlarmSettingsFragment;
import com.microsoft.mimicker.utilities.Loggable;
import com.microsoft.mimicker.utilities.Logger;
import com.microsoft.mimicker.utilities.Util;
import com.uservoice.uservoicesdk.UserVoice;

import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.objects.FeedbackUserDataElement;

public class AlarmMainActivity extends AppCompatActivity
        implements AlarmListFragment.AlarmListListener,
        OnboardingTutorialFragment.OnOnboardingTutorialListener,
        OnboardingToSFragment.OnOnboardingToSListener,
        AlarmSettingsFragment.AlarmSettingsListener {

    public final static String SHOULD_ONBOARD = "onboarding";
    public final static String SHOULD_TOS = "show-tos";
    private boolean mEditingAlarm = false;
    private boolean mOboardingStarted = false;
    private SharedPreferences mPreferences = null;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        String packageName = getApplicationContext().getPackageName();
        mPreferences = getSharedPreferences(packageName, MODE_PRIVATE);
        PreferenceManager.setDefaultValues(this, R.xml.pref_global, false);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Logger.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String hockeyappToken = Util.getToken(this, "hockeyapp");
        if (!BuildConfig.DEBUG)
            UpdateManager.register(this, hockeyappToken);
        Util.registerCrashReport(this);

        if (mPreferences.getBoolean(SHOULD_ONBOARD, true)) {
            if (!mOboardingStarted) {
                mOboardingStarted = true;

                Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ONBOARDING);
                Logger.track(userAction);

                showTutorial(null);
            }
        }
        else if (mPreferences.getBoolean(SHOULD_TOS, true)) {
            showToS();
        }
        else if (!mEditingAlarm) {
            showAlarmList(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        UpdateManager.unregister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FeedbackManager.unregister();
        Logger.flush();
    }

    public void showFeedback(MenuItem item){
        final String hockeyappToken = Util.getToken(this, "hockeyapp");
        FeedbackManager.register(this, hockeyappToken, null);
        FeedbackManager.setRequireUserEmail(FeedbackUserDataElement.OPTIONAL);
        FeedbackManager.showFeedbackActivity(this);
    }

    //
    // Launch User Voice forum form to allow user feedback submission
    //
    public void showUserVoiceFeedback(MenuItem item){
        UserVoice.launchUserVoice(this);
    }

    public void showTutorial(MenuItem item){
        showFragment(new OnboardingTutorialFragment());
    }

    @Override
    public void onSkip() {
        if (mPreferences.getBoolean(SHOULD_TOS, true)) {
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_ONBOARDING_SKIP);
            Logger.track(userAction);
            showToS();
        }
        else {
            showAlarmList(false);
        }
    }

    @Override
    public void onAccept() {
        showAlarmList(false);
    }

    @Override
    public void onBackPressed() {
        if (mEditingAlarm) {
            AlarmSettingsFragment fragment = ((AlarmSettingsFragment)getSupportFragmentManager()
                    .findFragmentByTag(AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG));
            if (fragment != null) {
                fragment.onCancel();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
        } else {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    public void showToS() {
        mPreferences.edit().putBoolean(SHOULD_ONBOARD, false).apply();
        showFragment(new OnboardingToSFragment());
    }

    public void showAlarmList(boolean animateEntrance) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animateEntrance) {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        transaction.replace(R.id.fragment_container, new AlarmListFragment(),
                AlarmListFragment.ALARM_LIST_FRAGMENT_TAG);
        transaction.commit();
        setTitle(R.string.alarm_list_title);
    }

    @Override
    public void onSettingsSaveOrIgnoreChanges() {
        showAlarmList(true);
        mEditingAlarm = false;
    }

    @Override
    public void onSettingsDeleteOrNewCancel() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, R.anim.slide_down);
        transaction.replace(R.id.fragment_container, new AlarmListFragment());
        transaction.commit();
        setTitle(R.string.alarm_list_title);
    }

    @Override
    public void onAlarmSelected(Alarm alarm) {
        showAlarmSettingsFragment(alarm.getId().toString());
        mEditingAlarm = true;
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void showAlarmSettingsFragment(String alarmId) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.fragment_container,
                            AlarmSettingsFragment.newInstance(alarmId),
                            AlarmSettingsFragment.SETTINGS_FRAGMENT_TAG);
        transaction.commit();
    }
}