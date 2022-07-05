/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.tools.diagnostics;

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

import java.text.MessageFormat;
import java.util.List;

/**
 * An internal implementation of the {@code Diagnostic} class that is used by the {@code DiagnosticFactory}
 * to create diagnostics.
 *
 * @since 2.0.0
 */
class DefaultDiagnostic extends Diagnostic {
    private final DiagnosticInfo diagnosticInfo;
    private final Location location;
    private final List<DiagnosticProperty<?>> properties;
    private final String message;

    DefaultDiagnostic(DiagnosticInfo diagnosticInfo,
                      Location location,
                      List<DiagnosticProperty<?>> properties,
                      Object[] args) {
        this.diagnosticInfo = diagnosticInfo;
        this.location = location;
        this.properties = properties;
        this.message = MessageFormat.format(diagnosticInfo.messageFormat(), args);
    }

    @Override
    public Location location() {
        return location;
    }

    @Override
    public DiagnosticInfo diagnosticInfo() {
        return diagnosticInfo;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return properties;
    }

    @Override
    public String toString() {
        LineRange lineRange = this.location.lineRange();
        String filePath = lineRange.filePath();
        LineRange oneBasedLineRange = LineRange.from(
                filePath,
                LinePosition.from(lineRange.startLine().line() + 1, lineRange.startLine().offset() + 1),
                LinePosition.from(lineRange.endLine().line() + 1, lineRange.endLine().offset() + 1));

        return diagnosticInfo().severity().toString() + " ["
                + filePath + ":" + oneBasedLineRange + "] " + message();
    }
}
