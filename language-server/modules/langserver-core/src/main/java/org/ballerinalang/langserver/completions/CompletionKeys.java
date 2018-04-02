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

import org.ballerinalang.langserver.LSContext;
import org.ballerinalang.model.tree.Node;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

import java.util.List;

/**
 * Text Document Service context keys for the completion operation context.
 * @since 0.95.5
 */
public class CompletionKeys {
    public static final LSContext.Key<BLangNode> SYMBOL_ENV_NODE_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<SymbolInfo>> VISIBLE_SYMBOLS_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Node> BLOCK_OWNER_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<BLangNode> PREVIOUS_NODE_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> LOOP_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> CURRENT_NODE_TRANSACTION_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> TRANSACTION_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> INVOCATION_STATEMENT_KEY
            = new LSContext.Key<>();
    
    // Meta context Keys
    public static final LSContext.Key<Boolean> META_CONTEXT_IS_ENDPOINT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<String> META_CONTEXT_ENDPOINT_NAME_KEY
            = new LSContext.Key<>();
}
