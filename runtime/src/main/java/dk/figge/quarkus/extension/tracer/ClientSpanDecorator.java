package dk.figge.quarkus.extension.tracer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.internal.URIUtils;
import io.opentracing.tag.Tags;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ClientSpanDecorator extends AbstractTracingDecorator implements io.opentracing.contrib.jaxrs2.client.ClientSpanDecorator {
    private static final Logger logger = Logger.getLogger(ClientSpanDecorator.class);


    public ClientSpanDecorator(TracingConfig tracingConfig, ObjectMapper objectMapper) {
        super(tracingConfig, objectMapper);
    }

    @Override
    public void decorateRequest(ClientRequestContext clientRequestContext, Span span) {
        Tags.COMPONENT.set(span, "tracing-decorator");
        Tags.PEER_HOSTNAME.set(span, clientRequestContext.getUri().getHost());
        Tags.PEER_PORT.set(span, clientRequestContext.getUri().getPort());
        Tags.HTTP_METHOD.set(span, clientRequestContext.getMethod());

        final var url = URIUtils.url(clientRequestContext.getUri());
        span.setOperationName(clientRequestContext.getMethod() + " " + url);
        Tags.HTTP_URL.set(span, url);

        if (tracingConfig.includeClientRequestHeaders) {
            getHeaders(clientRequestContext.getHeaders()).ifPresent(headers -> span.setTag("http.request.headers", headers));
        }

        if (tracingConfig.includeClientRequestBody) {
            getRequestBody(clientRequestContext).ifPresent(body -> span.setTag("http.request.body", body));
        }
    }

    @Override
    public void decorateResponse(ClientResponseContext clientResponseContext, Span span) {
        if (tracingConfig.includeClientResponseHeaders) {
            getHeaders(clientResponseContext.getHeaders()).ifPresent(headers -> span.setTag("http.response.headers", headers));
        }
        if (tracingConfig.includeClientResponseStatus) {
            Tags.HTTP_STATUS.set(span, clientResponseContext.getStatus());
        }
        if (tracingConfig.includeClientResponseBody) {
            getResponseBody(clientResponseContext).ifPresent(body -> span.setTag("http.response.body", body));
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

    private Optional<String> getRequestBody(ClientRequestContext context) {
        try {
            if (!context.hasEntity()) {
                return Optional.empty();
            } else if (isJson(context.getHeaders())) {
                return Optional.of(objectMapper.writeValueAsString(context.getEntity()));
            } else {
                return Optional.of("<Not JSON>");
            }
        } catch (JsonProcessingException e) {
            logger.warnf(e, "Not able to convert request entity (%s) to JSON: %s", context.getEntityClass(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> getResponseBody(ClientResponseContext context) {
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
            logger.warnf(e, "Unable to read client response entity: %s", e.getMessage());
            return Optional.empty();
        }
    }


}
