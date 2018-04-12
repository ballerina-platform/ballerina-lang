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
package org.ballerinalang.langserver.completion.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.TextDocumentServiceUtil;
import org.ballerinalang.langserver.completions.CompletionCustomErrorStrategy;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.langserver.completions.resolvers.TopLevelResolver;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManagerImpl;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Message utils for manipulating the Message objects.
 */
public class CompletionTestUtil {

    private static final Gson GSON = new Gson();

    /**
     * Get a new request message from the content.
     *
     * @param position position of the cursor
     * @param uri      documentURI
     * @return {@link TextDocumentPositionParams}
     */
    public static TextDocumentPositionParams getPositionParams(Position position, String uri) {
        TextDocumentPositionParams textDocumentPositionParams = new TextDocumentPositionParams();
        TextDocumentIdentifier documentIdentifier = new TextDocumentIdentifier();
        documentIdentifier.setUri(Paths.get(uri).toUri().toString());

        textDocumentPositionParams.setPosition(position);
        textDocumentPositionParams.setTextDocument(documentIdentifier);

        return textDocumentPositionParams;
    }

    private static String getCompletionItemPropertyString(CompletionItem completionItem) {

        // TODO: Need to add kind and sort text as well
        return "{" +
                completionItem.getInsertText() + "," +
                completionItem.getDetail() + "," +
                completionItem.getDocumentation() + "," +
                completionItem.getLabel() +
                "}";
    }

    private static List<String> getStringListForEvaluation(List<CompletionItem> completionItems) {
        List<String> evalList = new ArrayList<>();
        completionItems.forEach(completionItem -> evalList.add(getCompletionItemPropertyString(completionItem)));
        return evalList;
    }

    public static List<CompletionItem> getExpectedItemList(JsonArray expectedItems) {
        List<CompletionItem> expectedList = new ArrayList<>();
        expectedItems.forEach(jsonElement -> {
            CompletionItem completionItem = GSON.fromJson(jsonElement, CompletionItem.class);
            expectedList.add(completionItem);
        });

        return expectedList;
    }

    /**
     * Check whether list1 is a subset of list2.
     *
     * @param list1 - completion item list being checked
     * @param list2 - completion item list being checked against
     * @return whether list1 is a subset of list2
     */
    public static boolean isSubList(List<CompletionItem> list1, List<CompletionItem> list2) {
        return getStringListForEvaluation(list2).containsAll(getStringListForEvaluation(list1));
    }

    /**
     * Get the completions list.
     *
     * @param documentManager Document manager instance
     * @param pos             {@link TextDocumentPositionParams} position params
     */
    public static List<CompletionItem> getCompletions(WorkspaceDocumentManagerImpl documentManager,
                                                      TextDocumentPositionParams pos) {
        List<CompletionItem> completions;
        LSServiceOperationContext completionContext = new LSServiceOperationContext();
        completionContext.put(DocumentServiceKeys.POSITION_KEY, pos);
        completionContext.put(DocumentServiceKeys.FILE_URI_KEY, pos.getTextDocument().getUri());

        BLangPackage bLangPackage = TextDocumentServiceUtil.getBLangPackage(completionContext,
                documentManager, false, CompletionCustomErrorStrategy.class, false)
                .get(0);
        completionContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                bLangPackage.symbol.getName().getValue());
        // Visit the package to resolve the symbols
        TreeVisitor treeVisitor = new TreeVisitor(completionContext);
        bLangPackage.accept(treeVisitor);

        BLangNode symbolEnvNode = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        if (symbolEnvNode == null) {
            completions = CompletionItemResolver.getResolverByClass(TopLevelResolver.class)
                    .resolveItems(completionContext);
        } else {
            completions = CompletionItemResolver.getResolverByClass(symbolEnvNode.getClass())
                    .resolveItems(completionContext);
        }

        return completions;
    }

    /**
     * Prepare the Document manager instance with the given file and issue the did open operation.
     *
     * @param uri        File Uri
     * @param balContent File Content
     * @return {@link WorkspaceDocumentManagerImpl}
     */
    public static WorkspaceDocumentManagerImpl prepareDocumentManager(String uri, String balContent) {
        Path openedPath;
        WorkspaceDocumentManagerImpl documentManager = WorkspaceDocumentManagerImpl.getInstance();

        openedPath = Paths.get(uri);
        documentManager.openFile(openedPath, balContent);

        return documentManager;
    }
}
