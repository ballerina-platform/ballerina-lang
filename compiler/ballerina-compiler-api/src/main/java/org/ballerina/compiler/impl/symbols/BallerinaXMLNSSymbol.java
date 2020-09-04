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
package org.ballerina.compiler.impl.symbols;

import org.ballerina.compiler.api.symbols.SymbolKind;
import org.ballerina.compiler.api.symbols.XMLNamespaceSymbol;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

/**
 * Represents an XML Namespace Symbol.
 * 
 * @since 1.3.0
 */
public class BallerinaXMLNSSymbol extends BallerinaSymbol implements XMLNamespaceSymbol {

    private BallerinaXMLNSSymbol(String name, PackageID moduleID, BSymbol symbol) {
        super(name, moduleID, SymbolKind.XMLNS, symbol);
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    public static class XmlNSSymbolBuilder extends SymbolBuilder<XmlNSSymbolBuilder> {

        /**
         * Symbol Builder's Constructor.
         *
         * @param name Symbol Name
         * @param moduleID module ID of the symbol
         * @param symbol namespace symbol
         */
        public XmlNSSymbolBuilder(String name, PackageID moduleID, BSymbol symbol) {
            super(name, moduleID, SymbolKind.XMLNS, symbol);
        }

        @Override
        public BallerinaXMLNSSymbol build() {
            return new BallerinaXMLNSSymbol(this.name, this.moduleID, this.bSymbol);
        }
    }
}
