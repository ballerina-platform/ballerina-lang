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
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.tools.diagnostics.Location;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

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
    public FunctionTypeSymbol typeDescriptor() {
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
    public List<Qualifier> qualifiers() {
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
    public List<AnnotationSymbol> annotations() {
        return this.functionSymbol.annotations();
    }

    @Override
    public Location location() {
        return this.functionSymbol.location();
    }

    @Override
    public String signature() {
        StringJoiner qualifierJoiner = new StringJoiner(" ");
        this.functionSymbol.qualifiers().stream().map(Qualifier::getValue).forEach(qualifierJoiner::add);
        qualifierJoiner.add("function ");

        StringBuilder signature = new StringBuilder(qualifierJoiner.toString());
        StringJoiner joiner = new StringJoiner(", ");
        signature.append(this.functionSymbol.name()).append("(");
        for (ParameterSymbol requiredParam : this.typeDescriptor().parameters()) {
            String ballerinaParameterSignature = requiredParam.signature();
            joiner.add(ballerinaParameterSignature);
        }
        this.typeDescriptor().restParam().ifPresent(ballerinaParameter -> joiner.add(ballerinaParameter.signature()));
        signature.append(joiner.toString()).append(")");
        this.typeDescriptor().returnTypeDescriptor().ifPresent(typeDescriptor -> signature.append(" returns ")
                .append(typeDescriptor.signature()));
        return signature.toString();
    }
}
