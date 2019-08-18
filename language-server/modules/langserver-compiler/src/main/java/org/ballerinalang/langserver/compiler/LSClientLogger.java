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
package org.ballerinalang.langserver.compiler;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Use this class to send notifications and log messages to the client.
 *
 * @since 1.0.0
 */
public class LSClientLogger {
    private static LanguageClient languageClient = null;
    private static boolean debugEnabled = false;
    private static boolean isInitialized = false;
    private static boolean traceEnabled = false;

    /**
     * Initializes the client logger.
     *
     * @param languageClient {@link LanguageClient}
     * @param lsDebugEnabled LS Debug Enabled
     * @param lsTraceEnabled LS Trace Enabled
     */
    public static void init(LanguageClient languageClient, boolean lsDebugEnabled, boolean lsTraceEnabled) {
        LSClientLogger.debugEnabled = lsDebugEnabled;
        LSClientLogger.traceEnabled = lsTraceEnabled;
        LSClientLogger.languageClient = languageClient;
        LSClientLogger.isInitialized = true;
    }

    /**
     * Notify user an error message through LSP protocol.
     *
     * @param operation operation name
     * @param error     {@link Throwable}
     */
    public static void notifyUser(String operation, Throwable error) {
        if (!LSClientLogger.isInitialized) {
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
     * @param message      log message
     * @param error        {@link Throwable}
     * @param textDocument text document
     * @param position     position
     */
    public static void logError(String message, Throwable error, TextDocumentIdentifier textDocument,
                                Position... position) {
        if (!LSClientLogger.isInitialized) {
            return;
        }
        String details = getErrorDetails(textDocument, error, position);
        if (LSClientLogger.debugEnabled && LSClientLogger.languageClient != null) {
            final Charset charset = StandardCharsets.UTF_8;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                PrintStream ps = new PrintStream(baos, true, charset.name());
                error.printStackTrace(ps);
            } catch (UnsupportedEncodingException e1) {
                //ignore
            }
            LSClientLogger.languageClient.logMessage(
                    new MessageParams(MessageType.Error, message + " " + details + "\n" + baos));
        }
    }

    /**
     * Logs the trace message through the LSP protocol.
     *
     * @param message      log message
     */
    public static void logTrace(String message) {
        if (!LSClientLogger.isInitialized) {
            return;
        }
        if (LSClientLogger.traceEnabled && LSClientLogger.languageClient != null) {
            LSClientLogger.languageClient.logMessage(
                    new MessageParams(MessageType.Info, message));
        }
    }

    /**
     * Returns True if debug enabled.
     *
     * @return  True if debug enabled.
     */
    public static boolean isDebugEnabled() {
        return LSClientLogger.debugEnabled;
    }

    /**
     * Returns True if trace enabled.
     *
     * @return  True if trace enabled.
     */
    public static boolean isTraceEnabled() {
        return LSClientLogger.traceEnabled;
    }

    private static String getErrorDetails(TextDocumentIdentifier textDocument, Throwable error, Position... position) {
        String msg = error.getMessage();
        StringBuilder result = new StringBuilder("{");
        if (textDocument != null) {
            result.append("uri: ").append(textDocument.getUri().replaceFirst("file://", ""));
        }
        if (position != null && position[0] != null) {
            if (position.length == 2) {
                // Range
                result.append(", line: ").append(position[0].getLine() + 1)
                        .append(", col:").append(position[0].getCharacter() + 1);
                result.append("- line: ").append(position[1].getLine() + 1)
                        .append(", col:").append(position[1].getCharacter() + 1);
            } else {
                // Position
                result.append(", line: ").append(position[0].getLine() + 1)
                        .append(", col:").append(position[0].getCharacter() + 1);
            }
        }
        if (msg != null && !msg.isEmpty()) {
            result.append(", error: ").append(msg);
        } else {
            result.append(", error: ").append(error.toString());
            for (StackTraceElement elm : error.getStackTrace()) {
                if (elm.getClassName().startsWith("org.wso2.")) {
                    result.append(", ").append(elm.toString());
                    break;
                }
            }
        }
        result.append("}");
        return result.toString();
    }
}
