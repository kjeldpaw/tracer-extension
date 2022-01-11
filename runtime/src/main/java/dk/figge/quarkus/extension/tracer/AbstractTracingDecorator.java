package dk.figge.quarkus.extension.tracer;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

public abstract class AbstractTracingDecorator {
    protected final TracingConfig tracingConfig;
    protected final ObjectMapper objectMapper;

    public AbstractTracingDecorator(TracingConfig tracingConfig, ObjectMapper objectMapper) {
        this.tracingConfig = tracingConfig;
        this.objectMapper = objectMapper;
    }

    protected boolean isJson(final MultivaluedMap<String, ?> headers) {
        if (headers == null) {
            return false;
        } else {
            final var contentTypes = headers.entrySet().stream()
                    .filter(entry -> "Content-Type".equalsIgnoreCase(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findAny();
            return contentTypes.map(types -> types.stream().anyMatch(type -> MediaType.APPLICATION_JSON.equals(type) || MediaType.APPLICATION_JSON_TYPE.equals(type))).orElse(false);
        }
    }
}
