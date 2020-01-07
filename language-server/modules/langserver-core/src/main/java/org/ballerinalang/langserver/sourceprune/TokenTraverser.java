/*
  Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.ballerinalang.langserver.sourceprune;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.TokenStream;

import java.util.List;

/**
 * Token traverser.
 * 
 * @since 1.1.1
 */
public interface TokenTraverser {
    /**
     * Traverse the tokens.
     *
     * @param tokenStream token stream
     * @param tokenIndex token index
     * @return removed tokens
     */
    List<CommonToken> traverse(TokenStream tokenStream, int tokenIndex);
}
