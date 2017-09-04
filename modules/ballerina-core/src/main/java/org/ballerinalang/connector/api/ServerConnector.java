package org.ballerinalang.connector.api;

import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Created by rajith on 9/4/17.
 */
public interface ServerConnector {

    String getProtocolPackage();

    void serviceRegistered(Service service)throws BallerinaException;

    void serviceUnregistered(Service service)throws BallerinaException;
}
