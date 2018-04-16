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

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A util class for handling common functions across compiler.
 *
 * @since 0.965.0
 */
public class CompilerUtils {

    private static final String DISTRIBUTED_TRANSACTIONS = "distributed.transactions";
    
    private static final String MAIN_FUNCTION_NAME = "main";

    public static boolean isDistributedTransactionsEnabled() {
        boolean distributedTransactionEnabled = true; //TODO:Default will be true. Read from new VMOptions
        String distributedTxEnabledProp = System.getProperty(DISTRIBUTED_TRANSACTIONS);
        if (distributedTxEnabledProp != null) {
            distributedTransactionEnabled = Boolean.valueOf(distributedTxEnabledProp);
        }
        return distributedTransactionEnabled;
    }
    
    public static boolean isMainFunction(BLangFunction funcNode) {
        if (!MAIN_FUNCTION_NAME.equals(funcNode.name.value)) {
            return false;
        }
        BInvokableSymbol symbol = funcNode.symbol;
        if (!(symbol.params.size() == 0 && symbol.defaultableParams.size() == 0
                && symbol.restParam != null && symbol.retType.tag == TypeTags.NIL)) {
            return false;
        }
        if (symbol.restParam.type.tag == TypeTags.ARRAY) {
            BArrayType argsType = (BArrayType) symbol.restParam.type;
            if (argsType.eType.tag == TypeTags.STRING) {
                return true;
            }
        }
        return false;
    }
    
}
