/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.transport.http.netty.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;

/**
 * Declarative service component for the Netty transport. This handles registration &amp; unregistration of relevant
 * OSGi services.
 */
@Component(
        name = "org.wso2.carbon.transport.http.netty.internal.HTTPTransportServiceComponent",
        immediate = true)
@Deprecated
public class HTTPTransportServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(HTTPTransportServiceComponent.class);

    private static final String CHANNEL_ID_KEY = "channel.id";

    private HTTPTransportContextHolder dataHolder = HTTPTransportContextHolder.getInstance();

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do
    }

    @Reference(
            name = "message-processor",
            service = CarbonMessageProcessor.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeMessageProcessor")
    protected void addMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        HTTPTransportContextHolder.getInstance().setMessageProcessor(carbonMessageProcessor);
    }

    protected void removeMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        HTTPTransportContextHolder.getInstance().removeMessageProcessor(carbonMessageProcessor);
    }

    @Reference(
            name = "messaging-handler",
            service = MessagingHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeNettyStatHandler")
    protected void addNettyStatHandler(MessagingHandler messagingHandler) {
        HTTPTransportContextHolder.getInstance().getHandlerExecutor().addHandler(messagingHandler);
    }

    protected void removeNettyStatHandler(MessagingHandler messagingHandler) {
        HTTPTransportContextHolder.getInstance().getHandlerExecutor().removeHandler(messagingHandler);
    }
}
