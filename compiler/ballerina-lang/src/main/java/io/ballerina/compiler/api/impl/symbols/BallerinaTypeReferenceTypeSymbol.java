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
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
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

    public BallerinaTypeReferenceTypeSymbol(CompilerContext context, ModuleID moduleID, BType bType,
                                            BSymbol tSymbol, boolean fromIntersectionType) {
        super(context, TypeDescKind.TYPE_REFERENCE, bType);
        this.definitionName = tSymbol != null ? tSymbol.getOriginalName().getValue() : null;
        this.tSymbol = tSymbol;
        this.fromIntersectionType = fromIntersectionType;
        this.location = tSymbol != null ? tSymbol.pos : null;
    }

    @Override
    public TypeSymbol typeDescriptor() {
        if (this.typeDescriptorImpl == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.typeDescriptorImpl = typesFactory.getTypeDescriptor(
                    this.getBType(), this.getBType().tsymbol, true, !fromIntersectionType, false);
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
        this.definition = symbolFactory.getBCompiledSymbol(tSymbol, this.name());
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
        BSymbol symbol = this.getBType().tsymbol.owner;
        while (symbol != null) {
            if (symbol instanceof BPackageSymbol) {
                break;
            }
            symbol = symbol.owner;
        }

        if (symbol == null) {
            return Optional.empty();
        }

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        this.module = symbolFactory.createModuleSymbol((BPackageSymbol) symbol, symbol.name.value);
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
        } else {
            this.signature = !this.isAnonOrg(moduleID) ? moduleID.orgName() + Names.ORG_NAME_SEPARATOR +
                    moduleID.moduleName() + Names.VERSION_SEPARATOR + moduleID.version() + ":" +
                    this.definitionName
                    : this.definitionName;
        }

        return this.signature;
    }

    private boolean isAnonOrg(ModuleID moduleID) {
        return ANON_ORG.equals(moduleID.orgName());
    }
}
