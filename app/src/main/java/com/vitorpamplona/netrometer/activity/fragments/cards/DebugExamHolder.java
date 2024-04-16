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

import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.activity.fragments.ReadingsFragment;
import com.vitorpamplona.netrometer.model.RefractionType;
import com.vitorpamplona.netrometer.model.db.objects.DebugExam;
import com.vitorpamplona.netrometer.model.db.objects.Refraction;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by vitor on 12/19/15.
 */
public class DebugExamHolder extends RecyclerView.ViewHolder {
    // inner class to hold a reference to each item of RecyclerView
    public TextView txtViewNotes;
    private TextView mLeftSphere, mLeftCyl, mLeftAxis, mLeftAdd;
    private TextView mRightSphere, mRightCyl, mRightAxis, mRightAdd;

    private TextView mLeftSphereOld, mLeftCylOld, mLeftAxisOld, mLeftAddOld;
    private TextView mRightSphereOld, mRightCylOld, mRightAxisOld, mRightAddOld;

    public TextView mPD,mPDOld, mDateRef;
    public View divisor;
    public TextView mTime;
    public View dayDivision;
    public Button mPrinter;
    public Button mArchive;
    public ImageView mPrescribed;

    DecimalFormat sphereCylFormatter = new DecimalFormat("+0.00;-0.00");
    private DecimalFormat mPdFormatter = new DecimalFormat("0");
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat mTimeFormatter = new SimpleDateFormat("HH:mm");

    private View layoutCard;

