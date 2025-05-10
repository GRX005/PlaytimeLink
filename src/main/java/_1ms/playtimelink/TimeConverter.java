/*      This file is part of the PlaytimeLink project.
        Copyright (C) 2024-2025 _1ms

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>. */

package _1ms.playtimelink;

import java.util.Arrays;

public class TimeConverter {
    // Durations in seconds
    private static final long[] DURS = {
            12L * 30L * 24L * 3600L,  // years
            30L * 24L * 3600L,        // months
            7L  * 24L * 3600L,        // weeks
            24L * 3600L,              // days
            3600L,                    // hours
            60L,                      // minutes
            1L                        // seconds
    };
    private static final String[] UNITS = {
            "years", "months", "weeks", "days", "hours", "minutes", "seconds"
    };

    public static String convert(long secs, String toUnit) {
        // find which index we want
        int target = Arrays.asList(UNITS).indexOf(toUnit.toLowerCase());
        if (target < 0)
            throw new IllegalArgumentException("Unknown unit: " + toUnit);

        // peel off larger units first
        long rem = secs;
        for (int i = 0; i < DURS.length; i++) {
            long count = rem / DURS[i];
            rem %= DURS[i];
            if (i == target) {
                return Long.toString(count);
            }
        }
        // unreachable because target is within DURS
        return "ERR_CONVERT";
    }

    public static String calcTotalPT(long secs, String v) {
        return switch (v) {
            case "years" -> Long.toString(secs/DURS[0]);
            case "months" -> Long.toString(secs/DURS[1]);
            case "weeks" -> Long.toString(secs/DURS[2]);
            case "days" -> Long.toString(secs/DURS[3]);
            case "hours" -> Long.toString(secs/DURS[4]);
            case "minutes" -> Long.toString(secs/DURS[5]);
            case "seconds" -> Long.toString(secs/DURS[6]);
            default -> "ERR_INVALID_PLACEHOLDER";
        };
    }
}
