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
import org.wso2.ballerinalang.compiler.util.Name;

/**
 * A simple wrapper symbol for representing a worker symbol so that we can keep track of the symbol of the function
 * associated with the worker as well.
 *
 * @since 2.0.0
 */
public class BWorkerSymbol extends BVarSymbol {

    private BInvokableSymbol associatedFn;

    public BWorkerSymbol(long flags, Name varName, PackageID pkgID, BType type, BSymbol owner, Location location,
                         SymbolOrigin symbolOrigin) {
        super(flags, varName, pkgID, type, owner, location, symbolOrigin);
        this.kind = SymbolKind.WORKER;
    }

    public void setAssociatedFuncSymbol(BInvokableSymbol symbol) {
        this.associatedFn = symbol;
    }

    public BInvokableSymbol getAssociatedFuncSymbol() {
        return this.associatedFn;
    }
}
