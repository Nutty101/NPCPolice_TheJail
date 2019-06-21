package net.livecar.nuttyworks.thejail.database;

import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.MISSIONTYPE;
import net.livecar.nuttyworks.thejail.enumerations.REQUESTTYPE;
import net.livecar.nuttyworks.thejail.playerdata.PlayerScore;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DatabaseManager {

    private TheJail_Plugin pluginRef;

    private ArrayBlockingQueue<DatabaseQueuedRequest> processingRequests;
    private ArrayBlockingQueue<DatabaseQueuedRequest> returnedRequests;
    private Thread databaseThread;
    private DatabaseInterface getDatabaseManager;
    private int queueMonitorID = -1;

    public DatabaseManager(TheJail_Plugin plugin) {

        this.pluginRef = plugin;

        processingRequests = new ArrayBlockingQueue<>(500);
        returnedRequests = new ArrayBlockingQueue<>(500);

        // What datastorage does the config use
        switch (pluginRef.getDefaultConfig.getString("database.type", "sqlite")) {
            case "sqlite":
                getDatabaseManager = new DatabaseSqlLite(pluginRef, processingRequests, returnedRequests);
                databaseThread = new Thread((Runnable) this.getDatabaseManager);
                databaseThread.setName("NPC_Police-SQLite DB");
                break;
            case "mysql":
                getDatabaseManager = new DatabaseMySql(pluginRef, processingRequests, returnedRequests);
                databaseThread = new Thread((Runnable) this.getDatabaseManager);
                databaseThread.setName("NPC_Police-MySql DB");
                break;
            default:
                break;
        }
    }

    public boolean startDatabase() {
        if (this.databaseThread == null)
            return false;

        this.databaseThread.start();

        queueMonitorID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(
                pluginRef, () -> {
                    try {
                        processReturnQueue();
                    } catch (Exception e) {
                    }
                }, 30L, 5L
        );

        return true;
    }

    public boolean stopDatabase() {
        if (queueMonitorID != -1)
            Bukkit.getServer().getScheduler().cancelTask(queueMonitorID);

        if (this.databaseThread == null)
            return false;

        // This can pause the server (but it's closing down anyway, who cares)
        int loopCounter = 0;
        while (!processingRequests.isEmpty()) {
            this.databaseThread.interrupt();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            loopCounter++;
            if (loopCounter > 50)
                break;
        }

        loopCounter = 0;
        while (!this.getDatabaseManager.isSleeping()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            loopCounter++;
            if (loopCounter > 50)
                break;

        }

        this.getDatabaseManager.closeConnections();
        return true;
    }

    public void queueSaveScoreRequest(final PlayerScore plrRecord) {
        addProcessingRequestToQueue(new DatabaseQueuedRequest(REQUESTTYPE.LOG_SCORE, plrRecord));
    }

    public void requestUpdatedStats() {

        //Fire off a bunch of requests to populate the scores.
        for (MISSIONTYPE scoreType : MISSIONTYPE.values()) {
            if (scoreType != MISSIONTYPE.NONE && scoreType != MISSIONTYPE.RETURN) {
                for (Integer interval : new Integer[]{9999, 365, 7, 1}) {
                    addProcessingRequestToQueue(new DatabaseQueuedRequest(REQUESTTYPE.GET_SCORES, scoreType.toString(), interval));
                }
            }
        }

        //Combined types
        for (String scoreType : new String[]{"all","courier"}) {
            for (Integer interval : new Integer[]{9999, 365, 7, 1}) {
                addProcessingRequestToQueue(new DatabaseQueuedRequest(REQUESTTYPE.GET_SCORES, scoreType, interval));
            }
        }


    }

    private void addProcessingRequestToQueue(final DatabaseQueuedRequest queueRequest) {
        try {
            processingRequests.put(queueRequest);
            if (this.getDatabaseManager.isSleeping()) {
                this.databaseThread.interrupt();
            }
            return;
        } catch (InterruptedException e) {
            // Why would this get interrupted??
        }
    }


    private void processReturnQueue() {
        synchronized (returnedRequests) {
            if (returnedRequests.isEmpty()) {
                return;
            }

            if (returnedRequests != null) {
                while (!returnedRequests.isEmpty()) {

                    DatabaseQueuedRequest newRequest = null;

                    try {
                        newRequest = this.returnedRequests.take();
                    } catch (InterruptedException e) {
                    }

                    switch (newRequest.getRequestType()) {
                        case GET_SCORES:
                            pluginRef.getPlayerScores.setScoreSet(newRequest.getMissionType(), newRequest.getScoreResults());
                            break;
                        default:
                            break;

                    }

                }
            }
        }
    }
}



