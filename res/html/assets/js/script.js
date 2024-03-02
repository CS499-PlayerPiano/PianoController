let selectedDifficulty = null;

$(document).ready(function () {


    function updateMoonState(index, mouseX) {
        let isHalf = mouseX <= $('.moon').eq(index).width() / 2;
        $('.moon:lt(' + index + ')').addClass('hovered').text('🌕'); //moons to the left of the cursor will be full
        $('.moon:eq(' + index + ')').addClass('hovered').text(isHalf ? '🌗' : '🌕'); // sets current moon to either full or half depending on the mouse placement
        $('.moon:gt(' + index + ')').removeClass('hovered').text('🌑'); // moons to the right of the cursor will be empty
    }

    $('.moon').on('mouseenter mousemove', function (event) {
        let currentIndex = $(this).index(); //which moon is being hovered
        let mouseX = event.pageX - $(this).offset().left; //gets mouse position
        updateMoonState(currentIndex, mouseX);//changes current moon based on the mouse position
    }).on('mouseleave', function () {
        if (selectedDifficulty !== null) {
            let fullMoons = Math.floor(selectedDifficulty / 2); // Calculate the number of full moons
            let isHalf = selectedDifficulty % 2 === 1; // Check if the last moon is half
            for (let i = 0; i < $('.moon').length; i++) {
                if (i < fullMoons) {
                    $('.moon').eq(i).addClass('hovered').text('🌕');
                } else if (i === fullMoons && isHalf) {
                    $('.moon').eq(i).addClass('hovered').text('🌗');
                } else {
                    $('.moon').eq(i).removeClass('hovered').text('🌑');
                }
            }
        } else {
            $('.moon').removeClass('hovered').text('🌑'); // Reset all moons to empty if no moon was clicked
        }
    });

    $('.moon').on('click', function () {
        let currentIndex = $(this).index(); //gets index of the clicked moon
        let isHalf = $(this).text() === '🌗'; // checks state of current moon
        selectedDifficulty = 1 + (currentIndex * 2) + (isHalf ? 0 : 1); //set selected difficulty based on the moon clicked

        $('.moon').removeClass('selected');
        $(this).addClass('selected');

        $grid.isotope(); //reapply isotope filter
    });
    $('.dropdown-container').on('click', '.undo-button', function () {
        $('#sort-dropdown').prop('selectedIndex', 0); // Set the dropdown to default option
        $grid.isotope({ sortBy: 'original-order', sortAscending: true})
        $('.moon').removeClass('hovered').text('🌑'); // Reset moon states
        $('#quicksearch').val(''); // Clear search field
        qsRegex = null; // Reset search regex
        $grid.isotope(); // Reapply isotope filter
    });
});

//Global variables
let $grid;
let songsDB;
const piano = new Piano(); // Create a new piano instance

//Define listeners
piano.onQueueUpdated(onQueueChanged);
piano.onSongPausedUnpaused(onPausedUnpaused);
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

    setUIIsPaused(data.isPaused);
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
    let percentage = (currentMS / endMS) * 100;
    setProgressBarUI(percentage);
}

// 0-100
function setProgressBarUI(percentage) {
    document.getElementById('progressBar').style.width = percentage + '%';
}

function setNowPlayingInfo(currentSong) {

    let currentUrl = location.href;
    let name = "Nothing Playing";
    let artist = "";
    let artwork = "NOTHING_PLAYING.png";

    if (currentSong != undefined && currentSong != null) {
        name = currentSong.name;
        artist = currentSong.artists.join(', ');
        artwork = currentSong.artwork;
    }

    artwork = currentUrl + "api/album-art/" + artwork;

    $('#npArtwork').attr('src', artwork);
    $('#npTitle').text(name);
    $('#npArtist').text(artist);
}

function onPausedUnpaused(data) {
    let isPaused = data.isPaused;

    setUIIsPaused(isPaused);

    console.log("Paused/Unpaused event!", data)
}

