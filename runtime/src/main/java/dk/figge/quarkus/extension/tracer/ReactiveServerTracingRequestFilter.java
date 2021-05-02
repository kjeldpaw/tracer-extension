package dk.figge.quarkus.extension.tracer;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReactiveServerTracingRequestFilter implements ContainerRequestFilter {
    private final boolean includeRequestHeaders;

    public ReactiveServerTracingRequestFilter() {
        Config config = ConfigProvider.getConfig();
        this.includeRequestHeaders = config.getOptionalValue("dk.figge.quarkus.extension.tracer.include-request-headers", Boolean.class).orElse(false);
    }

    @ServerRequestFilter
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        Object objectWrapper = containerRequestContext.getProperty(SpanWrapper.PROPERTY_NAME);
        if (!(objectWrapper instanceof SpanWrapper)) {
            return;
        }
        SpanWrapper spanWrapper = (SpanWrapper) objectWrapper;
        Span span = spanWrapper.get();

        Map<String, String> log = new HashMap<>();
        log.put("request.uri", containerRequestContext.getUriInfo().getRequestUri().toString());
        log.put("request.method", containerRequestContext.getMethod());

        if (includeRequestHeaders) {
            containerRequestContext.getHeaders().forEach((key, value) -> log.put("request.headers." + key.toLowerCase(), String.join(", ", value)));
        }

        span.log(log);
    }
}
