package plu.capstone.playerpiano.controller;

import java.util.LinkedList;
import java.util.Queue;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;

//TODO: Move currentSheetMusic to QueueManager
public class QueueManager {

    private Logger logger = new Logger(this);
    private Queue<SheetMusic> songQueue = new LinkedList<>();

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
                if (PlayerPianoController.getInstance().getCurrentSheetMusic() != null) {
                    synchronized (PlayerPianoController.getInstance().getCurrentSheetMusic()) {
                        if (PlayerPianoController.getInstance().getCurrentSheetMusic().isPlaying()) {
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
                PlayerPianoController.getInstance().playSheetMusic(nextSong);
            }
        }, "Queue checker").start();
    }


    public void queueSong(SheetMusic song) {
        synchronized (songQueue) {
            logger.info("Queuing song");
            songQueue.add(song);
        }
    }

    public void skipSong() {
        logger.info("Skipping song");
        synchronized (PlayerPianoController.getInstance().getCurrentSheetMusic()) {
            if(PlayerPianoController.getInstance().getCurrentSheetMusic() != null) {
                logger.info("Skipping song 2");
                PlayerPianoController.getInstance().getCurrentSheetMusic().stop();
            }
        }

        logger.info("Skipping song 3");
    }

}
