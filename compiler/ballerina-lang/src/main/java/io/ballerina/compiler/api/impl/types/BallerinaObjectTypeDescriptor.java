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
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.types.FieldDescriptor;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private List<MethodSymbol> methods;
    private MethodSymbol initFunction;

    public BallerinaObjectTypeDescriptor(ModuleID moduleID, BObjectType objectType) {
        super(TypeDescKind.OBJECT, moduleID, objectType);
        // TODO: Fix this
        // objectTypeReference = null;
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

        // TODO: Check whether we can identify the listeners as well

        return this.typeQualifiers;
    }

    @Override
    public List<FieldDescriptor> fieldDescriptors() {
        if (this.objectFields == null) {
            this.objectFields = new ArrayList<>();
            for (BField field : ((BObjectType) this.getBType()).fields.values()) {
                this.objectFields.add(new BallerinaFieldDescriptor(field));
            }
        }
        return objectFields;
    }

    /**
     * Get the list of methods.
     *
     * @return {@link List} of object methods
     */
    // TODO: Rename to method declarations
    public List<MethodSymbol> methods() {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
            for (BAttachedFunction attachedFunc : ((BObjectTypeSymbol) ((BObjectType) this
                    .getBType()).tsymbol).attachedFuncs) {
                this.methods
                        .add(SymbolFactory.createMethodSymbol(attachedFunc.symbol, attachedFunc.funcName.getValue()));
            }
        }

        return this.methods;
    }

    @Override
    public Optional<MethodSymbol> initMethod() {
        if (this.initFunction == null) {
            BAttachedFunction initFunction =
                    ((BObjectTypeSymbol) ((BObjectType) this.getBType()).tsymbol).initializerFunc;
            this.initFunction = initFunction == null ? null
                    : SymbolFactory.createMethodSymbol(initFunction.symbol, initFunction.funcName.getValue());
        }

        return Optional.ofNullable(this.initFunction);
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
}
