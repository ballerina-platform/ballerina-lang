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

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;
import io.ballerina.compiler.api.types.MethodDescriptor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;

import java.util.Set;

/**
 * Represents an object method declaration.
 *
 * @since 2.0.0
 */
public class BallerinaMethodDescriptor implements MethodDescriptor {

    private final String name;
    private final Set<Qualifier> qualifiers;
    private FunctionTypeDescriptor typeDescriptor;
    private BInvokableSymbol internalSymbol;

    public BallerinaMethodDescriptor(String name, Set<Qualifier> qualifiers, FunctionTypeDescriptor typeDescriptor) {
        this.name = name;
        this.qualifiers = qualifiers;
        this.typeDescriptor = typeDescriptor;
    }

    public BallerinaMethodDescriptor(String name, Set<Qualifier> qualifiers, FunctionTypeDescriptor typeDescriptor,
                                     BInvokableSymbol symbol) {
        this.name = name;
        this.qualifiers = qualifiers;
        this.typeDescriptor = typeDescriptor;
        this.internalSymbol = symbol;
    }

    @Override
    public Set<Qualifier> qualifiers() {
        return this.qualifiers;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public FunctionTypeDescriptor typeDescriptor() {
        return this.typeDescriptor;
    }

    // Setters and getters. Only to be used in the type builder.

    void setTypeDescriptor(FunctionTypeDescriptor typeDescriptor) {
        if (this.typeDescriptor == null) {
            this.typeDescriptor = typeDescriptor;
        }
    }

    BInvokableSymbol getInternalSymbol() {
        return this.internalSymbol;
    }
}
