package plugins;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import plu.capstone.playerpiano.controller.midi.Note;
import plu.capstone.playerpiano.controller.plugin.Plugin;

public class PluginSynth extends Plugin {

    private Synthesizer midiSynth;

    @Override
    public void onEnable() {
        setDisabled();
        try {
            midiSynth = MidiSystem.getSynthesizer();
            midiSynth.open();
            midiSynth.loadAllInstruments(midiSynth.getDefaultSoundbank());
        }
        catch(MidiUnavailableException e) {
            logger.error("Failed to open MidiSynth. No sound will be played!", e);
        }
    }

    @Override
    public void onNotePlayed(Note note, long timestamp) {
        if(midiSynth == null) {
            return;
        }

        MidiChannel[] mChannels = midiSynth.getChannels();
        if(note.isNoteOn()) {
            mChannels[note.getChannelNum()].noteOn(note.getKeyNumber(), note.getVelocity());
        }
        else {
            mChannels[note.getChannelNum()].noteOff(note.getKeyNumber(), note.getVelocity());
        }
    }
}
