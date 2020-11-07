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
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.model.tree.PackageNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

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
        classLoader = jBallerinaBackend.getClassLoader();
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

    BIRNode.BIRPackage defaultModuleBIR() {
        return packageCompilation.defaultModuleBLangPackage().symbol.bir;
    }

    PackageDescriptor packageDescriptor() {
        return this.pkg.packageDescriptor();
    }

    private void addClasspathEntries(Path jarFilePath, List<URL> jarFiles) throws IOException {

        if (!Files.exists(jarFilePath)) {
            return;
        }

        if (Files.isRegularFile(jarFilePath) &&
                jarFilePath.toString().endsWith(BLANG_COMPILED_JAR_EXT)) {
            jarFiles.add(jarFilePath.normalize().toUri().toURL());
            return;
        }

        Files.walk(jarFilePath)
                .filter(filePath -> Files.isRegularFile(filePath) &&
                        filePath.toString().endsWith(BLANG_COMPILED_JAR_EXT))
                .forEach(filePath -> {
                    try {
                        jarFiles.add(filePath.normalize().toUri().toURL());
                    } catch (MalformedURLException e) {
                        throw new BLangRuntimeException("Error while adding classpath libraries from :" +
                                jarFilePath.toString(), e);
                    }
                });

    }
}
