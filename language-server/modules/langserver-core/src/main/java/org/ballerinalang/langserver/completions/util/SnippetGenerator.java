/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.util.SnippetBlock.Kind;

import java.util.Arrays;
import java.util.List;

/**
 * Generates Snippets for language constructs.
 *
 * @since 0.982.0
 */
public class SnippetGenerator {

    private static final String FILTER_TEXT_SEPARATOR = "_";

    private SnippetGenerator() {
    }

    /**
     * Get Annotation Definition statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAnnotationDefSnippet() {
        String snippet = "annotation ${1:typeName} ${2:name} on ${3:attachmentPoint};";
        return new SnippetBlock(ItemResolverConstants.ANNOTATION, ItemResolverConstants.ANNOTATION, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get On keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getOnSnippet() {
        return new SnippetBlock(ItemResolverConstants.ON, ItemResolverConstants.ON, "on ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get new keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getNewKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.NEW, ItemResolverConstants.NEW, "new ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get default keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getDefaultKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.DEFAULT, ItemResolverConstants.DEFAULT, "default",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get abstract keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAbstractKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.ABSTRACT, ItemResolverConstants.ABSTRACT, "abstract ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get client keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getClientKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.CLIENT, ItemResolverConstants.CLIENT, "client ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get readonly keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getReadonlyKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.READONLY, ItemResolverConstants.READONLY, "readonly ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get external keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getExternalKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.EXTERNAL, ItemResolverConstants.EXTERNAL, "external;",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get typeof keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTypeofKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.TYPEOF, ItemResolverConstants.TYPEOF, "typeof ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get is keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIsKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.IS, ItemResolverConstants.IS, "is",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get if keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIfKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.IF, ItemResolverConstants.IF, "if",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get {@code ascending} keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAscendingKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.ASCENDING, ItemResolverConstants.ASCENDING, "ascending",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get {@code descending} keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getDescendingKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.DESCENDING, ItemResolverConstants.DESCENDING, "descending",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get {@code transactional} keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTransactionalKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.TRANSACTIONAL, ItemResolverConstants.TRANSACTIONAL,
                "transactional", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Break statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBreakSnippet() {
        return new SnippetBlock(ItemResolverConstants.BREAK, ItemResolverConstants.BREAK, "break;",
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Check Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getCheckKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.CHECK_KEYWORD, ItemResolverConstants.CHECK_KEYWORD, "check ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get checkpanic Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getCheckPanicKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.CHECKPANIC_KEYWORD, ItemResolverConstants.CHECKPANIC_KEYWORD,
                "checkpanic ", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Wait Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWaitKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.WAIT_KEYWORD, ItemResolverConstants.WAIT_KEYWORD, "wait ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Start Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getStartKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.START_KEYWORD, ItemResolverConstants.START_KEYWORD, "start ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get as Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAsKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.AS_KEYWORD, ItemResolverConstants.AS_KEYWORD, "as ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get version Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getVersionKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.VERSION_KEYWORD, ItemResolverConstants.VERSION_KEYWORD,
                "version", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get From Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFromKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.FROM_KEYWORD, ItemResolverConstants.FROM_KEYWORD, "from ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Where Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWhereKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.WHERE_KEYWORD, ItemResolverConstants.WHERE_KEYWORD, "where ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Join Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getJoinKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.JOIN_KEYWORD, ItemResolverConstants.JOIN_KEYWORD, "join ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Outer Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getOuterKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.OUTER_KEYWORD, ItemResolverConstants.OUTER_KEYWORD, "outer ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Order By Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getOrderByKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.ORDERBY_KEYWORD, ItemResolverConstants.ORDERBY_KEYWORD,
                "order by ", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Limit Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getLimitKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.LIMIT_KEYWORD, ItemResolverConstants.LIMIT_KEYWORD, "limit ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Select Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getSelectKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.SELECT_KEYWORD, ItemResolverConstants.SELECT_KEYWORD, "select ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get {@code equals} Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getEqualsKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.EQUALS_KEYWORD, ItemResolverConstants.EQUALS_KEYWORD, "equals ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Flush Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFlushKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.FLUSH_KEYWORD, ItemResolverConstants.FLUSH_KEYWORD, "flush ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Import Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getImportKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.IMPORT, ItemResolverConstants.IMPORT, "import ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Function Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFunctionKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.FUNCTION, ItemResolverConstants.FUNCTION, "function ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get resource Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getResourceKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.RESOURCE, ItemResolverConstants.RESOURCE, "resource ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Continue Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getContinueStatmentSnippet() {
        return new SnippetBlock(ItemResolverConstants.CONTINUE, ItemResolverConstants.CONTINUE, "continue;",
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Listener Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getListenerKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.LISTENER_KEYWORD, ItemResolverConstants.LISTENER_KEYWORD,
                "listener ", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Returns Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getReturnsKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.RETURNS_KEYWORD, ItemResolverConstants.RETURNS_KEYWORD,
                "returns ", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Untaint Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getUntaintKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.UNTAINTED_KEYWORD, ItemResolverConstants.UNTAINTED_KEYWORD,
                "untainted ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Foreach Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getForeachSnippet() {
        String snippet = "foreach ${1:var} ${2:item} in ${3:itemList} {" + CommonUtil.LINE_SEPARATOR + "\t${4}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.FOREACH, ItemResolverConstants.FOREACH, snippet,
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get ForeachRangeExpression Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getForeachRangeExpressionSnippet() {
        String snippet = "foreach ${1:int} ${2:i} in ${3:0}...${4:9} {" + CommonUtil.LINE_SEPARATOR + "\t${5}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.FOREACH_RANGE_EXP, ItemResolverConstants.FOREACH, snippet,
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Fork Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getForkStatementSnippet() {
        String snippet = "fork {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.FORK, ItemResolverConstants.FORK, snippet,
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Function Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFunctionDefSnippet() {
        String snippet = "function ${1:name}(${2})${3} {" + CommonUtil.LINE_SEPARATOR + "\t${4}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.FUNCTION, ItemResolverConstants.FUNCTION, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get expression bodied function snippet block.
     *
     * @return {@link SnippetBlock} Generated snippet
     */
    public static SnippetBlock getExpressionBodiedFunctionDefSnippet() {
        String snippet = "function ${1:name}(${2})${3} => (${4});";
        return new SnippetBlock(ItemResolverConstants.EXPRESSION_BODIED_FUNCTION, ItemResolverConstants.FUNCTION,
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Resource Function Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getResourceFunctionDefSnippet() {
        String snippet = "function ${1:accessor} ${2:path} (${3})${4} {" + CommonUtil.LINE_SEPARATOR + "\t${5}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.RESOURCE_FUNC_DEF, ItemResolverConstants.FUNCTION, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Resource Function Signature Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getResourceFunctionSignatureSnippet() {
        String snippet = "resource function ${1:accessor} ${2:path}(${3})${4} {" + CommonUtil.LINE_SEPARATOR + "\t${5}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.RESOURCE_FUNC_DEF,
                generateFilterText(Arrays.asList(ItemResolverConstants.RESOURCE, ItemResolverConstants.FUNCTION)),
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Function Signature Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFunctionSignatureSnippet() {
        String snippet = "function ${1:name}(${2})${3};";
        return new SnippetBlock(ItemResolverConstants.FUNCTION_SIGNATURE, ItemResolverConstants.FUNCTION, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get If Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIfStatementSnippet() {
        String snippet = "if ${1:true} {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.IF, ItemResolverConstants.IF, snippet,
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Else If Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getElseIfStatementSnippet() {
        String snippet = "else if ${1:true} {" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.ELSE_IF, ItemResolverConstants.ELSE, snippet,
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Else Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getElseStatementSnippet() {
        String snippet = "else {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.ELSE, ItemResolverConstants.ELSE, snippet,
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Lock Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getLockStatementSnippet() {
        String snippet = "lock {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.LOCK, ItemResolverConstants.LOCK, snippet,
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Main Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getMainFunctionSnippet() {
        String snippet = "public function main() {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.MAIN_FUNCTION,
                generateFilterText(Arrays.asList(ItemResolverConstants.PUBLIC_KEYWORD,
                        ItemResolverConstants.FUNCTION, "main")), snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Match Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getMatchStatementSnippet() {
        return new SnippetBlock(ItemResolverConstants.MATCH, ItemResolverConstants.MATCH, "match ",
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Namespace Declaration Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getXMLNSDeclarationSnippet() {
        String snippet = "xmlns \"${1}\" as ${2:ns};";

        return new SnippetBlock(ItemResolverConstants.XMLNS, ItemResolverConstants.XMLNS, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Object Type Descriptor Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getObjectTypeDescSnippet() {
        String snippet = "object {${1}}";

        return new SnippetBlock(ItemResolverConstants.OBJECT_TYPE_DESC, ItemResolverConstants.OBJECT_KEYWORD, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Object Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getObjectDefinitionSnippet() {
        String snippet = "type ${1:ObjectName} object {${2}};";

        return new SnippetBlock(ItemResolverConstants.OBJECT_TYPE,
                generateFilterText(Arrays.asList(ItemResolverConstants.TYPE_TYPE,
                        ItemResolverConstants.OBJECT_KEYWORD)), snippet, ItemResolverConstants.SNIPPET_TYPE,
                Kind.SNIPPET);
    }

    /**
     * Get Public Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getPublicKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.PUBLIC_KEYWORD, ItemResolverConstants.PUBLIC_KEYWORD, "public ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get {@code isolated} Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIsolatedKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.ISOLATED_KEYWORD, ItemResolverConstants.ISOLATED_KEYWORD,
                "isolated ", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Private Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getPrivateKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.PRIVATE_KEYWORD, ItemResolverConstants.PRIVATE_KEYWORD,
                "private ", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Type Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTypeKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.TYPE_TYPE, ItemResolverConstants.TYPE_TYPE, "type ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Record Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRecordKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.RECORD_KEYWORD, ItemResolverConstants.RECORD_KEYWORD, "record ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Object Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getObjectKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.OBJECT_KEYWORD, ItemResolverConstants.OBJECT_KEYWORD, "object ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Annotation Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAnnotationKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.ANNOTATION, ItemResolverConstants.ANNOTATION,
                "annotation ", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Record Type Descriptor Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRecordTypeDescSnippet() {
        String snippet = "record {${1}}";
        return new SnippetBlock(ItemResolverConstants.RECORD_TYPE_DESC, ItemResolverConstants.RECORD_KEYWORD, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Record Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRecordDefinitionSnippet() {
        String snippet = "type ${1:RecordName} record {" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "};";

        return new SnippetBlock(ItemResolverConstants.RECORD_TYPE,
                generateFilterText(Arrays.asList(ItemResolverConstants.TYPE_TYPE,
                        ItemResolverConstants.RECORD_KEYWORD)),
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Error Type Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getErrorTypeDefinitionSnippet() {
        String snippet = "type ${1:ErrorName} error<${2:map<anydata>}>;";

        return new SnippetBlock(ItemResolverConstants.ERROR_TYPE,
                generateFilterText(Arrays.asList(ItemResolverConstants.TYPE_TYPE,
                        ItemResolverConstants.ERROR)), snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Error Type Descriptor Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getErrorTypeDescSnippet() {
        String snippet = "error<${1:map<anydata>}>;";

        return new SnippetBlock(ItemResolverConstants.ERROR_TYPE, ItemResolverConstants.ERROR, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Table Type Descriptor Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTableTypeDescSnippet() {
        String snippet = "type ${1:TypeName} table<${2}>;";

        return new SnippetBlock(ItemResolverConstants.TABLE_TYPE,
                generateFilterText(Arrays.asList(ItemResolverConstants.TYPE_TYPE, ItemResolverConstants.TABLE)),
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Table Type Descriptor Snippet Block with key.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTableWithKeyTypeDescSnippet() {
        String snippet = "type ${1:TypeName} table<${2}> key${3}";

        return new SnippetBlock(ItemResolverConstants.TABLE_WITH_KEY_TYPE,
                generateFilterText(Arrays.asList(ItemResolverConstants.TYPE_TYPE,
                        ItemResolverConstants.TABLE, ItemResolverConstants.KEY)),
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get stream definition Snippet Block with key.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getStreamDefSnippet() {
        String snippet = "stream<${1}> ${2:streamName} = new;";

        return new SnippetBlock(ItemResolverConstants.STREAM_DEF, "stream", snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Closed Record Type Descriptor Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getClosedRecordTypeDescSnippet() {
        String snippet = "record {|${1}|}";

        return new SnippetBlock(ItemResolverConstants.CLOSED_RECORD_TYPE_DESC, ItemResolverConstants.RECORD_KEYWORD,
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Closed Record Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getClosedRecordDefinitionSnippet() {
        String snippet = "type ${1:RecordName} record {|" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "|};";

        return new SnippetBlock(ItemResolverConstants.CLOSED_RECORD_TYPE,
                generateFilterText(Arrays.asList(ItemResolverConstants.TYPE_TYPE,
                        ItemResolverConstants.RECORD_KEYWORD)),
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Return Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getReturnStatementSnippet() {
        return new SnippetBlock(ItemResolverConstants.RETURN, ItemResolverConstants.RETURN, "return ",
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Return; Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getReturnSCStatementSnippet() {
        return new SnippetBlock(ItemResolverConstants.RETURN_SC, ItemResolverConstants.RETURN_SC, "return;",
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Service Variable Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getCommonServiceSnippet() {
        String snippet = "service on ${1:listenerName} {"
                + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.SERVICE, ItemResolverConstants.SERVICE, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get {@code class} Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getClassDefSnippet() {
        String snippet = "class ${1:className} {" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.CLASS, ItemResolverConstants.CLASS, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get {@code enum} Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getEnumDefSnippet() {
        String snippet = "enum ${1:enumName} {" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.ENUM, ItemResolverConstants.ENUM, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Error Constructor expression Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getErrorConstructorSnippet() {
        String snippet = "error(\"${1}\")";
        return new SnippetBlock("error constructor", ItemResolverConstants.ERROR, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Object Constructor expression Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getObjectConstructorSnippet() {
        String snippet = "object {${1}}";
        return new SnippetBlock("object constructor", ItemResolverConstants.OBJECT_KEYWORD, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Base16 literal Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBase16LiteralSnippet() {
        String snippet = "base16 `${1}`";
        return new SnippetBlock("base16", "base16", snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Base64 literal Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBase64LiteralSnippet() {
        String snippet = "base64 `${1}`";
        return new SnippetBlock("base64", "base64", snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Panic Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getPanicStatementSnippet() {
        return new SnippetBlock(ItemResolverConstants.PANIC, ItemResolverConstants.PANIC, "panic ",
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Const Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getConstKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.CONST_KEYWORD, ItemResolverConstants.CONST_KEYWORD, "const ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Final Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFinalKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.FINAL_KEYWORD, ItemResolverConstants.FINAL_KEYWORD, "final ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Configurable Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getConfigurableKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.CONFIGURABLE_KEYWORD,
                ItemResolverConstants.CONFIGURABLE_KEYWORD, "configurable",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get {@code fail} Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFailKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.FAIL_KEYWORD, ItemResolverConstants.FAIL_KEYWORD, "fail ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get {@code remote} Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRemoteKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.REMOTE_KEYWORD, ItemResolverConstants.REMOTE_KEYWORD, "remote",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Transaction Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTransactionStatementSnippet() {
        String snippet = "transaction {" + CommonUtil.LINE_SEPARATOR
                + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.TRANSACTION, ItemResolverConstants.TRANSACTION, snippet,
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Retry Transaction Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRetryTransactionStatementSnippet() {
        String snippet = "retry transaction {" + CommonUtil.LINE_SEPARATOR
                + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.RETRY_TRANSACTION,
                generateFilterText(Arrays.asList(ItemResolverConstants.RETRY, ItemResolverConstants.TRANSACTION)),
                snippet, ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Retry Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRetryStatementSnippet() {
        String snippet = "retry {" + CommonUtil.LINE_SEPARATOR
                + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.RETRY, ItemResolverConstants.RETRY, snippet,
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get Trap Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTrapSnippet() {
        String snippet = "trap ";
        return new SnippetBlock(ItemResolverConstants.TRAP, ItemResolverConstants.TRAP, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Var Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getVarKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.VAR_KEYWORD, ItemResolverConstants.VAR_KEYWORD, "var ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get in Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getInKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.IN_KEYWORD, ItemResolverConstants.IN_KEYWORD, "in ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Enum Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getEnumKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.ENUM_KEYWORD, ItemResolverConstants.ENUM_KEYWORD, "enum ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Enum Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getXMLNSKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.XMLNS, ItemResolverConstants.XMLNS, "xmlns ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get {@code class} Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getClassKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.CLASS, ItemResolverConstants.CLASS, "class ",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get {@code distinct} Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getDistinctKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.DISTINCT, ItemResolverConstants.DISTINCT, "distinct",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get Rollback Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRollbackStatementSnippet() {
        return new SnippetBlock(ItemResolverConstants.ROLLBACK, ItemResolverConstants.ROLLBACK, "rollback;",
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get commit statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getCommitStatementSnippet() {
        return new SnippetBlock(ItemResolverConstants.COMMIT, ItemResolverConstants.COMMIT, "commit;",
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get While Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWhileStatementSnippet() {
        String snippet = "while ${1:true} {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.WHILE, ItemResolverConstants.WHILE, snippet,
                ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get {@code do} Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getDoStatementSnippet() {
        return getDoSnippet(ItemResolverConstants.STATEMENT_TYPE, Kind.STATEMENT);
    }

    /**
     * Get {@code do} Clause Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getDoClauseSnippet() {
        return getDoSnippet(ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Common method to generate {@code do} statement/clause.
     *
     * @param snippetType      Snippet type
     * @param snippetBlockKind Snippet block kind
     * @return Snippet block for do stmt/clause
     */
    private static SnippetBlock getDoSnippet(String snippetType, Kind snippetBlockKind) {
        String snippet = "do {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.DO, ItemResolverConstants.DO, snippet, snippetType,
                snippetBlockKind);
    }

    /**
     * Get From clause Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFromClauseSnippet() {
        String snippet = "from ${1:var} ${2:item} in " + "${3}";

        return new SnippetBlock(ItemResolverConstants.FROM_CLAUSE, ItemResolverConstants.FROM_KEYWORD, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Let clause Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getLetClauseSnippet() {
        String snippet = "let ${1:var} ${2:varName} = " + "${3}";

        return new SnippetBlock(ItemResolverConstants.LET_CLAUSE, ItemResolverConstants.LET, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Join clause Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getJoinClauseSnippet() {
        String snippet = "join ${1:var} ${2:varName} in ${3:expr} on ${4:onExpr} equals ${5:equalsExpr}";

        return new SnippetBlock(ItemResolverConstants.JOIN_CLAUSE, ItemResolverConstants.JOIN_KEYWORD, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get on fail clause Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getOnFailClauseSnippet() {
        String snippet = "on fail ${1:var} ${2:varName} {" + CommonUtil.LINE_SEPARATOR + "\t${3}"
                + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(ItemResolverConstants.ON_FAIL_CLAUSE,
                generateFilterText(Arrays.asList(ItemResolverConstants.ON, ItemResolverConstants.FAIL_KEYWORD)),
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get on conflict clause Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getOnConflictClauseSnippet() {
        String snippet = "on conflict ";

        return new SnippetBlock(ItemResolverConstants.ON_CONFLICT_CLAUSE,
                generateFilterText(Arrays.asList(ItemResolverConstants.ON, "conflict")),
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Worker Declaration Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWorkerDeclarationSnippet() {
        String snippet = "worker ${1:name} {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(ItemResolverConstants.WORKER, ItemResolverConstants.WORKER, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Remote function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRemoteFunctionSnippet() {
        String snippet = "remote function ${1:name}(${2})${3} {" + CommonUtil.LINE_SEPARATOR + "\t${4}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.REMOTE_FUNCTION_TYPE,
                generateFilterText(Arrays.asList(ItemResolverConstants.REMOTE_KEYWORD, ItemResolverConstants.FUNCTION)),
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Remote method declaration Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRemoteMethodDeclSnippet() {
        String snippet = "remote function ${1:name}(${2})${3};";
        return new SnippetBlock(ItemResolverConstants.REMOTE_FUNCTION_TYPE,
                generateFilterText(Arrays.asList(ItemResolverConstants.REMOTE_KEYWORD, ItemResolverConstants.FUNCTION)),
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Object Initializer Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getInitFunctionSnippet() {
        String snippet = "function init(${1}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}" +
                CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.NEW_OBJECT_INITIALIZER_TYPE,
                generateFilterText(Arrays.asList("init", ItemResolverConstants.FUNCTION)), snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Attach Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAttachFunctionSnippet() {
        String snippet = "public function attach(service ${1:s}, string? ${2:name} = ()) returns error? {"
                + CommonUtil.LINE_SEPARATOR + "\t${3}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.ATTACH_FUNCTION_TYPE,
                generateFilterText(Arrays.asList("attach", ItemResolverConstants.PUBLIC_KEYWORD,
                        ItemResolverConstants.FUNCTION)), snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Start Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getStartFunctionSnippet() {
        String snippet = "public function 'start() returns error? {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.START_FUNCTION_TYPE,
                generateFilterText(Arrays.asList(ItemResolverConstants.START_KEYWORD,
                        ItemResolverConstants.PUBLIC_KEYWORD, ItemResolverConstants.FUNCTION)),
                snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Graceful Stop Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getGracefulStopFunctionSnippet() {
        String snippet = "public function gracefulStop() returns error? {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.GRACEFUL_STOP_FUNCTION_TYPE,
                generateFilterText(Arrays.asList("gracefulStop", ItemResolverConstants.PUBLIC_KEYWORD,
                        ItemResolverConstants.FUNCTION)), snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Immediate Stop Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getImmediateStopFunctionSnippet() {
        String snippet = "public function immediateStop() returns error? {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.IMMEDIATE_STOP_FUNCTION_TYPE,
                generateFilterText(Arrays.asList("immediateStop", ItemResolverConstants.PUBLIC_KEYWORD,
                        ItemResolverConstants.FUNCTION)), snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get detach Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getDetachFunctionSnippet() {
        String snippet = "public function __detach(service ${1:s}) returns error? {"
                + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.DETACH_FUNCTION_TYPE,
                generateFilterText(Arrays.asList("detach", ItemResolverConstants.PUBLIC_KEYWORD,
                        ItemResolverConstants.FUNCTION)), snippet, ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Regular Expression Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRegularExpressionSnippet() {
        String snippet = "re `${1}`";
        return new SnippetBlock(ItemResolverConstants.REG_EXP, ItemResolverConstants.REG_EXP, snippet, 
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get string Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getStringSnippet() {
        String snippet = "string `${1}`";
        return new SnippetBlock(ItemResolverConstants.STRING_TEMP, ItemResolverConstants.STRING_TEMP, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get xml Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getXmlSnippet() {
        String snippet = "xml `${1}`";
        return new SnippetBlock(ItemResolverConstants.XML_TEMP, ItemResolverConstants.XML_TEMP, snippet,
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Paranthesis Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getParanthesisSnippet() {
        return new SnippetBlock(ItemResolverConstants.PARANTHESIS, ItemResolverConstants.PARANTHESIS, "(${1})",
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get Square Bracket Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getSquareBracketSnippet() {
        return new SnippetBlock(ItemResolverConstants.SQUARE_BRACKET, ItemResolverConstants.SQUARE_BRACKET, "[${1}]",
                ItemResolverConstants.SNIPPET_TYPE, Kind.SNIPPET);
    }

    /**
     * Get table keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTableKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.TABLE, ItemResolverConstants.TABLE, "table",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get service keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getServiceKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.SERVICE, ItemResolverConstants.SERVICE,
                "service", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get string keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getStringKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.STRING, ItemResolverConstants.STRING,
                "string", ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get xml keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getXMLKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.XML, ItemResolverConstants.XML, "xml",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get let keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getLetKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.LET, ItemResolverConstants.LET, "let",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get let keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getKeyKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.KEY, ItemResolverConstants.KEY, "key",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get trap keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTrapKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.TRAP, ItemResolverConstants.TRAP, "trap",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get trap keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getErrorKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.ERROR, ItemResolverConstants.ERROR, "error",
                ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    /**
     * Get map type Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getMapTypeSnippet() {
        return new SnippetBlock("map", "map", "map", ItemResolverConstants.TYPE, Kind.TYPE);
    }

    /**
     * Get Keyword Snippet Block.
     *
     * @param keyword keyword to be added
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getKeywordSnippet(String keyword) {
        return new SnippetBlock(keyword, keyword, keyword, ItemResolverConstants.KEYWORD_TYPE, Kind.KEYWORD);
    }

    private static String generateFilterText(List<String> filters) {
        return String.join(FILTER_TEXT_SEPARATOR, filters);
    }
}
