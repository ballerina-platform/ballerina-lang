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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Optional;

/**
 * Represents an array type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaMapTypeSymbol extends AbstractTypeSymbol implements MapTypeSymbol {

    private TypeSymbol memberTypeDesc;
    private String signature;

    public BallerinaMapTypeSymbol(CompilerContext context, ModuleID moduleID, BMapType mapType) {
        super(context, TypeDescKind.MAP, mapType);
    }

    @Override
    public Optional<TypeSymbol> typeParameter() {
        if (this.memberTypeDesc == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.memberTypeDesc = typesFactory.getTypeDescriptor(((BMapType) this.getBType()).constraint);
        }

        return Optional.ofNullable(this.memberTypeDesc);
    }

    @Override
    public TypeSymbol typeParam() {
        if (this.memberTypeDesc == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.memberTypeDesc = typesFactory.getTypeDescriptor(((BMapType) this.getBType()).constraint);
        }

        return this.memberTypeDesc;
    }

    @Override
    public String signature() {
        if (this.signature == null) {
            TypeSymbol memberTypeDescriptor = this.typeParam();
            this.signature = "map<" + memberTypeDescriptor.signature() + ">";
        }

        return this.signature;
    }
}
