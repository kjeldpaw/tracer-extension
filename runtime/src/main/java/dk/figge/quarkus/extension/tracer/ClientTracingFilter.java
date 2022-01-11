package dk.figge.quarkus.extension.tracer;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs2.client.ClientSpanDecorator;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Priority(Priorities.USER)
public class ClientTracingFilter implements ClientRequestFilter, ClientResponseFilter {
    private final List<ClientSpanDecorator> clientSpanDecorators;

    public ClientTracingFilter(List<ClientSpanDecorator> clientSpanDecorators) {
        this.clientSpanDecorators = clientSpanDecorators;
    }

    @Override
    public void filter(final ClientRequestContext clientRequestContext) {
        getSpan(clientRequestContext).ifPresent(span -> clientSpanDecorators.forEach(clientSpanDecorator -> clientSpanDecorator.decorateRequest(clientRequestContext, span)));
    }

    @Override
    public void filter(final ClientRequestContext clientRequestContext, final ClientResponseContext clientResponseContext) throws IOException {
        getSpan(clientRequestContext).ifPresent(span -> clientSpanDecorators.forEach(clientSpanDecorator -> clientSpanDecorator.decorateResponse(clientResponseContext, span)));
    }

    private Optional<Span> getSpan(ClientRequestContext context) {
        Object wrapper = context.getProperty(SpanWrapper.PROPERTY_NAME);
        if(wrapper instanceof SpanWrapper) {
            SpanWrapper spanWrapper = (SpanWrapper) wrapper;
            return spanWrapper.isFinished() ? Optional.empty() : Optional.of(spanWrapper.get());
        } else {
            return Optional.empty();
        }
    }
}
