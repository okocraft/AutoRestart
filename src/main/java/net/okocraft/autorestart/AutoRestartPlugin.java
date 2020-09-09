package net.okocraft.autorestart;

import net.okocraft.autorestart.command.AutoRestartCommand;
import net.okocraft.autorestart.config.GeneralConfig;
import net.okocraft.autorestart.config.MessageConfig;
import net.okocraft.autorestart.tasks.CountdownTask;
import net.okocraft.autorestart.timer.BossBarTimer;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AutoRestartPlugin extends JavaPlugin {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "AutoRestart-Scheduler"));
    private final BossBarTimer timer = new BossBarTimer(this);
    private final Set<ScheduledFuture<?>> tasks = new HashSet<>();

    private GeneralConfig generalConfig;
    private MessageConfig messageConfig;
    private LocalDateTime restartTime;
    private String restartReason;

    @Override
    public void onLoad() {
        long startTime = getTimeMillis();

        generalConfig = new GeneralConfig(this);
        messageConfig = new MessageConfig(this);
        getLogger().info("Loaded config.yml and messages.yml");

        getLogger().info("Loaded plugin in " + (getTimeMillis() - startTime) + "ms.");
    }

    @Override
    public void onEnable() {
        long startTime = getTimeMillis();

        scheduleRestarting();

        Optional.ofNullable(getCommand("autorestart")).ifPresent(this::registerCommand);

        getLogger().info("Enabled plugin in " + (getTimeMillis() - startTime) + "ms.");
    }

    @Override
    public void onDisable() {
        long startTime = getTimeMillis();

        cancelAllTask();

        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        getLogger().info("Disabled plugin in " + (getTimeMillis() - startTime) + "ms.");
    }

    public void reload() {
        checkRunning();

        long startTime = getTimeMillis();
        getLogger().info("Reloading...");

        cancelAllTask();

        generalConfig.reload();
        getLogger().info("config.yml was reloaded.");

        messageConfig.reload();
        getLogger().info("message.yml was reloaded.");

        scheduleRestarting();

        getLogger().info("Reloaded plugin in " + (getTimeMillis() - startTime) + "ms.");
    }

    @NotNull
    public GeneralConfig getGeneralConfig() {
        checkRunning();
        return generalConfig;
    }

    @NotNull
    public MessageConfig getMessageConfig() {
        checkRunning();
        return messageConfig;
    }

    public BossBarTimer getTimer() {
        checkRunning();
        return timer;
    }

    public void checkRunning() {
        if (getServer().getPluginManager().getPlugin("AutoRestart") == null) {
            throw new IllegalStateException("AutoRestart is not enabled.");
        }
    }

    public void scheduleRestarting(long seconds, @Nullable String reason) {
        cancelAllTask();

        CountdownTask task = new CountdownTask(this, seconds);

        scheduleTask(task, 0L);

        restartTime = LocalDateTime.now().plusSeconds(seconds);
        restartReason = reason;

        getLogger().info("Restart scheduled: " + getRestartTimeAsString());
    }

    public void scheduleRestarting() {
        cancelAllTask();
        restartTime = generalConfig.getNextAutoRestartTime();
        restartReason = messageConfig.getScheduledRestartReason();

        if (restartTime == null) {
            getLogger().info("Auto restart is not scheduled.");
        } else {
            Duration duration = Duration.between(LocalDateTime.now(), restartTime);

            long noticeTime = Math.min(duration.getSeconds(), generalConfig.getDefaultNoticeTime());

            CountdownTask task = new CountdownTask(this, noticeTime);
            scheduleTask(task, duration.getSeconds());

            getLogger().info("Auto restart scheduled: " + getRestartTimeAsString());
        }
    }

    @NotNull
    public String getRestartTimeAsString() {
        if (restartTime == null) {
            return "";
        } else {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(restartTime.withNano(0)).replace("T", " ");
        }
    }

    @NotNull
    public String getFormattedRestartReason() {
        if (restartReason != null) {
            return messageConfig.getFormattedReason(restartReason);
        } else {
            return "";
        }
    }

    public void scheduleTask(@NotNull Runnable task, long seconds) {
        tasks.add(scheduler.schedule(task, seconds, TimeUnit.SECONDS));
    }

    public void cancelAllTask() {
        if (0 < tasks.size()) {
            tasks.forEach(this::cancelTask);

            tasks.clear();

            if (timer.isRunning()) {
                timer.stop();
            }

            restartTime = null;
            restartReason = null;

            getLogger().info("Restart task was cancelled.");
        }
    }

    private void registerCommand(@NotNull PluginCommand command) {
        command.setExecutor(new AutoRestartCommand(this));
    }

    private void cancelTask(@NotNull ScheduledFuture<?> task) {
        if (!task.isDone() && !task.isCancelled()) {
            task.cancel(true);
        }
    }

    private long getTimeMillis() {
        return System.currentTimeMillis();
    }
}
