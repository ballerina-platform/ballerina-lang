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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.Optional;

/**
 * Represents the implementation of a Compiled Ballerina Symbol.
 * 
 * @since 1.3.0
 */
public class BallerinaSymbol implements BCompiledSymbol {
    private String name;
    private PackageID moduleID;
    private BallerinaSymbolKind ballerinaSymbolKind;
    private DocAttachment docAttachment;
    private boolean isLangLib;

    protected BallerinaSymbol(String name,
                              PackageID moduleID,
                              BallerinaSymbolKind ballerinaSymbolKind,
                              BSymbol symbol) {
        this.name = name;
        this.moduleID = moduleID;
        this.ballerinaSymbolKind = ballerinaSymbolKind;
        this.docAttachment = getDocAttachment(symbol);
        this.isLangLib = symbol != null && ((symbol.flags & Flags.LANG_LIB) == Flags.LANG_LIB);
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
        return new ModuleID(this.moduleID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BallerinaSymbolKind getKind() {
        return this.ballerinaSymbolKind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DocAttachment> getDocAttachment() {
        return Optional.ofNullable(this.docAttachment);
    }

    /**
     * Whether the symbol is a langlib symbol.
     * 
     * @return {@link Boolean} whether langlib or not
     */
    public boolean isLangLib() {
        return isLangLib;
    }

    private DocAttachment getDocAttachment(BSymbol symbol) {
        return symbol == null ? null : new DocAttachment(symbol.markdownDocumentation);
    }
    
    /**
     * Represents Ballerina Symbol Builder.
     * @param <T> Symbol Type
     */
    protected abstract static class SymbolBuilder<T extends SymbolBuilder<T>> {
        protected String name;
        protected PackageID moduleID;
        protected BallerinaSymbolKind ballerinaSymbolKind;
        protected BSymbol bSymbol;
        
        /**
         * Symbol Builder Constructor.
         * 
         * @param name Symbol Name
         * @param moduleID module ID of the symbol
         * @param symbolKind symbol kind
         * @param bSymbol symbol to evaluate
         */
        public SymbolBuilder(String name, PackageID moduleID, BallerinaSymbolKind symbolKind, BSymbol bSymbol) {
            this.name = name;
            this.moduleID = moduleID;
            this.ballerinaSymbolKind = symbolKind;
            this.bSymbol = bSymbol;
        }

        /**
         * Build the Ballerina Symbol.
         * 
         * @return {@link BallerinaSymbol} built
         */
        public abstract BallerinaSymbol build();
    }
}
