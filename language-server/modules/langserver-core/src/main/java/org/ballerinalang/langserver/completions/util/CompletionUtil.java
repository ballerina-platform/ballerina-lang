/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Common utility methods for the completion operation.
 */
public class CompletionUtil {
    /**
     * Resolve the visible symbols from the given BLang Package and the current context.
     *
     * @param completionContext     Completion Service Context
     */
    public static void resolveSymbols(LSServiceOperationContext completionContext) {
        // Visit the package to resolve the symbols
        TreeVisitor treeVisitor = new TreeVisitor(completionContext);
        BLangPackage bLangPackage = completionContext.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        bLangPackage.accept(treeVisitor);
        completionContext.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);
    }

    /**
     * Get the completion Items for the context.
     *
     * @param completionContext     Completion context
     * @return {@link List}         List of resolved completion Items
     */
    public static List<CompletionItem> getCompletionItems(LSServiceOperationContext completionContext) {
        try {
            completionContext.put(CompletionKeys.CURRENT_LINE_SEGMENT_KEY, getSourceSegmentOfLine(completionContext));
            BLangNode symbolEnvNode = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
            return CompletionItemResolver.get(symbolEnvNode.getClass()).resolveItems(completionContext);
        } catch (Exception | AssertionError e) {
            return new ArrayList<>();
        }
    }

    /**
     * From the source, extract the line segment for the current cursor position's line.
     *
     * @param context           Service Operation context
     * @return {@link String}   Extracted line segment
     */
    private static String getSourceSegmentOfLine(LSServiceOperationContext context) throws WorkspaceDocumentException {
        TextDocumentPositionParams positionParams = context.get(DocumentServiceKeys.POSITION_KEY);
        WorkspaceDocumentManager documentManager = context.get(CompletionKeys.DOC_MANAGER_KEY);
        int line = positionParams.getPosition().getLine();
        String fileUri = positionParams.getTextDocument().getUri();
        Path completionPath = new LSDocument(fileUri).getPath();
        Path compilationPath = getUntitledFilePath(completionPath.toString()).orElse(completionPath);
        String fileContent = documentManager.getFileContent(compilationPath);

        String[] splitContent = fileContent.split(CommonUtil.LINE_SEPARATOR_SPLIT);
        if (splitContent.length < line) {
            return "";
        }

        return splitContent[line];
    }
}
