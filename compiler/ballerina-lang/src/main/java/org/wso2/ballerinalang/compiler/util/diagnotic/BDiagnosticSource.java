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
import org.ballerinalang.util.diagnostic.Diagnostic;

/**
 * {@code BDiagnosticSource} represents the source file in a diagnostic.
 *
 * @since 0.94
 */
public class BDiagnosticSource implements Diagnostic.DiagnosticSource {

    public PackageID pkgID;
    public String cUnitName;

    public BDiagnosticSource(PackageID packageID, String compUnitName) {
        this.pkgID = packageID;
        this.cUnitName = compUnitName;
    }

    @Override
    public String getPackageName() {
        return pkgID.name.value;
    }

    @Override
    public String getPackageVersion() {
        return pkgID.version.value;
    }

    @Override
    public String getCompilationUnitName() {
        return cUnitName;
    }
}
