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
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.nativeimpl.connectors.http.server.HTTPListenerManager;
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.deployer.BalDeployer;
import org.wso2.ballerina.core.runtime.errors.handler.ServerConnectorErrorHandler;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportListenerManager;
import org.wso2.carbon.messaging.TransportSender;

import java.io.File;

import static org.wso2.ballerina.core.runtime.Constants.SYSTEM_PROP_BAL_FILE;


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
        try {
            log.info("Starting Ballerina...!");

            //Creating the processor and registering the service
            bundleContext.registerService(CarbonMessageProcessor.class, new MessageProcessor(), null);

            // Registering HTTP Listener Manager with transport framework
            bundleContext.registerService(TransportListenerManager.class, HTTPListenerManager.getInstance(), null);

            //Determine the runtime mode
            String runThisBalFile = System.getProperty(SYSTEM_PROP_BAL_FILE);
            if (runThisBalFile != null) {
                ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.RUN_FILE);
                if (log.isDebugEnabled()) {
                    log.debug("Runtime mode is set to : " + Constants.RuntimeMode.RUN_FILE);
                }
                // Check for file existence before calling the deployer
                File f = new File(runThisBalFile);
                if (f.exists()) {
                    BalDeployer.deployBalFile(f);
                } else {
                    // Check whether this is path relative to the bin directory (ballerina.sh)
                    String relativePath = System.getProperty("user.dir") + "/bin/" + runThisBalFile;
                    File fRelative = new File(relativePath);
                    if (fRelative.exists()) {
                        BalDeployer.deployBalFile(fRelative);
                    } else {
                        throw new BallerinaException("File " + runThisBalFile + " not found in the given location");
                    }
                }
            }

        } catch (BallerinaException ex) {
            String msg = "Error while loading Ballerina runtime";
            log.error(msg, ex);
            //throw new ParserException(msg, ex);
        } catch (Exception ex) {
            String msg = "Error while loading Ballerina";
            log.error(msg, ex);
            //throw new ParserException(msg, ex);
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
