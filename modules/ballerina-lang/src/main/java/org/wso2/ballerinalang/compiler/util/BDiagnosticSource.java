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

import org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticSource;

/**
 * @since 0.94
 */
public class BDiagnosticSource implements DiagnosticSource {

    public String pkgName;
    public String pkgVersion;
    public String cUnitName;

    public BDiagnosticSource(String packageName, String packageVersion, String compUnitName) {
        this.pkgName = packageName;
        this.pkgVersion = packageVersion;
        this.cUnitName = compUnitName;
    }

    @Override
    public String getPackageName() {
        return pkgName;
    }

    @Override
    public String getPackageVersion() {
        return pkgVersion;
    }

    @Override
    public String getCompilationUnitName() {
        return cUnitName;
    }
}
