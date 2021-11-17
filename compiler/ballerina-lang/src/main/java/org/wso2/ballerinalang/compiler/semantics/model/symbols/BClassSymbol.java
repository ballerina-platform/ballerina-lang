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
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.Annotatable;
import org.ballerinalang.model.symbols.AnnotationSymbol;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BClassSymbol} represents a class symbol in a scope.
 *
 * @since 2.0
 */
public class BClassSymbol extends BObjectTypeSymbol implements Annotatable {

    public boolean isServiceDecl;
    private List<BAnnotationSymbol> annots;

    public BClassSymbol(int symTag, long flags, Name name, PackageID pkgID, BType type,
                        BSymbol owner, Location pos, SymbolOrigin origin) {
        super(symTag, flags, name, pkgID, type, owner, pos, origin);
        this.referencedFunctions = new ArrayList<>();
        this.annots = new ArrayList<>();
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
}
