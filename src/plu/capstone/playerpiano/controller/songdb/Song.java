package plu.capstone.playerpiano.controller.songdb;

import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;


//@JsonSchema
@AllArgsConstructor
public final class Song implements Serializable {

    private String name;
    private String[] artists;
    private String midiFile;
    private String artwork;
    private String[] tags;

    @OpenApiDescription("Display name of the song")
    @OpenApiExample("Coconut Mall")
    public String getName() {return name;}

    @OpenApiDescription("Artists who made the song")
    @OpenApiExample("[Ryo Nagamatsu]")
    public String[] getArtists() {return artists;}

    @OpenApiDescription("MIDI file name of the song")
    @OpenApiExample("Coconut_Mall.mid")
    public String getMidiFile() {return midiFile;}

    @OpenApiDescription("Artwork url")
    @OpenApiExample("api/album-art/Mario_Kart_Wii_OST.jpg")
    public String getArtwork() {return artwork;}

    @OpenApiDescription("Search tags for the song")
    @OpenApiExample("[Nintendo, Mario Kart, Wii]")
    public String[] getTags() {return tags;}
}
