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
package org.wso2.ballerina.nativeimpl.connectors.http;

import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.Attribute;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAnnotation;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

/**
 * Native HTTP Connector.
 */
@BallerinaConnector(
        packageName = ClientConnector.CONNECTOR_PACKAGE,
        connectorName = ClientConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "serviceUri", type = TypeEnum.STRING)
        })
@Component(
        name = "ballerina.net.connectors.http",
        immediate = true,
        service = AbstractNativeConnector.class)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
value = "Native HTTP Client Connector") })
public class ClientConnector extends AbstractNativeConnector {

    public ClientConnector(SymbolScope enclosingScope) {
        super(enclosingScope);
    }
    
    public static final String CONNECTOR_PACKAGE = "ballerina.net.http";
    public static final String CONNECTOR_NAME = "ClientConnector";

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
    public ClientConnector getInstance() {
        return new ClientConnector(symbolScope);
    }

    public String getServiceUri() {
        return serviceUri;
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return (pkgPath + ":" + typeName).hashCode();
    }
}
