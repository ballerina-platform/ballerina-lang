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

import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;

import java.util.List;

/**
 * Represent FUnction Symbol.
 * 
 * @since 1.3.0
 */
public class BallerinaFunctionSymbol extends BallerinaVariable {
    private BInvokableSymbol invokableSymbol;
    
    private BallerinaFunctionSymbol(String name,
                                    PackageID moduleID,
                                    SymbolKind symbolKind,
                                    List<AccessModifier> accessModifiers,
                                    TypeDescriptor typeDescriptor,
                                    BInvokableSymbol invokableSymbol) {
        super(name, moduleID, symbolKind, accessModifiers, typeDescriptor);
        this.invokableSymbol = invokableSymbol;
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    public static class FunctionSymbolBuilder extends BallerinaVariable.VariableSymbolBuilder {
        private BInvokableSymbol invokableSymbol;
        /**
         * Symbol Builder's Constructor.
         *
         * @param name Symbol Name
         * @param moduleID module ID of the symbol
         */
        public FunctionSymbolBuilder(String name, PackageID moduleID, SymbolKind symbolKind) {
            super(name, moduleID, symbolKind);
        }

        public BallerinaSymbol build() {
            return new BallerinaFunctionSymbol(this.name, this.moduleID, this.symbolKind, this.accessModifiers,
                    this.typeDescriptor, this.invokableSymbol);
        }

        public FunctionSymbolBuilder withInvokableSymbol(BInvokableSymbol invokableSymbol) {
            this.invokableSymbol = invokableSymbol;
            return this;
        }
    }
    
    // TODO: Implement the params and return type getters 
}
