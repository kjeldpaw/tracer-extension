package dk.figge.quarkus.extension.tracer;

import java.io.Serializable;

public class TestModel implements Serializable {
    private String message;

    public TestModel(String message) {
        this.message = message;
    }

    public TestModel() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
