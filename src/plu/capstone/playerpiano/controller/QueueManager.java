package plu.capstone.playerpiano.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import lombok.Getter;
import plu.capstone.playerpiano.controller.plugin.Plugin;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusicCallback;
import plu.capstone.playerpiano.sheetmusic.events.Note;

//TODO: Move currentSheetMusic to QueueManager
public class QueueManager {

    private Logger logger = new Logger(this);
    private Queue<SheetMusic> songQueue = new LinkedList<>();
    @Getter
    private SheetMusic currentSheetMusic;

    private final PlayerPianoController controller;
    public QueueManager(PlayerPianoController playerPianoController) {
        controller = playerPianoController;
    }

    public void start() {
        new Thread(() -> {
            // Loop for queueing songs

            //We don't need to check the queue every millisecond, so we sleep for a second.
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            while(true) {
                if (currentSheetMusic != null) {
                    synchronized (currentSheetMusic) {
                        if (currentSheetMusic.isPlaying()) {
                            continue;
                        }
                    }
                }
                //If we are here, we are not playing a song.

                SheetMusic nextSong = null;

                synchronized (songQueue) {

                    if (songQueue.isEmpty()) {
                        continue;
                    }
                    logger.info("polling next song");
                    nextSong = songQueue.poll();
                }
                //Sanity check
                if (nextSong == null) {
                    logger.warning("Song queue returned null!");
                    continue;
                }

                logger.info("Playing song!");
                playSheetMusic(nextSong);
            }
        }, "Queue checker").start();
    }

    /**
     * Stops any currently playing music, and plays the given sheet music.
     * @param music The sheet music to play.
     */
    private void playSheetMusic(SheetMusic music) {

        //Stop the current sheet music if we are playing one
        this.stopSheetMusic();


        this.currentSheetMusic = music; //needs to be set before we can sync on it

        synchronized (currentSheetMusic) {
            for (Plugin plugin : controller.getPluginLoader().getPlugins()) {
                if (plugin.isEnabled()) {
                    currentSheetMusic.addCallback(plugin);
                }
            }

            // We use a new thread for this, so we don't hang the main thread
            // TODO: This should be properly handled by the plugin system & locking!!
            // TODO: It seems to work, but this change will most likely break things!
            new Thread(() -> {
                currentSheetMusic.play();
            }, "Sheet music playing thread").start();

        }

    }

    /**
     * Stops the current sheet music.
     */
    public void stopSheetMusic() {
        if(currentSheetMusic != null) {
            synchronized (currentSheetMusic) {
                currentSheetMusic.stop();
            }
        }
    }

    /**
     * Queues a song to be played.
     * @param song The song to queue.
     */
    public void queueSong(SheetMusic song) {
        synchronized (songQueue) {
            logger.info("Queuing song");
            songQueue.add(song);
        }
    }

    /**
     * Skips the current song.
     */
    public void skipSong() {
        logger.info("Skipping song");
        synchronized (currentSheetMusic) {
            if(currentSheetMusic != null) {
                logger.info("Skipping song 2");
                currentSheetMusic.stop();
            }
        }

        logger.info("Skipping song 3");
    }

    /**
     * @return true if the sheet music is currently playing.
     */
    public boolean isSheetMusicPlaying() {
        if(currentSheetMusic != null) {
            return currentSheetMusic.isPlaying();
        }
        return false;
    }





    //////////////////////////////////////
    /**
     * Plays multiple notes.
     * @param notes The notes to play live.
     */
    public void playNotes(List<Note> notes) {
        this.playNotes(notes, SheetMusicCallback.LIVE_TIMESTAMP);
    }

    /**
     * Plays multiple notes.
     * @param notes The notes to play.
     * @param timestamp The timestamp of the notes.
     *                  Use {@link plu.capstone.playerpiano.sheetmusic.SheetMusicCallback#LIVE_TIMESTAMP} for live playing, or {@link #playNotes(java.util.List)} for a shortcut
     */
    public void playNotes(List<Note> notes, long timestamp) {
        if(notes == null || notes.size() == 0) {
            logger.warning("Attempted to play null note!");
            return;
        }
        for(Plugin plugin : controller.getPluginLoader().getPlugins()) {
            if(plugin.isEnabled()) {
                plugin.onNotesPlayed(notes, timestamp);
            }
        }
    }

}
