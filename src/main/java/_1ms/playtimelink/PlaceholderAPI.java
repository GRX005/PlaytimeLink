package _1ms.playtimelink;

import _1ms.BuildConstants;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
            int i = 0;
            final Set<Map.Entry<String, Long>> entryA = requestSender.getPttop().entrySet();
            if(entryA.isEmpty())
                return main.getLoadingMsg();
            for(Map.Entry<String, Long> entry : entryA) {
                i++;
                if(entry.getKey().equalsIgnoreCase(Objects.requireNonNull(player).getName()))
                    return String.valueOf(i);
            }
            return "NOT_IN_TOPLIST";
        }
        ID = ID.substring(9);
        if(!ID.startsWith("top")) {
            final long pt = requestSender.getPlayTime(Objects.requireNonNull(player).getName());
            if(pt == 0)
                return main.getLoadingMsg();
            switch (ID) {
                case "hours" -> {
                    return calculatePlayTime(pt, 'h');
                }
                case "minutes" -> {
                    return calculatePlayTime(pt, 'm');
                }
                case "seconds" -> {
                    return calculatePlayTime(pt, 's');
                }
            }
        }
        int index = 0;
        if(!requestSender.isReqTopList()) {
            requestSender.setReqTopList(true);
            requestSender.startGetTL();
        }
        for (Map.Entry<String, Long> entry : requestSender.getPttop().entrySet()) {
            index++;
            if (ID.startsWith(String.valueOf(index), 7)) {
                if(ID.startsWith("name", 3)) {
                    return entry.getKey();
                }
                switch (ID.substring(9)) {
                    case "hours" -> { //VPTlink_playtime_toptime1_hours
                        return calculatePlayTime(entry.getValue(), 'h');
                    }
                    case "minutes" -> {
                        return calculatePlayTime(entry.getValue(), 'm');
                    }
                    case "seconds" -> {
                        return calculatePlayTime(entry.getValue(), 's');
                    }
                }
            }
        }
        return main.getLoadingMsg();
    }

    public String calculatePlayTime(long rawValue, char v) {
        switch (v) {
            case 'h' -> {
                return String.valueOf(rawValue / 3600000);
            }
            case 'm' -> {
                return String.valueOf((rawValue % 3600000) / 60000);
            }
            case 's' -> {
                return String.valueOf(((rawValue % 3600000) % 60000) / 1000);
            }
        }
        return "ERR";
    }
}
