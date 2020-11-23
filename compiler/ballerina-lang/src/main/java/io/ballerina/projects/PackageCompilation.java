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
package io.ballerina.projects;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.projects.CompilerBackend.TargetPlatform;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Compilation at package level by resolving all the dependencies.
 *
 * @since 2.0.0
 */
public class PackageCompilation {
    private final PackageContext rootPackageContext;
    private final PackageResolution packageResolution;
    private final CompilerContext compilerContext;
    private final Map<TargetPlatform, CompilerBackend> compilerBackends;

    private DiagnosticResult diagnosticResult;
    private boolean compiled;

    private PackageCompilation(PackageContext rootPackageContext, PackageResolution packageResolution) {
        this.rootPackageContext = rootPackageContext;
        this.packageResolution = packageResolution;

        ProjectEnvironment projectEnvContext = rootPackageContext.project().projectEnvironmentContext();
        this.compilerContext = projectEnvContext.getService(CompilerContext.class);

        // We have only the jvm backend for now.
        this.compilerBackends = new HashMap<>(1);
    }

    static PackageCompilation from(PackageContext rootPackageContext) {
        PackageResolution packageResolution = rootPackageContext.getResolution();
        return new PackageCompilation(rootPackageContext, packageResolution);
    }

    public PackageResolution getResolution() {
        return packageResolution;
    }

    public DiagnosticResult diagnosticResult() {
        // TODO think about parallel invocations of this method
        if (!compiled) {
            compile();
        }
        return diagnosticResult;
    }

    public SemanticModel getSemanticModel(ModuleId moduleId) {
        // TODO think about parallel invocations of this method
        if (!compiled) {
            compile();
        }

        ModuleContext moduleContext = this.rootPackageContext.moduleContext(moduleId);
        // We check whether the particular module compilation state equal to the typecheck phase here. 
        // If the states do not match, then this is a illegal state exception.
        if (moduleContext.compilationState() != ModuleCompilationState.COMPILED) {
            throw new IllegalStateException("Semantic model cannot be retrieved when the module is in " +
                    "compilation state '" + moduleContext.compilationState().name() + "'. " +
                    "This is an internal error which will be fixed in a later release.");
        }

        return new BallerinaSemanticModel(moduleContext.bLangPackage(), this.compilerContext);
    }

    // TODO Remove this method. We should not expose BLangPackage from this class
    public BLangPackage defaultModuleBLangPackage() {
        return this.rootPackageContext.defaultModuleContext().bLangPackage();
    }

    @SuppressWarnings("unchecked")
    <T extends CompilerBackend> T getCompilerBackend(TargetPlatform targetPlatform,
                                                     Function<TargetPlatform, T> backendCreator) {
        return (T) compilerBackends.computeIfAbsent(targetPlatform, backendCreator);
    }

    PackageContext packageContext() {
        return rootPackageContext;
    }

    private void compile() {
        List<Diagnostic> diagnostics = new ArrayList<>();
        for (ModuleContext moduleContext : packageResolution.topologicallySortedModuleList()) {
            moduleContext.compile(compilerContext);
            diagnostics.addAll(moduleContext.diagnostics());
        }

        addOtherDiagnostics(diagnostics);
        diagnosticResult = new DefaultDiagnosticResult(diagnostics);
        compiled = true;
    }

    private void addOtherDiagnostics(List<Diagnostic> diagnostics) {
        Optional<BallerinaToml> ballerinaTomlOptional = rootPackageContext.ballerinaToml();
        if (ballerinaTomlOptional.isEmpty()) {
            return;
        }

        BallerinaToml ballerinaToml = ballerinaTomlOptional.get();
        diagnostics.addAll(ballerinaToml.diagnostics().allDiagnostics);
    }
}
