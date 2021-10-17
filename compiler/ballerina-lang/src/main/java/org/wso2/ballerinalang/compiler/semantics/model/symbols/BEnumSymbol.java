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

package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.Annotatable;
import org.ballerinalang.model.symbols.AnnotationSymbol;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an enum.
 *
 * @since 2.0.0
 */
public class BEnumSymbol extends BTypeSymbol implements Annotatable {

    public List<BConstantSymbol> members;
    private List<BAnnotationSymbol> annots;

    public BEnumSymbol(List<BConstantSymbol> members, long flags, Name name, Name originalName,
                       PackageID pkgID, BType type, BSymbol owner, Location pos, SymbolOrigin origin) {
        super(SymTag.ENUM, flags, name, originalName, pkgID, type, owner, pos, origin);
        this.members = members;
        this.kind = SymbolKind.ENUM;
        this.annots = new ArrayList<>();
    }

    public BEnumSymbol(List<BConstantSymbol> members, long flags, Name name, PackageID pkgID, BType type,
                       BSymbol owner, Location pos, SymbolOrigin origin) {
        this(members, flags, name, name, pkgID, type, owner, pos, origin);
    }

    @Override
    public void addAnnotation(AnnotationSymbol symbol) {
        if (symbol == null) {
            return;
        }
        this.annots.add((BAnnotationSymbol) symbol);
    }

    public void addAnnotations(List<BAnnotationSymbol> annotSymbols) {
        for (BAnnotationSymbol symbol : annotSymbols) {
            addAnnotation(symbol);
        }
    }

    @Override
    public List<? extends AnnotationSymbol> getAnnotations() {
        return this.annots;
    }
}
