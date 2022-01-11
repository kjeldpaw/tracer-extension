package dk.figge.quarkus.extension.tracer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.internal.URIUtils;
import io.opentracing.tag.Tags;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ServerSpanDecorator extends AbstractTracingDecorator implements io.opentracing.contrib.jaxrs2.server.ServerSpanDecorator {
    private static final Logger logger = Logger.getLogger(ServerSpanDecorator.class);

    public ServerSpanDecorator(TracingConfig tracingConfig, ObjectMapper objectMapper) {
        super(tracingConfig, objectMapper);
    }

    @Override
    public void decorateRequest(ContainerRequestContext context, Span span) {
        Tags.COMPONENT.set(span, "tracing-decorator");
        Tags.HTTP_METHOD.set(span, context.getMethod());
        final var url = URIUtils.url(context.getUriInfo().getRequestUri());
        if (url != null) {
            Tags.HTTP_URL.set(span, url);
        }
        if (tracingConfig.includeServerRequestHeaders) {
            getHeaders(context.getHeaders()).ifPresent(headers -> span.setTag("http.request.headers", headers));
        }
        if (tracingConfig.includeServerRequestBody) {
            getRequestBody(context).ifPresent(body -> span.setTag("http.request.body", body));
        }
    }

    @Override
    public void decorateResponse(ContainerResponseContext context, Span span) {
        if (tracingConfig.includeServerResponseHeaders) {
            getHeaders(context.getHeaders()).ifPresent(headers -> span.setTag("http.response.headers", headers));
        }
        if (tracingConfig.includeServerResponseStatus) {
            Tags.HTTP_STATUS.set(span, context.getStatus());
        }
        if (tracingConfig.includeServerResponseBody) {
            getResponseBody(context).ifPresent(body -> span.setTag("http.response.body",body));
        }
    }

    private Optional<String> getHeaders(final MultivaluedMap<String, ?> headers) {
        try {
            if (headers == null || headers.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(objectMapper.writeValueAsString(headers));
            }
        } catch (JsonProcessingException e) {
            logger.warnf(e, "Failed serializing headers: %s", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> getRequestBody(ContainerRequestContext context) {
        try {
            if (context.hasEntity()) {
                final var inputStream = context.getEntityStream();
                final var body = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                context.setEntityStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
                return Optional.of(body);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.warnf(e, "Unable to read server request entity: %s", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> getResponseBody(ContainerResponseContext context) {
        try {
            if (!context.hasEntity()) {
                return Optional.empty();
            } else if (isJson(context.getHeaders())) {
                return Optional.of(objectMapper.writeValueAsString(context.getEntity()));
            } else {
                return Optional.of("<Not JSON>");
            }
        } catch (JsonProcessingException e) {
            logger.warnf(e, "Not able to convert response entity (%s) to JSON: %s", context.getEntityClass(), e.getMessage());
            return Optional.empty();
        }
    }
}
