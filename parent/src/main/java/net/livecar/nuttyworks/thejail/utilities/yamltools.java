package net.livecar.nuttyworks.thejail.utilities;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class yamltools {
    public static YamlConfiguration loadConfiguration(File file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");

        YamlConfiguration config = new YamlConfiguration();

        InputStream inputStream = null;
        Reader inputStreamReader = null;

        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e2) {
            return null;
        }

        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e2) {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            return null;
        }

        try {
            config.load(inputStreamReader);
            inputStreamReader.close();
            inputStream.close();
            return config;
        } catch (IOException exIO){
            throw exIO;
        } catch (InvalidConfigurationException exIC) {
            throw exIC;
        }
    }
}
