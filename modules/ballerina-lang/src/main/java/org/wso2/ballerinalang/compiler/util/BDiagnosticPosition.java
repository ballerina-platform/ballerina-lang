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
package org.wso2.ballerinalang.compiler.util;

import org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition;

/**
 * @since 0.94
 */
public class BDiagnosticPosition implements DiagnosticPosition {

    public BDiagnosticSource source;
    public int sLine;
    public int eLine;
    public int sCol;
    public int eCol;

    public BDiagnosticPosition(BDiagnosticSource source,
                               int startLine,
                               int endLine,
                               int startCol,
                               int endCol) {
        this.source = source;
        this.sLine = startLine;
        this.eLine = endLine;
        this.sCol = startCol;
        this.eCol = endCol;
    }

    @Override
    public BDiagnosticSource getSource() {
        return source;
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
    public int startColumn() {
        return sCol;
    }

    @Override
    public int endColumn() {
        return eCol;
    }
}
