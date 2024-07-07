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

package io.ballerina.wsdltoballerina.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Objects;

public class DiagnosticMessage {

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;
    private final Object[] args;

    private DiagnosticMessage(String code, String description, DiagnosticSeverity severity, Object[] args) {
        this.code = code;
        this.description = description;
        this.severity = severity;
        this.args = args;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public DiagnosticSeverity getSeverity() {
        return this.severity;
    }

    public Object[] getArgs() {
        return Objects.requireNonNullElse(this.args, new Object[0]).clone();
    }

    public static DiagnosticMessage wsdlToBallerinaErr100(Object[] args) {
        return new DiagnosticMessage("WSDL_TO_BALLERINA_100",
                "Invalid WSDL. Provided WSDL is invalid.", DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessage wsdlToBallerinaErr101(Object[] args) {
        return new DiagnosticMessage("WSDL_TO_BALLERINA_101",
                "Error occurred while formatting the Ballerina syntax tree.", DiagnosticSeverity.ERROR, args);
    }
}
