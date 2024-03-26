package plu.capstone.playerpiano;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import plu.capstone.playerpiano.logger.Logger;

public class JsonConfigWrapper {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();

    private JsonObject underlyingConfig = new JsonObject();

    private final File CONFIG_FILE;
    private final Logger logger;

    /**
     * Creates a new config wrapper for JSON.
     * @param file The file to use for the config.
     */
    public JsonConfigWrapper(File file) {
        this(file, new JsonObject());
    }

    /**
     * Creates a new PluginConfig object for the given plugin with the given config.
     * @param existingJson The existing JSON object to use.
     */
    public JsonConfigWrapper(File file, JsonObject existingJson) {
        CONFIG_FILE = file;
        underlyingConfig = existingJson;
        if(!CONFIG_FILE.exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }
        logger = new Logger(file.getName());
    }

    /**
     * Loads the config from the config file, or creates a new one if it doesn't exist.
     */
    public void loadConfig() {
        if(!CONFIG_FILE.exists()) {
            logger.info("Creating default config file");
            saveConfig();
        }
        try {
            underlyingConfig = GSON.fromJson(new FileReader(CONFIG_FILE), JsonObject.class);
        } catch (FileNotFoundException e) {
            logger.error("Failed to load config file!", e);
        }
    }

    /**
     * Saves the config to the config file.
     */
    public void saveConfig() {
        if(!CONFIG_FILE.exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }
        String jsonString = GSON.toJson(underlyingConfig);
        try {
            Files.write(CONFIG_FILE.toPath(), jsonString.getBytes());
        }
        catch(Exception e) {
            logger.error("Failed to save config file!", e);
        }
    }

    /**
     * Small helper method to get an element from the config, or return null if it doesn't exist.
     * @param key The key to get the element for.
     * @return The element, or null if it doesn't exist.
     */
    private JsonElement getElementOrReturnNull(String key) {
        JsonElement element = underlyingConfig.get(key);
        if(element == null || element.isJsonNull()) {return null;}
        return element;
    }

    /**
     * Sets the given key to the given value.
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setString(String key, String value) {
        underlyingConfig.addProperty(key, value);
    }

    /**
     * Gets the value for the given key, or null if it doesn't exist.
     * @param key The key to get the value for.
     * @return The value, or null if it doesn't exist.
     */
    public String getString(String key) {return getString(key, null);}

    /**
     * Gets the value for the given key, or the given default value if it doesn't exist.
     * @param key The key to get the value for.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The value, or the default value if it doesn't exist.
     */
    public String getString(String key, String defaultValue) {
        checkOrSetDefault(key, defaultValue);
        JsonElement element = getElementOrReturnNull(key);
        if(element == null) {return null;}
        return element.getAsString();
    }

    /**
     * Sets the given key to the given value.
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setStringList(String key, String... value) {
        underlyingConfig.add(key, GSON.toJsonTree(value));
    }

    /**
     * Gets the value for the given key, or null if it doesn't exist.
     * @param key The key to get the value for.
     * @return The value, or null if it doesn't exist.
     */
    public String[] getStringList(String key) {return getStringList(key, null);}

    /**
     * Gets the value for the given key, or the given default value if it doesn't exist.
     * @param key The key to get the value for.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The value, or the default value if it doesn't exist.
     */
    public String[] getStringList(String key, String... defaultValue) {
        if(defaultValue == null) {defaultValue = new String[0];}
        checkOrSetDefault(key, defaultValue);
        return GSON.fromJson(underlyingConfig.get(key), String[].class);
    }

    /**
     * Sets the given key to the given value.
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setInteger(String key, int value) {
        underlyingConfig.addProperty(key, value);
    }

    /**
     * Gets the value for the given key, or 0 if it doesn't exist.
     * @param key The key to get the value for.
     * @return The value, or 0 if it doesn't exist.
     */
    public int getInteger(String key) {return getInteger(key, 0);}

    /**
     * Gets the value for the given key, or the given default value if it doesn't exist.
     * @param key The key to get the value for.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The value, or the default value if it doesn't exist.
     */
    public int getInteger(String key, int defaultValue) {
        checkOrSetDefault(key, defaultValue);
        JsonElement element = getElementOrReturnNull(key);
        if(element == null) {return 0;}
        return element.getAsInt();
    }

    /**
     * Sets the given key to the given value.
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setIntegerList(String key, int... value) {
        underlyingConfig.add(key, GSON.toJsonTree(value));
    }

    /**
     * Gets the value for the given key, or an empty array if it doesn't exist.
     * @param key The key to get the value for.
     * @return The value, or an empty array if it doesn't exist.
     */
    public int[] getIntegerList(String key) {return getIntegerList(key, null);}

    /**
     * Gets the value for the given key, or the given default value if it doesn't exist.
     * @param key The key to get the value for.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The value, or the default value if it doesn't exist.
     */
    public int[] getIntegerList(String key, int... defaultValue) {
        if(defaultValue == null) {defaultValue = new int[0];}
        checkOrSetDefault(key, defaultValue);
        return GSON.fromJson(underlyingConfig.get(key), int[].class);
    }

    /**
     * Sets the given key to the given value.
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setDouble(String key, double value) {
        underlyingConfig.addProperty(key, value);
    }

    /**
     * Gets the value for the given key, or 0 if it doesn't exist.
     * @param key The key to get the value for.
     * @return The value, or 0 if it doesn't exist.
     */
    public double getDouble(String key) {return getDouble(key, 0);}

