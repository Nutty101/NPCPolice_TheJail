package net.livecar.nuttyworks.thejail.playerdata;

import org.bukkit.Bukkit;

import java.time.LocalDate;
import java.util.UUID;

public class PlayerScore {

    public UUID             playerID      = null;
    public Long             score         = 0L;
    public LocalDate        lastDate      = null;
    public String           missiontype   = "";

    public PlayerScore(UUID plrID, Long score, String missionType)
    {
        this.playerID   = plrID;
        this.score      = score;
        this.missiontype= missionType;
    }

    public String getUserName() {
        try {
            String name = Bukkit.getServer().getOfflinePlayer(playerID).getName();
            if (name == null)
                return "unknown";
            return name;
        } catch (Exception e) {
            return "unknown";
        }
    }

}
