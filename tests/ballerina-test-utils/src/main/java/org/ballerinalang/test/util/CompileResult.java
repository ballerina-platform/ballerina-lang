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

import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents the result of a ballerina file compilation.
 *
 * @since 0.94
 */
public class CompileResult {

    private PackageNode pkgNode;
    private CompileResultDiagnosticListener diagnosticListener;

    URLClassLoader classLoader;

    public CompileResult(CompileResultDiagnosticListener diagnosticListener) {
        this.diagnosticListener = diagnosticListener;
    }

    public Diagnostic[] getDiagnostics() {
        List<Diagnostic> diagnostics = this.diagnosticListener.getDiagnostics();
        diagnostics.sort(Comparator.comparing((Diagnostic d) -> d.getSource().getCompilationUnitName()).
                thenComparingInt(d -> d.getPosition().getStartLine()));
        return diagnostics.toArray(new Diagnostic[diagnostics.size()]);
    }

    public int getErrorCount() {
        return this.diagnosticListener.errorCount;
    }

    public int getWarnCount() {
        return this.diagnosticListener.warnCount;
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
        if (this.diagnosticListener.errorCount == 0) {
            builder.append("Compilation Successful");
        } else {
            builder.append("Compilation Failed:\n");
            for (Diagnostic diag : this.getDiagnostics()) {
                builder.append(diag + "\n");
            }
        }
        return builder.toString();
    }

    /**
     * Diagnostic listener implementation for module compilation.
     *
     * @since 0.990.4
     */
    public static class CompileResultDiagnosticListener implements DiagnosticListener {
        private List<Diagnostic> diagnostics;
        private int errorCount = 0;
        private int warnCount = 0;

        public CompileResultDiagnosticListener() {
            this.diagnostics = new ArrayList<>();
        }

        @Override
        public void received(Diagnostic diagnostic) {
            this.diagnostics.add(diagnostic);
            switch (diagnostic.getKind()) {
                case ERROR:
                    errorCount++;
                    break;
                case WARNING:
                    warnCount++;
                    break;
                default:
                    break;
            }
        }

        public int getErrorCount() {
            return errorCount;
        }

        public int getWarnCount() {
            return warnCount;
        }

        public List<Diagnostic> getDiagnostics() {
            Collections.sort(diagnostics);
            return diagnostics;
        }
    }
}
