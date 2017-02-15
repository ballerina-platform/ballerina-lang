/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.runtime.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.listner.HTTPListenerManager;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;
import org.wso2.carbon.messaging.TransportListenerManager;
import org.wso2.carbon.messaging.TransportSender;

/**
 * Service component for Ballerina.
 */
@Component(
        name = "org.wso2.ballerina.BallerinaService",
        immediate = true
)
public class BallerinaServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(BallerinaServiceComponent.class);

    @Activate
    protected void start(BundleContext bundleContext) {

        // Load built-in native constructs
        BuiltInNativeConstructLoader.loadConstructs();

        //Creating the processor and registering the service
        bundleContext.registerService(CarbonMessageProcessor.class, new MessageProcessor(), null);

        // Registering HTTP Listener Manager with transport framework
        bundleContext.registerService(TransportListenerManager.class, HTTPListenerManager.getInstance(), null);

        if (log.isDebugEnabled()) {
            log.debug("Ballerina runtime started...!");
        }
    }

    @Reference(
            name = "transport-sender",
            service = TransportSender.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeTransportSender"
    )
    protected void addTransportSender(TransportSender transportSender) {
        ServiceContextHolder.getInstance().addTransportSender(transportSender);
    }

    protected void removeTransportSender(TransportSender transportSender) {
        ServiceContextHolder.getInstance().removeTransportSender(transportSender);
    }

    @Reference(
            name = "global-symbolic-scope",
            service = SymScope.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeSymbolicScope"
    )
    protected void addSymbolicScope(SymScope symScope) {
//        ServiceContextHolder.getInstance().addTransportSender(transportSender);
    }

    protected void removeSymbolicScope(SymScope symScope) {
//        ServiceContextHolder.getInstance().removeTransportSender(transportSender);
    }

    @Reference(
            name = "error-handler",
            service = ServerConnectorErrorHandler.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeErrorHandler"
    )
    protected void addErrorHandler(ServerConnectorErrorHandler errorHandler) {
        ServiceContextHolder.getInstance().registerErrorHandler(errorHandler);
    }

    protected void removeErrorHandler(ServerConnectorErrorHandler errorHandler) {
        ServiceContextHolder.getInstance().unregisterErrorHandler(errorHandler.getProtocol());
    }

}
