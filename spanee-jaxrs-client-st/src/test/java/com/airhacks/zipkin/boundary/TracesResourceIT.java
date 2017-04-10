/*
 */
package com.airhacks.zipkin.boundary;

import com.airhacks.spanee.boundary.SpanEEClientRequestFilter;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author airhacks.com
 */
public class TracesResourceIT {

    private Client client;
    private WebTarget tut;

    @Before
    public void init() {

        this.client = ClientBuilder.newBuilder()
                .register(new SpanEEClientRequestFilter("http://localhost:9411"))
                .build();
        this.tut = this.client.target("http://localhost:8080/tracee-jaxrs/resources/traces");
    }

    @Test
    public void traces() {
        String result = this.tut.request().get(String.class);
        System.out.println("result = " + result);
    }

}
