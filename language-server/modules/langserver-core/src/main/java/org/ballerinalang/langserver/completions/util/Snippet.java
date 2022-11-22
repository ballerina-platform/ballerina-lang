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

import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;

/**
 * Snippet for the Ballerina language constructs.
 */
public enum Snippet {

    // Definition Snippets
    DEF_ANNOTATION(SnippetGenerator.getAnnotationDefSnippet()),

    DEF_FUNCTION(SnippetGenerator.getFunctionDefSnippet()),

    DEF_EXPRESSION_BODIED_FUNCTION(SnippetGenerator.getExpressionBodiedFunctionDefSnippet()),

    DEF_RESOURCE_FUNCTION(SnippetGenerator.getResourceFunctionDefSnippet()),

    DEF_RESOURCE_FUNCTION_SIGNATURE(SnippetGenerator.getResourceFunctionSignatureSnippet()),

    DEF_FUNCTION_SIGNATURE(SnippetGenerator.getFunctionSignatureSnippet()),

    DEF_MAIN_FUNCTION(SnippetGenerator.getMainFunctionSnippet()),

    DEF_OBJECT_TYPE_DESC_SNIPPET(SnippetGenerator.getObjectTypeDescSnippet()),

    DEF_OBJECT_SNIPPET(SnippetGenerator.getObjectDefinitionSnippet()),

    DEF_RECORD(SnippetGenerator.getRecordDefinitionSnippet()),

    DEF_CLOSED_RECORD(SnippetGenerator.getClosedRecordDefinitionSnippet()),

    DEF_ERROR_TYPE(SnippetGenerator.getErrorTypeDefinitionSnippet()),

    DEF_RECORD_TYPE_DESC(SnippetGenerator.getRecordTypeDescSnippet()),

    DEF_CLOSED_RECORD_TYPE_DESC(SnippetGenerator.getClosedRecordTypeDescSnippet()),

    DEF_ERROR_TYPE_DESC(SnippetGenerator.getErrorTypeDescSnippet()),

    DEF_TABLE_TYPE_DESC(SnippetGenerator.getTableTypeDescSnippet()),

    DEF_TABLE_WITH_KEY_TYPE_DESC(SnippetGenerator.getTableWithKeyTypeDescSnippet()),

    DEF_STREAM(SnippetGenerator.getStreamDefSnippet()),

//    DEF_RESOURCE_HTTP(SnippetGenerator.getResourceDefinitionSnippet()),
//
//    DEF_RESOURCE_COMMON(SnippetGenerator.getCommonResourceDefinitionSnippet()),
//
//    DEF_RESOURCE_GRPC(SnippetGenerator.getGRPCResourceDefinitionSnippet()),
//
//    DEF_RESOURCE_WS_OPEN(SnippetGenerator.getWebSocketResourceOnOpenSnippet()),
//
//    DEF_RESOURCE_WS_TEXT(SnippetGenerator.getWebSocketResourceOnTextSnippet()),
//
//    DEF_RESOURCE_WS_BINARY(SnippetGenerator.getWebSocketResourceOnBinarySnippet()),
//
//    DEF_RESOURCE_WS_PING(SnippetGenerator.getWebSocketResourceOnPingSnippet()),
//
//    DEF_RESOURCE_WS_PONG(SnippetGenerator.getWebSocketResourceOnPongSnippet()),
//
//    DEF_RESOURCE_WS_IDLE(SnippetGenerator.getWebSocketResourceOnIdleTimeoutSnippet()),
//
//    DEF_RESOURCE_WS_ERROR(SnippetGenerator.getWebSocketResourceOnErrorSnippet()),
//
//    DEF_RESOURCE_WS_CLOSE(SnippetGenerator.getWebSocketResourceOnCloseSnippet()),
//
//    DEF_RESOURCE_WS_CS_TEXT(SnippetGenerator.getWebSocketClientServiceResourceOnTextSnippet()),
//
//    DEF_RESOURCE_WS_CS_BINARY(SnippetGenerator.getWebSocketClientServiceResourceOnBinarySnippet()),
//
//    DEF_RESOURCE_WS_CS_PING(SnippetGenerator.getWebSocketClientServiceResourceOnPingSnippet()),
//
//    DEF_RESOURCE_WS_CS_PONG(SnippetGenerator.getWebSocketClientServiceResourceOnPongSnippet()),
//
//    DEF_RESOURCE_WS_CS_IDLE(SnippetGenerator.getWebSocketClientServiceResourceOnIdleTimeoutSnippet()),
//
//    DEF_RESOURCE_WS_CS_ERROR(SnippetGenerator.getWebSocketClientServiceResourceOnErrorSnippet()),
//
//    DEF_RESOURCE_WS_CS_CLOSE(SnippetGenerator.getWebSocketClientServiceResourceOnCloseSnippet()),
//
//    DEF_RESOURCE_WEBSUB_INTENT(SnippetGenerator.getWebSubResourceOnIntentVerificationSnippet()),
//
//    DEF_RESOURCE_WEBSUB_NOTIFY(SnippetGenerator.getWebSubResourceOnNotificationSnippet()),

