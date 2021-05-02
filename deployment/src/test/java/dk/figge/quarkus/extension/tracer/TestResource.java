package dk.figge.quarkus.extension.tracer;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class TestResource implements RestService {

    @Context
    UriInfo uriInfo;

    @Override
    public Response hello() {
        return Response.ok().build();
    }

    @Override
    public Response restClient() {
        RestService client = RestClientBuilder.newBuilder().baseUri(uriInfo.getBaseUri()).build(RestService.class);
        client.hello();
        return Response.ok().build();
    }

    @Override
    public Response model(TestModel input) {
        return Response.ok(new TestModel("test response")).build();
    }
}
