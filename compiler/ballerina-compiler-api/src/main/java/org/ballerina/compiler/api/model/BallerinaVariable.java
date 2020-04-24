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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ballerina variable.
 * 
 * @since 1.3.0
 */
public class BallerinaVariable extends BallerinaSymbol {
    
    private List<AccessModifier> accessModifiers;
    private TypeDescriptor typeDescriptor;
    
    protected BallerinaVariable(String name,
                              PackageID moduleID,
                              BallerinaSymbolKind ballerinaSymbolKind,
                              List<AccessModifier> accessModifiers,
                              TypeDescriptor typeDescriptor) {
        super(name, moduleID, ballerinaSymbolKind);
        this.accessModifiers = accessModifiers;
        this.typeDescriptor = typeDescriptor;
    }

    /**
     * Get the list of access modifiers attached to this Variable symbol.
     * 
     * @return {@link List} of access modifiers
     */
    public List<AccessModifier> getAccessModifiers() {
        return accessModifiers;
    }

    /**
     * Get the Type of the variable.
     * 
     * @return {@link TypeDescriptor} of the variable
     */
    public TypeDescriptor getTypeDescriptor() {
        return typeDescriptor;
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    public static class VariableSymbolBuilder extends SymbolBuilder<VariableSymbolBuilder> {
        protected List<AccessModifier> accessModifiers = new ArrayList<>();
        protected TypeDescriptor typeDescriptor;
        /**
         * Symbol Builder's Constructor.
         *
         * @param name Symbol Name
         * @param moduleID module ID of the symbol
         */
        public VariableSymbolBuilder(String name, PackageID moduleID) {
            super(name, moduleID, BallerinaSymbolKind.VARIABLE);
        }

        public BallerinaVariable build() {
            return new BallerinaVariable(this.name, this.moduleID, this.ballerinaSymbolKind, this.accessModifiers,
                    this.typeDescriptor);
        }
        
        public VariableSymbolBuilder withTypeDescriptor(TypeDescriptor typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }
        
        public VariableSymbolBuilder withAccessModifier(AccessModifier accessModifier) {
            this.accessModifiers.add(accessModifier);
            return this;
        }
    }
}
