/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.document.visitor;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;

/**
 * The section that need to be deleted.
 */
public class DeleteRange implements Diagnostic.DiagnosticPosition {

    public int sLine;
    public int eLine;
    public int sCol;
    public int eCol;

    public DeleteRange(int startLine,
                       int endLine,
                       int startCol,
                       int endCol) {
        this.sLine = startLine;
        this.eLine = endLine;
        this.sCol = startCol;
        this.eCol = endCol;
    }

    @Override
    public BDiagnosticSource getSource() {
        return null;
    }

    @Override
    public int getStartLine() {
        return sLine;
    }

    @Override
    public int getEndLine() {
        return eLine;
    }

    @Override
    public int getStartColumn() {
        return sCol;
    }

    @Override
    public int getEndColumn() {
        return eCol;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeleteRange)) {
            return false;
        }
        DeleteRange deleteRange = (DeleteRange) obj;
        return (sLine == deleteRange.sLine && eLine == deleteRange.eLine &&
                sCol == deleteRange.sCol && eCol == deleteRange.eCol);
    }

    @Override
    public int hashCode() {
        return sLine * 31 + eLine * 31 * 31 + sCol * 7 + eCol * 7;
    }

    @Override
    public int compareTo(Diagnostic.DiagnosticPosition diagnosticPosition) {
        // Compare the source first.
        int value = this.getSource().compareTo(diagnosticPosition.getSource());
        if (value != 0) {
            return value;
        }

        // If the sources are same, then compare the start line.
        if (sLine < diagnosticPosition.getStartLine()) {
            return -1;
        } else if (sLine > diagnosticPosition.getStartLine()) {
            return 1;
        }

        // If the start line is the same, then compare the start column.
        if (sCol < diagnosticPosition.getStartColumn()) {
            return -1;
        } else if (sCol > diagnosticPosition.getStartColumn()) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return sLine + ":" + sCol + "=>" + eLine + ":" + eCol;
    }
}
