package plu.capstone.playerpiano.controller.songdb;

import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiDescription;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;


//@JsonSchema
public final class Song implements Serializable {

    public String name;
    public String[] artists;
    public String midiFile;
    public String artwork;
    public String[] tags;

    @OpenApiDescription("Display name of the song")
    public String getName() {return name;}

    @OpenApiDescription("Artists who made the song")
    public String[] getArtists() {return artists;}

    @OpenApiDescription("MIDI file name of the song")
    public String getMidiFile() {return midiFile;}

    @OpenApiDescription("Artwork url")
    public String getArtwork() {return artwork;}

    @OpenApiDescription("Search tags for the song")
    public String[] getTags() {return tags;}
}
