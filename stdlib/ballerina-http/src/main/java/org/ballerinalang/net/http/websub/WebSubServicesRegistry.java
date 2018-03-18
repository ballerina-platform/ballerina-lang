package org.ballerinalang.net.http.websub;

import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.http.WebSocketServicesRegistry;

import java.util.Map;

/**
 * The WebSub service registry which uses an {@link HTTPServicesRegistry} to maintain WebSub Subscriber HTTP services.
 *
 * @since 0.965.0
 */
public class WebSubServicesRegistry  {

    private HTTPServicesRegistry httpServicesRegistry = new HTTPServicesRegistry(new WebSocketServicesRegistry());

    HTTPServicesRegistry getHttpServicesRegistry() {
        return httpServicesRegistry;
    }

    public void registerWebSubSubscriberService(Service service) {
        httpServicesRegistry.registerWebSubSubscriberService(service);
    }

    public Map<String, HttpService> getServicesInfoByInterface() {
        return httpServicesRegistry.getServicesInfoByInterface();
    }

}
