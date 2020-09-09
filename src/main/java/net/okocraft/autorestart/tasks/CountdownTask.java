package net.okocraft.autorestart.tasks;

import net.okocraft.autorestart.AutoRestartPlugin;
import org.jetbrains.annotations.NotNull;

public class CountdownTask implements Runnable {

    private final AutoRestartPlugin plugin;
    private long time;

    public CountdownTask(@NotNull AutoRestartPlugin plugin, long time) {
        this.plugin = plugin;
        this.time = time;
    }

    @Override
    public void run() {
        if (plugin.getGeneralConfig().isBossBarEnabled()) {
            plugin.getTimer().start(time);
        }

        checkBroadcastTime();
        plugin.scheduleTask(this::count, 1);
    }

    private void count() {
        time--;

        if (0 < time) {
            checkBroadcastTime();

            if (plugin.getTimer().isRunning()) {
                plugin.getTimer().update();
            }

            plugin.scheduleTask(this::count, 1);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, new RestartTask(plugin));
        }
    }

    private void checkBroadcastTime() {
        if (plugin.getGeneralConfig().getSecondsToBroadcast().contains(time)) {
            String message = plugin.getMessageConfig().getCountdownMessage(time, plugin.getFormattedRestartReason());
            plugin.getServer().broadcastMessage(message);
        }
    }
}
