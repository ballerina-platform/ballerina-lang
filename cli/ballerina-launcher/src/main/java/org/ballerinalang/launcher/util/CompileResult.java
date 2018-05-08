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
package org.ballerinalang.launcher.util;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.diagnostic.Diagnostic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents the result of a ballerina file compilation.
 * 
 * @since 0.94
 */
public class CompileResult {

    private List<Diagnostic> diagnostics;
    private PackageNode pkgNode;
    private ProgramFile progFile;
    //Used for stateful function invocation.
    private WorkerExecutionContext context;
    private int errorCount = 0;
    private int warnCount = 0;

    public CompileResult() {
        diagnostics = new ArrayList<Diagnostic>();
    }

    public void addDiagnostic(Diagnostic diag) {
        this.diagnostics.add(diag);
        switch (diag.getKind()) {
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

    public Diagnostic[] getDiagnostics() {
        diagnostics.sort(Comparator.comparing((Diagnostic d) -> d.getSource().getCompilationUnitName()).
                thenComparingInt(d -> d.getPosition().getStartLine()));
        return diagnostics.toArray(new Diagnostic[diagnostics.size()]);
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarnCount() {
        return warnCount;
    }

    public ProgramFile getProgFile() {
        return progFile;
    }

    public void setProgFile(ProgramFile progFile) {
        this.progFile = progFile;
    }

    public PackageNode getAST() {
        return pkgNode;
    }

    public void setAST(PackageNode pkgNode) {
        this.pkgNode = pkgNode;
    }

    public WorkerExecutionContext getContext() {
        return context;
    }

    public void setContext(WorkerExecutionContext context) {
        this.context = context;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.errorCount == 0) {
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
