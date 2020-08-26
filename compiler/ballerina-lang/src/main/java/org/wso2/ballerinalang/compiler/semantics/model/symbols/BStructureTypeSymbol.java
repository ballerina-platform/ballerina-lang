/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BStructureTypeSymbol} represents a structure type symbol in a scope.
 *
 * @since 0.971.0
 */
public abstract class BStructureTypeSymbol extends BTypeSymbol {

    public List<BAttachedFunction> attachedFuncs;
    public BAttachedFunction initializerFunc;

    BStructureTypeSymbol(SymbolKind kind, int symTag, int flags, Name name, PackageID pkgID, BType type,
                         BSymbol owner, DiagnosticPos pos) {
        super(symTag, flags, name, pkgID, type, owner, pos, );
        this.attachedFuncs = new ArrayList<>(0);
        this.kind = kind;
    }
}
