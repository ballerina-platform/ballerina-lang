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
import org.ballerina.compiler.api.semantic.TypesFactory;
import org.ballerina.compiler.api.types.ObjectTypeDescriptor;
import org.ballerina.compiler.api.types.TypeDescKind;
import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerina.compiler.api.types.TypeReferenceTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.util.Flags;

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
                                     BallerinaSymbolKind symbolKind,
                                     TypeDescriptor typeDescriptor,
                                     BallerinaFunctionSymbol initFunction,
                                     List<BallerinaFunctionSymbol> methods,
                                     BSymbol symbol) {
        super(name, moduleID, symbolKind, new ArrayList<>(), typeDescriptor, symbol);
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
     * Whether the object is an abstract object.
     *
     * @return {@link Boolean} status
     */
    public boolean isAbstract() {
        ObjectTypeDescriptor typeDesc;
        if (!this.getTypeDescriptor().isPresent()) {
            return false;
        }
        if (this.getTypeDescriptor().get().getKind() == TypeDescKind.TYPE_REFERENCE) {
            typeDesc = (ObjectTypeDescriptor) ((TypeReferenceTypeDescriptor) this.getTypeDescriptor().get())
                    .getTypeDescriptor();
        } else {
            typeDesc = ((ObjectTypeDescriptor) this.getTypeDescriptor().get());
        }
        return typeDesc.getTypeQualifiers().contains(ObjectTypeDescriptor.TypeQualifier.ABSTRACT);
    }

    /**
     * Get the client actions defined.
     * 
     * @return {@link List} of client actions
     */
    public List<BallerinaFunctionSymbol> getClientActions() {
        List<BallerinaFunctionSymbol> list = new ArrayList<>();
        if (this.getKind() == BallerinaSymbolKind.CLIENT) {
            for (BallerinaFunctionSymbol functionSymbol : methods) {
                if (functionSymbol.getKind() == BallerinaSymbolKind.REMOTE_FUNCTION) {
                    list.add(functionSymbol);
                }
            }
        }
        
        return list;
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
            withObjectTypeSymbol(bObjectTypeSymbol, name);
        }

        @Override
        public BallerinaObjectVarSymbol build() {
            if (this.typeDescriptor == null) {
                throw new AssertionError("Type Descriptor cannot be null");
            }
            BallerinaSymbolKind symbolKind;
            if (((this.bSymbol.flags & Flags.LISTENER) == Flags.LISTENER)) {
                symbolKind = BallerinaSymbolKind.LISTENER;
            } else if (((this.bSymbol.flags & Flags.CLIENT) == Flags.CLIENT)) {
                symbolKind = BallerinaSymbolKind.CLIENT;
            } else {
                symbolKind = BallerinaSymbolKind.OBJECT;
            }
            return new BallerinaObjectVarSymbol(this.name,
                    this.moduleID,
                    symbolKind,
                    this.typeDescriptor,
                    this.initFunction,
                    this.methods,
                    this.bSymbol);
        }

        private void withObjectTypeSymbol(BObjectTypeSymbol objectTypeSymbol, String name) {
            this.initFunction = SymbolFactory.createFunctionSymbol(objectTypeSymbol.initializerFunc.symbol, name);
            List<BallerinaFunctionSymbol> list = new ArrayList<>();
            for (BAttachedFunction function : objectTypeSymbol.attachedFuncs) {
                String funcName = function.symbol.getName().getValue();
                BallerinaFunctionSymbol functionSymbol = SymbolFactory.createFunctionSymbol(function.symbol, funcName);
                list.add(functionSymbol);
            }
            this.methods = list;
            this.typeDescriptor = TypesFactory.getTypeDescriptor(objectTypeSymbol.getType());
        }
    }
}
