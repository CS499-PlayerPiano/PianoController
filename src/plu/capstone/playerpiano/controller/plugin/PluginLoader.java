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
import plu.capstone.playerpiano.controller.logger.Logger;

public class PluginLoader {

    private final Logger logger = new Logger(this);

    @Getter
    private List<Plugin> plugins = new ArrayList<>();

    //Load all plugins from a package path
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

    public Plugin findPluginByName(String name) {
        for(Plugin plugin : plugins) {
            if(plugin.getName().equals(name)) {
                return plugin;
            }
        }
        return null;
    }

    public void findAllClassesUsingClassLoader(String packageName, List<Class> foundClasses) {

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
                    System.out.println("Not a class: " + line);
                    findAllClassesUsingClassLoader(line, foundClasses);
                }

            }

        }
        //Not a java file most likely
        catch(NullPointerException e) {}

    }

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
