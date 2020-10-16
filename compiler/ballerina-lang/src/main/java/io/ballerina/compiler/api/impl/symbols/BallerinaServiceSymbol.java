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
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
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
    private List<MethodSymbol> resources;
    private List<MethodSymbol> methods;

    private BallerinaServiceSymbol(String name, List<MethodSymbol> resources, List<MethodSymbol> methods,
                                   PackageID moduleID, BServiceSymbol serviceSymbol) {
        super(name, moduleID, SymbolKind.SERVICE, serviceSymbol);
        this.serviceSymbol = serviceSymbol;
        this.resources = resources;
        this.methods = methods;
    }

    @Override
    public List<MethodSymbol> resources() {
        return this.resources;
    }

    @Override
    public List<MethodSymbol> methods() {
        return this.methods;
    }

    /**
     * Represents Ballerina Service Symbol Builder.
     *
     * @since 2.0.0
     */
    public static class ServiceSymbolBuilder extends SymbolBuilder<ServiceSymbolBuilder> {

        private final SymbolFactory symbolFactory;

        public ServiceSymbolBuilder(String name, PackageID moduleID, BServiceSymbol serviceSymbol,
                                    SymbolFactory symbolFactory) {
            super(name, moduleID, SymbolKind.SERVICE, serviceSymbol);
            this.symbolFactory = symbolFactory;
        }

        @Override
        public BallerinaServiceSymbol build() {
            List<MethodSymbol> resources = getResources((BServiceSymbol) this.bSymbol);
            List<MethodSymbol> methods = getMethods((BServiceSymbol) this.bSymbol);
            return new BallerinaServiceSymbol(this.name, resources, methods, this.moduleID,
                                              (BServiceSymbol) this.bSymbol);
        }

        private List<MethodSymbol> getMethods(BServiceSymbol serviceSymbol) {
            List<MethodSymbol> methods = new ArrayList<>();

            for (BAttachedFunction method : ((BObjectTypeSymbol) serviceSymbol.type.tsymbol).attachedFuncs) {
                if (!Symbols.isFlagOn(method.symbol.flags, Flags.RESOURCE)) {
                    String name = method.symbol.getName().getValue();
                    MethodSymbol methodSymbol = symbolFactory.createMethodSymbol(method.symbol, name);
                    methods.add(methodSymbol);
                }
            }

            return Collections.unmodifiableList(methods);
        }

        private List<MethodSymbol> getResources(BServiceSymbol serviceSymbol) {
            List<MethodSymbol> resources = new ArrayList<>();

            for (BAttachedFunction method : ((BObjectTypeSymbol) serviceSymbol.type.tsymbol).attachedFuncs) {
                if (Symbols.isFlagOn(method.symbol.flags, Flags.RESOURCE)) {
                    String name = method.symbol.getName().getValue();
                    MethodSymbol resourceSymbol = symbolFactory.createMethodSymbol(method.symbol, name);
                    resources.add(resourceSymbol);
                }
            }

            return Collections.unmodifiableList(resources);
        }
    }
}
