/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.common.constants;

/**
 * Constants related to {@link org.eclipse.lsp4j.Command}.
 * @since v0.964.0
 */
public class CommandConstants {
    public static final String UNDEFINED_PACKAGE = "undefined package";


    // Command Arguments
    public static final String ARG_KEY_DOC_URI = "doc.uri";

    public static final String ARG_KEY_PKG_NAME = "package";

    public static final String ARG_KEY_NODE_TYPE = "node.type";

    public static final String ARG_KEY_NODE_LINE = "node.line";


    // Command Titles
    public static final String IMPORT_PKG_TITLE = "Import Package ";

    public static final String ADD_DOCUMENTATION_TITLE = "Document This";

    public static final String ADD_ALL_DOC_TITLE = "Document All";


    // Commands List
    public static final String CMD_IMPORT_PACKAGE = "IMPORT_PKG";

    public static final String CMD_ADD_DOCUMENTATION = "ADD_DOC";

    public static final String CMD_ADD_ALL_DOC = "ADD_ALL_DOC";
}
