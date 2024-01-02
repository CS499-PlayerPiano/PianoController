# PianoController
TBA: More documentation


## Commands

### Help
```bash
java -jar PianoController.jar

java -jar PianoController.jar help
```

### Run server
```bash
java -jar PianoController.jar run-server
```

### Convert MIDI to Piano file
```bash
java -jar PianoController.jar parse-midi --input input.mid --output output.piano
java -jar PianoController.jar parse-midi -i input.mid -o output.piano

# Version defaults to latest
java -jar PianoController.jar parse-midi --input input.mid --output output.piano --version 6
java -jar PianoController.jar parse-midi -i input.mid -o output.piano -v 6
```

### SongDB Verification
```bash
java -jar PianoController.jar songdb-verification

#Githb action changes the working directory
java -jar PianoController.jar songdb-verification --github-action
java -jar PianoController.jar songdb-verification -ga
```