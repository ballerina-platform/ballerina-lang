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
public class BallerinaConstantSymbol extends BallerinaVariable {
    private Object constValue;
    
    private BallerinaConstantSymbol(String name,
                                    PackageID moduleID,
                                    SymbolKind symbolKind,
                                    List<AccessModifier> accessModifiers,
                                    TypeDescriptor typeDescriptor,
                                    Object constValue) {
        super(name, moduleID, symbolKind, accessModifiers, typeDescriptor);
        this.constValue = constValue;
    }

    /**
     * Get the constant value.
     * 
     * @return {@link Object} value of the constant
     */
    public Object getConstValue() {
        return constValue;
    }

    /**
     * Represents Ballerina Constant Symbol Builder.
     */
    public static class ConstantSymbolBuilder extends VariableSymbolBuilder {
        private Object constantValue;
        /**
         * Symbol Builder's Constructor.
         *
         * @param name Symbol Name
         * @param moduleID module ID of the symbol
         */
        public ConstantSymbolBuilder(String name, PackageID moduleID, SymbolKind symbolKind) {
            super(name, moduleID, symbolKind);
        }

        public BallerinaSymbol build() {
            return new BallerinaConstantSymbol(this.name, this.moduleID, this.symbolKind, this.accessModifiers,
                    this.typeDescriptor, this.constantValue);
        }

        public ConstantSymbolBuilder withConstValue(Object constValue) {
            this.constantValue = constValue;
            return this;
        }
    }
}
