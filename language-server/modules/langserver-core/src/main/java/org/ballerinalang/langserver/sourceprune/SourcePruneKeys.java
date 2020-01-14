/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.sourceprune;

import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.compiler.LSContext;

import java.util.List;

/**
 * Source prune context keys.
 * 
 * @since 0.995.0
 */
public class SourcePruneKeys {
    private SourcePruneKeys() {
    }

    public static final LSContext.Key<Integer> LEFT_PARAN_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> RIGHT_PARAN_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> LEFT_BRACE_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> ADD_SEMICOLON_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> RIGHT_BRACE_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> LEFT_BRACKET_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> RIGHT_BRACKET_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> GT_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> LT_COUNT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<Integer>> LHS_TRAVERSE_TERMINALS_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<Integer>> RHS_TRAVERSE_TERMINALS_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<Integer>> BLOCK_REMOVE_KW_TERMINALS_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> REMOVE_DEFINITION_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<Token>> TOKEN_LIST_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> CURSOR_TOKEN_INDEX_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> FORCE_CAPTURED_STATEMENT_WITH_PARENTHESIS_KEY
            = new LSContext.Key<>();
}
