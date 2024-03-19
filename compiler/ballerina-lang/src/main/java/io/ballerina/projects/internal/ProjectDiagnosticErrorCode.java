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
package io.ballerina.projects.internal;

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This class contains a list of project diagnostic error codes.
 *
 * @since 2.0.0
 */
public enum ProjectDiagnosticErrorCode implements DiagnosticCode {

    // Error codes used in the ManifestBuilder
    MISSING_PKG_INFO_IN_BALLERINA_TOML("BCE5001", "missing.package.info"),
    INVALID_PATH("BCE5002", "error.invalid.path"),
    INVALID_ICON("BCE5003", "error.invalid.icon"),
    INVALID_PROVIDED_DEPENDENCY("BCE5004", "invalid.provided.dependency"),
    INVALID_PROVIDED_SCOPE_IN_BUILD("BCE5005", "invalid.provided.scope"),

    // Error codes used in DependencyManifestBuilder.
    OLD_DEPENDENCIES_TOML("BCE5101", "old.dependencies.toml"),
    LOCAL_PACKAGES_IN_DEPENDENCIES_TOML("BCE5102", "local.packages.in.dependencies.toml"),
    CORRUPTED_DEPENDENCIES_TOML("BCE5103", "corrupted.dependencies.toml"),

    // Error codes used during dependency resolution.
    INCOMPATIBLE_DEPENDENCY_VERSIONS("BCE5201", "incompatible.dependency.versions"),
    PACKAGE_NOT_FOUND("BCE5202", "package.not.found"),
    DEPRECATED_PACKAGE("BCE5203", "deprecated.package"),
    BUILT_WITH_OLDER_SL_UPDATE_DISTRIBUTION("BCE5204", "built.with.older.sl.update.distribution"),
    CUSTOM_REPOSITORY_NOT_FOUND("BCE5205", "custom.repository.not.found"),

    // Error codes related to build tools.
    MISSING_TOOL_PROPERTIES_IN_BALLERINA_TOML("BCE5301", "missing.tool.properties"),
    INCOMPATIBLE_TYPE_FOR_TOOL_PROPERTY("BCE5302", "incompatible.tool.properties"),
    EMPTY_TOOL_PROPERTY("BCE5303", "empty.tool.properties"),
    TOOL_OPTIONS_VALIDATION_SKIPPED("BCE5304", "tool.options.validation.skipped"),
    RECURRING_TOOL_PROPERTIES("BCE5305", "recurring.tool.properties"),
    BUILD_TOOL_NOT_FOUND("BCE5306", "build.tool.not.found"),
    TOOL_OPTIONS_VALIDATION_FAILED("BCE5307", "tool.options.validation.failed"),

    // Error codes used for compiler plugins.
    UNSUPPORTED_COMPILER_PLUGIN_TYPE("BCE5401", "unsupported.compiler.plugin.type"),

    // Error codes used for Jar resolving.
    CONFLICTING_PLATFORM_JAR_FILES("BCE5501", "conflicting.platform.jars.type"),
    PROVIDED_PLATFORM_JAR_IN_EXECUTABLE("BCE5502", "provided.platform.jars"),
    ;

    private final String diagnosticId;
    private final String messageKey;

    ProjectDiagnosticErrorCode(String diagnosticId, String messageKey) {
        this.diagnosticId = diagnosticId;
        this.messageKey = messageKey;
    }

    @Override
    public DiagnosticSeverity severity() {
        return DiagnosticSeverity.ERROR;
    }

    @Override
    public String diagnosticId() {
        return diagnosticId;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }

    public boolean equals(DiagnosticCode code) {
        return this.messageKey.equals(code.messageKey());
    }
}
