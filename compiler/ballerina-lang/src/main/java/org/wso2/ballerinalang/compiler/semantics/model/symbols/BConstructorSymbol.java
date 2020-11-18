/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.symbols.ConstructorSymbol;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * @since 0.995
 */
public class BConstructorSymbol extends BInvokableSymbol implements ConstructorSymbol {

    public BConstructorSymbol(int flags,
                              Name name,
                              PackageID pkgID,
                              BType type,
                              BSymbol owner,
                              DiagnosticPos pos,
                              SymbolOrigin origin) {
        super(SymTag.CONSTRUCTOR, flags, name, pkgID, type, owner, pos, origin);
    }

    @Override
    public SymbolKind getKind() {
        return this.kind;
    }
}
