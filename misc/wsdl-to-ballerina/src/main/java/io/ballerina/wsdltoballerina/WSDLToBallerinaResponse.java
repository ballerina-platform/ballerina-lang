/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.wsdltoballerina;

import io.ballerina.wsdltoballerina.diagnostic.WSDLToBallerinaDiagnostic;

import java.util.ArrayList;
import java.util.List;

public class WSDLToBallerinaResponse {
    private GeneratedSourceFile clientSource;
    private GeneratedSourceFile typesSource;
    private List<WSDLToBallerinaDiagnostic> diagnostics = new ArrayList<>();

    public GeneratedSourceFile getClientSource() {
        return clientSource;
    }

    public void setClientSource(GeneratedSourceFile clientSource) {
        this.clientSource = clientSource;
    }

    public GeneratedSourceFile getTypesSource() {
        return typesSource;
    }

    public void setTypesSource(GeneratedSourceFile typesSource) {
        this.typesSource = typesSource;
    }

    public List<WSDLToBallerinaDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<WSDLToBallerinaDiagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }
}
