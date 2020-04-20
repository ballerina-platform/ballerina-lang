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

/**
 * Represents an XML Namespace Symbol.
 * 
 * @since 1.3.0
 */
public class XmlNSSymbol extends BallerinaSymbol {
    private XmlNSSymbol(String name, PackageID moduleID, SymbolKind symbolKind) {
        super(name, moduleID, symbolKind);
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    public static class XmlNSSymbolBuilder extends SymbolBuilder<XmlNSSymbolBuilder> {
        protected String name;
        protected PackageID moduleID;
        /**
         * Symbol Builder's Constructor.
         *
         * @param name Symbol Name
         * @param moduleID module ID of the symbol
         */
        public XmlNSSymbolBuilder(String name, PackageID moduleID, SymbolKind symbolKind) {
            super(name, moduleID, symbolKind);
        }

        public BallerinaSymbol build() {
            return new XmlNSSymbol(this.name, this.moduleID, symbolKind);
        }
    }
}
