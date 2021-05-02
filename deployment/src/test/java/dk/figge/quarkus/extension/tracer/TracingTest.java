package dk.figge.quarkus.extension.tracer;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.GlobalTracerTestUtil;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TracingTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(TestResource.class)
                    .addClass(RestService.class)
                    .addClass(TestModel.class)
                    .addAsResource("application.properties")
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
            );

    static MockTracer mockTracer = new MockTracer();

    static {
        GlobalTracer.register(mockTracer);
    }

    @BeforeEach
    public void before() {
        mockTracer.reset();
    }

    @AfterAll
    public static void afterAll() {
        GlobalTracerTestUtil.resetGlobalTracer();
    }

    @Test
    public void testSingleServerRequest() {
        try {
            RestAssured.defaultParser = Parser.TEXT;
            when().get("/hello").then().statusCode(200);
            assertEquals(1, mockTracer.finishedSpans().size());
            assertEquals(2, mockTracer.finishedSpans().get(0).logEntries().size());
            assertEquals("GET:dk.figge.quarkus.extension.tracer.TestResource.hello", mockTracer.finishedSpans().get(0).operationName());

            MockSpan.LogEntry requestLogEntry = mockTracer.finishedSpans().get(0).logEntries().get(0);
            assertEquals("http://localhost:8081/hello", requestLogEntry.fields().get("request.uri"));
            assertEquals("GET", requestLogEntry.fields().get("request.method"));
            assertEquals("No body", requestLogEntry.fields().get("request.body"));

            MockSpan.LogEntry responseLogEntry = mockTracer.finishedSpans().get(0).logEntries().get(1);
            assertEquals("No body", responseLogEntry.fields().get("response.body"));
        } finally {
            RestAssured.reset();
        }
    }

    @Test
    public void testRestClient() {
        try {
            RestAssured.defaultParser = Parser.TEXT;
            when().get("/restClient").then().statusCode(200);

            assertEquals(3, mockTracer.finishedSpans().size());

            assertEquals(2, mockTracer.finishedSpans().get(0).logEntries().size());
            assertEquals("GET:dk.figge.quarkus.extension.tracer.TestResource.hello", mockTracer.finishedSpans().get(0).operationName());

            MockSpan.LogEntry requestLogEntry0 = mockTracer.finishedSpans().get(0).logEntries().get(0);
            assertEquals("No body", requestLogEntry0.fields().get("request.body"));

            MockSpan.LogEntry responseLogEntry0 = mockTracer.finishedSpans().get(0).logEntries().get(1);
            assertEquals("No body", responseLogEntry0.fields().get("response.body"));

            assertEquals("GET", mockTracer.finishedSpans().get(1).operationName());

            assertEquals(2, mockTracer.finishedSpans().get(2).logEntries().size());
            assertEquals("GET:dk.figge.quarkus.extension.tracer.TestResource.restClient", mockTracer.finishedSpans().get(2).operationName());

            MockSpan.LogEntry requestLogEntry2 = mockTracer.finishedSpans().get(2).logEntries().get(0);
            assertEquals("No body", requestLogEntry2.fields().get("request.body"));

            MockSpan.LogEntry responseLogEntry2 = mockTracer.finishedSpans().get(2).logEntries().get(1);
            assertEquals("No body", responseLogEntry2.fields().get("response.body"));
        } finally {
            RestAssured.reset();
        }
    }

    @Test
    public void testServerRequestWithBody() {
        RestAssured.defaultParser = Parser.JSON;

        given()
                .header("Content-Type", "application/json")
                .body(new TestModel("this is a request"))
                .when()
                .post("/model")
                .then()
                .statusCode(200);

        assertEquals("POST:dk.figge.quarkus.extension.tracer.TestResource.model", mockTracer.finishedSpans().get(0).operationName());

        MockSpan.LogEntry requestLogEntry = mockTracer.finishedSpans().get(0).logEntries().get(0);
        assertEquals("POST", requestLogEntry.fields().get("request.method"));
        assertEquals("{\"message\":\"this is a request\"}", requestLogEntry.fields().get("request.body"));

        MockSpan.LogEntry responseLogEntry = mockTracer.finishedSpans().get(0).logEntries().get(1);
        assertEquals("{\"message\":\"test response\"}", responseLogEntry.fields().get("response.body"));
        assertEquals("200", responseLogEntry.fields().get("response.status"));
    }
}
