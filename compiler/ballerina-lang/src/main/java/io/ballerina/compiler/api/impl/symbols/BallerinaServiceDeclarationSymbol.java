/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.ServiceAttachPoint;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import static io.ballerina.compiler.api.symbols.SymbolKind.SERVICE_DECLARATION;

/**
 * Represents a service declaration.
 *
 * @since 2.0.0
 */
public class BallerinaServiceDeclarationSymbol extends BallerinaSymbol implements ServiceDeclarationSymbol {

    private final List<AnnotationSymbol> annots;
    private final List<Qualifier> qualifiers;
    private final TypeSymbol typeDescriptor;
    private final ServiceAttachPoint attachPoint;

    private List<TypeSymbol> listenerTypes;
    private Map<String, ClassFieldSymbol> fields;
    private Map<String, MethodSymbol> methods;
    private Documentation docAttachment;

    protected BallerinaServiceDeclarationSymbol(String name, TypeSymbol typeDescriptor, ServiceAttachPoint attachPoint,
                                                List<Qualifier> qualifiers, List<AnnotationSymbol> annots,
                                                BSymbol bSymbol, CompilerContext context) {
        super(name, SERVICE_DECLARATION, bSymbol, context);
        this.typeDescriptor = typeDescriptor;
        this.attachPoint = attachPoint;
        this.annots = Collections.unmodifiableList(annots);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
    }

    @Override
    public Optional<TypeSymbol> typeDescriptor() {
        return Optional.ofNullable(this.typeDescriptor);
    }

    @Override
    public Optional<ServiceAttachPoint> attachPoint() {
        return Optional.ofNullable(this.attachPoint);
    }

    @Override
    public List<TypeSymbol> listenerTypes() {
        if (this.listenerTypes != null) {
            return this.listenerTypes;
        }

        TypesFactory typesFactory = TypesFactory.getInstance(this.context);
        BServiceSymbol serviceSymbol = (BServiceSymbol) this.getInternalSymbol();
        List<TypeSymbol> listenerTypes = new ArrayList<>();

        for (BType listenerType : serviceSymbol.getListenerTypes()) {
            listenerTypes.add(typesFactory.getTypeDescriptor(listenerType));
        }

        this.listenerTypes = Collections.unmodifiableList(listenerTypes);
        return this.listenerTypes;
    }

    @Override
    public Map<String, ClassFieldSymbol> fieldDescriptors() {
        if (this.fields != null) {
            return this.fields;
        }

        BServiceSymbol symbol = (BServiceSymbol) getInternalSymbol();
        BObjectType classType = (BObjectType) symbol.getAssociatedClassSymbol().type;
        Map<String, ClassFieldSymbol> fields = new LinkedHashMap<>();

        for (BField field : classType.fields.values()) {
            fields.put(field.name.value, new BallerinaClassFieldSymbol(this.context, field));
        }

        this.fields = Collections.unmodifiableMap(fields);
        return this.fields;
    }

    @Override
    public Map<String, ? extends MethodSymbol> methods() {
        if (methods != null) {
            return this.methods;
        }

        BServiceSymbol symbol = (BServiceSymbol) getInternalSymbol();
        BClassSymbol classSymbol = symbol.getAssociatedClassSymbol();
        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        Map<String, MethodSymbol> methods = new LinkedHashMap<>();

        for (BAttachedFunction method : classSymbol.attachedFuncs) {
            if (method instanceof BResourceFunction) {
                BResourceFunction resFn = (BResourceFunction) method;
                StringJoiner stringJoiner = new StringJoiner("/");

                for (Name name : resFn.resourcePath) {
                    stringJoiner.add(name.value);
                }

                methods.put(resFn.accessor.value + " " + stringJoiner.toString(),
                            symbolFactory.createResourceMethodSymbol(method.symbol));
            } else {
                methods.put(method.funcName.value,
                            symbolFactory.createMethodSymbol(method.symbol, method.funcName.value));
            }
        }

        this.methods = Collections.unmodifiableMap(methods);
        return this.methods;
    }

    @Override
    public List<AnnotationSymbol> annotations() {
        return this.annots;
    }

    @Override
    public Optional<Documentation> documentation() {
        if (this.docAttachment != null) {
            return Optional.of(this.docAttachment);
        }

        this.docAttachment = getDocAttachment(((BServiceSymbol) getInternalSymbol()).getAssociatedClassSymbol());
        return Optional.ofNullable(this.docAttachment);
    }

    @Override
    public List<Qualifier> qualifiers() {
        return this.qualifiers;
    }

    /**
     * A service declaration symbol builder.
     *
     * @since 2.0.0
     */
    public static class ServiceDeclSymbolBuilder
            extends SymbolBuilder<BallerinaServiceDeclarationSymbol.ServiceDeclSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();
        protected List<AnnotationSymbol> annots = new ArrayList<>();
        protected TypeSymbol typeDescriptor;
        protected ServiceAttachPoint attachPoint;

        public ServiceDeclSymbolBuilder(BSymbol symbol, CompilerContext context) {
            super(null, SERVICE_DECLARATION, symbol, context);
        }

        public ServiceDeclSymbolBuilder withTypeDescriptor(TypeSymbol typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public ServiceDeclSymbolBuilder withQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        public ServiceDeclSymbolBuilder withAnnotation(AnnotationSymbol annot) {
            this.annots.add(annot);
            return this;
        }

        public ServiceDeclSymbolBuilder withAttachPoint(ServiceAttachPoint attachPoint) {
            this.attachPoint = attachPoint;
            return this;
        }

        @Override
        public BallerinaServiceDeclarationSymbol build() {
            return new BallerinaServiceDeclarationSymbol(this.name, this.typeDescriptor, this.attachPoint,
                                                         this.qualifiers, this.annots, this.bSymbol, this.context);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getInternalSymbol().getName().getValue(),
                this.getModule().orElse(null),
                this.getLocation().map(Location::lineRange).orElse(null));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ServiceDeclarationSymbol)) {
            return false;
        }

        BallerinaServiceDeclarationSymbol symbol = (BallerinaServiceDeclarationSymbol) obj;
        return isInternalSymbolNameEquals(symbol.getInternalSymbol().getName().getValue())
                && isSameModule(this.getModule(), symbol.getModule())
                && isSameLocation(this.getLocation(), symbol.getLocation());
    }

    private boolean isInternalSymbolNameEquals(String name) {
        String symbolName = this.getInternalSymbol().getName().getValue();
        if (name.equals(symbolName)) {
            return true;
        }
        return unescapedUnicode(name).equals(unescapedUnicode(symbolName));
    }
}
