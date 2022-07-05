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

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.XMLNamespaceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * Represents an XML Namespace Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaXMLNSSymbol extends BallerinaSymbol implements XMLNamespaceSymbol {

    private final String namespaceUri;

    private BallerinaXMLNSSymbol(String name, BSymbol symbol, String namespaceUri, CompilerContext context) {
        super(name, SymbolKind.XMLNS, symbol, context);
        this.namespaceUri = namespaceUri;
    }

    @Override
    public String namespaceUri() {
        return this.namespaceUri;
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    public static class XmlNSSymbolBuilder extends SymbolBuilder<XmlNSSymbolBuilder> {

        protected String uri;

        /**
         * Symbol Builder's Constructor.
         *
         * @param name    Symbol Name
         * @param symbol  namespace symbol
         * @param context context of the compilation
         */
        public XmlNSSymbolBuilder(String name, BXMLNSSymbol symbol, CompilerContext context) {
            super(name, SymbolKind.XMLNS, symbol, context);
            this.uri = symbol.namespaceURI;
        }

        @Override
        public BallerinaXMLNSSymbol build() {
            return new BallerinaXMLNSSymbol(this.name, this.bSymbol, this.uri, this.context);
        }
    }
}
