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

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.symbols.CompilationErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an error in compiling.
 *
 * @since 2.0.0
 */
public class BallerinaCompilationErrorTypeSymbol extends AbstractTypeSymbol implements CompilationErrorTypeSymbol {

    private static final List<FunctionSymbol> langLibMethods = Collections.unmodifiableList(new ArrayList<>());

    public BallerinaCompilationErrorTypeSymbol(CompilerContext context, BType error) {
        super(context, TypeDescKind.COMPILATION_ERROR, error);
    }

    @Override
    public String signature() {
        return "$CompilationError$";
    }

    @Override
    public List<FunctionSymbol> langLibMethods() {
        return langLibMethods;
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }
}
