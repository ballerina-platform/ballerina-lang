/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.common.utils;

import org.wso2.ballerinalang.compiler.util.Name;

import java.nio.file.Paths;
import java.util.List;

/**
 * Common utils to be reuse in language server implementation.
 * */
public class CommonUtil {
    /**
     * Get the package URI to the given package name.
     *
     * @param pkgName        Name of the package that need the URI for
     * @param currentPkgPath String URI of the current package
     * @param currentPkgName Name of the current package
     * @return String URI for the given path.
     */
    public static String getPackageURI(List<Name> pkgName, String currentPkgPath, List<Name> currentPkgName) {
        String newPackagePath;
        // If current package path is not null and current package is not default package continue,
        // else new package path is same as the current package path.
        if (currentPkgPath != null && !currentPkgName.get(0).getValue().equals(".")) {
            int indexOfCurrentPkgName = currentPkgPath.indexOf(currentPkgName.get(0).getValue());
            newPackagePath = currentPkgPath.substring(0, indexOfCurrentPkgName);
            for (Name pkgDir : pkgName) {
                newPackagePath = Paths.get(newPackagePath, pkgDir.getValue()).toString();
            }
        } else {
            newPackagePath = currentPkgPath;
        }
        return newPackagePath;
    }
}
