
package com.airhacks.tracee.boundary;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 *
 * @author airhacks.com
 */
public interface Networking {

    final static String ZIPKIN_URI = "zipkin.uri";
    final static String DEFAULT_ZIPKIN_HOST = "http://localhost:9411";


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

    static String configureBaseURI() {
        return getZipkinURIFromSystemProperty().
                orElse(getZipkinURIFromEnvironment().
                        orElse(DEFAULT_ZIPKIN_HOST)
                );
    }

    static Optional<String> getZipkinURIFromSystemProperty() {
        return Optional.ofNullable(System.getProperty(ZIPKIN_URI));
    }

    static Optional<String> getZipkinURIFromEnvironment() {
        return Optional.ofNullable(System.getenv().get(ZIPKIN_URI));
    }



}
