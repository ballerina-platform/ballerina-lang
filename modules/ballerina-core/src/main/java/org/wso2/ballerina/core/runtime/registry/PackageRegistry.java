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

package org.wso2.ballerina.core.runtime.registry;

import org.wso2.ballerina.core.model.NativeFunction;
import org.wso2.ballerina.core.model.Package;

import java.util.HashMap;

/**
 * The place where all the package definitions are stored
 *
 * @since 1.0.0
 */
public class PackageRegistry {

    HashMap<String, Package> packages = new HashMap<String, Package>();

    private static PackageRegistry instance = new PackageRegistry();

    private PackageRegistry() {
    }

    public static PackageRegistry getInstance() {
        return instance;
    }

    public void registerPackage(Package aPackage) {
        packages.put(aPackage.getFullQualifiedName(), aPackage);
    }

    public Package getPackage(String fqn) {
        return packages.get(fqn);
    }

    /**
     * Register Native Function.
     *
     * @param function AbstractNativeFunction instance.
     */
    public void registerNativeFunction(NativeFunction function) {
        Package aPackage = packages.get(function.getPackageName());
        if (aPackage == null) {
            aPackage = new Package(function.getPackageName());
            packages.put(function.getPackageName(), aPackage);
        }
        if (function.isPublic()) {
            aPackage.getPublicFunctions().put(function.getName(), function);
        } else {
            aPackage.getPrivateFunctions().put(function.getName(), function);
        }
    }

    /**
     * Unregister Native function.
     *
     * @param function AbstractNativeFunction instance.
     */
    public void unregisterNativeFunctions(NativeFunction function) {
        Package aPackage = packages.get(function.getPackageName());
        if (aPackage == null) {
            // Nothing to do.
            return;
        }
        if (function.isPublic()) {
            aPackage.getPublicFunctions().remove(function.getName());
        } else {
            aPackage.getPrivateFunctions().remove(function.getName());
        }
    }

}
