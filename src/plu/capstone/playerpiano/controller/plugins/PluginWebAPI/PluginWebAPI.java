package plugins.PluginWebAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;
import io.javalin.websocket.WsContext;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import plu.capstone.playerpiano.controller.plugin.Plugin;
import plugins.PluginWebAPI.endpoints.Endpoint;
import plugins.PluginWebAPI.endpoints.v1.V1_ControlPiano;
import plugins.PluginWebAPI.endpoints.v1.V1_EndpointGetSongData;

public class PluginWebAPI extends Plugin {

    private static final int PORT = 8898;
    private Javalin app;

    private Set<WsContext> wsClients = new HashSet<>();

    private final Set<Endpoint> ENDPOINTS = Set.of(
            new V1_EndpointGetSongData(),
            new V1_ControlPiano()
    );

    @Override
    protected void onEnable() {

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



        });

        for(Endpoint endpoint : ENDPOINTS) {
            endpoint.register(this, app);
        }

        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                wsClients.add(ctx);
                System.out.println("[WS] Connected");
                ctx.send("Hello from server");
            });
            ws.onMessage(ctx -> {
                System.out.println("[WS] Received message: " + ctx.message());
                ctx.send("Pong! " + ctx.message());
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
}
