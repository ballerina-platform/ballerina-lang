/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.Names;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;

/**
 * Utility class containing diagnostic logging helper methods.
 *
 * @since 2201.10.0
 */
public class DiagnosticUtils {

    private DiagnosticUtils() {
    }

    static void logNonAccessibleSymbolError(BLangDiagnosticLog dlog, Location position, BSymbol symbol) {
        if (isObjectInitializer(symbol)) {
            String objName = getObjectSymbolName(symbol);
            dlog.error(position, DiagnosticErrorCode.ATTEMPT_INITIALIZE_NON_ACCESSIBLE_OBJECT, objName);
        } else {
            dlog.error(position, DiagnosticErrorCode.ATTEMPT_REFER_NON_ACCESSIBLE_SYMBOL, symbol != null ? symbol.name : "");
        }
    }

    private static boolean isObjectInitializer(BSymbol symbol) {
        if (symbol == null) {
            return false;
        }
        if (symbol.owner instanceof BObjectTypeSymbol objectTypeSymbol) {
            if (objectTypeSymbol.initializerFunc != null && objectTypeSymbol.initializerFunc.symbol == symbol) {
                return true;
            }
        }
        return symbol.name != null && symbol.name.value != null &&
                symbol.name.value.endsWith("." + Names.USER_DEFINED_INIT_SUFFIX.value);
    }

    private static String getObjectSymbolName(BSymbol symbol) {
        if (symbol.owner instanceof BObjectTypeSymbol && symbol.owner.name != null) {
            return symbol.owner.name.value;
        }
        if (symbol.name != null && symbol.name.value != null &&
                symbol.name.value.endsWith("." + Names.USER_DEFINED_INIT_SUFFIX.value)) {
            return symbol.name.value.substring(0,
                    symbol.name.value.length() - Names.USER_DEFINED_INIT_SUFFIX.value.length() - 1);
        }
        return symbol.name != null ? symbol.name.value : "";
    }
}
