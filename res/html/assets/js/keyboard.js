const piano = new Piano(); // Create a new piano instance

//Define listeners
piano.onConnected(onConnectedEvent);
//If running locally, uncomment this line!
//piano.overrideAPI('wss://piano.ericshome.xyz/ws', 'https://piano.ericshome.xyz/api/')
piano.init(); // Initialize the piano, should be called on BODY load, not HEAD load.

const NOTE_MAP = {
    "00A": 21,
    "00As": 22,
    "00B": 23,
    "0C": 24,
    "0Cs": 25,
    "0D": 26,
    "0Ds": 27,
    "0E": 28,
    "0F": 29,
    "0Fs": 30,
    "0G": 31,
    "0Gs": 32,
    "0A": 33,
    "0As": 34,
    "0B": 35,
    "1C": 36,
    "1Cs": 37,
    "1D": 38,
    "1Ds": 39,
    "1E": 40,
    "1F": 41,
    "1Fs": 42,
    "1G": 43,
    "1Gs": 44,
    "1A": 45,
    "1As": 46,
    "1B": 47,
    "2C": 48,
    "2Cs": 49,
    "2D": 50,
    "2Ds": 51,
    "2E": 52,
    "2F": 53,
    "2Fs": 54,
    "2G": 55,
    "2Gs": 56,
    "2A": 57,
    "2As": 58,
    "2B": 59,
    "3C": 60,
    "3Cs": 61,
    "3D": 62,
    "3Ds": 63,
    "3E": 64,
    "3F": 65,
    "3Fs": 66,
    "3G": 67,
    "3Gs": 68,
    "3A": 69,
    "3As": 70,
    "3B": 71,
    "4C": 72,
    "4Cs": 73,
    "4D": 74,
    "4Ds": 75,
    "4E": 76,
    "4F": 77,
    "4Fs": 78,
    "4G": 79,
    "4Gs": 80,
    "4A": 81,
    "4As": 82,
    "4B": 83,
    "5C": 84,
    "5Cs": 85,
    "5D": 86,
    "5Ds": 87,
    "5E": 88,
    "5F": 89,
    "5Fs": 90,
    "5G": 91,
    "5Gs": 92,
    "5A": 93,
    "5As": 94,
    "5B": 95,
    "6C": 96,
    "6Cs": 97,
    "6D": 98,
    "6Ds": 99,
    "6E": 100,
    "6F": 101,
    "6Fs": 102,
    "6G": 103,
    "6Gs": 104,
    "6A": 105,
    "6As": 106,
    "6B": 107,
    "7C": 108
};

function onConnectedEvent(data) {
    console.log("Connected event!", data)
}

$(document).ready(function () {

    $('.key').on('mousedown touchstart', function () {
        let id = $(this).attr('id');

        //add id to pressedNotes if not already in there
        if (!pressedNotes.includes(id)) {
            console.log("Note on: ", id);
            pressedNotes.push(id);
            updatePressedNotes();
        }
    });

    $('.key').on('mouseup touchend', function () {
        let id = $(this).attr('id');

        //remove id from pressedNotes
        let index = pressedNotes.indexOf(id);
        if (index > -1) {
            console.log("Note off: ", id);
            pressedNotes.splice(index, 1);
            updatePressedNotes();
        }
    });
});

let pressedNotes = [];
function updatePressedNotes() {

    let pressedMidiNotes = [];
    pressedNotes.forEach(note => {
        pressedMidiNotes.push(NOTE_MAP[note]);
    });

    //send them to the piano

    console.log('prressedMiidiNotes', pressedMidiNotes);

}