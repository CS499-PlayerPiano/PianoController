package plu.capstone.playerpiano.subprogram.mainserver.webserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.websocket.WsContext;
import java.io.File;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import lombok.Getter;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.FileSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;
import org.jetbrains.annotations.NotNull;
import plu.capstone.playerpiano.outputs.Output;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.subprogram.mainserver.QueueManager;
import plu.capstone.playerpiano.subprogram.mainserver.SubProgramMainController;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints.Endpoint;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints.EndpointCapstonePresentation;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints.EndpointControlPiano;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints.EndpointGetSongData;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints.EndpointStaticRedirects;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints.EndpointStatistics;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints.EndpointsUser;

public class JavalinWebServerOutput extends Output {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    private Javalin app;

    private Set<WsContext> wsClients = new HashSet<>();

    private static final Duration WS_DURATION = Duration.ofSeconds(
            Integer.MAX_VALUE,
            0
    );

    private final Set<Endpoint> ENDPOINTS = Set.of(
            new EndpointGetSongData(),
            new EndpointControlPiano(),
            new EndpointsUser(),
            new EndpointStaticRedirects(),
            new EndpointStatistics(),
            new EndpointCapstonePresentation()
    );

    private static final String API_DOCS_JSON = "/api/openapi.json";
    private static final String API_DOCS_SWAGGER_PATH = "/docs";

    private int PORT = 8898;
    private int TIMESTAMP_INTERVAL = 500;
    private int STATISTICS_INTERVAL = 1000;

    @Getter
    private final QueueManager queueManager;

    @Getter
    private SubProgramMainController program;

    public JavalinWebServerOutput(SubProgramMainController program) {
        super();
        this.queueManager = program.getQueueManager();
        this.program = program;
    }

    @Override
    public String getName() {
        return "Javalin Web Server";
    }

    @Override
    protected void onEnable() {
        PORT = getConfig().getInteger("port", PORT);
        TIMESTAMP_INTERVAL = getConfig().getInteger("timestampInterval", TIMESTAMP_INTERVAL);
        start();
    }

