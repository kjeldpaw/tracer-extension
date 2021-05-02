package dk.figge.quarkus.extension.tracer;

import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import io.quarkus.resteasy.reactive.spi.CustomContainerRequestFilterBuildItem;
import io.quarkus.resteasy.reactive.spi.CustomContainerResponseFilterBuildItem;

public class QuarkusTracingProcessor {
    private static final String FEATURE = "tracer";

    @BuildStep
    void setupFilter(BuildProducer<ResteasyJaxrsProviderBuildItem> providers,
                     BuildProducer<FeatureBuildItem> feature,
                     BuildProducer<CustomContainerResponseFilterBuildItem> customResponseFilters,
                     BuildProducer<CustomContainerRequestFilterBuildItem> customRequestFilters,
                     Capabilities capabilities) {
        feature.produce(new FeatureBuildItem(FEATURE));
        if (capabilities.isPresent(Capability.SMALLRYE_OPENTRACING)) {
            if (capabilities.isPresent(Capability.RESTEASY_REACTIVE)) {
                customRequestFilters.produce(new CustomContainerRequestFilterBuildItem(ReactiveServerTracingRequestFilter.class.getName()));
                customResponseFilters.produce(new CustomContainerResponseFilterBuildItem(ReactiveServerTracingResponseFilter.class.getName()));
            } else if(capabilities.isPresent(Capability.RESTEASY)) {
                providers.produce(new ResteasyJaxrsProviderBuildItem(ServertracingVertxDynamicFeature.class.getName()));
            }
        }

    }
}
