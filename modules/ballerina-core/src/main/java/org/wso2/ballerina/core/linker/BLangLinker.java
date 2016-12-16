/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.linker;

import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.interpreter.SymTable;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;

/**
 * {@code BLangLinker} responsible for linking locally defined functions, actions, native functions and native actions
 *
 * @since 1.0.0
 */
public class BLangLinker {

    private BallerinaFile bFile;

    public BLangLinker(BallerinaFile bFile) {
        this.bFile = bFile;
    }

    public void link(SymScope globalSymScope) {
        SymScope packageScope = bFile.getPackageScope();
        packageScope.setParent(globalSymScope);

        SymTable symTable = new SymTable(packageScope);

        FunctionInvocationExpr[] funcIExprs = bFile.getFuncIExprs();
        if (funcIExprs != null) {
            for (FunctionInvocationExpr funcIExpr : funcIExprs) {
                SymbolName symbolName = funcIExpr.getFunctionName();

                Symbol symbol = symTable.lookup(symbolName);
                if (symbol == null) {
                    throw new RuntimeException("Undefined function: " + funcIExpr.getFunctionName().getName());
                }

                // Linking
                funcIExpr.setFunction(symbol.getFunction());
            }
        }

        ActionInvocationExpr[] actionIExprs = bFile.getActionIExprs();
        if (actionIExprs != null) {
            for (ActionInvocationExpr actionInvocationExpr : actionIExprs) {
                SymbolName symbolName = actionInvocationExpr.getActionName();

                Symbol symbol = symTable.lookup(symbolName);
                if (symbol == null) {
                    throw new RuntimeException("Undefined function: " + actionInvocationExpr.getActionName().getName());
                }

                // Linking
                actionInvocationExpr.setCalleeAction(symbol.getAction());
            }

        }
    }

}
