/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.test.resolution.packages.internal;

import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.environment.ModuleLoadRequest;

/**
 * Contains utility methods used throughout the test framework.
 *
 * @since 2.0.0
 */
public class Utils {
    private Utils() {
    }

    public static PackageDependencyScope getDependencyScope(Object scopeValue) {
        if (scopeValue != null) {
            String scopeStr = scopeValue.toString();
            if (scopeStr.equals("testOnly")) {
                return PackageDependencyScope.TEST_ONLY;
            } else {
                throw new IllegalStateException("Unsupported scope: " + scopeStr);
            }
        } else {
            return PackageDependencyScope.DEFAULT;
        }
    }

    public static ModuleLoadRequest getModuleLoadRequest(String name,
                                                         PackageDependencyScope scope,
                                                         DependencyResolutionType resolutionType) {
        String[] split = name.split("/");
        if (split.length != 2) {
            throw new IllegalArgumentException("Import declarations in app.dot file " +
                    "should have <org>/<module-name> format");
        }
        return new ModuleLoadRequest(PackageOrg.from(split[0]), split[1], scope, resolutionType);
    }

    public static PackageDescriptor getPkgDescFromNode(String name, String repo) {
        String[] split = name.split("/");
        String[] split1 = split[1].split(":");
        return PackageDescriptor.from(PackageOrg.from(split[0]), PackageName.from(split1[0]),
                PackageVersion.from(split1[1]), repo);
    }

    public static PackageDescriptor getPkgDescFromNode(String name) {
        return getPkgDescFromNode(name, null);
    }

    public static ModuleName getModuleName(PackageName packageName, String moduleNameStr) {
        ModuleName moduleName;
        if (packageName.value().equals(moduleNameStr)) {
            moduleName = ModuleName.from(packageName);
        } else {
            String moduleNamePart = moduleNameStr.substring(packageName.value().length() + 1);
            if (moduleNamePart.isEmpty()) {
                moduleNamePart = null;
            }
            moduleName = ModuleName.from(packageName, moduleNamePart);
        }
        return moduleName;
    }
}
