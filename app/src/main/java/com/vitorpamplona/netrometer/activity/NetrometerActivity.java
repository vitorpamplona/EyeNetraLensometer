/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * This program is offered under a commercial and under the AGPL license.
 * For commercial licensing, contact me at vitor@vitorpamplona.com.
 * For AGPL licensing, see below.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * This application has not been clinically tested, approved by or registered in any health agency.
 * Even though this repository grants licenses to use to any person that follow it's license,
 * any clinical or commercial use must additionally follow the laws and regulations of the
 * pertinent jurisdictions. Having a license to use the source code does not imply on having
 * regulatory approvals to use or market any part of this code.
 */
package com.vitorpamplona.netrometer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.activity.fragments.AbstractCalibratingFragment;
import com.vitorpamplona.netrometer.activity.fragments.AbstractMeasuringFragment;
import com.vitorpamplona.netrometer.activity.fragments.AbstractNetrometerFragment;
import com.vitorpamplona.netrometer.activity.fragments.CalibratingFragment;
import com.vitorpamplona.netrometer.activity.fragments.CalibratingSmartStageFragment;
import com.vitorpamplona.netrometer.activity.fragments.DebugFragment;
import com.vitorpamplona.netrometer.activity.fragments.LiveFragment;
import com.vitorpamplona.netrometer.activity.fragments.MeasureBifocalsFragment;
import com.vitorpamplona.netrometer.activity.fragments.MeasureBifocalsSmartStageFragment;
import com.vitorpamplona.netrometer.activity.fragments.MeasureProgressiveFragment;
import com.vitorpamplona.netrometer.activity.fragments.MeasureProgressiveSmartStageFragment;
import com.vitorpamplona.netrometer.activity.fragments.MeasureSingleVisionFragment;
import com.vitorpamplona.netrometer.activity.fragments.MeasureSingleVisionSmartStageFragment;
import com.vitorpamplona.netrometer.activity.fragments.ReadingsFragment;
import com.vitorpamplona.netrometer.activity.fragments.ResultsFragment;
import com.vitorpamplona.netrometer.activity.fragments.ResultsSmartStageFragment;
import com.vitorpamplona.netrometer.activity.fragments.SettingsFragment;
import com.vitorpamplona.netrometer.activity.fragments.StartNoStageFragment;
import com.vitorpamplona.netrometer.activity.fragments.StartWithStageFragment;
import com.vitorpamplona.netrometer.imageprocessing.CameraPreview;
import com.vitorpamplona.netrometer.imageprocessing.CameraPreview.CameraPreviewListener;
import com.vitorpamplona.netrometer.imageprocessing.NetrometerImageProcessing;
import com.vitorpamplona.netrometer.imageprocessing.ProgressiveImageProcessing;
import com.vitorpamplona.netrometer.imageprocessing.SingleVisionImageProcessing;
import com.vitorpamplona.netrometer.model.ExamResults;
import com.vitorpamplona.netrometer.model.db.objects.DebugExam;
import com.vitorpamplona.netrometer.printer.AGPPrinterAPI;
import com.vitorpamplona.netrometer.printer.Printer;
import com.vitorpamplona.netrometer.settings.AppSettings;
import com.vitorpamplona.netrometer.settings.Params;

import org.ndeftools.Message;
import org.ndeftools.Record;
import org.ndeftools.wellknown.TextRecord;

import java.util.List;
import java.util.Set;

