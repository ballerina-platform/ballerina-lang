/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.SnippetGenerator;

/**
 * Snippet for the Ballerina language constructs.
 */
public enum Snippet {

    // Definition Snippets
    DEF_ANNOTATION(SnippetGenerator.getAnnotationDefSnippet()),

    DEF_FUNCTION(SnippetGenerator.getFunctionDefSnippet()),

    DEF_FUNCTION_SIGNATURE(SnippetGenerator.getFunctionSignatureSnippet()),

    DEF_MAIN_FUNCTION(SnippetGenerator.getMainFunctionSnippet()),

    DEF_OBJECT_SNIPPET(SnippetGenerator.getObjectDefinitionSnippet()),

    DEF_RECORD(SnippetGenerator.getRecordDefinitionSnippet()),

    DEF_CLOSED_RECORD(SnippetGenerator.getClosedRecordDefinitionSnippet()),

    DEF_RESOURCE_HTTP(SnippetGenerator.getResourceDefinitionSnippet()),

    DEF_RESOURCE_COMMON(SnippetGenerator.getCommonResourceDefinitionSnippet()),

    DEF_RESOURCE_GRPC(SnippetGenerator.getGRPCResourceDefinitionSnippet()),

    DEF_RESOURCE_WS_OPEN(SnippetGenerator.getWebSocketResourceOnOpenSnippet()),

    DEF_RESOURCE_WS_TEXT(SnippetGenerator.getWebSocketResourceOnTextSnippet()),

    DEF_RESOURCE_WS_BINARY(SnippetGenerator.getWebSocketResourceOnBinarySnippet()),

    DEF_RESOURCE_WS_PING(SnippetGenerator.getWebSocketResourceOnPingSnippet()),

    DEF_RESOURCE_WS_PONG(SnippetGenerator.getWebSocketResourceOnPongSnippet()),

    DEF_RESOURCE_WS_IDLE(SnippetGenerator.getWebSocketResourceOnIdleTimeoutSnippet()),

    DEF_RESOURCE_WS_ERROR(SnippetGenerator.getWebSocketResourceOnErrorSnippet()),

    DEF_RESOURCE_WS_CLOSE(SnippetGenerator.getWebSocketResourceOnCloseSnippet()),

    DEF_RESOURCE_WS_CS_TEXT(SnippetGenerator.getWebSocketClientServiceResourceOnTextSnippet()),

    DEF_RESOURCE_WS_CS_BINARY(SnippetGenerator.getWebSocketClientServiceResourceOnBinarySnippet()),

    DEF_RESOURCE_WS_CS_PING(SnippetGenerator.getWebSocketClientServiceResourceOnPingSnippet()),

    DEF_RESOURCE_WS_CS_PONG(SnippetGenerator.getWebSocketClientServiceResourceOnPongSnippet()),

    DEF_RESOURCE_WS_CS_IDLE(SnippetGenerator.getWebSocketClientServiceResourceOnIdleTimeoutSnippet()),

    DEF_RESOURCE_WS_CS_ERROR(SnippetGenerator.getWebSocketClientServiceResourceOnErrorSnippet()),

    DEF_RESOURCE_WS_CS_CLOSE(SnippetGenerator.getWebSocketClientServiceResourceOnCloseSnippet()),

    DEF_RESOURCE_WEBSUB_INTENT(SnippetGenerator.getWebSubResourceOnIntentVerificationSnippet()),

    DEF_RESOURCE_WEBSUB_NOTIFY(SnippetGenerator.getWebSubResourceOnNotificationSnippet()),

    DEF_SERVICE(SnippetGenerator.getServiceDefSnippet()),

    DEF_SERVICE_VAR(SnippetGenerator.getServiceVarSnippet()),

    DEF_SERVICE_WEBSOCKET(SnippetGenerator.getWebSocketServiceDefSnippet()),

    DEF_SERVICE_WS_CLIENT(SnippetGenerator.getWebSocketClientServiceDefSnippet()),

    DEF_SERVICE_WEBSUB(SnippetGenerator.getWebSubServiceDefSnippet()),

