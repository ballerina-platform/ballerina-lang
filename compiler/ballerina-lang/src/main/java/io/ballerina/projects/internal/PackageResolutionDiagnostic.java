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

import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

/**
 * Decorator for diagnostics exposed in the package resolution phase.
 *
 * @since 2.0.0
 */
public class PackageResolutionDiagnostic extends PackageDiagnostic {

    public PackageResolutionDiagnostic(Diagnostic diagnostic, ModuleDescriptor moduleDescriptor, Project project) {
        super(diagnostic, moduleDescriptor, project);
    }

    @Override
    public String toString() {
        String filePath = null;
        if (this.diagnostic.location() != null) {
            filePath = this.diagnostic.location().lineRange().filePath();
        }

        // add package info if it is a dependency
        if (this.project.kind().equals(ProjectKind.BALA_PROJECT)) {
            filePath = moduleDescriptor.org() + "/" + moduleDescriptor.name().toString() + "/"
                    + moduleDescriptor.version() + "::" + filePath;
        }

        if (this.diagnostic.location() != null) {
            var lineRange = diagnostic.location().lineRange();
            var oneBasedLineRange = LineRange.from(
                    filePath,
                    LinePosition.from(lineRange.startLine().line() + 1, lineRange.startLine().offset() + 1),
                    LinePosition.from(lineRange.endLine().line() + 1, lineRange.endLine().offset() + 1));

            return diagnosticInfo().severity().toString() + " [" + filePath + ":" + oneBasedLineRange + "] "
                    + message();
        } else if (filePath != null) {
            return diagnosticInfo().severity().toString() + " [" + filePath + "] " + message();
        } else {
            return diagnosticInfo().severity().toString() + " " + message();
        }
    }
}
