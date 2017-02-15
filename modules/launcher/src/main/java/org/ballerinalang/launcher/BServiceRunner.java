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
package org.ballerinalang.launcher;

import org.wso2.ballerina.core.debugger.DebugManager;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.nonblocking.ModeResolver;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.nativeimpl.connectors.BallerinaConnectorManager;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.registry.ApplicationRegistry;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Starts Ballerina services
 *
 * @since 0.8.0
 */
class BServiceRunner {

    private static PrintStream outStream = System.err;

    static void start(Path[] serviceFilePaths) {
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());

        for (Path serviceFilePath : serviceFilePaths) {
            try {
                start(serviceFilePath);
            } catch (BLauncherException e) {
                outStream.println("error: fail to deploy service(s) in " + LauncherUtils.getFileName(serviceFilePath));
                LauncherUtils.printLauncherException(e, outStream);
                // Continuing service deployment
            }
        }

        // TODO
        //outStream.println("ballerina: server startup in 500 ms");
    }

    private static void start(Path serviceFilePath) {
        BallerinaFile bFile = LauncherUtils.buildLangModel(serviceFilePath);
        String fileName = LauncherUtils.getFileName(serviceFilePath);

        if (bFile.getServices().length == 0) {
            throw LauncherUtils.createLauncherException("error: no service(s) found in " + fileName);
        }

        try {
            // Create a runtime environment for this Ballerina application
            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bFile);

            // Get the existing application associated with this ballerina config
            Application app = ApplicationRegistry.getInstance().getApplication(fileName);
            if (app == null) {
                // Create a new application with ballerina file name, if there is no app currently exists.
                app = new Application(fileName);
                app.setRuntimeEnv(runtimeEnv);
                ApplicationRegistry.getInstance().registerApplication(app);
            }

            Package aPackage = app.getPackage(fileName);
            if (aPackage == null) {
                // check if package name is null
                if (bFile.getPackagePath() != null) {
                    aPackage = new Package(bFile.getPackagePath());
                } else {
                    aPackage = new Package("default");
                }
                app.addPackage(aPackage);
            }
            aPackage.addFiles(bFile);

            // Here we need to link all the resources with this application. We execute the matching resource
            // when a request is made. At that point, we need to access runtime environment to execute the resource.
            for (Service service : bFile.getServices()) {
                for (Resource resource : service.getResources()) {
                    resource.setApplication(app);
                }
            }

            //if remote debugging is enables we will start listening for debug connections.
            if (ModeResolver.getInstance().isDebugEnabled()) {
                DebugManager debugManager = DebugManager.getInstance();
                // This will start the websocket server.
                debugManager.init();
            }

            ApplicationRegistry.getInstance().updatePackage(aPackage);
            outStream.println("ballerina: deployed service(s) in " + LauncherUtils.getFileName(serviceFilePath));
        } catch (Throwable e) {
            throw LauncherUtils.createLauncherException(LauncherUtils.getFileName(serviceFilePath) + ": " +
                    LauncherUtils.makeFirstLetterUpperCase(e.getMessage()));
        }
    }
}
