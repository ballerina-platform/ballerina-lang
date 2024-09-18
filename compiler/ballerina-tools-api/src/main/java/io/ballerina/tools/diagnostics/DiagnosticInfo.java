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

import java.util.Arrays;

/**
 * Represents an abstract shape of a {@link Diagnostic} that is independent of
 * the location and message arguments.
 *
 * @since 2.0.0
 */
public class DiagnosticInfo {

    private final String code;
    private final String messageFormat;
    private final DiagnosticSeverity severity;

    /**
     * Constructs an abstract shape of a {@link Diagnostic}.
     *
     * @param code          a code that can be used to uniquely identify a diagnostic category
     * @param messageFormat a pattern that can be formatted with the {@link java.text.MessageFormat} utility
     * @param severity      the severity of the diagnostic
     */
    public DiagnosticInfo(String code,
                          String messageFormat,
                          DiagnosticSeverity severity) {
        this.code = code;
        this.messageFormat = messageFormat;
        this.severity = severity;
    }

    public String code() {
        return code;
    }

    public String messageFormat() {
        return messageFormat;
    }

    public DiagnosticSeverity severity() {
        return severity;
    }

    @Override
    public int hashCode() {
        if (this.code == null) {
            return Arrays.hashCode(new int[]{messageFormat.hashCode(), severity.hashCode()});
        }
        return Arrays.hashCode(new int[]{code.hashCode(), messageFormat.hashCode(), severity.hashCode()});
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiagnosticInfo that) {
            if (this.code != null) {
                return this.code.equals(that.code) && this.messageFormat.equals(that.messageFormat)
                        && this.severity.equals(that.severity);
            } else if (that.code != null) {
                return false;
            }
            return this.messageFormat.equals(that.messageFormat) && this.severity.equals(that.severity);
        }
        return false;
    }
}
