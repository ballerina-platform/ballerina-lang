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

import org.ballerina.compiler.api.SymbolFactory;
import org.ballerinalang.model.elements.PackageID;

/**
 * Represents the implementation of a Compiled Ballerina Symbol.
 * 
 * @since 1.3.0
 */
public class BallerinaSymbol implements BCompiledSymbol {
    private String name;
    private PackageID moduleID;
    private SymbolKind symbolKind;

    protected BallerinaSymbol(String name, PackageID moduleID, SymbolKind symbolKind) {
        this.name = name;
        this.moduleID = moduleID;
        this.symbolKind = symbolKind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleID getModuleID() {
        return SymbolFactory.createModuleID(this.moduleID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SymbolKind getKind() {
        return this.symbolKind;
    }

    /**
     * Represents Ballerina Symbol Builder.
     * @param <T> Symbol Type
     */
    protected abstract static class SymbolBuilder<T extends SymbolBuilder<T>> {
        protected String name;
        protected PackageID moduleID;
        protected SymbolKind symbolKind;
        /**
         * Symbol Builder Constructor.
         * 
         * @param name Symbol Name
         * @param moduleID module ID of the symbol
         */
        public SymbolBuilder(String name, PackageID moduleID, SymbolKind symbolKind) {
            this.name = name;
            this.moduleID = moduleID;
            this.symbolKind = symbolKind;
        }

        /**
         * Build the Ballerina Symbol.
         * 
         * @return {@link BallerinaSymbol} built
         */
        public abstract BallerinaSymbol build();
    }
}
