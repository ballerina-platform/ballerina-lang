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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final String toolId;
    private final String filePath;
    private final String targetModule;
    private final Map<String, Object> options;
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    ToolContext(Package currentPackage, String toolId, String filePath,
                String targetModule, TomlTableNode optionsTable) {
        this.currentPackage = currentPackage;
        this.toolId = toolId;
        this.filePath = filePath;
        this.targetModule = targetModule;
        this.options = getOptions(optionsTable);
    }

    public static ToolContext from(PackageManifest.Tool tool, Package currentPackage) {
        return new ToolContext(currentPackage, tool.getId(),
                tool.getFilePath(), tool.getTargetModule(),
                tool.getOptionsTable());
    }

    /**
     * @return the id of the tool configuration.
     */
    public String toolId() {
        return this.toolId;
    }

    /**
     * @return the filepath extracted from tool configuration.
     */
    public String filePath() {
        return this.filePath;
    }

    /**
     * @return the target module extracted from tool configuration.
     */
    public String targetModule() {
        return targetModule;
    }

    /**
     * @return a map of the optional tool configurations.
     */
    public Map<String, Object> options() {
        return this.options;
    }

    /**
     * @return the cache path derived using the tool id.
     */
    public Path cachePath() {
        Path sourceRoot = currentPackage.project().sourceRoot();
        return sourceRoot.resolve(TARGET_DIR_NAME).resolve(TOOL_CACHE_DIR).resolve(toolId);
    }

    /**
     * @return the output path derived using the target module
     */
    public Path outputPath() {
        Path sourceRoot = currentPackage.project().sourceRoot();
        if (targetModule == null || targetModule.isEmpty()) {
            return sourceRoot.resolve(GENERATED_MODULES_ROOT);
        } else {
            return sourceRoot.resolve(GENERATED_MODULES_ROOT).resolve(targetModule);
        }
    }

    /**
     * @return the current package instance.
     */
    public Package currentPackage() {
        return currentPackage;
    }

    /**
     * @return a list of tool diagnostics.
     */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /**
     * Reports a diagnostic against the build tool executed.
     *
     * @param diagnostic the {@code Diagnostic} to be reported
     */
    public void reportDiagnostic(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    private Map<String, Object> getOptions(TomlTableNode optionsTable) {
        Map<String, Object> options = new HashMap<>();
        if (null == optionsTable) {
            return options;
        }
        for (String option: optionsTable.entries().keySet()) {
            options.put(option, optionsTable.entries().get(option).toNativeObject());
        }
        return options;
    }
}

