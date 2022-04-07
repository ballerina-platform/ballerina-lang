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

package io.ballerina.projects.internal;

import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

/**
 * Represents a diagnostic in the package resolution.
 *
 * @since 2201.0.2
 */
public class PackageResolutionDiagnostic extends PackageDiagnostic {

    public PackageResolutionDiagnostic(DiagnosticInfo diagnosticInfo, Location location) {
        super(diagnosticInfo, location);
    }

    public PackageResolutionDiagnostic(DiagnosticInfo diagnosticInfo, String filePath) {
        super(diagnosticInfo, new NullLocation(filePath));
    }

    @Override
    public String toString() {
        String filePath = this.diagnostic.location().lineRange().filePath();
        if (this.diagnostic.location().lineRange().startLine().line() == 0 &&
                this.diagnostic.location().lineRange().startLine().offset() == 0) {
            return diagnosticInfo().severity().toString() + " ["
                    + filePath + "] " + message();
        }
        LineRange lineRange = diagnostic.location().lineRange();
        LineRange oneBasedLineRange = LineRange.from(
                filePath,
                LinePosition.from(lineRange.startLine().line() + 1, lineRange.startLine().offset() + 1),
                LinePosition.from(lineRange.endLine().line() + 1, lineRange.endLine().offset() + 1));

        return diagnosticInfo().severity().toString() + " ["
                + filePath + ":" + oneBasedLineRange + "] " + message();
    }

    private static class NullLocation implements Location {
        private final String filepath;

        NullLocation(String filePath) {
            this.filepath = filePath;
        }

        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from(filepath, from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }

}