    DEF_SERVICE_GRPC(SnippetGenerator.getGRPCServiceDefSnippet()),

    DEF_WORKER(SnippetGenerator.getWorkerDeclarationSnippet()),

    DEF_ERROR(SnippetGenerator.getErrorDefinitionSnippet()),

    DEF_REMOTE_FUNCTION(SnippetGenerator.getRemoteFunctionSnippet()),

    DEF_INIT_FUNCTION(SnippetGenerator.getInitFunctionSnippet()),

    DEF_ATTACH_FUNCTION(SnippetGenerator.getAttachFunctionSnippet()),

    DEF_START_FUNCTION(SnippetGenerator.getStartFunctionSnippet()),

    DEF_GRACEFUL_STOP_FUNCTION(SnippetGenerator.getGracefulStopFunctionSnippet()),

    DEF_IMMEDIATE_STOP_FUNCTION(SnippetGenerator.getImmediateStopFunctionSnippet()),

    DEF_DETACH_FUNCTION(SnippetGenerator.getDetachFunctionSnippet()),

    // Expressions Snippets
    EXPR_MATCH(SnippetGenerator.getMatchExpressionSnippet()),
    
    
    // Keyword Snippets
    KW_ON(SnippetGenerator.getOnSnippet()),

    KW_NEW(SnippetGenerator.getNewKeywordSnippet()),

    KW_CHECK(SnippetGenerator.getCheckKeywordSnippet()),

    KW_CHECK_PANIC(SnippetGenerator.getCheckPanicKeywordSnippet()),

    KW_WAIT(SnippetGenerator.getWaitKeywordSnippet()),

    KW_START(SnippetGenerator.getStartKeywordSnippet()),

    KW_FLUSH(SnippetGenerator.getFlushKeywordSnippet()),

    KW_IMPORT(SnippetGenerator.getImportKeywordSnippet()),

    KW_FUNCTION(SnippetGenerator.getFunctionKeywordSnippet()),

    KW_PUBLIC(SnippetGenerator.getPublicKeywordSnippet()),

    KW_PRIVATE(SnippetGenerator.getPrivateKeywordSnippet()),

    KW_FINAL(SnippetGenerator.getFinalKeywordSnippet()),

    KW_CONST(SnippetGenerator.getConstKeywordSnippet()),

    KW_TYPE(SnippetGenerator.getTypeKeywordSnippet()),

    KW_ANNOTATION(SnippetGenerator.getAnnotationKeywordSnippet()),

    KW_VAR(SnippetGenerator.getVarKeywordSnippet()),

    KW_LISTENER(SnippetGenerator.getListenerKeywordSnippet()),

    KW_RETURNS(SnippetGenerator.getReturnsKeywordSnippet()),

    KW_UNTAINT(SnippetGenerator.getUntaintKeywordSnippet()),

    KW_ABSTRACT(SnippetGenerator.getAbstractKeywordSnippet()),

    KW_CLIENT(SnippetGenerator.getClientKeywordSnippet()),

    KW_EXTERNAL(SnippetGenerator.getExternalKeywordSnippet()),

    KW_TYPEOF(SnippetGenerator.getTypeofKeywordSnippet()),

    // Statement Snippets
    STMT_ABORT(SnippetGenerator.getAbortSnippet()),

    STMT_BREAK(SnippetGenerator.getBreakSnippet()),

    STMT_CONTINUE(SnippetGenerator.getContinueStatmentSnippet()),

    STMT_FOREACH(SnippetGenerator.getForeachSnippet()),

    STMT_FORK(SnippetGenerator.getForkStatementSnippet()),

    STMT_IF(SnippetGenerator.getIfStatementSnippet()),

    STMT_ELSE_IF(SnippetGenerator.getElseIfStatementSnippet()),

    STMT_ELSE(SnippetGenerator.getElseStatementSnippet()),

    STMT_LOCK(SnippetGenerator.getLockStatementSnippet()),

    STMT_MATCH(SnippetGenerator.getMatchStatementSnippet()),

    STMT_NAMESPACE_DECLARATION(SnippetGenerator.getNamespaceDeclarationSnippet()),

