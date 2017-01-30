package org.wso2.ballerina.tooling.service.workspace.swagger.factories;


import org.wso2.ballerina.tooling.service.workspace.swagger.impl.ServicesApiServiceImpl;

/**
 * Service factory class which returns service implementation objects on demand.
 */
public class ServicesApiServiceFactory {
    private final static ServicesApiServiceImpl service = new ServicesApiServiceImpl();

    public static ServicesApiServiceImpl getServicesApi() {
        return service;
    }
}
