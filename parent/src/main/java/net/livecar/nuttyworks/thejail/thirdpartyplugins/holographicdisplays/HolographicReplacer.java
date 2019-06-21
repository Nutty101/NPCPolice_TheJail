package net.livecar.nuttyworks.thejail.thirdpartyplugins.holographicdisplays;

import com.gmail.filoghost.holographicdisplays.api.placeholder.PlaceholderReplacer;
import net.livecar.nuttyworks.thejail.enumerations.MISSIONTYPE;

public class HolographicReplacer implements PlaceholderReplacer {

    private String          mission;
    private String          interval;
    private Integer         rankNumber;
    private String          function;

    private HolographicDisplaysPlugin classRef;

    public HolographicReplacer(HolographicDisplaysPlugin reference, String mission, String interval, Integer rankNumber, String function) {
        this.mission = mission;
        this.interval = interval;
        this.rankNumber = rankNumber;
        this.function = function;
        this.classRef = reference;
    }

    @Override
    public String update() {
        try {
            return classRef.processPlaceHolder(this.mission, this.interval, this.rankNumber, this.function);
        } catch (Exception err)
        {
            return "";
        }
    }
}
