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
            return main.getNotfoundMsg();
        }
        ID = ID.substring(9);
        if(!ID.startsWith("top")) { //%VPTlink_playtime_|hours%
            final long pt = requestSender.getPlayTime(Objects.requireNonNull(player).getName());
            if (pt == -1)
                return main.getLoadingMsg();
            return ID.startsWith("total") ? calcTotalPT(pt, ID.substring(5)) : calculatePlayTime(pt, ID); //%VPTlink_playtime_totalhours%
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
                ID = ID.substring(9);
                return ID.startsWith("total") ? calcTotalPT(entry.getValue(), ID.substring(5)) : calculatePlayTime(entry.getValue(), ID); //%VPTlink_playtime_toptime1_totalhours%
            }
        }
        return main.getLoadingMsg();
    }
    public String calculatePlayTime(long rawValue, String v) {
        return switch (v) {
            case "years" -> String.valueOf(rawValue / 31536000000L); // 1 year = 31,536,000,000 ms
            case "months" -> String.valueOf((rawValue % 31536000000L) / 2419200000L); // 1 month = 2,419,200,000 ms
            case "weeks" -> String.valueOf(((rawValue % 31536000000L) % 2419200000L) / 604800000L); // 1 week = 604,800,000 ms
            case "days" -> String.valueOf((((rawValue % 31536000000L) % 2419200000L) % 604800000L) / 86400000); // 1 day = 86,400,000 ms
            case "hours" -> String.valueOf(((((rawValue % 31536000000L) % 2419200000L) % 604800000L) % 86400000) / 3600000); // 1 hour = 3,600,000 ms
            case "minutes" -> String.valueOf((((((rawValue % 31536000000L) % 2419200000L) % 604800000L) % 86400000) % 3600000) / 60000); // 1 minute = 60,000 ms
            case "seconds" -> String.valueOf(((((((rawValue % 31536000000L) % 2419200000L) % 604800000L) % 86400000) % 3600000) % 60000) / 1000); // 1 second = 1,000 ms
            default -> "ERR_INVALID_PLACEHOLDER";
        };
    }

    private String calcTotalPT(long rawValue, String v) {
        return switch (v) {
            case "years" -> String.valueOf(rawValue / 31536000000L);
            case "months" -> String.valueOf(rawValue / 2419200000L);
            case "weeks" -> String.valueOf(rawValue / 604800000L);
            case "days" -> String.valueOf(rawValue / 86400000);
            case "hours" -> String.valueOf(rawValue / 3600000);
            case "minutes" -> String.valueOf(rawValue / 60000);
            case "seconds" -> String.valueOf(rawValue / 1000);
            default -> "ERR_INVALID_PLACEHOLDER";
        };
    }
}
