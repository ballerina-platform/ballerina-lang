/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.langserver.consts;


/**
 * Application launcher constants.
 */
public class LangServerConstants {
    /**
     * The minimum server port number. Lets start with 5006 which is the default port number
     */
    public static final int MIN_PORT_NUMBER = 5006;
    
    /**
     * The maximum server currentMinPort number.
     */
    public static final int MAX_PORT_NUMBER = 5999;
    
    public static final String B_LANGSERVER_WEBSOCKET_PATH = "/blangserver";
    
    public static final String B_LANGSERVER_PORT = "9093";

    /**
     * Error Codes
     */
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    public static final int SERVER_ERROR_START = -32099;
    public static final int SERVER_ERROR_END = -32000;
    public static final int SERVER_NOT_INITIALIZED = -32002;
    public static final int UNKNOWN_ERROR_CODE = -32001;
    public static final int REQUEST_CANCELLED = -32800;

    /**
     * Error messages
     */
    public static final String PARSE_ERROR_LINE = "Parse error";
    public static final String INVALID_REQUEST_LINE = "Invalid Request";
    public static final String METHOD_NOT_FOUND_LINE = "Method not found";
    public static final String INVALID_PARAMS_LINE = "Invalid params";
    public static final String INTERNAL_ERROR_LINE = "Internal error";
    public static final String SERVER_NOT_INITIALIZED_LINE = "Server not initialized";

    /**
     * Message methods
     */
    public static final String INITIALIZE = "initialize";
    public static final String SHUTDOWN = "shutdown";
    public static final String EXIT = "exit";
    public static final String TEXT_DOCUMENT_DID_OPEN = "textDocument/didOpen";
    public static final String TEXT_DOCUMENT_DID_CLOSE = "textDocument/didClose";
    public static final String TEXT_DOCUMENT_DID_SAVE = "textDocument/didSave";
    public static final String TEXT_DOCUMENT_DOCUMENT_SYMBOL = "textDocument/documentSymbol";

    public static final String WORKSPACE_SYMBOL = "workspace/symbol";

    /**
     * Workspace symbol query string
     */
    public static final String BUILTIN_TYPES = "builtinTypes";
    public static final String PACKAGES = "packages";

    /**
     * Types of programs that are executable.
     */
    public static enum ProgramType {
        RUN, SERVICE
    }
}
