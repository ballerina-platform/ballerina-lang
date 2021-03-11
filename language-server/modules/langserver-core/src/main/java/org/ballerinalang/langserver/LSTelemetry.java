/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSOperation;

import java.util.StringJoiner;

/**
 * Represents a telemetry event for language server.
 *
 * @since 2.0.0
 */
public class LSTelemetry {
    private final String component;
    private final String version;
    private final String message;
    private final String errorMessage;
    private final String errorStackTrace;

    private static final String LS_PACKAGE_NAME = "org.ballerinalang.langserver";
    private static final String LS_TELEMETRY_COMPONENT_NAME = "component.langserver";

    private LSTelemetry(String component, String version, String message, String errorMessage, String errorStackTrace) {
        this.component = component;
        this.version = version;
        this.message = message;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    public static LSTelemetry from(LSOperation operation, String message, Throwable error) {
        return new LSTelemetry(getComponentName(operation), CommonUtil.SDK_VERSION, message,
                               getErrorMessage(error),
                               getStackTrace(error));
    }

    private static String getErrorMessage(Throwable error) {
        String claz = error.getClass().getName();
        String message = error.getMessage();
        return (message != null) ? (claz + ": " + message) : claz;
    }

    private static String getComponentName(LSOperation operation) {
        if (operation == null) {
            return LS_TELEMETRY_COMPONENT_NAME;
        }
        return LS_TELEMETRY_COMPONENT_NAME + "." + operation.getName().replaceAll("/", "_");
    }

    private static String getStackTrace(Throwable error) {
        StringJoiner errorStackTrace = new StringJoiner("\n");
        errorStackTrace.add("Error: " + getErrorMessage(error));
        for (StackTraceElement elm : error.getStackTrace()) {
            errorStackTrace.add("\tat " + elm.toString());
            if (elm.getClassName().startsWith(LS_PACKAGE_NAME)) {
                // Stop logging after capturing first langserver-invocation
                break;
            }
        }
        Throwable cause = error.getCause();
        if (cause != null) {
            errorStackTrace.add("Caused By:").add(getStackTrace(cause));
        }
        return errorStackTrace.toString();
    }

    public String getComponent() {
        return component;
    }

    public String getVersion() {
        return version;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }
}
