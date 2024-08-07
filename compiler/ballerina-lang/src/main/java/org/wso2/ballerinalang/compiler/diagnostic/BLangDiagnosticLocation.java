/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.diagnostic;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.util.Objects;

/**
 * Represent the location of a diagnostic in a {@code TextDocument}.
 * <p>
 * It is a combination of source file path, start and end line numbers, and start and end column numbers.
 *
 * @since 2.0.0
 */
public class BLangDiagnosticLocation implements Location {

    private String filePath;
    private int startLine, endLine, startColumn, endColumn, startOffset, length;

    @Deprecated
    public BLangDiagnosticLocation(String filePath, int startLine, int endLine, int startColumn, int endColumn) {
        this(filePath, startLine, endLine, startColumn, endColumn, 0, 0);
    }

    public BLangDiagnosticLocation(String filePath, int startLine, int endLine, int startColumn, int endColumn,
                                   int startOffset, int length) {
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.startOffset = startOffset;
        this.length = length;
    }

    @Override
    public LineRange lineRange() {
        return LineRange.from(filePath, LinePosition.from(startLine, startColumn),
                LinePosition.from(endLine, endColumn));
    }

    @Override
    public TextRange textRange() {
        return TextRange.from(startOffset, length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BLangDiagnosticLocation location) {
            return lineRange().equals(location.lineRange()) && textRange().equals(location.textRange());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineRange(), textRange());
    }

    @Override
    public String toString() {
        // Desugar of lineRange().toString() + textRange().toString();
        int endOffset = startOffset + length;
        return "(" + startLine + "," + endLine + ")" + "(" + startOffset + "," + endOffset + ")";
    }
}
