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

package io.ballerina.compiler.api.impl.types.util;

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;
import io.ballerina.compiler.api.types.util.LangLibMethod;

import java.util.Set;

/**
 * Represents a lang library function which can be called using a method call expr.
 *
 * @since 2.0.0
 */
public class BallerinaLangLibMethod implements LangLibMethod {

    private String name;
    private Set<Qualifier> qualifiers;
    private FunctionTypeDescriptor typeDescriptor;

    public BallerinaLangLibMethod(String name, Set<Qualifier> qualifiers, FunctionTypeDescriptor typeDescriptor) {
        this.name = name;
        this.qualifiers = qualifiers;
        this.typeDescriptor = typeDescriptor;
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

    // Setters. Only to be used by the type builder
    public void setFunctionType(FunctionTypeDescriptor typeDescriptor) {
        this.typeDescriptor = typeDescriptor;
    }
}
