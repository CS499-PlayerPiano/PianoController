package plu.capstone.playerpiano.plugins;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import plu.capstone.playerpiano.logger.Logger;

/**
 * A plugin management class. Currently only supports loading plugins from the PluginInstances enum
 *
 * TODO: Load jar plugins from a directory
 */
public class PluginLoader {

    private boolean hasLoaded = false;

    private final Logger logger = new Logger(this);

    @Getter
    private List<Plugin> plugins = new ArrayList<>();

    /**
     * Loads all plugins from the instances enum
     */
    public void loadFromPluginEnum() {

        if(hasLoaded) {
            logger.warning("Plugins have already been loaded!");
            return;
        }

        hasLoaded = true;

        logger.debug("Plugin Classes in Enum: ");
        for(Class clazz : PluginInstances.getAllPlugins()) {
            loadPlugin(clazz);
        }
    }

    public Plugin loadPlugin(Class<? extends Plugin> clazz) {
        Class superClass = clazz.getSuperclass();

        if(superClass != null && superClass == Plugin.class || superClass == PluginStateKeeper.class) {
            logger.debug("  - " + clazz.getSimpleName());

            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();

                plugins.add((Plugin) instance);
                return (Plugin)instance;

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                logger.warning("Failed to initialize empty constructor for: " + clazz.getSimpleName());
            }

        }
        else {
            logger.error("  - " + clazz.getSimpleName() + " is not a plugin!");
        }
        return null;
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
