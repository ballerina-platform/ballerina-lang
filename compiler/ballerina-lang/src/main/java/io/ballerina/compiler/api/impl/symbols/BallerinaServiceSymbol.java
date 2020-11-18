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
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ServiceSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
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
public class BallerinaServiceSymbol extends BallerinaSymbol implements ServiceSymbol {

    private final BServiceSymbol serviceSymbol;
    private List<FunctionSymbol> resources;
    private List<FunctionSymbol> functions;

    private BallerinaServiceSymbol(String name, PackageID moduleID, BServiceSymbol serviceSymbol) {
        super(name, moduleID, SymbolKind.SERVICE, serviceSymbol);
        this.serviceSymbol = serviceSymbol;
    }

    /**
     * Get the Service's resources.
     *
     * @return {@link List} of resource functions
     */
    @Override
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
    @Override
    public List<FunctionSymbol> functions() {
        if (functions == null) {
            this.functions = this.getFunctions(this.serviceSymbol);
        }

        return this.functions;
    }

    private List<FunctionSymbol> getResources(BServiceSymbol serviceSymbol) {
        List<BallerinaFunctionSymbol> resources = new ArrayList<>();
        if (serviceSymbol.type.tsymbol instanceof BObjectTypeSymbol) {
            for (BAttachedFunction function : ((BObjectTypeSymbol) serviceSymbol.type.tsymbol).attachedFuncs) {
                if ((function.symbol.flags & Flags.RESOURCE) == Flags.RESOURCE) {
                    String name = function.symbol.getName().getValue();
                    BallerinaFunctionSymbol functionSymbol = SymbolFactory.createFunctionSymbol(function.symbol, name);
                    resources.add(functionSymbol);
                }
            }
        }

        return Collections.unmodifiableList(resources);
    }

    private List<FunctionSymbol> getFunctions(BServiceSymbol serviceSymbol) {
        List<BallerinaFunctionSymbol> functions = new ArrayList<>();
        if (serviceSymbol.type.tsymbol instanceof BObjectTypeSymbol) {
            for (BAttachedFunction function : ((BObjectTypeSymbol) serviceSymbol.type.tsymbol).attachedFuncs) {
                if ((function.symbol.flags & Flags.RESOURCE) != Flags.RESOURCE) {
                    String name = function.symbol.getName().getValue();
                    BallerinaFunctionSymbol functionSymbol = SymbolFactory.createFunctionSymbol(function.symbol, name);
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
            super(name, moduleID, SymbolKind.SERVICE, serviceSymbol);
        }

        @Override
        public BallerinaServiceSymbol build() {
            return new BallerinaServiceSymbol(this.name, this.moduleID, (BServiceSymbol) this.bSymbol);
        }
    }
}
