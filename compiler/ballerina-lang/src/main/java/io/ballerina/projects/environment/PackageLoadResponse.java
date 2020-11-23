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
package io.ballerina.projects.environment;

import io.ballerina.projects.Package;

/**
 * {@code PackageLoadResponse} is used to return a response to a given {@code PackageLoadRequest}.
 *
 * @since 2.0.0
 */
public class PackageLoadResponse {
    private final Package resolvedPackage;
    private final PackageLoadRequest packageLoadRequest;

    // TODO We can use this call to send diagnostics if any
    private PackageLoadResponse(Package resolvedPackage, PackageLoadRequest packageLoadRequest) {
        this.resolvedPackage = resolvedPackage;
        this.packageLoadRequest = packageLoadRequest;
    }

    public static PackageLoadResponse from(Package resolvedPackage, PackageLoadRequest packageLoadRequest) {
        return new PackageLoadResponse(resolvedPackage, packageLoadRequest);
    }

    public Package resolvedPackage() {
        return resolvedPackage;
    }

    public PackageLoadRequest packageLoadRequest() {
        return packageLoadRequest;
    }
}
