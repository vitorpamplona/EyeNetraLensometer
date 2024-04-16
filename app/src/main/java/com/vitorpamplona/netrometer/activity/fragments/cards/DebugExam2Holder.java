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
package com.vitorpamplona.netrometer.activity.fragments.cards;

import android.app.Activity;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.TypeFaceProvider;
import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.activity.fragments.ReadingsFragment;
import com.vitorpamplona.netrometer.model.RefractionType;
import com.vitorpamplona.netrometer.model.db.objects.DebugExam;
import com.vitorpamplona.netrometer.model.db.objects.Refraction;
import com.vitorpamplona.netrometer.utils.AgeCalculator;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DebugExam2Holder extends RecyclerView.ViewHolder {
    // inner class to hold a reference to each item of RecyclerView
    public TextView txtViewPatientName;
    public TextView txtViewEmail;
    public TextView txtViewPhone;
    public TextView mTime;
    public TextView mDateRef;
    public View dayDivision;
    public Button mArchive;
    public ImageView mPrescribed;

    private Format mDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private Format mTimeFormatter = new SimpleDateFormat("hh:mm a");

    private View layoutCard;

    public DebugExam2Holder(View view) {
        super(view);

        mDateFormatter = android.text.format.DateFormat.getMediumDateFormat(view.getContext().getApplicationContext());
        mTimeFormatter = android.text.format.DateFormat.getTimeFormat(view.getContext().getApplicationContext());

        dayDivision = view.findViewById(R.id.dayDivision);
        txtViewPatientName = (TextView) view.findViewById(R.id.cus_name);

        txtViewEmail= (TextView) view.findViewById(R.id.cus_email);
        txtViewPhone= (TextView) view.findViewById(R.id.cus_phone);

        layoutCard = view.findViewById(R.id.layoutCard);

        mPrescribed = (ImageView) view.findViewById(R.id.prescribed_icon);

        mTime = (TextView) view.findViewById(R.id.time);
        mArchive = (Button) view.findViewById(R.id.item_archive);
        mDateRef = (TextView) view.findViewById(R.id.date_reference);
    }

    public void hideDate() {
        dayDivision.setVisibility(View.GONE);
    }
    public void showDate() { dayDivision.setVisibility(View.VISIBLE); }

    public String getDate() {
        return mDateRef.getText().toString();
    }

    public Integer getFormattedAge(DebugExam table) {
        return AgeCalculator.calculateAge(table.getDateOfBirth());
    }

    public void loadMeasurement(final DebugExam exam, final NetrometerActivity act, final ReadingsFragment.DebugExamAdapter adapter) {
        mPrescribed.setVisibility(exam.isPrescribed() ? View.VISIBLE : View.INVISIBLE);
        txtViewEmail.setText(exam.getPrescriptionEmail());

        txtViewPhone.setVisibility(View.VISIBLE);
        txtViewEmail.setVisibility(View.VISIBLE);

        try {
            Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse(exam.getPrescriptionPhone(), null);
            txtViewPhone.setText(PhoneNumberUtil.getInstance().format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL));
        } catch (Exception e) {
            txtViewPhone.setText(exam.getPrescriptionPhone());
        }

        txtViewEmail.setTypeface(TypeFaceProvider.getTypeFace(act, TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        txtViewPhone.setTypeface(TypeFaceProvider.getTypeFace(act, TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        txtViewPatientName.setTypeface(TypeFaceProvider.getTypeFace(act, TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));

        if (exam.getPrescriptionEmail() == null || exam.getPrescriptionEmail().trim().isEmpty())
            txtViewEmail.setVisibility(View.GONE);
        if (exam.getPrescriptionPhone() == null || exam.getPrescriptionPhone().trim().isEmpty())
            txtViewPhone.setVisibility(View.GONE);

        mArchive.setVisibility(View.VISIBLE);
        mArchive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exam.setStatus("archived");

                //Saving on DB
                NetrometerApplication.get().getSqliteHelper().saveDebugExam(exam);
                NetrometerApplication.get().getSqliteHelper().debugExamTable.setToSyncDebug(exam);
                NetrometerApplication.get().getSqliteHelper().debugExamTable.setToSyncInsight(exam);
                NetrometerApplication.get().getSqliteHelper().debugExamTable.setReadyToDeleteWhenSync(exam);

                //Removing item and updating list
                adapter.removeItem(getAdapterPosition());
            }
        });

        layoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exam.getSmartStage())
                    act.loadResultSmartStageFragment(false, exam);
                else
                    act.loadResultNoSmartStageFragment(false, exam);
            }
        });

        txtViewPatientName.setText(exam.getStudyName());
        if (exam.getDateOfBirth() != null)
            txtViewPatientName.setText(exam.getStudyName() + ", " + getFormattedAge(exam));

        if (exam.getStudyName() == null || exam.getStudyName().isEmpty()) {
            txtViewPatientName.setText(act.getResources().getString(R.string.number_prefix_reading_card_empty_note) + exam.getSequenceNumber());
        }

        String date = formatDate(exam.getTested(), act);
        mDateRef.setText(date);
        mTime.setText(formatTime(exam.getTested()));
    }



    protected String formatTime(Date d) {
        if (d == null) return "";

        return mTimeFormatter.format(d);
    }

    public String formatDate(Date d, Activity act) {
        if (d == null) return "";

        Date today = Calendar.getInstance().getTime();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        if (mDateFormatter.format(today).equals(mDateFormatter.format(d))) {
            return act.getResources().getString(R.string.today);
        }
        if (mDateFormatter.format(yesterday.getTime()).equals(mDateFormatter.format(d))) {
            return act.getResources().getString(R.string.yesterday);
        }

        return mDateFormatter.format(d);
    }


}
