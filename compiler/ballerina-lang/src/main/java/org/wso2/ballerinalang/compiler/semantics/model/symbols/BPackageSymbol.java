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
import org.ballerinalang.repository.CompiledPackage;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.PackageFile;

import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.PACKAGE;

/**
 * @since 0.94
 */
public class BPackageSymbol extends BTypeSymbol {

    public BInvokableSymbol initFunctionSymbol, startFunctionSymbol, stopFunctionSymbol;
    public PackageFile packageFile;
    public CompiledPackage compiledPackage;

    // TODO Refactor following two flags
    public boolean entryPointExists = false;
    public boolean initFunctionsInvoked = false;

    public BPackageSymbol(PackageID pkgID, BSymbol owner) {
        super(PACKAGE, 0, pkgID.name, pkgID, null, owner);
        this.type = new BPackageType(this);
    }

    @Override
    public SymbolKind getKind() {
        return SymbolKind.PACKAGE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BPackageSymbol that = (BPackageSymbol) o;
        return pkgID.equals(that.pkgID);
    }

    @Override
    public int hashCode() {
        return pkgID.hashCode();
    }
}
