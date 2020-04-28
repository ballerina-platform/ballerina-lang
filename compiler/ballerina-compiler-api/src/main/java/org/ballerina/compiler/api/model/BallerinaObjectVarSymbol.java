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
import org.ballerina.compiler.api.types.ObjectTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ballerina Object Symbol.
 * 
 * @since 1.3.0
 */
public class BallerinaObjectVarSymbol extends BallerinaVariable {
    
    private BallerinaFunctionSymbol initFunction;
    private List<BallerinaFunctionSymbol> methods;
    
    private BallerinaObjectVarSymbol(String name,
                                     PackageID moduleID,
                                     ObjectTypeDescriptor typeDescriptor,
                                     BallerinaFunctionSymbol initFunction,
                                     List<BallerinaFunctionSymbol> methods,
                                     BSymbol symbol) {
        super(name, moduleID, BallerinaSymbolKind.VARIABLE, new ArrayList<>(), typeDescriptor, symbol);
        this.initFunction = initFunction;
        this.methods = methods;
    }

    /**
     * Get the initializer Function.
     * 
     * @return {@link BallerinaFunctionSymbol} init function symbol
     */
    public BallerinaFunctionSymbol getInitFunction() {
        return initFunction;
    }

    /**
     * Get the attached methods.
     * 
     * @return {@link List} of ballerina function symbols
     */
    public List<BallerinaFunctionSymbol> getMethods() {
        return methods;
    }

    /**
     * Represents Object Type Builder.
     * 
     * @since 1.3.0
     */
    public static class ObjectVarSymbolBuilder extends BallerinaVariable.VariableSymbolBuilder {
        
        private BallerinaFunctionSymbol initFunction;
        private List<BallerinaFunctionSymbol> methods;
        
        public ObjectVarSymbolBuilder(String name, PackageID moduleID, BObjectTypeSymbol bObjectTypeSymbol) {
            super(name, moduleID, bObjectTypeSymbol);
            withObjectTypeSymbol(bObjectTypeSymbol);
        }

        @Override
        public BallerinaObjectVarSymbol build() {
            if (this.typeDescriptor == null) {
                throw new AssertionError("Type Descriptor cannot be null");
            }
            return new BallerinaObjectVarSymbol(this.name,
                    this.moduleID,
                    (ObjectTypeDescriptor) this.typeDescriptor,
                    this.initFunction,
                    this.methods,
                    this.bSymbol);
        }

        private void withObjectTypeSymbol(BObjectTypeSymbol objectTypeSymbol) {
            this.initFunction = SymbolFactory.createFunctionSymbol(objectTypeSymbol.initializerFunc.symbol);
            List<BallerinaFunctionSymbol> list = new ArrayList<>();
            for (BAttachedFunction function : objectTypeSymbol.attachedFuncs) {
                BallerinaFunctionSymbol functionSymbol = SymbolFactory.createFunctionSymbol(function.symbol);
                list.add(functionSymbol);
            }
            this.methods = list;
        }
    }
}
