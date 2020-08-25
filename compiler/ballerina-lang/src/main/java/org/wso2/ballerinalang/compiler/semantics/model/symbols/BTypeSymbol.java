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
import org.ballerinalang.model.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * @since 0.94
 */
public class BTypeSymbol extends BSymbol implements TypeSymbol {

    public boolean isLabel;

    public BTypeSymbol(int symTag, int flags, Name name, PackageID pkgID, BType type, BSymbol owner,
                       DiagnosticPos pos) {
        super(symTag, flags, name, pkgID, type, owner, pos, );
        this.isLabel = false;
    }

    @Override
    public String toString() {
        if (this.pkgID == PackageID.DEFAULT ||
                this.pkgID.equals(PackageID.ANNOTATIONS) ||
                this.pkgID.name == Names.DEFAULT_PACKAGE) {
            return this.name.value;
        }
        return this.pkgID.toString() + ":" + this.name;
    }

    @Override
    public BTypeSymbol createLabelSymbol() {
        BTypeSymbol typeSymbol = Symbols.createTypeSymbol(SymTag.TYPE_DEF, flags, Names.EMPTY, pkgID, type, owner, pos);
        typeSymbol.isLabel = true;
        return typeSymbol;
    }
}
