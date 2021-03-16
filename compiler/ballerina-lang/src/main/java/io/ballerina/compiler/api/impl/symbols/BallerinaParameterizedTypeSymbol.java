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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.LangLibrary;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ParameterizedTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.List;

/**
 * Represents a parameterized type.
 *
 * @since 2.0.0
 */
public class BallerinaParameterizedTypeSymbol extends AbstractTypeSymbol implements ParameterizedTypeSymbol {

    private TypeSymbol paramValueType;
    private String signature;

    public BallerinaParameterizedTypeSymbol(CompilerContext context, ModuleID moduleID,
                                            BParameterizedType parameterizedType) {
        super(context, TypeDescKind.PARAMETERIZED, parameterizedType);
    }

    @Override
    public TypeSymbol paramValueType() {
        if (this.paramValueType != null) {
            return this.paramValueType;
        }

        TypesFactory typesFactory = TypesFactory.getInstance(this.context);
        this.paramValueType = typesFactory.getTypeDescriptor(((BParameterizedType) this.getBType()).paramValueType);
        return this.paramValueType;
    }

    @Override
    public List<FunctionSymbol> langLibMethods() {
        if (this.langLibFunctions == null) {
            LangLibrary langLibrary = LangLibrary.getInstance(this.context);
            List<FunctionSymbol> functions = langLibrary.getMethods(this.paramValueType().typeKind());
            this.langLibFunctions = filterLangLibMethods(functions, this.getBType());
        }

        return this.langLibFunctions;
    }

    @Override
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        this.signature = ((BParameterizedType) this.getBType()).paramSymbol.name.value;
        return this.signature;
    }
}
