package plu.capstone.playerpiano.controller.plugins.test;

import plu.capstone.playerpiano.controller.plugin.Plugin;

public class TestPlugin extends Plugin {

    @Override
    public void onEnable() {
       logger.info("TestPlugin loaded!");
    }
}
