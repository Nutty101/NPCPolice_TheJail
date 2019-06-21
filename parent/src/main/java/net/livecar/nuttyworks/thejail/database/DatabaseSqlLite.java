package net.livecar.nuttyworks.thejail.database;

import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.REQUESTTYPE;
import net.livecar.nuttyworks.thejail.playerdata.PlayerScore;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;

public class DatabaseSqlLite extends Thread implements DatabaseInterface {
    private Connection dbConnection = null;
    private File databaseFile;
    private ArrayBlockingQueue<DatabaseQueuedRequest> processingRequests;
    private ArrayBlockingQueue<DatabaseQueuedRequest> returnedRequests;
    private boolean sleeping = false;
    private TheJail_Plugin getStorageReference = null;

    public DatabaseSqlLite(TheJail_Plugin pluginRef, ArrayBlockingQueue<DatabaseQueuedRequest> processQueue, ArrayBlockingQueue<DatabaseQueuedRequest> resultQueue) {
        processingRequests = processQueue;
        returnedRequests = resultQueue;
        getStorageReference = pluginRef;
        databaseFile = new File(getStorageReference.getDataFolder().getAbsolutePath() + "/thejail.db");
    }

    @Override
    public boolean isSleeping() {
        return sleeping;
    }

    @Override
    public void closeConnections() {
        close(dbConnection);
        dbConnection = null;
    }

