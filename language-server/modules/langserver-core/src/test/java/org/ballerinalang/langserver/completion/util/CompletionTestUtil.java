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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.completions.CompletionCustomErrorStrategy;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.CompletionSubRuleParser;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
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
        // Here we replace the Windows specific \r\n to \n for evaluation only
        return ("{" +
                completionItem.getInsertText() + "," +
                completionItem.getDetail() + "," +
                completionItem.getDocumentation() + "," +
                completionItem.getLabel() +
                "}").replace("\r\n", "\n");
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
     * Check whether list2 does not contains all the elements in list1.
     *
     * @param list1 - negative completion item list being checked
     * @param list2 - completion item list being checked against
     * @return whether list1 is a subset of list2
     */
    public static boolean containsAtLeastOne(List<CompletionItem> list1, List<CompletionItem> list2) {
        List<String> pivotList = getStringListForEvaluation(list2);
        for (String negativeItem : getStringListForEvaluation(list1)) {
            if (pivotList.contains(negativeItem)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the completions list.
     *
     * @param documentManager Document manager instance
     * @param pos             {@link TextDocumentPositionParams} position params
     */
    public static List<CompletionItem> getCompletions(WorkspaceDocumentManager documentManager,
                                                      TextDocumentPositionParams pos) {
        List<CompletionItem> completions;
        LSServiceOperationContext completionContext = new LSServiceOperationContext();
        completionContext.put(DocumentServiceKeys.POSITION_KEY, pos);
        completionContext.put(DocumentServiceKeys.FILE_URI_KEY, pos.getTextDocument().getUri());
        completionContext.put(CompletionKeys.DOC_MANAGER_KEY, documentManager);
        BLangPackage bLangPackage = LSCompiler.getBLangPackage(completionContext, documentManager, false,
                CompletionCustomErrorStrategy.class, false).get(0);
        completionContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                              bLangPackage.symbol.getName().getValue());

        CompletionUtil.resolveSymbols(completionContext, bLangPackage);
        CompletionSubRuleParser.parse(completionContext);
        completions = CompletionUtil.getCompletionItems(completionContext);

        return completions;
    }

    /**
     * Prepare the Document manager instance with the given file and issue the did open operation.
     *
     * @param uri        File Uri
     * @param balContent File Content
     * @return {@link WorkspaceDocumentManager}
     */
    public static WorkspaceDocumentManagerImpl prepareDocumentManager(String uri, String balContent) {
        Path openedPath;
        WorkspaceDocumentManagerImpl documentManager = WorkspaceDocumentManagerImpl.getInstance();

        openedPath = Paths.get(uri);
        documentManager.openFile(openedPath, balContent);

        return documentManager;
    }
}
