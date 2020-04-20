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

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ballerina module.
 * 
 * @since 1.3.0
 */
public class BallerinaModule extends BallerinaSymbol {
    private BPackageSymbol packageSymbol;
    
    protected BallerinaModule(String name,
                              PackageID moduleID,
                              SymbolKind symbolKind, BPackageSymbol packageSymbol) {
        super(name, moduleID, symbolKind);
        this.packageSymbol = packageSymbol;
    }

    /**
     * Get the type definitions defined within the module.
     * 
     * @return {@link List} of type definitions
     */
    public List<BallerinaTypeDefinition> getTypeDefinitions() {
        // todo: complete the generator
        return new ArrayList<>();
    }
    
    public List<BallerinaFunctionSymbol> getFunctions() {
        // todo: complete the generator
        return new ArrayList<>();
    }
    
    public List<BallerinaConstantSymbol> getConstants() {
        // todo: complete the generator
        return new ArrayList<>();
    }
    
    /**
     * Represents Ballerina Module Symbol Builder.
     */
    public static class ModuleSymbolBuilder extends SymbolBuilder<ModuleSymbolBuilder> {
        BPackageSymbol packageSymbol;
        
        /**
         * Symbol Builder's Constructor.
         *
         * @param name Symbol Name
         * @param moduleID module ID of the symbol
         */
        public ModuleSymbolBuilder(String name, PackageID moduleID, SymbolKind symbolKind) {
            super(name, moduleID, symbolKind);
        }

        /**
         * {@inheritDoc}
         */
        public BallerinaSymbol build() {
            if (this.packageSymbol == null) {
                throw new AssertionError("Package Symbol cannot be null");
            }
            return new BallerinaModule(this.name, this.moduleID, this.symbolKind, this.packageSymbol);
        }
        
        public ModuleSymbolBuilder withPackageSymbol(BPackageSymbol packageSymbol) {
            this.packageSymbol = packageSymbol;
            return this;
        }
    }
}
