/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.util;

import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;

import static org.wso2.ballerinalang.compiler.util.Constants.MAIN_FUNCTION_NAME;

/**
 * A util class for handling common functions across compiler.
 *
 * @since 0.965.0
 */
public class CompilerUtils {

    private static final String DISTRIBUTED_TRANSACTIONS = "distributed.transactions";
    
    public static boolean isDistributedTransactionsEnabled() {
        boolean distributedTransactionEnabled = true; //TODO:Default will be true. Read from new VMOptions
        String distributedTxEnabledProp = System.getProperty(DISTRIBUTED_TRANSACTIONS);
        if (distributedTxEnabledProp != null) {
            distributedTransactionEnabled = Boolean.valueOf(distributedTxEnabledProp);
        }
        return distributedTransactionEnabled;
    }

    public static boolean isMainFunction(BLangFunction funcNode) {
        return MAIN_FUNCTION_NAME.equals(funcNode.name.value) && Symbols.isPublic(funcNode.symbol);
    }

    public static boolean getBooleanValueIfSet(CompilerOptions compilerOptions, CompilerOptionName optionName) {

        return compilerOptions.isSet(optionName) && Boolean.parseBoolean(compilerOptions.get(optionName));
    }

    public static String getMajorVersion(String version) {
        return version.split("\\.")[0];
    }

    public static String getPackageIDStringWithMajorVersion(PackageID packageID) {
        if (Names.DOT.equals(packageID.name)) {
            return packageID.name.value;
        }
        String org = "";
        if (packageID.orgName != null && !packageID.orgName.equals(Names.ANON_ORG)) {
            org = packageID.orgName + Names.ORG_NAME_SEPARATOR.value;
        }
        if (packageID.version.equals(Names.EMPTY)) {
            return org + packageID.name.value;
        }
        return org + packageID.name + Names.VERSION_SEPARATOR.value + getMajorVersion(packageID.version.value);
    }

}
