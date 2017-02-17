/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.connectors.jms.client;

import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaConnector;
import org.ballerinalang.natives.connectors.AbstractNativeConnector;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.osgi.service.component.annotations.Component;

/**
 * Native JMS Connector.
 */
@BallerinaConnector(
        packageName = "ballerina.net.jms",
        connectorName = ClientConnector.CONNECTOR_NAME,
        args = { @Argument(name = "initialContextFactory", type = TypeEnum.STRING),
                 @Argument(name = "jndiProviderUrl", type = TypeEnum.STRING) })
@Component(
        name = "ballerina.net.connectors.jms",
        immediate = true,
        service = AbstractNativeConnector.class)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Native JMS Client Connector") })
public class ClientConnector extends AbstractNativeConnector {

    public static final String CONNECTOR_NAME = "ClientConnector";

    private String initialContextFactory;
    private String jndiProviderUrl;

    public ClientConnector(SymbolScope enclosingScope) {
        super(enclosingScope);
    }

    @Override
    public boolean init(BValue[] bValueRefs) {
        if (bValueRefs != null && bValueRefs.length == 2 && !bValueRefs[0].stringValue().equals("") &&
            !bValueRefs[1].stringValue().equals("")) {
            initialContextFactory = bValueRefs[0].stringValue();
            jndiProviderUrl = bValueRefs[1].stringValue();
        } else {
            throw new BallerinaException("Connector parameters not defined correctly.");
        }
        return true;
    }


    @Override
    public AbstractNativeConnector getInstance() {
        return new ClientConnector(symbolScope);
    }

    String getInitialContextFactory() {
        return initialContextFactory;
    }

    String getJndiProviderUrl() {
        return jndiProviderUrl;
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return (pkgPath + ":" + typeName).hashCode();
    }
}
