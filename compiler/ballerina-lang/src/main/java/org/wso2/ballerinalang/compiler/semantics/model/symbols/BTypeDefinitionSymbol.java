/*
*  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.0.0
 */
public class BTypeDefinitionSymbol extends BSymbol {

    public BTypeReferenceType referenceType = null;
    public List<BLangAnnotationAttachment> annAttachments;

    public BTypeDefinitionSymbol(long flags, Name name, PackageID pkgID, BType type, BSymbol owner,
                                 Location pos, SymbolOrigin origin) {
        super(SymTag.TYPE_DEF, flags, name, pkgID, type, owner, pos, origin);
        this.kind = SymbolKind.TYPE_DEF;
        this.annAttachments = new ArrayList<>();
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
}
