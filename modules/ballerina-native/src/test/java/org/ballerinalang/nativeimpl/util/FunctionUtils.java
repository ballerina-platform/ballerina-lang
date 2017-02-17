/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.nativeimpl.util;

import org.ballerinalang.bre.SymScope;
import org.ballerinalang.model.Symbol;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.util.LangModelUtils;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Utility functions for Function Invocations.
 *
 * @since 0.8.0
 */
public class FunctionUtils {

    private FunctionUtils() {
    }

    /**
     * Add Native Function instance to given SymScope.
     *
     * @param symScope SymScope instance.
     * @param function Function instance.
     */
    public static void addNativeFunction(SymScope symScope, AbstractNativeFunction function) {
        SymbolName symbolName = LangModelUtils.getSymNameWithParams(function.getPackagePath() + ":" +
                function.getClass().getAnnotation(BallerinaFunction.class).functionName(), function.getParameterDefs());
        Symbol symbol = new Symbol(function);
        symScope.insert(symbolName, symbol);
    }
}
