/* Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Ballerina compiler backend that does not emit backend code, used for testing BIR.
 *
 * @since 2.0.0
 */
public class NullBackend extends CompilerBackend {
    private final PackageContext packageContext;
    private final CompilerContext compilerContext;
    private DefaultDiagnosticResult diagnosticResult;

    private NullBackend(PackageCompilation compilation) {
        this.packageContext = compilation.packageContext();
        ProjectEnvironment projectEnvContext = this.packageContext.project().projectEnvironmentContext();
        this.compilerContext = projectEnvContext.getService(CompilerContext.class);
    }

    public static NullBackend from(PackageCompilation compilation) {
        NullBackend nullBackend = new NullBackend(compilation);
        performBIRGen(compilation, nullBackend);
        return nullBackend;
    }

    private static void performBIRGen(PackageCompilation compilation, NullBackend nullBackend) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        for (ModuleContext moduleContext : compilation.getResolution().topologicallySortedModuleList()) {
            moduleContext.generatePlatformSpecificCode(nullBackend.compilerContext, nullBackend);
            diagnostics.addAll(moduleContext.diagnostics());
        }
        nullBackend.diagnosticResult = new DefaultDiagnosticResult(diagnostics);
    }


    @Override
    public Collection<PlatformLibrary> platformLibraryDependencies(PackageId packageId) {
        return null;
    }

    @Override
    public Collection<PlatformLibrary> platformLibraryDependencies(PackageId packageId, PlatformLibraryScope scope) {
        return null;
    }

    @Override
    public PlatformLibrary codeGeneratedLibrary(PackageId packageId, ModuleName moduleName) {
        return null;
    }

    @Override
    public PlatformLibrary codeGeneratedOptimizedLibrary(PackageId packageId, ModuleName moduleName) {
        return null;
    }

    @Override
    public PlatformLibrary codeGeneratedTestLibrary(PackageId packageId, ModuleName moduleName) {
        return null;
    }

    @Override
    public PlatformLibrary codeGeneratedResourcesLibrary(PackageId packageId) {
        return null;
    }

    @Override
    public PlatformLibrary runtimeLibrary() {
        return null;
    }

    @Override
    public TargetPlatform targetPlatform() {
        return null;
    }

    @Override
    public void performCodeGen(ModuleContext moduleContext, CompilationCache compilationCache) {
    }

    @Override
    public String libraryFileExtension() {
        return null;
    }

    public boolean hasErrors() {
        if (diagnosticResult != null) {
            return diagnosticResult.hasErrors();
        }
        return true;
    }
}
