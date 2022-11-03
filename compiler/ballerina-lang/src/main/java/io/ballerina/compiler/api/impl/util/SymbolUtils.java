/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl.util;

import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.identifier.Utils;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Common util methods related to symbols.
 */
public class SymbolUtils {

    public static String unescapeUnicode(String value) {
        if (value.startsWith("'")) {
            return Utils.unescapeUnicodeCodepoints(value.substring(1));
        }
        return Utils.unescapeUnicodeCodepoints(value);
    }

    public static List<FunctionSymbol> filterLangLibMethods(CompilerContext context, List<FunctionSymbol> functions,
                                                            BType internalType) {
        Types types = Types.getInstance(context);
        List<FunctionSymbol> filteredFunctions = new ArrayList<>();

        for (FunctionSymbol function : functions) {

            List<ParameterSymbol> functionParams = function.typeDescriptor().params().get();

            if (functionParams.isEmpty()) {
                // If the function-type-descriptor doesn't have params, then, check for the rest-param
                Optional<ParameterSymbol> restParamOptional = function.typeDescriptor().restParam();
                if (restParamOptional.isPresent()) {
                    BArrayType restArrayType =
                            (BArrayType) ((AbstractTypeSymbol) restParamOptional.get().typeDescriptor()).getBType();
                    if (types.isAssignable(internalType, restArrayType.eType)) {
                        filteredFunctions.add(function);
                    }
                }
                continue;
            }

            ParameterSymbol firstParam = functionParams.get(0);
            BType firstParamType = ((AbstractTypeSymbol) firstParam.typeDescriptor()).getBType();

            if (types.isAssignable(internalType, firstParamType)) {
                filteredFunctions.add(function);
            }
        }

        return filteredFunctions;
    }
}
