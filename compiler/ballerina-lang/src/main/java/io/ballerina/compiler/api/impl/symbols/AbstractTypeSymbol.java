/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.impl.LangLibrary;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.runtime.api.utils.IdentifierUtils;
import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a Ballerina Type Descriptor.
 *
 * @since 2.0.0
 */
public abstract class AbstractTypeSymbol implements TypeSymbol {

    protected final CompilerContext context;
    protected List<FunctionSymbol> langLibFunctions;

    private final TypeDescKind typeDescKind;
    private final BType bType;

    public AbstractTypeSymbol(CompilerContext context, TypeDescKind typeDescKind, BType bType) {
        this.context = context;
        this.typeDescKind = typeDescKind;
        this.bType = bType;
    }

    @Override
    public TypeDescKind typeKind() {
        return typeDescKind;
    }

    @Override
    public Optional<String> getName() {
        return Optional.empty();
    }

    @Override
    public Optional<ModuleSymbol> getModule() {
        return Optional.empty();
    }

    @Override
    public abstract String signature();

    @Override
    public SymbolKind kind() {
        return SymbolKind.TYPE;
    }

    @Override
    public Location location() {
        return null;
    }

    @Override
    public Optional<Location> getLocation() {
        return Optional.empty();
    }

    @Override
    public List<FunctionSymbol> langLibMethods() {
        if (this.langLibFunctions == null) {
            LangLibrary langLibrary = LangLibrary.getInstance(this.context);
            List<FunctionSymbol> functions = langLibrary.getMethods(this.getBType());
            this.langLibFunctions = filterLangLibMethods(functions, this.getBType());
        }

        return this.langLibFunctions;
    }

    @Override
    public boolean assignableTo(TypeSymbol targetType) {
        Types types = Types.getInstance(this.context);
        return types.isAssignable(this.bType, getTargetBType(targetType));
    }

    @Override
    public boolean subtypeOf(TypeSymbol targetType) {
        Types types = Types.getInstance(this.context);
        return types.isAssignable(this.bType, getTargetBType(targetType));
    }

    @Override
    public boolean nameEquals(String name) {
        Optional<String> symbolName = this.getName();
        if (symbolName.isEmpty() || name == null) {
            return false;
        }
        String symName = symbolName.get();
        if (name.equals(symName)) {
            return true;
        }
        return unescapedUnicode(name).equals(unescapedUnicode(symName));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeSymbol)) {
            return false;
        }

        Types types = Types.getInstance(this.context);
        return types.isSameType(this.bType, ((AbstractTypeSymbol) obj).getBType());
    }

    @Override
    public int hashCode() {
        return this.bType.hashCode();
    }

    /**
     * Get the BType.
     *
     * @return {@link BType} associated with the type desc
     */
    public BType getBType() {
        return bType;
    }

    protected List<FunctionSymbol> filterLangLibMethods(List<FunctionSymbol> functions, BType internalType) {
        Types types = Types.getInstance(this.context);
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

    // Private util methods

    private BType getTargetBType(TypeSymbol typeSymbol) {
        if (typeSymbol.kind() == SymbolKind.TYPE) {
            return ((AbstractTypeSymbol) typeSymbol).getBType();
        }

        return ((BallerinaClassSymbol) typeSymbol).getBType();
    }

    private String unescapedUnicode(String value) {
        if (value.startsWith("'")) {
            return Utils.unescapeUnicodeCodepoints(value.substring(1));
        }
        return Utils.unescapeUnicodeCodepoints(value);
    }
}
