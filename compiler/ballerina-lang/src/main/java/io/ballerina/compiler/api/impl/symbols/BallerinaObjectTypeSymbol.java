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
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.FieldSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents an object type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaObjectTypeSymbol extends AbstractTypeSymbol implements ObjectTypeSymbol {

    private List<Qualifier> qualifiers;
    private List<FieldSymbol> objectFields;
    private List<MethodSymbol> methods;
    private List<TypeSymbol> typeInclusions;

    public BallerinaObjectTypeSymbol(CompilerContext context, ModuleID moduleID, BObjectType objectType) {
        super(context, TypeDescKind.OBJECT, moduleID, objectType);
    }

    @Override
    public List<Qualifier> qualifiers() {
        if (this.qualifiers != null) {
            return this.qualifiers;
        }

        List<Qualifier> qualifiers = new ArrayList<>();
        BObjectType objectType = (BObjectType) getBType();
        final long mask = objectType.tsymbol.flags;

        // distinct has to come first because we are modeling the distinct-type-descriptor here.
        addIfFlagSet(qualifiers, mask, Flags.DISTINCT, Qualifier.DISTINCT);
        addIfFlagSet(qualifiers, mask, Flags.ISOLATED, Qualifier.ISOLATED);
        addIfFlagSet(qualifiers, mask, Flags.CLIENT, Qualifier.CLIENT);
        addIfFlagSet(qualifiers, mask, Flags.SERVICE, Qualifier.SERVICE);

        this.qualifiers = Collections.unmodifiableList(qualifiers);
        return this.qualifiers;
    }

    @Override
    public List<FieldSymbol> fieldDescriptors() {
        if (this.objectFields == null) {
            this.objectFields = new ArrayList<>();
            for (BField field : ((BObjectType) this.getBType()).fields.values()) {
                this.objectFields.add(new BallerinaFieldSymbol(this.context, field));
            }
        }
        return objectFields;
    }

    /**
     * Get the list of methods.
     *
     * @return {@link List} of object methods
     */
    public List<MethodSymbol> methods() {
        if (this.methods == null) {
            SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
            List<MethodSymbol> methods = new ArrayList<>();

            for (BAttachedFunction attachedFunc : ((BObjectTypeSymbol) this.getBType().tsymbol).attachedFuncs) {
                methods.add(symbolFactory.createMethodSymbol(attachedFunc.symbol, attachedFunc.funcName.getValue()));
            }

            this.methods = Collections.unmodifiableList(methods);
        }

        return this.methods;
    }

    @Override
    public List<TypeSymbol> typeInclusions() {
        if (this.typeInclusions == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            List<BType> inclusions = ((BObjectType) this.getBType()).typeInclusions;

            List<TypeSymbol> typeRefs = new ArrayList<>();
            for (BType inclusion : inclusions) {
                TypeSymbol type = typesFactory.getTypeDescriptor(inclusion);

                // If the inclusion was not a type ref, the type would be semantic error and the type factory will
                // return null. Therefore, skipping them.
                if (type != null) {
                    typeRefs.add(type);
                }
            }

            this.typeInclusions = Collections.unmodifiableList(typeRefs);
        }

        return this.typeInclusions;
    }

    @Override
    public String signature() {
        StringBuilder signature = new StringBuilder();
        StringJoiner qualifierJoiner = new StringJoiner(" ");
        StringJoiner fieldJoiner = new StringJoiner(";");
        StringJoiner methodJoiner = new StringJoiner(" ");

        for (Qualifier typeQualifier : this.qualifiers()) {
            String value = typeQualifier.getValue();
            qualifierJoiner.add(value);
        }
        qualifierJoiner.add("object {");
        signature.append(qualifierJoiner.toString());

        // this.getObjectTypeReference()
        //         .ifPresent(typeDescriptor -> fieldJoiner.add("*" + typeDescriptor.getSignature()));
        this.fieldDescriptors().forEach(objectFieldDescriptor -> fieldJoiner.add(objectFieldDescriptor.signature()));
        this.methods().forEach(method -> methodJoiner.add(method.signature()).add(";"));

        return signature.append(fieldJoiner.toString())
                .append(methodJoiner.toString())
                .append("}")
                .toString();
    }

    private void addIfFlagSet(List<Qualifier> quals, final long mask, final long flag, Qualifier qualifier) {
        if (Symbols.isFlagOn(mask, flag)) {
            quals.add(qualifier);
        }
    }
}