    private void start() {

        Gson gson = new GsonBuilder().create();
        JsonMapper gsonMapper = new JsonMapper() {
            @Override
            public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                return gson.toJson(obj, type);
            }

            @Override
            public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                return gson.fromJson(json, targetType);
            }
        };

        app = Javalin.create(config -> {

            config.jsonMapper(gsonMapper);

            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";                   // change to host files on a subpath, like '/assets'
                staticFiles.directory = "res/html";              // the directory where your files are located
                staticFiles.location = Location.EXTERNAL;      // Location.CLASSPATH (jar) or Location.EXTERNAL (file system)
                staticFiles.precompress = false;                // if the files should be pre-compressed and cached in memory (optimization)
                //staticFiles.aliasCheck = null;                  // you can configure this to enable symlinks (= ContextHandler.ApproveAliases())
                //staticFiles.headers = Map.of(...);              // headers that will be set for the files
                //staticFiles.skipFileFunction = req -> false;    // you can use this to skip certain files in the dir, based on the HttpServletRequest
                //staticFiles.mimeTypes.add(mimeType, ext);       // you can add custom mimetypes for extensions
            });

            config.jetty.sessionHandler(() -> fileSessionHandler());

            config.plugins.register(new OpenApiPlugin(
                            new OpenApiPluginConfiguration()
                                    .withDocumentationPath(API_DOCS_JSON)
                                    .withDefinitionConfiguration((version, definition) -> definition
                                            .withOpenApiInfo((openApiInfo) -> {
                                                openApiInfo.setTitle("Piano Controller API");
                                                openApiInfo.setVersion("1.0.0");
                                                openApiInfo.setDescription("API for controlling the piano");
                                            })

                                    )
                    )

            );

            // Enable CORS for all origins for debugging.
            //TODO: remove this for production!
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.anyHost();
                });
            });

            SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
            swaggerConfiguration.setUiPath(API_DOCS_SWAGGER_PATH);
            swaggerConfiguration.setDocumentationPath(API_DOCS_JSON);

            swaggerConfiguration.injectStylesheet("/assets/swagger/hide-things-we-dont-need.css");

            swaggerConfiguration.setValidatorUrl("");


            config.plugins.register(new SwaggerPlugin(swaggerConfiguration));

            logger.info("Starting web server on port " + PORT);
            logger.info("OpenAPI JSON docs: http://localhost:" + PORT + API_DOCS_JSON);
            logger.info("OpenAPI Swwagger docs: http://localhost:" + PORT + API_DOCS_SWAGGER_PATH);

        });

        for(Endpoint endpoint : ENDPOINTS) {
            endpoint.register(this, app);
        }

        app.ws("/ws", ws -> {

            ws.onConnect(ctx -> {
                wsClients.add(ctx);
                ctx.session.setIdleTimeout(WS_DURATION);
                System.out.println("[WS] Connected");
                JsonObject data = new JsonObject();
                data.addProperty("sessionID", ctx.getSessionId());
                data.add("currentSong", queueManager.getCurrentPlayingSong());
                data.addProperty("isPaused", queueManager.isPaused());
                sendWSPacket(ctx, PacketIds.CONNECTED, data);

            });
            ws.onMessage(ctx -> {
                System.out.println("[WS] Received message: " + ctx.message());

            });
            ws.onClose(ctx -> {
                System.out.println("[WS] Closed");
                wsClients.remove(ctx);
            });
            ws.onError(ctx -> {
                System.out.println("[WS] Errored");
            });
        });

        app.start(PORT);

    }



    public static SessionHandler fileSessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
        sessionCache.setSessionDataStore(fileSessionDataStore());
        sessionHandler.setSessionCache(sessionCache);
        sessionHandler.setHttpOnly(true);
        // make additional changes to your SessionHandler here
        return sessionHandler;
    }

    private static FileSessionDataStore fileSessionDataStore() {
        FileSessionDataStore fileSessionDataStore = new FileSessionDataStore();
        File baseDir = new File("tmp/");
        File storeDir = new File(baseDir, "javalin-session-store");
        storeDir.mkdir();
        fileSessionDataStore.setStoreDir(storeDir);
        return fileSessionDataStore;
    }

    public void sendWSPacket(PacketIds packedId) {
        this.sendWSPacket(packedId, new JsonObject());
    }
    public void sendWSPacket(PacketIds packedId, JsonObject data) {
        for(WsContext ctx : wsClients) {
            sendWSPacket(ctx, packedId, data);
        }
    }

    public void sendWSPacket(WsContext ctx, PacketIds packedId, JsonObject data) {
        JsonObject packet = new JsonObject();
        packet.addProperty("packetId", packedId.getId());
        packet.add("data", data);
        ctx.send(packet.toString());
    }

    long lastTimestamp = 0;
    @Override
    public void onTimestampEvent(long current, long end) {

        //only send packet if one second has passed
        if(current - lastTimestamp > TIMESTAMP_INTERVAL || lastTimestamp == 0) {
            lastTimestamp = current;
            JsonObject data = new JsonObject();
            data.addProperty("current", current);
            data.addProperty("end", end);
            sendWSPacket(PacketIds.TIMESTAMP, data);
        }

    }

    @Override
    public void onPause() {
        JsonObject isPaused = new JsonObject();
        isPaused.addProperty("isPaused", true);
        sendWSPacket(PacketIds.SONG_PAUSED, isPaused);
    }

    @Override
    public void onUnpause() {
        JsonObject isPaused = new JsonObject();
        isPaused.addProperty("isPaused", false);
        sendWSPacket(PacketIds.SONG_PAUSED, isPaused);
    }

    @Override
    public void onNotesPlayed(List<NoteEvent> notes, long timestamp) {
        JsonObject data = new JsonObject();
        data.addProperty("timestamp", timestamp);
        data.add("notes", GSON.toJsonTree(notes));
        sendWSPacket(PacketIds.NOTES_PLAYED, data);
    }

    @Override
    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
        sendWSPacket(PacketIds.SONG_START, queueManager.getCurrentPlayingSong());

        JsonObject pausedData = new JsonObject();
        pausedData.addProperty("isPaused", false);
        sendWSPacket(PacketIds.SONG_PAUSED, pausedData);
        lastTimestamp = 0;
    }

    @Override
    public void onSongFinished(long timestamp) {
        sendWSPacket(PacketIds.SONG_FINISHED);
        lastTimestamp = 0;

        JsonObject pausedData = new JsonObject();
        pausedData.addProperty("isPaused", true);
        sendWSPacket(PacketIds.SONG_PAUSED, pausedData);
    }

}
