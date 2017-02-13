/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.mockerina.nativeimpl;

import org.ballerinalang.mockerina.MockerinaRegistry;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;
import org.wso2.ballerina.core.runtime.dispatching.ServiceDispatcher;
import org.wso2.ballerina.core.runtime.registry.ApplicationRegistry;
import org.wso2.ballerina.core.runtime.registry.DispatcherRegistry;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Native function ballerina.lang.mock:startServer.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.mock",
        functionName = "startService", args = {
        @Argument(name = "serviceName", type = TypeEnum.STRING) }, returnType = {
        @ReturnType(type = TypeEnum.STRING) }, isPublic = true)
public class StartService extends AbstractNativeFunction {

    static String getFileName(Path sourceFilePath) {
        Path fileNamePath = sourceFilePath.getFileName();
        return (fileNamePath != null) ? fileNamePath.toString() : sourceFilePath.toString();
    }

    /**
     *
     * Starts the service specified in the 'serviceName' argument.
     *
     * The MockerinaRegistry has the original BallerinaFile with all the services.
     * It also contain the corresponding actual BallerinaFile object without any services. (the derivativeBFile)
     * An Application named 'default' is registered for the derivativeBFile.
     *
     */
    @Override
    public BValue[] execute(Context ctx) {
        String serviceName = getArgument(ctx, 0).stringValue();     //todo only one service for now

        Application app = ApplicationRegistry.getInstance().getApplication("default");
        Optional<Service> matchingService = Optional.empty();
        for (BallerinaFile aBallerinaFile : MockerinaRegistry.getInstance().getOriginalBallerinaFiles()) {
            Service[] services = aBallerinaFile.getServices();

            // 1) First, we get the Service for the given serviceName from the original BallerinaFile
            matchingService = Arrays.stream(services).filter(s -> s.getName().equals(serviceName)).findAny();
            if (!matchingService.isPresent()) {
                continue;
            } else {
                // 2) Then, we append that to the list of services in the derivativeBFile.
                BallerinaFile derivativeBallerinaFile = MockerinaRegistry.getInstance()
                        .getDerivativeBallerinaFile(aBallerinaFile); //the actual bfile registered in AppRegistry
                addService(app, matchingService.get(), derivativeBallerinaFile);
            }

        }

        // 5) fail if no matching service for the given 'serviceName' argument is found.
        if (!matchingService.isPresent()) {
            StringBuilder listOfServices = new StringBuilder();
            Arrays.stream(MockerinaRegistry.getInstance().getOriginalBallerinaFiles())
                    .map(BallerinaFile::getServices)
                    .flatMap(Arrays::stream)
                    .forEachOrdered(service -> listOfServices.append(service.getSymbolName().getName()).append(", "));
            throw new BallerinaException("No service with the name " + serviceName + " found. "
                    + "Did you mean to start one of these services? " + listOfServices);
        }

        // 6) return the service url
        BString str = (BString) getArgument(ctx, 0); //todo return the service url
        return getBValues(str);
    }

    private void addService(Application app, Service matchingService, BallerinaFile derivativeBallerinaFile) {

        Service[] registeredServices = derivativeBallerinaFile.getServices();
        registeredServices = Arrays.copyOf(registeredServices, registeredServices.length + 1);
        registeredServices[registeredServices.length - 1] = matchingService;
        derivativeBallerinaFile.setServices(registeredServices);

        // 3) re-generate the runtime environment for the modified bfile
        RuntimeEnvironment runtimeEnvironment = RuntimeEnvironment.get(derivativeBallerinaFile);
        app.setRuntimeEnv(runtimeEnvironment);
        // as done in org.ballerinalang.testerina.core.TestRunner
        for (Resource resource : matchingService.getResources()) {
            resource.setApplication(app);
        }

        Package aPackage = app.getPackage("default");
        aPackage.getServices().add(matchingService);

        // 4) inform the service dispatchers
        //ApplicationRegistry.getInstance().updatePackage(aPackage);
        for (ServiceDispatcher serviceDispatcher : DispatcherRegistry.getInstance().getServiceDispatchers().values()) {
            serviceDispatcher.serviceRegistered(matchingService);
        }
    }

}
