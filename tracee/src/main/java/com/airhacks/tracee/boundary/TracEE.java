
package com.airhacks.tracee.boundary;

import java.util.Optional;
import static java.util.UUID.randomUUID;
import java.util.function.Supplier;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 *
 * @author airhacks.com
 */
public class TracEE {
    final static String TRACEE_HEADER = "span.id";
    final static String API_PATH = "/api/v1/spans";

    private Client client;
    private WebTarget tut;
    private final boolean clientMode;


    private Logging LOG;

    public TracEE(Supplier<String> uriSupplier, boolean clientMode) {
        String uri = uriSupplier.get();
        this.LOG = System.out::println;
        this.LOG.log("URI: " + uri + " Client mode: " + clientMode);
        this.client = ClientBuilder.newClient();
        this.tut = this.client.target(uri).path(API_PATH);
        this.clientMode = clientMode;
    }


    public String saveParentSpan(String spanName, String serviceName, String ipv4, long durationInNanos) {
        this.LOG.log("Saving parent span");
        long timestamp = System.currentTimeMillis() * 1000;
        String id = this.createId();
        this.saveSpan(null, id, id, spanName, serviceName, ipv4, timestamp, durationInNanos);
        return id;
    }

    public String saveChildSpan(String parentId, String spanName, String serviceName, String ipv4, long durationInNanos) {
        this.LOG.log("Saving child span");
        long timestamp = System.currentTimeMillis() * 1000;
        String id = this.createId();
        this.saveSpan(parentId, id, parentId, spanName, serviceName, ipv4, timestamp, durationInNanos);
        return id;
    }


    void saveSpan(String parentSpanId, String spanId, String traceId, String spanName, String serviceName, String ipv4, long timestamp, long duration) {
        JsonArray span = createSpans(parentSpanId, spanId, traceId, spanName, serviceName, ipv4, timestamp, duration);
        Response response = this.tut.request().post(Entity.json(span));
        final int status = response.getStatus();
        if (status == 202) {
            this.LOG.log("Successfully sent");
            this.LOG.log("span: " + span);
        } else {
            this.LOG.log("Problem sending span. Status: " + status);
        }
    }

    JsonArray createSpans(String parentSpanId, String spanId, String traceId, String spanName, String serviceName, String ipv4, long timestamp, long duration) {
        return Json.createArrayBuilder().add(
                createSpanSlot(parentSpanId, spanId, traceId, spanName, timestamp, duration,
                        createAnnotations(timestamp, duration, serviceName, ipv4))).
                build();
    }

    JsonObject createSpanSlot(String parentSpanId, String spanId, String traceId, String name, long timestamp, long duration, JsonArray annotations) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        Optional.ofNullable(parentSpanId).
                ifPresent(t -> builder.add("parentId", t));

        return builder.
                add("traceId", traceId).
                add("id", spanId).
                add("timestamp", timestamp).
                add("name", name).
                add("duration", duration).
                add("annotations", annotations).
                build();
    }

    JsonArray createAnnotations(long timestamp, long duration, String serviceName, String ipv4) {
        return Json.createArrayBuilder().
                add(createAnnotation(timestamp, getSpanStart(), serviceName, ipv4)).
                add(createAnnotation(timestamp + duration, getSpanEnd(), serviceName, ipv4)).
                build();
    }

    String getSpanStart() {
        if (this.clientMode) {
            return "cr";
        } else {
            return "sr";
        }
    }

    String getSpanEnd() {
        if (this.clientMode) {
            return "cs";
        } else {
            return "ss";
        }
    }

    JsonObject createAnnotation(long timestamp, String annotation, String serviceName, String ipv4) {
        return Json.createObjectBuilder().
                add("timestamp", timestamp).
                add("value", annotation).
                add("endpoint", createEndpoint(serviceName, ipv4)).
                build();
        /*
        [{"timestamp"
            :1485343629961969,
         "value":"sr",
         "endpoint":{"serviceName"

        :"unknown","ipv4":"172.20.10.4"}},
        {"timestamp"
            :1485343629962804,"value":"ss","endpoint":{"serviceName"


        :"unknown","ipv4":"172.20.10.4"}}],"binaryAnnotations":[
        {"key"
            :"http.status_code","value":"200","endpoint":{"serviceName"


        :"unknown","ipv4":"172.20.10.4"}},
        {"key"
            :"http.url","value":"http://localhost:8080/zipkin-jaxrs-client/resources/traces","endpoint":{"serviceName"


        :"unknown","ipv4":"172.20.10.4"}}]
         */

    }

    JsonObject createEndpoint(String serviceName, String ipv4) {
        return Json.createObjectBuilder().
                add("serviceName", serviceName).
                add("ipv4", ipv4).
                build();
    }

    public String createId() {
        return Long.toHexString(randomUUID().getLeastSignificantBits());
    }


}
