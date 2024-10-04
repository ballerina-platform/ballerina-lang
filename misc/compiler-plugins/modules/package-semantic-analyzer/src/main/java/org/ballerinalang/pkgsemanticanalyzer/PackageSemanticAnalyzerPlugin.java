/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.pkgsemanticanalyzer;

import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilationAnalysisContext;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code PackageSemanticAnalyzerPlugin} analyzes package semantics after compilation phase.
 *
 * @since 2.0.0
 */
public class PackageSemanticAnalyzerPlugin extends CompilerPlugin {
    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeAnalyzer(new PackageSemanticAnalyzer());
    }

    /**
     * {@code PackageSemanticAnalyzer} analyzes package semantics and adds diagnostics after compilation phase.
     *
     * @since 2.0.0
     */
    public static class PackageSemanticAnalyzer extends CodeAnalyzer {
        @Override
        public void init(CodeAnalysisContext analysisContext) {
            analysisContext.addCompilationAnalysisTask(compilationAnalysisContext -> {
                List<String> exportedModules = compilationAnalysisContext.currentPackage().manifest().exportedModules();
                List<String> packageModules = new ArrayList<>();

                // Add package modules to `packageModules` list
                for (ModuleId moduleId : compilationAnalysisContext.currentPackage().moduleIds()) {
                    Module module = compilationAnalysisContext.currentPackage().module(moduleId);
                    packageModules.add(module.moduleName().toString());
                }

                for (String exportedModule : exportedModules) {
                    if (!packageModules.contains(exportedModule)) {
                        TomlNodeLocation location = getExportValueNodeLocation(compilationAnalysisContext,
                                                                               exportedModule);
                        reportTomlDiagnostic(compilationAnalysisContext, location, null,
                                             "exported module '" + exportedModule + "' is not a module of the package",
                                             DiagnosticSeverity.ERROR);
                    }
                }
            });
        }
    }

    private static TomlNodeLocation getExportValueNodeLocation(CompilationAnalysisContext compilationAnalysisContext,
                                                               String exportModuleName) {
        Package currentPackage = compilationAnalysisContext.currentPackage();
        if (currentPackage.ballerinaToml().isPresent()) {
            TomlTableNode rootNode = currentPackage.ballerinaToml().get().tomlAstNode();
            TomlTableNode pkgNode = (TomlTableNode) rootNode.entries().get("package");

            TopLevelNode exportNode = pkgNode.entries().get("export");
            if (exportNode != null && exportNode.kind() != TomlType.NONE) {
                TomlValueNode exportValueNode = ((TomlKeyValueNode) exportNode).value();
                if (exportValueNode.kind() == TomlType.ARRAY) {
                    TomlArrayValueNode exportArrayValueNode = (TomlArrayValueNode) exportValueNode;
                    for (TomlValueNode value : exportArrayValueNode.elements()) {
                        if (value.kind() == TomlType.STRING) {
                            if (exportModuleName.equals(((TomlStringValueNode) value).getValue())) {
                                return value.location();
                            }

                        }
                    }
                }
            }
            // This exception should not throw
            throw new ProjectException("exported module does not exists in the 'Ballerina.toml': " + exportModuleName);
        }
        // This exception should not throw
        throw new ProjectException("'Ballerina.toml' does not exist in the current project");
    }

    private static void reportTomlDiagnostic(CompilationAnalysisContext compilationAnalysisContext,
                                             TomlNodeLocation location, @Nullable String code, String message,
                                             DiagnosticSeverity severity) {
        var diagnosticInfo = new DiagnosticInfo(code, message, severity);
        var tomlDiagnostic = new TomlDiagnostic(location, diagnosticInfo, message);
        compilationAnalysisContext.reportDiagnostic(tomlDiagnostic);
    }
}
