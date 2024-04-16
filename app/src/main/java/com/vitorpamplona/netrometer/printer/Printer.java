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
package com.vitorpamplona.netrometer.printer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.print.PrintHelper;

import com.vitorpamplona.netrometer.model.db.objects.DebugExam;

import java.util.List;


public class Printer {
    private Activity act;

    public Printer(Activity context) {
        this.act = context;
    }

    public Printer(Activity context, AGPPrinterAPI.TryConnecting connectingListener) {
        this.act = context;

        if (connectingListener != null) {
            if (isAvailable()) {
                connectingListener.isConnectable();
            } else {
                connectingListener.cannotConnect();
            }
        }
    }

    public boolean print(DebugExam results) {
        return print(new ResultsFormatter(act).getFormattedResults(results));
    }

    public boolean print(List<String> results) {
        Bitmap txt = drawPage(results);
        callPrintingIntent(txt);
        return true;
    }

    private Bitmap drawPage(List<String> toPrint) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.MONOSPACE);

        int fontSize = 36;

        paint.setTextSize(fontSize);

        int topMargin = 5;
        int bottomMargin = 5;
        int leftMargin = 5;
        int rightMargin = 5;

        float fontSpacing = fontSize * 1.1f;

        int width = (int)paint.measureText(toPrint.get(0)) + leftMargin + rightMargin;
        int height = (int) (fontSpacing * toPrint.size()) + topMargin + bottomMargin;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int titleBaseLine = topMargin + (int)fontSize;

        int index = 0;
        for (String s : toPrint) {
            canvas.drawText(s, leftMargin, titleBaseLine + fontSpacing * index, paint);
            index++;
        }

        return bitmap;
    }

    public boolean isAvailable() {
        return true;
    }

    public void destroy(){

    }

    public void callPrintingIntent(Bitmap b) {
        PrintHelper photoPrinter = new PrintHelper(act);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        photoPrinter.printBitmap("Lensometer Reading", b);
    }
}
