package plu.capstone.playerpiano.subprogram.mainserver;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.Queue;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.PacketIds;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusicCallback;

public class QueueManager {

    private final Logger logger = new Logger(this);
    private final Queue<QueuedSongWithMetadata> songQueue = new LinkedList<>();

    private QueuedSongWithMetadata currentSheetMusic;

    private final SubProgramMainController controller;


    public QueueManager(SubProgramMainController playerPianoController) {
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
                            playSheetMusic();
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
        controller.getWebServerOutput().sendWSPacket(PacketIds.QUEUE_UPDATED, getQueueAsJson());
    }

    /**
     * Stops any currently playing music, and plays the given sheet music.
     */
    private void playSheetMusic() {

        if(currentSheetMusic == null || currentSheetMusic.getSheetMusic() == null) {
            logger.error("Tried to play sheet music, but it was null! How did we get here? (1)");
            return;
        }

        synchronized (currentSheetMusic) {
            for (SheetMusicCallback callbacks : controller.getOutputs()) {
                logger.debug("Adding callback to sheet music: " + callbacks.getClass().getName());
                currentSheetMusic.getSheetMusic().addCallback(callbacks);
            }

            //We want to track the song that was played in the DB
            controller.getStatistics().onSongPlayed(currentSheetMusic.getName());

            // We use a new thread for this, so we don't hang the main thread
            new Thread(() -> {
                if(currentSheetMusic == null || currentSheetMusic.getSheetMusic() == null) {
                    logger.error("Tried to play sheet music, but it was null! How did we get here? (2)");
                    return;
                }
                currentSheetMusic.getSheetMusic().play();
            }, "Sheet music playing thread").start();

        }

    }

    public void stopOrSkipCurrentSong() {
        if(currentSheetMusic != null && currentSheetMusic.getSheetMusic() != null) {
            synchronized (currentSheetMusic) {
                currentSheetMusic.getSheetMusic().stop();
            }
            currentSheetMusic = null;
        }
        else {
            logger.error("Tried to stop or skip current song, but it was null! Ignoring...");
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

}
