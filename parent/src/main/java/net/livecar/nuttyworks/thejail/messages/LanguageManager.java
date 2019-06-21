package net.livecar.nuttyworks.thejail.messages;

import net.livecar.nuttyworks.thejail.TheJail_Plugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import net.livecar.nuttyworks.thejail.utilities.yamltools;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;

public class LanguageManager {

    public HashMap<String, FileConfiguration> languageStorage = new HashMap<String, FileConfiguration>();

    private TheJail_Plugin pluginRef = null;

    public LanguageManager(TheJail_Plugin pluginRef) {
        this.pluginRef = pluginRef;
    }

    public void loadLanguages() {
        loadLanguages(false);
    }

    public void loadLanguages(boolean silent) {
        if (languageStorage == null)
            languageStorage = new HashMap<String, FileConfiguration>();
        languageStorage.clear();

        File[] languageFiles = pluginRef.languagePath.listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(".yml");
                    }
                }
        );

        for (File ymlFile : languageFiles) {
            FileConfiguration oConfig = null;

            try {
                oConfig = yamltools.loadConfiguration(ymlFile);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }

            if (oConfig == null) {
                pluginRef.getMessageManager.logToConsole("Problem loading language file (" + ymlFile.getName().toLowerCase().replace(".yml", "") + ")");
            } else {
                languageStorage.put(ymlFile.getName().toLowerCase().replace(".yml", ""), oConfig);
                if (!silent) {

                }
            }
        }
    }
}
