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

    INVALID_BALA_FILE("BCE5000", "invalid.bala.file"),
    OLD_DEPENDENCIES_TOML("BCE5001", "old.dependencies.toml"),
    LOCAL_PACKAGES_IN_DEPENDENCIES_TOML("BCE5002", "local.packages.in.dependencies.toml"),
    CORRUPTED_DEPENDENCIES_TOML("BCE5003", "corrupted.dependencies.toml"),
    INCOMPATIBLE_DEPENDENCY_VERSIONS("BCE5004", "incompatible.dependency.versions"),
    PACKAGE_NOT_FOUND("BCE5005", "package.not.found"),
    MISSING_PKG_INFO_IN_BALLERINA_TOML("BCE5006", "missing.package.info"),
    MODULE_NOT_FOUND("BCE5100", "module.not.found"),
    UNSUPPORTED_COMPILER_PLUGIN_TYPE("BCE5200", "unsupported.compiler.plugin.type")
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
