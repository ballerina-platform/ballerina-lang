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

import io.ballerina.compiler.api.symbols.DiagnosticState;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.Annotatable;
import org.ballerinalang.model.symbols.AnnotationSymbol;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.symbols.VariableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.VARIABLE;

/**
 * @since 0.94
 */
public class BVarSymbol extends BSymbol implements VariableSymbol, Annotatable {

    private List<BAnnotationSymbol> annots;
    public boolean isDefaultable = false;
    public boolean isWildcard = false;
    public DiagnosticState state = DiagnosticState.VALID;

    // Only used for type-narrowing. Cache of the original symbol.
    public BVarSymbol originalSymbol;
    // Used to identify whether the symbol is narrowed for a single if statement which is not completed normally.
    public boolean isAffectedByReachability = false;

    public BVarSymbol(long flags, Name name, Name originalName, PackageID pkgID, BType type, BSymbol owner,
                      Location pos, SymbolOrigin origin) {
        super(VARIABLE, flags, name, originalName, pkgID, type, owner, pos, origin);
        this.annots = new ArrayList<>();
        this.kind = SymbolKind.VARIABLE;
    }

    public BVarSymbol(long flags, Name name, PackageID pkgID, BType type, BSymbol owner, Location pos,
                      SymbolOrigin origin) {
        this(flags, name, name, pkgID, type, owner, pos, origin);
    }

    public BVarSymbol(long flags, boolean isIgnorable, Name name, Name originalName, PackageID pkgID, BType type,
                      BSymbol owner, Location pos, SymbolOrigin origin) {
        this(flags, name, originalName, pkgID, type, owner, pos, origin);
        this.isWildcard = isIgnorable;
    }

    @Override
    public void addAnnotation(AnnotationSymbol symbol) {
        if (symbol == null) {
            return;
        }
        this.annots.add((BAnnotationSymbol) symbol);
    }

    @Override
    public List<? extends AnnotationSymbol> getAnnotations() {
        return this.annots;
    }

    @Override
    public Object getConstValue() {
        return null;
    }

    @Override
    public SymbolKind getKind() {
        return this.kind;
    }
}
