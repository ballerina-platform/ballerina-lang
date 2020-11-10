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
package io.ballerina.tools.diagnostics;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a abstract shape of a {@code Diagnostic} that is independent of
 * the location and message arguments.
 *
 * @since 2.0.0
 */
public class DiagnosticInfo {

    private final String code;
    private final String messageTemplate;
    private final DiagnosticSeverity severity;
    private final Collection<DiagnosticRelatedInformation> relatedInformation;

    public DiagnosticInfo(String code,
                          String messageTemplate,
                          DiagnosticSeverity severity) {
        this(code, messageTemplate, severity, Collections.emptyList());
    }

    public DiagnosticInfo(String code,
                          String messageTemplate,
                          DiagnosticSeverity severity,
                          Collection<DiagnosticRelatedInformation> relatedInformation) {
        this.code = code;
        this.messageTemplate = messageTemplate;
        this.severity = severity;
        this.relatedInformation = relatedInformation;
    }

    public String code() {
        return code;
    }

    public String messageTemplate() {
        return messageTemplate;
    }

    public DiagnosticSeverity severity() {
        return severity;
    }

    public Collection<DiagnosticRelatedInformation> relatedInformation() {
        return relatedInformation;
    }
}
