/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.internal;

import io.ballerina.projects.ProjectException;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.WorkspaceManifest;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;
import static io.ballerina.projects.util.TomlUtil.createDiagnostic;
import static io.ballerina.projects.util.TomlUtil.getStringArrayFromTableNode;

public class WorkspaceManifestBuilder {
    private final TomlDocument workspaceBallerinaToml;
    private final Path workspaceRoot;
    private final ArrayList<Path> packages;
    private final ArrayList<Diagnostic> diagnosticList;

    private WorkspaceManifestBuilder(TomlDocument tomlDocument, Path workspaceRoot) {
        this.workspaceBallerinaToml = tomlDocument;
        this.workspaceRoot = workspaceRoot;
        this.diagnosticList = new ArrayList<>();
        this.packages = new ArrayList<>();
        validateBallerinaToml();
    }

    private void validateBallerinaToml() {
        TomlValidator wpBallerinaTomlValidator;
        try {
            wpBallerinaTomlValidator = new TomlValidator(
                    Schema.from(FileUtils.readFileAsString("workspace-ballerina-toml-schema.json")));
        } catch (IOException e) {
            throw new ProjectException("Failed to read the workspace Ballerina.toml validator schema file.");
        }
        Toml toml = workspaceBallerinaToml.toml();
        wpBallerinaTomlValidator.validate(toml);
        TomlTableNode tomlAstNode = toml.rootNode();

        if (!tomlAstNode.entries().isEmpty()) {
            TopLevelNode topLevelPkgNode = tomlAstNode.entries().get("workspace");
            if (topLevelPkgNode != null && topLevelPkgNode.kind() == TomlType.TABLE) {
                TomlTableNode pkgNode = (TomlTableNode) topLevelPkgNode;
                List<String> packages = getStringArrayFromTableNode(pkgNode, "packages");
                for (String packagePath : packages) {
                    Path projectRoot = this.workspaceRoot.resolve(packagePath);
                    Path ballerinaTomlPath = projectRoot.resolve(BALLERINA_TOML);
                    if (!Files.exists(ballerinaTomlPath)) {
                        Diagnostic diagnostic = createDiagnostic(pkgNode,
                                "could not locate the package path '" + packagePath + "'",
                                ProjectDiagnosticErrorCode.INVALID_PATH, DiagnosticSeverity.ERROR);
                        diagnosticList.add(diagnostic);
                        continue;
                    }
                    if (!Files.isDirectory(projectRoot)) {
                        String message = "invalid package path '" + packagePath + "'. Expected a directory";
                        Diagnostic diagnostic = createDiagnostic(pkgNode, message,
                                ProjectDiagnosticErrorCode.INVALID_PATH, DiagnosticSeverity.ERROR);
                        diagnosticList.add(diagnostic);
                        continue;
                    }
                    this.packages.add(workspaceRoot.resolve(packagePath).toAbsolutePath().normalize());
                }
            }
        }
    }

    public static WorkspaceManifestBuilder from(TomlDocument tomlDocument, Path workspaceRoot) {
        return new WorkspaceManifestBuilder(tomlDocument, workspaceRoot);
    }

    public WorkspaceManifest manifest() {
        return new WorkspaceManifest(packages, new DefaultDiagnosticResult(diagnosticList));
    }
}
