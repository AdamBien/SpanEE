
package com.airhacks.tracee.boundary;

import static com.airhacks.tracee.boundary.TracEE.TRACEE_HEADER;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author airhacks.com
 */
@Provider
public class TracEEContainerRequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private final static String ZIPKIN_URI = "zipkin.uri";
    private final static ConcurrentHashMap<ContainerRequestContext, Long> concurrentRequests = new ConcurrentHashMap<>();

    private final static String API_PATH = "/api/v1/spans";

    private String zipkinHost = "http://localhost:9411";


    private TracEE tracee;

    public TracEEContainerRequestFilter() {
        System.out.println("Instantiated: " + this);
        this.configure();
    }

    public void configure() {
        this.zipkinHost = getZipkinURIFromSystemProperty().
                orElse(getZipkinURIFromEnvironment().
                        orElse(this.zipkinHost)
                );
        this.zipkinHost += API_PATH;
        System.out.println("Using zipkin uri" + this.zipkinHost);
        this.tracee = new TracEE(this.zipkinHost, false);
    }


    Optional<String> getZipkinURIFromSystemProperty() {
        return Optional.ofNullable(System.getProperty(ZIPKIN_URI));
    }

    Optional<String> getZipkinURIFromEnvironment() {
        return Optional.ofNullable(System.getenv().get(ZIPKIN_URI));
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        System.out.println("Request: " + requestContext);
        concurrentRequests.put(requestContext, System.nanoTime());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        System.out.println("Response = " + requestContext + " " + responseContext);
        Long start = concurrentRequests.get(requestContext);
        concurrentRequests.remove(requestContext);
        long duration = (System.nanoTime() - start);
        System.out.println("Duration: " + duration);
        UriInfo uriInfo = requestContext.getUriInfo();

        String ipv4 = extractIpAddress(uriInfo);
        System.out.println("ipv4 = " + ipv4);
        String serviceName = extractServiceName(uriInfo);
        System.out.println("serviceName = " + serviceName);
        String spanName = extractSpanName(uriInfo);
        System.out.println("spanName = " + spanName);
        Optional<String> traceId = extractTraceId(requestContext);
        String spanId = traceId.map(id -> this.tracee.saveChildSpan(id, spanName, serviceName, ipv4, 0)).
                orElseGet(() -> this.tracee.saveParentSpan(spanName, serviceName, ipv4, duration));
        System.out.println("Storing span id: " + spanId);
        storeSpandId(responseContext, spanId);
    }

    String extractSpanName(UriInfo info) {
        return info.getPath();
    }

    String extractIpAddress(UriInfo uriInfo) {
        return Networking.extractIpAddress(uriInfo.getBaseUri());
    }

    String extractServiceName(UriInfo uriInfo) {
        URI absolutePath = uriInfo.getBaseUri();
        return absolutePath.getPath();
    }

    Optional<String> extractTraceId(ContainerRequestContext requestContext) {
        final String correlationId = requestContext.getHeaderString(TRACEE_HEADER);
        System.out.println("correlationId = " + correlationId);
        return Optional.ofNullable(correlationId);
    }

    void storeSpandId(ContainerResponseContext responseContext, String id) {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.putSingle(TRACEE_HEADER, id);
    }

}