function setUIIsPaused(isPaused) {

    //Does this look better then just returning?
    //NULL means no song is playing
    if (isPaused == undefined || isPaused == null) {
        isPaused = true;
    }

    if (isPaused) {
        $('#npbPlayPause').text('play_arrow');
    } else {
        $('#npbPlayPause').text('pause');
    }
}

//Handle play pause clicking
document.getElementById('npbPlayPause').addEventListener('click', function () {
    piano.pauseUnpauseSong();
});

//Handle skip forward clicking
document.getElementById('npbSkipForward').addEventListener('click', function () {
    piano.skipSong();
});

piano.getSongList((songs) => { // Get the list of songs from the API
    songsDB = songs;
    const html = `
    <div class="song-element" data-tags="%tags%" data-difficulty="%difficulty%" data-song-index="%song-index%" data-length-ms="%lengthMS%" data-genres="%genres%" data-developer-favorite="%developer-favorite%" data-debug="%debugSong%">
        %starOverlay%
        <div class="image-container">
            <img loading="lazy" src="%artwork%" alt="Song Image">
        </div>
        <div class="song-info">
            <p class="song-title">%title%</p>
            <p class="artist">%artist%</p>
            <p class="length">%length%</p>
            <p class="noteDensity"><span class="emojis">%emojis%</span></p>

        </div>
    </div>
`
    // Add each song to the list, name is the text, midiFile is the value
    for (let i = 0; i < songs.length; i++) {
        let song = songs[i];
        song.noteDensity = (song.noteCount / song.songLengthMS) * 1000;
        song.difficulty = noteDensityToDifficulty(song.noteDensity);

        console.log(song);

        let isDebugSong = song.tags.includes("debugging");

        let songElement = html;
        songElement = songElement.replace('%artwork%', song.artwork);
        songElement = songElement.replace('%title%', song.name);
        songElement = songElement.replace('%artist%', arrayToCommaSeparatedString(song.artists));
        songElement = songElement.replace('%genres%', arrayToCommaSeparatedString(song.genre));
        songElement = songElement.replace('%tags%', arrayToCommaSeparatedString(song.tags));
        songElement = songElement.replace('%noteDensity%', song.noteDensity);
        songElement = songElement.replace('%difficulty%', song.difficulty);
        songElement = songElement.replace('%emojis%', difficultyToEmojis(song.difficulty));
        songElement = songElement.replace('%song-index%', i);
        songElement = songElement.replace('%length%', formatMS(song.songLengthMS));
        songElement = songElement.replace('%lengthMS%', song.songLengthMS);
        songElement = songElement.replace('%debugSong%', isDebugSong);

        songElement = songElement.replace('%developer-favorite%', song.favorite);
        songElement = songElement.replace('%starOverlay%', song.favorite ? '<div class="star-overlay">⭐</div>' : '');

        $('.grid').append(songElement);
    }

    initSearchBarThingy(); // initialize isotope

    $('.grid').on('click', '.song-element', function (e) {
        e.stopPropagation();

        let artwork = $(this).find('.image-container img').attr('src');
        let title = $(this).find('.song-title').text();
        let artist = $(this).find('.artist').text();
        let songIndex = $(this).attr('data-song-index');

        $('.now-playing').html(`
        <img src="${artwork}" alt="${title} - ${artist}" style="width: 300px; height: 300px; object-fit: cover;">
        <h2>${title}</h2>
        <p>${artist}</p>
        <button class="back-to-list">Back to Song List</button>
        <button class ="back-to-list", onClick="queueSongByIndex(${songIndex})"> Queue Song</button>
        `);
        showQueueContainer();
    });

    $('.now-playing').on('click', '.back-to-list', function () {
        hideQueueContainer();
    });

    $('.now-playing-container').on('click', function () {
        hideQueueContainer();
    });

});

