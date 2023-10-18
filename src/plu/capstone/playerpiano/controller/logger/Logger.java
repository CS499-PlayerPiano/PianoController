package plu.capstone.playerpiano.controller.logger;

import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import lombok.Getter;
import lombok.Setter;

/**
 * A simple logger class that prints to the console, with some nice formatting.
 */
public class Logger {

    @Getter
    private final String name;

    @Setter
    private boolean debugEnabled = true;

    /**
     * Creates a new logger with the given name of the objects class
     * @param obj the object to get the class name from
     */
    public Logger(Object obj) {
        this(obj.getClass());
    }

    /**
     * Creates a new logger with the given class name
     * @param clazz the class to use for the name
     */
    public Logger(Class<?> clazz) {
        this(clazz.getSimpleName());
    }

    /**
     * Creates a new logger with the given name
     * @param name the name to use
     */
    public Logger(String name) {
        this.name = name;
    }

    /**
     * Creates a new logger with the given parent and child names
     * @param parent the parent
     * @param child the child
     */
    public Logger(Logger parent, String child) {
        this.name = parent.name + " - " + child;
    }

    /**
     * Prints a debug message to the console
     * @param msg the message to print
     */
    public void debug(String msg) {
        if(!debugEnabled) {return;}
        this.print(ConsoleColors.BLACK_BRIGHT, "Debug", ConsoleColors.BLACK_BRIGHT, msg, false);
    }

    /**
     * Prints an info message to the console
     * @param msg the message to print
     */
    public void info(String msg) {
        this.print(ConsoleColors.GREEN_BRIGHT, "Info", ConsoleColors.RESET, msg, false);
    }


    /**
     * Prints a warning message to the console
     * @param msg the message to print
     */
    public void warning(String msg) {
        this.print(ConsoleColors.YELLOW_BRIGHT, "Warning", ConsoleColors.YELLOW_BRIGHT, msg, false);
    }

    /**
     * Prints an error message to the console
     * @param msg the message to print
     */
    public void error(String msg) {
        this.print(ConsoleColors.RED_BRIGHT, "Error", ConsoleColors.RED_BRIGHT, msg, true);
    }

    /**
     * Prints an error message to the console with a stack trace
     * @param msg the message to print
     * @param t the stacktrace to also print
     */
    public void error(String msg, Throwable t) {
        this.print(ConsoleColors.RED_BRIGHT, "Error", ConsoleColors.RED_BRIGHT, msg, true);
        this.print(ConsoleColors.RED_BRIGHT, "Error", ConsoleColors.RED_BRIGHT, fromThrowable(t), true);
    }

    /**
     * Prints a message to the console
     * @param prefixColor the color of the prefix
     * @param prefix the prefix
     * @param msgColor the color of the message
     * @param msg the message
     * @param error if the message is an error and should be printed to stderr
     */
    @SuppressWarnings("resource")
    private void print(String prefixColor, String prefix, String msgColor, String msg, boolean error) {

        //https://stackoverflow.com/questions/18584809/java-system-out-effect-on-performance
        String x = ConsoleColors.BLUE + "[" + name + "] " + prefixColor + "[" + prefix + "] " + msgColor + msg + ConsoleColors.RESET;

        try {
            FileDescriptor fd = error ? FileDescriptor.err : FileDescriptor.out;
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fd), "UTF-8"), 4096 ));
            out.println(x);
            out.flush();

        } catch (UnsupportedEncodingException e) {
            final PrintStream out = error ? System.err : System.out;
            out.println(x);
        }
    }

    /**
     * Converts a throwable to a string
     * @param in the throwable to convert
     * @return the string
     */
    private static String fromThrowable(Throwable in) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        in.printStackTrace(pw);
        pw.flush();
        pw.close();
        sw.flush();
        final String result = sw.toString();
        try {
            sw.close();
        }
        catch (IOException e) {

        }

        return result;

    }

}