    public DebugExamHolder(View view) {
        super(view);

        layoutCard = view.findViewById(R.id.layoutCard);

        dayDivision = view.findViewById(R.id.dayDivision);
        divisor = view.findViewById(R.id.list_item_seperator);
        txtViewNotes = (TextView) view.findViewById(R.id.item_notes);
        mLeftSphere = (TextView) view.findViewById(R.id.left_sphere);
        mLeftCyl = (TextView) view.findViewById(R.id.left_cyl);
        mLeftAxis = (TextView) view.findViewById(R.id.left_axis);
        mLeftAdd = (TextView) view.findViewById(R.id.left_add);

        mLeftSphereOld = (TextView) view.findViewById(R.id.left_sphere_old);
        mLeftCylOld = (TextView) view.findViewById(R.id.left_cyl_old);
        mLeftAxisOld = (TextView) view.findViewById(R.id.left_axis_old);
        mLeftAddOld = (TextView) view.findViewById(R.id.left_add_old);

        mRightSphere = (TextView) view.findViewById(R.id.right_sphere);
        mRightCyl = (TextView) view.findViewById(R.id.right_cyl);
        mRightAxis = (TextView) view.findViewById(R.id.right_axis);
        mRightAdd = (TextView) view.findViewById(R.id.right_add);

        mRightSphereOld = (TextView) view.findViewById(R.id.right_sphere_old);
        mRightCylOld = (TextView) view.findViewById(R.id.right_cyl_old);
        mRightAxisOld = (TextView) view.findViewById(R.id.right_axis_old);
        mRightAddOld = (TextView) view.findViewById(R.id.right_add_old);

        mPrescribed = (ImageView) view.findViewById(R.id.prescribed_icon);

        mPD = (TextView) view.findViewById(R.id.pd);
        mPDOld = (TextView) view.findViewById(R.id.pd_old);

        mTime = (TextView) view.findViewById(R.id.time);

        mPrinter = (Button) view.findViewById(R.id.item_printer);
        mArchive = (Button) view.findViewById(R.id.item_archive);

        mDateRef = (TextView) view.findViewById(R.id.date_reference);

        mRightSphereOld.setPaintFlags(mRightSphereOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mRightCylOld.setPaintFlags(mRightCylOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mRightAxisOld.setPaintFlags(mRightAxisOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mRightAddOld.setPaintFlags(mRightAddOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mLeftSphereOld.setPaintFlags(mLeftSphereOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mLeftCylOld.setPaintFlags(mLeftCylOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mLeftAxisOld.setPaintFlags(mLeftAxisOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mLeftAddOld.setPaintFlags(mLeftAddOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public void hideDate() {
        dayDivision.setVisibility(View.GONE);
    }
    public void showDate() { dayDivision.setVisibility(View.VISIBLE); }

    public String getDate() {
        return mDateRef.getText().toString();
    }

    public void loadMeasurement(final DebugExam exam, final NetrometerActivity act, final ReadingsFragment.DebugExamAdapter adapter) {
        mPrinter.setVisibility(act.isPrinterReady() ? View.VISIBLE : View.INVISIBLE);
        mPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                act.printResults(exam);
            }
        });

        mPrescribed.setVisibility(exam.isPrescribed() ? View.VISIBLE : View.INVISIBLE);

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

        txtViewNotes.setText(exam.getStudyName());
        if (exam.getStudyName() == null || exam.getStudyName().isEmpty()) {
            txtViewNotes.setText(act.getResources().getString(R.string.number_prefix_reading_card_empty_note) + exam.getSequenceNumber());
        }

        layoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exam.getSmartStage())
                    act.loadResultSmartStageFragment(false, exam);
                else
                    act.loadResultNoSmartStageFragment(false, exam);
            }
        });

        String date = formatDate(exam.getTested(), act.getResources());
        mDateRef.setText(date);

        mTime.setText(formatTime(exam.getTested()));

        Refraction ref = exam.getRefraction(RefractionType.ENTERING_RX);
        Refraction refChanged = exam.getRefraction(RefractionType.SUBJECTIVE);

        if (ref != null) {
            if (NetrometerApplication.get().getSettings().isNegativeCylModel()) {
                ref.putInNegativeCilinder();
            } else {
                ref.putInPositiveCilinder();
            }

            if (refChanged != null) {
                if (NetrometerApplication.get().getSettings().isNegativeCylModel()) {
                    refChanged.putInNegativeCilinder();
                } else {
                    refChanged.putInPositiveCilinder();
                }
            }

            if (refChanged != null) {
                mLeftSphere.setText(formatSphere(refChanged.getLeftSphere()));
                mLeftCyl.setText(formatCylinder(refChanged.getLeftSphere(),refChanged.getLeftCylinder()));
                mLeftAxis.setText(formatAxis(refChanged.getLeftSphere(),refChanged.getLeftAxis()));
                mLeftAdd.setText(formatAdd(refChanged.getLeftSphere(),refChanged.getLeftAdd()));
            } else {
                mLeftSphere.setText(formatSphere(ref.getLeftSphere()));
                mLeftCyl.setText(formatCylinder(ref.getLeftSphere(),ref.getLeftCylinder()));
                mLeftAxis.setText(formatAxis(ref.getLeftSphere(),ref.getLeftAxis()));
                mLeftAdd.setText(formatAdd(ref.getLeftSphere(),ref.getLeftAdd()));
            }
            mLeftSphereOld.setText(formatSphere(ref.getLeftSphere()));
            mLeftCylOld.setText(formatCylinder(ref.getLeftSphere(),ref.getLeftCylinder()));
            mLeftAxisOld.setText(formatAxis(ref.getLeftSphere(),ref.getLeftAxis()));
            mLeftAddOld.setText(formatAdd(ref.getLeftSphere(),ref.getLeftAdd()));

            if (refChanged != null) {
                mRightSphere.setText(formatSphere(refChanged.getRightSphere()));
                mRightCyl.setText(formatCylinder(refChanged.getRightSphere(),refChanged.getRightCylinder()));
                mRightAxis.setText(formatAxis(refChanged.getRightSphere(),refChanged.getRightAxis()));
                mRightAdd.setText(formatAdd(refChanged.getRightSphere(),refChanged.getRightAdd()));
            } else {
                mRightSphere.setText(formatSphere(ref.getRightSphere()));
                mRightCyl.setText(formatCylinder(ref.getRightSphere(),ref.getRightCylinder()));
                mRightAxis.setText(formatAxis(ref.getRightSphere(),ref.getRightAxis()));
                mRightAdd.setText(formatAdd(ref.getRightSphere(),ref.getRightAdd()));
            }

            mRightSphereOld.setText(formatSphere(ref.getRightSphere()));
            mRightCylOld.setText(formatCylinder(ref.getRightSphere(),ref.getRightCylinder()));
            mRightAxisOld.setText(formatAxis(ref.getRightSphere(),ref.getRightAxis()));
            mRightAddOld.setText(formatAdd(ref.getRightSphere(),ref.getRightAdd()));

            if (refChanged != null) {
                mPD.setText(formatPd(refChanged.getSumOfPds()));
            } else {
                mPD.setText(formatPd(ref.getSumOfPds()));
            }

            mPDOld.setText(formatPd(ref.getSumOfPds()));
        } else {
            mLeftSphereOld.setText("-.--");
            mLeftCylOld.setText("-.--");
            mLeftAxisOld.setText("---");
            mLeftAddOld.setText("-.--");

            mRightSphereOld.setText("-.--");
            mRightCylOld.setText("-.--");
            mRightAxisOld.setText("---");
            mRightAddOld.setText("-.--");

            if (refChanged != null) {
                mRightSphere.setText(formatSphere(refChanged.getRightSphere()));
                mRightCyl.setText(formatCylinder(refChanged.getRightSphere(),refChanged.getRightCylinder()));
                mRightAxis.setText(formatAxis(refChanged.getRightSphere(),refChanged.getRightAxis()));
                mRightAdd.setText(formatAdd(refChanged.getRightSphere(),refChanged.getRightAdd()));

                mLeftSphere.setText(formatSphere(refChanged.getLeftSphere()));
                mLeftCyl.setText(formatCylinder(refChanged.getLeftSphere(),refChanged.getLeftCylinder()));
                mLeftAxis.setText(formatAxis(refChanged.getLeftSphere(),refChanged.getLeftAxis()));
                mLeftAdd.setText(formatAdd(refChanged.getLeftSphere(),refChanged.getLeftAdd()));

                mPD.setText(formatPd(refChanged.getSumOfPds()));
            } else {
                mLeftSphere.setText("-.--");
                mLeftCyl.setText("-.--");
                mLeftAxis.setText("---");
                mLeftAdd.setText("-.--");

                mRightSphere.setText("-.--");
                mRightCyl.setText("-.--");
                mRightAxis.setText("---");
                mRightAdd.setText("-.--");

                mPD.setText("---");
            }
        }

        setVisibleIfDifferent(mRightSphere, mRightSphereOld);
        setVisibleIfDifferent(mRightCyl, mRightCylOld);
        setVisibleIfDifferent(mRightAxis, mRightAxisOld);
        setVisibleIfDifferent(mRightAdd, mRightAddOld);
        setVisibleIfDifferent(mLeftSphere, mLeftSphereOld);
        setVisibleIfDifferent(mLeftCyl, mLeftCylOld);
        setVisibleIfDifferent(mLeftAxis, mLeftAxisOld);
        setVisibleIfDifferent(mLeftAdd, mLeftAddOld);
    }

    public void setVisibleIfDifferent(TextView v1, TextView v1Old) {
        //if (!v1.getText().equals(v1Old.getText()))
        //    v1Old.setVisibility(View.VISIBLE);
        //else
            v1Old.setVisibility(View.GONE);
    }

    protected String formatTime(Date d) {
        if (d == null) return "";

        return mTimeFormatter.format(d);
    }

    public String formatDate(Date d, Resources res) {
        if (d == null) return "";

        Date today = Calendar.getInstance().getTime();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        if (mDateFormatter.format(today).equals(mDateFormatter.format(d))) {
            return res.getString(R.string.today);
        }
        if (mDateFormatter.format(yesterday.getTime()).equals(mDateFormatter.format(d))) {
            return res.getString(R.string.yesterday);
        }

        return mDateFormatter.format(d);
    }

    protected String formatPd(Float f) {
        if (f == null || f < 1) return "-";
        float fl = f == null ? 0 : f;
        return mPdFormatter.format((double)fl) + "mm";
    }
    protected String formatSphere(Float fSph) {
        if((fSph == null || fSph>98)) {
            return "-";
        }
        else return sphereCylFormatter.format(fSph);
    }
    protected String formatCylinder(Float fSph, Float fCyl) {
        if((fCyl == null || fSph ==null || fSph>98)){
            return "-";
        } else return sphereCylFormatter.format(fCyl);
    }
    DecimalFormat axisFormatter = new DecimalFormat("0°;0°");
    protected String formatAxis(Float fSph, Float fAxis) {
        if(fAxis == null|| fSph ==null || fSph>98){
            return "-";
        } else return axisFormatter.format(fAxis);
    }
    protected String formatAdd(Float fSph, Float fAdd) {
        if(fAdd == null || fSph == null || fSph>98 || fAdd>98){
            return "-";
        } else return sphereCylFormatter.format(fAdd);
    }

}
