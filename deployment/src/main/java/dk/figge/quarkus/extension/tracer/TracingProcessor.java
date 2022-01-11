package dk.figge.quarkus.extension.tracer;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import io.quarkus.resteasy.reactive.spi.CustomContainerRequestFilterBuildItem;
import io.quarkus.resteasy.reactive.spi.CustomContainerResponseFilterBuildItem;

public class TracingProcessor {
    private static final String FEATURE = "tracing-decorator";

    @BuildStep
    void setupFilter(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
                     BuildProducer<FeatureBuildItem> feature,
                     BuildProducer<ResteasyJaxrsProviderBuildItem> providers) {
        feature.produce(new FeatureBuildItem(FEATURE));

        additionalBeans.produce(new AdditionalBeanBuildItem(ClientTracingFeature.class));
        additionalBeans.produce(new AdditionalBeanBuildItem(ServerTracingFeature.class));

        providers.produce(new ResteasyJaxrsProviderBuildItem(ClientTracingFeature.class.getName()));
        providers.produce(new ResteasyJaxrsProviderBuildItem(ServerTracingFeature.class.getName()));
    }
}
