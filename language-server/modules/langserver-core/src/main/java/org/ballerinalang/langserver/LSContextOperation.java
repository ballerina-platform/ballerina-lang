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

/**
 * Represents operations in language server.
 *
 * @since 1.0.0
 */
public enum LSContextOperation implements LSOperation {
    TXT_COMPLETION("text/completion"),
    TXT_DID_CHANGE("text/didChange"),
    TXT_DID_CLOSE("text/didClose"),
    DIAGNOSTICS("debouncer/diagnostics"),
    TXT_DID_OPEN("text/didOpen"),
    TXT_HOVER("text/hover"),
    TXT_SIGNATURE("text/signature"),
    TXT_DEFINITION("text/definition"),
    TXT_REFERENCES("text/references"),
    TXT_DOC_SYMBOL("text/documentSymbol"),
    TXT_CODE_ACTION("text/codeAction"),
    TXT_FORMATTING("text/formatting"),
    TXT_RANGE_FORMATTING("text/rangeFormatting"),
    TXT_CODE_LENS("text/codeLens"),
    TXT_PREPARE_RENAME("text/prepareRename"),
    TXT_RENAME("text/rename"),
    TXT_IMPL("text/implementation"),
    TXT_FOLDING_RANGE("text/foldingRange"),
    TXT_SEMANTIC_TOKENS_FULL("text/semanticTokens/full"),
    WS_SYMBOL("workspace/symbol"),
    WS_EXEC_CMD("workspace/executeCommand"),
    WS_WF_CHANGED("workspace/didChangeWatchedFiles"),
    DOC_SERVICE_AST("ballerinaDocument/ast"),
    DOC_DIAGNOSTICS("ballerinaDocument/diagnostics"),
    LS_INIT("langserver/init"),
    SOURCE_PRUNER("sourcePruner"),
    TEST_GEN("testGeneration"),
    CREATE_PROJECT("createProject"),
    RELOAD_PROJECT("reloadProject");

    private final String name;

    LSContextOperation(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
