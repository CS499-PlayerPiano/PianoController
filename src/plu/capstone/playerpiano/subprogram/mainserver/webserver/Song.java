package plu.capstone.playerpiano.subprogram.mainserver.webserver;

import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


//@JsonSchema
@AllArgsConstructor
public final class Song implements Serializable {

    private String name;
    private String[] artists;
    private String midiFile;
    @Setter private String artwork;
    private String[] genre;
    private String[] tags;
    private long noteCount;
    private long songLengthMS;
    private boolean favorite;

    @Setter
    private long timesPlayed;

    @OpenApiDescription("Display name of the song")
    @OpenApiExample("Coconut Mall")
    public String getName() {return name;}

    @OpenApiDescription("Artists who made the song")
    //@OpenApiExample("[Ryo Nagamatsu]") //TODO: Doesn't work with arrays
    public String[] getArtists() {return artists;}

    @OpenApiDescription("MIDI file name of the song")
    @OpenApiExample("Coconut_Mall.mid")
    public String getMidiFile() {return midiFile;}

    @OpenApiDescription("Artwork url")
    @OpenApiExample("api/album-art/Mario_Kart_Wii_OST.jpg")
    public String getArtwork() {return artwork;}

    @OpenApiDescription("Search tags for the song")
    //@OpenApiExample("[Nintendo, Mario Kart, Wii]") //TODO: Doesn't work with arrays
    public String[] getTags() {return tags;}

    @OpenApiDescription("Genre tags for the song")
    //@OpenApiExample("[Nintendo, Mario Kart, Wii]") //TODO: Doesn't work with arrays
    public String[] getGenres() {return genre;}

    @OpenApiDescription("Number of notes in the song")
    @OpenApiExample("1780")
    public long getNoteCount() {return noteCount;}

    @OpenApiDescription("Length of the song in milliseconds")
    @OpenApiExample("123632")
    public long getSongLengthMS() {return songLengthMS;}

    @OpenApiDescription("Number of times the song has been played")
    @OpenApiExample("5")
    public long getTimesPlayed() {return timesPlayed;}

    @OpenApiDescription("Whether the song is a developer favorite")
    @OpenApiExample("true")
    public boolean isFavorite() {return favorite;}
}
