package plu.capstone.playerpiano.logger;

import org.slf4j.helpers.MarkerIgnoringBase;

public class SLF4JConverter extends MarkerIgnoringBase {

    private Logger ourLogger;

    SLF4JConverter(String name) {
        ourLogger = new Logger("SLF4J-" + name);
    }

    @Override
    public boolean isTraceEnabled() {return false;}

    @Override
    public void trace(String msg) {}

    @Override
    public void trace(String format, Object arg) {}

    @Override
    public void trace(String format, Object arg1, Object arg2) {}

    @Override
    public void trace(String format, Object... arguments) {}

    @Override
    public void trace(String msg, Throwable t) {}

    @Override
    public boolean isDebugEnabled() {return true;}

    @Override
    public void debug(String msg) {
        ourLogger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        ourLogger.debug(format.formatted(arg));
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        ourLogger.debug(format.formatted(arg1, arg2));
    }

    @Override
    public void debug(String format, Object... arguments) {
        ourLogger.debug(format.formatted(arguments));
    }

    @Override
    public void debug(String msg, Throwable t) {
        ourLogger.debug(msg, t);
    }

    @Override
    public boolean isInfoEnabled() {return true;}

    @Override
    public void info(String msg) {
        ourLogger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        ourLogger.info(format.formatted(arg));
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        ourLogger.info(format.formatted(arg1, arg2));
    }

    @Override
    public void info(String format, Object... arguments) {
        ourLogger.info(format.formatted(arguments));
    }

    @Override
    public void info(String msg, Throwable t) {
        ourLogger.info(msg, t);
    }

    @Override
    public boolean isWarnEnabled() {return true;}

    @Override
    public void warn(String msg) {
        ourLogger.warning(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        ourLogger.warning(format.formatted(arg));
    }

    @Override
    public void warn(String format, Object... arguments) {
        ourLogger.warning(format.formatted(arguments));
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        ourLogger.warning(format.formatted(arg1, arg2));
    }

    @Override
    public void warn(String msg, Throwable t) {
        ourLogger.warning(msg, t);
    }

    @Override
    public boolean isErrorEnabled() {return true;}

    @Override
    public void error(String msg) {
        ourLogger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        ourLogger.error(format.formatted(arg));
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        ourLogger.error(format.formatted(arg1, arg2));
    }

    @Override
    public void error(String format, Object... arguments) {
        ourLogger.error(format.formatted(arguments));
    }

    @Override
    public void error(String msg, Throwable t) {
        ourLogger.error(msg, t);
    }
}
