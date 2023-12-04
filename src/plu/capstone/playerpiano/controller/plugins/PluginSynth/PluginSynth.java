package plu.capstone.playerpiano.controller.plugins.PluginSynth;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import plu.capstone.playerpiano.sheetmusic.Note;
import plu.capstone.playerpiano.controller.plugin.Plugin;

/**
 * This plugin is responsible for playing notes through the computer's speakers.
 */
public class PluginSynth extends Plugin {

    private static final int NUM_CHANNELS = 16;

    //each channel has its own synth so that multiple notes can be played at once without java dying
    private Synthesizer[] midiSynth = new Synthesizer[NUM_CHANNELS];

    /**
     * When the plugin is enabled, we need to open the synthesizers.
     * We also need to load the default soundbank so that we can play notes.
     * Each channel has its own synthesizer so that multiple notes can be played at once.
     */
    @Override
    public void onEnable() {
        try {
            for(int i = 0; i < NUM_CHANNELS; i++) {
                midiSynth[i] = MidiSystem.getSynthesizer();
                midiSynth[i].open();
                midiSynth[i].loadAllInstruments(midiSynth[i].getDefaultSoundbank());
            }
        }
        catch(MidiUnavailableException e) {
            logger.error("Failed to open MidiSynth. No sound will be played!", e);
        }
    }

    /**
     * Every time a note is played, we need to send it to the synthesizer.
     * Each channel has its own synthesizer so that multiple notes can be played at once.
     * @param note the note that was played
     * @param timestamp the timestamp of when the note was played
     */
    @Override
    public void onNotePlayed(Note note, long timestamp) {

        final Synthesizer synth = midiSynth[note.getChannelNum()];
        if(synth == null) {return;}
        final MidiChannel channel = synth.getChannels()[note.getChannelNum()];
        final int keyNum = note.getKeyNumber();
        final int velocity = note.getVelocity();

        if(note.isNoteOn()) {
            channel.noteOn(keyNum, velocity);
        }
        else {
            channel.noteOff(keyNum, velocity);
        }
    }
}
