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
package io.ballerina.toml.internal.diagnostics;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Contains utility methods to generate diagnostic messages.
 *
 * @since 2.0.0
 */
public class DiagnosticMessageHelper {
    private static ResourceBundle messages = ResourceBundle.getBundle(
            "syntax_diagnostic_message", Locale.getDefault());

    private DiagnosticMessageHelper() {
    }

    public static String getDiagnosticMessage(DiagnosticCode diagnosticCode, Object... args) {
        String msgKey = diagnosticCode.messageKey();
        String msg = messages.getString(msgKey);
        return MessageFormat.format(msg, args);
    }
}
