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
import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;
import java.util.Optional;

/**
 * Represents a TypeReference type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaTypeReferenceTypeSymbol extends AbstractTypeSymbol implements TypeReferenceTypeSymbol {

    private static final String ANON_ORG = "$anon";
    private final String definitionName;
    private TypeSymbol typeDescriptorImpl;
    private Location location;
    private String signature;
    private ModuleSymbol module;
    private Symbol definition;
    private boolean moduleEvaluated;
    private boolean fromIntersectionType;
    public BSymbol tSymbol;
    public BType referredType;


    public BallerinaTypeReferenceTypeSymbol(CompilerContext context, BType bType, BSymbol tSymbol,
                                            boolean fromIntersectionType) {
        super(context, TypeDescKind.TYPE_REFERENCE, bType);
        referredType = getReferredType(bType);
        this.definitionName = tSymbol.getOriginalName().getValue();
        this.tSymbol = tSymbol;
        this.fromIntersectionType = fromIntersectionType;
        this.location = tSymbol.pos;
    }

    @Override
    public TypeSymbol typeDescriptor() {
        if (this.typeDescriptorImpl == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.typeDescriptorImpl = typesFactory.getTypeDescriptor(
                    referredType, referredType.tsymbol, true, !fromIntersectionType, false);
        }

        return this.typeDescriptorImpl;
    }

    @Override
    public List<FunctionSymbol> langLibMethods() {
        return this.typeDescriptor().langLibMethods();
    }

    @Override
    public String name() {
        return this.definitionName;
    }

    @Override
    public Symbol definition() {
        if (this.definition != null) {
            return this.definition;
        }

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);

        if (this.getReferredType(this.getBType()).tag == TypeTags.PARAMETERIZED_TYPE) {
            this.definition = symbolFactory.getBCompiledSymbol(((BParameterizedType) this.tSymbol.type).paramSymbol,
                                                               this.name());
        } else {
            Scope.ScopeEntry scopeEntry = tSymbol.owner.scope.lookup(Names.fromString(this.name()));
            this.definition = symbolFactory.getBCompiledSymbol(scopeEntry.symbol, this.name());
        }

        return this.definition;
    }

    @Override
    public Optional<String> getName() {
        return Optional.of(this.definitionName);
    }

    @Override
    public Optional<ModuleSymbol> getModule() {
        if (this.module != null || this.moduleEvaluated) {
            return Optional.ofNullable(this.module);
        }

        this.moduleEvaluated = true;
        Symbol definition = this.definition();

        if (definition.getModule().isEmpty()) {
            return Optional.empty();
        }

        this.module = definition.getModule().get();
        return Optional.of(this.module);
    }

    @Override
    public Location location() {
        if (location == null) {
            BType type = this.getBType();

            if (type.tsymbol != null) {
                this.location = type.tsymbol.pos;
            }
        }

        return this.location;
    }

    @Override
    public Optional<Location> getLocation() {

        return Optional.of(this.location);
    }

    @Override
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        ModuleID moduleID = this.getModule().get().id();
        if (moduleID == null ||
                (moduleID.moduleName().equals("lang.annotations") && moduleID.orgName().equals("ballerina")) ||
                this.getBType().tag == TypeTags.PARAMETERIZED_TYPE) {
            this.signature = this.definitionName;
        } else if (moduleID.moduleName().equals("lang.xml") && moduleID.orgName().equals("ballerina")) {
            this.signature = "xml:" + this.definitionName;
        } else {
            this.signature = !this.isAnonOrg(moduleID) ? moduleID.orgName() + Names.ORG_NAME_SEPARATOR +
                    moduleID.moduleName() + Names.VERSION_SEPARATOR + moduleID.version() + ":" +
                    this.definitionName
                    : this.definitionName;
        }

        return this.signature;
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }

    private boolean isAnonOrg(ModuleID moduleID) {
        return ANON_ORG.equals(moduleID.orgName());
    }

    private BType getReferredType(BType type) {
        if (type.tag == TypeTags.TYPEREFDESC) {
            return ((BTypeReferenceType) type).referredType;
        }

        return type;
    }
}
