package plu.capstone.playerpiano.subprogram;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import lombok.Getter;
import picocli.CommandLine.Command;
import plu.capstone.playerpiano.JsonConfigWrapper;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.outputs.Output;
import plu.capstone.playerpiano.outputs.logger.OutputLogger;
import plu.capstone.playerpiano.outputs.pianogui.OutputVirtualPianoGui;
import plu.capstone.playerpiano.outputs.synth.OutputSynth;
import plu.capstone.playerpiano.sheetmusic.SheetMusicCallback;

@Command
public abstract class SubProgram implements Callable<Integer> {

    @Getter
   private Set<Output> outputs = new HashSet<>();
    {
        outputs.add(new OutputLogger());
        outputs.add(new OutputSynth());
        outputs.add(new OutputVirtualPianoGui());
    }

    private final Logger logger = new Logger(this);

    public abstract String getSubCommand();

    @Override
    public final Integer call() {
        try {
            addProgramSpecificOutputPlugins(outputs);
            loadOutputPlugins();
            run();
            return 0;
        } catch(Throwable e) {
            e.printStackTrace();
            return 1;
        }
    }

    protected void addProgramSpecificOutputPlugins(Set<Output> outputs) {}

    public abstract void run() throws Exception;

    @Getter
    private JsonConfigWrapper outputConfig = new JsonConfigWrapper(new File("config/outputs.json"));

    private void loadOutputPlugins() {

        for(Output output : outputs) {
            JsonConfigWrapper config = outputConfig.getNestedConfig(output.getName());
            if(config.getBoolean("enabled", false)) {
                logger.info("Loading output: " + output.getName());
                output.load(config);
            }
            else {
                logger.info("Skipping disabled output: " + output.getName());
            }
        }

    }

}
