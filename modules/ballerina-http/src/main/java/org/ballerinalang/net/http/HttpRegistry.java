package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.Registry;

/**
 * Created by rajith on 9/4/17.
 */
public class HttpRegistry implements Registry {
    HttpServerConnector httpServerConnector;

    public HttpRegistry(HttpServerConnector httpServerConnector) {
        this.httpServerConnector = httpServerConnector;
    }

    public HttpServerConnector getHttpServerConnector() {
        return httpServerConnector;
    }
}
