/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package io.ballerina.projects.util;

import io.ballerina.projects.ProjectException;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Utility methods required for tools.
 *
 * @since 2201.9.0
 */
public class ToolUtils {
    private ToolUtils() {}

    /**
     * Report a package diagnostic for tool not found.
     *
     * @param toolId tool id of the build tool
     * @return diagnostic
     */
    public static PackageDiagnostic getBuildToolNotFoundDiagnostic(String toolId) {
        String message = "Build tool '" + toolId + "' not found";
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(), message, DiagnosticSeverity.ERROR);
        return new PackageDiagnostic(diagnosticInfo, toolId);
    }

    /**
     * Validate the id field of a BalTool.toml.
     *
     * @param toolId id of a bal tool
     * @throws ProjectException an exception based on the validation violation
     */
    public static void validateBalToolId(String toolId) {
        StringBuilder errorMessage = new StringBuilder("Tool id should ");
        if (toolId == null || toolId.isEmpty()) {
            errorMessage.append("not be empty");
        } else if (!toolId.matches("^[a-zA-Z0-9_]+$")) {
            errorMessage.append("only contain alphanumeric characters and underscores");
        } else if (toolId.startsWith("_")) {
            errorMessage.append("not start with an underscore");
        } else if (toolId.endsWith("_")) {
            errorMessage.append("not end with an underscore");
        } else if (toolId.contains("__")) {
            errorMessage.append("not contain consecutive underscores");
        } else {
            return;
        }
        errorMessage.append(" in BalTool.toml file");
        throw new ProjectException(errorMessage.toString());
    }

    /**
     * Selects the class matching the name from the ServiceLoader.
     *
     * @param commandName name of the tool (tool type)
     * @param buildRunners service loader of CodeGeneratorTool interface
     * @return class that matches the tool name
     */
    public static Optional<CodeGeneratorTool> getTargetTool(
            String commandName, ServiceLoader<CodeGeneratorTool> buildRunners) {
        for (CodeGeneratorTool buildRunner : buildRunners) {
            if (ToolUtils.deriveSubcommandName(buildRunner.toolName()).equals(commandName)) {
                return Optional.of(buildRunner);
            }
        }
        return Optional.empty();
    }


    /**
     * Derive the fully qualified tool id using the array of strings passed.
     * Eg:- {"health", "fhir"} -> "health.fhir"
     *
     * @param toolName list of subcommands of a tool
     * @return the full qualified name of the tool
     */
    public static String deriveSubcommandName(String[] toolName) {
        return Arrays.stream(toolName).reduce((s1, s2) -> s1 + "." + s2).orElse("");
    }
}
