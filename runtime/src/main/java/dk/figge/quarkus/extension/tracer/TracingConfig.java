package dk.figge.quarkus.extension.tracer;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "tracing-decorator", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class TracingConfig {

    /**
     * Include client request headers
     */
    @ConfigItem(name = "client.include-request-headers", defaultValue = "true")
    public boolean includeClientRequestHeaders;

    /**
     * Include client request body
     */
    @ConfigItem(name = "client.include-request-body", defaultValue = "true")
    public boolean includeClientRequestBody;

    /**
     * Include client response headers
     */
    @ConfigItem(name = "client.include-response-headers", defaultValue = "true")
    public boolean includeClientResponseHeaders;

    /**
     * Include client response body
     */
    @ConfigItem(name = "client.include-response-body", defaultValue = "true")
    public boolean includeClientResponseBody;

    /**
     * Include client response status
     */
    @ConfigItem(name = "client.include-response-status", defaultValue = "true")
    public boolean includeClientResponseStatus;

    /**
     * Include server request headers
     */
    @ConfigItem(name = "server.include-request-headers", defaultValue = "true")
    public boolean includeServerRequestHeaders;

    /**
     * Include server request body
     */
    @ConfigItem(name = "server.include-request-body", defaultValue = "true")
    public boolean includeServerRequestBody;

    /**
     * Include server response headers
     */
    @ConfigItem(name = "server.include-response-headers", defaultValue = "true")
    public boolean includeServerResponseHeaders;

    /**
     * Include server response body
     */
    @ConfigItem(name = "server.include-response-body", defaultValue = "true")
    public boolean includeServerResponseBody;

    /**
     * Include server response status
     */
    @ConfigItem(name = "server.include-response-status", defaultValue = "true")
    public boolean includeServerResponseStatus;
}