    DEF_SERVICE_COMMON(SnippetGenerator.getCommonServiceSnippet()),

//    DEF_SERVICE_VAR(SnippetGenerator.getServiceVarSnippet()),

    DEF_CLASS(SnippetGenerator.getClassDefSnippet()),

    DEF_ENUM(SnippetGenerator.getEnumDefSnippet()),

    DEF_WORKER(SnippetGenerator.getWorkerDeclarationSnippet()),

    DEF_REMOTE_FUNCTION(SnippetGenerator.getRemoteFunctionSnippet()),

    DEF_REMOTE_METHOD_DECL(SnippetGenerator.getRemoteMethodDeclSnippet()),

    DEF_INIT_FUNCTION(SnippetGenerator.getInitFunctionSnippet()),

    DEF_ATTACH_FUNCTION(SnippetGenerator.getAttachFunctionSnippet()),

    DEF_START_FUNCTION(SnippetGenerator.getStartFunctionSnippet()),

    DEF_GRACEFUL_STOP_FUNCTION(SnippetGenerator.getGracefulStopFunctionSnippet()),

    DEF_IMMEDIATE_STOP_FUNCTION(SnippetGenerator.getImmediateStopFunctionSnippet()),

    DEF_DETACH_FUNCTION(SnippetGenerator.getDetachFunctionSnippet()),
    
    // Expressions Snippets
    EXPR_ERROR_CONSTRUCTOR(SnippetGenerator.getErrorConstructorSnippet()),

    EXPR_OBJECT_CONSTRUCTOR(SnippetGenerator.getObjectConstructorSnippet()),

    EXPR_BASE16_LITERAL(SnippetGenerator.getBase16LiteralSnippet()),

    EXPR_BASE64_LITERAL(SnippetGenerator.getBase64LiteralSnippet()),


    // Keyword Snippets
    KW_ON(SnippetGenerator.getOnSnippet()),

    KW_NEW(SnippetGenerator.getNewKeywordSnippet()),

    KW_DEFAULT(SnippetGenerator.getDefaultKeywordSnippet()),

    KW_TABLE(SnippetGenerator.getTableKeywordSnippet()),

    KW_SERVICE(SnippetGenerator.getServiceKeywordSnippet()),

    KW_STRING(SnippetGenerator.getStringKeywordSnippet()),

    KW_XML(SnippetGenerator.getXMLKeywordSnippet()),

    KW_LET(SnippetGenerator.getLetKeywordSnippet()),

    KW_KEY(SnippetGenerator.getKeyKeywordSnippet()),

    KW_TRAP(SnippetGenerator.getTrapKeywordSnippet()),

    KW_ERROR(SnippetGenerator.getErrorKeywordSnippet()),

    KW_CHECK(SnippetGenerator.getCheckKeywordSnippet()),

    KW_CHECK_PANIC(SnippetGenerator.getCheckPanicKeywordSnippet()),

    KW_WAIT(SnippetGenerator.getWaitKeywordSnippet()),

    KW_START(SnippetGenerator.getStartKeywordSnippet()),

    KW_AS(SnippetGenerator.getAsKeywordSnippet()),

    KW_VERSION(SnippetGenerator.getVersionKeywordSnippet()),

    KW_FROM(SnippetGenerator.getFromKeywordSnippet()),

    KW_WHERE(SnippetGenerator.getWhereKeywordSnippet()),

