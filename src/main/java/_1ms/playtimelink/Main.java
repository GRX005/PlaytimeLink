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

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Main extends JavaPlugin {

    public final HashMap<Long, String> rewardsH = new HashMap<>();
    public RequestSender requestSender;
    public PlaceholderAPI placeholderAPI;
    public UpdateHandler updateHandler;
    @Getter
    private final String loadingMsg = getConfig().getString("Messages.LOADING");
    @Getter
    private final String notfoundMsg = getConfig().getString("Messages.NOT_IN_TOPLIST");

    @Override
    public void onEnable() {
        InitInstances();
        saveDefaultConfig();
        checkAndUpdateConfig(Paths.get(getDataFolder()+ File.separator+"config.yml"));
        requestSender.runPlaytimeUpdates();
        final boolean isUpdate = getConfig().getBoolean("Data.CHECK_FOR_UPDATES");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPI.register();
        }
        if (getConfig().getBoolean("Data.BSTATS")) {
            Metrics metrics = new Metrics(this, 22917);
            metrics.addCustomChart(new SimplePie("updater", ()-> String.valueOf(isUpdate)));
            metrics.addCustomChart(new SimplePie("rewards", ()-> String.valueOf(rewardsH.size())));
        }
        getServer().getMessenger().registerIncomingPluginChannel(this, "velocity:playtime", requestSender);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "velocity:playtime");
        if (isUpdate)
            Thread.ofVirtual().start(updateHandler::checkForUpdates);
        final ConfigurationSection confSec = getConfig().getConfigurationSection("Rewards");
        if (confSec != null) {
            final Set<String> set = Objects.requireNonNull(confSec).getKeys(true);
            set.iterator().forEachRemaining(key ->
                    rewardsH.put(Long.valueOf(key), getConfig().getString("Rewards." + key))
            );

            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                final HashMap<String, String> cmdAndPlayer = new HashMap<>();

                rewardsH.forEach((key, command) -> {
                    final HashMap<String, Long> hashToUse = requestSender.isReqTopList() ? requestSender.getPttop() : requestSender.getPlaytime();
                    hashToUse.forEach((player, val) -> {
                        try {
                            if(!Objects.requireNonNull(Bukkit.getPlayer(player)).isPermissionSet("vptlink.rewards.exempt") && Objects.equals(key, val))
                                cmdAndPlayer.put(command, player);
                        } catch (Exception ignored) {}
                    });
                });

                if (!cmdAndPlayer.isEmpty()) {
                    Bukkit.getScheduler().runTask(this, () ->
                            cmdAndPlayer.forEach((key, value) -> Bukkit.dispatchCommand(getServer().getConsoleSender(), key.replace("%player%", value)))
                    );
                }
            }, 0, 20);
        }

        getLogger().info("PlaytimeLink has been loaded.");
    }

    private void InitInstances() {
        requestSender = new RequestSender(this);
        placeholderAPI = new PlaceholderAPI(requestSender, this);
        updateHandler = new UpdateHandler(this);
    }

    public static void checkAndUpdateConfig(Path path) {
        try {
            final List<String> lines = Files.readAllLines(path);
            if(lines.get(1).contains("3"))
                return;
            if (!lines.get(1).contains("file-version")) {
                lines.add(1, "file-version: 3");
                lines.add(4, "  NOT_IN_TOPLIST: \"Not in toplist\"");

                lines.add("#Same as in the main plugin. ([space]'time in millisecs': cmd) (ex: '10000': say hello)");
                lines.add("Rewards:");
            } else {
                lines.remove(1);
                lines.add(1, "file-version: 3");
                lines.add(4, "  NOT_IN_TOPLIST: \"Not in toplist\"");
            }
            Files.write(path, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
