package dk.figge.quarkus.extension.tracer;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import java.util.HashMap;
import java.util.Map;

public class ServerTracingResponseFilter extends AbstractContainerResponseFilter {
    private final boolean includeResponseBody;
    private final boolean includeResponseHeaders;
    private final boolean includeResponseStatus;

    public ServerTracingResponseFilter() {
        super();
        Config config = ConfigProvider.getConfig();
        this.includeResponseBody = config.getOptionalValue("dk.figge.quarkus.extension.tracer.include-response-body", Boolean.class).orElse(false);
        this.includeResponseHeaders = config.getOptionalValue("dk.figge.quarkus.extension.tracer.include-response-headers", Boolean.class).orElse(false);
        this.includeResponseStatus = config.getOptionalValue("dk.figge.quarkus.extension.tracer.include-response-status", Boolean.class).orElse(false);
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
        Object objectWrapper = containerRequestContext.getProperty(SpanWrapper.PROPERTY_NAME);

        if (!(objectWrapper instanceof SpanWrapper)) {
            return;
        }

        SpanWrapper spanWrapper = (SpanWrapper) objectWrapper;
        Span span = spanWrapper.get();

        Map<String, String> log = new HashMap<>();
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
}
