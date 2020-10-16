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
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.Parameter;
import io.ballerina.compiler.api.types.ParameterKind;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a parameter with a name and type.
 *
 * @since 2.0.0
 */
public class BallerinaParameter implements Parameter {

    // add the metadata field
    private List<Qualifier> qualifiers;
    private String parameterName;
    private BallerinaTypeDescriptor typeDescriptor;
    private ParameterKind kind;

    public BallerinaParameter(String parameterName, BallerinaTypeDescriptor typeDescriptor, List<Qualifier> qualifiers,
                              ParameterKind kind) {
        // TODO: Add the metadata
        this.parameterName = parameterName;
        this.typeDescriptor = typeDescriptor;
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.kind = kind;
    }

    /**
     * Get the parameter name.
     *
     * @return {@link Optional} name of the field
     */
    @Override
    public Optional<String> name() {
        return Optional.ofNullable(parameterName);
    }

    /**
     * Get the type descriptor of the field.
     *
     * @return {@link BallerinaTypeDescriptor} of the field
     */
    @Override
    public BallerinaTypeDescriptor typeDescriptor() {
        return typeDescriptor;
    }

    /**
     * Get the access modifiers.
     *
     * @return {@link List} of access modifiers
     */
    @Override
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    /**
     * Get the signature of the field.
     *
     * @return {@link String} signature
     */
    @Override
    public String signature() {
        StringJoiner joiner = new StringJoiner(" ");
        this.qualifiers().forEach(accessModifier -> joiner.add(accessModifier.getValue()));
        joiner.add(this.typeDescriptor().signature());
        if (this.name().isPresent()) {
            joiner.add(this.name().get());
        }

        return joiner.toString();
    }

    @Override
    public ParameterKind kind() {
        return this.kind;
    }
}
