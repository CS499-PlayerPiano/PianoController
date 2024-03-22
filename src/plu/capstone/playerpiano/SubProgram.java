package plu.capstone.playerpiano;

import com.google.gson.JsonObject;
import java.io.File;
import lombok.Getter;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.outputs.Output;
import plu.capstone.playerpiano.outputs.logger.OutputLogger;

public class SubProgram {

    private final Logger logger = new Logger(this);

    private static final Output[] OUTPUTS = {
        new OutputLogger()
    };

    @Getter
    private JsonConfigWrapper outputConfig = new JsonConfigWrapper(new File("config/outputs.json"));

    public void loadOutputPlugins() {

        for(Output output : OUTPUTS) {
            logger.info("Loading output: " + output.getName());
            JsonConfigWrapper config = outputConfig.getNestedConfig(output.getName());
            output.load(config);
        }

    }

}