    STMT_RETRY(SnippetGenerator.getRetryStatementSnippet()),

    STMT_RETURN(SnippetGenerator.getReturnStatementSnippet()),

    STMT_PANIC(SnippetGenerator.getPanicStatementSnippet()),

    STMT_TRANSACTION(SnippetGenerator.getTransactionStatementSnippet()),

    STMT_TRAP(SnippetGenerator.getTrapSnippet()),

    STMT_WHILE(SnippetGenerator.getWhileStatementSnippet()),

    // Iterable Operation snippets
    ITR_FOREACH(SnippetGenerator.getIterableForeachSnippet()),

    ITR_MAP(SnippetGenerator.getIterableMapSnippet()),

    ITR_FILTER(SnippetGenerator.getIterableFilterSnippet()),

    ITR_COUNT(SnippetGenerator.getIterableCountSnippet()),

    ITR_SELECT(SnippetGenerator.getIterableSelectSnippet()),

    ITR_MIN(SnippetGenerator.getIterableMinSnippet()),

    ITR_MAX(SnippetGenerator.getIterableMaxSnippet()),

    ITR_AVERAGE(SnippetGenerator.getIterableAverageSnippet()),

    ITR_SUM(SnippetGenerator.getIterableSumSnippet()),

    // Builtin Functions' snippets
    BUILTIN_LENGTH(SnippetGenerator.getBuiltinLengthSnippet()),

    BUILTIN_CLONE(SnippetGenerator.getBuiltinIsCloneSnippet()),

    BUILTIN_FREEZE(SnippetGenerator.getBuiltinFreezeSnippet()),

    BUILTIN_IS_FROZEN(SnippetGenerator.getBuiltinIsFrozenSnippet()),

    BUILTIN_STAMP(SnippetGenerator.getBuiltinStampSnippet()),

    BUILTIN_HAS_KEY(SnippetGenerator.getBuiltinHasKeySnippet()),

    BUILTIN_REMOVE(SnippetGenerator.getBuiltinRemoveSnippet()),

    BUILTIN_VALUES(SnippetGenerator.getBuiltinValuesSnippet()),

    BUILTIN_KEYS(SnippetGenerator.getBuiltinKeysSnippet()),

    BUILTIN_CLEAR(SnippetGenerator.getBuiltinClearSnippet()),

    BUILTIN_CONVERT(SnippetGenerator.getBuiltinConvertSnippet()),

    BUILTIN_IS_NAN(SnippetGenerator.getBuiltinIsNaNSnippet()),

    BUILTIN_IS_FINITE(SnippetGenerator.getBuiltinIsFiniteSnippet()),

    BUILTIN_IS_INFINITE(SnippetGenerator.getBuiltinIsInFiniteSnippet()),

    BUILTIN_DETAIL(SnippetGenerator.getBuiltinDetailSnippet()),

    BUILTIN_REASON(SnippetGenerator.getBuiltinReasonSnippet()),
    
    // Iterable operators' lambda function parameters
    ITR_ON_MAP_PARAMS(SnippetGenerator.getIterableOnMapParamSnippet()),

    ITR_ON_JSON_PARAMS(SnippetGenerator.getIterableOnJsonParamSnippet()),

    ITR_ON_XML_PARAMS(SnippetGenerator.getIterableOnXmlParamSnippet());

    private String snippetName;
    private SnippetBlock snippetBlock;

    Snippet(SnippetBlock snippetBlock) {
        this.snippetName = null;
        this.snippetBlock = snippetBlock;
    }

    Snippet(String snippetName, SnippetBlock snippetBlock) {
        this.snippetName = snippetName;
        this.snippetBlock = snippetBlock;
    }

    /**
     * Get the Snippet Name.
     *
     * @return {@link String} snippet name
     */
    public String snippetName() {
        return this.snippetName;
    }

    /**
     * Get the SnippetBlock.
     *
     * @return {@link SnippetBlock} SnippetBlock
     */
    public SnippetBlock get() {
        return this.snippetBlock;
    }
}
