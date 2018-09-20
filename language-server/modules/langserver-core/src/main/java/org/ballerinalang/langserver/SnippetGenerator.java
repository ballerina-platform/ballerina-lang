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

import org.ballerinalang.langserver.common.utils.CommonUtil;

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
        return new SnippetBlock("abort;");
    }

    /**
     * Get Annotation Definition statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getAnnotationDefSnippet() {
        String snippet = "annotation<${1:attachmentPoint}> ${2:name};";

        return new SnippetBlock(snippet);
    }
    

    /**
     * Get Bind statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBindSnippet() {
        return new SnippetBlock("bind ");
    }

    /**
     * Get Break statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getBreakSnippet() {
        return new SnippetBlock("break;");
    }

    /**
     * Get Match Expression Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getMatchExpressionSnippet() {
        String snippet = "but {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "};";
        
        return new SnippetBlock(snippet);
    }

    /**
     * Get Check Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getCheckKeywordSnippet() {
        return new SnippetBlock("check ");
    }

    /**
     * Get Import Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getImportKeywordSnippet() {
        return new SnippetBlock("import ");
    }

    /**
     * Get Continue Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getContinueStatmentSnippet() {
        return new SnippetBlock("continue;");
    }

    /**
     * Get Endpoint Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getEndpointSnippet() {
        String snippet = "endpoint ${1:http:Listener} ${2:listener} {" + CommonUtil.LINE_SEPARATOR + "\t${3}"
                + CommonUtil.LINE_SEPARATOR + "};";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Foreach Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getForeachSnippet() {
        String snippet = "foreach ${1:item} in ${2:itemList} {" + CommonUtil.LINE_SEPARATOR + "\t${3}"
                + CommonUtil.LINE_SEPARATOR + "}";
        
        return new SnippetBlock(snippet);
    }

    /**
     * Get ForkJoin Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getForkJoinStatementSnippet() {
        String snippet = "fork {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR
                + "} join (${2:all}) (map ${3:results}) {" + CommonUtil.LINE_SEPARATOR + "\t${4}"
                + CommonUtil.LINE_SEPARATOR + "}";
        
        return new SnippetBlock(snippet);
    }

    /**
     * Get Function Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFunctionDefSnippet() {
        String snippet = "function ${1:name}(${2}) {" + CommonUtil.LINE_SEPARATOR + "\t${3}"
                + CommonUtil.LINE_SEPARATOR + "}";
        
        return new SnippetBlock(snippet);
    }

    /**
     * Get Function Signature Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getFunctionSignatureSnippet() {
        String snippet = "function ${1:name}(${2});";
        
        return new SnippetBlock(snippet);
    }

    /**
     * Get If Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIfStatementSnippet() {
        String snippet = "if (${1:true}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";
        
        return new SnippetBlock(snippet);
    }

    /**
     * Get Lengthof Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getLengthofKeywordSnippet() {        
        return new SnippetBlock("lengthof ");
    }

    /**
     * Get Lock Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getLockStatementSnippet() {
        String snippet = "lock {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}";      
        return new SnippetBlock(snippet);
    }

    /**
     * Get Main Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getMainFunctionSnippet() {
        String snippet = "public function main(string... args) {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "}";      
        return new SnippetBlock(snippet);
    }

    /**
     * Get Match Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getMatchStatementSnippet() {     
        return new SnippetBlock("match ");
    }

    /**
     * Get Namespace Declaration Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getNamespaceDeclarationSnippet() {
        String snippet = "xmlns \"${1}\" as ${2:ns};";

        return new SnippetBlock(snippet);
    }

    /**
     * Get ObjectConstructor Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getObjectConstructorSnippet() {
        String snippet = "public new(${1:args}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR
                + "}";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Object Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getObjectDefinitionSnippet() {
        String snippet = "type ${1:ObjectName} object {" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "};";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Public Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getPublicKeywordSnippet() {
        return new SnippetBlock("public ");
    }

    /**
     * Get Type Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTypeKeywordSnippet() {
        return new SnippetBlock("type ");
    }

    /**
     * Get Record Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRecordDefinitionSnippet() {
        String snippet = "type ${1:RecordName} record {" + CommonUtil.LINE_SEPARATOR + "\t${2}"
                + CommonUtil.LINE_SEPARATOR + "};";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Resource Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getResourceDefinitionSnippet() {
        String snippet = "${1:newResource} (endpoint ${2:caller}, ${3:http:Request request}) {"
                + CommonUtil.LINE_SEPARATOR + "\t${4}" + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Retry Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getRetryStatementSnippet() {
        return new SnippetBlock("retry;");
    }

    /**
     * Get Return Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getReturnStatementSnippet() {
        return new SnippetBlock("return;");
    }

    /**
     * Get Service Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getServiceDefSnippet() {
        String snippet = "service<${1:http:Service}> ${2:serviceName} bind { port: 9090 } {"
                + CommonUtil.LINE_SEPARATOR + "\t${3:newResource} (endpoint ${4:caller}, "
                + "${5:http:Request request}) {" + CommonUtil.LINE_SEPARATOR + "\t}" + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Web Socket Service Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSocketServiceDefSnippet() {
        String snippet = "service<http:WebSocketService> ${1:serviceName} bind { port: 9090 } {"
                + CommonUtil.LINE_SEPARATOR + "\tonOpen(endpoint caller) {"
                + CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}" + CommonUtil.LINE_SEPARATOR
                + "\tonText(endpoint caller, string text, boolean final) {" + CommonUtil.LINE_SEPARATOR + "\t\t"
                + CommonUtil.LINE_SEPARATOR + "\t}" + CommonUtil.LINE_SEPARATOR
                + "\tonClose(endpoint caller, int statusCode, string reason) {" + CommonUtil.LINE_SEPARATOR
                + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}" + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Web Sub Service Definition Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWebSubServiceDefSnippet() {
        String snippet = "service<websub:Service> ${1:websubSubscriber} bind {port: 9090} {" + CommonUtil.LINE_SEPARATOR
                + "\tonIntentVerification(endpoint caller, websub:IntentVerificationRequest request) {"
                + CommonUtil.LINE_SEPARATOR + "\t\t" + CommonUtil.LINE_SEPARATOR + "\t}" + CommonUtil.LINE_SEPARATOR
                + "\tonNotification(websub:Notification notification) {" + CommonUtil.LINE_SEPARATOR + "\t\t"
                + CommonUtil.LINE_SEPARATOR + "\t}" + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Throw Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getThrowStatementSnippet() {
        return new SnippetBlock("throw ");
    }

    /**
     * Get Transaction Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTransactionStatementSnippet() {
        String snippet = "transaction with retries = ${1:1}, oncommit = ${2:onCommitFunction}, "
                + "onabort = ${3:onAbortFunction} " + "{" + CommonUtil.LINE_SEPARATOR
                + "\t${4}" + CommonUtil.LINE_SEPARATOR + "} onretry {" + CommonUtil.LINE_SEPARATOR + "\t${5}"
                + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Try-Catch Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getTryCatchStatementSnippet() {
        String snippet = "try {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR
                + "} catch (${2:error} ${3:err}) {" + CommonUtil.LINE_SEPARATOR + "\t${4}"
                + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Var Keyword Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getVarKeywordSnippet() {
        return new SnippetBlock("var ");
    }

    /**
     * Get While Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWhileStatementSnippet() {
        String snippet = "while (${1:true}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Worker Trigger Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWorkerTriggerStatementSnippet() {
        String snippet = "${1:var1} -> ${2:w1};";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Worker Reply Statement Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWorkerReplyStatementSnippet() {
        String snippet = "${1:var1} <- ${2:w1};";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Worker Declaration Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getWorkerDeclarationSnippet() {
        String snippet = "worker ${1:name} {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}";

        return new SnippetBlock(snippet);
    }
    
    // Iterable Operations Snippets

    /**
     * Get Foreach Iterable Operation Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableForeachSnippet() {
        String snippet = "foreach((%params%) => {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "});";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Map Iterable Operation Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableMapSnippet() {
        String snippet = "map((%params%) => (any) {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "});";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Map Iterable Operation Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableFilterSnippet() {
        String snippet = "filter((%params%) => (boolean) {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                + CommonUtil.LINE_SEPARATOR + "});";

        return new SnippetBlock(snippet);
    }

    /**
     * Get Count Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableCountSnippet() {
        return new SnippetBlock("count();");
    }

    /**
     * Get Min Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableMinSnippet() {
        return new SnippetBlock("min();");
    }

    /**
     * Get Max Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableMaxSnippet() {
        return new SnippetBlock("max();");
    }

    /**
     * Get Average Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableAverageSnippet() {
        return new SnippetBlock("average();");
    }

    /**
     * Get Sum Iterable Function Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableSumSnippet() {
        return new SnippetBlock("sum();");
    }

    // Iterable operators' lambda function parameters

    /**
     * Get Params on Map Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableOnMapParamSnippet() {
        return new SnippetBlock("string k, any v");
    }

    /**
     * Get Params on Json Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableOnJsonParamSnippet() {
        return new SnippetBlock("json v");
    }

    /**
     * Get Params on XML Snippet Block.
     *
     * @return {@link SnippetBlock}     Generated Snippet Block
     */
    public static SnippetBlock getIterableOnXmlParamSnippet() {
        return new SnippetBlock("xml v");
    }
}
