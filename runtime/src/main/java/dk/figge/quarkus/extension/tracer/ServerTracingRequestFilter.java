package dk.figge.quarkus.extension.tracer;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServerTracingRequestFilter implements ContainerRequestFilter {
    private final static Logger logger = Logger.getLogger(ServerTracingRequestFilter.class);

    private final boolean includeRequestHeaders;
    private final boolean includeRequestBody;

    public ServerTracingRequestFilter() {
        Config config = ConfigProvider.getConfig();
        this.includeRequestBody = config.getOptionalValue("dk.figge.quarkus.extension.tracer.include-request-body", Boolean.class).orElse(false);
        this.includeRequestHeaders = config.getOptionalValue("dk.figge.quarkus.extension.tracer.include-request-headers", Boolean.class).orElse(false);
    }

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

        if (includeRequestBody) {
            log.put("request.body", getRequestBody(containerRequestContext).orElse("No body"));
        }
        if (includeRequestHeaders) {
            containerRequestContext.getHeaders().forEach((key, value) -> log.put("request.headers." + key.toLowerCase(), String.join(", ", value)));
        }

        span.log(log);
    }

    private Optional<String> getRequestBody(ContainerRequestContext containerRequestContext) {
        try {
            if(containerRequestContext.hasEntity()) {
                try(InputStream inputStream = containerRequestContext.getEntityStream()) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    IOUtils.copy(inputStream, byteArrayOutputStream);
                    byte[] requestEntity = byteArrayOutputStream.toByteArray();
                    containerRequestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
                    return Optional.of(new String(requestEntity, StandardCharsets.UTF_8));
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            logger.warnf("Not able to read request body: {0}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
