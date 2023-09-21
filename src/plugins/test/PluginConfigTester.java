package plugins.test;

import plu.capstone.playerpiano.controller.plugin.Plugin;
import plu.capstone.playerpiano.controller.plugin.PluginConfig;

public class PluginConfigTester extends Plugin {

    enum TestEnum {
        TEST1,
        TEST2,
        TEST3
    }

    @Override
    public void setDefaultConfigValues() {
        config.setString("testString", "test");
        config.setBoolean("testBoolean", true);
        config.setInteger("testInteger", 1);
        config.setDouble("testDouble", 1.0);
        config.setBooleanList("testBooleanList", true, false, true);
        config.setIntegerList("testIntegerList", 1, 2, 3);
        config.setDoubleList("testDoubleList", 1.0, 2.0, 3.0);
        config.setStringList("testStringList", "test1", "test2", "test3");
        config.setEnum("testEnum", TestEnum.TEST3);

        PluginConfig nestedConfig = config.getNestedConfig("testNestedConfig1");
        nestedConfig.setString("testString", "test");

        PluginConfig nestedConfig2 = new PluginConfig(this);
        nestedConfig2.setString("testString", "test");
        config.setNestedConfig("testNestedConfig2", nestedConfig2);

    }

    @Override
    protected void onEnable() {

        logger.info("testString - no default value: " + config.getString("testString"));
        logger.info("testBoolean - no default value: " + config.getBoolean("testBoolean"));
        logger.info("testInteger - no default value: " + config.getInteger("testInteger"));
        logger.info("testDouble - no default value: " + config.getDouble("testDouble"));
        logger.info("testBooleanList - no default value: " + config.getBooleanList("testBooleanList"));
        logger.info("testIntegerList - no default value: " + config.getIntegerList("testIntegerList"));
        logger.info("testDoubleList - no default value: " + config.getDoubleList("testDoubleList"));
        logger.info("testStringList - no default value: " + config.getStringList("testStringList"));
        logger.info("testNestedConfig1 - no default value: " + config.getNestedConfig("testNestedConfig1"));
        logger.info("testNestedConfig2 - no default value: " + config.getNestedConfig("testNestedConfig2"));

        //logger.info("testEnum - default value: " + config.getEnum("testEnum", TestEnum.class, TestEnum.TEST1));
        logger.info("testEnum - no default value: " + config.getEnum("testEnum", TestEnum.class));

        logger.info("valueThatDoesNotExist - default value: " + config.getString("valueThatDoesNotExist", "default"));

        logger.info("entireConfig: " + config.toString());

        TestEnum test = null;

        //Read config values that are not set to default
    }
}
