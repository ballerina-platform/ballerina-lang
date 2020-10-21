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
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    private final Path targetPath;

    private URLClassLoader classLoader;
    private final Diagnostic[] diagnostics;
    private int errorCount = 0;
    private int warnCount = 0;

    public CompileResult(Package pkg) {
        this.pkg = pkg;
        this.packageCompilation = pkg.getCompilation();
        this.targetPath = null;
        this.diagnostics = packageCompilation.diagnostics().toArray(new Diagnostic[]{});
        populateDiagnosticsCount();
    }

    public CompileResult(Package pkg, Path targetPath) {
        this.pkg = pkg;
        this.packageCompilation = pkg.getCompilation();
        this.targetPath = targetPath;
        this.diagnostics = packageCompilation.diagnostics().toArray(new Diagnostic[]{});
        populateDiagnosticsCount();
    }

    public PackageNode getAST() {
        return packageCompilation.defaultModuleBLangPackage();
    }

    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            try {
                this.classLoader = jarCacheClassLoader();
            } catch (IOException e) {
                throw new BLangRuntimeException("Error while creating class loader for compiled jar entries", e);
            }
        }
        return this.classLoader;
    }

    public Diagnostic[] getDiagnostics() {
        return this.diagnostics;
    }

    public int getErrorCount() {
        return this.errorCount;
    }

    public int getWarnCount() {
        return this.warnCount;
    }

    BIRNode.BIRPackage defaultModuleBIR() {
        return packageCompilation.defaultModuleBLangPackage().symbol.bir;
    }

    PackageDescriptor packageDescriptor() {
        return this.pkg.packageDescriptor();
    }

    private void addClasspathEntries(Path jarFilePath, List<URL> jarFiles) throws IOException {

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

    private URLClassLoader jarCacheClassLoader() throws IOException {
        List<URL> jarFiles = new ArrayList<>();
        addClasspathEntries(targetPath, jarFiles);
        URL[] urls = new URL[jarFiles.size()];
        urls = jarFiles.toArray(urls);
        return new URLClassLoader(urls);
    }

    private void populateDiagnosticsCount() {
        for (Diagnostic diagnostic : packageCompilation.diagnostics()) {
            switch (diagnostic.diagnosticInfo().severity()) {
                case WARNING:
                    warnCount++;
                    break;
                case ERROR:
                    errorCount++;
                    break;
            }
        }
    }

}
