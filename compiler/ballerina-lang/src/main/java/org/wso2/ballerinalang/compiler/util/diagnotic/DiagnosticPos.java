/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.util.diagnotic;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.model.elements.PackageID;

/**
 * {code DiagnosticPos} represents a specific position in a source file.
 *
 * Source position is a combination of the source file, start and end line numbers,
 * and start and end column numbers.
 *
 * @since 0.94
 */
@Deprecated
public class DiagnosticPos implements Location {

    private LineRange lineRange;
    private TextRange textRange;
    private PackageID packageID;

    public DiagnosticPos(String filePath, PackageID pkgId, int startLine, int endLine,
                         int startColumn, int endColumn) {
        this.packageID = pkgId;
        this.lineRange = LineRange.from(filePath, LinePosition.from(startLine, startColumn),
                LinePosition.from(endLine, endColumn));
        this.textRange = TextRange.from(0, 0);
    }

    @Override
    public LineRange lineRange() {
        return lineRange;
    }

    @Override
    public TextRange textRange() {
        return textRange;
    }

    public PackageID getPackageID() {
        return packageID;
    }

    public int getStartLine() {
        return lineRange.startLine().line();
    }

    public int getEndLine() {
        return lineRange.endLine().line();
    }

    public int getStartColumn() {
        return lineRange.startLine().offset();
    }

    public int getEndColumn() {
        return lineRange.endLine().offset();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DiagnosticPos diagnosticPos)) {
            return false;
        }
        return packageID.equals(diagnosticPos.getPackageID()) &&
                lineRange().fileName().equals(diagnosticPos.lineRange().fileName()) &&
                (getStartLine() == diagnosticPos.getStartLine() && getEndLine() == diagnosticPos.getEndLine() &&
                getStartColumn() == diagnosticPos.getStartColumn() && getEndColumn() == diagnosticPos.getEndColumn());
    }

    @Override
    public int hashCode() {
        return  packageID.hashCode() + lineRange().fileName().hashCode() +
                getStartLine() + getEndLine() + getStartColumn() + getEndColumn();
    }
    
    public int compareTo(DiagnosticPos diagnosticPosition) {

        // Compare the source first.
        String thisDiagnosticString = packageID.name.value + packageID.version.value + lineRange().fileName();
        String otherDiagnosticString = diagnosticPosition.getPackageID().name.value +
                diagnosticPosition.getPackageID().version.value +
                diagnosticPosition.lineRange().fileName();
        int value = thisDiagnosticString.compareTo(otherDiagnosticString);

        if (value != 0) {
            return value;
        }

        // If the sources are same, then compare the start line.
        if (getStartLine() < diagnosticPosition.getStartLine()) {
            return -1;
        } else if (getStartLine() > diagnosticPosition.getStartLine()) {
            return 1;
        }

        // If the start line is the same, then compare the start column.
        if (getStartColumn() < diagnosticPosition.getStartColumn()) {
            return -1;
        } else if (getStartColumn() > diagnosticPosition.getStartColumn()) {
            return 1;
        }

        return 0;
    }

    @Override
    public String toString() {
        return lineRange.toString() + textRange.toString();
    }
}
