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
package org.ballerina.compiler.api.model;

import org.ballerina.compiler.api.semantic.SymbolFactory;
import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent FUnction Symbol.
 * 
 * @since 1.3.0
 */
public class BallerinaServiceSymbol extends BallerinaVariable {
    private BServiceSymbol serviceSymbol;
    
    private BallerinaServiceSymbol(String name,
                                   PackageID moduleID,
                                   List<AccessModifier> accessModifiers,
                                   TypeDescriptor typeDescriptor,
                                   BServiceSymbol serviceSymbol) {
        super(name, moduleID, BallerinaSymbolKind.SERVICE, accessModifiers, typeDescriptor, serviceSymbol);
        this.serviceSymbol = serviceSymbol;
    }

    /**
     * Get the Service's resources.
     * 
     * @return {@link List} of resource functions
     */
    public List<BallerinaFunctionSymbol> getResources() {
        List<BallerinaFunctionSymbol> resources = new ArrayList<>();
        if (!(this.serviceSymbol.type.tsymbol instanceof BObjectTypeSymbol)) {
            // Do we need to throw an assertion error instead
            return resources;
        }
        for (BAttachedFunction function : ((BObjectTypeSymbol) this.serviceSymbol.type.tsymbol).attachedFuncs) {
            if ((function.symbol.flags & Flags.REMOTE) == Flags.REMOTE) {
                String name = function.symbol.getName().getValue();
                BallerinaFunctionSymbol functionSymbol = SymbolFactory.createFunctionSymbol(function.symbol, name);
                resources.add(functionSymbol);
            }
        }
        return resources;
    }

    /**
     * Represents Ballerina Service Symbol Builder.
     * 
     * @since 1.3.0
     */
    public static class ServiceSymbolBuilder extends VariableSymbolBuilder {
        
        public ServiceSymbolBuilder(String name, PackageID moduleID, BServiceSymbol serviceSymbol) {
            super(name, moduleID, serviceSymbol);
        }

        public BallerinaServiceSymbol build() {
            return new BallerinaServiceSymbol(this.name,
                    this.moduleID,
                    this.accessModifiers,
                    this.typeDescriptor,
                    (BServiceSymbol) this.bSymbol);
        }
    }
}
