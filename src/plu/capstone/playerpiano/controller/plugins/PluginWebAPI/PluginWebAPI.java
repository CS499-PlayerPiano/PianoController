package plu.capstone.playerpiano.controller.plugins.PluginWebAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.websocket.WsContext;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import plu.capstone.playerpiano.controller.plugin.Plugin;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.endpoints.Endpoint;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.endpoints.EndpointControlPiano;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.endpoints.EndpointGetSongData;

public class PluginWebAPI extends Plugin {

    private static final int PORT = 8898;
    private Javalin app;

    private Set<WsContext> wsClients = new HashSet<>();

    private final Set<Endpoint> ENDPOINTS = Set.of(
            new EndpointGetSongData(),
            new EndpointControlPiano()
    );

    String API_DOCS_JSON = "/api/openapi.json";
    String API_DOCS_SWAGGER_PATH = "/docs";
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

            SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
            swaggerConfiguration.setUiPath(API_DOCS_SWAGGER_PATH);
            swaggerConfiguration.setDocumentationPath(API_DOCS_JSON);

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
