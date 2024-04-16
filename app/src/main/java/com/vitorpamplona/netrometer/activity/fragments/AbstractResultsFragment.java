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
package com.vitorpamplona.netrometer.activity.fragments;

import android.app.Dialog;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.TypeFaceProvider;
import com.vitorpamplona.netrometer.activity.views.DatePickerFragment;
import com.vitorpamplona.netrometer.imageprocessing.NetrometerImageProcessing;
import com.vitorpamplona.netrometer.model.ExamResults;
import com.vitorpamplona.netrometer.model.Prescription;
import com.vitorpamplona.netrometer.model.RefractionType;
import com.vitorpamplona.netrometer.model.db.SQLiteHelper;
import com.vitorpamplona.netrometer.model.db.objects.DebugExam;
import com.vitorpamplona.netrometer.model.db.objects.Refraction;
import com.vitorpamplona.netrometer.utils.AcuityFormatter;
import com.vitorpamplona.netrometer.utils.AgeCalculator;
import com.vitorpamplona.netrometer.utils.ExportLastReading;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;

public abstract class AbstractResultsFragment extends AbstractNetrometerFragment implements DatePickerFragment.DateInputIO {

    private TextView mLeftSphere, mLeftCyl, mLeftAxis, mLeftAcuity, mLeftAdd;
    private TextView mRightSphere, mRightCyl, mRightAxis, mRightAcuity, mRightAdd;
    private TextView mPD, mPDOld;

    private TextView mLeftSphereOld, mLeftCylOld, mLeftAxisOld, mLeftAcuityOld, mLeftAddOld;
    private TextView mRightSphereOld, mRightCylOld, mRightAxisOld, mRightAcuityOld, mRightAddOld;

    private TextView mTxLeft, mTxRight, mTxCyl, mTxCylChange, mTxSph,mTxSphChange;
    private RelativeLayout mRlCyl, mRlSph;

    private EditText mEdNote, mEdAge, mEdEmail;

    private IntlPhoneInput mPhoneInputView;
    private EditText mPhoneEditText;


    private TextView mInstructions1;
    private Button mNextButton;

    DecimalFormat sphereCylFormatter = new DecimalFormat("+0.00;-0.00");
    DecimalFormat axisFormatter = new DecimalFormat("0°;0°");

    private DecimalFormat pdFormatter = new DecimalFormat("0mm");

    private boolean goBackHome;
    private DebugExam mFinalResults;

