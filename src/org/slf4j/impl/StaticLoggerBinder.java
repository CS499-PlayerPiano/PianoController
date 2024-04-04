package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;
import plu.capstone.playerpiano.logger.SLF4JConverterFactory;

public class StaticLoggerBinder implements LoggerFactoryBinder {

    private static StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return new SLF4JConverterFactory();
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return SLF4JConverterFactory.class.getName();
    }

    public static StaticLoggerBinder getSingleton() {
        return StaticLoggerBinder.SINGLETON;
    }


}
