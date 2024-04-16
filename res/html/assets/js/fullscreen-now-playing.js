

let songsDB;
const piano = new Piano(); // Create a new piano instance

//Define listeners
piano.onQueueUpdated(onQueueChanged);
piano.onConnected(onConnectedEvent);
piano.onSongFinished(onSongFinishedEvent);
piano.onSongStarted(onSongStartedEvent);
piano.onTimestampUpdated(onTimestampUpdatedEvent);
//If running locally, uncomment this line!
//piano.overrideAPI('wss://piano.ericshome.xyz/ws', 'https://piano.ericshome.xyz/api/')
piano.init(); // Initialize the piano, should be called on BODY load, not HEAD load.

function onQueueChanged(queue) {
    console.log("Queue changed!", queue)
    setNowPlayingInfo(queue.nowPlaying);
}

function onConnectedEvent(data) {
    console.log("Connected event!", data)
    let currentSong = data.currentSong;
    setNowPlayingInfo(currentSong);
    setProgressBarUI(0);
}

function onSongStartedEvent(data) {
    console.log("Song started event!", data)
    setProgressBarUI(0);
}

function onSongFinishedEvent(data) {
    console.log("Song finished event!", data)
    setNowPlayingInfo(null);

    setTimeout(() => {
        setProgressBarUI(0);
        console.log("Resetting progress bar (delayed)");
    }, 50);
}

function onTimestampUpdatedEvent(data) {
    // console.log("Timestamp updated event!", data);
    let currentMS = data.current;
    let endMS = data.end;

    document.getElementById("currentTime").innerText = formatTime(currentMS);
    document.getElementById("songLength").innerText = formatTime(endMS);

    let percentage = (currentMS / endMS) * 100;
    setProgressBarUI(percentage);
}

// 0-100
function setProgressBarUI(percentage) {
    document.getElementById('progressBar').style.width = percentage + '%';
}

function setNowPlayingInfo(currentSong) {

    let name = "Nothing Playing";
    let artist = "";
    let artwork = "NOTHING_PLAYING.png";

    if (currentSong != undefined && currentSong != null) {
        name = currentSong.name;
        artist = currentSong.artists.join(', ');
        artwork = currentSong.artwork;
    }

    if (artist == "") {
        document.getElementById('byText').style.display = 'none';
    }
    else {
        document.getElementById('byText').style.display = 'block';
    }

    artwork = piano.getAPIURL() + "album-art/" + artwork;

    $('#npArtwork').attr('src', artwork);
    $('#songTitle').text(name);
    $('#artistName').text(artist);
}

piano.getSongList((songs) => { // Get the list of songs from the API
    songsDB = songs;
    console.log("Songs loaded", songsDB);
});

function formatTime(ms) {
    let seconds = Math.floor(ms / 1000);
    let minutes = Math.floor(seconds / 60);
    seconds = seconds % 60;

    if (seconds < 10) {
        seconds = "0" + seconds;
    }

    return minutes + ":" + seconds;
}