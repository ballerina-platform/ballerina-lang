/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.XMLNamespaceSymbol;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;

/**
 * Represents an XML Namespace Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaXMLNSSymbol extends BallerinaSymbol implements XMLNamespaceSymbol {

    private final String namespaceUri;

    private BallerinaXMLNSSymbol(String name, PackageID moduleID, BSymbol symbol, String namespaceUri) {
        super(name, moduleID, SymbolKind.XMLNS, symbol);
        this.namespaceUri = namespaceUri;
    }

    @Override
    public String namespaceUri() {
        return this.namespaceUri;
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    public static class XmlNSSymbolBuilder extends SymbolBuilder<XmlNSSymbolBuilder> {

        protected String uri;

        /**
         * Symbol Builder's Constructor.
         *
         * @param name     Symbol Name
         * @param moduleID module ID of the symbol
         * @param symbol   namespace symbol
         */
        public XmlNSSymbolBuilder(String name, PackageID moduleID, BXMLNSSymbol symbol) {
            super(name, moduleID, SymbolKind.XMLNS, symbol);
            this.uri = symbol.namespaceURI;
        }

        @Override
        public BallerinaXMLNSSymbol build() {
            return new BallerinaXMLNSSymbol(this.name, this.moduleID, this.bSymbol, this.uri);
        }
    }
}
