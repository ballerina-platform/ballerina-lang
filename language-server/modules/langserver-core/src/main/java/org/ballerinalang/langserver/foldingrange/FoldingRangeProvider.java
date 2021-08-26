/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.foldingrange;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.langserver.commons.FoldingRangeContext;
import org.eclipse.lsp4j.FoldingRange;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provider class of folding ranges.
 *
 * @since 2.0.0
 */
public class FoldingRangeProvider {

    /**
     * Returns the list of folding ranges for the given syntax tree.
     *
     * @param context {@link FoldingRangeContext}
     * @return the list of folding ranges
     */
    public static List<FoldingRange> getFoldingRange(FoldingRangeContext context) {
        Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
        if (syntaxTree.isEmpty()) {
            return Collections.emptyList();
        }

        ModulePartNode modulePartNode = syntaxTree.get().rootNode();
        FoldingRangeFinder foldingRangeFinder = new FoldingRangeFinder(context.getLineFoldingOnly());
        return foldingRangeFinder.getFoldingRange(modulePartNode);
    }
}