    TextWatcher mEdNoteListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (mFinalResults != null) {
                mFinalResults.setStudyName(editable.toString());
            }
        }
    };

    TextWatcher mEdEmailListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (mFinalResults != null) {
                mFinalResults.setPrescriptionEmail(editable.toString());
                mEdEmail.setError(null);
            }
        }
    };


    public AbstractResultsFragment() {
        goBackHome =  true;
    }
    public AbstractResultsFragment setHomeWhenDone(boolean goBackHome, DebugExam exam) {
        this.goBackHome = goBackHome;
        this.mFinalResults = exam;
        return this;
    }

    public abstract int getLayout();
    public abstract boolean isSmartStage();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getNetActivity() != null) {
                    if (getNetActivity().getCurrentFocus() != null)
                        getNetActivity().getCurrentFocus().clearFocus();
                    getNetActivity().hideKeyboard();
                }
            }
        });

        mEdNote = (EditText) view.findViewById(R.id.edPatient);
        mEdAge = (EditText) view.findViewById(R.id.edAge);
        mEdAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setCallbacks(AbstractResultsFragment.this);
                newFragment.show(getNetActivity().getFragmentManager(), "dobPicker");
            }
        });

        mEdEmail = (EditText) view.findViewById(R.id.edEmail);

        mPhoneInputView = (IntlPhoneInput) view.findViewById(R.id.my_phone_input);
        mPhoneInputView.setEmptyDefault(NetrometerApplication.get().getSettings().getLastFlag());

        mPhoneEditText = (EditText) mPhoneInputView.findViewById(net.rimoto.intlphoneinput.R.id.intl_phone_edit__phone);
        mPhoneEditText.setGravity(Gravity.CENTER_HORIZONTAL);
        //mPhoneEditText.setTextColor(getResources().getColorStateList(R.drawable.edit_text_selector));
        mPhoneEditText.setPadding(mPhoneEditText.getPaddingLeft(), 1, mPhoneEditText.getPaddingRight(), mPhoneEditText.getPaddingBottom());
        mPhoneEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mPhoneEditText.setHint("Phone");


        mLeftSphere = (TextView) view.findViewById(R.id.left_sphere);
        mLeftCyl = (TextView) view.findViewById(R.id.left_cyl);
        mLeftAxis = (TextView) view.findViewById(R.id.left_axis);
        mLeftAdd = (TextView) view.findViewById(R.id.left_add);
        mLeftAcuity = (TextView) view.findViewById(R.id.left_acuity);
        mPD = (TextView) view.findViewById(R.id.pd);

        mLeftSphereOld = (TextView) view.findViewById(R.id.left_sphere_old);
        mLeftCylOld = (TextView) view.findViewById(R.id.left_cyl_old);
        mLeftAxisOld = (TextView) view.findViewById(R.id.left_axis_old);
        mLeftAddOld = (TextView) view.findViewById(R.id.left_add_old);
        mLeftAcuityOld = (TextView) view.findViewById(R.id.left_acuity_old);
        mPDOld = (TextView) view.findViewById(R.id.pd_old);

        mRightSphere = (TextView) view.findViewById(R.id.right_sphere);
        mRightCyl = (TextView) view.findViewById(R.id.right_cyl);
        mRightAxis = (TextView) view.findViewById(R.id.right_axis);
        mRightAdd = (TextView) view.findViewById(R.id.right_add);
        mRightAcuity = (TextView) view.findViewById(R.id.right_acuity);

        mRightSphereOld = (TextView) view.findViewById(R.id.right_sphere_old);
        mRightCylOld = (TextView) view.findViewById(R.id.right_cyl_old);
        mRightAxisOld = (TextView) view.findViewById(R.id.right_axis_old);
        mRightAddOld = (TextView) view.findViewById(R.id.right_add_old);
        mRightAcuityOld = (TextView) view.findViewById(R.id.right_acuity_old);


        mRightSphereOld.setPaintFlags(mRightSphereOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mRightCylOld.setPaintFlags(mRightCylOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mRightAxisOld.setPaintFlags(mRightAxisOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mRightAcuityOld.setPaintFlags(mRightAcuityOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mLeftSphereOld.setPaintFlags(mLeftSphereOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mLeftCylOld.setPaintFlags(mLeftCylOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mLeftAxisOld.setPaintFlags(mLeftAxisOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mLeftAcuityOld.setPaintFlags(mLeftAcuityOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);


        mInstructions1 = (TextView) view.findViewById(R.id.instructions_1);
        mNextButton = (Button) view.findViewById(R.id.next_button);

        mTxLeft = (TextView) view.findViewById(R.id.txLeft);
        mTxRight = (TextView) view.findViewById(R.id.txRight);

        mTxCyl = (TextView) view.findViewById(R.id.txCyl);
        mTxCyl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNetActivity().getApp().getSettings().toggleCylModel();
                loadViews();
            }
        });

        mTxCylChange = (TextView) view.findViewById(R.id.txCylChange);
        mTxCylChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNetActivity().getApp().getSettings().toggleCylModel();
                loadViews();
            }
        });

        mTxSph = (TextView) view.findViewById(R.id.txSph);
        mTxSph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNetActivity().getApp().getSettings().toggleSpherocylindricalMode();
                loadViews();
            }
        });

        mTxSphChange = (TextView) view.findViewById(R.id.txSphChange);
        mTxSphChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNetActivity().getApp().getSettings().toggleSpherocylindricalMode();
                loadViews();
            }
        });


        mEdNote.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        textView.clearFocus();
                        getNetActivity().hideKeyboard();
                        break;
                    case EditorInfo.IME_ACTION_NEXT:
                        textView.clearFocus();
                        getNetActivity().hideKeyboard();
                        break;
                }

                if (mFinalResults != null) {
                    mFinalResults.setStudyName(mEdNote.getText().toString());
                    return true;
                }
                return false;
            }
        });
        mEdNote.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) getNetActivity().showKeyboardDontAdjust();
                else getNetActivity().hideKeyboard();
            }
        });

        mEdAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setCallbacks(AbstractResultsFragment.this);
                newFragment.show(getNetActivity().getFragmentManager(), "dobPicker");
            }
        });

        mEdEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        textView.clearFocus();
                        getNetActivity().hideKeyboard();
                        break;
                    case EditorInfo.IME_ACTION_NEXT:
                        textView.clearFocus();
                        getNetActivity().hideKeyboard();
                        break;
                }

                if (mFinalResults != null) {
                    mFinalResults.setPrescriptionEmail(mEdEmail.getText().toString());
                    mEdEmail.setError(null);
                    return true;
                }
                return false;
            }
        });

        mPhoneEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        textView.clearFocus();
                        getNetActivity().hideKeyboard();
                        break;
                    case EditorInfo.IME_ACTION_NEXT:
                        textView.clearFocus();
                        getNetActivity().hideKeyboard();
                        break;
                }

                if (mFinalResults != null && mPhoneInputView.isValid()) {
                    mFinalResults.setPrescriptionPhone( mPhoneInputView.getNumber());
                    NetrometerApplication.get().getSettings().setLastFlag(mPhoneInputView.getSelectedCountry().getIso());
                    mPhoneEditText.setError(null);
                    return true;
                } else {
                    mPhoneEditText.setError("Invalid phone format for this country");
                }
                return false;
            }
        });

        mPhoneInputView.setOnValidityChange(new IntlPhoneInput.IntlPhoneInputListener() {
            @Override
            public void done(View view, boolean isValid) {
                if (isValid && mFinalResults != null) {
                    mFinalResults.setPrescriptionPhone( mPhoneInputView.getNumber());
                    NetrometerApplication.get().getSettings().setLastFlag(mPhoneInputView.getSelectedCountry().getIso());
                    mPhoneEditText.setError(null);
                } else {
                    mPhoneEditText.setError("Invalid phone format for this country");
                }
            }
        });

        mEdEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) getNetActivity().showKeyboardDontAdjust();
                else getNetActivity().hideKeyboard();
            }
        });


        mRlCyl = (RelativeLayout) view.findViewById(R.id.rlCyl);
        mRlSph = (RelativeLayout) view.findViewById(R.id.rlSph);


        mTxRight.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        mTxLeft.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextPressed();
            }
        });

        loadViews();

        if (mFinalResults == null && getImageProcessor() != null)
            new ComputeAndLoadResultsTwoLens().execute(getImageProcessor());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getNetActivity().showMenu();
        getNetActivity().hideNewCustomReadingButton();
        getNetActivity().hideCameraPreviewButton();
        getNetActivity().animateToHideCamera();
        getNetActivity().enableToolbarView();

        if (mFinalResults != null)
            getNetActivity().enablePrinterIfFound(mFinalResults);
    }


    public String[] getArrayWithSteps (float iMinValue, float iMaxValue, float iStep, NumberFormat formatter) {
        int iStepsArray = Math.abs((int) ((iMaxValue - iMinValue) / iStep + 1)); //get the lenght array that will return
        String[] arrayValues= new String[iStepsArray]; //Create array with length of iStepsArray
        for(int i = 0; i < iStepsArray; i++) {
            arrayValues[i] = String.valueOf(formatter.format(iMinValue + (i*iStep)));
        }
        return arrayValues;
    }

    public void showRefractionChangeDialog(final View view, float min, float max, float step, final NumberFormat formatter) {
        // do not show the changing power screens if the results are not here yet.
        if (mFinalResults == null) return;

        final Dialog d = new Dialog(this.getNetActivity());
        d.setTitle(R.string.adjust_refraction);
        d.setContentView(R.layout.number_dialog);
        d.setCancelable(true);

        Button btCancel = (Button) d.findViewById(R.id.btCancel);
        Button btSave = (Button) d.findViewById(R.id.btSave);

        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);
        prepareNumberPicker(np, min, max, step, formatter, ((TextView)view).getText().toString());

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveNewValue(view, formatter.parse(np.getDisplayedValues()[np.getValue()]).floatValue(), formatter);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                d.dismiss();
            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();
    }

    public void showAcuityDialog(final View view, AcuityFormatter.ACUITY_TYPE type, int title) {
        final Dialog d = new Dialog(this.getNetActivity());
        d.setTitle(title);
        d.setContentView(R.layout.number_dialog);
        d.setCancelable(true);

        Button btCancel = (Button) d.findViewById(R.id.btCancel);
        Button btSave = (Button) d.findViewById(R.id.btSave);

        final NumberPicker npDen = (NumberPicker) d.findViewById(R.id.numberPicker);
        prepareNumberPicker(npDen, type, ((TextView) view).getText().toString());

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AcuityFormatter formatter = new AcuityFormatter();
                saveNewAcuityValue(view, formatter.parse(npDen.getDisplayedValues()[npDen.getValue()]), null);
                d.dismiss();
            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();
    }

    public NumberPicker prepareNumberPicker( NumberPicker np, AcuityFormatter.ACUITY_TYPE type, String defaultValue ) {
        int[] values;
        if (type == AcuityFormatter.ACUITY_TYPE.IMPERIAL) {
            values = AcuityFormatter.snellenChartImperial;
        } else {
            values = AcuityFormatter.snellenChartMetric;
        }
        AcuityFormatter formatter = new AcuityFormatter();

        String[] valuesStr = new String[values.length+1];
        valuesStr[0] = "--/--";
        for (int i = 0; i<values.length; i++) {
            valuesStr[i+1] = formatter.getNominator(type) +"/"+ String.valueOf(values[i]);
        }
        np.setMaxValue(values.length - 1); // max value 100
        np.setMinValue(0);   // min value 0
        np.setDisplayedValues(valuesStr);

        if ("--/--".equals(defaultValue)) {
            int index = 0;
            np.setValue(index);
        } else {
            for (int i = 0; i<valuesStr.length; i++) {
                if (valuesStr[i].equals(defaultValue)) {
                    np.setValue(i);
                }
            }
        }

        return np;
    }

    public NumberPicker prepareNumberPicker(NumberPicker np, float min, float max, float step, final NumberFormat formatter, String defaultValue) {
        final String[] values = getArrayWithSteps(min, max, step, formatter);
        np.setMaxValue(values.length - 1); // max value 100
        np.setMinValue(0);   // min value 0
        np.setDisplayedValues(values);
        try {
            if ("-.--".equals(defaultValue) || "---".equals(defaultValue)) {
                int index = (int) ((0 - min) / step);
                np.setValue(index);
            } else {
                float value = formatter.parse(defaultValue).floatValue();
                int index = (int) ((value - min) / step);
                np.setValue(index);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return np;
    }

    public void showEyeChangeDialog(final View view) {
        final Dialog d = new Dialog(this.getNetActivity());
        d.setTitle(R.string.adjust_refraction);
        d.setContentView(R.layout.refraction_dialog);
        d.setCancelable(true);

        Button btCancel = (Button) d.findViewById(R.id.btCancel);
        Button btSave = (Button) d.findViewById(R.id.btSave);

        final NumberPicker npSph = (NumberPicker) d.findViewById(R.id.numberPickerSph);
        final NumberPicker npCyl = (NumberPicker) d.findViewById(R.id.numberPickerCyl);
        final NumberPicker npAxis = (NumberPicker) d.findViewById(R.id.numberPickerAxis);
        final NumberPicker npAdd = (NumberPicker) d.findViewById(R.id.numberPickerAdd);

        if (view.getId() == R.id.txRight) {
            prepareNumberPicker(npSph, 20, -20, -0.25f, sphereCylFormatter, mRightSphere.getText().toString());
            prepareNumberPicker(npCyl, 0, -15, -0.25f, sphereCylFormatter, mRightCyl.getText().toString());
            prepareNumberPicker(npAxis, 1, 180, 1, axisFormatter, mRightAxis.getText().toString());
            prepareNumberPicker(npAdd,0, 4, 0.25f, sphereCylFormatter, mRightAdd.getText().toString());

            btSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        saveNewValue(mRightSphere, sphereCylFormatter.parse(npSph.getDisplayedValues()[npSph.getValue()]).floatValue(), sphereCylFormatter);
                        saveNewValue(mRightCyl, sphereCylFormatter.parse(npCyl.getDisplayedValues()[npCyl.getValue()]).floatValue(), sphereCylFormatter);
                        saveNewValue(mRightAxis, axisFormatter.parse(npAxis.getDisplayedValues()[npAxis.getValue()]).floatValue(), axisFormatter);
                        saveNewValue(mRightAdd,   sphereCylFormatter.parse(npAdd.getDisplayedValues()[npAdd.getValue()]).floatValue(), sphereCylFormatter);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    d.dismiss();
                }
            });
        } else {
            prepareNumberPicker(npSph, 20, -20, -0.25f, sphereCylFormatter, mLeftSphere.getText().toString());
            prepareNumberPicker(npCyl, 0, -15, -0.25f, sphereCylFormatter, mLeftCyl.getText().toString());
            prepareNumberPicker(npAxis, 1, 180, 1, axisFormatter, mLeftAxis.getText().toString());
            prepareNumberPicker(npAdd,0, 4, 0.25f, sphereCylFormatter, mLeftAdd.getText().toString());

            btSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        saveNewValue(mLeftSphere, sphereCylFormatter.parse(npSph.getDisplayedValues()[npSph.getValue()]).floatValue(), sphereCylFormatter);
                        saveNewValue(mLeftCyl,    sphereCylFormatter.parse(npCyl.getDisplayedValues()[npCyl.getValue()]).floatValue(), sphereCylFormatter);
                        saveNewValue(mLeftAxis,   axisFormatter.parse(npAxis.getDisplayedValues()[npAxis.getValue()]).floatValue(), axisFormatter);
                        saveNewValue(mLeftAdd,   sphereCylFormatter.parse(npAdd.getDisplayedValues()[npAdd.getValue()]).floatValue(), sphereCylFormatter);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    d.dismiss();
                }
            });
        }
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });

        d.show();
    }

    public void spheroPopup(final View view) {
        showRefractionChangeDialog(view, 20, -20, -0.25f, sphereCylFormatter);
    }
    public void cylPopup(final View view) {
        if (getNetActivity().getApp().getSettings().isNegativeCylModel())
            showRefractionChangeDialog(view, 0, -15, -0.25f, sphereCylFormatter);
        else
            showRefractionChangeDialog(view, 0, 15, 0.25f, sphereCylFormatter);
    }
    public void axisPopup(final View view) {
        showRefractionChangeDialog(view, 1, 180, 1, axisFormatter);
    }
    public void addPopup(final View view) {
        showRefractionChangeDialog(view, 0, 4, 0.25f, sphereCylFormatter);
    }
    public void pdPopup(final View view) {
        showRefractionChangeDialog(view, 50, 80, 1f, pdFormatter);
    }
    public void acuityPopup(final View view) {
        showAcuityDialog(view,
                getSettings().isImperialSystem() ? AcuityFormatter.ACUITY_TYPE.IMPERIAL : AcuityFormatter.ACUITY_TYPE.METRIC,
                R.string.acuity_label);
    }

    private void saveNewSphere(Refraction ref, boolean right, Float newValue) {
        if (isSpherocylindricalMode()) {
            if (right)
                ref.setRightSphere(newValue);
            else
                ref.setLeftSphere(newValue);
        } else {
            if (right)
                ref.setRightSphere(addSphEqToSphCylReturnsNewSph(ref.getRightSphere(), ref.getRightCylinder(), newValue));
            else
                ref.setLeftSphere(addSphEqToSphCylReturnsNewSph(ref.getLeftSphere(), ref.getLeftCylinder(), newValue));
        }
    }

    private Float addSphEqToSphCylReturnsNewSph(Float originalSph, Float originalCyl, Float newSphEq) {
        if (newSphEq == null) return originalSph;

        float originalSphEq = 0;
        if (originalSph != null)
            originalSphEq = originalSph;
        if (originalCyl != null)
            originalSphEq += originalCyl/2;

        return (originalSph == null ? 0 : originalSph) - (originalSphEq - newSphEq);
    }

    private void saveNewValue(View view, Float newValue, NumberFormat formatter) {
        if (mFinalResults == null) return;

        Refraction subj = mFinalResults.getOrCreateFrom(RefractionType.SUBJECTIVE, RefractionType.ENTERING_RX);

        if (!isNegativeCylModel()) {
            subj.putInPositiveCilinder();
        }

        switch (view.getId()) {
            case R.id.right_sphere: saveNewSphere(subj, true, newValue); break;
            case R.id.right_cyl :   subj.setRightCylinder(newValue); break;
            case R.id.right_axis :  subj.setRightAxis(newValue); break;
            case R.id.right_add :   subj.setRightAdd(newValue); break;

            case R.id.left_sphere : saveNewSphere(subj, false, newValue); break;
            case R.id.left_cyl :    subj.setLeftCylinder(newValue); break;
            case R.id.left_axis :   subj.setLeftAxis(newValue); break;
            case R.id.left_add :    subj.setLeftAdd(newValue); break;

            case R.id.pd :          subj.setLeftPd(newValue/2.0f);
                                    subj.setRightPd(newValue/2.0f); break;
        }

        if (!isNegativeCylModel()) {
            subj.putInNegativeCilinder();
        }

        loadViews();
    }

    private void saveNewAcuityValue(View view, Float newValue, NumberFormat formatter) {
        if (mFinalResults == null) return;

        DebugExam results = mFinalResults;

        Refraction subj = results.getRefraction(RefractionType.SUBJECTIVE);
        Refraction netra = results.getRefraction(RefractionType.ENTERING_RX);
        if (subj != null) {
            switch (view.getId()) {
                case R.id.right_acuity:  subj.setRightAcuity(newValue); break;
                case R.id.left_acuity:   subj.setLeftAcuity(newValue); break;
            }

            if (netra != null) {
                switch (view.getId()) {
                    case R.id.right_acuity:
                        if (subj.equalsRight(netra)) netra.setRightAcuity(newValue);
                        break;
                    case R.id.left_acuity:
                        if (subj.equalsLeft(netra)) netra.setLeftAcuity(newValue);
                        break;
                }
            }
        } else {
            if (netra == null) netra = results.getOrCreateFrom(RefractionType.ENTERING_RX, RefractionType.ENTERING_RX);

            switch (view.getId()) {
                case R.id.right_acuity:  netra.setRightAcuity(newValue); break;
                case R.id.left_acuity:   netra.setLeftAcuity(newValue); break;
            }
        }

        loadViews();
    }

    public boolean isNegativeCylModel(){
        return getNetActivity().getApp().getSettings().isNegativeCylModel();
    }

    private void formatTextCylToggle() {
        if (isNegativeCylModel()) {
            mTxCyl.setText(R.string.minus_cyl_label);
            mTxCylChange.setText(R.string.plus);
            mRlCyl.setBackgroundResource(R.drawable.bkg_minus_cyl_box);
            mTxCyl.setTextAppearance(this.getActivity(), R.style.MinusCylBoxText);
            mTxCylChange.setTextAppearance(this.getActivity(), R.style.MinusCylChangeBoxText);
        } else {
            mTxCyl.setText(R.string.plus_cyl_label);
            mTxCylChange.setText(R.string.minus);
            mRlCyl.setBackgroundResource(R.drawable.bkg_plus_cyl_box);
            mTxCyl.setTextAppearance(this.getActivity(), R.style.PlusCylBoxText);
            mTxCylChange.setTextAppearance(this.getActivity(), R.style.PlusCylChangeBoxText);
        }
    }

    private void formatTextSphEqToggle() {
        if (isSpherocylindricalMode()) {
            mTxSph.setText(R.string.result_sph);
            mTxSphChange.setText(R.string.sph_eq_label);
            mRlSph.setBackgroundResource(R.drawable.bkg_sph_cyl_box);
            mTxSph.setTextAppearance(this.getActivity(), R.style.SphCylBoxText);
            mTxSphChange.setTextAppearance(this.getActivity(), R.style.SphCylChangeBoxText);
        } else {
            mTxSph.setText(R.string.sph_eq_label);
            mTxSphChange.setText(R.string.result_sph);
            mRlSph.setBackgroundResource(R.drawable.bkg_sph_eq_box);
            mTxSph.setTextAppearance(this.getActivity(), R.style.SphEqBoxText);
            mTxSphChange.setTextAppearance(this.getActivity(), R.style.SphEqChangeBoxText);
        }
    }


    public class ComputeAndLoadResultsTwoLens extends AsyncTask<NetrometerImageProcessing, ExamResults, ExamResults> {
        @Override
        public void onPreExecute(){
            enableDisplayingProgress();
        }

        @Override
        public ExamResults doInBackground(NetrometerImageProcessing... params) {
            ExamResults e = getNetActivity().getApp().buildNewExamResults();
            e.setSmartStage(isSmartStage());

            Thread right = new Thread(new ComputeAndLoadRefraction(true, e.getRightEye().getEnteringRX(), params[0]), "Results Right Eye Computer");
            Thread left = new Thread(new ComputeAndLoadRefraction(false, e.getLeftEye().getEnteringRX(), params[0]), "Results Left Eye Computer");

            right.start();
            left.start();

            try {
                right.join();
                left.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                throw new RuntimeException(e1);
            }

            return e;
        }

        @Override
        public void onPostExecute(ExamResults results){
            DebugExam table = new DebugExam(results);

            if (getNetActivity() == null) {
                asyncSave(table);
            } else {
                disableDisplayingProgress();

                clearImageProcessing();

                asyncSave(table);

                getNetActivity().enablePrinterIfFound(table);

                publishResults(table);

                Prescription leftPrescription = results.getLeftEye().getEnteringRX();
                Prescription rightPrescription = results.getRightEye().getEnteringRX();

                if (!leftPrescription.isValid() || !rightPrescription.isValid()) {
                    Toast.makeText(getActivity(), R.string.invalid_results_no_lenses, Toast.LENGTH_LONG).show();
                } else if(!leftPrescription.isNearValid() || !rightPrescription.isNearValid()){
                    Toast.makeText(getActivity(), R.string.invalid_near_result, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class ComputeAndLoadRefraction implements Runnable {

        private boolean mRightEye = false;
        private Prescription mRef;
        private NetrometerImageProcessing mProc;

        public ComputeAndLoadRefraction(boolean rightEye, Prescription data, NetrometerImageProcessing proc) {
            this.mRightEye = rightEye;
            this.mRef = data;
            this.mProc = proc;
        }

        @Override
        public void run() {
            //mRef.netro().setFrameWidth(Params.PREVIEW_FRAME_WIDTH);
            //mRef.netro().setFrameHeight(Params.PREVIEW_FRAME_HEIGHT);
            //mRef.netro().setZeroFrame(mProc.getZeroFrame());
            //if (mRightEye)
            //    mRef.netro().setFrame(mProc.getRightFrame());
            //else
            //    mRef.netro().setFrame(mProc.getLeftFrame());

            set(mRef, mProc.calculatePrescription(mRightEye));

            mRef = null;
            mProc = null;
        }

        public void set(Prescription p, com.vitorpamplona.netrometer.imageprocessing.model.Refraction r) {
            if (r ==null || !r.isValid) {
                p.setSphere(99f);
                p.setCylinder(0f);
                p.setAxis(0f);
                p.setAddLens(0f);
                return;
            }

            p.setSphere((float) r.dSphere);
            p.setCylinder((float) r.dCylinder);
            p.setAxis((float) r.dAxis);
            p.setAddLens((float) r.dAdd);
            if(r.dPartialPD!=null){p.setNosePupilDistance(r.dPartialPD.floatValue());}
        }
    }

    private void publishResults(DebugExam results1) {
        this.mFinalResults = results1;

        mEdAge.setEnabled(!mFinalResults.isPrescribed());
        mEdNote.setEnabled(!mFinalResults.isPrescribed());
        mEdEmail.setEnabled(!mFinalResults.isPrescribed());
        mPhoneInputView.setEnabled(!mFinalResults.isPrescribed());

        mEdNote.setError(null);
        mEdEmail.setError(null);
        mEdAge.setError(null);
        mPhoneEditText.setError(null);
        mRightSphere.setError(null);
        mLeftSphere.setError(null);
        mPD.setError(null);

        if (mFinalResults.isPrescribed()) {
            mTxRight.setOnClickListener(null);
            mTxLeft.setOnClickListener(null);

            mLeftSphere.setOnClickListener(null);
            mLeftCyl.setOnClickListener(null);
            mLeftAxis.setOnClickListener(null);
            mLeftAdd.setOnClickListener(null);
            mLeftAcuity.setOnClickListener(null);

            mRightSphere.setOnClickListener(null);
            mRightCyl.setOnClickListener(null);
            mRightAxis.setOnClickListener(null);
            mRightAdd.setOnClickListener(null);
            mRightAcuity.setOnClickListener(null);

            mPD.setOnClickListener(null);
        } else {
            mTxRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AbstractResultsFragment.this.showEyeChangeDialog(view);
                }
            });
            mTxLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AbstractResultsFragment.this.showEyeChangeDialog(view);
                }
            });

            mLeftSphere.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {AbstractResultsFragment.this.spheroPopup(view);
                }
            });
            mLeftCyl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {AbstractResultsFragment.this.cylPopup(view);
                }
            });
            mLeftAxis.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { AbstractResultsFragment.this.axisPopup(view); } });
            mLeftAdd.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { AbstractResultsFragment.this.addPopup(view); } });
            mLeftAcuity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { AbstractResultsFragment.this.acuityPopup(view);
                }
            });

            mRightSphere.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { AbstractResultsFragment.this.spheroPopup(view); } });
            mRightCyl.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { AbstractResultsFragment.this.cylPopup(view); } });
            mRightAxis.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { AbstractResultsFragment.this.axisPopup(view); } });
            mRightAdd.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { AbstractResultsFragment.this.addPopup(view); } });
            mRightAcuity.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { AbstractResultsFragment.this.acuityPopup(view); } });

            mPD.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { AbstractResultsFragment.this.pdPopup(view); } });
        }

        mEdNote.removeTextChangedListener(mEdNoteListener);
        mEdEmail.removeTextChangedListener(mEdEmailListener);

        mEdEmail.setText(mFinalResults.getPrescriptionEmail());
        mPhoneInputView.setNumber(mFinalResults.getPrescriptionPhone());

        mEdNote.setText(mFinalResults.getStudyName());
        loadAge(mFinalResults);

        mEdNote.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        textView.clearFocus();
                        getNetActivity().hideKeyboard();
                        break;
                    case EditorInfo.IME_ACTION_NEXT:
                        textView.clearFocus();
                        getNetActivity().hideKeyboard();
                        break;
                }

                if (AbstractResultsFragment.this.mFinalResults != null) {
                    AbstractResultsFragment.this.mFinalResults.setStudyName(mEdNote.getText().toString());
                    return true;
                }
                return false;
            }
        });

        mEdNote.addTextChangedListener(mEdNoteListener);
        mEdEmail.addTextChangedListener(mEdEmailListener);

        if (mFinalResults.isPrescribed()) {
            if (mFinalResults.getPrescriptionEmail() == null || mFinalResults.getPrescriptionEmail().trim().isEmpty())
                mEdEmail.setVisibility(View.GONE);
            if (mFinalResults.getPrescriptionPhone() == null || mFinalResults.getPrescriptionPhone().trim().isEmpty())
                mPhoneInputView.setVisibility(View.GONE);
        }


        Refraction changed = mFinalResults.getRefraction(RefractionType.SUBJECTIVE);
        Refraction original = mFinalResults.getRefraction(RefractionType.ENTERING_RX);

        formatTextCylToggle();
        formatTextSphEqToggle();

        if (original != null && (original.getLeftSphere() == null || (original.getLeftSphere() != null && original.getLeftSphere() < 98))) {
            if (isNegativeCylModel()) {
                original.putInNegativeCilinder();
            } else {
                original.putInPositiveCilinder();
            }

            if (changed != null) {
                if (isNegativeCylModel()) {
                    changed.putInNegativeCilinder();
                } else {
                    changed.putInPositiveCilinder();
                }

                setLeftMainPowersUp(changed);
                mInstructions1.setText(R.string.adjust_refraction);

                // Avoid saving with Positive
                changed.putInNegativeCilinder();
            } else {
                setLeftMainPowersUp(original);
                mInstructions1.setText(R.string.netrometer_results);
            }

            setOldLeftPowersUp(original);

            // Avoid saving with Positive
            original.putInNegativeCilinder();
        } else {
            mLeftSphereOld.setText("-.--");
            mLeftCylOld.setText("-.--");
            mLeftAxisOld.setText("---");
            mLeftAddOld.setText("-.--");
            mLeftAcuityOld.setText("-.--");

            if (changed != null) {
                if (isNegativeCylModel()) {
                    changed.putInNegativeCilinder();
                } else {
                    changed.putInPositiveCilinder();
                }

                setLeftMainPowersUp(changed);
                mInstructions1.setText(R.string.adjust_refraction);

                // Avoid saving with Positive
                changed.putInNegativeCilinder();
            } else {
                mLeftSphere.setText("-.--");
                mLeftCyl.setText("-.--");
                mLeftAxis.setText("---");
                mLeftAdd.setText("-.--");
                mLeftAcuity.setText("---");

                mInstructions1.setText(R.string.netrometer_results);
            }
        }

        if (original != null && (original.getRightSphere() == null || (original.getRightSphere() != null && original.getRightSphere() < 98))) {

            if (isNegativeCylModel()) {
                original.putInNegativeCilinder();
            } else {
                original.putInPositiveCilinder();
            }

            if (changed != null) {
                if (isNegativeCylModel()) {
                    changed.putInNegativeCilinder();
                } else {
                    changed.putInPositiveCilinder();
                }

                setRightMainPowersUp(changed);
                mInstructions1.setText(R.string.adjust_refraction);

                // Avoid saving with Positive
                changed.putInNegativeCilinder();
            } else {
                setRightMainPowersUp(original);

                mInstructions1.setText(R.string.netrometer_results);
            }

            setOldRightPowersUp(original);

            // Avoid saving with Positive
            original.putInNegativeCilinder();
        } else {
            mRightSphereOld.setText("-.--");
            mRightCylOld.setText("-.--");
            mRightAxisOld.setText("---");
            mRightAddOld.setText("-.--");
            mRightAcuityOld.setText("-.--");

            if (changed != null) {
                if (isNegativeCylModel()) {
                    changed.putInNegativeCilinder();
                } else {
                    changed.putInPositiveCilinder();
                }

                setRightMainPowersUp(changed);
                mInstructions1.setText(R.string.adjust_refraction);

                // Avoid saving with Positive
                changed.putInNegativeCilinder();
            } else {
                mRightSphere.setText("-.--");
                mRightCyl.setText("-.--");
                mRightAxis.setText("---");
                mRightAdd.setText("-.--");
                mRightAcuity.setText("---");

                mInstructions1.setText(R.string.netrometer_results);
            }
        }

        if (changed != null) {
            mPD.setText(formatPd(changed.getRightPd(), changed.getLeftPd()));
        } else if (original != null) {
            mPD.setText(formatPd(original.getRightPd(), original.getLeftPd()));
        } else {
            mPD.setText("---");
        }

        if (original != null) {
            mPDOld.setText(formatPd(original.getRightPd(), original.getLeftPd()));
        } else {
            mPDOld.setText("---");
        }

        setVisibleIfDifferent(mRightSphere, mRightSphereOld);
        setVisibleIfDifferent(mRightCyl, mRightCylOld);
        setVisibleIfDifferent(mRightAxis, mRightAxisOld);
        setVisibleIfDifferent(mRightAdd, mRightAddOld);
        setVisibleIfDifferent(mRightAcuity, mRightAcuityOld);
        setVisibleIfDifferent(mLeftSphere, mLeftSphereOld);
        setVisibleIfDifferent(mLeftCyl, mLeftCylOld);
        setVisibleIfDifferent(mLeftAxis, mLeftAxisOld);
        setVisibleIfDifferent(mLeftAdd, mLeftAddOld);
        setVisibleIfDifferent(mLeftAcuity, mLeftAcuityOld);
        setVisibleIfDifferent(mPD, mPDOld);

    }

    private void setRightMainPowersUp(Refraction r) {
        setRightMainPowersUp(r.getRightSphere(), r.getRightCylinder(), r.getRightAxis(), r.getRightAdd(), r.getRightAcuity());
    }

    private void setLeftMainPowersUp(Refraction r) {
        setLeftMainPowersUp(r.getLeftSphere(), r.getLeftCylinder(), r.getLeftAxis(),r.getLeftAdd(), r.getLeftAcuity());
    }

    private void setRightMainPowersUp(Float sph, Float cyl, Float axis, Float add, Float acuity) {
        if (isSpherocylindricalMode()) {
            mRightSphere.setText(formatSphere(sph));
            mRightCyl.setText(formatCylinder(cyl));
            mRightAxis.setText(formatAxis(cyl, axis));
            mRightAdd.setText(formatAdd(add));
            mRightAcuity.setText(formatAcuity(acuity));
        } else {
            mRightSphere.setText(formatSphereEq(sph, cyl));
            mRightCyl.setText("-.--");
            mRightAxis.setText("---");
            mRightAdd.setText(formatAdd(add));
            mRightAcuity.setText(formatAcuity(acuity));
        }
    }

    private void setLeftMainPowersUp(Float sph, Float cyl, Float axis, Float add, Float acuity) {
        if (isSpherocylindricalMode()) {
            mLeftSphere.setText(formatSphere(sph));
            mLeftCyl.setText(formatCylinder(cyl));
            mLeftAxis.setText(formatAxis(cyl, axis));
            mLeftAdd.setText(formatAdd(add));
            mLeftAcuity.setText(formatAcuity(acuity));
        } else {
            mLeftSphere.setText(formatSphereEq(sph, cyl));
            mLeftCyl.setText("-.--");
            mLeftAxis.setText("---");
            mLeftAdd.setText(formatAdd(add));
            mLeftAcuity.setText(formatAcuity(acuity));
        }
    }

    private void setOldRightPowersUp(Refraction r) {
        setOldRightPowersUp(r.getRightSphere(), r.getRightCylinder(), r.getRightAxis(), r.getRightAdd(), r.getRightAcuity());
    }

    private void setOldLeftPowersUp(Refraction r) {
        setOldLeftPowersUp(r.getLeftSphere(), r.getLeftCylinder(), r.getLeftAxis(),r.getLeftAdd(), r.getLeftAcuity());
    }

    private void setOldRightPowersUp(Float sph, Float cyl, Float axis, Float add, Float acuity) {
        if (isSpherocylindricalMode()) {
            mRightSphereOld.setText(formatSphere(sph));
            mRightCylOld.setText(formatCylinder(cyl));
            mRightAxisOld.setText(formatAxis(cyl, axis));
            mRightAddOld.setText(formatAdd(add));
            mRightAcuityOld.setText(formatAcuity(acuity));
        } else {
            mRightSphereOld.setText(formatSphereEq(sph, cyl));
            mRightCylOld.setText("-.--");
            mRightAxisOld.setText("---");
            mRightAddOld.setText(formatAdd(add));
            mRightAcuityOld.setText(formatAcuity(acuity));
        }
    }

    private void setOldLeftPowersUp(Float sph, Float cyl, Float axis, Float add, Float acuity) {
        if (isSpherocylindricalMode()) {
            mLeftSphereOld.setText(formatSphere(sph));
            mLeftCylOld.setText(formatCylinder(cyl));
            mLeftAxisOld.setText(formatAxis(cyl, axis));
            mLeftAddOld.setText(formatAdd(add));
            mLeftAcuityOld.setText(formatAcuity(acuity));
        } else {
            mLeftSphereOld.setText(formatSphereEq(sph, cyl));
            mLeftCylOld.setText("-.--");
            mLeftAxisOld.setText("---");
            mLeftAddOld.setText(formatAdd(add));
            mLeftAcuityOld.setText(formatAcuity(acuity));
        }
    }

    private String formatAdd(Float add) {
        if (add != null && Math.abs(add) > 0.001 && Math.abs(add) < 97)
            return sphereCylFormatter.format(add);
        else
            return "-.--";
    }

    private boolean isSpherocylindricalMode() {
        return getNetActivity().getApp().getSettings().isSpherocylindricalMode();
    }

    protected String formatPd(Float right, Float left) {
        if (right == null || left == null) return "-.--";
        return pdFormatter.format((double)right + left);
    }

    protected String formatAxis(Float cyl, Float axis) {
        if (cyl == null) return "---";
        if (axis == null) return "---";

        float fl = axis;
        if (Math.abs(fl) < 0.0001)
            fl = 180;
        return axisFormatter.format(fl);
    }

    protected String formatSphere(Float f) {
        if (f == null) return "-.--";

        return sphereCylFormatter.format(f);
    }
    protected String formatCylinder(Float f) {
        if (f == null) return "-.--";

        return sphereCylFormatter.format(f);
    }
    protected String formatSphereEq(Float f, Float cyl) {
        if (f == null && cyl == null) return "-.--";

        if (f == null) f = 0.0f;

        if (cyl == null) return sphereCylFormatter.format(f);

        return sphereCylFormatter.format(f + cyl / 2);
    }

    protected String formatAcuity(Float acuity) {
        if (acuity == null) return "--/--";

        AcuityFormatter formatter = new AcuityFormatter();
        return formatter.format(acuity, NetrometerApplication.get().getSettings().isImperialSystem() ? AcuityFormatter.ACUITY_TYPE.IMPERIAL : AcuityFormatter.ACUITY_TYPE.METRIC);
    }

    public boolean isLeftValid(Refraction r) {
        return r != null && (r.getLeftSphere()  != null);
    }
    public boolean isRightValid(Refraction r) {
        return r != null && (r.getRightSphere()  != null);
    }

    private void loadViews() {
        if (mFinalResults == null) {
            resetViews();
        } else {
            publishResults(mFinalResults);
        }
    }

    private void loadAge(DebugExam results) {
        Integer age = getFormattedAge(results);
        if (age != null)
            mEdAge.setText(age.toString());
        else
            mEdAge.getText().clear();
    }

    public Calendar getDefaultDate() {
        final Calendar c = Calendar.getInstance();
        if (mFinalResults != null && mFinalResults.getDateOfBirth() != null) {
            c.setTime(mFinalResults.getDateOfBirth());
        } else {
            c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 28);
        }
        return c;
    }

    public String getDateTitle() {
        return getResources().getString(R.string.date_of_birth);
    }

    public void onDateChanged(Calendar c) {
        if (mFinalResults != null) {
            mFinalResults.setDateOfBirth(c.getTime());
            loadViews();
        }
    }

    public Integer getFormattedAge(DebugExam table) {
        return AgeCalculator.calculateAge(table.getDateOfBirth());
    }

    private void resetViews() {
        mEdNote.getText().clear();
        mEdAge.getText().clear();

        mLeftSphere.setText("-.--");
        mLeftCyl.setText("-.--");
        mLeftAxis.setText("---");
        mLeftAdd.setText("-.--");
        mLeftAcuity.setText("--/--");

        mRightSphere.setText("-.--");
        mRightCyl.setText("-.--");
        mRightAxis.setText("---");
        mRightAdd.setText("-.--");
        mRightAcuity.setText("--/--");

        mLeftSphereOld.setText("-.--");
        mLeftCylOld.setText("-.--");
        mLeftAxisOld.setText("---");
        mLeftAddOld.setText("---");
        mLeftAcuityOld.setText("--/--");

        mRightSphereOld.setText("-.--");
        mRightCylOld.setText("-.--");
        mRightAxisOld.setText("---");
        mRightAddOld.setText("---");
        mRightAcuityOld.setText("--/--");
    }

    public void setVisibleIfDifferent(TextView v1, TextView v1Old) {
        if (!v1.getText().equals(v1Old.getText()))
            v1Old.setVisibility(View.VISIBLE);
        else
            v1Old.setVisibility(View.GONE);
    }

    public void enableDisplayingProgress(){
        if (getNetActivity() != null) getNetActivity().enableProgress();
        mNextButton.setEnabled(false);
    }

    public void disableDisplayingProgress(){
        if (getNetActivity() != null) getNetActivity().disableProgress();
        mNextButton.setEnabled(true);
    }

    private void clearImageProcessing() {
        if (getNetActivity() != null) getNetActivity().clearImageProcessing();
    }

    Thread tSaving;

    public void asyncSave(DebugExam e) {
        tSaving = new Thread(new Saving(e), "Async save");
        tSaving.start();
    }

    public class Saving implements Runnable {
        DebugExam e;
        SQLiteHelper sql;

        public Saving(DebugExam e) {
            this.e = e;
            sql = NetrometerApplication.get().getSqliteHelper();
        }

        public void run() {
            sql.saveDebugExam(e);
            sql.debugExamTable.setToSyncDebug(e);
            sql.debugExamTable.setToSyncInsight(e);

            if (!e.isPrescribed() && e.isReadyToPrescribe())
                sql.debugExamTable.setToSyncPrescription(e);

            new ExportLastReading().save(e);

            e = null;
            sql = null;
        }
    }

    @Override
    public void onPause() {
        getNetActivity().disableProgress();

        if (mFinalResults != null)
            new SaveThread(mFinalResults, NetrometerApplication.get().getSqliteHelper()).execute();

        //mFinalResults = null;

        super.onPause();
    }

    public boolean onBackPressed() {
        if (goBackHome) {
            getNetActivity().loadStartFragment();
            return true;
        } else {
            getNetActivity().loadReadingsFragment(false);
            return true;
        }
    }

    public void onNextPressed() {
        if (goBackHome)
            getNetActivity().loadStartFragment();
        else
            getNetActivity().loadReadingsFragment(false);
    }


    public class SaveThread extends AsyncTask<DebugExam, Integer, Boolean> {

        DebugExam results;
        SQLiteHelper sql;

        public SaveThread(DebugExam results, SQLiteHelper sql) {
            this.results = results;
            this.sql = sql;
        }

        @Override
        protected Boolean doInBackground(DebugExam... params) {
            // Wating to Save the test
            if (tSaving != null) {
                try {
                    tSaving.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            results.setStudyName(mEdNote.getText().toString());
            sql.saveDebugExam(results);
            sql.debugExamTable.setToSyncDebug(results);
            sql.debugExamTable.setToSyncInsight(results);
            sql.debugExamTable.setReadyToDeleteWhenSync(results);

            if (!results.isPrescribed() && results.isReadyToPrescribe())
                sql.debugExamTable.setToSyncPrescription(results);

            new ExportLastReading().save(results);

            results = null;
            sql = null;

            System.gc();

            return true;
        }
    }
}
