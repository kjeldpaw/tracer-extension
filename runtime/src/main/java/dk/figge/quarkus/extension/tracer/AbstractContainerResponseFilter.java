package dk.figge.quarkus.extension.tracer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.util.Optional;

public abstract class AbstractContainerResponseFilter implements ContainerResponseFilter {
    private final static Logger logger = Logger.getLogger(AbstractContainerResponseFilter.class);
    private final ObjectMapper objectMapper;

    public AbstractContainerResponseFilter() {
        this.objectMapper = new ObjectMapper();
    }

    protected Optional<String> getResponseBody(ContainerResponseContext containerResponseContext) {
        try {
            if(containerResponseContext.hasEntity()) {
                return Optional.of(objectMapper.writeValueAsString(containerResponseContext.getEntity()));
            } else {
                return Optional.empty();
            }
        } catch (JsonProcessingException e) {
            logger.warnf("Not able to serialize response body: {0}", e.getMessage(), e);
            return Optional.empty();
        }
    }

}
