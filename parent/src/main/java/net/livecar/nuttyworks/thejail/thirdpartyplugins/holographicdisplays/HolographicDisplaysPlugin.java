package net.livecar.nuttyworks.thejail.thirdpartyplugins.holographicdisplays;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.MISSIONTYPE;
import net.livecar.nuttyworks.thejail.playerdata.PlayerScore;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class HolographicDisplaysPlugin {

    private TheJail_Plugin pluginRef = null;

    public HolographicDisplaysPlugin(TheJail_Plugin plugin) {
        pluginRef = plugin;
    }

    public void fullRegister()
    {
        //function is for added functionality in the future.
        scoresOnlyRegister();
    }

    public void scoresOnlyRegister() {
        List<String> missionTypes = new ArrayList();
        for (MISSIONTYPE missiontype : MISSIONTYPE.values()) {
            if (missiontype != MISSIONTYPE.NONE || missiontype != MISSIONTYPE.RETURN) {
                missionTypes.add(missiontype.toString().toLowerCase());
            }
        }
        missionTypes.add("all");
        missionTypes.add("courier");
        for (String mission : missionTypes) {
            for (String interval : new String[]{"day", "week", "month", "year", "alltime"}) {
                for (Integer rankNumber = 1; rankNumber < 20; rankNumber++) {
                    for (String function : new String[]{"name", "date", "score"}) {
                        HologramsAPI.registerPlaceholder(pluginRef, "{nptj_" + mission.toLowerCase() + "_" + interval + "_" + rankNumber + "_" + function + "}", 5, new HolographicReplacer(this, mission, interval, rankNumber, function));
                    }
                }
            }
        }
    }

    public String processPlaceHolder(String mission, String interval, Integer rankNumber, String function)
    {
        int days = 1;
        switch (interval)
        {
            case "day":
                days = 1;
                break;
            case "week":
                days = 7;
                break;
            case "month":
                days = 30;
                break;
            case "year":
                days = 365;
                break;
            case "alltime":
                days = 9999;
                break;
            default:
                return "";
        }

        LinkedHashMap<UUID, PlayerScore> scores = pluginRef.getPlayerScores.getTopScores(mission.toLowerCase() + "_" + days);
        if (scores.size() >= rankNumber)
        {
            List<PlayerScore> plrScores = new ArrayList<PlayerScore>(scores.values());
            if (function.equalsIgnoreCase("name"))
                return plrScores.get(rankNumber-1).getUserName();
            if (function.equalsIgnoreCase("score"))
                return formattedScore(plrScores.get(rankNumber-1).score);
            if (function.equalsIgnoreCase("date"))
                return plrScores.get(rankNumber-1).lastDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
        }

        return "";
    }


    private String formattedScore(Long score)
    {
        long timeSeconds = score % 60;
        long timeMinutes = (score % 3600) / 60;
        long timeHours = score / 3600;

        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", timeHours, timeMinutes, timeSeconds);
    }
}
