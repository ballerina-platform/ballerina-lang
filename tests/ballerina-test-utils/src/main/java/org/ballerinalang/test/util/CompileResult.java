/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.util;

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.model.tree.PackageNode;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.diagnostic.DiagnosticComparator;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of a ballerina file compilation.
 *
 * @since 0.94
 */
public class CompileResult {

    private PackageNode pkgNode;
    private CompilerContext context;
    URLClassLoader classLoader;
    private Diagnostic[] diagnostics = null;
    private int errorCount = 0;
    private int warnCount = 0;

    public CompileResult(CompilerContext context, BLangPackage packageNode) {
        this.context = context;
        this.pkgNode = packageNode;
    }

    public Diagnostic[] getDiagnostics() {
        if (this.diagnostics == null) {
            populateDiagnostics();
        }

        return this.diagnostics;
    }

    public int getErrorCount() {
        if (diagnostics == null) {
            populateDiagnostics();
        }

        return this.errorCount;
    }

    public int getWarnCount() {
        if (diagnostics == null) {
            populateDiagnostics();
        }

        return this.warnCount;
    }

    public PackageNode getAST() {
        return pkgNode;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    void setClassLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (getErrorCount() == 0) {
            builder.append("Compilation Successful");
        } else {
            builder.append("Compilation Failed:\n");
            for (Diagnostic diag : this.getDiagnostics()) {
                builder.append(diag + "\n");
            }
        }
        return builder.toString();
    }

    private void populateDiagnostics() {
        BLangPackage bLangPkg = (BLangPackage) this.pkgNode;
        this.errorCount = bLangPkg.getErrorCount();
        this.warnCount = bLangPkg.getWarnCount();

        // Get diagnostics for imported packages
        List<Diagnostic> diagList = bLangPkg.getDiagnostics();
        PackageCache packageCache = PackageCache.getInstance(context);

        for (BLangImportPackage importedPackage : bLangPkg.getImports()) {
            BPackageSymbol pkgSymbol = importedPackage.symbol;
            if (pkgSymbol == null) {
                continue;
            }

            BLangPackage pkg = packageCache.get(pkgSymbol.pkgID);
            if (pkg == null) {
                continue;
            }

            diagList.addAll(pkg.getDiagnostics());
            this.errorCount += pkg.getErrorCount();
            this.warnCount += pkg.getWarnCount();
        }

        Collections.sort(diagList, new DiagnosticComparator());
        this.diagnostics = diagList.toArray(new Diagnostic[diagList.size()]);
    }
}
