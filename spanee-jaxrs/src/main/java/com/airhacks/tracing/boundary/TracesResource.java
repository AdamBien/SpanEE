
package com.airhacks.tracing.boundary;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 *
 * @author airhacks.com
 */
@Path("traces")
public class TracesResource {

    @GET
    public String all() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(TracesResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "all traces";
    }

}
