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
package com.vitorpamplona.netrometer.activity.views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;

public class FixedTitleDatePickerDialog extends DatePickerDialog {

        private CharSequence title;

        public FixedTitleDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth, String title) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
            setPermanentTitle(title);
            getDatePicker().setCalendarViewShown(false);
            getDatePicker().setSpinnersShown(true);
            focusOnFirstEditText(getDatePicker().getRootView());
        }

        public boolean focusOnFirstEditText(View view) {
            if (view instanceof ViewGroup) {
                for (int i = 0; i< ((ViewGroup) view).getChildCount(); i++) {
                    boolean value = focusOnFirstEditText(((ViewGroup) view).getChildAt(i));
                    if (value) return true;
                }
            }

            if (view instanceof EditText) {
                view.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                return true;
            }

            return false;

        }

        public void setPermanentTitle(CharSequence title) {
            this.title = title;
            setTitle(title);
        }

        @Override
        public void onDateChanged(DatePicker view, int year, int month, int day) {
            super.onDateChanged(view, year, month, day);
            setTitle(title);
        }
}