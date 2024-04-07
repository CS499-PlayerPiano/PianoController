package plu.capstone.playerpiano.subprogram;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import lombok.Getter;
import picocli.CommandLine.Command;
import plu.capstone.playerpiano.JsonConfigWrapper;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.outputs.Output;
import plu.capstone.playerpiano.outputs.arduino.OutputArduino;
import plu.capstone.playerpiano.outputs.logger.OutputLogger;
import plu.capstone.playerpiano.outputs.pianogui.OutputVirtualPianoGui;
import plu.capstone.playerpiano.subprogram.mainserver.statisticsdb.OutputStatisticsDB;
import plu.capstone.playerpiano.outputs.synth.OutputSynth;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;

@Command
public abstract class SubProgram implements Callable<Integer> {

    //Special case due to sub programs needing custom functions
    protected final OutputArduino OUTPUT_ARDUINO = new OutputArduino();
    @Getter
   private Set<Output> outputs = new HashSet<>();
    {
        outputs.add(new OutputLogger());
        outputs.add(new OutputSynth());
        outputs.add(new OutputVirtualPianoGui());
        outputs.add(OUTPUT_ARDUINO);
        //outputs.add(new OutputSynthesiaGui()); //WIP
    }

    private Set<Output> subProgramSpecificOutputsTMP = new HashSet<>();

    private final Logger logger = new Logger(this);

    public abstract String getSubCommand();

    private final JsonConfigWrapper outputConfig = new JsonConfigWrapper(new File("config/outputs.json"));
    private final JsonConfigWrapper MASTER_SUB_PROGRAM_CONFIG = new JsonConfigWrapper(new File("config/programs.json"));

    @Getter
    private JsonConfigWrapper subProgramConfig;

    @Override
    public final Integer call() {
        try {
            MASTER_SUB_PROGRAM_CONFIG.loadConfig();
            subProgramConfig = MASTER_SUB_PROGRAM_CONFIG.getNestedConfig(getSubCommand());

            logger.info("Running sub program: " + getSubCommand());
            logger.debug("Sub program config: " + subProgramConfig.toString());

            outputConfig.loadConfig();
            addProgramSpecificOutputPlugins(subProgramSpecificOutputsTMP);
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



    private void loadOutputPlugins() {

        Set<Output> disabledOutputs = new HashSet<>();
        for(Output output : outputs) {
            JsonConfigWrapper config = outputConfig.getNestedConfig(output.getName());
            if(config.getBoolean("enabled", false)) {
                logger.info("Loading output: " + output.getName());
                output.load(config);
            }
            else {
                logger.info("Skipping disabled output: " + output.getName());
                disabledOutputs.add(output);
            }
        }

        for(Output output : subProgramSpecificOutputsTMP) {
            JsonConfigWrapper config = outputConfig.getNestedConfig(output.getName());
            logger.info("Loading program specific output: " + output.getName());
            output.load(config);
            outputs.add(output); //Add this custom output to the main list
        }

        //Avoid concurrent modification exception
        outputs.removeAll(disabledOutputs);

        //clear the temp set
        subProgramSpecificOutputsTMP.clear();

    }

    public void playNote(NoteEvent note) {
        for(Output output : outputs) {
            output.onNotePlayed(note, -1);
        }
    }

}
