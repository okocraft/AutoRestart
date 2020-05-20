package net.okocraft.autorestart.timer;

import net.okocraft.autorestart.AutoRestartPlugin;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.jetbrains.annotations.NotNull;

public class BossBarTimer {

    private final AutoRestartPlugin plugin;

    private BossBar bar;
    private long time;
    private long remaining;

    public BossBarTimer(@NotNull AutoRestartPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(long time) throws IllegalArgumentException, IllegalStateException {
        if (time < 1) {
            throw new IllegalArgumentException("time must be 1 or more.");
        }

        if (isRunning()) {
            throw new IllegalStateException("BossBar timer is already running.");
        }

        this.time = time;
        remaining = time;
        bar = plugin.getServer().createBossBar(getTitle(), BarColor.RED, BarStyle.SEGMENTED_10);
        bar.setVisible(true);
    }

    public void update() throws IllegalStateException {
        if (!isRunning()) {
            throw new IllegalStateException("BossBar timer is not running.");
        }

        remaining--;

        bar.setProgress((double) remaining / time);
        bar.setTitle(getTitle());

        plugin.getServer().getOnlinePlayers().forEach(bar::addPlayer);
    }

    public void stop() throws IllegalStateException {
        if (!isRunning()) {
            throw new IllegalStateException("BossBar timer is not running.");
        }

        bar.setVisible(false);
        bar.removeAll();
        bar = null;

        time = 0;
        remaining = 0;
    }

    public boolean isRunning() {
        return bar != null;
    }

    @NotNull
    private String getTitle() {
        return plugin.getMessageConfig().getCountdownBarTitle(remaining);
    }
}
