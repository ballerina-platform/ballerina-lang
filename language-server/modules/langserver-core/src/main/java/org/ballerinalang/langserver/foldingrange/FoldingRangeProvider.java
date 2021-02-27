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
import io.ballerina.projects.Document;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.FoldingRangeContext;
import org.eclipse.lsp4j.FoldingRange;

import java.nio.file.Path;
import java.util.ArrayList;
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
     * @param foldingRangeContext {@link FoldingRangeContext}
     * @return the list of folding ranges
     */
    public static List<FoldingRange> getFoldingRange(FoldingRangeContext foldingRangeContext) {
        SyntaxTree syntaxTree = getSyntaxTree(foldingRangeContext);
        if (syntaxTree == null) {
            return Collections.emptyList();
        }

        ModulePartNode modulePartNode = syntaxTree.rootNode();
        FoldingRangeFinder foldingRangeFinder = new FoldingRangeFinder(foldingRangeContext.getLineFoldingOnly());
        return new ArrayList<>(foldingRangeFinder.getFoldingRange(modulePartNode));
    }

    /**
     * Returns the syntax tree instance for a given folding range context.
     *
     * @param foldingRangeContext {@link FoldingRangeContext}
     * @return {@link SyntaxTree}
     */
    private static SyntaxTree getSyntaxTree(FoldingRangeContext foldingRangeContext) {
        Optional<Path> filePath = CommonUtil.getPathFromURI(foldingRangeContext.fileUri());
        if (filePath.isEmpty()) {
            return null;
        }
        Optional<Document> document = foldingRangeContext.workspace().document(filePath.get());
        if (document.isEmpty()) {
            return null;
        }
        return document.get().syntaxTree();
    }
}
