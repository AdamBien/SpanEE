/*
 */
package com.airhacks.tracee.boundary;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author airhacks.com
 */
public class TracEEIT {

    TracEE cut;

    @Before
    public void initClient() {
        this.cut = new TracEE("http://localhost:9411/api/v1/spans", true);
    }

    @Test
    public void saveSpan() throws InterruptedException {

        String parentSpanId = this.cut.saveParentSpan("storeRegistration", "registration-service", "127.0.0.1", 4000 * 1000);
        System.out.println("parentSpanId = " + parentSpanId);
        Thread.sleep(500);

        String childSpanId = this.cut.saveChildSpan(parentSpanId, "validateAddress", "validation-service", "127.0.0.1", (2500 * 1000));
        System.out.println("childSpan = " + childSpanId);

        Thread.sleep(1000);
        childSpanId = this.cut.saveChildSpan(parentSpanId, "sendMail", "mailingService", "128.0.0.1", (1500 * 1000));
        System.out.println("childSpanId = " + childSpanId);
        
    }

}
