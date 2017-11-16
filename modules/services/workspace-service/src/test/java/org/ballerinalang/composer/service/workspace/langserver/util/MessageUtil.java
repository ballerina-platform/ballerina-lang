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
package org.ballerinalang.composer.service.workspace.langserver.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.ballerinalang.composer.service.workspace.langserver.dto.TextDocumentPositionParams;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Message utils for manipulating the Message objects
 */
public class MessageUtil {
    private static final String METHOD_COMPLETION = "textDocument/completion";
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Gson GSON = new Gson();
    public static final String MESSAGE_ID = "tests.ballerina.message.id";

    /**
     * Get a new request message from the content
     * @param balContent text content
     * @param position position of the cursor
     * @param id message id
     * @return {@link RequestMessage}
     */
    public static RequestMessage getRequestMessage(String balContent, Position position, String id) {
        RequestMessage requestMessage = new RequestMessage();
        TextDocumentPositionParams textDocumentPositionParams = new TextDocumentPositionParams();

        textDocumentPositionParams.setFilePath("/");
        textDocumentPositionParams.setFileName("untitled");
        textDocumentPositionParams.setPackageName(".");
        textDocumentPositionParams.setPosition(position);
        textDocumentPositionParams.setText(balContent);

        requestMessage.setId(id);
        requestMessage.setMethod(METHOD_COMPLETION);
        requestMessage.setParams(textDocumentPositionParams);
        requestMessage.setJsonrpc("2.0");

        return requestMessage;
    }

    private static String getCompletionItemPropertyString(CompletionItem completionItem) {

        return "{" +
                completionItem.getInsertText() + "," +
                completionItem.getDetail() + "," +
                completionItem.getDocumentation() + "," +
                completionItem.getKind() + "," +
                completionItem.getLabel() +
                "}";
    }

    private static List<String> getStringListForEvaluation(List<CompletionItem> completionItems) {
        List<String> evalList = new ArrayList<>();
        completionItems.forEach(completionItem -> evalList.add(getCompletionItemPropertyString(completionItem)));
        return evalList;
    }

    public static boolean listMatches(List<CompletionItem> list1, List<CompletionItem> list2) {
        return list1.size() == list2.size() &&
                getStringListForEvaluation(list1).containsAll(getStringListForEvaluation(list2));
    }

    public static List<CompletionItem> getExpectedItemList(String expectedResultPath)
            throws IOException, URISyntaxException {
        String expectedResult = FileUtils.fileContent(expectedResultPath);
        JsonObject expectedJson = JSON_PARSER.parse(expectedResult).getAsJsonObject();
        JsonArray result = expectedJson.getAsJsonArray("result");
        List<CompletionItem> expectedList = new ArrayList<>();
        result.forEach(jsonElement -> {
            CompletionItem completionItem = GSON.fromJson(jsonElement, CompletionItem.class);
            expectedList.add(completionItem);
        });

        return expectedList;
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
     * Check whether list1 is a subset of list2
     * @param list1 - completion item list being checked
     * @param list2 - completion item list being checked against
     * @return whether list1 is a subset of list2
     */
    public static boolean isSubList(List<CompletionItem> list1, List<CompletionItem> list2) {
        return getStringListForEvaluation(list2).containsAll(getStringListForEvaluation(list1));
    }
}
