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
package com.vitorpamplona.netrometer.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Clock {
    List<Event> events;
    long last;
    String category;

    public Clock(String category) {
        this.category = category;
        start();
    }

    public void start() {
        last = Calendar.getInstance().getTimeInMillis();
        events = new ArrayList<Event>();
    }

    public long capture(String name) {
        long curr = Calendar.getInstance().getTimeInMillis();
        long time = curr - last;
        events.add(new Event(time, name));
        last = curr;
        return time;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Event e : events) {
            builder.append(e.toString() + "\n");
        }
        return builder.toString();
    }


    public void log() {
        for (Event e : events) {
            Log.i(category, e.toString());
        }
    }

    public void write() {
        for (Event e : events) {
            System.out.println(category +":\t"+ e.toString());
        }
    }
}