public class NetrometerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CODE_WRITE_SETTINGS_PERMISSION = 122;
    protected CameraPreview mCameraPreview;
    private NetrometerImageProcessing mImageProcessor;

    protected ProcessingState mState = ProcessingState.NOT_STARTED;

    private View mNewCustomReading;
    private View mPrinter;
    private View mShowCamera;

    private ImageView mNewCustomReadingButton;
    private ImageView mShowCameraButton;

    ActionBarDrawerToggle mToggle;
    DrawerLayout mDrawer;

    private ImageView mLogo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mLogo = (ImageView) findViewById(R.id.logovertical);

        mNewCustomReading = (View) findViewById(R.id.new_reading);
        mNewCustomReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewReading();
            }
        });
        mNewCustomReading.setVisibility(View.GONE);

        mNewCustomReadingButton = (ImageView) findViewById(R.id.new_reading_button);
        mNewCustomReadingButton.getBackground().setColorFilter(getResources().getColor(R.color.buttonsColor), PorterDuff.Mode.SRC_IN);

        mPrinter = (View) findViewById(R.id.printer);
        mPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printLastResults();
            }
        });

        mShowCamera = (View) findViewById(R.id.showCamera);
        mShowCameraButton = (ImageView) findViewById(R.id.show_camera_button);
        mShowCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCamera();
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(mToggle);
        mToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void toggleCamera() {
        if (getSettings().isCameraPreviewActive()) {
            mShowCameraButton.setBackgroundResource(R.drawable.ic_visibility_off_black_24dp);
            mShowCameraButton.getBackground().setColorFilter(getResources().getColor(R.color.buttonsColor), PorterDuff.Mode.SRC_IN);

            animateToHideCamera();
            getSettings().setCameraPreviewActive(false);

            if (getCurrentFragment() instanceof AbstractMeasuringFragment) {
                ((AbstractMeasuringFragment) getCurrentFragment()).resetTexts();
            }
        } else {
            mShowCameraButton.setBackgroundResource(R.drawable.ic_visibility_black_24dp);
            mShowCameraButton.getBackground().setColorFilter(getResources().getColor(R.color.buttonsColor), PorterDuff.Mode.SRC_IN);

            animateToShowCamera();
            getSettings().setCameraPreviewActive(true);

            if (getCurrentFragment() instanceof AbstractMeasuringFragment) {
                ((AbstractMeasuringFragment) getCurrentFragment()).resetTexts();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Log.i("NFC", "onNewIntent" + getIntent().getAction().toString());
        //Log.i("NFC", "onNewIntent" + getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES));

        String action = intent.getAction();
        if (intent != null && intent.getAction() != null
                && intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            setVersionFromNdefMessage(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent() != null
                && getIntent().getAction() != null
                && getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            setVersionFromNdefMessage(getIntent());
        }

        if (isNotProcessing()) {
            if (hasNoFragmentActive()) {
                loadStartFragment();
            }

            if (isShowingExamFragment()) {
                loadStartFragment();
            }
        }
    }

    private boolean isNotProcessing() {
        return mState == ProcessingState.NOT_STARTED || mState == ProcessingState.PAUSED;
    }

    private void setSettings() {

        // Adjust time out only if it's too small.
        String timeout = Settings.System.getString(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        if (Integer.parseInt(timeout) < 120000)
            Settings.System.putString(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, "120000"); // timeout.

        Settings.System.putString(getContentResolver(), "screen_mode_setting", "1"); // Standard Mode
        Settings.System.putString(getContentResolver(), "screen_mode_automatic_setting", "0"); // Adaptive Display False.
        Settings.System.putString(getContentResolver(), "power_saving_mode", "0");  // Do not adjust screen tone automatically
        // No need to change display birghtness in the lensometer
        //Settings.System.putString(getContentResolver(), "screen_brightness", "255"); // Max Brightness
        //Settings.System.putString(getContentResolver(), "screen_brightness_mode", "1"); // Auto-brightness
        Settings.System.putString(getContentResolver(), "multi_window_enabled", "0"); // Multi-Window false
        Settings.System.putString(getContentResolver(), "auto_adjust_touch", "0");   // Do not adjust touch.
        Settings.System.putString(getContentResolver(), "psm_battery_level", "10");  // Batery level to 10% starts power saving mode .
        Settings.System.putString(getContentResolver(), "smart_pause", "0");         // Deactivate smart Pause
        Settings.System.putString(getContentResolver(), "smart_scroll", "0");        // Deactivate Smart Scrool.
        Settings.System.putString(getContentResolver(), "intelligent_sleep_mode", "0"); // deactivate Smart Stay.
        Settings.System.putString(getContentResolver(), "display_battery_percentage", "1"); // Show battery display.
        Settings.System.putString(getContentResolver(), "e_reading_display_mode", "0");
    }

    private void checkSystemSettings() {
        if (!getApp().isSamsungS4()) return;

        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(this);
        } else {
            permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }

        if (permission) {
            setSettings();
        } else {
            AlertDialog.Builder authDialog = new AlertDialog.Builder(this);
            authDialog.setTitle(R.string.permission_request_title);
            authDialog.setMessage(R.string.permission_request_desc);
            authDialog.setCancelable(true);
            authDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:com.vitorpamplona.netrometer"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        NetrometerActivity.this.startActivityForResult(intent, NetrometerActivity.CODE_WRITE_SETTINGS_PERMISSION);
                    } else {
                        Log.d("Activity", "Permission Request");
                        ActivityCompat.requestPermissions(NetrometerActivity.this, new String[]{Manifest.permission.WRITE_SETTINGS}, NetrometerActivity.CODE_WRITE_SETTINGS_PERMISSION);
                    }
                }
            });
            authDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    System.exit(0);
                }
            });
            authDialog.show();
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NetrometerActivity.CODE_WRITE_SETTINGS_PERMISSION && Settings.System.canWrite(this)){
            Log.d("Activity", "Permission Response");
            setSettings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NetrometerActivity.CODE_WRITE_SETTINGS_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setSettings();
        }
    }

    public boolean isFlashActive() {
        return mCameraPreview.isFlashActive();
    }

    private boolean isShowingExamFragment() {
        Fragment f = getFragmentManager().findFragmentById(R.id.fragment_view);
        return (f instanceof MeasureBifocalsFragment
                || f instanceof MeasureProgressiveFragment
                || f instanceof MeasureSingleVisionFragment // Needs to restart because of camera calibration.
                || f instanceof MeasureBifocalsSmartStageFragment
                || f instanceof MeasureProgressiveSmartStageFragment
                || f instanceof MeasureSingleVisionSmartStageFragment);
    }

    private boolean isHomeFragmentActive() {
        Fragment f = getFragmentManager().findFragmentById(R.id.fragment_view);
        return (f instanceof StartNoStageFragment);
    }

    private boolean isReadingsFragmentActive() {
        Fragment f = getFragmentManager().findFragmentById(R.id.fragment_view);
        return (f instanceof ReadingsFragment);
    }

    private boolean hasNoFragmentActive() {
        Fragment f = getFragmentManager().findFragmentById(R.id.fragment_view);
        return f == null;
    }

    private boolean hasFragmentActive() {
        Fragment f = getFragmentManager().findFragmentById(R.id.fragment_view);
        return f != null;
    }


    public String getGoogleServicesVersion() {
        PackageInfo googleServices = null;
        try {
            googleServices = getPackageManager().getPackageInfo("com.google.android.gms", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (googleServices != null) {
            return googleServices.versionName;
        } else {
            return "Not Installed";
        }
    }

    public void hideNewCustomReadingButton() {
        mNewCustomReading.setVisibility(View.GONE);
    }

    public void showNewCustomReadingButton() {
        mNewCustomReading.setVisibility(View.VISIBLE);
    }

    public void addNewReading() {
        ExamResults e = NetrometerApplication.get().buildNewExamResults();
        DebugExam e1 = new DebugExam(e);
        NetrometerApplication.get().getSqliteHelper().saveDebugExam(e1);

        if (isReadingsFragmentActive()) {
            ((ReadingsFragment) getCurrentFragment()).refreshAddedCard();
            loadResultNoSmartStageFragment(false, e1);
        }
    }


    DebugExam lastResult;
    AGPPrinterAPI api = null;
    Printer hp = null;

    public void enablePrinterIfFound(DebugExam lastResult) {
        this.lastResult = lastResult;
        if (api != null) {
            api.destroy();
        }
        if (hp != null) {
            hp.destroy();
        }

        api = new AGPPrinterAPI(this, new AGPPrinterAPI.TryConnecting() {
            @Override
            public void isConnectable() {
                checkPrinterReady();
            }

            @Override
            public void cannotConnect() {
                checkPrinterReady();
            }
        });


        hp = new Printer(this);
        if (hp.isAvailable()) {
            showPrinterButton();
        }
    }

    public void checkPrinterReady() {
        if (isPrinterReady())
            showPrinterButton();
        else
            hidePrinterButton();
    }

    public void enablePrinterIfFound(AGPPrinterAPI.TryConnecting feedback) {
        if (api != null)
            api.destroy();
        if (hp != null)
            hp.destroy();

        api = new AGPPrinterAPI(this, feedback);
        hp = new Printer(this, feedback);
    }

    public boolean isPrinterReady() {
        return (api != null && api.isPrinterAvailable())
                || (hp != null && hp.isAvailable());
    }

    public void showPrinterButton() {
        mPrinter.setVisibility(View.VISIBLE);
    }

    public void hidePrinterButton() {
        mPrinter.setVisibility(View.GONE);
        if (api != null)
            api.destroy();
        api = null;
        if (hp != null)
            hp.destroy();
        hp = null;
    }

    public void printLastResults() {
        if (this.getWindow() != null && this.getWindow().getCurrentFocus() != null) {
            this.getWindow().getCurrentFocus().clearFocus();
            hideKeyboard();
        }

        if (api != null && api.isPrinterAvailable()) {
            api.print(lastResult);
        } else if (hp != null) {
            hp.print(lastResult);
        }
    }

    public void printResults(DebugExam exam) {
        if (api != null && api.isPrinterAvailable()) {
            api.print(exam);
        } else if (hp != null) {
            hp.print(exam);
        }
    }

    public void hideCameraPreviewButton() {
        mShowCamera.setVisibility(View.GONE);
    }

    public void showCameraPreviewButton() {
        if (getSettings().isCameraPreviewActive()) {
            mShowCameraButton.setBackgroundResource(R.drawable.ic_visibility_black_24dp);
        } else {
            mShowCameraButton.setBackgroundResource(R.drawable.ic_visibility_off_black_24dp);
        }
        mShowCameraButton.getBackground().setColorFilter(getResources().getColor(R.color.buttonsColor), PorterDuff.Mode.SRC_IN);
        mShowCamera.setVisibility(View.VISIBLE);
    }

    public void enableProgress() {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    public void disableProgress() {
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

    public void enableToolbarView() {
        findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
    }

    public void disableToolbarView() {
        findViewById(R.id.toolbar).setVisibility(View.GONE);
    }


    public int toPX(int dp) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }

    public void showMenu() {
        mToggle.setDrawerIndicatorEnabled(true);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mLogo.setPadding(0, 0, toPX(70), 0);
    }

    public void hideMenu() {
        mToggle.setDrawerIndicatorEnabled(false);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mLogo.setPadding(0, 0, toPX(0), 0);
    }

    public void hideKeyboard() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void showKeyboardDontAdjust() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    public void loadStartFragment() {
        finishImageProcessing();
        clearImageProcessing();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        if (getSettings().wasUsingSmartStage()) {
            transaction.replace(R.id.fragment_view, new StartWithStageFragment());
        } else {
            transaction.replace(R.id.fragment_view, new StartNoStageFragment());
        }

        transaction.commit();

        System.gc();
    }

    private void showCPUUsage() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        int i = 0;
        for (Thread t : threadSet) {
            Log.i("Thread", "t" + i++ + " " + t.getState() + " " + t.getName());
        }
    }

    public void loadStartWithoutSmartStageFragment() {
        getSettings().setLastSmartStageChoice(false);

        animateToHideCamera();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.enter_from_left, R.animator.exit_to_right);
        transaction.replace(R.id.fragment_view, new StartNoStageFragment());
        transaction.commit();
    }

    public void loadStartWithSmartStageFragment() {
        getSettings().setLastSmartStageChoice(true);

        animateToHideCamera();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left);
        transaction.replace(R.id.fragment_view, new StartWithStageFragment());
        transaction.commit();
    }

    public void loadLiveFragment() {
        animateToShowCamera();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new LiveFragment());
        transaction.commit();
    }

    public void loadCalibratingNoSmartStage(AbstractCalibratingFragment.LENS_TYPE lensType) {
        animateToHideCamera();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new CalibratingFragment().setLensType(lensType));
        transaction.commit();
    }

    public void loadCalibratingSmartStage(AbstractCalibratingFragment.LENS_TYPE lensType) {
        animateToHideCamera();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new CalibratingSmartStageFragment().setLensType(lensType));
        transaction.commit();
    }

    public void loadResultNoSmartStageFragment(boolean goBackHome, DebugExam exam) {
        animateToHideCamera();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new ResultsFragment().setHomeWhenDone(goBackHome, exam));
        transaction.commit();
    }

    public void loadResultSmartStageFragment(boolean goBackHome, DebugExam exam) {
        animateToHideCamera();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new ResultsSmartStageFragment().setHomeWhenDone(goBackHome, exam));
        transaction.commit();
    }

    public void animateToShowCamera() {
        findViewById(R.id.background).animate().
                alpha(0.0f).setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    public void animateToHideCamera() {
        findViewById(R.id.background).animate().
                alpha(1.0f).setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    public void loadMeasureSingleVisionFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new MeasureSingleVisionFragment());
        transaction.commit();
    }

    public void loadMeasureBifocalsFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new MeasureBifocalsFragment());
        transaction.commit();
    }

    public void loadMeasureProgressiveFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new MeasureProgressiveFragment());
        transaction.commit();
    }

    public void loadMeasureSingleVisionSmartStageFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new MeasureSingleVisionSmartStageFragment());
        transaction.commit();
    }

    public void loadMeasureBifocalsSmartStageFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new MeasureBifocalsSmartStageFragment());
        transaction.commit();
    }

    public void loadMeasureProgressiveSmartStageFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new MeasureProgressiveSmartStageFragment());
        transaction.commit();
    }

    public void loadSettingsFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new SettingsFragment());
        transaction.commit();
    }

    ReadingsFragment r = new ReadingsFragment();

    public void loadReadingsFragment(boolean resetPosition) {
        if (resetPosition) r.backToTop();
        if (getCurrentFragment() != null && getCurrentFragment().getId() == r.getId()) return;

        animateToHideCamera();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, r);
        transaction.commit();
    }


    public void loadDebugFragment() {
        animateToShowCamera();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        transaction.replace(R.id.fragment_view, new DebugFragment());
        transaction.commit();
    }


    public boolean initializeImageProcessing(AbstractCalibratingFragment.LENS_TYPE lensType, boolean isSmartStage) {
        mState = ProcessingState.STARTING;
        setUpCamera();

        if (isSmartStage) {
            switch (lensType) {
                case PROGRESSIVES:
                    mImageProcessor = new ProgressiveImageProcessing(getApp().getDevice());
                    break;
                case BIFOCALS:
                    mImageProcessor = new ProgressiveImageProcessing(getApp().getDevice());
                    break;
                case SINGLE_VISION:
                    mImageProcessor = new ProgressiveImageProcessing(getApp().getDevice());
                    break;
                default:
                    mImageProcessor = new ProgressiveImageProcessing(getApp().getDevice());
                    break;
            }
        } else {
            switch (lensType) {
                case PROGRESSIVES:
                    mImageProcessor = new ProgressiveImageProcessing(getApp().getDevice());
                    break;
                case BIFOCALS:
                    mImageProcessor = new ProgressiveImageProcessing(getApp().getDevice());
                    break;
                case SINGLE_VISION:
                    mImageProcessor = new SingleVisionImageProcessing(getApp().getDevice());
                    break;
                default:
                    mImageProcessor = new SingleVisionImageProcessing(getApp().getDevice());
                    break;
            }
        }

        mState = ProcessingState.STARTED;
        return startProcessing();
    }

    public void finishImageProcessing() {
        stopProcessing();
        mState = ProcessingState.NOT_STARTED;
    }

    public void clearImageProcessing() {
        mCameraPreview = null;
        mImageProcessor = null;
    }

    private AbstractNetrometerFragment getCurrentFragment() {
        return (AbstractNetrometerFragment) getFragmentManager().findFragmentById(R.id.fragment_view);
    }

    private void setUpCamera() {

        mCameraPreview = new CameraPreview(this, Params.PREVIEW_FRAME_WIDTH, Params.PREVIEW_FRAME_HEIGHT);
        mCameraPreview.setSurface((SurfaceView) findViewById(R.id.surface));
        mCameraPreview.setCameraPreviewListener(new CameraPreviewListener() {

            boolean focused;

            @Override
            public void onFrameReceived(byte[] data) {
                if (focused && data != null && mImageProcessor != null) {
                    //Clock c = new Clock("FrameReceived");
                    mImageProcessor.feedFrame(data);
                    //c.capture("NewFrame");
                    //c.log();
                }
            }

            @Override
            public void onCameraLoadFinished() {
                if (mCameraPreview != null)
                    mCameraPreview.focus();
            }

            @Override
            public void onAutoFocusFinished(boolean success) {
                if (success) {
                    focused = true;
                } else {
                    focused = false;
                    if (mCameraPreview != null)
                        mCameraPreview.focus();
                }
            }
        });
    }

    protected void onStart() {
        super.onStart();

        checkSystemSettings();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i("NFC", "onStop");

        stopProcessing();
        finishImageProcessing();
        clearImageProcessing();
    }

    public boolean startProcessing() {
        if (mState != ProcessingState.NOT_STARTED && mState != ProcessingState.STARTING && mState != ProcessingState.RUNNING) {
            Log.i("Restarting Camera", "Start Processing " + mState);

            mCameraPreview.stopCamera();
            mCameraPreview.setSurface((SurfaceView) findViewById(R.id.surface));
            if (mCameraPreview.safeStartCamera()) {
                mState = ProcessingState.RUNNING;
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void cameraBusyAlert() {
        AlertDialog.Builder authDialog = new AlertDialog.Builder(this);
        authDialog.setTitle(R.string.camera_locked_title);
        authDialog.setMessage(R.string.camera_locked_desc);
        authDialog.setCancelable(true);
        authDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        authDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        authDialog.show();
    }

    public void stopProcessing() {
        Log.i("Restarting Camera", "Stop Processing " + mState);
        if (mState != ProcessingState.NOT_STARTED) {
            if (mCameraPreview != null)
                mCameraPreview.stopCamera();
            mState = ProcessingState.PAUSED;
            Log.i("New State", mState.toString());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            getCurrentFragment().onBackPressed();
        }
    }

    private void setVersionFromNdefMessage(Intent intent) {
        long id = AppSettings.SETTING_HARDWARE_ID_DEFAULT;
        Log.i("CameraPreviewNetrometer", "setVersionFromNdefMessage Default "+id);
        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (messages != null) {
            for (int i = 0; i < messages.length; i++) {
                try {
                    List<Record> records = new Message((NdefMessage)messages[i]);

                    for(int k = 0; k < records.size(); k++) {
                        Record record = records.get(k);

                        if (record instanceof TextRecord){
                            String text = ((TextRecord) record).getText();
                            id = Long.valueOf(text.substring(text.indexOf(" ")+1));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    id = AppSettings.SETTING_HARDWARE_ID_DEFAULT;
                }
            }
        }

        if (id > 0) {
            getSettings().setHardwareId(id);
            Log.i("CameraPreviewNetrometer", "Set Hardware ID "+id);
        }
        Log.i("CameraPreviewNetrometer", "setVersionFromNdefMessage "+id);
    }

    public void setVersionFromNdefMessageForUnitTest(long id) {

        if (id > 0)
            getSettings().setHardwareId(id);
        else{
            id = AppSettings.SETTING_HARDWARE_ID_DEFAULT;
            getSettings().setHardwareId(id);
        }
    }

    public AppSettings getSettings() {
        return getApp().getSettings();
    }

    public NetrometerApplication getApp() {
        return (NetrometerApplication) getApplication();
    }

    public NetrometerImageProcessing getImageProcessor() {
        return mImageProcessor;
    }


    public boolean isInvalidNetrometerId(long netID) {
        return netID <= 1 || netID > 2000;
    }

    public boolean isUnknownNetrometer() {
        return isInvalidNetrometerId(getApp().getSettings().getHardwareId());
    }

    public void captureNetrometerIDManually() {
        final Dialog authDialog = new Dialog(this);
        authDialog.setTitle(R.string.capturing_netrometer_ID_title);
        authDialog.setContentView(R.layout.request_netrometer_id);
        authDialog.setCancelable(true);

        final EditText edNetroId = (EditText) authDialog.findViewById(R.id.edNetroId);
        Button btCancel = (Button) authDialog.findViewById(R.id.btCancel);
        Button btSave = (Button) authDialog.findViewById(R.id.btSave);

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authDialog.dismiss(); // dismiss the dialog
            }
        });
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edNetroId.getText().toString().isEmpty()) {
                    long id = Long.parseLong(edNetroId.getText().toString());
                    if (isInvalidNetrometerId(id)) {
                        Toast.makeText(NetrometerActivity.this, "Not a valid serial number. Please make sure it is the right number to get accurate results.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    getSettings().setHardwareId(id);
                }
                authDialog.dismiss();
            }
        });


        authDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        authDialog.show();
    }

    protected enum ProcessingState{
        NOT_STARTED,
        STARTING,
        STARTED,
        RUNNING,
        PAUSED
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            loadStartFragment();
        } else if (id == R.id.nav_readings){
            loadReadingsFragment(true);
        } else if (id == R.id.nav_settings) {
            loadSettingsFragment();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}