//Saveing and loading scroll position seems like a bit of a hack
//Real fix here is most likely some CSS Magic that I am not aware of.
let savedScrollPosition = 0;
function showQueueContainer() {
    savedScrollPosition = $(window).scrollTop(); // save scroll position
    $('.sticky-container').hide(); // hide header
    $('.container').hide(); // hide song list
    $('.now-playing-container').show(); // show queue container
    console.log("Saving scroll position", savedScrollPosition);
}

function hideQueueContainer() {
    $('.sticky-container').show(); // show header
    $('.container').show(); // show song list
    $('.now-playing-container').hide(); // hide queue container
    $(window).scrollTop(savedScrollPosition); // restore scroll position
    console.log("Restoring scroll position", savedScrollPosition);
}

function queueSongByIndex(index) {
    let song = songsDB[index];
    queueSong(song);
}

/**
 * Converts a note density to difficulty
 * @param {number} noteDensity The note density
 * @returns {number} The difficulty
 */
function noteDensityToDifficulty(noteDensity) {
    if (noteDensity < 5) {
        return 1;
    } else if (noteDensity >= 5 && noteDensity < 6) {
        return 2;
    } else if (noteDensity >= 6 && noteDensity < 7.5) {
        return 3
    } else if (noteDensity >= 7.5 && noteDensity < 9) {
        return 4;
    } else if (noteDensity >= 9 && noteDensity < 10.5) {
        return 5;
    } else if (noteDensity >= 10.5 && noteDensity < 13) {
        return 6;
    } else if (noteDensity >= 13 && noteDensity < 15) {
        return 7;
    } else if (noteDensity >= 15 && noteDensity < 22.5) {
        return 8;
    } else if (noteDensity >= 22.5 && noteDensity < 30) {
        return 9;
    } else {
        return 10;
    }
}

/**
 * Converts a difficulty to emojis
 * @param {number} difficulty The difficulty
 * @returns {string} The emojis
 */
function difficultyToEmojis(difficulty) {
    switch (difficulty) {
        case 0: return '🌑🌑🌑🌑🌑';
        case 1: return '🌗🌑🌑🌑🌑';
        case 2: return '🌕🌑🌑🌑🌑';
        case 3: return '🌕🌗🌑🌑🌑';
        case 4: return '🌕🌕🌑🌑🌑';
        case 5: return '🌕🌕🌗🌑🌑';
        case 6: return '🌕🌕🌕🌑🌑';
        case 7: return '🌕🌕🌕🌗🌑';
        case 8: return '🌕🌕🌕🌕🌑';
        case 9: return '🌕🌕🌕🌕🌗';
        case 10: return '🌕🌕🌕🌕🌕';
        default: return '🔥🔥🔥🔥🔥';
    }
}

/**
 * Converts an array to a comma separated string
 * @param {Array} array The array
 * @returns {string} The comma separated string
 */
function arrayToCommaSeparatedString(array) {
    let string = '';
    for (let i = 0; i < array.length; i++) {
        string += array[i];
        if (i < array.length - 1) {
            string += ', ';
        }
    }
    return string;
}


//on sort-dropdown change
//Change the sort of the isotope grid depending on the dropdown value
$('#sort-dropdown').on('change', function () {
    let value = $(this).val();

    //if the option has data-descending, sort descending

    let ascending = true;
    //if the option has data-ascending AND it's false, sort descending
    if ($(this).find(':selected').data('ascending') == false) {
        ascending = false;
    }

    console.log("Sorting by", value, "ascending", ascending);

    $grid.isotope({ sortBy: value, sortAscending: ascending });
});

