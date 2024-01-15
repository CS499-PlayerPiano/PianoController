class Piano {

    // Declare private fields
    #websocket;
    #onSongStartedCallback;
    #onSongFinishedCallback;
    #onTimestampCallback;
    #onNotesPlayedCallback;
    #onQueueUpdatedCallback;

    // Declare public fields & initialize public / private fields
    constructor() {
        this.#websocket = null;

        this.#onSongStartedCallback = null;
        this.#onSongFinishedCallback = null;
        this.#onTimestampCallback = null;
        this.#onNotesPlayedCallback = null;
        this.#onQueueUpdatedCallback = null;
    }

    // Called when the page is loaded
    init() {
        console.log('[Piano] Piano init');

        // Connect to the server
        this.#connect();
        this.#getOrCreatePersistentSession();
        this.#getInitialQueue();
    }

    // Connect to the server, reconnect when the connection is closed
    #connect() {
        let protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        let wsUrl = protocol + '//' + window.location.host + '/ws';
        this.#websocket = new WebSocket(wsUrl);

        // Log open event
        this.#websocket.onopen = () => {
            console.log('[Piano - WS] Connected!');
        };

        // Log close event & reconnect when the connection is closed
        this.#websocket.onclose = () => {
            console.log('[Piano - WS] Disconnected!');
            // Reconnect when the connection is closed
            setTimeout(() => {
                console.log('[Piano - WS] Reconnecting...');
                this.#connect();
            }, 1000); // Change the time interval as needed
        };

        // Log errors
        this.#websocket.onerror = (error) => {
            console.error('[Piano - WS] WebSocket error:', error);
        };

        // Parse the message received from the server
        this.#websocket.onmessage = (event) => {
            this.#parseWebsocketMessage(event);
        };
    }

    #getInitialQueue() {
        this.#sendGetRequest("control/getQueue", (intResp) => {
            if (intResp.status == 200) {
                console.log('[Piano - API] Initial queue:', intResp.response);
                if (this.#onQueueUpdatedCallback != null) {
                    this.#onQueueUpdatedCallback(intResp.response);
                }
            }
            else {
                console.error('[Piano - API] Error getting initial queue:', intResp);
            }
        });
    }

    #getOrCreatePersistentSession() {
        this.#sendGetRequest("users/getSession", (intResp) => {
            if (intResp.status == 200) {
                console.log('[Piano - API] Persistent Session created:', intResp.response.session);
            }
            else {
                console.error('[Piano - API] Error creating session:', intResp);
            }
        });
    }

    // Called when a message is received from the server
    #parseWebsocketMessage(event) {
        let pkt = JSON.parse(event.data);

        // Song started event
        if (pkt.packetId == "songStarted") {

            if (this.#onSongStartedCallback != null) {
                this.#onSongStartedCallback();
            }
        }

        // Song finished event
        else if (pkt.packetId == "songFinished") {
            if (this.#onSongFinishedCallback != null) {
                this.#onSongFinishedCallback();
            }
        }

        // Timestamp updated event
        else if (pkt.packetId == "timestamp") {
            if (this.#onTimestampCallback != null) {
                this.#onTimestampCallback(pkt.data);
            }
        }

        // Notes played event
        else if (pkt.packetId == "notesPlayed") {
            if (this.#onNotesPlayedCallback != null) {
                this.#onNotesPlayedCallback(pkt.data);
            }
        }

        // Queue updated event
        else if (pkt.packetId == "queueUpdated") {
            if (this.#onQueueUpdatedCallback != null) {
                this.#onQueueUpdatedCallback(pkt.data);
            }
        }

        // Connected event
        else if (pkt.packetId == "connected") {
            console.log('[Piano - WS] Recieved connected packet:');
            console.log('[Piano - WS]   - Session ID: ' + pkt.data.sessionID);
        }
        else {
            console.error('[Piano - WS] Unknown packetId: "' + pkt.packetId + '" with data:', pkt.data);
        }
    }

    // Send a GET request to the server
    #sendGetRequest(loc, responseCallback = null) {
        let xhr = new XMLHttpRequest();
        xhr.open("GET", "/api/" + loc, true);

        xhr.onload = () => {

            if (responseCallback != null) {
                let tmp = {
                    "status": xhr.status,
                    "response": JSON.parse(xhr.response)
                };
                responseCallback(tmp);
            }
        };

        xhr.send();
    }

    // Send a POST request to the server
    #sendPostRequest(loc, data = null, responseCallback = null) {
        let xhr = new XMLHttpRequest();
        xhr.open("POST", "/api/" + loc, true);
        xhr.setRequestHeader('Content-Type', 'application/json');

        xhr.onload = () => {

            if (responseCallback != null) {
                let tmp = {
                    "status": xhr.status,
                    "response": JSON.parse(xhr.response)
                };
                responseCallback(tmp);
            }
        };

        if (data == null) {
            xhr.send();
        } else {
            xhr.send(JSON.stringify(data));
        }

    }

    // Send a control request to the server
    #sendControlRequest(loc, data = null, responseCallback = null) {
        this.#sendPostRequest("control/" + loc, data, responseCallback);
    }

    // Get the list of songs from the server
    getSongList(responseCallback = null) {
        this.#sendGetRequest("songs", (intResp) => {
            if (responseCallback != null) {
                if (intResp.status == 200) {
                    responseCallback(intResp.response);
                }
                else {
                    console.error('[Piano - API] Error getting song list:', intResp);
                }
            }
        });
    }

    // Get the list of songs from the server
    queueSong(song, responseCallback = null) {
        let tmp = {
            midiFile: song.midiFile
        }
        this.#sendControlRequest("queue", tmp, responseCallback);
    }


    skipSong() {
        this.#sendControlRequest("skip", null, (intResp) => {
            if (intResp.status == 200) {
                console.log('[Piano - API] Song skipped');
            }
            else {
                console.error('[Piano - API] Error skipping song:', intResp);
            }
        });
    }

    // Callback setter for song started event
    onSongStarted(callback) {
        this.#onSongStartedCallback = callback;
    }

    // Callback setter for song finished event
    onSongFinished(callback) {
        this.#onSongFinishedCallback = callback;
    }

    // Callback setter for notes played event
    onNotesPlayed(callback) {
        this.#onNotesPlayedCallback = callback;
    }

    // Callback setter for timestamp updated event
    onTimestampUpdated(callback) {
        this.#onTimestampCallback = callback;
    }

    // Callback setter for queue updated event
    onQueueUpdated(callback) {
        this.#onQueueUpdatedCallback = callback;
    }

}   