package plu.capstone.playerpiano.controller.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import plu.capstone.playerpiano.controller.logger.Logger;

public class PluginConfig {

    public interface DefaultConfig {
        void setDefaultValues();
    }

    private JsonObject underlyingConfig = new JsonObject();

    private final Plugin plugin;
    private final Logger logger;
    private final DefaultConfig defaultConfig;

    private final File CONFIG_FILE;

    public PluginConfig(Plugin plugin) {
        this.plugin = plugin;
        logger = new Logger(plugin.getName() + "-Config");
        defaultConfig = plugin::setDefaultConfigValues;
        CONFIG_FILE = new File("plugins/config/" + this.plugin.getName() + ".json");
    }

    private PluginConfig(Plugin plugin, JsonObject config) {
        this(plugin);
        underlyingConfig = config;
    }

    public void loadConfig() {
        if(!CONFIG_FILE.exists()) {
            logger.info("Creating default config file");
            underlyingConfig.addProperty("enabled", true);
            defaultConfig.setDefaultValues();
            saveConfig();
        }
        try {
            underlyingConfig = plugin.GSON.fromJson(new FileReader(CONFIG_FILE), JsonObject.class);
        } catch (FileNotFoundException e) {
            logger.error("Failed to load config file!", e);
        }
    }

    public void saveConfig() {
        if(!CONFIG_FILE.exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }
        String jsonString = plugin.GSON.toJson(underlyingConfig);
        try {
            Files.write(CONFIG_FILE.toPath(), jsonString.getBytes());
        }
        catch(Exception e) {
            logger.error("Failed to save config file!", e);
        }
    }

    private JsonElement getElementOrReturnNull(String key) {
        JsonElement element = underlyingConfig.get(key);
        if(element == null || element.isJsonNull()) {return null;}
        return element;
    }

    public void setString(String key, String value) {
        underlyingConfig.addProperty(key, value);
    }

    public String getString(String key) {return getString(key, null);}
    public String getString(String key, String defaultValue) {
        checkOrSetDefault(key, defaultValue);
        JsonElement element = getElementOrReturnNull(key);
        if(element == null) {return null;}
        return element.getAsString();
    }

    public void setStringList(String key, String... value) {
        underlyingConfig.add(key, plugin.GSON.toJsonTree(value));
    }

    public String[] getStringList(String key) {return getStringList(key, null);}
    public String[] getStringList(String key, String... defaultValue) {
        if(defaultValue == null) {defaultValue = new String[0];}
        checkOrSetDefault(key, defaultValue);
        return plugin.GSON.fromJson(underlyingConfig.get(key), String[].class);
    }

    public void setInteger(String key, int value) {
        underlyingConfig.addProperty(key, value);
    }

    public int getInteger(String key) {return getInteger(key, 0);}
    public int getInteger(String key, int defaultValue) {
        checkOrSetDefault(key, defaultValue);
        JsonElement element = getElementOrReturnNull(key);
        if(element == null) {return 0;}
        return element.getAsInt();
    }

    public void setIntegerList(String key, int... value) {
        underlyingConfig.add(key, plugin.GSON.toJsonTree(value));
    }

    public int[] getIntegerList(String key) {return getIntegerList(key, null);}
    public int[] getIntegerList(String key, int... defaultValue) {
        if(defaultValue == null) {defaultValue = new int[0];}
        checkOrSetDefault(key, defaultValue);
        return plugin.GSON.fromJson(underlyingConfig.get(key), int[].class);
    }

    ///////////
    public void setDouble(String key, double value) {
        underlyingConfig.addProperty(key, value);
    }

    public double getDouble(String key) {return getDouble(key, 0);}
    public double getDouble(String key, double defaultValue) {
        checkOrSetDefault(key, defaultValue);
        JsonElement element = getElementOrReturnNull(key);
        if(element == null) {return 0;}
        return element.getAsDouble();
    }

    public void setDoubleList(String key, double... value) {
        underlyingConfig.add(key, plugin.GSON.toJsonTree(value));
    }

    public double[] getDoubleList(String key) {return getDoubleList(key, null);}
    public double[] getDoubleList(String key, double... defaultValue) {
        if(defaultValue == null) {defaultValue = new double[0];}
        checkOrSetDefault(key, defaultValue);
        return plugin.GSON.fromJson(underlyingConfig.get(key), double[].class);
    }

    public void setBoolean(String key, Boolean value) {
        underlyingConfig.addProperty(key, value);
    }

    public boolean getBoolean(String key) {return getBoolean(key, false);}
    public boolean getBoolean(String key, boolean defaultValue) {
        checkOrSetDefault(key, defaultValue);
        JsonElement element = getElementOrReturnNull(key);
        if(element == null) {return false;}
        return element.getAsBoolean();
    }

    public void setBooleanList(String key, boolean... value) {
        underlyingConfig.add(key, plugin.GSON.toJsonTree(value));
    }

    public boolean[] getBooleanList(String key) {return getBooleanList(key, null);}
    public boolean[] getBooleanList(String key, boolean... defaultValue) {
        if(defaultValue == null) {defaultValue = new boolean[0];}
        checkOrSetDefault(key, defaultValue);
        return plugin.GSON.fromJson(underlyingConfig.get(key), boolean[].class);
    }

    public void setNestedConfig(String key, PluginConfig value) {
        underlyingConfig.add(key, value.underlyingConfig);
    }

    public PluginConfig getNestedConfig(String key) {
        return getNestedConfig(key, null);
    }
    public PluginConfig getNestedConfig(String key, PluginConfig defaultValue) {
        if(defaultValue == null) {
            defaultValue = new PluginConfig(plugin);
        }
        checkOrSetDefault(key, defaultValue);
        return new PluginConfig(plugin, underlyingConfig.getAsJsonObject(key));
    }

    public void setEnum(String key, Enum value) {
        underlyingConfig.addProperty(key, value.name());
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumType) {
        return getEnum(key, enumType, null);
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumType, T defaultValue) {
        checkOrSetDefault(key, defaultValue);
        try {
            String name = getString(key);
            if(name == null) {return defaultValue;}
            return Enum.valueOf(enumType, name);
        }
        catch(IllegalArgumentException e) {
            logger.error("Failed to parse enum value for key: " + key, e);
            return defaultValue;
        }
    }

    private void checkOrSetDefault(String key, Object obj) {
        if(!underlyingConfig.has(key)) {

            if(obj instanceof String) {
                this.setString(key, (String) obj);
            }
            else if(obj instanceof String[]) {
                this.setStringList(key, (String[]) obj);
            }
            else if(obj instanceof Integer) {
                this.setInteger(key, (int) obj);
            }
            else if(obj instanceof int[]) {
                this.setIntegerList(key, (int[]) obj);
            }
            else if(obj instanceof Double) {
                this.setDouble(key, (double) obj);
            }
            else if(obj instanceof double[]) {
                this.setDoubleList(key, (double[]) obj);
            }
            else if(obj instanceof Boolean) {
                this.setBoolean(key, (boolean) obj);
            }
            else if(obj instanceof boolean[]) {
                this.setBooleanList(key, (boolean[]) obj);
            }
            else if(obj instanceof PluginConfig) {
                this.setNestedConfig(key, (PluginConfig) obj);
            }
            else if(obj instanceof Enum) {
                this.setEnum(key, (Enum) obj);
            }
            else if(obj == null) {
                underlyingConfig.addProperty(key, (String)null);
            }
            else {
                logger.error("Unknown type for default value: " + obj.getClass().getName());
            }

            saveConfig();
        }
    }

    @Override
    public String toString() {
        return underlyingConfig.toString();
    }
}
