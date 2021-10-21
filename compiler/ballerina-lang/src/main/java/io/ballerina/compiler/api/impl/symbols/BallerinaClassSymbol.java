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
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a Class Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaClassSymbol extends BallerinaSymbol implements ClassSymbol {

    private final ObjectTypeSymbol typeDescriptor;
    private final List<Qualifier> qualifiers;
    private final List<AnnotationSymbol> annots;
    private final boolean deprecated;
    private final BClassSymbol internalSymbol;
    private final CompilerContext context;
    private final Documentation docAttachment;
    private MethodSymbol initMethod;
    private Map<String, ClassFieldSymbol> classFields;

    protected BallerinaClassSymbol(CompilerContext context, String name, List<Qualifier> qualifiers,
                                   List<AnnotationSymbol> annots, ObjectTypeSymbol typeDescriptor,
                                   BClassSymbol classSymbol) {
        super(name, SymbolKind.CLASS, classSymbol, context);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.annots = Collections.unmodifiableList(annots);
        this.docAttachment = getDocAttachment(classSymbol);
        this.typeDescriptor = typeDescriptor;
        this.deprecated = Symbols.isFlagOn(classSymbol.flags, Flags.DEPRECATED);
        this.internalSymbol = classSymbol;
        this.context = context;
    }

    @Override
    public Map<String, ClassFieldSymbol> fieldDescriptors() {
        if (this.classFields != null) {
            return classFields;
        }

        Map<String, ClassFieldSymbol> fields = new LinkedHashMap<>();
        BObjectType type = (BObjectType) this.getBType();

        for (BField field : type.fields.values()) {
            fields.put(field.name.value, new BallerinaClassFieldSymbol(this.context, field));
        }

        this.classFields = Collections.unmodifiableMap(fields);
        return classFields;
    }

    @Override
    public Map<String, MethodSymbol> methods() {
        return this.typeDescriptor.methods();
    }

    @Override
    public Optional<MethodSymbol> initMethod() {
        if (this.initMethod == null && this.internalSymbol.initializerFunc != null) {
            SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
            this.initMethod = symbolFactory.createMethodSymbol(internalSymbol.initializerFunc.symbol,
                                                               internalSymbol.initializerFunc.funcName.value);
        }

        return Optional.ofNullable(this.initMethod);
    }

    @Override
    public List<TypeSymbol> typeInclusions() {
        return this.typeDescriptor.typeInclusions();
    }

    @Override
    public List<AnnotationSymbol> annotations() {
        return this.annots;
    }

    @Override
    public Optional<Documentation> documentation() {
        return Optional.ofNullable(this.docAttachment);
    }

    @Override
    public TypeDescKind typeKind() {
        return TypeDescKind.OBJECT;
    }

    @Override
    public String signature() {
        if (this.getName().get().startsWith("$anonType$")) {
            return typeDescriptor.signature();
        }
        return this.getName().get();
    }

    @Override
    public List<FunctionSymbol> langLibMethods() {
        return this.typeDescriptor.langLibMethods();
    }

    @Override
    public boolean assignableTo(TypeSymbol targetType) {
        Types types = Types.getInstance(this.context);
        return types.isAssignable(this.internalSymbol.type, getTargetBType(targetType));
    }

    @Override
    public boolean subtypeOf(TypeSymbol targetType) {
        Types types = Types.getInstance(this.context);
        return types.isAssignable(this.internalSymbol.type, getTargetBType(targetType));
    }

    @Override
    public boolean deprecated() {
        return this.deprecated;
    }

    @Override
    public List<Qualifier> qualifiers() {
        return this.qualifiers;
    }

    BType getBType() {
        return this.internalSymbol.type;
    }

    private BType getTargetBType(TypeSymbol typeSymbol) {
        if (typeSymbol.kind() == SymbolKind.TYPE) {
            return ((AbstractTypeSymbol) typeSymbol).getBType();
        }

        return ((BallerinaClassSymbol) typeSymbol).getBType();
    }

    /**
     * A builder for class symbols.
     *
     * @since 2.0.0
     */
    public static class ClassSymbolBuilder extends SymbolBuilder<BallerinaClassSymbol.ClassSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();
        protected List<AnnotationSymbol> annots = new ArrayList<>();
        protected ObjectTypeSymbol typeDescriptor;

        public ClassSymbolBuilder(CompilerContext context, String name, BSymbol symbol) {
            super(name, SymbolKind.CLASS, symbol, context);
        }

        public BallerinaClassSymbol.ClassSymbolBuilder withTypeDescriptor(ObjectTypeSymbol typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public BallerinaClassSymbol.ClassSymbolBuilder withQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        public ClassSymbolBuilder withAnnotation(AnnotationSymbol annot) {
            this.annots.add(annot);
            return this;
        }

        @Override
        public BallerinaClassSymbol build() {
            return new BallerinaClassSymbol(this.context, this.name, this.qualifiers, this.annots,
                                            this.typeDescriptor, (BClassSymbol) this.bSymbol);
        }
    }
}