    KW_JOIN(SnippetGenerator.getJoinKeywordSnippet()),

    KW_ORDERBY(SnippetGenerator.getOrderByKeywordSnippet()),

    KW_LIMIT(SnippetGenerator.getLimitKeywordSnippet()),

    KW_SELECT(SnippetGenerator.getSelectKeywordSnippet()),

    KW_EQUALS(SnippetGenerator.getEqualsKeywordSnippet()),

    KW_FLUSH(SnippetGenerator.getFlushKeywordSnippet()),

    KW_IMPORT(SnippetGenerator.getImportKeywordSnippet()),

    KW_FUNCTION(SnippetGenerator.getFunctionKeywordSnippet()),

    KW_RESOURCE(SnippetGenerator.getResourceKeywordSnippet()),

    KW_PUBLIC(SnippetGenerator.getPublicKeywordSnippet()),

    KW_ISOLATED(SnippetGenerator.getIsolatedKeywordSnippet()),

    KW_PRIVATE(SnippetGenerator.getPrivateKeywordSnippet()),

    KW_FINAL(SnippetGenerator.getFinalKeywordSnippet()),

    KW_CONFIGURABLE(SnippetGenerator.getConfigurableKeywordSnippet()),

    KW_FAIL(SnippetGenerator.getFailKeywordSnippet()),

    KW_REMOTE(SnippetGenerator.getRemoteKeywordSnippet()),

    KW_CONST(SnippetGenerator.getConstKeywordSnippet()),

    KW_TYPE(SnippetGenerator.getTypeKeywordSnippet()),

    KW_RECORD(SnippetGenerator.getRecordKeywordSnippet()),

    KW_OBJECT(SnippetGenerator.getObjectKeywordSnippet()),

    KW_ANNOTATION(SnippetGenerator.getAnnotationKeywordSnippet()),

    KW_VAR(SnippetGenerator.getVarKeywordSnippet()),

    KW_IN(SnippetGenerator.getInKeywordSnippet()),

    KW_ENUM(SnippetGenerator.getEnumKeywordSnippet()),

    KW_XMLNS(SnippetGenerator.getXMLNSKeywordSnippet()),

    KW_CLASS(SnippetGenerator.getClassKeywordSnippet()),

    KW_DISTINCT(SnippetGenerator.getDistinctKeywordSnippet()),

    KW_LISTENER(SnippetGenerator.getListenerKeywordSnippet()),

    KW_RETURNS(SnippetGenerator.getReturnsKeywordSnippet()),

    KW_UNTAINT(SnippetGenerator.getUntaintKeywordSnippet()),

    KW_ABSTRACT(SnippetGenerator.getAbstractKeywordSnippet()),

    KW_CLIENT(SnippetGenerator.getClientKeywordSnippet()),

    KW_READONLY(SnippetGenerator.getReadonlyKeywordSnippet()),

    KW_EXTERNAL(SnippetGenerator.getExternalKeywordSnippet()),

    KW_TYPEOF(SnippetGenerator.getTypeofKeywordSnippet()),

    KW_IS(SnippetGenerator.getIsKeywordSnippet()),

    KW_IF(SnippetGenerator.getIfKeywordSnippet()),

    KW_ASCENDING(SnippetGenerator.getAscendingKeywordSnippet()),

    KW_DESCENDING(SnippetGenerator.getDescendingKeywordSnippet()),

    KW_TRANSACTIONAL(SnippetGenerator.getTransactionalKeywordSnippet()),

    KW_WORKER(SnippetGenerator.getKeywordSnippet("worker")),

    KW_FIELD(SnippetGenerator.getKeywordSnippet("field")),

    KW_SOURCE(SnippetGenerator.getKeywordSnippet("source")),

    KW_OBJ_FUNCTION(SnippetGenerator.getKeywordSnippet("object function")),

    KW_SERVICE_REMOTE_FUNCTION(SnippetGenerator.getKeywordSnippet("service remote function")),

    KW_REMOTE_FUNCTION(SnippetGenerator.getKeywordSnippet("remote function")),

    KW_PARAMETER(SnippetGenerator.getKeywordSnippet("parameter")),

    KW_RETURN(SnippetGenerator.getKeywordSnippet("return")),

