/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;
import java.util.Set;

/**
 * Represents a ballerina method.
 *
 * @since 2.0.0
 */
public class BallerinaMethodSymbol implements MethodSymbol {

    private final FunctionSymbol functionSymbol;

    public BallerinaMethodSymbol(FunctionSymbol functionSymbol) {
        this.functionSymbol = functionSymbol;
    }

    @Override
    public FunctionTypeDescriptor typeDescriptor() {
        return this.functionSymbol.typeDescriptor();
    }

    @Override
    public String name() {
        return this.functionSymbol.name();
    }

    @Override
    public ModuleID moduleID() {
        return this.functionSymbol.moduleID();
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.METHOD;
    }

    @Override
    public Optional<Documentation> docAttachment() {
        return this.functionSymbol.docAttachment();
    }

    @Override
    public Set<Qualifier> qualifiers() {
        return this.functionSymbol.qualifiers();
    }

    @Override
    public boolean external() {
        return this.functionSymbol.external();
    }

    @Override
    public boolean deprecated() {
        return this.functionSymbol.deprecated();
    }

    @Override
    public Location location() {
        return this.functionSymbol.location();
    }
}
