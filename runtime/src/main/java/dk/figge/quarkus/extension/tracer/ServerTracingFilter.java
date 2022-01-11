package dk.figge.quarkus.extension.tracer;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import io.opentracing.contrib.jaxrs2.server.ServerSpanDecorator;
import io.quarkus.arc.Priority;

import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.util.List;
import java.util.Optional;

@Priority(Priorities.HEADER_DECORATOR + 1)
public class ServerTracingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private final List<ServerSpanDecorator> serverSpanDecorators;

    public ServerTracingFilter(List<ServerSpanDecorator> serverSpanDecorators) {
        this.serverSpanDecorators = serverSpanDecorators;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        getSpan(containerRequestContext).ifPresent(span -> serverSpanDecorators.forEach(serverSpanDecorator -> serverSpanDecorator.decorateRequest(containerRequestContext, span)));
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
        getSpan(containerRequestContext).ifPresent(span -> serverSpanDecorators.forEach(serverSpanDecorator -> serverSpanDecorator.decorateResponse(containerResponseContext, span)));
    }

    private Optional<Span> getSpan(ContainerRequestContext context) {
        Object wrapper = context.getProperty(SpanWrapper.PROPERTY_NAME);
        if (wrapper instanceof SpanWrapper) {
            SpanWrapper spanWrapper = (SpanWrapper) wrapper;
            return spanWrapper.isFinished() ? Optional.empty() : Optional.of(spanWrapper.get());
        } else {
            return Optional.empty();
        }
    }
}
