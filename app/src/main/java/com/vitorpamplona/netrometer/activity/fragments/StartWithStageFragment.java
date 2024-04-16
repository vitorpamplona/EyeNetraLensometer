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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vitorpamplona.netrometer.R;
import com.vitorpamplona.netrometer.TypeFaceProvider;
import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.utils.FraudulentUserException;

public class StartWithStageFragment extends AbstractNetrometerFragment {

    private TextView mInstructions1, mInstructions2;
    private ImageButton mStartSingleVision, mStartProgressives, mStartBifocals;
    private ImageButton mFlipSmartStage;
    private boolean mExecuting;

    private TextView
        mTxStartSingleVision,
        mTxStartBifocals,
        mTxStartProgressives;

    public StartWithStageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_with_stage, container, false);

        mInstructions1 = (TextView) view.findViewById(R.id.instructions_1);
        mInstructions2 = (TextView) view.findViewById(R.id.instructions_2);
        mStartSingleVision = (ImageButton) view.findViewById(R.id.start_single_vision);
        mStartBifocals = (ImageButton) view.findViewById(R.id.start_bifocal);
        mStartProgressives = (ImageButton) view.findViewById(R.id.start_progressive);
        mFlipSmartStage = (ImageButton) view.findViewById(R.id.removeStage);

        mTxStartSingleVision = (TextView) view.findViewById(R.id.txSingleVision);
        mTxStartBifocals = (TextView) view.findViewById(R.id.txBifocals);
        mTxStartProgressives = (TextView) view.findViewById(R.id.txProgressive);

        mInstructions2.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        mTxStartSingleVision.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        mTxStartBifocals.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));
        mTxStartProgressives.setTypeface(TypeFaceProvider.getTypeFace(getActivity(), TypeFaceProvider.TYPEFACE_SOURCE_SANS_PRO_LIGHT));

        mFlipSmartStage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipSmartStage();
            }
        });

        mStartSingleVision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSingleVisionStarted();
            }
        });

        mStartProgressives.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onProgressiveStarted();
            }
        });

        mStartBifocals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBifocalsStarted();
            }
        });

        if (isDebugMode()) {
            activateDebugListener(mInstructions1);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getNetActivity().showMenu();
        getNetActivity().hidePrinterButton();
        getNetActivity().hideNewCustomReadingButton();
        getNetActivity().hideCameraPreviewButton();
        getNetActivity().animateToHideCamera();
        getNetActivity().enableToolbarView();
    }

    public void flipSmartStage() {
        getNetActivity().loadStartWithoutSmartStageFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getImageProcessor()!=null) {
            getImageProcessor().registerCenterFinderListener(null);
        }
    }

    private void onSingleVisionStarted() {
        getNetActivity().loadCalibratingSmartStage(AbstractCalibratingFragment.LENS_TYPE.SINGLE_VISION);
    }
    private void onBifocalsStarted() {
        getNetActivity().loadCalibratingSmartStage(AbstractCalibratingFragment.LENS_TYPE.BIFOCALS);
    }
    private void onProgressiveStarted() {
        getNetActivity().loadCalibratingSmartStage(AbstractCalibratingFragment.LENS_TYPE.PROGRESSIVES);
    }

    @Override
    public boolean onBackPressed() {
        getActivity().finish();
        return true;
    }

    private void activateDebugListener(View view) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                ((NetrometerActivity) getActivity()).loadDebugFragment();
                return false;
            }
        });
    }

}
