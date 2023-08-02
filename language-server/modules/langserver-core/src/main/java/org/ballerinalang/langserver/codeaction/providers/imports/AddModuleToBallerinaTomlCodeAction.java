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
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.internal.environment.BallerinaUserHome;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.NodeList;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.LSPackageLoader.ModuleInfo;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
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
 * Code Action for add dependency for Ballerina.toml.
 *
 * @since 2201.8.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddModuleToBallerinaTomlCodeAction implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "Add Module to Ballerina.toml";
    
    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        if (!diagnostic.diagnosticInfo().code().equals(DiagnosticErrorCode.MODULE_NOT_FOUND.diagnosticId())) {
            return Collections.emptyList();
        }
        String commandTitle = CommandConstants.ADD_MODULE_TO_BALLERINA_TOML;
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }

        List<DiagnosticProperty<?>> properties = diagnostic.properties();
        if (properties.isEmpty()) {
            return Collections.emptyList();
        }
        
        String orgName;
        String pkgName;
        String[] orgNameAndRest = ((String) properties.get(0).value()).split("/");
        if (orgNameAndRest.length == 2) {
            orgName = orgNameAndRest[0];
            String[] pkgNameAndRest = orgNameAndRest[1].split("\\.");
            if (pkgNameAndRest.length == 0) {
                return Collections.emptyList();
            }
            pkgName = pkgNameAndRest[0];
        } else {
            return Collections.emptyList();
        }
        
        ArrayList<PackageVersion> versions = new ArrayList<>();
        getVersions(LSPackageLoader.getInstance(context.languageServercontext()), project.get(), orgName, pkgName, 
                versions);
        if (versions.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<BallerinaToml> toml = project.get().currentPackage().ballerinaToml();
        if (toml.isEmpty()) {
            return Collections.emptyList();
        }
        Position dependencyStart = new Position(getDependencyStartLine(toml.get()), 0);
        
        String dependency = "[[dependency]]\n" +
                "org = \"" + orgName + "\"\n" +
                "name = \"" + pkgName + "\"\n" +
                "version = \"" + getLatestVersion(versions) + "\"\n" +
                "repository = \"local\"\n\n";
        TextEdit textEdit = new TextEdit(new Range(dependencyStart, dependencyStart), dependency);
        CodeAction action = CodeActionUtil.createCodeAction(commandTitle, List.of(textEdit), 
                project.get().sourceRoot().resolve(ProjectConstants.BALLERINA_TOML).toString(), 
                CodeActionKind.QuickFix);
        return Collections.singletonList(action);
    }

    @Override
    public String getName() {
        return NAME;
    }
    
    private void getVersions(LSPackageLoader lsPackageLoader, Project project, String orgName, String pkgName, 
                             ArrayList<PackageVersion> versions) {
        BallerinaUserHome ballerinaUserHome = BallerinaUserHome.from(project.projectEnvironmentContext().environment());
        PackageRepository localRepository = ballerinaUserHome.localPackageRepository();
        List<ModuleInfo> modules = lsPackageLoader.getLocalRepoPackages(localRepository);
        for (ModuleInfo mod : modules) {
            if (mod.packageOrg().value().equals(orgName) && mod.packageName().value().equals(pkgName)) {
                versions.add(mod.packageVersion());
            }
        }
    }
    
    private String getLatestVersion(ArrayList<PackageVersion> versions) {
        PackageVersion latestVersion = versions.get(0);
        for (int i = 1; i < versions.size(); i++) {
            PackageVersion version = versions.get(i);
            if (version.compareTo(latestVersion) == SemanticVersion.VersionCompatibilityResult.GREATER_THAN) {
                latestVersion = version;
            }
        }
        return latestVersion.toString();
    }
    
    private int getDependencyStartLine(BallerinaToml toml) {
        DocumentNode tomlSyntaxTree = toml.tomlDocument().syntaxTree().rootNode();
        NodeList<DocumentMemberDeclarationNode> members = tomlSyntaxTree.members();
        for (DocumentMemberDeclarationNode member : members) {
            if (member.kind() == SyntaxKind.TABLE) {
                TableNode tableNode = (TableNode) member;
                if (TomlSyntaxTreeUtil.toQualifiedName(tableNode.identifier().value()).equals("package")) {
                    return tableNode.lineRange().endLine().line() + 2;
                }
            }
        }   
        return 0;
    }
}
