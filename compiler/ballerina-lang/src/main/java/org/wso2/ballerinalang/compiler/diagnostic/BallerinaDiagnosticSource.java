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

import org.ballerinalang.model.elements.PackageID;

/**
 * {@code BallerinaDiagnosticSource} represents the source file in a diagnostic.
 *
 * @since 2.0
 */
public class BallerinaDiagnosticSource {

    public PackageID pkgID;
    public String cUnitName;

    public BallerinaDiagnosticSource(PackageID packageID, String compUnitName) {
        this.pkgID = packageID;
        this.cUnitName = compUnitName;
    }

    public String getPackageName() {
        return pkgID.name.value;
    }

    public String getPackageVersion() {
        return pkgID.version.value;
    }

    public String getCompilationUnitName() {
        return cUnitName;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BallerinaDiagnosticSource)) {
            return false;
        }
        BallerinaDiagnosticSource diagnosticSource = (BallerinaDiagnosticSource) obj;
        return pkgID.equals(diagnosticSource.pkgID) && cUnitName.equals(diagnosticSource.cUnitName);
    }

    public int hashCode() {
        return pkgID.hashCode() + cUnitName.hashCode();
    }
}