    /**
     * Gets the value for the given key, or the given default value if it doesn't exist.
     * @param key The key to get the value for.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The value, or the default value if it doesn't exist.
     */
    public double getDouble(String key, double defaultValue) {
        checkOrSetDefault(key, defaultValue);
        JsonElement element = getElementOrReturnNull(key);
        if(element == null) {return 0;}
        return element.getAsDouble();
    }

    /**
     * Sets the given key to the given value.
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setDoubleList(String key, double... value) {
        underlyingConfig.add(key, GSON.toJsonTree(value));
    }

    /**
     * Gets the value for the given key, or an empty array if it doesn't exist.
     * @param key The key to get the value for.
     * @return The value, or an empty array if it doesn't exist.
     */
    public double[] getDoubleList(String key) {return getDoubleList(key, null);}

    /**
     * Gets the value for the given key, or the given default value if it doesn't exist.
     * @param key The key to get the value for.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The value, or the default value if it doesn't exist.
     */
    public double[] getDoubleList(String key, double... defaultValue) {
        if(defaultValue == null) {defaultValue = new double[0];}
        checkOrSetDefault(key, defaultValue);
        return GSON.fromJson(underlyingConfig.get(key), double[].class);
    }

    /**
     * Sets the given key to the given value.
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setBoolean(String key, Boolean value) {
        underlyingConfig.addProperty(key, value);
    }

    /**
     * Gets the value for the given key, or false if it doesn't exist.
     * @param key The key to get the value for.
     * @return The value, or false if it doesn't exist.
     */
    public boolean getBoolean(String key) {return getBoolean(key, false);}

    /**
     * Gets the value for the given key, or the given default value if it doesn't exist.
     * @param key The key to get the value for.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The value, or the default value if it doesn't exist.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        checkOrSetDefault(key, defaultValue);
        JsonElement element = getElementOrReturnNull(key);
        if(element == null) {return false;}
        return element.getAsBoolean();
    }

    /**
     * Sets the given key to the given value.
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setBooleanList(String key, boolean... value) {
        underlyingConfig.add(key, GSON.toJsonTree(value));
    }

    /**
     * Gets the value for the given key, or an empty array if it doesn't exist.
     * @param key The key to get the value for.
     * @return The value, or an empty array if it doesn't exist.
     */
    public boolean[] getBooleanList(String key) {return getBooleanList(key, null);}

    /**
     * Gets the value for the given key, or the given default value if it doesn't exist.
     * @param key The key to get the value for.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The value, or the default value if it doesn't exist.
     */
    public boolean[] getBooleanList(String key, boolean... defaultValue) {
        if(defaultValue == null) {defaultValue = new boolean[0];}
        checkOrSetDefault(key, defaultValue);
        return GSON.fromJson(underlyingConfig.get(key), boolean[].class);
    }

    /**
     * Sets the given key to the given value.
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setNestedConfig(String key, JsonConfigWrapper value) {
        underlyingConfig.add(key, value.underlyingConfig);
    }

    /**
     * Gets the value for the given key, or an empty config if it doesn't exist.
     * @param key The key to get the value for.
     * @return The value, or an empty config if it doesn't exist.
     */
    public JsonConfigWrapper getNestedConfig(String key) {
        return getNestedConfig(key, null);
    }

    /**
     * Gets the value for the given key, or the given default value if it doesn't exist.
     * @param key The key to get the value for.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The value, or the default value if it doesn't exist.
     */
    public JsonConfigWrapper getNestedConfig(String key, JsonConfigWrapper defaultValue) {
        if(defaultValue == null) {
            defaultValue = new JsonConfigWrapper(CONFIG_FILE);
        }
        checkOrSetDefault(key, defaultValue);
        return new JsonConfigWrapper(CONFIG_FILE, underlyingConfig.getAsJsonObject(key));
    }

    /**
     * Sets the given key to the given value.
     * @param key The key to set.
     * @param value The value to set.
     */
    public void setEnum(String key, Enum value) {
        underlyingConfig.addProperty(key, value.name());
    }

    /**
     * Gets the value for the given key, or null if it doesn't exist.
     * @param key The key to get the value for.
     * @param enumType The type of enum to parse.
     * @return The value, or null if it doesn't exist.
     * @param <T> The type of enum to parse.
     */
    public <T extends Enum<T>> T getEnum(String key, Class<T> enumType) {
        return getEnum(key, enumType, null);
    }

    /**
     * Gets the value for the given key, or the given default value if it doesn't exist.
     * @param key The key to get the value for.
     * @param enumType The type of enum to parse.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The value, or the default value if it doesn't exist.
     * @param <T> The type of enum to parse.
     */
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

    /**
     * Checks if the given key exists in the config. If it doesn't, it sets it to the given default value.
     * @param key The key to check.
     * @param obj The default value to set if the key doesn't exist.
     */
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
            else if(obj instanceof JsonConfigWrapper) {
                this.setNestedConfig(key, (JsonConfigWrapper) obj);
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

    /**
     * Returns a JSON string representation of the config.
     * @return A JSON string representation of the config.
     */
    @Override
    public String toString() {
        return underlyingConfig.toString();
    }
}
