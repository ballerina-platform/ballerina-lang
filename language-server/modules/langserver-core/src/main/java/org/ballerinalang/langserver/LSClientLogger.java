/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.config.LSClientConfig;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.ballerinalang.langserver.telemetry.LSErrorTelemetryEvent;
import org.ballerinalang.langserver.telemetry.LSTelemetryEvent;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Use this class to send notifications and log messages to the client.
 *
 * @since 1.0.0
 */
public class LSClientLogger {
    private LanguageClient languageClient;
    private boolean isInitializedOnce;
    private LSClientConfigHolder configHolder;
    private static final LanguageServerContext.Key<LSClientLogger> CLIENT_LOGGER_KEY =
            new LanguageServerContext.Key<>();

    public static LSClientLogger getInstance(LanguageServerContext serverContext) {
        LSClientLogger lsClientLogger = serverContext.get(CLIENT_LOGGER_KEY);
        if (lsClientLogger == null) {
            lsClientLogger = new LSClientLogger(serverContext);
        }

        return lsClientLogger;
    }

    private LSClientLogger(LanguageServerContext serverContext) {
        serverContext.put(CLIENT_LOGGER_KEY, this);
    }

    /**
     * Initializes the client logger.
     *
     * @param languageClient {@link LanguageClient}
     */
    public void initialize(LanguageClient languageClient, LanguageServerContext serverContext) {
        this.languageClient = languageClient;
        this.isInitializedOnce = true;
        this.configHolder = LSClientConfigHolder.getInstance(serverContext);
    }

    public void notifyUser(String operation, Throwable error) {
        if (!this.isInitializedOnce) {
            return;
        }
        if (languageClient != null) {
            languageClient.showMessage(
                    new MessageParams(MessageType.Error, operation + " failed, " + error.getMessage()));
        }
    }

    /**
     * Logs the error message through the LSP protocol.
     *
     * @param message    log message
     * @param error      {@link Throwable}
     * @param identifier text document
     * @param pos        pos
     */
    public void logError(LSOperation operation, String message, Throwable error, TextDocumentIdentifier identifier,
                         Position... pos) {
        if (!this.isInitializedOnce || this.languageClient == null) {
            return;
        }
        LSClientConfig config = this.configHolder.getConfig();
        if (config.isEnableTelemetry()) {
            this.languageClient.telemetryEvent(LSErrorTelemetryEvent.from(operation, message, error));
        }
        String details = getErrorDetails(identifier, error, pos);
        if (config.isDebugLogEnabled()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name());
                error.printStackTrace(ps);
            } catch (UnsupportedEncodingException e1) {
                //ignore
            }
            this.languageClient.logMessage(
                    new MessageParams(MessageType.Error, message + " " + details + "\n" + baos));
        }
    }

    /**
     * Logs the trace message through the LSP protocol.
     *
     * @param message log message
     */
    public void logTrace(String message) {
        if (!this.isInitializedOnce) {
            return;
        }
        if (this.configHolder.getConfig().isTraceLogEnabled() && this.languageClient != null) {
            this.languageClient.logMessage(new MessageParams(MessageType.Info, message));
        }
    }

    /**
     * Logs a warning log through LSP protocol. Logs only when trace logs are enabled.
     *
     * @param message log message
     */
    public void logWarning(String message) {
        if (!this.isInitializedOnce) {
            return;
        }
        if (this.configHolder.getConfig().isTraceLogEnabled() && this.languageClient != null) {
            this.languageClient.logMessage(new MessageParams(MessageType.Warning, message));
        }
    }
    
    /**
     * Logs an info message through the LSP protocol.
     *
     * @param message log message
     */
    public void logMessage(String message) {
        if (!this.isInitializedOnce) {
            return;
        }
        if (this.languageClient != null) {
            this.languageClient.logMessage(new MessageParams(MessageType.Log, message));
        }
    }

    /**
     * Sends a telemetry event to the client. Though this is doesn't do any logging directly, sending telemetry events
     * via the client is related to this context.
     *
     * @param event Telemetry event
     */
    public void telemetryEvent(LSTelemetryEvent event) {
        if (!this.isInitializedOnce || this.languageClient == null) {
            return;
        }

        if (this.configHolder.getConfig().isEnableTelemetry()) {
            this.languageClient.telemetryEvent(event);
        }
    }

    private static String getErrorDetails(TextDocumentIdentifier identifier, Throwable error, Position... position) {
        String msg = error.getMessage();
        StringBuilder result = new StringBuilder("{");
        if (identifier != null) {
            result.append("uri: '").append(identifier.getUri().replaceFirst("file://", "")).append("'");
        }
        if (position != null && position.length > 0 && position[0] != null) {
            if (position.length == 2) {
                // Range
                result.append(", [").append(position[0].getLine() + 1)
                        .append(":").append(position[0].getCharacter() + 1).append("]");
                result.append("- [").append(position[1].getLine() + 1)
                        .append(":").append(position[1].getCharacter() + 1).append("]");
            } else {
                // Position
                result.append(", [").append(position[0].getLine() + 1)
                        .append(":").append(position[0].getCharacter() + 1).append("]");
            }
        }
        if (msg != null && !msg.isEmpty()) {
            result.append(", error: '").append(msg);
        } else {
            result.append(", error: '").append(error.toString());
            for (StackTraceElement elm : error.getStackTrace()) {
                if (elm.getClassName().startsWith("org.wso2.")) {
                    result.append(", ").append(elm.toString());
                    break;
                }
            }
        }
        result.append("'}");
        return result.toString();
    }
}
