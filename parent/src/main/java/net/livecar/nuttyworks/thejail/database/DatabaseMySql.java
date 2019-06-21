package net.livecar.nuttyworks.thejail.database;

import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.REQUESTTYPE;
import net.livecar.nuttyworks.thejail.playerdata.PlayerScore;
import org.bukkit.Bukkit;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;

public class DatabaseMySql extends Thread implements DatabaseInterface {

    private Connection dbConnection = null;
    private ArrayBlockingQueue<DatabaseQueuedRequest> processingRequests;
    private ArrayBlockingQueue<DatabaseQueuedRequest> returnedRequests;
    private boolean sleeping = false;
    private TheJail_Plugin getStorageReference = null;

    private String dbHost;
    private String dbPort;
    private String dbName;
    private String dbLogin;
    private String dbPass;
    private boolean dbSSL;
    private String dbTablePrefix;

    public DatabaseMySql(TheJail_Plugin pluginRef, ArrayBlockingQueue<DatabaseQueuedRequest> processQueue, ArrayBlockingQueue<DatabaseQueuedRequest> resultQueue) {
        processingRequests = processQueue;
        returnedRequests = resultQueue;
        getStorageReference = pluginRef;

        dbHost = getStorageReference.getDefaultConfig.getString("database.host");
        dbPort = getStorageReference.getDefaultConfig.getString("database.port");
        dbName = getStorageReference.getDefaultConfig.getString("database.name");
        dbLogin = getStorageReference.getDefaultConfig.getString("database.login");
        dbPass = getStorageReference.getDefaultConfig.getString("database.password");
        dbSSL = getStorageReference.getDefaultConfig.getBoolean("database.usessl",false);
        dbTablePrefix = getStorageReference.getDefaultConfig.getString("database.table_prefix");
    }

    @Override
    public boolean isSleeping() {
        return sleeping;
    }

    public void closeConnections() {
        close(dbConnection);
        dbConnection = null;
    }

