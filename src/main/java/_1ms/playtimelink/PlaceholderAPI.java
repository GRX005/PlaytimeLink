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

import _1ms.BuildConstants;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static _1ms.playtimelink.TimeConverter.calcTotalPT;
import static _1ms.playtimelink.TimeConverter.convert;

public class PlaceholderAPI extends PlaceholderExpansion {

    private final RequestSender requestSender;
    private final Main main;

    public PlaceholderAPI(RequestSender requestSender, Main main) {
        this.requestSender = requestSender;
        this.main = main;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "VPTlink";
    }

    @Override
    public @NotNull String getAuthor() {
        return "_1ms";
    }

    @Override
    public @NotNull String getVersion() {
        return BuildConstants.VERSION;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(@Nullable Player player, @NotNull String ID) {
        if(ID.equals("place")) {
            if(!requestSender.isReqTopList()) { //Start the PTTOP request task if it's needed.
                requestSender.setReqTopList(true);
                requestSender.startGetTL();
            }
            int i = 0;
            final Set<Map.Entry<String, Long>> entryA = requestSender.getPttop().entrySet();
            if(entryA.isEmpty())
                return main.getLoadingMsg();
            for(Map.Entry<String, Long> entry : entryA) {
                i++;
                if(entry.getKey().equalsIgnoreCase(Objects.requireNonNull(player).getName()))
                    return String.valueOf(i);
            }
            return main.getNotfoundMsg(); //Should always be found now but I'll leave it in regardless
        }

        if(!ID.startsWith("top")) { //%VPTlink_playtime_|hours%
            final long pt = requestSender.getPlayTime(Objects.requireNonNull(player).getName());
            if (pt == -1)
                return main.getLoadingMsg();
            return ID.startsWith("total") ? calcTotalPT(pt, ID.substring(5)) : convert(pt, ID); //%VPTlink_playtime_totalhours%
        }
        int index = 0;
        if(!requestSender.isReqTopList()) { //Start the PTTOP request task if it's needed.
            requestSender.setReqTopList(true);
            requestSender.startGetTL();
        }
        for (Map.Entry<String, Long> entry : requestSender.getPttop().entrySet()) {
            index++;

            final StringBuilder num = new StringBuilder();
            String IDCOPY = ID.substring(7);

            while (!IDCOPY.isEmpty() && Character.isDigit(IDCOPY.charAt(0))) {
                num.append(IDCOPY.charAt(0));
                IDCOPY = IDCOPY.substring(1); //For numbers with more than 1 digits.
            }
            if (num.toString().equals(String.valueOf(index))) {
                if(ID.startsWith("name", 3))
                    return entry.getKey();
                ID = ID.substring(9);
                while (Character.isDigit(ID.charAt(0))) //For numbers of lenght 3 or more
                    ID=ID.substring(1);
                if(ID.startsWith("_"))  //Only for lenght 2
                    ID=ID.substring(1);
                if(ID.startsWith("total")) {
                    ID=ID.substring(5);
                    return calcTotalPT(entry.getValue(), ID);
                }
                return convert(entry.getValue(), ID); //%VPTlink_playtime_toptime1_totalhours%
            }
        }
        return main.getLoadingMsg();
    }
}
