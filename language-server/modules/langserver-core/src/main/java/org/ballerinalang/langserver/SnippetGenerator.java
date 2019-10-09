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
package org.ballerinalang.langserver;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.SnippetBlock.SnippetType;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.IntStream;

/**
 * Generates Snippets for language constructs.
 *
 * @since 0.982.0
 */
public class SnippetGenerator {

    private SnippetGenerator() {
    }

    /**
     * Get Abort statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAbortSnippet() {
        return new SnippetBlock(ItemResolverConstants.ABORT, "abort;", ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Annotation Definition statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAnnotationDefSnippet() {
        String snippet = "annotation ${1:typeName} ${2:name} on ${3:attachmentPoint};";
        return new SnippetBlock(ItemResolverConstants.ANNOTATION, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get On keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getOnSnippet() {
        return new SnippetBlock(ItemResolverConstants.ON, "on ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get new keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getNewKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.NEW, "new ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get abstract keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAbstractKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.ABSTRACT, "abstract ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get client keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getClientKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.CLIENT, "client ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get external keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getExternalKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.EXTERNAL, "external;", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get typeof keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTypeofKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.TYPEOF, "typeof ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Break statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBreakSnippet() {
        return new SnippetBlock(ItemResolverConstants.BREAK, "break;", ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Match Expression Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getMatchExpressionSnippet() {
        String snippet = "but {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "};";
        return new SnippetBlock(ItemResolverConstants.BUT, snippet, ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Check Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getCheckKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.CHECK_KEYWORD, "check ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get checkpanic Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getCheckPanicKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.CHECKPANIC_KEYWORD, "checkpanic ",
                ItemResolverConstants.KEYWORD_TYPE, SnippetType.KEYWORD);
    }

    /**
     * Get Wait Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWaitKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.WAIT_KEYWORD, "wait ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Start Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getStartKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.START_KEYWORD, "start ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Flush Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFlushKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.FLUSH_KEYWORD, "flush ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Import Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getImportKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.IMPORT, "import ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Function Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFunctionKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.FUNCTION, "function ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Continue Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getContinueStatmentSnippet() {
        return new SnippetBlock(ItemResolverConstants.CONTINUE, "continue;", ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Listener Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getListenerKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.LISTENER_KEYWORD, "listener ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Returns Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getReturnsKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.RETURNS_KEYWORD, "returns ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Untaint Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getUntaintKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.UNTAINTED_KEYWORD, "untainted ",
                ItemResolverConstants.KEYWORD_TYPE, SnippetType.KEYWORD);
    }

    /**
     * Get Foreach Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getForeachSnippet() {
        String snippet = "foreach ${1:var} ${2:item} in ${3:itemList} {" + CommonUtil.LINE_SEPARATOR + "\t${4}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.FOREACH, snippet, ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Fork Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getForkStatementSnippet() {
        String snippet = "fork {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.FORK, snippet, ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Function Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFunctionDefSnippet() {
        String snippet = "function ${1:name}(${2}) {" + CommonUtil.LINE_SEPARATOR + "\t${3}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.FUNCTION, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get Function Signature Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFunctionSignatureSnippet() {
        String snippet = "function ${1:name}(${2});";
        return new SnippetBlock(ItemResolverConstants.FUNCTION_SIGNATURE, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get If Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIfStatementSnippet() {
        String snippet = "if (${1:true}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.IF, snippet, ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Else If Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getElseIfStatementSnippet() {
        String snippet = "else if (${1:true}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.ELSE_IF, snippet, ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Else Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getElseStatementSnippet() {
        String snippet = "else {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.ELSE, snippet, ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Lengthof Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getLengthofKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.LENGTHOF, "lengthof ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Lock Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getLockStatementSnippet() {
        String snippet = "lock {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.LOCK, snippet, ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Main Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getMainFunctionSnippet() {
        String snippet = "public function main() {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.MAIN_FUNCTION, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get Match Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getMatchStatementSnippet() {
        return new SnippetBlock(ItemResolverConstants.MATCH, "match ", ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Namespace Declaration Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getNamespaceDeclarationSnippet() {
        String snippet = "xmlns \"${1}\" as ${2:ns};";

        return new SnippetBlock(ItemResolverConstants.XMLNS, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Object Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getObjectDefinitionSnippet() {
        String snippet = "type ${1:ObjectName} object {" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "};";

        return new SnippetBlock(ItemResolverConstants.OBJECT_TYPE, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get Public Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getPublicKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.PUBLIC_KEYWORD, "public ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Private Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getPrivateKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.PRIVATE_KEYWORD, "private ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Type Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTypeKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.TYPE_TYPE, "type ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get Annotation Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAnnotationKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.ANNOTATION_TYPE, "annotation ",
                ItemResolverConstants.KEYWORD_TYPE, SnippetType.KEYWORD);
    }

    /**
     * Get Record Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRecordDefinitionSnippet() {
        String snippet = "type ${1:RecordName} record {" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "};";

        return new SnippetBlock(ItemResolverConstants.RECORD_TYPE, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get Closed Record Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getClosedRecordDefinitionSnippet() {
        String snippet = "type ${1:RecordName} record {|" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "|};";

        return new SnippetBlock(ItemResolverConstants.CLOSED_RECORD_TYPE, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get HTTP Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getResourceDefinitionSnippet() {
        ImmutablePair<String, String> httpImport = new ImmutablePair<>("ballerina", "http");
        String snippet = "resource function ${1:newResource}(http:Caller ${2:caller}, ${3:http:Request request}) {"
                + CommonUtil.LINE_SEPARATOR + "\t${4}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.HTTP_RESOURCE, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET, httpImport);
    }

    /**
     * Get Common/Generic Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getCommonResourceDefinitionSnippet() {
        String snippet = "resource function ${1:newResource}(${2}) {"
                + CommonUtil.LINE_SEPARATOR + "\t${3}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.RESOURCE, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get gRPC Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getGRPCResourceDefinitionSnippet() {
        Pair<String, String> httpImport = new ImmutablePair<>("ballerina", "http");
        String snippet = "resource function ${1:newResource}(grpc:Caller ${2:caller}, ${3:string request}) {"
                + CommonUtil.LINE_SEPARATOR + "\t${4}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.RESOURCE, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET, httpImport);
    }

    //--------------------------------------------WebSocket Service-----------------------------------------------------

    /**
     * Get WebSocket OnOpen Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketResourceOnOpenSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onOpen", "websocket onOpen", Collections.singletonList(
                "http:WebSocketCaller ${#:caller}"), new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket OnText Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketResourceOnTextSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onText", "websocket onText", Arrays.asList(
                "http:WebSocketCaller ${#:caller}", "string ${#:data}", "boolean ${#:finalFrame}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket OnBinary Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketResourceOnBinarySnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onBinary", "websocket onBinary", Arrays.asList(
                "http:WebSocketCaller ${#:caller}", "byte[] ${#:data}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket OnPing Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketResourceOnPingSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onPing", "websocket onPing", Arrays.asList(
                "http:WebSocketCaller ${#:caller}", "byte[] ${#:data}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket OnPong Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketResourceOnPongSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onPong", "websocket onPong", Arrays.asList(
                "http:WebSocketCaller ${#:caller}", "byte[] ${#:data}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket OnIdleTimeout Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketResourceOnIdleTimeoutSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onIdleTimeout", "websocket onIdleTimeout",
                                                             Collections
                                                                     .singletonList("http:WebSocketCaller ${#:caller}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket onError Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketResourceOnErrorSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onError", "websocket onError",
                                                             Arrays
                                                                     .asList("http:WebSocketCaller ${#:caller}",
                                                                             "error ${#:err}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket OnClose Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketResourceOnCloseSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onClose", "websocket onClose", Arrays.asList(
                "http:WebSocketCaller ${#:caller}", "int ${#:statusCode}", "string ${#:reason}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    //----------------------------------------WebSocket Client Service--------------------------------------------------

    /**
     * Get WebSocket Client Service OnText Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketClientServiceResourceOnTextSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onText", "websocketClient onText", Arrays.asList(
                "http:WebSocketClient ${#:wsEp}", "string ${#:data}", "boolean ${#:finalFrame}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket Client Service OnBinary Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketClientServiceResourceOnBinarySnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onBinary", "websocketClient onBinary", Arrays.asList(
                "http:WebSocketClient ${#:wsEp}", "byte[] ${#:data}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket Client Service OnPing Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketClientServiceResourceOnPingSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onPing", "websocketClient onPing", Arrays.asList(
                "http:WebSocketClient ${#:wsEp}", "byte[] ${#:data}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket Client Service OnPong Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketClientServiceResourceOnPongSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onPong", "websocketClient onPong", Arrays.asList(
                "http:WebSocketClient ${#:wsEp}", "byte[] ${#:data}"), new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket Client Service OnIdleTimeout Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketClientServiceResourceOnIdleTimeoutSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onIdleTimeout", "websocketClient onIdleTimeout",
                                                             Collections
                                                                     .singletonList("http:WebSocketClient ${#:wsEp}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket Client Service onError Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketClientServiceResourceOnErrorSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onError", "websocketClient onError",
                                                             Arrays.asList("http:WebSocketClient ${#:wsEp}",
                                                                           "error ${#:err}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSocket Client Service OnClose Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketClientServiceResourceOnCloseSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onClose", "websocketClient onClose", Arrays.asList(
                "http:WebSocketClient ${#:wsEp}", "int ${#:statusCode}", "string ${#:reason}"),
                                                             new ImmutablePair<>("ballerina", "http"));
    }

    /**
     * Get WebSub OnIntentVerification Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSubResourceOnIntentVerificationSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onIntentVerification", "onIntentVerification",
                                                             Arrays.asList("websub:Caller ${#:caller}",
                                                                           "websub:IntentVerificationRequest " +
                                                                                   "${#:request}"),
                                                             new ImmutablePair<>("ballerina", "websub"));
    }

    //---------------------------------------------WebSub Service-------------------------------------------------------

    /**
     * Get WebSub OnNotification Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSubResourceOnNotificationSnippet() {
        return SnippetGenerator.getResourceDefinitionSnippet("onNotification", "onNotification",
                                                             Collections.singletonList(
                                                                     "websub:Notification ${#:notification}"),
                                                             new ImmutablePair<>("ballerina", "websub"));
    }

    /**
     * Get Resource Definition Snippet Block.
     *
     * @param name             name of the snippet
     * @param label             label of the snippet
     * @param params           params for the snippet
     * @param orgToAliasImport import
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    private static SnippetBlock getResourceDefinitionSnippet(String name, String label, List<String> params,
                                                             Pair<String, String> orgToAliasImport) {
        StringJoiner paramsJoiner = new StringJoiner(",");
        IntStream.range(0, params.size()).forEach(
                i -> paramsJoiner.add(params.get(i).replace("#", String.valueOf(i + 1))));
        String snippet = "resource function " + name + "(" + paramsJoiner.toString() + ") {"
                + CommonUtil.LINE_SEPARATOR + "\t${" + (1 + params.size()) + "}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(label + " " + ItemResolverConstants.RESOURCE, snippet,
                                ItemResolverConstants.SNIPPET_TYPE, SnippetType.SNIPPET, orgToAliasImport);
    }

    /**
     * Get Retry Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRetryStatementSnippet() {
        return new SnippetBlock(ItemResolverConstants.RETRY, "retry;", ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Return Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getReturnStatementSnippet() {
        return new SnippetBlock(ItemResolverConstants.RETURN, "return ", ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Service Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getServiceDefSnippet() {
        ImmutablePair<String, String> httpImport = new ImmutablePair<>("ballerina", "http");
        String snippet = "service ${1:serviceName} on new http:Listener(8080) {"
                + CommonUtil.LINE_SEPARATOR + "\tresource function ${2:newResource}(http:Caller ${3:caller}, "
                + "http:Request ${5:request}) {" + CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR +
                "\t}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.SERVICE_HTTP, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET, httpImport);
    }

    /**
     * Get Service Variable Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getServiceVarSnippet() {
        String snippet = "service {"
                + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "};";
        return new SnippetBlock(ItemResolverConstants.SERVICE, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get Web Socket Service Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketServiceDefSnippet() {
        ImmutablePair<String, String> httpImport = new ImmutablePair<>("ballerina", "http");
        String snippet = "service ${1:serviceName} on new http:Listener(9090) {" + CommonUtil.LINE_SEPARATOR +
                "\tresource function onOpen(http:WebSocketCaller caller) {"
                + CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}" + CommonUtil.LINE_SEPARATOR +
                "\tresource function onText(http:WebSocketCaller caller, string data, boolean finalFrame) {"
                + CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}" + CommonUtil.LINE_SEPARATOR +
                "\tresource function onClose(http:WebSocketCaller caller, int statusCode, string reason) {"
                + CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.SERVICE_WEBSOCKET, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET, httpImport);
    }

    /**
     * Get Web Socket Client Service Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketClientServiceDefSnippet() {
        ImmutablePair<String, String> httpImport = new ImmutablePair<>("ballerina", "http");
        String snippet = "service ${1:clientCallbackService} = @http:WebSocketServiceConfig {} service {" +
                CommonUtil.LINE_SEPARATOR +
                "\tresource function onText(http:WebSocketClient wsEp, string text) {"
                + CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}" + CommonUtil.LINE_SEPARATOR +
                "\tresource function onClose(http:WebSocketClient wsEp, int statusCode, string reason) {"
                + CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}"
                + CommonUtil.LINE_SEPARATOR + "};";
        return new SnippetBlock(ItemResolverConstants.SERVICE_WEBSOCKET_CLIENT, snippet,
                                ItemResolverConstants.SNIPPET_TYPE, SnippetType.SNIPPET, httpImport);
    }

    /**
     * Get Web Sub Service Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSubServiceDefSnippet() {
        ImmutablePair<String, String> websubImport = new ImmutablePair<>("ballerina", "websub");
        String snippet = "service ${1:websubSubscriber} on new websub:Listener(9092) {" + CommonUtil.LINE_SEPARATOR +
                "\tresource function onIntentVerification(websub:Caller caller, websub:IntentVerificationRequest " +
                "request) {" + CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}" +
                CommonUtil.LINE_SEPARATOR + "\tresource function onNotification(websub:Notification notification) {" +
                CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}" +
                CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.SERVICE_WEBSUB, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET, websubImport);
    }

    /**
     * Get gRPC Service Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getGRPCServiceDefSnippet() {
        ImmutablePair<String, String> grpcImport = new ImmutablePair<>("ballerina", "grpc");
        String snippet = "service ${1:serviceName} on new grpc:Listener(9092) {" + CommonUtil.LINE_SEPARATOR +
                "\tresource function ${2:newResource}(grpc:Caller caller, ${3:string} request) {" +
                CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}" +
                CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.SERVICE_GRPC, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET, grpcImport);
    }

    /**
     * Get Panic Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getPanicStatementSnippet() {
        return new SnippetBlock(ItemResolverConstants.PANIC, "panic ", ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Const Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getConstKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.CONST_KEYWORD, "const ", ItemResolverConstants.KEYWORD_TYPE,
                SnippetType.KEYWORD);
    }

    /**
     * Get Final Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFinalKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.FINAL_KEYWORD, "final ", ItemResolverConstants.KEYWORD_TYPE,
                SnippetType.KEYWORD);
    }

    /**
     * Get Transaction Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTransactionStatementSnippet() {
        String snippet = "transaction with retries = ${1:0} {" + CommonUtil.LINE_SEPARATOR
                + "\t${2}" + CommonUtil.LINE_SEPARATOR + "} onretry {" + CommonUtil.LINE_SEPARATOR + "\t${3}"
                + CommonUtil.LINE_SEPARATOR + "} committed {" + CommonUtil.LINE_SEPARATOR + "\t${4}"
                + CommonUtil.LINE_SEPARATOR + "} aborted {" + CommonUtil.LINE_SEPARATOR + "\t${5}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.TRANSACTION, snippet, ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Trap Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTrapSnippet() {
        String snippet = "trap ";
        return new SnippetBlock(ItemResolverConstants.TRAP, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get Var Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getVarKeywordSnippet() {
        return new SnippetBlock(ItemResolverConstants.VAR_KEYWORD, "var ", ItemResolverConstants.KEYWORD_TYPE,
                                SnippetType.KEYWORD);
    }

    /**
     * Get While Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWhileStatementSnippet() {
        String snippet = "while (${1:true}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.WHILE,
                                snippet,
                                ItemResolverConstants.STATEMENT_TYPE,
                                SnippetType.STATEMENT);
    }

    /**
     * Get Worker Declaration Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWorkerDeclarationSnippet() {
        String snippet = "worker ${1:name} {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(ItemResolverConstants.WORKER, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    /**
     * Get Error Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getErrorDefinitionSnippet() {
        String snippet = "error ${1:name} = error(\"${2:errorCode}\", message = \"${3}\");";
        return new SnippetBlock(ItemResolverConstants.ERROR, snippet, ItemResolverConstants.SNIPPET_TYPE,
                                SnippetType.SNIPPET);
    }

    // Iterable Operations Snippets

    /**
     * Get Foreach Iterable Operation Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableForeachSnippet() {
        String snippet = "foreach(function(%params%) {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "});";

        return new SnippetBlock(ItemResolverConstants.ITR_FOREACH_LABEL, snippet, "", SnippetType.SNIPPET);
    }

    /**
     * Get Map Iterable Operation Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableMapSnippet() {
        String snippet = "map(function (%params%) returns any {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "})";

        return new SnippetBlock(ItemResolverConstants.ITR_MAP_LABEL, snippet, "", SnippetType.SNIPPET);
    }

    /**
     * Get Map Iterable Operation Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableFilterSnippet() {
        String snippet = "filter(function(%params%) returns (boolean) {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "})";

        return new SnippetBlock(ItemResolverConstants.ITR_FILTER_LABEL, snippet, "", SnippetType.SNIPPET);
    }

    /**
     * Get Count Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableCountSnippet() {
        return new SnippetBlock(ItemResolverConstants.ITR_COUNT_LABEL, "count()", "", SnippetType.SNIPPET);
    }

    /**
     * Get Length Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinLengthSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_LENGTH_LABEL, "length()", "", SnippetType.SNIPPET);
    }

    /**
     * Get clone Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinIsCloneSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_CLONE_LABEL, "clone()", "", SnippetType.SNIPPET);
    }

    /**
     * Get Freeze Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinFreezeSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_FREEZE_LABEL, "freeze()", "", SnippetType.SNIPPET);
    }

    /**
     * Get isFrozen Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinIsFrozenSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_IS_FROZEN_LABEL, "isFrozen()", "", SnippetType.SNIPPET);
    }

    /**
     * Get stamp Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinStampSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_STAMP_LABEL, "stamp(${1})", "", SnippetType.SNIPPET);
    }

    /**
     * Get hasKey Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinHasKeySnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_HASKEY_LABEL, "hasKey(${1})", "", SnippetType.SNIPPET);
    }

    /**
     * Get remove Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinRemoveSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_REMOVE_LABEL, "remove(${1})", "", SnippetType.SNIPPET);
    }

    /**
     * Get values Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinValuesSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_GET_VALUES_LABEL, "values()", "", SnippetType.SNIPPET);
    }

    /**
     * Get keys Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinKeysSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_KEYS_LABEL, "keys()", "", SnippetType.SNIPPET);
    }

    /**
     * Get clear Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinClearSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_CLEAR_LABEL, "clear()", "", SnippetType.SNIPPET);
    }

    /**
     * Get convert Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinConvertSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_CONVERT_LABEL, "convert(${1})", "", SnippetType.SNIPPET);
    }

    /**
     * Get isNaN Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinIsNaNSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_IS_NAN_LABEL, "isNaN()", "", SnippetType.SNIPPET);
    }

    /**
     * Get isFinite Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinIsFiniteSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_IS_FINITE_LABEL, "isFinite()", "", SnippetType.SNIPPET);
    }

    /**
     * Get isInFinite Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinIsInFiniteSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_IS_INFINITE_LABEL, "isInfinite()", "",
                SnippetType.SNIPPET);
    }

    /**
     * Get detail Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinDetailSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_DETAIL_LABEL, "detail()", "",
                SnippetType.SNIPPET);
    }

    /**
     * Get reason Builtin Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBuiltinReasonSnippet() {
        return new SnippetBlock(ItemResolverConstants.BUILTIN_REASON_LABEL, "reason()", "",
                SnippetType.SNIPPET);
    }

    /**
     * Get Select Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableSelectSnippet() {
        return new SnippetBlock(ItemResolverConstants.ITR_SELECT_LABEL, "select(${1:functionReference})", "",
                SnippetType.SNIPPET);
    }

    /**
     * Get Min Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableMinSnippet() {
        return new SnippetBlock(ItemResolverConstants.ITR_MIN_LABEL, "min()", "", SnippetType.SNIPPET);
    }

    /**
     * Get Max Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableMaxSnippet() {
        return new SnippetBlock(ItemResolverConstants.ITR_MAX_LABEL, "max()", "", SnippetType.SNIPPET);
    }

    /**
     * Get Average Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableAverageSnippet() {
        return new SnippetBlock(ItemResolverConstants.ITR_AVERAGE_LABEL, "average()", "", SnippetType.SNIPPET);
    }

    /**
     * Get Sum Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableSumSnippet() {
        return new SnippetBlock(ItemResolverConstants.ITR_SUM_LABEL, "sum()", "", SnippetType.SNIPPET);
    }

    // Iterable operators' lambda function parameters

    /**
     * Get Params on Map Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableOnMapParamSnippet() {
        return new SnippetBlock("(%key%, %value%) entry", SnippetType.SNIPPET);
    }

    /**
     * Get Params on Json Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableOnJsonParamSnippet() {
        return new SnippetBlock("json v", SnippetType.SNIPPET);
    }

    /**
     * Get Params on XML Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableOnXmlParamSnippet() {
        return new SnippetBlock("xml v", SnippetType.SNIPPET);
    }

    /**
     * Get Remote function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRemoteFunctionSnippet() {
        String snippet = "public remote function ${1:name}(${2}) {" + CommonUtil.LINE_SEPARATOR + "\t${3}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.REMOTE_FUNCTION_TYPE, snippet,
                ItemResolverConstants.SNIPPET_TYPE, SnippetType.SNIPPET);
    }

    /**
     * Get Object Initializer Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getInitFunctionSnippet() {
        String snippet = "public function __init(${1:any arg}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}" +
                CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.NEW_OBJECT_INITIALIZER_TYPE, snippet,
                ItemResolverConstants.SNIPPET_TYPE, SnippetType.SNIPPET);
    }

    /**
     * Get Attach Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAttachFunctionSnippet() {
        String snippet = "public function __attach(service ${1:s}, string? ${2:name} = ()) returns error? {"
                + CommonUtil.LINE_SEPARATOR + "\t${3}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.ATTACH_FUNCTION_TYPE, snippet, ItemResolverConstants.SNIPPET_TYPE,
                SnippetType.SNIPPET);
    }

    /**
     * Get Start Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getStartFunctionSnippet() {
        String snippet = "public function __start() returns error? {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.START_FUNCTION_TYPE, snippet, ItemResolverConstants.SNIPPET_TYPE,
                SnippetType.SNIPPET);
    }

    /**
     * Get Graceful Stop Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getGracefulStopFunctionSnippet() {
        String snippet = "public function __gracefulStop() returns error? {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.GRACEFUL_STOP_FUNCTION_TYPE, snippet,
                ItemResolverConstants.SNIPPET_TYPE, SnippetType.SNIPPET);
    }

    /**
     * Get Immediate Stop Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getImmediateStopFunctionSnippet() {
        String snippet = "public function __immediateStop() returns error? {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.IMMEDIATE_STOP_FUNCTION_TYPE, snippet,
                ItemResolverConstants.SNIPPET_TYPE, SnippetType.SNIPPET);
    }

    /**
     * Get detach Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getDetachFunctionSnippet() {
        String snippet = "public function __detach(service ${1:s}) returns error? {"
                + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";
        return new SnippetBlock(ItemResolverConstants.DETACH_FUNCTION_TYPE, snippet,
                ItemResolverConstants.SNIPPET_TYPE, SnippetType.SNIPPET);
    }
}
