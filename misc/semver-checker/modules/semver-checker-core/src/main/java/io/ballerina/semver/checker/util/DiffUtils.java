/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.semver.checker.util;

import io.ballerina.semver.checker.diff.FunctionDiff;
import io.ballerina.semver.checker.diff.ModuleDiff;
import io.ballerina.semver.checker.diff.PackageDiff;

/**
 * Diff model related utilities.
 *
 * @since 2201.2.0
 */
public class DiffUtils {

    /**
     * Retrieves package name of the given {@link PackageDiff} instance.
     *
     * @param packageDiff PackageDiff instance
     */
    public static String getPackageName(PackageDiff packageDiff) {
        switch (packageDiff.getType()) {
            case NEW:
                return packageDiff.getNewPackage().orElseThrow().packageName().value();
            case REMOVED:
                return packageDiff.getOldPackage().orElseThrow().packageName().value();
            case MODIFIED:
            case UNKNOWN:
            default:
                if (packageDiff.getNewPackage().isPresent()) {
                    return packageDiff.getNewPackage().orElseThrow().packageName().value();
                } else if (packageDiff.getOldPackage().isPresent()) {
                    return packageDiff.getOldPackage().orElseThrow().packageName().value();
                } else {
                    return "unknown";
                }
        }
    }

    /**
     * Retrieves module name of the given {@link ModuleDiff} instance.
     *
     * @param moduleDiff ModuleDiff instance
     */
    public static String getModuleName(ModuleDiff moduleDiff) {
        switch (moduleDiff.getType()) {
            case NEW:
                return moduleDiff.getNewModule().orElseThrow().moduleName().toString();
            case REMOVED:
                return moduleDiff.getOldModule().orElseThrow().moduleName().toString();
            case MODIFIED:
            case UNKNOWN:
            default:
                if (moduleDiff.getNewModule().isPresent()) {
                    return moduleDiff.getNewModule().orElseThrow().moduleName().toString();
                } else if (moduleDiff.getOldModule().isPresent()) {
                    return moduleDiff.getOldModule().orElseThrow().moduleName().toString();
                } else {
                    return "unknown";
                }
        }
    }

    /**
     * Retrieves function name of the given {@link FunctionDiff} instance.
     *
     * @param functionDiff FunctionDiff instance
     */
    public static String getFunctionName(FunctionDiff functionDiff) {
        switch (functionDiff.getType()) {
            case NEW:
                return functionDiff.getNewNode().orElseThrow().functionName().text();
            case REMOVED:
                return functionDiff.getOldNode().orElseThrow().functionName().text();
            case MODIFIED:
            case UNKNOWN:
            default:
                if (functionDiff.getNewNode().isPresent()) {
                    return functionDiff.getNewNode().orElseThrow().functionName().text();
                } else if (functionDiff.getOldNode().isPresent()) {
                    return functionDiff.getOldNode().orElseThrow().functionName().text();
                } else {
                    return "unknown";
                }
        }
    }
}
