package net.livecar.nuttyworks.thejail.thirdpartyplugins.placeholder;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import net.livecar.nuttyworks.thejail.enumerations.MISSIONTYPE;
import net.livecar.nuttyworks.thejail.playerdata.PlayerMission;
import net.livecar.nuttyworks.thejail.playerdata.PlayerScore;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class PlaceHolderPlugin extends PlaceholderExpansion {

	private TheJail_Plugin pluginRef = null;

	public PlaceHolderPlugin(TheJail_Plugin plugin) {
		pluginRef = plugin;
	}

    @Override
    public boolean canRegister() { return true; }

	@Override
	public String getIdentifier() {
		return "npj";
	}

	@Override
	public String getAuthor() {
		return "Sir_Nutty";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

    @Override
    public boolean persist(){return true;}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {

        try {
            if (identifier.toLowerCase().startsWith("user_"))
                return getUserPlaceholder(player, identifier.substring(5));
            if (identifier.toLowerCase().startsWith("top_"))
                return getTopPlaceholder(player, identifier.substring(4));
            return "INVALID";
        } catch (Exception err)
        {
            return "";
        }

	}

	private String getUserPlaceholder(Player player, String identifier)
    {

        if (pluginRef.onlyScores)
            return "";

        PlayerMission plrData = pluginRef.playerStorage.get(player.getUniqueId());

        if (identifier.equals("best")) {
            if (plrData == null)
                return "00:00:00";

            int totalSeconds = plrData.penalty;

            int timeSeconds = totalSeconds % 60;
            int timeMinutes = (totalSeconds % 3600) / 60;
            int timeHours = totalSeconds / 3600;

            return String.format(Locale.ENGLISH, "%02d:%02d:%02d", timeHours, timeMinutes, timeSeconds);
        }


        if (identifier.equals("penalty")) {
            if (plrData == null)
                return "00:00:00";

            int totalSeconds = plrData.penalty;

            int timeSeconds = totalSeconds % 60;
            int timeMinutes = (totalSeconds % 3600) / 60;
            int timeHours = totalSeconds / 3600;

            return String.format(Locale.ENGLISH, "%02d:%02d:%02d", timeHours, timeMinutes, timeSeconds);
        }

        if (identifier.equals("time")) {
            if (plrData == null)
                return "00:00:00";

            if (plrData.requestedAction == MISSIONTYPE.NONE)
                return "00:00:00";

            if (plrData.joinedTime == null)
                return "00:00:00";

            LocalDateTime curTime = LocalDateTime.now();
            LocalDateTime tmpTime = plrData.joinedTime;

            long timeHours = tmpTime.until(curTime, ChronoUnit.HOURS);
            tmpTime = tmpTime.plusHours(timeHours);

            long timeMinutes = tmpTime.until(curTime, ChronoUnit.MINUTES);
            tmpTime = tmpTime.plusMinutes(timeMinutes);

            long timeSeconds = tmpTime.until(curTime, ChronoUnit.SECONDS);

            return String.format(Locale.ENGLISH, "%02d:%02d:%02d", timeHours, timeMinutes, timeSeconds);
        }

        if (identifier.equals("target")) {

            if (plrData.targetNPC == 0) {
                return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(pluginRef.getMessageManager.getResultMessage("placeholder_strings.return")[0], pluginRef.getCitizensUtils.getQuesterNPCName()));
            }

            NPC target = CitizensAPI.getNPCRegistry().getById(plrData.targetNPC);
            if (target == null)
                return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(pluginRef.getMessageManager.getResultMessage("placeholder_strings.return")[0], pluginRef.getCitizensUtils.getQuesterNPCName()));

            switch (plrData.requestedAction) {
                case BEATUP:
                    return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(pluginRef.getMessageManager.getResultMessage("placeholder_strings.beatup")[0], target.getFullName()));
                case EXPORT:
                    return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(pluginRef.getMessageManager.getResultMessage("placeholder_strings.export")[0], target.getFullName()));
                case IMPORT:
                    return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(pluginRef.getMessageManager.getResultMessage("placeholder_strings.import")[0], target.getFullName()));
                case KILL:
                    return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(pluginRef.getMessageManager.getResultMessage("placeholder_strings.murder")[0], target.getFullName()));
                case RETURN:
                    return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(pluginRef.getMessageManager.getResultMessage("placeholder_strings.return")[0], pluginRef.getCitizensUtils.getQuesterNPCName()));
                default:
                    return "";
            }
        }

        return "???";
    }

    //Format = top_assault_week_1_score
    private String getTopPlaceholder(Player player, String identifier) {
        PlayerMission plrData = pluginRef.playerStorage.get(player.getUniqueId());

        String[] parts = identifier.split("_");

        if (parts.length != 4)
            return "ERROR";

        int interval = 1;
        int rank = 0;
        switch (parts[1])
        {
            case "day":
                interval = 1;
                break;
            case "week":
                interval = 7;
                break;
            case "month":
                interval = 30;
                break;
            case "year":
                interval = 365;
                break;
            case "alltime":
                interval = 9999;
                break;
        }

        if (StringUtils.isNumeric(parts[2]))
            rank = Integer.parseInt(parts[2])+1;
        else
            return "ERROR";

        LinkedHashMap<UUID, PlayerScore> scores = pluginRef.getPlayerScores.getTopScores(parts[0] + "_" + interval);
        if (scores.size() >= rank)
        {
            List<PlayerScore> plrScores = new ArrayList<PlayerScore>(scores.values());
            if (parts[3].equalsIgnoreCase("name"))
                return plrScores.get(rank).getUserName();
            if (parts[3].equalsIgnoreCase("score"))
                return formattedScore(plrScores.get(rank).score);
            if (parts[3].equalsIgnoreCase("date"))
                return plrScores.get(rank).lastDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
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


