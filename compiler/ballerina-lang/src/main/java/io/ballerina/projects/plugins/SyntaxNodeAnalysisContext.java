/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.plugins;

import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.model.tree.Node;

/**
 * This class provides a context for the syntax node analysis task.
 *
 * @see CodeAnalysisContext
 * @since 2.0.0
 */
public class SyntaxNodeAnalysisContext {

    /**
     * Returns the syntax node that matches with one of the specified {@code SyntaxKind}s.
     *
     * @return the syntax node on which the {@code AnalysisTask<SyntaxNodeAnalysisContext>} executed
     */
    public Node node() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@code Symbol} of the declaration that encloses the syntax node.
     *
     * @return the {@code Symbol} of the declaration that encloses the syntax node
     */
    public Symbol symbol() {
        throw new UnsupportedOperationException();
    }
}
