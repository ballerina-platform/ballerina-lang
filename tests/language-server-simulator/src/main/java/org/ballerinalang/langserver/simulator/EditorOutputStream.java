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
package org.ballerinalang.langserver.simulator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A custom output stream to consume messages sent from LS to the LS client side.
 *
 * @since 2.0.0
 */
class EditorOutputStream extends ByteArrayOutputStream {

    private static final Logger logger = LoggerFactory.getLogger(EditorOutputStream.class);

    private Editor editor;

    /**
     * LSP4J invokes this method after writing a message to the stream. At that point, we should have the complete
     * message in the byte array. Here we consume that and reset the array.
     *
     * @throws IOException IO errors
     * @see RemoteEndpoint#request(java.lang.String, java.lang.Object)
     */
    @Override
    public void flush() throws IOException {
        String message = this.toString(Charset.defaultCharset());
        reset();
        try {
            process(message);
        } catch (Throwable t) {
            logger.error("Error processing message", t);
        }
    }

    /**
     * Process a received message. We are interested in log message events and telemetry events to identify errors
     * occurred.
     *
     * @param message JSON RPC message received
     */
    void process(String message) {
        String[] parts = message.replace("\r\n", "\n").split("\n");
        if (parts.length > 1) {
            message = parts[parts.length - 1];
            JsonElement jsonMsg = JsonParser.parseString(message);

            if (jsonMsg.isJsonObject()) {
                JsonObject obj = jsonMsg.getAsJsonObject();
                String method = obj.get("method").getAsString();

                switch (method) {
                    case "telemetry/event":
                        logger.info("Got telemetry event: {}", obj);
                        if (editor != null && editor.activeTab() != null) {
                            logger.info("Current file: {}", editor.activeTab().filePath());
                            logger.info("Current file content: \n{}\n========================",
                                    editor.activeTab().textDocument().toString());
                        }
                        break;
                    case "window/logMessage":
                        logger.info("Received log message event: {}", obj);
                        break;
                    case "textDocument/publishDiagnostics":
                        // pass
                    default:
                        // pass
                }
            }
        }
    }

    public void setEditor(Editor editor) {
        this.editor = editor;
    }
}
