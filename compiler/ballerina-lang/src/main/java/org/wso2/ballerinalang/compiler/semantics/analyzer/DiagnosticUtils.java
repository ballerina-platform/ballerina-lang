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

/**
 * Utility class containing diagnostic logging helper methods.
 *
 * @since 2201.10.0
 */
public class DiagnosticUtils {

    private DiagnosticUtils() {
    }

    static void logNonAccessibleSymbolError(BLangDiagnosticLog dlog, Location position, BSymbol symbol) {
        if (symbol.name.value.endsWith("." + Names.USER_DEFINED_INIT_SUFFIX.value)) {
            String objName = symbol.name.value.substring(0,
                    symbol.name.value.length() - Names.USER_DEFINED_INIT_SUFFIX.value.length() - 1);
            dlog.error(position, DiagnosticErrorCode.ATTEMPT_INITIALIZE_NON_ACCESSIBLE_OBJECT, objName);
        } else {
            dlog.error(position, DiagnosticErrorCode.ATTEMPT_REFER_NON_ACCESSIBLE_SYMBOL, symbol.name);
        }
    }
}
