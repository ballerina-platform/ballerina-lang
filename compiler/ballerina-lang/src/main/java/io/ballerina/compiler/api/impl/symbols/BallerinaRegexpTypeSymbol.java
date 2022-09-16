/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.api.symbols.RegexpTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRegexpType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Optional;

/**
 * Represents an RegExp type descriptor.
 *
 * @since 2.3.0
 */
public class BallerinaRegexpTypeSymbol extends AbstractTypeSymbol implements RegexpTypeSymbol {

    private TypeSymbol typeParameter;
    private String typeName;
    private String signature;

    public BallerinaRegexpTypeSymbol(CompilerContext context, BRegexpType regexpType) {
        super(context, TypeDescKind.REGEXP, regexpType);
    }

    @Override
    public Optional<TypeSymbol> typeParameter() {
        return Optional.ofNullable(this.typeParameter);
    }

    @Override
    public Optional<String> getName() {
        return Optional.of(this.typeName);
    }

    @Override
    public String signature() {
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
}
