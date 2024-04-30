const piano = new Piano(); // Create a new piano instance

//Define listeners
piano.onConnected(onConnectedEvent);
//If running locally, uncomment this line!
//piano.overrideAPI('wss://piano.ericshome.xyz/ws', 'https://piano.ericshome.xyz/api/')
piano.init(); // Initialize the piano, should be called on BODY load, not HEAD load.

function onConnectedEvent(data) {
    console.log("Connected event!", data)
}

function playOneNote() {
    sendDemoRequest("playOneNote");
}

function playMultiNotes() {
    sendDemoRequest("playMultiNotes");
}

function playDemoSong(id) {
    sendDemoRequest("playDemoSong/" + id);
}

function stopDemoSong() {
    sendDemoRequest("stopDemoSong");
}

function sendDemoRequest(loc) {
    piano.sendPostRequest("capstonedemo/" + loc, null, (intResp) => {
        // console.log(intResp);
        if (intResp.status == 200) {
            // iziToast.success({
            //     title: 'Success',
            //     message: 'Sent ' + loc + ' request!',
            //     position: 'topRight'
            // });
        }
        else {
            iziToast.error({
                title: 'Error',
                message: 'Failed to send ' + loc + ' request:<br>' + JSON.stringify(intResp.response),
                position: 'topRight'
            });
        }
    });
}