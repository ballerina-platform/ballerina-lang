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
package org.ballerinalang.langserver.completions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.utils.completion.AnnotationAttachmentMetaInfo;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.tree.Node;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

import java.util.List;
import java.util.Stack;

/**
 * Text Document Service context keys for the completion operation context.
 *
 * @since 0.95.5
 */
public class CompletionKeys {

    private CompletionKeys() {
    }

    public static final LSContext.Key<BLangNode> SYMBOL_ENV_NODE_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<SymbolInfo>> VISIBLE_SYMBOLS_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Node> BLOCK_OWNER_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<BLangNode> PREVIOUS_NODE_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<String> NEXT_NODE_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> LOOP_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> CURRENT_NODE_TRANSACTION_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> TRANSACTION_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> INVOCATION_STATEMENT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<WorkspaceDocumentManager> DOC_MANAGER_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Stack<Token>> FORCE_CONSUMED_TOKENS_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<TokenStream> TOKEN_STREAM_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<ParserRuleContext> PARSER_RULE_CONTEXT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<CompletionCapabilities> CLIENT_CAPABILITIES_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<AnnotationAttachmentMetaInfo> ANNOTATION_ATTACHMENT_META_KEY
            = new LSContext.Key<>();
}
