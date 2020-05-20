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
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getTimer().start(time));
        }

        checkBroadcastTime();
        plugin.scheduleTask(this::count, 1);
    }

    private void count() {
        time--;
        if (0 < time) {
            checkBroadcastTime();
            plugin.getServer().getScheduler().runTask(plugin, this::checkBar);
            plugin.scheduleTask(this::count, 1);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, new RestartTask(plugin));
        }
    }

    private void checkBar() {
        if (plugin.getTimer().isRunning()) {
            plugin.getTimer().update();
        }
    }

    private void checkBroadcastTime() {
            if (plugin.getGeneralConfig().getSecondsToBroadcast().contains(time)) {
                String message = plugin.getMessageConfig().getCountdownMessage(time);
                plugin.getServer().broadcastMessage(message);
            }
    }
}
