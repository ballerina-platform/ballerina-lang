/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.ballerina.core.nativeimpl.connectors.http.client;

import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

/**
 * Native HTTP Connector.
 */
@BallerinaConnector(
        packageName = "ballerina.net.http",
        connectorName = HTTPConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "serviceUri", type = TypeEnum.STRING)
        })
@Component(
        name = "ballerina.net.connectors.http",
        immediate = true,
        service = AbstractNativeConnector.class)
public class HTTPConnector extends AbstractNativeConnector {

    public static final String CONNECTOR_NAME = "HTTPConnector";

    private String serviceUri;

    @Override
    public boolean init(BValue[] bValueRefs) {
        if (bValueRefs != null && bValueRefs.length == 1) {
            serviceUri = bValueRefs[0].stringValue();
        }
        return true;
    }

    //TODO Fix Issue#320
    @Override
    public HTTPConnector getInstance() {
        return new HTTPConnector();
    }

    public String getServiceUri() {
        return serviceUri;
    }

    @Override
    public String getPackageName() {
        return "ballerina.net.http";
    }
}
