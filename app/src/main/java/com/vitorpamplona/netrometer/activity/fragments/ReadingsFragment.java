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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vitorpamplona.netrometer.utils.DataUtil;
import com.vitorpamplona.netrometer.NetrometerApplication;
import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.TypeFaceProvider;
import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.activity.fragments.cards.DebugExam2Holder;
import com.vitorpamplona.netrometer.model.db.SQLiteHelper;
import com.vitorpamplona.netrometer.model.db.objects.DebugExam;
import com.vitorpamplona.netrometer.model.db.tables.DebugExamTable;
import com.vitorpamplona.netrometer.model.db.tables.Table;
import com.vitorpamplona.netrometer.printer.AGPPrinterAPI;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ReadingsFragment extends AbstractNetrometerFragment {

    private TextView mMeasurementsLabel;
    private TextView mPrescriptionsLabel;

    private TextView mMeasurements;
    private TextView mPrescriptions;

    private RecyclerView mRecyclerView;
    private DebugExamAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_nav_readings, container, false);


        mMeasurementsLabel = (TextView) view.findViewById(R.id.txMeasurementsLabel);
        mPrescriptionsLabel = (TextView) view.findViewById(R.id.txPrescriptionsLabel);
        mMeasurements = (TextView) view.findViewById(R.id.txMeasurements);
        mPrescriptions = (TextView) view.findViewById(R.id.txPrescriptions);

        mMeasurementsLabel.setTypeface(TypeFaceProvider.getTypeFace(getActivity(),TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        mPrescriptionsLabel.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // 2. set layoutManger
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getNetActivity()));
        // 3. create an adapter
        mAdapter = new DebugExamAdapter(getNetActivity().getApp().getSqliteHelper().allIds(getNetActivity().getApp().getSettings().getLoggedInUsername()), getNetActivity());
        // 4. set adapter
        mRecyclerView.setAdapter(mAdapter);
        // 5. set item animator to DefaultAnimator
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return view;
    }

    public void backToTop() {
        if (mRecyclerView != null)
            mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onResume() {
        getNetActivity().enableToolbarView();
        getNetActivity().showMenu();
        getNetActivity().hidePrinterButton();
        getNetActivity().showNewCustomReadingButton();
        getNetActivity().hideCameraPreviewButton();
        getNetActivity().animateToHideCamera();

        reloadStats();

        super.onResume();
    }

    public void afterSync() {
        reloadStats();
    }

    public void reloadStats() {
        SQLiteHelper.UsageStats usage = NetrometerApplication.get().getSqliteHelper().stats(
                NetrometerApplication.get().getSettings().getLoggedInUsername());
        mMeasurements.setText(""+ usage.measurements);
        mPrescriptions.setText(""+ usage.prescriptions);
        this.refreshCards();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onBackPressed() {
        getNetActivity().loadStartFragment();
        return true;
    }

    public void refreshCards() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    public void refreshAddedCard() {
        if (mAdapter != null) {
            mAdapter.addItem();
        }
        backToTop();
    }

    public class DebugExamAdapter extends RecyclerView.Adapter<DebugExam2Holder> {
        Cursor mCursorAdapter;
        NetrometerActivity activity;

        private Set<String> datesUsed = new HashSet<String>();

        public DebugExamAdapter(Cursor c, NetrometerActivity activity) {
            mCursorAdapter = c; this.activity = activity;
        }

        public void resetCursor() {
            mCursorAdapter = NetrometerApplication.get().getSqliteHelper().allIds(NetrometerApplication.get().getSettings().getLoggedInUsername());
            reloadStats();
        }

        public void removeItem(int position) {
            resetCursor();
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }

        public void addItem() {
            resetCursor();
            notifyItemInserted(0);
            notifyItemRangeChanged(0, getItemCount());
        }

        // Create new views (invoked by the layout manager)
        @Override
        public DebugExam2Holder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View itemLayoutView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.readings_card_2, null);

            DebugExam2Holder viewHolder = new DebugExam2Holder(itemLayoutView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(DebugExam2Holder holder, int position) {
            // Passing the binding operation to cursor loader
            String formattedDate = null;
            if (position > 0) {
                mCursorAdapter.moveToPosition(position - 1); //EDITED: added this line as suggested in the comments below, thanks :)
                Date date = DataUtil.timestampStringToDate(DataUtil.getString(mCursorAdapter, DebugExamTable.TESTED));
                formattedDate = holder.formatDate(date, activity);
            }

            mCursorAdapter.moveToPosition(position); //EDITED: added this line as suggested in the comments below, thanks :)
            long id = DataUtil.getLong(mCursorAdapter, Table.ID);
            DebugExam e = ReadingsFragment.this.getNetActivity().getApp().getSqliteHelper().findDebugExam(id);

            holder.loadMeasurement(e, activity, this);

            if (formattedDate != null && formattedDate.equals(holder.getDate())) {
                holder.hideDate();
            } else {
                holder.showDate();
            }
        }

        // Return the size of your itemsData (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mCursorAdapter.getCount();
        }
    }
}
