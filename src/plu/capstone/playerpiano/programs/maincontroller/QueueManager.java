package plu.capstone.playerpiano.programs.maincontroller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.plugins.Plugin;
import plu.capstone.playerpiano.plugins.impl.PluginWebAPI.PacketIds;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusicCallback;
import plu.capstone.playerpiano.sheetmusic.events.Note;

//TODO: Move currentSheetMusic to QueueManager
public class QueueManager {

    private final Logger logger = new Logger(this);
    private final Queue<QueuedSongWithMetadata> songQueue = new LinkedList<>();

    private QueuedSongWithMetadata currentSheetMusic;

    private final PlayerPianoController controller;
    public QueueManager(PlayerPianoController playerPianoController) {
        controller = playerPianoController;
    }

    public void start() {
        new Thread(() -> {

            while(true) {
                synchronized (songQueue) {

                    if(currentSheetMusic == null || currentSheetMusic.getSheetMusic() == null || !currentSheetMusic.getSheetMusic().isSheetMusicStillScrolling()) {
                        if(!songQueue.isEmpty()) {
                            stopOrSkipCurrentSong();
                            currentSheetMusic = songQueue.poll();
                            playSheetMusic(currentSheetMusic);
                            sendCurrentQueueAsWSPacket();
                        }
                        else {
                            currentSheetMusic = null;
                        }
                    }
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.error("Queue checker thread was interrupted!");
                }
            }

        }, "Queue checker").start();
    }

    public JsonObject getQueueAsJson() {
        JsonArray queueArray = new JsonArray();

        synchronized (songQueue) {
            for (QueuedSongWithMetadata song : songQueue) {
                queueArray.add(song.toJson());
            }
        }

        JsonObject obj = new JsonObject();
        obj.add("queue", queueArray);

        JsonObject nowPlaying = null;

        if (currentSheetMusic != null && currentSheetMusic.getSongDBEntry() != null) {
            synchronized (currentSheetMusic) {
                nowPlaying = currentSheetMusic.toJson();
            }
        }

        obj.add("nowPlaying", nowPlaying);

        return obj;
    }

    private void sendCurrentQueueAsWSPacket() {
        controller.sendWSPacket(PacketIds.QUEUE_UPDATED, getQueueAsJson());
    }

    /**
     * Stops any currently playing music, and plays the given sheet music.
     * @param music The sheet music to play.
     */
    private void playSheetMusic(QueuedSongWithMetadata music) {

        synchronized (currentSheetMusic) {
            for (Plugin plugin : controller.getPluginLoader().getPlugins()) {
                if (plugin.isEnabled()) {
                    currentSheetMusic.getSheetMusic().addCallback(plugin);
                }
            }

            // We use a new thread for this, so we don't hang the main thread
            new Thread(() -> {
                currentSheetMusic.getSheetMusic().play();
            }, "Sheet music playing thread").start();

        }

    }

    public void stopOrSkipCurrentSong() {
        if(currentSheetMusic != null && currentSheetMusic.getSheetMusic() != null) {
            synchronized (currentSheetMusic) {
                currentSheetMusic.getSheetMusic().stop();
            }
        }
    }

    /**
     * Queues a song to be played.
     * @param song The song to queue.
     * @return The position of the song in the queue. -1 if the song is already in the queue. -2 if the song is currently playing.
     */
    @Deprecated
    public int queueSong(SheetMusic song) throws QueueError {

        JsonObject obj = new JsonObject();
        obj.addProperty("name", "Queued through Deprecated method!");
        obj.add("artists", new JsonArray());
        obj.addProperty("artwork", "null");
        obj.addProperty("lengthMS", 0);
        obj.addProperty("queuedBy", "Queued through Deprecated method!");


        QueuedSongWithMetadata tmp = new QueuedSongWithMetadata(song, obj, "Queued through Deprecated method!");

        return queueSong(tmp);
    }

    public int queueSong(QueuedSongWithMetadata song) throws QueueError {
        synchronized (songQueue) {

            if(currentSheetMusic != null && currentSheetMusic.equals(song)) {
                throw new QueueError("Cannot queue a song that is currently playing!");
            }

            if(!songQueue.isEmpty() && songQueue.peek().equals(song)) {
                throw new QueueError("That song is already at the top of the queue!");
            }

            logger.info("Queuing song");
            songQueue.add(song);
            sendCurrentQueueAsWSPacket();
            return songQueue.size() - 1; //0 indexed
        }
    }

    public boolean pauseUnpauseSong() {
        if(currentSheetMusic != null && currentSheetMusic.getSheetMusic() != null) {
            synchronized (currentSheetMusic) {
                currentSheetMusic.getSheetMusic().setPaused(!currentSheetMusic.getSheetMusic().isPaused());
                return true;
            }
        }
        return false;
    }

    public Boolean isPaused() {
        if(currentSheetMusic != null && currentSheetMusic.getSheetMusic() != null) {
            synchronized (currentSheetMusic) {
                return currentSheetMusic.getSheetMusic().isPaused();
            }
        }
        return null;
    }

    /**
     * @return true if the sheet music is currently playing.
     */
    public boolean isSheetMusicPlaying() {
        if(currentSheetMusic != null && currentSheetMusic.getSheetMusic() != null) {
            synchronized (currentSheetMusic) {
                return currentSheetMusic.getSheetMusic().isSheetMusicStillScrolling();
            }
        }
        return false;
    }

    public JsonObject getCurrentPlayingSong() {
        if(currentSheetMusic != null && currentSheetMusic.getSongDBEntry() != null) {
            synchronized (currentSheetMusic) {
                return currentSheetMusic.toJson();
            }
        }
        return null;
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
        if(notes == null || notes.isEmpty()) {
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
