/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

/**
 * The class {@code ProjectContext} offers an API to the environment in
 * which the project runs to manipulate the project.
 *
 * @since 2.0.0
 */
public class ProjectContext {
    private Package currentPackage;

    ProjectContext() {
    }

    public Package currentPackage() {
        // TODO Handle concurrent read/write to the currentPackage variable
        return currentPackage;
    }

    private void setCurrentPackage(Package currentPackage) {
        // TODO Handle concurrent read/write to the currentPackage variable
        this.currentPackage = currentPackage;
    }

    public void addPackage(PackageConfig packageConfig) {
        // TODO 1) Create the Package instance by first creating PackageContext instance
        // TODO 2) Set the created package instance as the currentPackage
        Package newPackage = Package.from(packageConfig);
        setCurrentPackage(newPackage);
    }
}
