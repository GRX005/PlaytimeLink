package _1ms.playtimelink;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public final class Main extends JavaPlugin {

    public HashMap<Long, String> rewardsH = new HashMap<>();
    public RequestSender requestSender;
    public PlaceholderAPI placeholderAPI;
    public UpdateHandler updateHandler;
    @Getter
    private final String loadingMsg = getConfig().getString("Messages.LOADING");

    @Override
    public void onEnable() {
        InitInstances();
        saveDefaultConfig();
        requestSender.runPlaytimeUpdates();
        final boolean isUpdate = getConfig().getBoolean("Data.CHECK_FOR_UPDATES");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPI.register();
        }
        if (getConfig().getBoolean("Data.BSTATS")) {
            Metrics metrics = new Metrics(this, 22917);
            metrics.addCustomChart(new SimplePie("updater", () -> String.valueOf(isUpdate)));
        }
        getServer().getMessenger().registerIncomingPluginChannel(this, "velocity:playtime", requestSender);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "velocity:playtime");
        if (isUpdate)
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> updateHandler.checkForUpdates());
        final ConfigurationSection confSec = getConfig().getConfigurationSection("Rewards");
        if (confSec != null) {
            final Set<String> set = Objects.requireNonNull(confSec).getKeys(true);
            set.iterator().forEachRemaining(key -> rewardsH.put(Long.valueOf(key), getConfig().getString("Rewards." + key)));
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                final List<String> commandsToExecute = new ArrayList<>();
                rewardsH.keySet().forEach(key ->
                        requestSender.playtime.values().forEach(val -> {
                            if (Objects.equals(key, val)) {
                                commandsToExecute.add(rewardsH.get(key));
                            }
                        })
                );
                if (!commandsToExecute.isEmpty()) {
                    Bukkit.getScheduler().runTask(this, () ->
                            commandsToExecute.forEach(command ->
                                    Bukkit.dispatchCommand(getServer().getConsoleSender(), command)
                            )
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

}
