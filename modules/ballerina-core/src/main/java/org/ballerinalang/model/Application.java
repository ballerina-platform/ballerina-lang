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

package org.ballerinalang.model;

import org.ballerinalang.bre.RuntimeEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Application} represents an application in Ballerina.
 * <p>
 * Ballerina Application contains set of {@code BallerinaFile}s organized into {@code Package}s.
 */
public class Application {

    private String appName;
    private Map<String, Package> packages = new HashMap<>();

    // Each application instance should have its own runtime environment
    private RuntimeEnvironment runtimeEnv;

    /**
     * @param appName Name of the application, not null
     */
    public Application(String appName) {
        if (appName == null) {
            //TODO: Throw Ballerina Deployment Exception
        }
        this.appName = appName;
    }

    /**
     * Add a {@code Package} to the application.
     *
     * @param aPackage a Package
     */
    public void addPackage(Package aPackage) {
        packages.put(aPackage.getFullyQualifiedName(), aPackage);
    }

    /**
     * Get list of {@code Package}s in the Application.
     *
     * @return Packages in the Application
     */
    public Map<String, Package> getPackages() {
        return packages;
    }

    /**
     * Get a {@code Package}.
     *
     * @param fqn full qualified name
     * @return a Package
     */
    public Package getPackage(String fqn) {
        return packages.get(fqn);
    }

    /**
     * Remove a package from this application.
     *
     * @param packageName Name of the package
     */
    public void removePackage(String packageName) {
        packages.remove(packageName);
    }

    /**
     * Get the name of the Application.
     *
     * @return name of the Application
     */
    public String getAppName() {
        return appName;
    }

    public RuntimeEnvironment getRuntimeEnv() {
        return runtimeEnv;
    }

    public void setRuntimeEnv(RuntimeEnvironment runtimeEnv) {
        this.runtimeEnv = runtimeEnv;
    }
}
