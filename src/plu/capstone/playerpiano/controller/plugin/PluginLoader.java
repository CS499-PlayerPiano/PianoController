package plu.capstone.playerpiano.controller.plugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import plu.capstone.playerpiano.logger.Logger;

/**
 * A plugin management class. Currently only supports loading plugins from the PluginInstances enum
 *
 * TODO: Load jar plugins from a directory
 */
public class PluginLoader {

    private final Logger logger = new Logger(this);

    @Getter
    private List<Plugin> plugins = new ArrayList<>();

    /**
     * Loads all plugins from the instances enum
     */
    public void loadFromPluginEnum() {

        logger.debug("Plugin Classes in Enum: ");
        for(Class clazz : PluginInstances.getAllPlugins()) {

            Class superClass = clazz.getSuperclass();

            if(superClass != null && superClass == Plugin.class || superClass == PluginStateKeeper.class) {
                logger.debug("  - " + clazz.getSimpleName());

                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    plugins.add((Plugin) instance);

                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    logger.warning("Failed to initialize empty constructor for: " + clazz.getSimpleName());
                }

            }
            else {
                logger.error("  - " + clazz.getSimpleName() + " is not a plugin!");
            }

        }
    }

    /**
     * Finds a plugin by name
     * @param name The name of the plugin
     * @return The plugin, or null if not found
     */
    public Plugin findPluginByName(String name) {
        for(Plugin plugin : plugins) {
            if(plugin.getName().equals(name)) {
                return plugin;
            }
        }
        return null;
    }
}
