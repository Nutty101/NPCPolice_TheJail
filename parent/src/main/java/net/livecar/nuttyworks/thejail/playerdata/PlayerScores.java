package net.livecar.nuttyworks.thejail.playerdata;

import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.playerdata.PlayerScore;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PlayerScores {

    private HashMap<String, LinkedHashMap<UUID, PlayerScore>> scoreSets = null;
    private TheJail_Plugin              pluginRef 	        = null;
    private ScheduledExecutorService    playerMonitorTask   = null;

    public PlayerScores(TheJail_Plugin plugin)
    {
        this.pluginRef = plugin;
        startMonitor();
    }

    public void startMonitor()
    {
        scoreSets = new HashMap<>();

        if (playerMonitorTask == null) {
            playerMonitorTask = Executors.newSingleThreadScheduledExecutor();
            Runnable task = () -> pluginRef.getDatabaseManager.requestUpdatedStats();
            playerMonitorTask.scheduleAtFixedRate(task, 10, 5, TimeUnit.SECONDS);
        }
    }

    public void stopMonitor()
    {
        if (playerMonitorTask != null) {
            playerMonitorTask.shutdown();
            playerMonitorTask = null;
        }
    }

    public void setScoreSet(String scoreType, LinkedHashMap<UUID, PlayerScore> scores)
    {
        if (scoreSets.containsKey(scoreType.toLowerCase()))
            scoreSets.remove(scoreType.toLowerCase());

        scoreSets.put(scoreType.toLowerCase(), scores);
    }

    public LinkedHashMap<UUID, PlayerScore> getTopScores(String taskType)
    {
        if (scoreSets.containsKey(taskType.toLowerCase()))
            return scoreSets.get(taskType.toLowerCase());

        return new LinkedHashMap<UUID, PlayerScore>();
    };


}
