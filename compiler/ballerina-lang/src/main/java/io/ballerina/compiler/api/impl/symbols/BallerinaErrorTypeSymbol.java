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
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.Optional;

/**
 * Represents an error type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaErrorTypeSymbol extends AbstractTypeSymbol implements ErrorTypeSymbol {

    private static final String ANON_ORG = "$anon";
    private TypeSymbol detail;
    private String signature;
    private ModuleSymbol module;
    private boolean moduleEvaluated;

    public BallerinaErrorTypeSymbol(CompilerContext context, ModuleID moduleID, BErrorType errorType) {
        super(context, TypeDescKind.ERROR, errorType);
    }

    /**
     * Get the detail type descriptor.
     *
     * @return {@link TypeSymbol} detail
     */
    @Override
    public TypeSymbol detailTypeDescriptor() {
        if (this.detail == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.detail = typesFactory.getTypeDescriptor(((BErrorType) this.getBType()).getDetailType());
        }
        return this.detail;
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
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        String definitionName = "error";
        if (!isDefaultDetailTypeDesc()) {
            definitionName += "<" + detailTypeDescriptor().signature() + ">";
        }

        if (this.getModule().isEmpty()) {
            this.signature = definitionName;
            return this.signature;
        }

        ModuleID moduleID = this.getModule().get().id();
        if ("lang.annotations".equals(moduleID.moduleName()) && "ballerina".equals(moduleID.orgName())) {
            this.signature = definitionName;
        } else {
            this.signature = !this.isAnonOrg(moduleID) ?
                    moduleID.orgName() + Names.ORG_NAME_SEPARATOR + moduleID.moduleName() +
                    Names.VERSION_SEPARATOR + moduleID.version() + ":" + definitionName : definitionName;
        }

        return this.signature;
    }

    private boolean isDefaultDetailTypeDesc() {

        if (detailTypeDescriptor().typeKind() == TypeDescKind.MAP) {

            BallerinaMapTypeSymbol mapTypeSymbol = (BallerinaMapTypeSymbol) detailTypeDescriptor();
            Optional<String> name = mapTypeSymbol.typeParam().getName();

            if (name.isPresent()) {

                if (name.get().equals("Cloneable")) {
                    if (mapTypeSymbol.typeParam().getModule().isPresent()) {

                        ModuleID moduleID = mapTypeSymbol.typeParam().getModule().get().id();

                        return moduleID.orgName().equals("ballerina") && moduleID.packageName().equals("lang.value");
                    }
                }

                return name.get().equals("__Cloneable");
            }
        }

        return false;
    }

    private boolean isAnonOrg(ModuleID moduleID) {
        return ANON_ORG.equals(moduleID.orgName());
    }
}
