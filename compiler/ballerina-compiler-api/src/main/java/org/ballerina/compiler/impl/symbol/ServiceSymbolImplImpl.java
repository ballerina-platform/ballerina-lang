/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerina.compiler.impl.symbol;

import org.ballerina.compiler.api.symbol.BallerinaSymbolKind;
import org.ballerina.compiler.api.symbol.FunctionSymbol;
import org.ballerina.compiler.api.symbol.ServiceSymbol;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represent Service Symbol implementation.
 *
 * @since 2.0.0
 */
public class ServiceSymbolImplImpl extends BallerinaSymbolImpl implements ServiceSymbol {

    private final BServiceSymbol serviceSymbol;

    private List<FunctionSymbol> resources;

    private List<FunctionSymbol> functions;

    private ServiceSymbolImplImpl(String name,
                                  PackageID moduleID,
                                  BServiceSymbol serviceSymbol) {
        super(name, moduleID, BallerinaSymbolKind.SERVICE, serviceSymbol);
        this.serviceSymbol = serviceSymbol;
    }

    /**
     * Get the Service's resources.
     *
     * @return {@link List} of resource functions
     */
    public List<FunctionSymbol> resources() {
        if (resources == null) {
            this.resources = this.getResources(this.serviceSymbol);
        }

        return this.resources;
    }

    /**
     * Get the Service's functions.
     *
     * @return {@link List} of resource functions
     */
    public List<FunctionSymbol> functions() {
        if (functions == null) {
            this.functions = this.getFunctions(this.serviceSymbol);
        }

        return this.functions;
    }

    private List<FunctionSymbol> getResources(BServiceSymbol serviceSymbol) {
        List<FunctionSymbolImplImpl> resources = new ArrayList<>();
        if (serviceSymbol.type.tsymbol instanceof BObjectTypeSymbol) {
            for (BAttachedFunction function : ((BObjectTypeSymbol) serviceSymbol.type.tsymbol).attachedFuncs) {
                if ((function.symbol.flags & Flags.RESOURCE) == Flags.RESOURCE) {
                    String name = function.symbol.getName().getValue();
                    FunctionSymbolImplImpl functionSymbol = SymbolFactory.createFunctionSymbol(function.symbol, name);
                    resources.add(functionSymbol);
                }
            }
        }

        return Collections.unmodifiableList(resources);
    }

    private List<FunctionSymbol> getFunctions(BServiceSymbol serviceSymbol) {
        List<FunctionSymbolImplImpl> functions = new ArrayList<>();
        if (serviceSymbol.type.tsymbol instanceof BObjectTypeSymbol) {
            for (BAttachedFunction function : ((BObjectTypeSymbol) serviceSymbol.type.tsymbol).attachedFuncs) {
                if ((function.symbol.flags & Flags.RESOURCE) != Flags.RESOURCE) {
                    String name = function.symbol.getName().getValue();
                    FunctionSymbolImplImpl functionSymbol = SymbolFactory.createFunctionSymbol(function.symbol, name);
                    functions.add(functionSymbol);
                }
            }
        }

        return Collections.unmodifiableList(functions);
    }

    /**
     * Represents Ballerina Service Symbol Builder.
     *
     * @since 2.0.0
     */
    public static class ServiceSymbolBuilder extends SymbolBuilder<ServiceSymbolBuilder> {

        public ServiceSymbolBuilder(String name, PackageID moduleID, BServiceSymbol serviceSymbol) {
            super(name, moduleID, BallerinaSymbolKind.SERVICE, serviceSymbol);
        }

        public ServiceSymbolImplImpl build() {
            return new ServiceSymbolImplImpl(this.name, this.moduleID, (BServiceSymbol) this.bSymbol);
        }
    }
}
