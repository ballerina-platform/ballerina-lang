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
import io.ballerina.compiler.api.impl.LangLibrary;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.SingletonTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * Represents a singleton type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaSingletonTypeSymbol extends AbstractTypeSymbol implements SingletonTypeSymbol {

    private final String typeName;

    public BallerinaSingletonTypeSymbol(CompilerContext context, BLangLiteral shape, BType bType) {
        super(context, TypeDescKind.SINGLETON, bType);

        if (shape.value == null && shape.originalValue == null) {
            this.typeName = "";
        // Special case handling for `()` since in BLangLiteral, `null` is used to represent nil.
        } else if (shape.getBType().tag == TypeTags.NIL) {
            this.typeName = "()";
        // Special case handling for string type.
        } else if (shape.getBType().tag == TypeTags.STRING) {
            this.typeName = "\"" + shape + "\"";
        } else {
            this.typeName = shape.toString();
        }
    }

    @Override
    public List<FunctionSymbol> langLibMethods() {
        if (this.langLibFunctions == null) {
            LangLibrary langLibrary = LangLibrary.getInstance(this.context);
            BFiniteType internalType = (BFiniteType) this.getBType();
            BType valueType = internalType.getValueSpace().iterator().next().getBType();
            List<FunctionSymbol> functions = langLibrary.getMethods(valueType);
            this.langLibFunctions = filterLangLibMethods(functions, valueType);
        }

        return this.langLibFunctions;
    }

    @Override
    public String signature() {
        return this.typeName;
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
