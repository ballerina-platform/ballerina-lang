package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.uri.URITemplate;
import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.net.uri.parser.Literal;

/**
 * Created by rajith on 9/4/17.
 */
public class HttpService implements Service {
    private Service service;
    private URITemplate uriTemplate;

    public HttpService(Service service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return service.getName();
    }

    @Override
    public Annotation getAnnotation(String pkgPath, String name) {
        return service.getAnnotation(pkgPath, name);
    }

    @Override
    public Resource[] getResources() {
        return service.getResources();
    }

    public URITemplate getUriTemplate() throws URITemplateException {
        if (uriTemplate == null) {
            uriTemplate = new URITemplate(new Literal("/"));
        }
        return uriTemplate;
    }
}