    KW_OBJECT_FIELD(SnippetGenerator.getKeywordSnippet("object field")),

    KW_RECORD_FIELD(SnippetGenerator.getKeywordSnippet("record field")),

    KW_SOURCE_ANNOTATION(SnippetGenerator.getKeywordSnippet("source annotation")),

    KW_SOURCE_EXTERNAL(SnippetGenerator.getKeywordSnippet("source external")),

    KW_SOURCE_VAR(SnippetGenerator.getKeywordSnippet("source var")),

    KW_SOURCE_CONST(SnippetGenerator.getKeywordSnippet("source const")),

    KW_SOURCE_LISTENER(SnippetGenerator.getKeywordSnippet("source listener")),

    KW_SOURCE_WORKER(SnippetGenerator.getKeywordSnippet("source worker")),

    KW_SOURCE_CLIENT(SnippetGenerator.getKeywordSnippet("source client")),

    KW_TRUE(SnippetGenerator.getKeywordSnippet("true")),

    KW_FALSE(SnippetGenerator.getKeywordSnippet("false")),

    KW_NIL(SnippetGenerator.getKeywordSnippet("null")),

    KW_OUTER(SnippetGenerator.getOuterKeywordSnippet()),

    // Statement Snippets
    STMT_BREAK(SnippetGenerator.getBreakSnippet()),

    STMT_ROLLBACK(SnippetGenerator.getRollbackStatementSnippet()),

    STMT_COMMIT(SnippetGenerator.getCommitStatementSnippet()),

    STMT_CONTINUE(SnippetGenerator.getContinueStatmentSnippet()),

    STMT_FOREACH(SnippetGenerator.getForeachSnippet()),

    STMT_FOREACH_RANGE_EXP(SnippetGenerator.getForeachRangeExpressionSnippet()),

    STMT_FORK(SnippetGenerator.getForkStatementSnippet()),

    STMT_IF(SnippetGenerator.getIfStatementSnippet()),

    STMT_ELSE_IF(SnippetGenerator.getElseIfStatementSnippet()),

    STMT_ELSE(SnippetGenerator.getElseStatementSnippet()),

    STMT_LOCK(SnippetGenerator.getLockStatementSnippet()),

    STMT_MATCH(SnippetGenerator.getMatchStatementSnippet()),

    STMT_NAMESPACE_DECLARATION(SnippetGenerator.getXMLNSDeclarationSnippet()),

    STMT_RETURN(SnippetGenerator.getReturnStatementSnippet()),

    STMT_RETURN_SC(SnippetGenerator.getReturnSCStatementSnippet()),

    STMT_PANIC(SnippetGenerator.getPanicStatementSnippet()),

    STMT_TRANSACTION(SnippetGenerator.getTransactionStatementSnippet()),

    STMT_RETRY(SnippetGenerator.getRetryStatementSnippet()),

    STMT_RETRY_TRANSACTION(SnippetGenerator.getRetryTransactionStatementSnippet()),

    STMT_TRAP(SnippetGenerator.getTrapSnippet()),

    STMT_WHILE(SnippetGenerator.getWhileStatementSnippet()),

    STMT_DO(SnippetGenerator.getDoStatementSnippet()),

    // Snippets related to various clauses such as from, where and etc
    CLAUSE_FROM(SnippetGenerator.getFromClauseSnippet()),

    CLAUSE_DO(SnippetGenerator.getDoClauseSnippet()),

    CLAUSE_LET(SnippetGenerator.getLetClauseSnippet()),

    CLAUSE_JOIN(SnippetGenerator.getJoinClauseSnippet()),

    CLAUSE_ON_FAIL(SnippetGenerator.getOnFailClauseSnippet()),

    CLAUSE_ON_CONFLICT(SnippetGenerator.getOnConflictClauseSnippet()),

    TYPE_MAP(SnippetGenerator.getMapTypeSnippet());

    private final SnippetBlock snippetBlock;

    Snippet(SnippetBlock snippetBlock) {
        this.snippetBlock = snippetBlock;
        this.snippetBlock.setId(name());
    }
    
    public boolean equals(LSCompletionItem lsCItem) {
        return lsCItem.getType() == LSCompletionItem.CompletionItemType.SNIPPET
                && ((SnippetCompletionItem) lsCItem).id().equals(name());
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
