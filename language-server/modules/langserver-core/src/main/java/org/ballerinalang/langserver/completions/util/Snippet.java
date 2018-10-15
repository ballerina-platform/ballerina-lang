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

    DEF_ENDPOINT(SnippetGenerator.getEndpointSnippet()),

    DEF_FUNCTION(SnippetGenerator.getFunctionDefSnippet()),

    DEF_FUNCTION_SIGNATURE(SnippetGenerator.getFunctionSignatureSnippet()),

    DEF_MAIN_FUNCTION(SnippetGenerator.getMainFunctionSnippet()),

    DEF_NEW_OBJECT_CONSTRUCTOR(SnippetGenerator.getObjectConstructorSnippet()),

    DEF_OBJECT_SNIPPET(SnippetGenerator.getObjectDefinitionSnippet()),

    DEF_RECORD(SnippetGenerator.getRecordDefinitionSnippet()),

    DEF_RESOURCE(SnippetGenerator.getResourceDefinitionSnippet()),

    DEF_SERVICE(SnippetGenerator.getServiceDefSnippet()),

    DEF_SERVICE_WEBSOCKET(SnippetGenerator.getWebSocketServiceDefSnippet()),

    DEF_SERVICE_WEBSUB(SnippetGenerator.getWebSubServiceDefSnippet()),

    DEF_WORKER(SnippetGenerator.getWorkerDeclarationSnippet()),

    DEF_ERROR(SnippetGenerator.getErrorDefinitionSnippet()),


    // Expressions Snippets
    EXPR_MATCH(SnippetGenerator.getMatchExpressionSnippet()),
    
    
    // Keyword Snippets
    KW_BIND(SnippetGenerator.getBindSnippet()),

    KW_CHECK(SnippetGenerator.getCheckKeywordSnippet()),

    KW_IMPORT(SnippetGenerator.getImportKeywordSnippet()),

    KW_LENGTHOF(SnippetGenerator.getLengthofKeywordSnippet()),

    KW_PUBLIC(SnippetGenerator.getPublicKeywordSnippet()),

    KW_TYPE(SnippetGenerator.getTypeKeywordSnippet()),

    KW_VAR(SnippetGenerator.getVarKeywordSnippet()),
    
    
    // Statement Snippets
    STMT_ABORT(SnippetGenerator.getAbortSnippet()),

    STMT_BREAK(SnippetGenerator.getBreakSnippet()),

    STMT_CONTINUE(SnippetGenerator.getContinueStatmentSnippet()),

    STMT_FOREACH(SnippetGenerator.getForeachSnippet()),

    STMT_FORK_JOIN(SnippetGenerator.getForkJoinStatementSnippet()),

    STMT_IF(SnippetGenerator.getIfStatementSnippet()),

    STMT_LOCK(SnippetGenerator.getLockStatementSnippet()),

    STMT_MATCH(SnippetGenerator.getMatchStatementSnippet()),

    STMT_NAMESPACE_DECLARATION(SnippetGenerator.getNamespaceDeclarationSnippet()),

    STMT_RETRY(SnippetGenerator.getRetryStatementSnippet()),

    STMT_RETURN(SnippetGenerator.getReturnStatementSnippet()),

    STMT_THROW(SnippetGenerator.getThrowStatementSnippet()),

    STMT_TRANSACTION(SnippetGenerator.getTransactionStatementSnippet()),

    STMT_TRY_CATCH(SnippetGenerator.getTryCatchStatementSnippet()),

    STMT_WHILE(SnippetGenerator.getWhileStatementSnippet()),

    STMT_WORKER_REPLY(SnippetGenerator.getWorkerReplyStatementSnippet()),

    STMT_WORKER_TRIGGER(SnippetGenerator.getWorkerTriggerStatementSnippet()),


    // Iterable Operation snippets
    ITR_FOREACH(SnippetGenerator.getIterableForeachSnippet()),

    ITR_MAP(SnippetGenerator.getIterableMapSnippet()),

    ITR_FILTER(SnippetGenerator.getIterableFilterSnippet()),

    ITR_COUNT(SnippetGenerator.getIterableCountSnippet()),

    ITR_MIN(SnippetGenerator.getIterableMinSnippet()),

    ITR_MAX(SnippetGenerator.getIterableMaxSnippet()),

    ITR_AVERAGE(SnippetGenerator.getIterableAverageSnippet()),

    ITR_SUM(SnippetGenerator.getIterableSumSnippet()),


    // Iterable operators' lambda function parameters
    ITR_ON_MAP_PARAMS(SnippetGenerator.getIterableOnMapParamSnippet()),

    ITR_ON_JSON_PARAMS(SnippetGenerator.getIterableOnJsonParamSnippet()),

    ITR_ON_XML_PARAMS(SnippetGenerator.getIterableOnXmlParamSnippet());

    private SnippetBlock snippetBlock;

    Snippet(SnippetBlock snippetBlock) {
        this.snippetBlock = snippetBlock;
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
