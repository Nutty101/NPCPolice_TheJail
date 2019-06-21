package net.livecar.nuttyworks.thejail.database;

import net.livecar.nuttyworks.thejail.enumerations.REQUESTTYPE;
import net.livecar.nuttyworks.thejail.playerdata.PlayerScore;

import java.util.LinkedHashMap;
import java.util.UUID;

public class DatabaseQueuedRequest {

    private PlayerScore scoreRecord;
    private REQUESTTYPE requestType;
    private Integer     interval    = 99999;
    private String      missionType = "";
    private LinkedHashMap<UUID, PlayerScore> scoreList;

    public DatabaseQueuedRequest(REQUESTTYPE requestType) {
        this.requestType = requestType;
    }

    public DatabaseQueuedRequest(REQUESTTYPE requestType, Integer lastDays) {
        this.requestType = requestType;
        this.interval    = lastDays;
    }

    public DatabaseQueuedRequest(REQUESTTYPE requestType, String missionType, Integer lastDays) {
        this.missionType = missionType;
        this.requestType = requestType;
        this.interval    = lastDays;
    }


    public DatabaseQueuedRequest(REQUESTTYPE requestType, PlayerScore scoreRecord) {
        this.requestType = requestType;
        this.scoreRecord = scoreRecord;
    }

    public DatabaseQueuedRequest(REQUESTTYPE requestType, String missionType, LinkedHashMap<UUID, PlayerScore> scoreResults) {
        this.missionType = missionType;
        this.requestType = requestType;
        this.scoreList = scoreResults;
    }

    public String getMissionType() { return this.missionType; }

    public PlayerScore getScoreRecord() { return this.scoreRecord;}

    public REQUESTTYPE getRequestType() {
        return requestType;
    }

    public Integer getInterval() { return this.interval; }

    public LinkedHashMap<UUID, PlayerScore> getScoreResults() {
        return this.scoreList;
    }

}
