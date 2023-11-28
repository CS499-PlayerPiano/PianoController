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
 * A plugin management class. Currently only supports loading plugins from a package path.
 *
 * TODO: Load jar plugins from a directory
 */
public class PluginLoader {

    private final Logger logger = new Logger(this);

    @Getter
    private List<Plugin> plugins = new ArrayList<>();

    /**
     * Loads all plugins from a package path.
     * @param pckge The package path to load from
     */
    public void loadFromPackage(String pckge) {

        List<Class> allClassesWeRecursivelyFound = new ArrayList<>();

        findAllClassesUsingClassLoader(pckge, allClassesWeRecursivelyFound);

        logger.debug("Found classes: ");
        for(Class clazz : allClassesWeRecursivelyFound) {

            Class superClass = clazz.getSuperclass();

            if(superClass != null && superClass == Plugin.class || superClass == PluginStateKeeper.class) {
                logger.debug("  - " + clazz.getSimpleName());

                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    plugins.add((Plugin) instance);

                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    logger.debug("Failed to initalize empty constructor for: " + clazz.getSimpleName());
                }

            }
            else {
                logger.debug("  - " + clazz.getSimpleName() + " is not a plugin!");
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

    /**
     * Finds all classes in a package path recursively
     * @param packageName The package path to search
     * @param foundClasses The list to add found classes to
     */
    private void findAllClassesUsingClassLoader(String packageName, List<Class> foundClasses) {

        try {
            InputStream stream = ClassLoader.getSystemClassLoader()
                    .getResourceAsStream(packageName.replaceAll("[.]", "/"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            Set<String> lines = reader.lines().collect(Collectors.toSet());

            for (String line : lines) {

                if (line.endsWith(".class")) {
                    //System.out.println("Found class: " + packageName + "." + line);
                    foundClasses.add(getClass(line, packageName));
                } else {
                    line = packageName + "." + line;
                    logger.debug("Not a class: " + line);
                    findAllClassesUsingClassLoader(line, foundClasses);
                }

            }

        }
        //Not a java file most likely
        catch(NullPointerException e) {}

    }

    /**
     * Gets a class from a package path
     * @param className The name of the class
     * @param packageName The package path
     * @return The class, or null if not found
     */
    private Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }

}
