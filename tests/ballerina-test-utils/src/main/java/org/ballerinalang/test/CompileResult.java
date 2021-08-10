/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.model.tree.PackageNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Projects based test compilation result.
 *
 * @since 2.0.0
 */
public class CompileResult {

    private final Package pkg;
    private final PackageCompilation packageCompilation;

    private ClassLoader classLoader;
    private final JBallerinaBackend jBallerinaBackend;
    private final DiagnosticResult diagnosticResult;
    private final Diagnostic[] diagnostics;

    public CompileResult(Package pkg, JBallerinaBackend jBallerinaBackend) {
        this.pkg = pkg;
        this.packageCompilation = pkg.getCompilation();
        this.jBallerinaBackend = jBallerinaBackend;
        this.diagnosticResult = jBallerinaBackend.diagnosticResult();
        this.diagnostics = diagnosticResult.diagnostics().toArray(new Diagnostic[0]);
    }

    Path projectSourceRoot() {
        return pkg.project().sourceRoot();
    }
    
    public Project project() {
        return pkg.project();
    }

    public PackageNode getAST() {
        return packageCompilation.defaultModuleBLangPackage();
    }

    public SemanticModel defaultModuleSemanticModel() {
        return packageCompilation.getSemanticModel(pkg.getDefaultModule().moduleId());
    }

    public SemanticModel semanticModel(String moduleName) {
        Module module = pkg.module(ModuleName.from(pkg.packageName(), moduleName));
        return packageCompilation.getSemanticModel(module.moduleId());
    }

    public ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        classLoader = jBallerinaBackend.jarResolver().getClassLoaderWithRequiredJarFilesForExecution();
        return classLoader;
    }

    public Diagnostic[] getDiagnostics() {
        return this.diagnostics;
    }

    public int getErrorCount() {
        return diagnosticResult.errorCount();
    }

    public int getWarnCount() {
        return diagnosticResult.warningCount();
    }

    public int getHintCount() {
        return diagnosticResult.hintCount();
    }

    BIRNode.BIRPackage defaultModuleBIR() {
        return packageCompilation.defaultModuleBLangPackage().symbol.bir;
    }

    PackageManifest packageManifest() {
        return this.pkg.manifest();
    }

    public DiagnosticResult getDiagnosticResult() {
        return diagnosticResult;
    }

    public PackageCompilation getPackageCompilation() {
        return packageCompilation;
    }

    public Collection<JarLibrary> getJarPathRequiredForExecution() {
        return jBallerinaBackend.jarResolver().getJarFilePathsRequiredForExecution();
    }
}
