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
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.langserver.completions.resolvers.TopLevelResolver;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

/**
 * Common utility methods for the completion operation.
 */
public class CompletionUtil {
    // In case of there are any specific error scenarios, then the fallback BLang package will be used
    // to get completions
    /**
     * Resolve the visible symbols from the given BLang Package and the current context.
     * @param completionContext     Completion Service Context
     * @param bLangPackage          BLang Package
     */
    public static void resolveSymbols(LSServiceOperationContext completionContext, BLangPackage bLangPackage) {
        // Visit the package to resolve the symbols
        TreeVisitor treeVisitor = new TreeVisitor(completionContext);
        bLangPackage.accept(treeVisitor);
        completionContext.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);

        if (completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY) == null) {
            treeVisitor.populateSymbols(treeVisitor.resolveAllVisibleSymbols(treeVisitor.getSymbolEnv()),
                    treeVisitor.getSymbolEnv());
        }
    }

    /**
     * Get the completion Items for the context.
     * @param completionContext     Completion context
     * @return {@link List}         List of resolved completion Items
     */
    public static List<CompletionItem> getCompletionItems(LSServiceOperationContext completionContext) {
        completionContext.put(CompletionKeys.CURRENT_LINE_SEGMENT_KEY, getSourceSegmentOfLine(completionContext));
        BLangNode symbolEnvNode = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);

        try {
            if (symbolEnvNode instanceof BLangPackage) {
                return CompletionItemResolver.getResolverByClass(TopLevelResolver.class)
                        .resolveItems(completionContext);
            } else {
                return CompletionItemResolver.getResolverByClass(symbolEnvNode.getClass())
                        .resolveItems(completionContext);
            }
        } catch (Exception | AssertionError e) {
            return new ArrayList<>();
        }
    }

    /**
     * From the source, extract the line segment for the current cursor position's line.
     * @param context           Service Operation context
     * @return {@link String}   Extracted line segment
     */
    private static String getSourceSegmentOfLine(LSServiceOperationContext context) {
        TextDocumentPositionParams positionParams = context.get(DocumentServiceKeys.POSITION_KEY);
        WorkspaceDocumentManager documentManager = context.get(CompletionKeys.DOC_MANAGER_KEY);
        int line = positionParams.getPosition().getLine();
        String fileUri = positionParams.getTextDocument().getUri();
        Path completionPath = CommonUtil.getPath(new LSDocument(fileUri));
        Optional<Lock> lock = documentManager.lockFile(completionPath);
        String fileContent = documentManager.getFileContent(completionPath);
        lock.ifPresent(Lock::unlock);

        String[] splitContent = fileContent.split(CommonUtil.LINE_SEPARATOR_SPLIT);
        if (splitContent.length < line) {
            return "";
        } else {
            return splitContent[line];
        }
    }
}
