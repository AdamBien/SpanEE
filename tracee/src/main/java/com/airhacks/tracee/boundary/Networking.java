
package com.airhacks.tracee.boundary;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 *
 * @author airhacks.com
 */
public interface Networking {

    static String extractIpAddress(URI baseUri) {
        String host = baseUri.getHost();
        InetAddress address = null;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            return "127.0.0.1";
        }
        return address.getHostAddress();
    }

    static String extractServiceName(URI absolutePath) {
        return absolutePath.getPath();
    }


}
