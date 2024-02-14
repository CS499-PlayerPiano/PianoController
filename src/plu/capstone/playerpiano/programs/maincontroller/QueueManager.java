package plu.capstone.playerpiano.programs.maincontroller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.swagger.util.Json;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import plu.capstone.playerpiano.plugins.Plugin;
import plu.capstone.playerpiano.plugins.impl.PluginWebAPI.PacketIds;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusicCallback;
import plu.capstone.playerpiano.sheetmusic.events.Note;

//TODO: Move currentSheetMusic to QueueManager
public class QueueManager {

    private Logger logger = new Logger(this);
    //    private Queue<SheetMusic> songQueue = new LinkedList<>();
    private final Queue<QueuedSongWithMetadata> songQueue = new LinkedList<>();

    private QueuedSongWithMetadata currentSheetMusic;

    private final PlayerPianoController controller;
    public QueueManager(PlayerPianoController playerPianoController) {
        controller = playerPianoController;
    }

    private boolean didSendEmptyQueue = false;

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
                if (currentSheetMusic != null && currentSheetMusic.sheetMusic != null) {
                    synchronized (currentSheetMusic) {
                        if (currentSheetMusic.sheetMusic.isSheetMusicStillScrolling()) {
                            continue;
                        }
                    }
                }
                //If we are here, we are not playing a song.

                QueuedSongWithMetadata nextSong = null;

                synchronized (songQueue) {

                    if (songQueue.isEmpty()) {
                        if(!didSendEmptyQueue) {
                            sendCurrentQueueAsWSPacket();
                            didSendEmptyQueue = true;
                        }
                        continue;
                    }
                    logger.info("polling next song");
                    nextSong = songQueue.poll();
                }
                //Sanity check
                if (nextSong == null || nextSong.sheetMusic == null) {
                    logger.warning("Song queue returned null!");
                    continue;
                }

                logger.info("Playing song!");

                playSheetMusic(nextSong);
                sendCurrentQueueAsWSPacket();
                didSendEmptyQueue = false;
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

        if (currentSheetMusic != null && currentSheetMusic.songDBEntry != null) {
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

        //Stop the current sheet music if we are playing one
        this.skipSong();


        this.currentSheetMusic = music; //needs to be set before we can sync on it

        synchronized (currentSheetMusic) {
            for (Plugin plugin : controller.getPluginLoader().getPlugins()) {
                if (plugin.isEnabled()) {
                    currentSheetMusic.sheetMusic.addCallback(plugin);
                }
            }

            // We use a new thread for this, so we don't hang the main thread
            // TODO: This should be properly handled by the plugin system & locking!!
            // TODO: It seems to work, but this change will most likely break things!
            new Thread(() -> {
                currentSheetMusic.sheetMusic.play();
            }, "Sheet music playing thread").start();

        }

    }

    /**
     * Stops the current sheet music.
     */
    //TODO: Stop should clear queue.
    //TODO: Skip should stop current song and play next song in queue.
    //TODO: Pause should pause current song.
//    public void stopSheetMusic() {
//        if(currentSheetMusic != null && currentSheetMusic.sheetMusic != null) {
//            synchronized (currentSheetMusic) {
//                currentSheetMusic.sheetMusic.stop();
//            }
//        }
//    }

    /**
     * Queues a song to be played.
     * @param song The song to queue.
     * @return The position of the song in the queue. -1 if the song is already in the queue. -2 if the song is currently playing.
     */
    @Deprecated
    public int queueSong(SheetMusic song) {
        JsonObject title = new JsonObject();
        title.addProperty("name", "Queued through Deprecated method!");
        JsonArray artist = new JsonArray();
        artist.add("Queued through Deprecated method!");
        title.add("artists", artist);
        QueuedSongWithMetadata tmp = new QueuedSongWithMetadata(song, title, "Queued through Deprecated method!");

        return queueSong(tmp);
    }

    /**
     * Queues a song to be played.
     * @param song The song to queue.
     * @return The position of the song in the queue. -1 if the song is already in the queue. -2 if the song is currently playing.
     */
    public int queueSong(QueuedSongWithMetadata song) {
        synchronized (songQueue) {

            if(currentSheetMusic != null && currentSheetMusic.equals(song)) {
                return -2;
            }

            if(songQueue.contains(song)) {
                return -1;
            }

            logger.info("Queuing song");
            songQueue.add(song);
            sendCurrentQueueAsWSPacket();
            return songQueue.size() - 1; //0 indexed
        }
    }

    /**
     * Returns the position of the song in the queue.
     * @param song The song to get the position of.
     * @return The position of the song in the queue. -1 if the song is not in the queue. 0 if the song is currently playing.
     */
    public int getPositionInQueue(SheetMusic song) {
        synchronized (songQueue) {
            int position = 1; //we start at 1 because 0 is the current song in terms of what we return

            if(songQueue.contains(song)) {
                return -1;
            }

            if(currentSheetMusic != null && currentSheetMusic.equals(song)) {
                return 0;
            }

            for(QueuedSongWithMetadata s : songQueue) {
                if(s.equals(song)) {
                    return position;
                }
                position++;
            }
            return position;
        }
    }

    /**
     * Skips the current song.
     */
    public void skipSong() {
        if(currentSheetMusic != null && currentSheetMusic.sheetMusic != null) {
            synchronized (currentSheetMusic) {
                currentSheetMusic.sheetMusic.stop();
            }
        }
    }

    public boolean pauseUnpauseSong() {
        if(currentSheetMusic != null && currentSheetMusic.sheetMusic != null) {
            synchronized (currentSheetMusic) {
                currentSheetMusic.sheetMusic.setPaused(!currentSheetMusic.sheetMusic.isPaused());
                return true;
            }
        }
        return false;
    }

    public Boolean isPaused() {
        if(currentSheetMusic != null && currentSheetMusic.sheetMusic != null) {
            synchronized (currentSheetMusic) {
                return currentSheetMusic.sheetMusic.isPaused();
            }
        }
        return null;
    }

    /**
     * @return true if the sheet music is currently playing.
     */
    public boolean isSheetMusicPlaying() {
        if(currentSheetMusic != null && currentSheetMusic.sheetMusic != null) {
            synchronized (currentSheetMusic) {
                return currentSheetMusic.sheetMusic.isSheetMusicStillScrolling();
            }
        }
        return false;
    }

    public JsonObject getCurrentPlayingSong() {
        if(currentSheetMusic != null && currentSheetMusic.songDBEntry != null) {
            synchronized (currentSheetMusic) {
                return currentSheetMusic.toJson();
            }
        }
        return null;
    }


    @AllArgsConstructor
    public static class QueuedSongWithMetadata {
        private final SheetMusic sheetMusic;

        private final JsonObject songDBEntry;
        private final String whoQueued;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof QueuedSongWithMetadata && ((QueuedSongWithMetadata) obj).sheetMusic.equals(sheetMusic);
        }

        @Override
        public int hashCode() {
            return sheetMusic.hashCode();
        }

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();

            obj.addProperty("name", songDBEntry.get("name").getAsString());
            obj.add("artists", songDBEntry.get("artists").getAsJsonArray());
            obj.addProperty("artwork", songDBEntry.get("artwork").getAsString());
            obj.addProperty("lengthMS", sheetMusic.getSongLengthMS());
            obj.addProperty("queuedBy", whoQueued);

            return obj;
        }
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
