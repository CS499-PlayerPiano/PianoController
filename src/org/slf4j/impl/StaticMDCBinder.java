package org.slf4j.impl;

import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.spi.MDCAdapter;

public class StaticMDCBinder {

    public static final StaticMDCBinder SINGLETON;

    private StaticMDCBinder() {
    }

    public MDCAdapter getMDCA() {
        return new BasicMDCAdapter();
    }

    public String getMDCAdapterClassStr() {
        return BasicMDCAdapter.class.getName();
    }

    static {
        SINGLETON = new StaticMDCBinder();
    }

}