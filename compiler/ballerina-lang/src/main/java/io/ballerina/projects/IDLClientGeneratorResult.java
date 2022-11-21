/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents the package with generated idl client modules and a
 * collection of diagnostics generated during IDL plugin execution.
 *
 * @since 2201.3.0
 */
public class IDLClientGeneratorResult {
    private final Package updatedPkg;
    private final DiagnosticResult diagnostics;

    IDLClientGeneratorResult(Package updatedPkg, Collection<Diagnostic> diagnostics) {
        this.updatedPkg = updatedPkg;
        this.diagnostics = new DefaultDiagnosticResult(diagnostics);
    }

    /**
     * Returns the updated package that contains the generated IDL clients modules.
     * <p>
     * This method does not modify the current package. It returns a new package instance
     * with generated idl client modules.
     *
     * @return the updated package
     */
    public Optional<Package> updatedPackage() {
        return Optional.ofNullable(updatedPkg);
    }

    /**
     * Returns the diagnostics reported during IDL client generation.
     *
     * @return a collected of diagnostics
     */
    public DiagnosticResult reportedDiagnostics() {
        return diagnostics;
    }
}
