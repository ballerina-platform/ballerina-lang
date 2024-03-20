/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction.providers.imports;

import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.LSPackageLoader.ModuleInfo;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.toml.common.TomlSyntaxTreeUtil;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for adding a dependency to Ballerina.toml file.
 *
 * @since 2201.8.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddModuleToBallerinaTomlCodeAction implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "Add Module to Ballerina.toml";
    
    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return diagnostic.diagnosticInfo().code().equals(DiagnosticErrorCode.MODULE_NOT_FOUND.diagnosticId());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<BallerinaToml> toml = project.get().currentPackage().ballerinaToml();
        if (toml.isEmpty()) {
            return Collections.emptyList();
        }
        
        Optional<String> msg = positionDetails.diagnosticProperty(0);
        if (msg.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Consider orgname empty case
        String[] orgAndRest = msg.get().split("/");
        if (orgAndRest.length != 2) {
            return Collections.emptyList();
        }
        String org = orgAndRest[0];
        String[] moduleAndPrefix = orgAndRest[1].split(" as ");
        List<PackageVersion> loadedPackageVersions = new ArrayList<>();
        String pkg = resolvePackageVersionsAndName(LSPackageLoader.getInstance(context.languageServercontext()), 
                project.get(), org, moduleAndPrefix[0], loadedPackageVersions);
        if (loadedPackageVersions.isEmpty()) {
            return Collections.emptyList();
        }
        
        Position dependencyStart = new Position(getDependencyStartLine(toml.get()), 0);
        String dependency = String.format("[[dependency]]%norg = \"%s\"%nname = \"%s\"%nversion = " +
                "\"%s\"%nrepository = \"local\"%n%n", org, pkg, getLatestVersion(loadedPackageVersions));
        TextEdit textEdit = new TextEdit(new Range(dependencyStart, dependencyStart), dependency);
        CodeAction action = CodeActionUtil.createCodeAction(CommandConstants.ADD_MODULE_TO_BALLERINA_TOML, 
                List.of(textEdit), project.get().sourceRoot().resolve(ProjectConstants.BALLERINA_TOML).toString(), 
                CodeActionKind.QuickFix);
        return Collections.singletonList(action);
    }
    
    private String resolvePackageVersionsAndName(LSPackageLoader lsPackageLoader, Project project, String org,
                                                 String moduleName, List<PackageVersion> versions) {
        String packageName = moduleName;
        while (true) {
            int i = packageName.lastIndexOf(".");
            if (i == -1) {
                versions.addAll(getAvailablePackageVersionsFromLocalRepo(lsPackageLoader, project, org, packageName));
                if (!versions.isEmpty()) {
                    return packageName;
                }
                versions.addAll(getAvailablePackageVersionsFromLocalRepo(lsPackageLoader, project, org, moduleName));
                return moduleName;
            }
            packageName = packageName.substring(0, i);
            versions.addAll(getAvailablePackageVersionsFromLocalRepo(lsPackageLoader, project, org, packageName));
            if (!versions.isEmpty()) {
                return packageName;
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
    
    private List<PackageVersion> getAvailablePackageVersionsFromLocalRepo(
            LSPackageLoader lsPackageLoader, Project project, String orgName, String pkgName) {
        List<ModuleInfo> modules = lsPackageLoader.getLocalRepoModules();
        List<PackageVersion> versions = new ArrayList<>();
        for (ModuleInfo mod : modules) {
            if (mod.packageOrg().value().equals(orgName) && mod.packageName().value().equals(pkgName)) {
                versions.add(mod.packageVersion());
            }
        }
        return versions;
    }
    
    private String getLatestVersion(List<PackageVersion> versions) {
        PackageVersion latestVersion = versions.get(0);
        for (int i = 1; i < versions.size(); i++) {
            PackageVersion version = versions.get(i);
            if (version.value().greaterThanOrEqualTo(latestVersion.value())) {
                latestVersion = version;
            }
        }
        return latestVersion.toString();
    }
    
    private int getDependencyStartLine(BallerinaToml toml) {
        DocumentNode tomlSyntaxTree = toml.tomlDocument().syntaxTree().rootNode();
        return tomlSyntaxTree.members().stream()
                .filter(member -> member.kind().equals(SyntaxKind.TABLE) &&
                        TomlSyntaxTreeUtil.toQualifiedName(((TableNode) member).identifier().value()).equals("package"))
                .findFirst()
                .map(member -> ((TableNode) member).lineRange().endLine().line() + 2)
                .orElse(0);
    }
}
