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
package sementic.nodes;

/**
 * {@code BDiagnosticSource} represents the source file in a diagnostic.
 *
 * @since 0.94
 */
public class DiagnosticSource implements Diagnostic.DiagnosticSource {

    public String cUnitName;

    public DiagnosticSource(String compUnitName) {
        this.cUnitName = compUnitName;
    }

    @Override
    public String getCompilationUnitName() {
        return cUnitName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DiagnosticSource)) {
            return false;
        }
        DiagnosticSource diagnosticSource = (DiagnosticSource) obj;
        return cUnitName.equals(diagnosticSource.cUnitName);
    }

    @Override
    public int hashCode() {
        return cUnitName.hashCode();
    }

    @Override
    public int compareTo(Diagnostic.DiagnosticSource diagnosticSource) {
        String thisDiagnosticSourceString =  getCompilationUnitName();
        String otherDiagnosticSourceString = diagnosticSource.getCompilationUnitName();
        return thisDiagnosticSourceString.compareTo(otherDiagnosticSourceString);
    }
}
