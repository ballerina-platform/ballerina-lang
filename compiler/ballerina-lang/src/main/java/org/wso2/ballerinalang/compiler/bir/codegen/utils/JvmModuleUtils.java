/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.utils;

import io.ballerina.identifier.Utils;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BALLERINA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BUILT_IN_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ENCODED_DOT_CHARACTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FILE_NAME_PERIOD_SEPERATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_PACKAGE_SEPERATOR;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * The common functions used in CodeGen related to modules.
 */
public class JvmModuleUtils {

    private JvmModuleUtils() {
    }

    public static String getPackageName(PackageID packageID) {
        return getPackageNameWithSeparator(packageID, "/");
    }

    private static String getPackageNameWithSeparator(PackageID packageID, String separator) {
        String packageName = "";
        String orgName = Utils.encodeNonFunctionIdentifier(packageID.orgName.value);
        String moduleName;
        if (!packageID.isTestPkg) {
            moduleName = Utils.encodeNonFunctionIdentifier(packageID.name.value);
        } else {
            moduleName = Utils.encodeNonFunctionIdentifier(packageID.name.value) + Names.TEST_PACKAGE.value;
        }
        if (!moduleName.equals(ENCODED_DOT_CHARACTER)) {
            if (!packageID.version.value.isEmpty()) {
                packageName = getMajorVersion(packageID.version.value) + separator;
            }
            packageName = moduleName + separator + packageName;
        }

        if (!orgName.equalsIgnoreCase("$anon")) {
            packageName = orgName + separator + packageName;
        }
        return packageName;
    }

    public static String getModuleLevelClassName(PackageID packageID, String sourceFileName) {
        return getModuleLevelClassName(packageID, sourceFileName, "/");
    }

    public static String getModuleLevelClassName(PackageID packageID, String sourceFileName, String separator) {
        String className = cleanupSourceFileName(sourceFileName);
        // handle source file path start with '/'.
        if (className.startsWith(JAVA_PACKAGE_SEPERATOR)) {
            className = className.substring(1);
        }
        return getPackageNameWithSeparator(packageID, separator) + className;
    }

    public static String getModuleLevelClassName(PackageID packageID, String prefix, String sourceFileName,
                                                 String separator) {
        String className = cleanupSourceFileName(sourceFileName);
        // handle source file path start with '/'.
        if (className.startsWith(JAVA_PACKAGE_SEPERATOR)) {
            className = className.substring(1);
        }
        return getPackageNameWithSeparator(packageID, separator) + prefix + className;
    }

    public static boolean isBallerinaBuiltinModule(String orgName, String moduleName) {
        return orgName.equals("ballerina") && moduleName.equals("builtin");
    }

    public static PackageID cleanupPackageID(PackageID pkgID) {
        Name org = new Name(Utils.encodeNonFunctionIdentifier(pkgID.orgName.value));
        Name module = new Name(Utils.encodeNonFunctionIdentifier(pkgID.name.value));
        return new PackageID(org, module, pkgID.version);
    }

    public static boolean isBuiltInPackage(PackageID packageID) {
        packageID = cleanupPackageID(packageID);
        return BALLERINA.equals(packageID.orgName.value) && BUILT_IN_PACKAGE_NAME.equals(packageID.name.value);
    }

    public static boolean isSameModule(PackageID moduleId, PackageID importModule) {
        PackageID cleanedPkg = cleanupPackageID(importModule);
        if (!moduleId.orgName.value.equals(cleanedPkg.orgName.value)) {
            return false;
        } else if (!moduleId.name.value.equals(cleanedPkg.name.value)) {
            return false;
        } else {
            return getMajorVersion(moduleId.version.value).equals(getMajorVersion(cleanedPkg.version.value));
        }
    }

    private static String cleanupSourceFileName(String name) {
        return name.replace(".", FILE_NAME_PERIOD_SEPERATOR);
    }
}
