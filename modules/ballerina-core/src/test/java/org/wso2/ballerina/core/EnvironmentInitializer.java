/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core;

import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.nativeimpl.connectors.BallerinaConnectorManager;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.runtime.registry.ApplicationRegistry;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * {@code EnvironmentInitializr} is responsible for initializing an environment for a particular ballerina file.
 */
public class EnvironmentInitializer {

    public static Application setup(String sourcePath) {
        // Initialize server connectors before starting the test cases
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());
        BallerinaConnectorManager.getInstance().registerServerConnectorErrorHandler(new TestErrorHandler());
        // Resister HTTP Dispatchers

        // Load constructors
        BuiltInNativeConstructLoader.loadConstructs();

        SymScope symScope = GlobalScopeHolder.getInstance().getScope();

        BallerinaFile bFile = ParserUtils.parseBalFile(sourcePath, symScope);
        RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bFile);

        Application app = new Application("default");
        app.setRuntimeEnv(runtimeEnv);

        Package aPackage;
        if (bFile.getPackagePath() != null) {
            aPackage = new Package(bFile.getPackagePath());
        } else {
            aPackage = new Package("default");
        }
        aPackage.addFiles(bFile);
        app.addPackage(aPackage);

        for (Service service : bFile.getServices()) {
            for (Resource resource : service.getResources()) {
                resource.setApplication(app);
            }
        }
        ApplicationRegistry.getInstance().registerApplication(app);
        return app;
    }

    public static void cleanup(Application application) {
        ApplicationRegistry.getInstance().unregisterApplication(application);
    }

}
