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
package io.ballerina.projects;

import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.projects.util.ProjectConstants.GENERATED_MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.TARGET_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TOOL_CACHE_DIR;

/**
 * Represents a build tool instance.
 *
 * @since 22201.9.0
 */
public class ToolContext {

    private final Package currentPackage;
    private final String toolType;
    private final String toolId;
    private final String filePath;
    private final String targetModule;
    private final TomlTableNode optionsTable;
    private final Path cachePath;
    private final Path outputPath;
    private List<Diagnostic> diagnostics = new ArrayList<>();

    ToolContext(Package currentPackage, String type, String toolId, String filePath,
                String targetModule, TomlTableNode optionsTable) {
        this.currentPackage = currentPackage;
        this.toolType = type;
        this.toolId = toolId;
        this.filePath = filePath;
        this.targetModule = targetModule;
        this.optionsTable = optionsTable;
        Path sourceRoot = currentPackage.project().sourceRoot();
        this.cachePath = sourceRoot.resolve(TARGET_DIR_NAME).resolve(TOOL_CACHE_DIR).resolve(toolId);
        if (targetModule == null || targetModule.isEmpty()) {
            this.outputPath = sourceRoot.resolve(GENERATED_MODULES_ROOT);
        } else {
            this.outputPath = sourceRoot.resolve(GENERATED_MODULES_ROOT).resolve(targetModule);
        }
    }

    public static ToolContext from(PackageManifest.Tool tool, Package currentPackage) {
        return new ToolContext(currentPackage, tool.getType(), tool.getId(),
                tool.getFilePath(), tool.getTargetModule(),
                tool.getOptionsTable());
    }

    public String toolId() {
        return this.toolId;
    }

    public String toolType() {
        return this.toolType;
    }

    public String filePath() {
        return this.filePath;
    }

    public String targetModule() {
        return targetModule;
    }

    public TomlTableNode optionsTable() {
        return this.optionsTable;
    }

    public Path cachePath() {
        return cachePath;
    }

    public Path outputPath() {
        return outputPath;
    }

    public Package currentPackage() {
        return currentPackage;
    }
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    public void reportDiagnostic(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }
}

