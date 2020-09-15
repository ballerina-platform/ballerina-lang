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
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.net.URLClassLoader;
import java.util.Comparator;
import java.util.List;

/**
 * Represents the result of a ballerina file compilation.
 *
 * @since 0.94
 */
public class CompileResult {

    private PackageNode pkgNode;

    URLClassLoader classLoader;

    public CompileResult() {
    }

    public Diagnostic[] getDiagnostics() {
        List<Diagnostic> diagnostics = ((BLangPackage) this.pkgNode).getDiagnostics();
        diagnostics.sort(Comparator.comparing((Diagnostic d) -> d.location().lineRange().filePath()).
                thenComparingInt((Diagnostic d) -> d.location().lineRange().startLine().line()));
        return diagnostics.toArray(new Diagnostic[diagnostics.size()]);
    }

    public int getErrorCount() {
        return ((BLangPackage) this.pkgNode).getErrorCount();
    }

    public int getWarnCount() {
        return ((BLangPackage) this.pkgNode).getWarnCount();
    }

    public PackageNode getAST() {
        return pkgNode;
    }

    public void setAST(PackageNode pkgNode) {
        this.pkgNode = pkgNode;
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
}
