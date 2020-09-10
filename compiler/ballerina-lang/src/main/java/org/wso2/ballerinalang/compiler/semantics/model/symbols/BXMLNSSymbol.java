/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.symbols.VariableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * @since 0.94
 */
public class BXMLNSSymbol extends BSymbol implements VariableSymbol {

    /**
     * Holds the namespace URI for lookup during semantic validations.
     */
    public String namespaceURI;

    public BXMLNSSymbol(Name prefix, String namespaceURI, PackageID pkgID, BSymbol owner, DiagnosticPos pos,
                        SymbolOrigin origin) {
        super(SymTag.XMLNS, 0, prefix, pkgID, new BNoType(TypeTags.XMLNS), owner, pos, origin);
        this.namespaceURI = namespaceURI;
        this.kind = SymbolKind.XMLNS;
    }

    @Override
    public Object getConstValue() {
        return null;
    }

    @Override
    public SymbolKind getKind() {
        return SymbolKind.XMLNS;
    }
}
