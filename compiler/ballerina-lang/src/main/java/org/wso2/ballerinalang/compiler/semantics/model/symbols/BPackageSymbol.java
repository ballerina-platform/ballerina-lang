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
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.repository.CompiledPackage;
import org.wso2.ballerinalang.compiler.CompiledJarFile;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.BIRPackageFile;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.PackageFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.PACKAGE;

/**
 * @since 0.94
 */
public class BPackageSymbol extends BTypeSymbol {

    public BInvokableSymbol initFunctionSymbol, startFunctionSymbol, stopFunctionSymbol, testInitFunctionSymbol,
            testStartFunctionSymbol, testStopFunctionSymbol;
    public List<BPackageSymbol> imports = new ArrayList<>();
    public PackageFile packageFile;
    public CompiledPackage compiledPackage;
    public Name compUnit;
    public boolean isUsed = false;

    // TODO Temporary mechanism to hold a reference to the generated bir model
    public BIRNode.BIRPackage bir;   // TODO try to remove this
    public BIRPackageFile birPackageFile;

    // kep code generated jar binary content in memory
    public CompiledJarFile compiledJarFile;

    // TODO Refactor following two flags
    public boolean entryPointExists = false;

    public BPackageSymbol(PackageID pkgID, BSymbol owner, DiagnosticPos pos, SymbolOrigin origin) {
        super(PACKAGE, 0, pkgID.name, pkgID, null, owner, pos, origin);
        this.type = new BPackageType(this);
    }

    public BPackageSymbol(PackageID pkgID, BSymbol owner, int flags, DiagnosticPos pos, SymbolOrigin origin) {
        this(pkgID, owner, pos, origin);
        this.flags = flags;
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
        return pkgID.equals(that.pkgID) && Symbols.isFlagOn(flags, that.flags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgID, flags);
    }

    @Override
    public BPackageSymbol createLabelSymbol() {
        BPackageSymbol copy = new BPackageSymbol(pkgID, owner, pos, origin);
        copy.initFunctionSymbol = initFunctionSymbol;
        copy.startFunctionSymbol = startFunctionSymbol;
        copy.stopFunctionSymbol = stopFunctionSymbol;
        copy.testInitFunctionSymbol = testInitFunctionSymbol;
        copy.testStartFunctionSymbol = testStartFunctionSymbol;
        copy.testStopFunctionSymbol = testStopFunctionSymbol;
        copy.packageFile = packageFile;
        copy.compiledPackage = compiledPackage;
        copy.entryPointExists = entryPointExists;
        copy.isLabel = true;
        return copy;
    }

    @Override
    public String toString() {
        return pkgID.toString();
    }

    public BIRNode.BIRPackage getBir() {
        return bir;
    }
}
