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

import java.nio.file.Path;

import static io.ballerina.projects.util.ProjectConstants.GENERATED_MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.TARGET_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TOOL_CACHE_DIR;

public class ToolContext {

    Package packageInstance;
    String toolType;
    String toolId;
    String filePath;
    String targetModule;

    TomlTableNode optionsTable;

    Path cachePath;

    Path outputPath;

    ToolContext(Package packageInstance, String type, String toolId, String filePath,
                String targetModule, TomlTableNode optionsTable) {
        this.packageInstance = packageInstance;
        this.toolType = type;
        this.toolId = toolId;
        this.filePath = filePath;
        this.targetModule = targetModule;
        this.optionsTable = optionsTable;
        Path sourceRoot = packageInstance.project().sourceRoot();
        this.cachePath = sourceRoot.resolve(TARGET_DIR_NAME).resolve(TOOL_CACHE_DIR).resolve(toolId);
        if (targetModule == null || targetModule.isEmpty()) {
            this.outputPath = sourceRoot.resolve(GENERATED_MODULES_ROOT);
        } else {
            this.outputPath = sourceRoot.resolve(GENERATED_MODULES_ROOT).resolve(targetModule);
        }
    }

    public static ToolContext from(PackageManifest.Tool tool, Package packageInstance) {
        return new ToolContext(packageInstance, tool.getType(), tool.getId(),
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

    public Package packageInstance() {
        return packageInstance;
    }
}

