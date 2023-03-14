/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.impl.symbols.resourcepath.BallerinaDotResourcePath;
import io.ballerina.compiler.api.impl.symbols.resourcepath.BallerinaPathRestParam;
import io.ballerina.compiler.api.impl.symbols.resourcepath.BallerinaPathSegmentList;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Represents an implementation of the resource method symbol.
 *
 * @since 2.0.0
 */
public class BallerinaResourceMethodSymbol extends BallerinaMethodSymbol implements ResourceMethodSymbol {

    private static final String DOT_RESOURCE_PATH = ".";
    private static final String PATH_PARAM = "^";
    private static final String PATH_REST_PARAM = "^^";

    private final CompilerContext context;
    private final BInvokableSymbol internalSymbol;

    private ResourcePath resourcePath;
    private String signature;

    public BallerinaResourceMethodSymbol(FunctionSymbol functionSymbol, BInvokableSymbol symbol,
                                         CompilerContext context) {
        super(functionSymbol, symbol, context);
        this.internalSymbol = symbol;
        this.context = context;
    }

    @Override
    public ResourcePath resourcePath() {
        if (this.resourcePath != null) {
            return this.resourcePath;
        }

        BObjectTypeSymbol classSymbol = (BObjectTypeSymbol) this.internalSymbol.owner;
        BResourceFunction resourceFn = getBResourceFunction(classSymbol.attachedFuncs, this.internalSymbol);
        List<Name> internalResPath = resourceFn.pathSegmentSymbols.stream().map(s -> s.name)
                                     .collect(Collectors.toList());

        if (internalResPath.isEmpty()) {
            throw new IllegalStateException("Resource path is empty in resource function: " + resourceFn.toString());
        }

        switch (internalResPath.get(0).value) {
            case DOT_RESOURCE_PATH:
                this.resourcePath = new BallerinaDotResourcePath();
                break;
            case PATH_REST_PARAM:
                this.resourcePath = new BallerinaPathRestParam(resourceFn.restPathParam, this.context);
                break;
            default:
                this.resourcePath = new BallerinaPathSegmentList(internalResPath, resourceFn.pathParams,
                                                                 resourceFn.restPathParam, this.context);
        }

        return this.resourcePath;
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.RESOURCE_METHOD;
    }

    @Override
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        StringJoiner qualifierJoiner = new StringJoiner(" ");
        this.qualifiers().stream().map(Qualifier::getValue).forEach(qualifierJoiner::add);
        qualifierJoiner.add("function ");

        StringBuilder signature = new StringBuilder(qualifierJoiner.toString());
        StringJoiner joiner = new StringJoiner(", ");

        signature.append(this.getName().get()).append(" ").append(this.resourcePath().signature()).append(" (");

        for (ParameterSymbol requiredParam : this.typeDescriptor().params().get()) {
            String ballerinaParameterSignature = requiredParam.signature();
            joiner.add(ballerinaParameterSignature);
        }

        this.typeDescriptor().restParam().ifPresent(ballerinaParameter -> joiner.add(ballerinaParameter.signature()));
        signature.append(joiner.toString()).append(")");
        this.typeDescriptor().returnTypeDescriptor().ifPresent(typeDescriptor -> signature.append(" returns ")
                .append(typeDescriptor.signature()));

        this.signature = signature.toString();
        return this.signature;
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }

    private BResourceFunction getBResourceFunction(List<BAttachedFunction> methods, BInvokableSymbol internalSymbol) {
        for (BAttachedFunction method : methods) {
            if (internalSymbol == method.symbol && method instanceof BResourceFunction) {
                return (BResourceFunction) method;
            }
        }

        throw new IllegalStateException(
                "Matching BResourceFunction not found for internal symbol: " + internalSymbol.name.value);
    }
}
