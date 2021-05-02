package dk.figge.quarkus.extension.tracer;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

public class ServertracingVertxDynamicFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
        featureContext.register(new ServerTracingRequestFilter());
        featureContext.register(new ServerTracingResponseFilter());
    }
}
