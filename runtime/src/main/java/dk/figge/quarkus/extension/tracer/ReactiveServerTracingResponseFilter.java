package dk.figge.quarkus.extension.tracer;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ReactiveServerTracingResponseFilter extends AbstractContainerResponseFilter {
    private final static Logger logger = Logger.getLogger(ReactiveServerTracingResponseFilter.class);

    private final boolean includeRequestBody;
    private final boolean includeResponseBody;
    private final boolean includeResponseHeaders;
    private final boolean includeResponseStatus;

    public ReactiveServerTracingResponseFilter() {
        super();
        Config config = ConfigProvider.getConfig();
        this.includeRequestBody = config.getOptionalValue("dk.figge.quarkus.extension.tracer.include-request-body", Boolean.class).orElse(false);
        this.includeResponseBody = config.getOptionalValue("dk.figge.quarkus.extension.tracer.include-response-body", Boolean.class).orElse(false);
        this.includeResponseHeaders = config.getOptionalValue("dk.figge.quarkus.extension.tracer.include-response-headers", Boolean.class).orElse(false);
        this.includeResponseStatus = config.getOptionalValue("dk.figge.quarkus.extension.tracer.include-response-status", Boolean.class).orElse(false);
    }

    @ServerResponseFilter
    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        Object objectWrapper = containerRequestContext.getProperty(SpanWrapper.PROPERTY_NAME);

        if (!(objectWrapper instanceof SpanWrapper)) {
            return;
        }

        SpanWrapper spanWrapper = (SpanWrapper) objectWrapper;
        Span span = spanWrapper.get();

        Map<String, String> log = new HashMap<>();
        if (includeRequestBody) {
            log.put("request.body", getRequestBody(containerRequestContext).orElse("No body"));
        }
        if (includeResponseBody) {
            log.put("response.body", getResponseBody(containerResponseContext).orElse("No body"));
        }
        if (includeResponseHeaders) {
            containerResponseContext.getHeaders().forEach((key, value) -> log.put("response.headers." + key.toLowerCase(), String.join(", ", value.toString())));
        }
        if (includeResponseStatus) {
            log.put("response.status", "" + containerResponseContext.getStatus());
        }

        span.log(log);
    }

    private Optional<String> getRequestBody(ContainerRequestContext containerRequestContext) {
        try {
            if (containerRequestContext.hasEntity()) {
                InputStream inputStream = containerRequestContext.getEntityStream();
                if (inputStream instanceof ByteArrayInputStream) {
                    ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) inputStream;
                    byteArrayInputStream.reset();
                    byte[] array = new byte[byteArrayInputStream.available()];
                    byteArrayInputStream.read(array);
                    String body = new String(array, StandardCharsets.UTF_8);
                    return Optional.of(body);
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            logger.warnf("Not able to read request body: {0}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