    @Override
    public void openDatabase() {
        if (!databaseFile.exists()) {
            // Create the database
            dbConnection = null;
            try {
                dbConnection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
                try (Statement sqlStatement = dbConnection.createStatement()) {
                    sqlStatement.setQueryTimeout(30);
                    sqlStatement.executeUpdate("Create table scores (player_id text, mission_type text, score numeric, last_recorded numeric)");
                    close(sqlStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }

        } else if (dbConnection == null) {
            try {
                dbConnection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        openDatabase();
        while (true) {
            try {
                // Process the queue if there are any requests pending and then
                // wait.
                sleeping = false;
                processQueue();
            } catch (InterruptedException e) {
                // Wakeup call or activity requested
            }
            if (processingRequests == null)
                return;
        }
    }

    private void processQueue() throws InterruptedException {
        synchronized (processingRequests) {
            if (processingRequests.isEmpty()) {
                sleeping = true;
                processingRequests.wait();
            }
            if (processingRequests != null) {
                while (!processingRequests.isEmpty()) {

                    DatabaseQueuedRequest newRequest = processingRequests.take();
                    switch (newRequest.getRequestType()) {
                        case LOG_SCORE:
                            logScore(newRequest.getScoreRecord());
                            break;
                        case GET_SCORES:
                            returnedRequests.put(new DatabaseQueuedRequest(REQUESTTYPE.GET_SCORES, newRequest.getMissionType() + "_" + newRequest.getInterval(), getScores(newRequest)));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void logScore(PlayerScore playerData) {
        if (dbConnection == null) {
            openDatabase();
        }
        if (dbConnection == null) {
            return;
        }

        try (Statement sqlStatement = dbConnection.createStatement()) {
            sqlStatement.setQueryTimeout(2);
            try (ResultSet results = sqlStatement.executeQuery("SELECT player_id FROM scores WHERE player_id='" + playerData.playerID.toString() + "' and mission_type='" + playerData.missiontype + "'")) {
                if (!results.isBeforeFirst()) {
                    // Need to create a default record.
                    try (PreparedStatement preparedStatement = dbConnection.prepareStatement("INSERT INTO scores (Player_id,mission_type,score,last_recorded) Values(?,?,?,?)")) {
                        preparedStatement.setString(1, playerData.playerID.toString());
                        preparedStatement.setString(2, playerData.missiontype);
                        preparedStatement.setString(3, "99999999");
                        preparedStatement.setDate(4, new java.sql.Date(0));
                        preparedStatement.executeUpdate();
                        close(preparedStatement);
                    }
                }
                close(results);

                // Update
                try (PreparedStatement preparedStatement = dbConnection.prepareStatement("UPDATE scores " + " SET score=?," + " last_recorded=?" + " WHERE player_id=? and mission_type=? and score > ?")) {
                    preparedStatement.setDouble(1, playerData.score);
                    preparedStatement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                    preparedStatement.setString(3, playerData.playerID.toString());
                    preparedStatement.setString(4, playerData.missiontype);
                    preparedStatement.setDouble(5, playerData.score);
                    preparedStatement.executeUpdate();
                    close(preparedStatement);
                }
            } catch (Exception err) {
            }

        } catch (Exception err) {
        }
    }


    @Override
    public LinkedHashMap<UUID, PlayerScore> getScores(DatabaseQueuedRequest request) {

        if (request.getMissionType().equalsIgnoreCase("all") || request.getMissionType().equalsIgnoreCase("courier"))
        {
            return getScoresCombined(request);
        } else {
            return getScoresSpecific(request);
        }
    }

    private LinkedHashMap<UUID, PlayerScore> getScoresCombined(DatabaseQueuedRequest request) {
        LocalDate minDate = LocalDate.from(LocalDateTime.now()).minusDays(request.getInterval());
        LinkedHashMap<UUID, PlayerScore> returnList = new LinkedHashMap<>();

        if (dbConnection == null) {
            openDatabase();
        }
        if (dbConnection == null) {
            return null;
        }

        Statement sqlStatement = null;
        ResultSet results = null;

        try {
            sqlStatement = dbConnection.createStatement();
            sqlStatement.setQueryTimeout(30);

            String query = "SELECT player_ID,score,last_recorded FROM scores Where mission_type = ? and last_recorded > ? Order by last_recorded desc Limit 30";

            if (request.getMissionType().equalsIgnoreCase("all")) {
                query = "SELECT player_ID,score,last_recorded FROM scores Where mission_type = ? and last_recorded > ? Order by last_recorded desc Limit 30";
            } else if (request.getMissionType().equalsIgnoreCase("courier")) {
                query = "SELECT player_ID,score,last_recorded FROM scores Where (mission_type = 'import' or mission_type = 'export' ) and last_recorded > ? Order by last_recorded desc Limit 30";
            }

            try (PreparedStatement preparedStatement = dbConnection.prepareStatement(query)) {
                preparedStatement.setString(1, request.getMissionType());
                preparedStatement.setDate(2, java.sql.Date.valueOf(minDate));
                results = preparedStatement.executeQuery();
                close(preparedStatement);
            }

            while (results.next()) {
                PlayerScore score = new PlayerScore(UUID.fromString(results.getString("player_ID")),  results.getLong("score"),request.getMissionType());
                score.lastDate = results.getDate("last_record").toLocalDate();
                returnList.put(score.playerID,score);
            }

            close(results);
            close(sqlStatement);
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(results);
            close(sqlStatement);
        }
        return null;
    }

    private LinkedHashMap<UUID, PlayerScore> getScoresSpecific(DatabaseQueuedRequest request) {

        LocalDate minDate = LocalDate.from(LocalDateTime.now()).minusDays(request.getInterval());
        LinkedHashMap<UUID, PlayerScore> returnList = new LinkedHashMap<>();

        if (dbConnection == null) {
            openDatabase();
        }
        if (dbConnection == null) {
            return null;
        }

        Statement sqlStatement = null;
        ResultSet results = null;

        try {
            sqlStatement = dbConnection.createStatement();
            sqlStatement.setQueryTimeout(30);

            try (PreparedStatement preparedStatement = dbConnection.prepareStatement("SELECT player_ID,score,last_recorded FROM scores Where mission_type = ? and last_recorded > ? Order by last_arrest desc Limit 30")) {
                preparedStatement.setString(1, request.getMissionType());
                preparedStatement.setDate(2, java.sql.Date.valueOf(minDate));
                results = preparedStatement.executeQuery();
                close(preparedStatement);
            }


            while (results.next()) {
                PlayerScore score = new PlayerScore(UUID.fromString(results.getString("player_ID")),  results.getLong("score"),request.getMissionType());
                score.lastDate = results.getDate("last_record").toLocalDate();
                returnList.put(score.playerID,score);
            }

            close(results);
            close(sqlStatement);
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(results);
            close(sqlStatement);
        }
        return null;
    }

    private void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception excp) {
                // Nothing
            }
        }
    }

    private void close(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (Exception excp) {
            }
        }
    }

    private void close(Connection cn) {
        if (cn != null) {
            try {
                cn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void close(Statement stmt) {
        if (stmt != null) {

            try {
                stmt.close();
            } catch (Exception excp) {
            }
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        if (dbConnection == null) {
            openDatabase();
        }
        if (dbConnection == null) {
            return false;
        }

        try (Statement sqlStatement = dbConnection.createStatement()) {
            sqlStatement.setQueryTimeout(1);
            try (ResultSet results = sqlStatement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
                while (results.next()) {
                    if (results.getString("name").equalsIgnoreCase(columnName)) {
                        close(results);
                        close(sqlStatement);
                        return true;
                    }

                }
            }
        } catch (Exception excp) {

        }
        return false;
    }
}
