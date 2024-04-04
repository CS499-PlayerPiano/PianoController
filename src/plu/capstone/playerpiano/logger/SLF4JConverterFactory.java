package plu.capstone.playerpiano.logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class SLF4JConverterFactory  implements ILoggerFactory {

    ConcurrentMap<String, Logger> loggerMap;

    public SLF4JConverterFactory() {
        this.loggerMap = new ConcurrentHashMap<String, Logger>();
    }

    @Override
    public Logger getLogger(final String name) {
        final Logger simpleLogger = this.loggerMap.get(name);
        if (simpleLogger != null) {
            return simpleLogger;
        }
        final Logger newInstance = (Logger)new SLF4JConverter(name);
        final Logger oldInstance = this.loggerMap.putIfAbsent(name, newInstance);
        return (oldInstance == null) ? newInstance : oldInstance;
    }

    void reset() {
        this.loggerMap.clear();
    }

}
