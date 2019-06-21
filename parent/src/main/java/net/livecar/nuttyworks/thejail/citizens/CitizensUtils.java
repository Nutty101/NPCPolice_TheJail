package net.livecar.nuttyworks.thejail.citizens;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.thejail.TheJail_Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CitizensUtils {
    private TheJail_Plugin pluginRef = null;

    public CitizensUtils(TheJail_Plugin pluginRef) { this.pluginRef = pluginRef; }

    public List<Integer> getTargetNPCList()
    {
        List<Integer> targetNPCs = new ArrayList<>();

        for (Iterator<NPC> npcIter = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().iterator(); npcIter.hasNext(); ) {
            NPC tmpNPC = npcIter.next();
            if (tmpNPC.hasTrait(TheJailTargetTrait.class))
                targetNPCs.add(tmpNPC.getId());
        }

        return targetNPCs;
    }

    public NPC getFirstQuester()
    {
        for (Iterator<NPC> npcIter = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().iterator(); npcIter.hasNext(); ) {
            NPC tmpNPC = npcIter.next();
            if (tmpNPC.hasTrait(TheJailQuesterTrait.class))
                return tmpNPC;
        }
        return null;
    }


    public List<Integer> getQuesterNPCList()
    {
        List<Integer> questerNPCs = new ArrayList<>();

        for (Iterator<NPC> npcIter = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().iterator(); npcIter.hasNext(); ) {
            NPC tmpNPC = npcIter.next();
            if (tmpNPC.hasTrait(TheJailQuesterTrait.class))
                questerNPCs.add(tmpNPC.getId());
        }

        return questerNPCs;
    }

    public String getQuesterNPCName()
    {
        for (Iterator<NPC> npcIter = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().iterator(); npcIter.hasNext(); ) {
            NPC tmpNPC = npcIter.next();
            if (tmpNPC.hasTrait(TheJailQuesterTrait.class))
                return tmpNPC.getFullName();
        }

        return pluginRef.getDefaultConfig.getString("messages.setup.unconfigured");
    }

}
