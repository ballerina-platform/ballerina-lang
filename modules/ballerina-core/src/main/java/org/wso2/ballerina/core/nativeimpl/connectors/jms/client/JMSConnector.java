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

package org.wso2.ballerina.core.nativeimpl.connectors.jms.client;

import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

/**
 * Native JMS Connector.
 */
@BallerinaConnector(
        packageName = "ballerina.net.jms",
        connectorName = JMSConnector.CONNECTOR_NAME,
        args = { @Argument(name = "initialContextFactory", type = TypeEnum.STRING),
                 @Argument(name = "jndiProviderUrl", type = TypeEnum.STRING) })
@Component(
        name = "ballerina.net.connectors.jms",
        immediate = true,
        service = AbstractNativeConnector.class)

public class JMSConnector extends AbstractNativeConnector {

    public static final String CONNECTOR_NAME = "JMSConnector";

    private String initialContextFactory;
    private String jndiProviderUrl;

    @Override public boolean init(BValue[] bValueRefs) {
        if (bValueRefs != null && bValueRefs.length == 2) {
            initialContextFactory = bValueRefs[0].stringValue();
            jndiProviderUrl = bValueRefs[1].stringValue();
        }
        return true;
    }


    @Override public AbstractNativeConnector getInstance() {
        return new JMSConnector();
    }

    @Override public String getPackageName() {
        return null;
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public String getJndiProviderUrl() {
        return jndiProviderUrl;
    }
}
