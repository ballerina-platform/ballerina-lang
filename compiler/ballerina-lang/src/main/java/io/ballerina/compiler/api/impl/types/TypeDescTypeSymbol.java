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
package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.TypesFactory;
import io.ballerina.compiler.api.types.TypeSymbol;
import io.ballerina.compiler.api.types.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;

import java.util.Optional;

/**
 * Represents a typeDesc type descriptor.
 *
 * @since 2.0.0
 */
public class TypeDescTypeSymbol extends AbstractTypeSymbol
        implements io.ballerina.compiler.api.types.TypeDescTypeSymbol {

    private TypeSymbol typeParameter;

    public TypeDescTypeSymbol(ModuleID moduleID, BTypedescType typedescType) {
        super(TypeDescKind.TYPEDESC, moduleID, typedescType);
    }

    @Override
    public Optional<TypeSymbol> typeParameter() {
        if (this.typeParameter == null) {
            this.typeParameter = TypesFactory.getTypeDescriptor(((BTypedescType) this.getBType()).constraint);
        }

        return Optional.ofNullable(this.typeParameter);
    }

    @Override
    public String signature() {
        if (this.typeParameter().isPresent()) {
            return this.kind().name() + "<" + this.typeParameter().get().signature() + ">";
        }
        return this.kind().name();
    }
}
