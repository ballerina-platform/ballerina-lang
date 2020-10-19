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
import io.ballerina.compiler.api.types.FieldDescriptor;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.api.types.util.TypeDescKind;
import io.ballerina.compiler.api.types.MethodDescriptor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents an object type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaObjectTypeDescriptor extends AbstractTypeDescriptor implements ObjectTypeDescriptor {

    private List<TypeQualifier> typeQualifiers;
    // private TypeDescriptor objectTypeReference;
    private List<FieldDescriptor> objectFields;
    private List<MethodDescriptor> methods;

    public BallerinaObjectTypeDescriptor(ModuleID moduleID, BObjectType objectType) {
        super(TypeDescKind.OBJECT, moduleID, objectType);
        // TODO: Fix this
        // objectTypeReference = null;
    }

    public BallerinaObjectTypeDescriptor(ModuleID moduleID, List<FieldDescriptor> fields,
                                         List<MethodDescriptor> methods, BObjectType objectType) {
        super(TypeDescKind.OBJECT, moduleID, objectType);
        this.objectFields = fields;
        this.methods = methods;
    }

    @Override
    public List<TypeQualifier> typeQualifiers() {
        if (this.typeQualifiers != null) {
            return this.typeQualifiers;
        }

        this.typeQualifiers = new ArrayList<>();
        BObjectType objectType = (BObjectType) getBType();

        if ((objectType.tsymbol.flags & Flags.CLIENT) == Flags.CLIENT) {
            this.typeQualifiers.add(TypeQualifier.CLIENT);
        }

        if ((objectType.flags & Flags.LISTENER) == Flags.LISTENER) {
            this.typeQualifiers.add(TypeQualifier.LISTENER);
        }

        return this.typeQualifiers;
    }

    @Override
    public List<FieldDescriptor> fieldDescriptors() {
        return objectFields;
    }

    /**
     * Get the list of methods.
     *
     * @return {@link List} of object methods
     */
    public List<MethodDescriptor> methods() {
        return this.methods;
    }

    @Override
    public String signature() {
        StringBuilder signature = new StringBuilder();
        StringJoiner qualifierJoiner = new StringJoiner(" ");
        StringJoiner fieldJoiner = new StringJoiner(";");
        StringJoiner methodJoiner = new StringJoiner(" ");

        for (TypeQualifier typeQualifier : this.typeQualifiers()) {
            String value = typeQualifier.getValue();
            qualifierJoiner.add(value);
        }
        signature.append(qualifierJoiner.toString()).append(" object {");

        // this.getObjectTypeReference()
        //         .ifPresent(typeDescriptor -> fieldJoiner.add("*" + typeDescriptor.getSignature()));
        this.fieldDescriptors().forEach(objectFieldDescriptor -> fieldJoiner.add(objectFieldDescriptor.signature()));
        this.methods().forEach(method -> methodJoiner.add(method.typeDescriptor().signature()));

        return signature.append(fieldJoiner.toString())
                .append(methodJoiner.toString())
                .append("}")
                .toString();
    }

    // Setters. Only to be used by the type builder.
    void setFieldDescriptors(List<FieldDescriptor> fields) {
        this.objectFields = fields;
    }

    void setMethods(List<MethodDescriptor> methods) {
        this.methods = methods;
    }
}
