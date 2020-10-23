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
package org.ballerinalang.langserver.commons.completion;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.commons.LSContext;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

import java.util.List;

/**
 * Text Document Service context keys for the completion operation context.
 *
 * @since 0.95.5
 */
public class CompletionKeys {

    private CompletionKeys() {
    }

    public static final LSContext.Key<BLangNode> SCOPE_NODE_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<BLangNode> PREVIOUS_NODE_KEY
            = new LSContext.Key<>();
    @Deprecated
    public static final LSContext.Key<Integer> LOOP_COUNT_KEY
            = new LSContext.Key<>();
    @Deprecated
    public static final LSContext.Key<Boolean> CURRENT_NODE_TRANSACTION_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> TRANSACTION_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<CompletionCapabilities> CLIENT_CAPABILITIES_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Token> TOKEN_AT_CURSOR_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<NonTerminalNode> NODE_AT_CURSOR_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<Class<?>>> RESOLVER_CHAIN
            = new LSContext.Key<>();

    // Following key is used for the completion within the if else/ while condition context
    public static final LSContext.Key<Boolean> IN_CONDITION_CONTEXT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> TEXT_POSITION_IN_TREE = new LSContext.Key<>();
}
