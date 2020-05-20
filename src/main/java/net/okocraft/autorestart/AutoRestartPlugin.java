package net.okocraft.autorestart;

import net.okocraft.autorestart.command.AutoRestartCommand;
import net.okocraft.autorestart.config.GeneralConfig;
import net.okocraft.autorestart.config.MessageConfig;
import net.okocraft.autorestart.tasks.CountdownTask;
import net.okocraft.autorestart.timer.BossBarTimer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public void onLoad() {
        long start = getTimeMillis();

        generalConfig = new GeneralConfig(this);
        messageConfig = new MessageConfig(this);
        getLogger().info("Loaded config.yml and messages.yml");

        long finish = getTimeMillis();
        getLogger().info("Loaded plugin in " + (finish - start) + "ms.");
    }

    @Override
    public void onEnable() {
        long start = getTimeMillis();

        scheduleRestarting();
        Optional.ofNullable(getCommand("autorestart")).ifPresent(cmd -> cmd.setExecutor(new AutoRestartCommand(this)));

        long finish = getTimeMillis();
        getLogger().info("Enabled plugin in " + (finish - start) + "ms.");
    }

    @Override
    public void onDisable() {
        long start = getTimeMillis();

        cancelAllTask();

        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        long finish = getTimeMillis();
        getLogger().info("Disabled plugin in " + (finish - start) + "ms.");
    }

    public void reload() {
        checkRunning();

        long start = getTimeMillis();
        getLogger().info("Reloading...");

        cancelAllTask();

        generalConfig.reload();
        getLogger().info("config.yml was reloaded.");
        messageConfig.reload();
        getLogger().info("message.yml was reloaded.");

        scheduleRestarting();

        long finish = getTimeMillis();
        getLogger().info("Reloaded plugin in " + (finish - start) + "ms.");
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

    public void scheduleRestarting(long seconds) {
        cancelAllTask();
        CountdownTask task = new CountdownTask(this, seconds);
        scheduleTask(task, 0L);
        restartTime = LocalDateTime.now().plusSeconds(seconds);
        getLogger().info("Restart scheduled: " + getRestartTimeAsString());
    }

    public void scheduleRestarting() {
        cancelAllTask();
        restartTime = generalConfig.getNextAutoRestartTime();
        if (restartTime == null) {
            getLogger().info("Auto restart is not scheduled.");
        } else {
            long noticeTime = generalConfig.getDefaultNoticeTime();
            CountdownTask task = new CountdownTask(this, noticeTime);
            Duration duration = Duration.between(LocalDateTime.now(), restartTime.minusSeconds(noticeTime));

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

    public void scheduleTask(@NotNull Runnable task, long seconds) {
        tasks.add(scheduler.schedule(task, seconds, TimeUnit.SECONDS));
    }

    public void cancelAllTask() {
        tasks.forEach(this::cancelTask);
        tasks.clear();

        if (timer.isRunning()) {
            timer.stop();
        }

        restartTime = null;

        getLogger().info("Restart task was cancelled.");
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
