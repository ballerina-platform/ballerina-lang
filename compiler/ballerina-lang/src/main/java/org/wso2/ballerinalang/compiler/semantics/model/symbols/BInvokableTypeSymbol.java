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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.jetbrains.annotations.Nullable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a function type symbol.
 *
 * @since 1.1.0
 */
public class BInvokableTypeSymbol extends BTypeSymbol {

    public List<BVarSymbol> params = new ArrayList<>();
    @Nullable
    public BVarSymbol restParam;
    @Nullable
    public BType returnType;
    public final List<BAnnotationAttachmentSymbol> returnTypeAnnots = new ArrayList<>();
    public final Map<String, BInvokableSymbol> defaultValues = new HashMap<>();
    public BInvokableTypeSymbol(long symTag, long flags, PackageID pkgID, @Nullable BType type, BSymbol owner,
                                Location location,
                                SymbolOrigin origin) {
        super(symTag, flags, Names.EMPTY, Names.EMPTY, pkgID, type, owner, location, origin);
    }

    @Override
    public SymbolKind getKind() {
        return SymbolKind.INVOKABLE_TYPE;
    }
}
