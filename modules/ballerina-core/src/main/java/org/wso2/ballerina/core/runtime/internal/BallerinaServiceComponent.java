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
import org.wso2.ballerina.core.nativeimpl.connectors.http.server.HTTPListenerManager;
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.deployer.BalDeployer;
import org.wso2.ballerina.core.runtime.errors.handler.ServerConnectorErrorHandler;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportListenerManager;
import org.wso2.carbon.messaging.TransportSender;

import java.io.File;

import static org.wso2.ballerina.core.runtime.Constants.SYSTEM_PROP_RUN_FILE;
import static org.wso2.ballerina.core.runtime.Constants.SYSTEM_PROP_RUN_FILE_MODE;


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
        //Creating the processor and registering the service
        bundleContext.registerService(CarbonMessageProcessor.class, new MessageProcessor(), null);

        // Registering HTTP Listener Manager with transport framework
        bundleContext.registerService(TransportListenerManager.class, HTTPListenerManager.getInstance(), null);

        ServiceContextHolder.getInstance().setBundleContext(bundleContext);

        //Determine the runtime mode
        String runThisBalFile = System.getProperty(SYSTEM_PROP_RUN_FILE);
        String runtimeMode = System.getProperty(SYSTEM_PROP_RUN_FILE_MODE);
        if (runThisBalFile != null && runtimeMode != null) {
            if (Constants.SYSTEM_PROP_RUN_FILE_MODE_MAIN.equalsIgnoreCase(runtimeMode)) {
                ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.RUN_MAIN);
            } else if (Constants.SYSTEM_PROP_RUN_FILE_MODE_SERVICE.equalsIgnoreCase(runtimeMode)) {
                ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.RUN_SERVER);
            } else {
                ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
            }
            if (log.isDebugEnabled()) {
                log.debug("Runtime mode is set to : " + ServiceContextHolder.getInstance().getRuntimeMode());
            }
            ServiceContextHolder.getInstance().setRunningFile(new File(runThisBalFile));
        } else if (runThisBalFile != null || runtimeMode != null) {
            // one is not null and other is null. then Can't runtime mode.
            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
        }
        // Else is normal Ballerina Server mode.
        //log.info("Ballerina runtime started...!");
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

    @Reference(
            name = "carbon-service-info",
            service = CarbonServerInfo.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeCarbonServiceInfo"
    )
    protected void addCarbonServiceInfo(CarbonServerInfo info) {
        // We have to wait until Carbon Capability Provider completes its job to shutdown the OSGi runtime properly.
        if (Constants.RuntimeMode.ERROR == ServiceContextHolder.getInstance().getRuntimeMode()) {
            // We shouldn't get here, if we get here, that means a bug in startup scripts.
            log.error("Unable to identify Ballerina Runtime mode.");
            RuntimeUtils.shutdownRuntime();
            return;
        }
        if (Constants.RuntimeMode.SERVER == ServiceContextHolder.getInstance().getRuntimeMode()) {
            return;
        }
        File runningFile = ServiceContextHolder.getInstance().getRunningFile();
        if (runningFile == null) {
            // We shouldn't get here, if we get here, that means a bug in startup order
            log.error("Running File not found in the Service Context. " +
                    "Possible issue with the internal component startup order");
            RuntimeUtils.shutdownRuntime();
            return;
        }
        if (!runningFile.exists()) {
            log.error("File " + runningFile.getName() + " not found in the given location. Bye..!");
            // Shutdown OSGi environment.
            RuntimeUtils.shutdownRuntime();
            return;
        }
        if (Constants.RuntimeMode.RUN_MAIN == ServiceContextHolder.getInstance().getRuntimeMode() ||
                Constants.RuntimeMode.RUN_SERVER == ServiceContextHolder.getInstance().getRuntimeMode()) {
            BalDeployer.deployBalFile(runningFile);
        }
    }

    protected void removeCarbonServiceInfo(CarbonServerInfo info) {
        // Do Nothing.
    }

}