let qsRegex = null; // quick search regex
function initSearchBarThingy() {

    $grid = $('.grid').isotope({ // init Isotope
        itemSelector: '.song-element',
        layoutMode: 'fitRows',
        filter: function () {
            let $this = $(this);
            let title = $this.find('.song-title').text(); //Search by title, artist, and tags
            let artist = $this.find('.artist').text();
            let tags = $this.attr('data-tags');
            let genres = $this.attr('data-genres');
            let isDebugSong = parseBool($this.attr('data-debug'));

            let haveWeSearchedAnything = qsRegex !== null && $quicksearch.val().trim().length > 2; //Must be at least 3 characters to search

            let difficulty = true;
            if (selectedDifficulty !== null) {
                difficulty = $this.attr('data-difficulty') == selectedDifficulty;
            }

            //Else do normal sorting
            let searchTitle = qsRegex ? (title.match(qsRegex) != null) : true;
            let searchArtist = qsRegex ? (artist.match(qsRegex) != null) : true;
            let searchTags = qsRegex ? (tags.match(qsRegex) != null) : true;
            let searchGenres = qsRegex ? (genres.match(qsRegex) != null) : true;

            if (isDebugSong) {
                console.log("haveWeSearchedAnything", haveWeSearchedAnything, "searchTitle", searchTitle, "searchArtist", searchArtist, "searchTags", searchTags, "difficulty", difficulty, "searchGenres", searchGenres, "isDebugSong", isDebugSong)
                if (haveWeSearchedAnything) {
                    return searchTags || searchTitle;
                }
                return false;
            }




            //console.log("searchTitle", searchTitle, "searchArtist", searchArtist, "searchTags", searchTags, "difficulty", difficulty, "searchGenres", searchGenres, "isDebugSong", isDebugSong)


            return (searchTitle || searchArtist || searchTags || searchGenres) && difficulty;
        },
        getSortData: {
            difficulty: '[data-difficulty] parseInt',

            // Name and artist case insensitive
            name: function (itemElem) {
                return $(itemElem).find('.song-title').text().toLowerCase();
            },
            artist: function (itemElem) {
                return $(itemElem).find('.artist').text().toLowerCase();
            },


            tags: '[data-tags]',
            genres: '[data-genres]',
            length: '[data-length-ms] parseInt',
        }
    });
    // use value of search field to filter
    let $quicksearch = $('#quicksearch').keyup(debounce(function () {

        let searchValue = $quicksearch.val().trim();

        qsRegex = new RegExp(searchValue, 'gi');


        if (searchValue == '') {
            qsRegex = null;
        }
        console.log("Filtering by", qsRegex);
        $grid.isotope();
    }));
}

/**
 * Wrapper to queue a song and make a toast as well
 */
function queueSong(song) {
    piano.queueSong(song, (callback) => {
        let response = callback.response;
        if (callback.status == 200) {
            let position = response.position;
            let success = response.success;

            if (!success) {
                iziToast.warning({
                    title: 'Error',
                    message: response.error,
                    position: 'topRight'
                });
                return;
            }
            else {
                let message = `Song queued at position ${position + 1}`;
                iziToast.success({
                    title: 'Success',
                    message: message,
                    position: 'topRight'
                });
            }
        }
        else {
            iziToast.error({
                title: 'Error',
                message: 'An unknown error occurred while queueing the song.',
                position: 'topRight'
            });
        }
    });
}

// debounce so filtering doesn't happen every millisecond
function debounce(fn, threshold) {
    let timeout;
    threshold = threshold || 100;
    return function debounced() {
        clearTimeout(timeout);
        let args = arguments;
        let _this = this;
        function delayed() {
            fn.apply(_this, args);
        }
        timeout = setTimeout(delayed, threshold);
    };
}

function formatMS(duration) {
    let seconds = Math.floor((duration / 1000) % 60);
    let minutes = Math.floor((duration / (1000 * 60)) % 60);
    seconds = (seconds < 10) ? "0" + seconds : seconds;
    return minutes + "m " + seconds + "s";
}

//I really dislike javascript sometimes.
//Boolean("false") is true! Because logic.
function parseBool(value) {
    return (String(value).toLowerCase() === 'true');
}

$(document).mouseup(function (e) {
    var container = $(".now-playing-container");
    if (!container.is(e.target) && container.has(e.target).length === 0) {
        container.hide();
    }
});