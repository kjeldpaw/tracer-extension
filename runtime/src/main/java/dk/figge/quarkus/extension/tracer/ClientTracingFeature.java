package dk.figge.quarkus.extension.tracer;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.util.Collections;

@Provider
public class ClientTracingFeature implements Feature {

    @Inject
    TracingConfig tracingConfig;

    @Override
    public boolean configure(FeatureContext featureContext) {
        final var objectMapper = CDI.current().select(ObjectMapper.class).get();
        final var filter = new ClientTracingFilter(Collections.singletonList(new ClientSpanDecorator(tracingConfig, objectMapper)));
        featureContext.register(filter);
        return true;
    }
}
