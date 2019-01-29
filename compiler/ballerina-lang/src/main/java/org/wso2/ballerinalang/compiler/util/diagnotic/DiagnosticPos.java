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

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition;

/**
 * @since 0.94
 */
public class DiagnosticPos implements DiagnosticPosition {

    public BDiagnosticSource src;
    public int sLine;
    public int eLine;
    public int sCol;
    public int eCol;

    public DiagnosticPos(BDiagnosticSource source,
                         int startLine,
                         int endLine,
                         int startCol,
                         int endCol) {
        this.src = source;
        this.sLine = startLine;
        this.eLine = endLine;
        this.sCol = startCol;
        this.eCol = endCol;
    }

    @Override
    public BDiagnosticSource getSource() {
        return src;
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
    public String toString() {
        boolean cUnitNameAvailable = src.cUnitName != null && !src.cUnitName.isEmpty();
        String strPos = "";

        if (src.pkgID != PackageID.DEFAULT && cUnitNameAvailable) {
            strPos = strPos + src.pkgID + "::" + src.cUnitName + ":";
        } else if (src.pkgID != PackageID.DEFAULT) {
            strPos = strPos + src.pkgID + ":";
        } else {
            strPos = strPos + src.cUnitName + ":";
        }

        return strPos + sLine + ":" + sCol + ":";
    }
}
