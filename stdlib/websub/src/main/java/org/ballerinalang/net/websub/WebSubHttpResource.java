package org.ballerinalang.net.websub;

import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpService;

/**
 * WebSub HTTP wrapper for the {@code Resource} implementation. TODO: remove dependency on HTTP resource
 *
 * @since 0.965.0
 */
class WebSubHttpResource extends HttpResource {

    private WebSubHttpResource(AttachedFunction resource, HttpService parentService) {
        super(resource, parentService);
    }

    /**
     * Builds the WebSub HTTP resource representation for the resource.
     *
     * @param resource      the resource of the service for which the HTTP resource is built
     * @param httpService   the HTTP service representation of the service
     * @return  the built HTTP resource
     */
    static HttpResource buildWebSubHttpResource(AttachedFunction resource, HttpService httpService) {
        WebSubHttpResource httpResource = new WebSubHttpResource(resource, httpService);
        MapValue resourceConfigAnnotation = getResourceConfigAnnotation(resource);

        if (resourceConfigAnnotation != null) {
            throw new BallerinaException("resourceConfig annotation not allowed for WebSubSubscriber resource");
        }

        httpResource.setPath("/");
        return httpResource;
    }
}
