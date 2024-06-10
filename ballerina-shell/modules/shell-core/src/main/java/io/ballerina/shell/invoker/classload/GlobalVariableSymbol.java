/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.invoker.classload;

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.shell.utils.Identifier;

/**
 * Wraps symbol of a global variable.
 *
 * @since 2.0.0
 */
public class GlobalVariableSymbol {
    private final Identifier name;
    private final TypeSymbol typeSymbol;

    private GlobalVariableSymbol(String name, TypeSymbol typeSymbol) {
        this.name = new Identifier(name);
        this.typeSymbol = typeSymbol;
    }

    public static GlobalVariableSymbol fromSymbol(Symbol symbol) {
        if (symbol.getName().isEmpty()) {
            throw new UnsupportedOperationException("Cannot create a global symbol without name");
        }
        if (symbol instanceof VariableSymbol variableSymbol) {
            return new GlobalVariableSymbol(symbol.getName().get(), variableSymbol.typeDescriptor());
        } else if (symbol instanceof FunctionSymbol functionSymbol) {
            return new GlobalVariableSymbol(symbol.getName().get(), functionSymbol.typeDescriptor());
        }
        throw new UnsupportedOperationException("Symbol type not supported for creating global variable.");
    }

    public Identifier getName() {
        return name;
    }

    public TypeSymbol getTypeSymbol() {
        return typeSymbol;
    }
}
