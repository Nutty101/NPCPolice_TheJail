package net.livecar.nuttyworks.thejail.database;

import net.livecar.nuttyworks.thejail.playerdata.PlayerScore;

import java.util.LinkedHashMap;
import java.util.UUID;

public interface DatabaseInterface {
    void openDatabase();
    void closeConnections();
    boolean isSleeping();

    void logScore(PlayerScore plrScore);

    //Score results
    LinkedHashMap<UUID, PlayerScore> getScores(DatabaseQueuedRequest request);
}
