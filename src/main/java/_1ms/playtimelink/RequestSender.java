package _1ms.playtimelink;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RequestSender implements PluginMessageListener {
    private final Main main;
    public boolean isPreloaded;
    private BukkitTask PtTask;
    public HashMap<String, Long> playtime = new HashMap<>();
    @Getter
    private LinkedHashMap<String, Long> pttop = new LinkedHashMap<>();
    private final Random random = ThreadLocalRandom.current();
    private final Gson gson = new Gson();
    private final Type typeI = new TypeToken<HashMap<String, Long>>(){}.getType();
    private final Type typeT = new TypeToken<LinkedHashMap<String, Long>>(){}.getType();
    private final Collection<? extends Player> SA = Bukkit.getOnlinePlayers();
    @Setter @Getter
    private boolean reqTopList = false;
    public RequestSender(Main main) {
        this.main = main;
    }

    public long getPlayTime(String name) {
        return playtime.getOrDefault(name, -1L);
    }

    public void runPlaytimeUpdates() {
        PtTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(!SA.isEmpty()) {
                    requestPlaytime();
                }
            }
        }.runTaskTimerAsynchronously(main, 0L, 20L);
    }
    public void startGetTL() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(main, (task) -> {
            if(!SA.isEmpty()) {
                requestToplist();
                task.cancel();
            }
        }, 20L ,20L);
    }


    //Request
    private void requestPlaytime() {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("rpt");
        sendMSG(out); //Try sending it to an npc!
    }

    public void requestToplist() {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("rtl");
        sendMSG(out);
    }

    public void sendMSG(ByteArrayDataOutput out) {
        new ArrayList<>(SA).get(random.nextInt(SA.size())).sendPluginMessage(main, "velocity:playtime", out.toByteArray());
    }

    //Answer
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] bytes) {
        if(!channel.equals("velocity:playtime"))
            return;
        final ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        final String answer = in.readUTF();
        switch (answer) {
            case "pt" -> {
                if(!isPreloaded || playtime.isEmpty()) {
                    playtime = gson.fromJson(in.readUTF(), typeI);
                    return;
                }
                final HashMap<String, Long> playtimes = gson.fromJson(in.readUTF(), typeI);
                playtime.putAll(playtimes);
            }
            case "ptt" -> {
                if(reqTopList)
                    pttop = gson.fromJson(in.readUTF(), typeT);
            }
            case "rs" -> {
                final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("cc");
                sendMSG(out);
                if(PtTask != null && PtTask.isCancelled()) {
                    runPlaytimeUpdates();
                    if(!pttop.isEmpty()) {
                        startGetTL();
                    }
                }
            }
            case "conf" -> {
                isPreloaded = in.readBoolean();
                PtTask.cancel();
            }
        }
    }
}
