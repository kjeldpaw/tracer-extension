package dk.figge.quarkus.extension.tracer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public interface RestService {

    @GET
    @Path("/hello")
    Response hello();

    @GET
    @Path("/restClient")
    Response restClient();

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Path("/model")
    Response model(TestModel input);
}
