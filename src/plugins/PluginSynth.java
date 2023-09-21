package plugins;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import plu.capstone.playerpiano.controller.midi.Note;
import plu.capstone.playerpiano.controller.plugin.Plugin;

public class PluginSynth extends Plugin {

    private static final int NUM_CHANNELS = 16;

    //each channel has its own synth so that multiple notes can be played at once without java dying
    private Synthesizer[] midiSynth = new Synthesizer[NUM_CHANNELS];

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

    @Override
    public void onNotePlayed(Note note, long timestamp) {

        Synthesizer synth = midiSynth[note.getChannelNum()];

        if(synth == null) {return;}

        final MidiChannel channel = synth.getChannels()[note.getChannelNum()];
        if(note.isNoteOn()) {
            channel.noteOn(note.getKeyNumber(), note.getVelocity());
        }
        else {
            channel.noteOff(note.getKeyNumber(), note.getVelocity());
        }
    }
}
