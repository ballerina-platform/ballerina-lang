/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerina.compiler.impl.symbol;

import org.ballerina.compiler.api.element.DocAttachment;
import org.ballerina.compiler.api.element.ModuleID;
import org.ballerina.compiler.api.symbol.BallerinaSymbolKind;
import org.ballerina.compiler.api.symbol.FunctionSymbol;
import org.ballerina.compiler.api.symbol.MethodSymbol;
import org.ballerina.compiler.api.symbol.Qualifier;
import org.ballerina.compiler.api.type.BallerinaTypeDescriptor;

import java.util.List;
import java.util.Optional;

/**
 * Represents a ballerina method.
 *
 * @since 2.0.0
 */
public class MethodSymbolImpl implements MethodSymbol {

    private final FunctionSymbol functionSymbol;

    public MethodSymbolImpl(FunctionSymbol functionSymbol) {
        this.functionSymbol = functionSymbol;
    }

    @Override
    public Optional<BallerinaTypeDescriptor> typeDescriptor() {
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
    public BallerinaSymbolKind kind() {
        return BallerinaSymbolKind.METHOD;
    }

    @Override
    public Optional<DocAttachment> docAttachment() {
        return this.functionSymbol.docAttachment();
    }

    public List<Qualifier> qualifiers() {
        return this.functionSymbol.qualifiers();
    }
}
