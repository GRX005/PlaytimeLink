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
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RequestSender implements PluginMessageListener {
    private final Main main;
    public boolean isPreloaded;
    private int PtTaskID = -1;
    @Getter
    private HashMap<String, Long> playtime = new HashMap<>();
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

    public long getPlayTime(String name) {//Use pttop which contains all pt if it's being used.
        if(!reqTopList)
            return playtime.getOrDefault(name, -1L);
        if(!pttop.isEmpty())
            playtime.clear();
        return pttop.getOrDefault(name, -1L);
    }

    public void runPlaytimeUpdates() {
        PtTaskID = new BukkitRunnable() {
            @Override
            public void run() {
                if(!SA.isEmpty()) {
                    sendReq("rpt");
                }
            }
        }.runTaskTimerAsynchronously(main, 0L, 20L).getTaskId();
    }
    public void startGetTL() {
        new BukkitRunnable() {
            @Override
            public void run() { //1.8 comp.
                if(!SA.isEmpty()) {
                    sendReq("rtl");
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(main, 20L, 20L); //Needs 1s delay.
    }


    //Request Format & Send
    private void sendReq(String msg) {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(msg);
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
            case "rs" -> {//restart(proxy restarted)
                sendReq("cc"); //confirm
                if(PtTaskID != -1 && !Bukkit.getScheduler().isCurrentlyRunning(PtTaskID)) {
                    runPlaytimeUpdates();
                    if(!pttop.isEmpty())
                        startGetTL();
                }
            }
            case "conf" -> { //confirmation that the syncing started
                isPreloaded = in.readBoolean();
                Bukkit.getScheduler().cancelTask(PtTaskID);
            }
        }
    }
}
