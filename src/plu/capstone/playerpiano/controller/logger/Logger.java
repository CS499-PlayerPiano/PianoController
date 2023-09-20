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

public class Logger {

    private final String name;

    private boolean debugEnabled = true;

    public Logger(Object obj) {
        this(obj.getClass());
    }

    public Logger(Class<?> clazz) {
        this(clazz.getSimpleName());
    }

    public Logger(String name) {
        this.name = name;
    }

    public void debug(String msg) {
        if(!debugEnabled) {return;}
        this.print(ConsoleColors.BLACK_BRIGHT, "Debug", ConsoleColors.BLACK_BRIGHT, msg, false);
    }

    public void info(String msg) {
        this.print(ConsoleColors.GREEN_BRIGHT, "Info", ConsoleColors.RESET, msg, false);
    }

    public void warning(String msg) {
        this.print(ConsoleColors.YELLOW_BRIGHT, "Warning", ConsoleColors.YELLOW_BRIGHT, msg, false);
    }

    public void error(String msg) {
        this.print(ConsoleColors.RED_BRIGHT, "Error", ConsoleColors.RED_BRIGHT, msg, true);
    }

    public void error(String msg, Throwable t) {
        this.print(ConsoleColors.RED_BRIGHT, "Error", ConsoleColors.RED_BRIGHT, msg, true);
        this.print(ConsoleColors.RED_BRIGHT, "Error", ConsoleColors.RED_BRIGHT, fromThrowable(t), true);
    }

    @SuppressWarnings("resource")
    private void print(String prefixColor, String prefix, String msgColor, String msg, boolean error) {



        //https://stackoverflow.com/questions/18584809/java-system-out-effect-on-performance
        try {
            FileDescriptor fd = error ? FileDescriptor.err : FileDescriptor.out;
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fd), "UTF-8"), 4096 ));
            out.println(ConsoleColors.BLUE + "[" + name + "] " + prefixColor + "[" + prefix + "] "  + msgColor + msg + ConsoleColors.RESET);
            out.flush();

        } catch (UnsupportedEncodingException e) {
            final PrintStream out = error ? System.err : System.out;
            out.println(ConsoleColors.BLUE + "[" + name + "] " + prefixColor + "[" + prefix + "] "  + msgColor + msg + ConsoleColors.RESET);
        }
    }

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
