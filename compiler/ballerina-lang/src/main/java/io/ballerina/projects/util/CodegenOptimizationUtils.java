/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projects.util;

import org.ballerinalang.model.elements.PackageID;

import static io.ballerina.projects.util.CodegenOptimizationConstants.BALLERINAX;
import static io.ballerina.projects.util.CodegenOptimizationConstants.BALLERINA_JBALLERINA_JAVA;
import static io.ballerina.projects.util.CodegenOptimizationConstants.BALLERINA_LANG;
import static io.ballerina.projects.util.CodegenOptimizationConstants.BALLERINA_OBSERVE;
import static io.ballerina.projects.util.CodegenOptimizationConstants.DOT_DRIVER;

/**
 * Common utils for codegen optimization implementation.
 *
 * @since 2201.10.0
 */
public final class CodegenOptimizationUtils {

    private CodegenOptimizationUtils(){
    }

    public static boolean isObserveModule(PackageID packageID) {
        return packageID.getPackageNameWithOrg().equals(BALLERINA_OBSERVE);
    }

    public static boolean isDriverModule(PackageID packageID) {
        return packageID.getPackageNameWithOrg().startsWith(BALLERINAX) &&
                packageID.getPackageNameWithOrg().endsWith(DOT_DRIVER);
    }

    public static boolean isLangLibModule(PackageID packageID) {
        return packageID.getPackageNameWithOrg().startsWith(BALLERINA_LANG);
    }

    public static boolean isJBallerinaModule(PackageID packageID) {
        return packageID.getPackageNameWithOrg().equals(BALLERINA_JBALLERINA_JAVA);
    }

    public static boolean isWhiteListedModule(PackageID packageID) {
        return isObserveModule(packageID) || isDriverModule(packageID) || isJBallerinaModule(packageID)
                || isLangLibModule(packageID);
    }
}