    @Override
    public void openDatabase() {
        if (dbConnection == null) {
            try {
                dbConnection = DriverManager.getConnection("jdbc:mysql://" + this.dbHost + ":" + dbPort + "/" + dbName + (dbSSL?"?useSSL=true":"?useSSL=false"), dbLogin, dbPass);
                try (Statement sqlStatement = dbConnection.createStatement()) {
                    sqlStatement.setQueryTimeout(2);
                    try (ResultSet results = sqlStatement.executeQuery("SHOW TABLES LIKE '" + dbTablePrefix + "_scores'")) {
                        if (!results.isBeforeFirst()) {
                            try (Statement sqlStmtInsert = dbConnection.createStatement()) {
                                sqlStmtInsert.setQueryTimeout(30);
                                sqlStmtInsert.executeUpdate("Create table " + dbTablePrefix + "_scores (player_id varchar(40),mission_type varchar(25), score bigint, last_recorded datetime)");
                                close(sqlStmtInsert);
                            }
                        }
                    }
                    close(sqlStatement);
                }
            } catch (SQLException e1) {
                getStorageReference.getServer().getConsoleSender().sendMessage("Database Issue: " + e1.getMessage());
            }
        } else {
            if (!this.isDbConnected()) {
                try {
                    dbConnection = DriverManager.getConnection("jdbc:mysql://" + this.dbHost + ":" + dbPort + "/" + dbName, dbLogin, dbPass);
                } catch (SQLException e) {
                    getStorageReference.getServer().getConsoleSender().sendMessage("Database Issue: " + e.getMessage());
                }
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
            if (processingRequests == null) {
                Bukkit.getServer().getLogger().log(Level.INFO, "Database Thread Exiting");
                return;
            }
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
        if (!this.isDbConnected()) {
            try {
                dbConnection = DriverManager.getConnection("jdbc:mysql://" + this.dbHost + ":" + dbPort + "/" + dbName + (dbSSL?"?useSSL=true":"?useSSL=false"), dbLogin, dbPass);
            } catch (SQLException e) {
                // Problems!
                Bukkit.getServer().getLogger().log(Level.SEVERE, "[TheJail] Failure creating database connection");
                return;
            }
        }
        try (Statement sqlStatement = dbConnection.createStatement()) {
            sqlStatement.setQueryTimeout(2);
            try (ResultSet results = sqlStatement.executeQuery("SELECT player_id FROM " + dbTablePrefix + "_scores WHERE player_id='" + playerData.playerID.toString() + "' and mission_type='" + playerData.missiontype + "'")) {
                if (!results.isBeforeFirst()) {
                    // Need to create a default record.
                    try (PreparedStatement preparedStatement = dbConnection.prepareStatement("INSERT INTO " + dbTablePrefix + "_scores (Player_id,mission_type,score,last_recorded) Values(?,?,?,?)")) {
                        preparedStatement.setString(1, playerData.playerID.toString());
                        preparedStatement.setString(2, playerData.missiontype);
                        preparedStatement.setString(3, "9999999999");
                        preparedStatement.setDate(4, new java.sql.Date(0));
                        preparedStatement.executeUpdate();
                        close(preparedStatement);
                    }
                }
                close(results);

                // Update
                try (PreparedStatement preparedStatement = dbConnection.prepareStatement("UPDATE " + dbTablePrefix + "_scores " + " SET score=?," + " last_recorded=?" + " WHERE player_id=? and mission_type=? and score > ?")) {
                    preparedStatement.setLong(1, playerData.score);
                    preparedStatement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                    preparedStatement.setString(3, playerData.playerID.toString());
                    preparedStatement.setString(4, playerData.missiontype);
                    preparedStatement.setDouble(5, playerData.score);
                    preparedStatement.executeUpdate();
                    close(preparedStatement);
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

        } catch (Exception err) {
            err.printStackTrace();
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

        if (!this.isDbConnected()) {
            try {
                dbConnection = DriverManager.getConnection("jdbc:mysql://" + this.dbHost + ":" + dbPort + "/" + dbName + (dbSSL?"?useSSL=true":"?useSSL=false"), dbLogin, dbPass);
            } catch (SQLException e) {
                // Problems!
                Bukkit.getServer().getLogger().log(Level.SEVERE, "[TheJail] Failure creating database connection");
                return null;
            }
        }

        Statement sqlStatement = null;
        ResultSet results = null;

        try {
            sqlStatement = dbConnection.createStatement();
            sqlStatement.setQueryTimeout(30);

            String query = "SELECT player_ID,score,last_recorded FROM " + dbTablePrefix + "_scores Where last_recorded >= ? Order by score Limit 30";

            if (request.getMissionType().equalsIgnoreCase("all")) {
                query = "SELECT player_ID,score,last_recorded FROM " + dbTablePrefix + "_scores Where last_recorded >= ? Order by score Limit 30";
            } else if (request.getMissionType().equalsIgnoreCase("courier")) {
                query = "SELECT player_ID,score,last_recorded FROM " + dbTablePrefix + "_scores Where (mission_type = 'import' or mission_type = 'export' ) and last_recorded >= ? Order by score Limit 30";
            }

            try (PreparedStatement preparedStatement = dbConnection.prepareStatement(query)) {
                preparedStatement.setDate(1, java.sql.Date.valueOf(minDate));
                results = preparedStatement.executeQuery();
                while (results.next()) {
                    PlayerScore score = new PlayerScore(UUID.fromString(results.getString("player_ID")), results.getLong("score"), request.getMissionType());
                    score.lastDate = results.getDate("last_recorded").toLocalDate();
                    score.missiontype = request.getMissionType() + "_" + request.getInterval();
                    if (!returnList.containsKey(score.playerID))
                        returnList.put(score.playerID, score);
                }
                close(results);
                close(preparedStatement);
                close(sqlStatement);
            }

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

        if (!this.isDbConnected()) {
            try {
                dbConnection = DriverManager.getConnection("jdbc:mysql://" + this.dbHost + ":" + dbPort + "/" + dbName + (dbSSL?"?useSSL=true":"?useSSL=false"), dbLogin, dbPass);
            } catch (SQLException e) {
                // Problems!
                Bukkit.getServer().getLogger().log(Level.SEVERE, "[TheJail] Failure creating database connection");
                return null;
            }
        }

        Statement sqlStatement = null;
        ResultSet results = null;

        try {
            sqlStatement = dbConnection.createStatement();
            sqlStatement.setQueryTimeout(30);

            try (PreparedStatement preparedStatement = dbConnection.prepareStatement("SELECT player_ID,score,last_recorded FROM " + dbTablePrefix + "_scores Where mission_type = ? and last_recorded >= ? Order by score Limit 30")) {
                preparedStatement.setString(1, request.getMissionType());
                preparedStatement.setDate(2, java.sql.Date.valueOf(minDate));
                results = preparedStatement.executeQuery();
                while (results.next()) {
                    PlayerScore score = new PlayerScore(UUID.fromString(results.getString("player_ID")), results.getLong("score"), request.getMissionType());
                    score.lastDate = results.getDate("last_recorded").toLocalDate();
                    score.missiontype = request.getMissionType() + "_" + request.getInterval();
                    if (!returnList.containsKey(score.playerID))
                        returnList.put(score.playerID, score);

                }
                close(results);
                close(preparedStatement);
                close(sqlStatement);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(results);
            close(sqlStatement);
        }
        return null;
    }


    private boolean isDbConnected() {
        try {
            if (dbConnection == null)
                return false;
            if (dbConnection.isValid(1))
                return true;
        } catch (SQLException e) {
        }
        return false;
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
            } catch (Exception excp) {
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
            try (ResultSet results = sqlStatement.executeQuery("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '" + this.dbName + "' AND TABLE_NAME = '" + tableName + "' AND COLUMN_NAME = '" + columnName + "'")) {
                while (results.next()) {
                    if (results.getString("COLUMN_NAME").equalsIgnoreCase(columnName)) {
                        close(results);
                        close(sqlStatement);
                        return true;
                    }

                }
            }
        } catch (Exception excp) {
            getStorageReference.getServer().getConsoleSender().sendMessage("Database Issue: " + excp);
        }
        return false;
    }
}
