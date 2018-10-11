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
package org.ballerinalang.langserver.formatting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Node tree visitor for formatting.
 */
public class FormattingTreeUtil {
    private static final String TAB = "\t";
    private static final String SPACE_TAB = "    ";
    private static final String BETWEEN_BLOCK_SPACE = System.lineSeparator() + System.lineSeparator();
    private static final String SINGLE_SPACE = " ";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String EMPTY_SPACE = "";
    private static final String SKIP_FORMATTING = "skipFormatting";

    private String getWhiteSpaces(int column) {
        StringBuilder whiteSpaces = new StringBuilder();
        for (int i = 0; i < (column - 1); i++) {
            whiteSpaces.append(" ");
        }

        return whiteSpaces.toString();
    }

    private int getWhiteSpaceCount(String ws) {
        return ws.length();
    }

    private List<String> tokenizer(String text) {
        List<String> tokens = new ArrayList<>();
        String comment = "";

        for (int i = 0; i < text.length(); i++) {
            String character = text.charAt(i) + "";
            if (!character.contains("\n")) {
                comment += text.charAt(i);
            } else {
                if (!comment.trim().equals("")) {
                    tokens.add(comment.trim());
                    comment = "";
                }
                tokens.add(character);
            }

            if (i == (text.length() - 1) && !comment.trim().equals("")) {
                tokens.add(comment.trim());
                comment = "";
            }
        }
        return tokens;
    }

    private String getTextFromTokens(List<String> tokens, String indent) {
        String text = "";
        for (int i = 0; i < tokens.size(); i++) {
            if (!tokens.get(i).contains("\n")) {
                text += indent != null ? indent + tokens.get(i) : tokens.get(i);
            } else {
                text += tokens.get(i);
            }
        }

        return indent != null ? (text + indent) : text;
    }

    private void preserveHeight(JsonArray ws, String indent) {
        for (int i = 0; i < ws.size(); i++) {
            if (ws.get(i).isJsonObject() && ws.get(i).getAsJsonObject().has("ws") &&
                    (ws.get(i).getAsJsonObject().get("ws").getAsString().trim().length() > 0 ||
                            ws.get(i).getAsJsonObject().get("ws").getAsString().contains("\n"))) {
                List<String> tokens = this.tokenizer(ws.get(i).getAsJsonObject().get("ws").getAsString());
                ws.get(i).getAsJsonObject().addProperty("ws", this.getTextFromTokens(tokens, indent));
            }
        }
    }

    private boolean isHeightAvailable(String ws) {
        return ws.trim().length() > 0 || ws.contains("\n");
    }

    private boolean isCommentAvailable(String ws) {
        return ws.trim().length() > 0;
    }

    private boolean isNewLine(String text) {
        return text.contains("\n");
    }

    private int findIndex(JsonObject node) {
        int index = -1;
        JsonObject parent = node.getAsJsonObject("parent");

        for (Map.Entry<String, JsonElement> entry : parent.entrySet()) {
            if (entry.getValue().isJsonArray() && !entry.getKey().equals("ws")) {
                for (int i = 0; i < entry.getValue().getAsJsonArray().size(); i++) {
                    JsonElement element = entry.getValue().getAsJsonArray().get(i);
                    if (element.isJsonObject() && element.getAsJsonObject().has("id")
                            && element.getAsJsonObject().get("id").getAsString()
                            .equals(node.get("id").getAsString())) {
                        index = i;
                    }
                }
            }
        }

        return index;
    }

    // Start Formatting utils

    /**
     * format abort node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAbortNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format annotation node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format annotation attachment node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationAttachmentNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }
        if (node.has("ws") && !skip) {
            this.preserveHeight(node.getAsJsonArray("ws"),
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

            if (!this.isHeightAvailable(node.getAsJsonArray("ws")
                    .get(0).getAsJsonObject().get("ws").getAsString())) {
                node.getAsJsonArray("ws").get(0).getAsJsonObject()
                        .addProperty("ws", BETWEEN_BLOCK_SPACE +
                                this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            } else if (!this.isNewLine(node.getAsJsonArray("ws").get(0)
                    .getAsJsonObject().get("ws").getAsString().charAt(0) + "")) {
                node.getAsJsonArray("ws").get(0).getAsJsonObject()
                        .addProperty("ws",
                                NEW_LINE + node.getAsJsonArray("ws").get(0)
                                        .getAsJsonObject().get("ws").getAsString());
            }

            if (node.has("expression") && node.getAsJsonObject("expression").has("ws")) {
                node.getAsJsonObject("expression").get("position").getAsJsonObject()
                        .addProperty("startColumn",
                                node.getAsJsonObject("position").get("startColumn").getAsInt());
            }
        }
    }

    /**
     * format annotation attribute node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationAttributeNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format annotation attachment attribute node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationAttachmentAttributeNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format annotation attachment attribute value node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationAttachmentAttributeValueNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Array Literal Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatArrayLiteralExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Array Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatArrayTypeNode(JsonObject node) {
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));
        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }
    }

    /**
     * format Assignment node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAssignmentNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

            if (node.has("variable") && node.getAsJsonObject("variable").has("ws")) {
                node.getAsJsonObject("variable").get("position").getAsJsonObject().addProperty("startColumn",
                        node.getAsJsonObject("position").get("startColumn").getAsInt());
            }

            if (node.has("expression") && node.getAsJsonObject("expression").has("ws")) {
                node.getAsJsonObject("expression").addProperty(SKIP_FORMATTING, true);
            }

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                if (node.has("declaredWithVar") && node.get("declaredWithVar").getAsBoolean()) {
                    ws.get(0).getAsJsonObject().addProperty("ws", NEW_LINE +
                            this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
                } else {
                    ws.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                }
            }

            if (!this.isHeightAvailable(ws.get(1).getAsJsonObject().get("ws").getAsString())) {
                if (node.has("declaredWithVar") && node.get("declaredWithVar").getAsBoolean()) {
                    ws.get(1).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                    if (!this.isHeightAvailable(ws.get(2).getAsJsonObject().get("ws").getAsString())) {
                        ws.get(2).getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                    }
                } else {
                    ws.get(1).getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                }
            }
        }
    }

    /**
     * format Await Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAwaitExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Binary Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBinaryExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);

        if (node.has("leftExpression")) {
            node.getAsJsonObject("leftExpression").addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("rightExpression")) {
            node.getAsJsonObject("rightExpression").addProperty(SKIP_FORMATTING, true);
        }
    }

    /**
     * format Bind node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBindNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Block node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBlockNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }
        if (!skip) {
            JsonObject position = new JsonObject();
            position.addProperty("startColumn", node.get("parent").getAsJsonObject().get("position")
                    .getAsJsonObject().get("startColumn").getAsInt());
            node.add("position", position);

            for (int i = 0; i < node.getAsJsonArray("statements").size(); i++) {
                JsonElement child = node.getAsJsonArray("statements").get(i);
                child.getAsJsonObject().get("position").getAsJsonObject().addProperty("startColumn",
                        node.get("position").getAsJsonObject().get("startColumn").getAsInt()
                                + this.getWhiteSpaceCount(SPACE_TAB));
            }

            if (node.has("ws") && node.getAsJsonArray("ws").get(0).getAsJsonObject()
                    .get("text").getAsString().equals("else")) {
                JsonArray ws = node.getAsJsonArray("ws");
                this.preserveHeight(ws, this.getWhiteSpaces(node.getAsJsonObject("position")
                        .get("startColumn").getAsInt()));

                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                }

                if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(ws.size() - 2).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                }

                if (node.getAsJsonArray("statements").size() <= 0) {
                    if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                        ws.get(ws.size() - 1).getAsJsonObject()
                                .addProperty("ws", NEW_LINE +
                                        this.getWhiteSpaces(node.getAsJsonObject("position")
                                                .get("startColumn").getAsInt())
                                        + NEW_LINE +
                                        this.getWhiteSpaces(node.getAsJsonObject("position")
                                                .get("startColumn").getAsInt()));
                    }
                } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws",
                            NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject("position")
                                    .get("startColumn").getAsInt()));
                }
            }
        }
    }

    /**
     * format Braced Tuple Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBracedTupleExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Break node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBreakNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format built in ref type.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBuiltInRefTypeNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Catch node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatCatchNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Check Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatCheckExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);

        if (node.has("expression")) {
            node.getAsJsonObject("expression").addProperty(SKIP_FORMATTING, true);
        }
    }

    /**
     * format Compilation Unit node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatCompilationUnitNode(JsonObject node) {
        JsonArray topLevelNodes = node.get("topLevelNodes").getAsJsonArray();
        for (int i = 0; i < topLevelNodes.size(); i++) {
            JsonElement child = topLevelNodes.get(i);
            child.getAsJsonObject().get("position").getAsJsonObject().addProperty("startColumn",
                    1);
        }

        if (node.has("ws") && topLevelNodes.size() > 0) {
            JsonArray ws = node.get("ws").getAsJsonArray();
            this.preserveHeight(ws, null);
            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                ws.get(0).getAsJsonObject().addProperty("ws", NEW_LINE);
            } else if (!this.isNewLine(ws.get(0).getAsJsonObject().get("ws").getAsString()
                    .charAt(ws.get(0).getAsJsonObject().get("ws").getAsString().length() - 1) + "")) {
                ws.get(0).getAsJsonObject().addProperty("ws",
                        (ws.get(0).getAsJsonObject().get("ws").getAsString() + NEW_LINE));
            }
        }

        int i, j;
        boolean swapped = false;
        for (i = 0; i < topLevelNodes.size() - 1; i++) {
            swapped = false;
            for (j = 0; j < topLevelNodes.size() - i - 1; j++) {
                if (topLevelNodes.get(j).getAsJsonObject()
                        .get("kind").getAsString().equals("Import")
                        && topLevelNodes.get(j + 1).getAsJsonObject()
                        .get("kind").getAsString().equals("Import")) {
                    String refImportName = topLevelNodes.get(j).getAsJsonObject()
                            .get("orgName").getAsJsonObject().get("value").getAsString() + "/"
                            + topLevelNodes.get(j).getAsJsonObject().get("packageName")
                            .getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();

                    String compImportName = topLevelNodes.get(j + 1).getAsJsonObject()
                            .get("orgName").getAsJsonObject().get("value").getAsString() + "/"
                            + topLevelNodes.get(j + 1).getAsJsonObject().get("packageName")
                            .getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();

                    int comparisonResult = refImportName.compareTo(compImportName);
                    // Swap if the comparison value is positive.
                    if (comparisonResult > 0) {
                        // Swap ws to keep the formatting in level.
                        String refWS = topLevelNodes.get(j).getAsJsonObject().get("ws")
                                .getAsJsonArray().get(0).getAsJsonObject().get("ws").getAsString();

                        String compWS = topLevelNodes.get(j + 1).getAsJsonObject().get("ws")
                                .getAsJsonArray().get(0).getAsJsonObject().get("ws").getAsString();

                        JsonElement tempNode = topLevelNodes.get(j);
                        topLevelNodes.set(j, topLevelNodes.get(j + 1));
                        tempNode.getAsJsonObject().get("ws").getAsJsonArray().get(0)
                                .getAsJsonObject().addProperty("ws", compWS);
                        topLevelNodes.get(j).getAsJsonObject().get("ws").getAsJsonArray()
                                .get(0).getAsJsonObject().addProperty("ws", refWS);
                        topLevelNodes.set(j + 1, tempNode);

                        swapped = true;
                    }
                }
            }
            // If not swapped, break.
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * format Compound Assignment node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatCompoundAssignmentNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Constrained Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatConstrainedTypeNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);

        if (node.has("constraint")) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("type")) {
            node.addProperty(SKIP_FORMATTING, true);
        }
    }

    /**
     * format Deprecated node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatDeprecatedNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Documentation node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatDocumentationNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Documentation Attribute node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatDocumentationAttributeNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Done node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatDoneNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Elvis Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatElvisExprNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Endpoint node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatEndpointNode(JsonObject node) {
        if (node.has("ws")) {
            this.preserveHeight(node.getAsJsonArray("ws"),
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

            int endpointIndex = this.findIndex(node);

            if (!this.isHeightAvailable(node.getAsJsonArray("ws").get(0)
                    .getAsJsonObject().get("ws").getAsString())) {
                String whiteSpace = (endpointIndex > 0
                        || !node.getAsJsonObject("parent").get("kind").getAsString().equals("CompilationUnit"))
                        ? (BETWEEN_BLOCK_SPACE + this.getWhiteSpaces(node.getAsJsonObject("position")
                        .get("startColumn").getAsInt()))
                        : EMPTY_SPACE;
                node.getAsJsonArray("ws").get(0).getAsJsonObject().addProperty("ws", whiteSpace);
            } else if (!this.isNewLine(node.getAsJsonArray("ws").get(0)
                    .getAsJsonObject().get("ws").getAsString().charAt(0) + "") && endpointIndex != 0) {
                node.getAsJsonArray("ws").get(0).getAsJsonObject().addProperty("ws",
                        NEW_LINE + node.getAsJsonArray("ws").get(0)
                                .getAsJsonObject().get("ws").getAsString());
            }

            if (node.has("configurationExpression")
                    && node.getAsJsonObject("configurationExpression").has("ws")) {
                node.getAsJsonObject("configurationExpression").getAsJsonObject("position")
                        .addProperty("startColumn",
                                node.getAsJsonObject("position").get("startColumn").getAsInt());
            }
        }
    }

    /**
     * format Endpoint Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatEndpointTypeNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Expression Statement node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatExpressionStatementNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            node.getAsJsonObject("expression").getAsJsonObject("position")
                    .addProperty("startColumn",
                            node.getAsJsonObject("position").get("startColumn").getAsInt());

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                ws.get(0).getAsJsonObject().addProperty("ws", EMPTY_SPACE);
            }
        }
    }

    /**
     * format Field Based Access Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatFieldBasedAccessExprNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");
            if (node.has("expression")) {
                if (node.has("isExpression") && node.get("isExpression").getAsBoolean()) {
                    node.getAsJsonObject("expression").addProperty("isExpression", true);
                }
                node.getAsJsonObject("expression").getAsJsonObject("position").addProperty("startColumn",
                        node.getAsJsonObject("position").get("startColumn").getAsInt());
            }

            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            for (JsonElement jsonElement : ws) {
                if (!this.isHeightAvailable(jsonElement.getAsJsonObject().get("ws").getAsString())) {
                    jsonElement.getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                }
            }
        }
    }

    /**
     * format Foreach node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatForeachNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);

        if (node.has("collection")) {
            node.getAsJsonObject("collection").addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("variables")) {
            for (JsonElement jsonElement : node.getAsJsonArray("variables")) {
                jsonElement.getAsJsonObject().addProperty(SKIP_FORMATTING, true);
            }
        }
    }

    /**
     * format Forever node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatForeverNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Fork Join node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatForkJoinNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Function node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatFunctionNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws, this.getWhiteSpaces(node.getAsJsonObject("position")
                    .get("startColumn").getAsInt()));
            int functionIndex = this.findIndex(node);

            if (!this.isHeightAvailable(ws.get(0)
                    .getAsJsonObject().get("ws").getAsString())) {
                if (node.getAsJsonObject("parent")
                        .get("kind").getAsString().equals("Lambda")) {
                    ws.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                } else {
                    String whiteSpace = (functionIndex > 0 || node.getAsJsonObject("parent")
                            .get("kind").getAsString().equals("ObjectType"))
                            ? (BETWEEN_BLOCK_SPACE + this.getWhiteSpaces(node.getAsJsonObject("position")
                            .get("startColumn").getAsInt()))
                            : EMPTY_SPACE;
                    ws.get(0).getAsJsonObject().addProperty("ws", whiteSpace);
                }
            } else if (!this.isNewLine(ws.get(0).getAsJsonObject()
                    .get("ws").getAsString().charAt(0) + "") && functionIndex != 0) {
                ws.get(0).getAsJsonObject()
                        .addProperty("ws", NEW_LINE + ws
                                .get(0).getAsJsonObject().get("ws").getAsString());
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 2).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
            }

            if (node.has("body")
                    && node.getAsJsonObject("body").getAsJsonArray("statements").size() <= 0
                    && node.getAsJsonArray("endpointNodes").size() <= 0
                    && node.getAsJsonArray("workers").size() <= 0) {
                if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(ws.size() - 1).getAsJsonObject()
                            .addProperty("ws", NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt())
                                    + NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject("position")
                                            .get("startColumn").getAsInt()));
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws",
                        NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject("position")
                                .get("startColumn").getAsInt()));

            }

            if (node.has("parameters")) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                for (int i = 0; i < parameters.size(); i++) {
                    parameters.get(i).getAsJsonObject().addProperty(SKIP_FORMATTING, true);
                }
            }

            if (node.has("defaultableParameters")) {
                JsonArray defaulableParameters = node.getAsJsonArray("defaultableParameters");
                for (int i = 0; i < defaulableParameters.size(); i++) {
                    defaulableParameters.get(i).getAsJsonObject().addProperty(SKIP_FORMATTING, true);
                }
            }

            if (node.has("endpointNodes")) {
                for (int i = 0; i < node.getAsJsonArray("endpointNodes").size(); i++) {
                    node.getAsJsonArray("endpointNodes").get(i).getAsJsonObject()
                            .getAsJsonObject("position").addProperty("startColumn",
                            node.getAsJsonObject("position").get("startColumn").getAsInt() +
                                    this.getWhiteSpaceCount(SPACE_TAB));
                }
            }

            if (node.has("workers")) {
                JsonArray workers = node.getAsJsonArray("workers");
                for (int i = 0; i < workers.size(); i++) {
                    workers.get(i).getAsJsonObject().getAsJsonObject("position").addProperty("startColumn",
                            node.getAsJsonObject("position").get("startColumn").getAsInt() +
                                    this.getWhiteSpaceCount(SPACE_TAB));
                }
            }

            if (node.has("annotationAttachments")) {
                JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                for (int i = 0; i < annotationAttachments.size(); i++) {
                    annotationAttachments.get(i).getAsJsonObject().getAsJsonObject("position")
                            .addProperty("startColumn", node.getAsJsonObject("position").get("startColumn")
                                    .getAsInt());
                }
            }

            if (node.has("restParameters")) {
                node.getAsJsonObject("restParameters").addProperty(SKIP_FORMATTING, true);
            }

            if (node.has("returnTypeNode") &&
                    node.has("hasReturns") &&
                    node.get("hasReturns").getAsBoolean()) {
                node.getAsJsonObject("returnTypeNode").addProperty(SKIP_FORMATTING, true);
            }

            if (node.has("returnTypeAnnotationAttachments")) {
                for (JsonElement annotation : node.getAsJsonArray("returnTypeAnnotationAttachments")) {
                    annotation.getAsJsonObject().addProperty(SKIP_FORMATTING, true);
                }
            }
        }
    }

    /**
     * format Function Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatFunctionClauseNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Function Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatFunctionTypeNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                ws.get(0).getAsJsonObject().addProperty("ws", NEW_LINE +
                        this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            }

            if (node.has("paramTypeNode")) {
                JsonArray parameters = node.getAsJsonArray("paramTypeNode");
                for (int i = 0; i < parameters.size(); i++) {
                    parameters.get(i).getAsJsonObject().addProperty(SKIP_FORMATTING, true);
                }
            }

            if (node.has("returnKeywordExists") && node.get("returnKeywordExists").getAsBoolean()) {
                node.getAsJsonObject("returnTypeNode").addProperty(SKIP_FORMATTING, true);
            }

            if (node.has("returnTypeAnnotationAttachments")) {
                for (JsonElement annotation : node.getAsJsonArray("returnTypeAnnotationAttachments")) {
                    annotation.getAsJsonObject().addProperty(SKIP_FORMATTING, true);
                }
            }
        }
    }

    /**
     * format Group By node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatGroupByNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Having node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatHavingNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Identifier node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatIdentifierNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format If node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatIfNode(JsonObject node) {
        if (node.has("ws")) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

            if (node.has("isElseIfBlock") && node.get("isElseIfBlock").getAsBoolean()) {
                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                }
            } else {
                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(0).getAsJsonObject().addProperty("ws",
                            NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject("position")
                                    .get("startColumn").getAsInt()));
                }
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 2).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
            }

            if (node.has("body")
                    && node.getAsJsonObject("body").getAsJsonArray("statements").size() <= 0) {
                if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(ws.size() - 1).getAsJsonObject()
                            .addProperty("ws", NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject("position")
                                            .get("startColumn").getAsInt())
                                    + NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject("position")
                                            .get("startColumn").getAsInt()));
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws",
                        NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject("position")
                                .get("startColumn").getAsInt()));
            }

            if (node.has("elseStatement")
                    && !node.getAsJsonObject("elseStatement").get("kind").getAsString().equals("Block")) {
                node.getAsJsonObject("elseStatement").getAsJsonObject("position").addProperty("startColumn",
                        node.getAsJsonObject("position").get("startColumn").getAsInt());
            }

            if (node.has("condition") && node.getAsJsonObject("condition").has("ws")) {
                JsonArray conditionWs = node.getAsJsonObject("condition").getAsJsonArray("ws");
                this.preserveHeight(conditionWs, this.getWhiteSpaces(node.getAsJsonObject("position")
                        .get("startColumn").getAsInt()));

                if (!this.isCommentAvailable(conditionWs.get(0).getAsJsonObject().get("ws").getAsString())) {
                    conditionWs.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                }

                if (!this.isHeightAvailable(conditionWs.get(conditionWs.size() - 1).getAsJsonObject()
                        .get("ws").getAsString())) {
                    conditionWs.get(conditionWs.size() - 1).getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                }
            }
        }
    }

    /**
     * format Import node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatImportNode(JsonObject node) {
        if (node.has("ws")) {
            JsonArray ws = node.get("ws").getAsJsonArray();
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.get("position").getAsJsonObject().get("startColumn").getAsInt()));

            int importIndex = this.findIndex(node);
            if (importIndex > 0) {
                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(0).getAsJsonObject().addProperty("ws", NEW_LINE);
                }
            } else {
                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject()
                        .get("ws").getAsString())) {
                    ws.get(0).getAsJsonObject()
                            .addProperty("ws", EMPTY_SPACE);
                }
            }
        }
    }

    /**
     * format Index Based Access Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatIndexBasedAccessExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Int Range Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatIntRangeExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Invocation node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatInvocationNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws")) {
            if (!skip) {
                JsonArray ws = node.getAsJsonArray("ws");

                this.preserveHeight(ws,
                        this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

                if (node.getAsJsonObject("parent").get("kind").getAsString().equals("ExpressionStatement")) {
                    if (node.has("expression") && node.getAsJsonObject("expression")
                            .has("ws")) {
                        JsonArray expressionWs = node.getAsJsonObject("expression").get("ws").getAsJsonArray();
                        this.preserveHeight(expressionWs,
                                this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
                        if (!this.isHeightAvailable(expressionWs.get(0).getAsJsonObject().get("ws").getAsString())) {
                            if (expressionWs.get(0).getAsJsonObject().get("text").getAsString().equals(".") ||
                                    expressionWs.get(0).getAsJsonObject().get("text").getAsString().equals("=>")) {
                                expressionWs.get(0).getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                            } else {
                                expressionWs.get(0).getAsJsonObject().addProperty("ws", NEW_LINE +
                                        this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn")
                                                .getAsInt()));
                            }
                        }
                    } else if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                        ws.get(0).getAsJsonObject().addProperty("ws", NEW_LINE +
                                this.getWhiteSpaces(node.getAsJsonObject("parent")
                                        .getAsJsonObject("position").get("startColumn").getAsInt()));
                    }
                } else {
                    if (node.has("expression") && node.getAsJsonObject("expression")
                            .has("ws")) {
                        JsonArray expressionWs = node.getAsJsonObject("expression").get("ws").getAsJsonArray();
                        this.preserveHeight(expressionWs,
                                this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
                        if (!this.isHeightAvailable(expressionWs.get(0).getAsJsonObject().get("ws").getAsString())) {
                            if (expressionWs.get(0).getAsJsonObject().get("text").getAsString().equals(".") ||
                                    expressionWs.get(0).getAsJsonObject().get("text").getAsString().equals("=>")) {
                                expressionWs.get(0).getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                            } else {
                                expressionWs.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                            }
                        }
                    } else if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                        ws.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                    }
                }
            }

            if (node.has("argumentExpressions")) {
                JsonArray argumentExpressions = node.getAsJsonArray("argumentExpressions");
                for (int i = 0; i < argumentExpressions.size(); i++) {
                    if (argumentExpressions.get(i) != null) {
                        argumentExpressions.get(i).getAsJsonObject().addProperty(SKIP_FORMATTING, true);
                    }
                }
            }
        }
    }

    /**
     * format Lambda node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatLambdaNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Limit node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatLimitNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatLiteralNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Lock node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatLockNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Match node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
        if (node.has("expression")) {
            node.getAsJsonObject("expression").addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("patternClauses")) {
            for (JsonElement patternClause : node.getAsJsonArray("patternClauses")) {
                patternClause.getAsJsonObject().addProperty(SKIP_FORMATTING, true);
            }
        }
    }

    /**
     * format Match Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchExpressionNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
        if (node.has("expression")) {
            node.getAsJsonObject("expression").addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("patternClauses")) {
            for (JsonElement patternClause : node.getAsJsonArray("patternClauses")) {
                patternClause.getAsJsonObject().addProperty(SKIP_FORMATTING, true);
            }
        }
    }

    /**
     * format Match Expression Pattern Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchExpressionPatternClauseNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Match Pattern Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchPatternClauseNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Named Args Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatNamedArgsExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Object Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatObjectTypeNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Order By node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatOrderByNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Order By Variable node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatOrderByVariableNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Output Rate Limit node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatOutputRateLimitNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Pattern Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatPatternClauseNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Pattern Streaming Edge Input node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatPatternStreamingEdgeInputNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Pattern Streaming Input node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatPatternStreamingInputNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Post Increment node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatPostIncrementNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Record Literal Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRecordLiteralExprNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");
            String parentKind = node.getAsJsonObject("parent").get("kind").getAsString();
            if (parentKind.equals("Endpoint") || parentKind.equals("AnnotationAttachment") ||
                    parentKind.equals("Service") || parentKind.equals("Variable")) {
                this.preserveHeight(ws,
                        this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                }

                if (node.has("keyValuePairs")
                        && node.getAsJsonArray("keyValuePairs").size() <= 0) {
                    if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                        ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws",
                                NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject("position")
                                        .get("startColumn").getAsInt()) + NEW_LINE +
                                        this.getWhiteSpaces(node.getAsJsonObject("position")
                                                .get("startColumn").getAsInt()));
                    }
                } else {
                    if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                        ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws",
                                NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject("position")
                                        .get("startColumn").getAsInt()));
                    }
                }

                int indentedStartColumn = node.getAsJsonObject("position").get("startColumn").getAsInt()
                        + this.getWhiteSpaceCount(SPACE_TAB);

                if (node.has("keyValuePairs")) {
                    JsonArray keyValuePairs = node.getAsJsonArray("keyValuePairs");
                    for (int i = 0; i < keyValuePairs.size(); i++) {
                        JsonArray keyWs = keyValuePairs.get(i).getAsJsonObject()
                                .getAsJsonObject("key").get("ws").getAsJsonArray();
                        JsonArray valueWs = keyValuePairs.get(i).getAsJsonObject()
                                .getAsJsonObject("value").get("ws").getAsJsonArray();
                        JsonArray valuePairWs = keyValuePairs.get(i).getAsJsonObject()
                                .get("ws").getAsJsonArray();

                        this.preserveHeight(keyWs, this.getWhiteSpaces(indentedStartColumn));
                        this.preserveHeight(valueWs, this.getWhiteSpaces(indentedStartColumn));
                        this.preserveHeight(valuePairWs, this.getWhiteSpaces(indentedStartColumn));

                        if (!this.isHeightAvailable(keyWs.get(0).getAsJsonObject().get("ws").getAsString())) {
                            keyWs.get(0).getAsJsonObject().addProperty("ws",
                                    NEW_LINE + this.getWhiteSpaces(indentedStartColumn));
                        }

                        if (!this.isHeightAvailable(valueWs.get(0).getAsJsonObject().get("ws").getAsString())) {
                            valueWs.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                        }

                        if (!this.isHeightAvailable(valuePairWs.get(0).getAsJsonObject().get("ws").getAsString())) {
                            valuePairWs.get(0).getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                        }
                    }
                }

                for (int j = 0; j < ws.size(); j++) {
                    if (ws.get(j).getAsJsonObject().get("text").getAsString().equals(",") ||
                            ws.get(j).getAsJsonObject().get("text").getAsString().equals(";")) {
                        ws.get(j).getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                    }
                }
            }
        }
    }

    /**
     * format Record Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRecordTypeNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (!skip) {
            // Add the start column and sorted index
            JsonArray fields = node.getAsJsonArray("fields");
            for (int i = 0; i < fields.size(); i++) {
                JsonObject child = fields.get(i).getAsJsonObject();
                child.getAsJsonObject("position").addProperty("startColumn",
                        node.getAsJsonObject("position").get("startColumn").getAsInt()
                                + this.getWhiteSpaceCount(SPACE_TAB));
            }

            if (node.has("restFieldType") &&
                    node.get("restFieldType").getAsJsonObject().has("ws")) {
                node.get("restFieldType").getAsJsonObject().getAsJsonObject("position").addProperty("startColumn",
                        node.getAsJsonObject("position").get("startColumn").getAsInt()
                                + this.getWhiteSpaceCount(SPACE_TAB));
            }

            if (node.has("sealed") &&
                    node.get("sealed").getAsBoolean() &&
                    node.has("ws")) {
                JsonArray ws = node.getAsJsonArray("ws");
                this.preserveHeight(ws, this.getWhiteSpaces(node.getAsJsonObject("position")
                        .get("startColumn").getAsInt()) + SPACE_TAB);

                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(0).getAsJsonObject().addProperty("ws", NEW_LINE
                            + this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt())
                            + SPACE_TAB);
                }
            }
        }
    }

    /**
     * format Reply node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatReplyNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Resource node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatResourceNode(JsonObject node) {
        if (node.has("ws")) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws, this.getWhiteSpaces(node.getAsJsonObject("position")
                    .get("startColumn").getAsInt()));

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                ws.get(0).getAsJsonObject().addProperty("ws",
                        BETWEEN_BLOCK_SPACE +
                                this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 2).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
            }

            if (node.has("body")
                    && node.getAsJsonObject("body").getAsJsonArray("statements").size() <= 0
                    && node.getAsJsonArray("workers").size() <= 0
                    && node.getAsJsonArray("endpointNodes").size() <= 0) {
                if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws", NEW_LINE +
                            this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()) +
                            NEW_LINE +
                            this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws", NEW_LINE +
                        this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            }

            if (node.has("parameters")) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                for (int i = 0; i < parameters.size(); i++) {
                    parameters.get(i).getAsJsonObject().addProperty(SKIP_FORMATTING, true);
                }
            }

            if (node.has("endpointNodes")) {
                JsonArray endpointNodes = node.getAsJsonArray("endpointNodes");
                for (int i = 0; i < endpointNodes.size(); i++) {
                    endpointNodes.get(i).getAsJsonObject().getAsJsonObject("position")
                            .addProperty("startColumn",
                                    node.getAsJsonObject("position").get("startColumn").getAsInt()
                                            + this.getWhiteSpaceCount(SPACE_TAB));
                }
            }

            if (node.has("annotationAttachments")) {
                JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                for (int i = 0; i < annotationAttachments.size(); i++) {
                    annotationAttachments.get(i).getAsJsonObject().getAsJsonObject("position")
                            .addProperty("startColumn", node.getAsJsonObject("position").get("startColumn")
                                    .getAsInt());
                }
            }

            if (node.has("workers")) {
                JsonArray workers = node.getAsJsonArray("workers");
                for (int i = 0; i < workers.size(); i++) {
                    workers.get(i).getAsJsonObject().getAsJsonObject("position").addProperty("startColumn",
                            node.getAsJsonObject("position").get("startColumn").getAsInt() +
                                    this.getWhiteSpaceCount(SPACE_TAB));
                }
            }
        }
    }

    /**
     * format Rest Args Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRestArgsExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Retry node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRetryNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Return node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatReturnNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                ws.get(0).getAsJsonObject().addProperty("ws", NEW_LINE +
                        this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            }
        }
    }

    /**
     * format Select Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatSelectClauseNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Select Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatSelectExpressionNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Service node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatServiceNode(JsonObject node) {
        if (node.has("ws")) {
            JsonArray ws = node.getAsJsonArray("ws");
            JsonObject position = node.getAsJsonObject("position");

            this.preserveHeight(ws, this.getWhiteSpaces(position.get("startColumn").getAsInt()));
            int serviceIndex = this.findIndex(node);

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                String whiteSpace = serviceIndex > 0 ? BETWEEN_BLOCK_SPACE : EMPTY_SPACE;
                ws.get(0).getAsJsonObject().addProperty("ws", whiteSpace);
            } else if (!this.isNewLine(ws.get(0).getAsJsonObject().get("ws").getAsString().charAt(0) + "")
                    && serviceIndex != 0) {
                ws.get(0).getAsJsonObject().addProperty("ws",
                        NEW_LINE + ws.get(0).getAsJsonObject().get("ws").getAsString());
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 2).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
            }

            if (node.getAsJsonArray("resources").size() <= 0
                    && node.getAsJsonArray("variables").size() <= 0
                    && node.getAsJsonArray("endpointNodes").size() <= 0
                    && node.getAsJsonArray("namespaceDeclarations").size() <= 0) {
                if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws",
                            NEW_LINE + SPACE_TAB + NEW_LINE);
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws", NEW_LINE);
            }

            if (node.has("endpointNodes")) {
                JsonArray endpointNodes = node.getAsJsonArray("endpointNodes");
                for (int i = 0; i < endpointNodes.size(); i++) {
                    endpointNodes.get(i).getAsJsonObject().getAsJsonObject("position").addProperty("startColumn",
                            position.get("startColumn").getAsInt() + this.getWhiteSpaceCount(SPACE_TAB));
                }
            }

            if (node.has("resources")) {
                JsonArray resources = node.getAsJsonArray("resources");
                for (int i = 0; i < resources.size(); i++) {
                    resources.get(i).getAsJsonObject().getAsJsonObject("position").addProperty("startColumn",
                            position.get("startColumn").getAsInt() + this.getWhiteSpaceCount(SPACE_TAB));
                }
            }

            if (node.has("variables")) {
                JsonArray variables = node.getAsJsonArray("variables");
                for (int i = 0; i < variables.size(); i++) {
                    variables.get(i).getAsJsonObject().getAsJsonObject("position").addProperty("startColumn",
                            position.get("startColumn").getAsInt() + this.getWhiteSpaceCount(SPACE_TAB));
                }
            }

            if (node.has("annotationAttachments")) {
                JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                for (int i = 0; i < annotationAttachments.size(); i++) {
                    annotationAttachments.get(i).getAsJsonObject().getAsJsonObject("position")
                            .addProperty("startColumn", position.get("startColumn").getAsInt());
                }
            }

            if (node.has("anonymousEndpointBind")) {
                node.getAsJsonObject("anonymousEndpointBind").addProperty(SKIP_FORMATTING, true);
            }

            if (node.has("boundEndpoints")) {
                JsonArray boundEndpoints = node.getAsJsonArray("boundEndpoints");
                for (JsonElement boundEndpoint : boundEndpoints) {
                    boundEndpoint.getAsJsonObject().addProperty(SKIP_FORMATTING, true);
                }
            }

            if (node.has("workers")) {
                JsonArray workers = node.getAsJsonArray("workers");
                for (int i = 0; i < workers.size(); i++) {
                    workers.get(i).getAsJsonObject().getAsJsonObject("position").addProperty("startColumn",
                            position.get("startColumn").getAsInt() + this.getWhiteSpaceCount(SPACE_TAB));
                }
            }
        }
    }

    /**
     * format Simple Variable Ref node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatSimpleVariableRefNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");
            String parentKind = node.getAsJsonObject("parent").get("kind").getAsString();
            if (parentKind.equals("Assignment") || parentKind.equals("FieldBasedAccessExpr")) {
                this.preserveHeight(ws,
                        this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString()) &&
                        !(node.has("isExpression") && node.get("isExpression").getAsBoolean())) {
                    if (node.has("parent") &&
                            node.getAsJsonObject("parent").has("declaredWithVar") &&
                            node.getAsJsonObject("parent").get("declaredWithVar").getAsBoolean()) {
                        ws.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                    } else {
                        ws.get(0).getAsJsonObject().addProperty("ws", NEW_LINE
                                + this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
                    }
                }
            }
        }
    }

    /**
     * format Statement Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStatementExpressionNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Stream Action node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStreamActionNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Streaming Query node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStreamingQueryNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format String Template Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStringTemplateLiteralNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Ternary Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTernaryExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Throw node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatThrowNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Transaction node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTransactionNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Try node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTryNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            if (node.has("catchBlocks")) {
                for (JsonElement catchBlock : node.getAsJsonArray("catchBlocks")) {
                    catchBlock.getAsJsonObject().getAsJsonObject("position").addProperty("startColumn",
                            node.getAsJsonObject("position").get("startColumn").getAsInt());
                }
            }
        }
    }

    /**
     * format Tuple Destructure node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTupleDestructureNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Tuple Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTupleTypeNodeNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Type Cast Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeCastExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Type Conversion Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeConversionExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
        if (node.has("expression")) {
            node.getAsJsonObject("expression").addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("typeNode")) {
            node.getAsJsonObject("typeNode").addProperty(SKIP_FORMATTING, true);
        }
    }

    /**
     * format Type Definition node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeDefinitionNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean())
                || (!(node.has("isObjectType") && node.get("isObjectType").getAsBoolean())
                && !(node.has("isRecordType") && node.get("isRecordType").getAsBoolean())));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            int typeDefinitionIndex = this.findIndex(node);

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                String whiteSpace = typeDefinitionIndex > 0 ? BETWEEN_BLOCK_SPACE : EMPTY_SPACE;
                ws.get(0).getAsJsonObject().addProperty("ws", whiteSpace);
            } else if (!this.isNewLine(ws.get(0).getAsJsonObject().get("ws").getAsString().charAt(0) + "")
                    && typeDefinitionIndex != 0) {
                ws.get(0).getAsJsonObject().addProperty("ws", NEW_LINE +
                        ws.get(0).getAsJsonObject().get("ws").getAsString());
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 3).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 3).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
            }

            if (node.has("typeNode")
                    && node.getAsJsonObject("typeNode").getAsJsonArray("fields").size() <= 0) {
                if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(ws.size() - 2).getAsJsonObject().addProperty("ws",
                            NEW_LINE + SPACE_TAB + NEW_LINE);
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 2).getAsJsonObject().addProperty("ws", NEW_LINE);
            }

            if (node.has("typeNode")) {
                node.getAsJsonObject("typeNode").getAsJsonObject("position").addProperty("startColumn",
                        node.getAsJsonObject("position").get("startColumn").getAsInt());
            }
        }
    }

    /**
     * format Typedesc Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypedescExpressionNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Type Init Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeInitExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Unary Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatUnaryExprNode(JsonObject node) {
        node.addProperty(SKIP_FORMATTING, true);
    }

    /**
     * format Union Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatUnionTypeNodeNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("memberTypeNodes")) {
            for (JsonElement jsonElement : node.getAsJsonArray("memberTypeNodes")) {
                jsonElement.getAsJsonObject().addProperty(SKIP_FORMATTING, true);
            }
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");
            if (node.has("grouped") && node.get("grouped").getAsBoolean()) {
                this.preserveHeight(ws, this.getWhiteSpaces(node.getAsJsonObject("position")
                        .get("startColumn").getAsInt()));
                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                    node.getAsJsonArray("ws").get(0).getAsJsonObject()
                            .addProperty("ws", EMPTY_SPACE);
                }
            } else {
                node.getAsJsonArray("ws").get(0).getAsJsonObject().addProperty("ws", EMPTY_SPACE);
            }
        }
    }

    /**
     * format User Defined Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatUserDefinedTypeNode(JsonObject node) {
        if (node.has("isAnonType")
                && node.has("anonType")
                && node.get("isAnonType").getAsBoolean()) {
            node.getAsJsonObject("anonType").getAsJsonObject("position").addProperty("startColumn",
                    node.getAsJsonObject("position").get("startColumn").getAsInt());
        }
    }

    /**
     * format Value Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatValueTypeNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            JsonArray ws = node.getAsJsonArray("ws");

            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                ws.get(0).getAsJsonObject().addProperty("ws",
                        this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            }
        }
    }

    /**
     * format Variable Def node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatVariableDefNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            this.preserveHeight(node.getAsJsonArray("ws"),
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

            node.getAsJsonObject("variable").getAsJsonObject("position").addProperty("startColumn",
                    node.getAsJsonObject("position").get("startColumn").getAsInt());

            if (!this.isHeightAvailable(node.getAsJsonArray("ws").get(0)
                    .getAsJsonObject().get("ws").getAsString())) {
                node.getAsJsonArray("ws").get(0).getAsJsonObject()
                        .addProperty("ws", EMPTY_SPACE);
            }
        }
    }

    /**
     * format Variable node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatVariableNode(JsonObject node) {
        // skip to make formatting fault tolerance till fully implemented.
        boolean skip = ((node.has(SKIP_FORMATTING) && node.get(SKIP_FORMATTING).getAsBoolean()) ||
                (node.has("parent") && node.getAsJsonObject("parent").has(SKIP_FORMATTING) &&
                        node.getAsJsonObject("parent").get(SKIP_FORMATTING).getAsBoolean()));

        if (skip) {
            node.addProperty(SKIP_FORMATTING, true);
        }

        if (node.has("ws") && !skip) {
            String parentKind = node.getAsJsonObject("parent").get("kind").getAsString();

            this.preserveHeight(node.getAsJsonArray("ws"),
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            int variableIndex = this.findIndex(node);

            if (parentKind.equals("VariableDef") || parentKind.equals("CompilationUnit")
                    || parentKind.equals("RecordType")) {

                if (node.has("typeNode") &&
                        !node.getAsJsonObject("typeNode").get("kind").getAsString().equals("ArrayType")) {
                    JsonObject typeNode = node.getAsJsonObject("typeNode");
                    typeNode.getAsJsonObject("position").addProperty("startColumn",
                            node.getAsJsonObject("position").get("startColumn").getAsInt());
                    if (typeNode.has("ws")) {
                        this.preserveHeight(typeNode.getAsJsonArray("ws"),
                                this.getWhiteSpaces(node.getAsJsonObject("position")
                                        .get("startColumn").getAsInt()));

                        if (variableIndex > 0 || variableIndex < 0) {
                            if (!this.isHeightAvailable(typeNode.getAsJsonArray("ws").get(0)
                                    .getAsJsonObject().get("ws").getAsString()) &&
                                    !typeNode.has("type")) {

                                if ((node.has("final") && node.get("final").getAsBoolean()) ||
                                        (node.has("public") && node.get("public").getAsBoolean())) {
                                    typeNode.addProperty(SKIP_FORMATTING, true);
                                    typeNode.getAsJsonArray("ws").get(0).getAsJsonObject()
                                            .addProperty("ws", SINGLE_SPACE);
                                } else {
                                    typeNode.getAsJsonArray("ws").get(0).getAsJsonObject()
                                            .addProperty("ws", NEW_LINE
                                                    + this.getWhiteSpaces(node.getAsJsonObject("position")
                                                    .get("startColumn").getAsInt()));
                                }
                            }
                        } else {
                            if (!this.isHeightAvailable(typeNode.getAsJsonArray("ws").get(0)
                                    .getAsJsonObject().get("ws").getAsString())) {

                                if (parentKind.equals("RecordType")) {
                                    if (node.has("final") &&
                                            node.get("final").getAsBoolean() ||
                                            (node.has("public") && node.get("public").getAsBoolean())) {
                                        typeNode.addProperty(SKIP_FORMATTING, true);
                                        typeNode.getAsJsonArray("ws").get(0).getAsJsonObject()
                                                .addProperty("ws", SINGLE_SPACE);
                                    } else {
                                        typeNode.getAsJsonArray("ws").get(0).getAsJsonObject()
                                                .addProperty("ws", NEW_LINE +
                                                        this.getWhiteSpaces(node.getAsJsonObject("position")
                                                                .get("startColumn").getAsInt()));
                                    }
                                } else {
                                    if (node.has("final") &&
                                            node.get("final").getAsBoolean() ||
                                            (node.has("public") && node.get("public").getAsBoolean())) {
                                        typeNode.addProperty(SKIP_FORMATTING, true);
                                        typeNode.getAsJsonArray("ws").get(0).getAsJsonObject()
                                                .addProperty("ws", SINGLE_SPACE);
                                    } else {
                                        typeNode.getAsJsonArray("ws").get(0).getAsJsonObject()
                                                .addProperty("ws", EMPTY_SPACE);
                                    }
                                }
                            }
                        }
                    } else if (node.has("isAnonType")
                            && node.get("isAnonType").getAsBoolean()
                            && node.getAsJsonArray("ws").size() > 3) {
                        if (!this.isHeightAvailable(node.getAsJsonArray("ws").get(0)
                                .getAsJsonObject().get("ws").getAsString())) {
                            if (node.has("final") && node.get("final").getAsBoolean()) {
                                node.getAsJsonArray("ws").get(0).getAsJsonObject().addProperty("ws",
                                        SINGLE_SPACE);
                            } else {
                                node.getAsJsonArray("ws").get(0).getAsJsonObject().addProperty("ws",
                                        NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject("position")
                                                .get("startColumn").getAsInt()));
                            }
                        }

                        if (!this.isHeightAvailable(node.getAsJsonArray("ws").get(1)
                                .getAsJsonObject().get("ws").getAsString())) {
                            node.getAsJsonArray("ws").get(1).getAsJsonObject()
                                    .addProperty("ws", SINGLE_SPACE);
                        }

                        if (typeNode.has("anonType")
                                && typeNode.getAsJsonObject("anonType").has("fields")
                                && typeNode.getAsJsonObject("anonType").getAsJsonArray("fields").size() <= 0) {
                            if (!this.isHeightAvailable(node.getAsJsonArray("ws").get(2)
                                    .getAsJsonObject().get("ws").getAsString())) {
                                node.getAsJsonArray("ws").get(2).getAsJsonObject()
                                        .addProperty("ws", NEW_LINE + SPACE_TAB + NEW_LINE);
                            }
                        } else {
                            if (!this.isHeightAvailable(node.getAsJsonArray("ws").get(2)
                                    .getAsJsonObject().get("ws").getAsString())) {
                                node.getAsJsonArray("ws").get(2).getAsJsonObject()
                                        .addProperty("ws", NEW_LINE);
                            }
                        }
                    }
                } else if (node.has("typeNode")) {
                    JsonObject typeNode = node.getAsJsonObject("typeNode");
                    typeNode.getAsJsonObject("position").addProperty("startColumn",
                            node.getAsJsonObject("position").get("startColumn").getAsInt());
                    if (typeNode.has("ws")) {
                        this.preserveHeight(typeNode.getAsJsonArray("ws"),
                                this.getWhiteSpaces(node.getAsJsonObject("position")
                                        .get("startColumn").getAsInt()));
                        if (!this.isHeightAvailable(typeNode.getAsJsonArray("ws").get(0)
                                .getAsJsonObject().get("ws").getAsString())) {
                            typeNode.getAsJsonArray("ws").get(0).getAsJsonObject()
                                    .addProperty("ws", EMPTY_SPACE);
                        }
                    }
                }

                if (node.has("initialExpression")) {
                    node.getAsJsonObject("initialExpression").getAsJsonObject("position")
                            .addProperty("startColumn",
                                    node.getAsJsonObject("position").get("startColumn").getAsInt());
                }

                if (node.has("annotationAttachments")) {
                    JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                    for (int i = 0; i < annotationAttachments.size(); i++) {
                        annotationAttachments.get(i).getAsJsonObject().getAsJsonObject("position")
                                .addProperty("startColumn", node.getAsJsonObject("position").get("startColumn")
                                        .getAsInt());
                    }
                }
            }
        }
    }

    /**
     * format Where node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWhereNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format While node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWhileNode(JsonObject node) {
        if (node.has("ws")) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                ws.get(0).getAsJsonObject().addProperty("ws",
                        NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject("position")
                                .get("startColumn").getAsInt()));
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 2).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
            }

            if (node.has("body")
                    && node.getAsJsonObject("body").getAsJsonArray("statements").size() <= 0) {
                if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(ws.size() - 1).getAsJsonObject()
                            .addProperty("ws", NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject("position")
                                            .get("startColumn").getAsInt())
                                    + NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject("position")
                                            .get("startColumn").getAsInt()));
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws",
                        NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject("position")
                                .get("startColumn").getAsInt()));
            }

            if (node.has("condition") && node.getAsJsonObject("condition").has("ws")) {
                JsonArray conditionWs = node.getAsJsonObject("condition").getAsJsonArray("ws");
                this.preserveHeight(conditionWs, this.getWhiteSpaces(node.getAsJsonObject("position")
                        .get("startColumn").getAsInt()));

                if (!this.isCommentAvailable(conditionWs.get(0).getAsJsonObject().get("ws").getAsString())) {
                    conditionWs.get(0).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                }

                if (!this.isHeightAvailable(conditionWs.get(conditionWs.size() - 1).getAsJsonObject()
                        .get("ws").getAsString())) {
                    conditionWs.get(conditionWs.size() - 1).getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                }
            }
        }
    }

    /**
     * format Window Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWindowClauseNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Within node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWithinNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Worker node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWorkerNode(JsonObject node) {
        if (node.has("ws")) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get("ws").getAsString())) {
                ws.get(0).getAsJsonObject().addProperty("ws", NEW_LINE +
                        this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 3).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 3).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 2).getAsJsonObject().addProperty("ws", SINGLE_SPACE);
            }

            if (node.has("body")
                    && node.getAsJsonObject("body").getAsJsonArray("statements").size() <= 0
                    && node.getAsJsonArray("workers").size() <= 0
                    && node.getAsJsonArray("endpointNodes").size() <= 0) {
                if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                    ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws", NEW_LINE +
                            this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()) +
                            NEW_LINE +
                            this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get("ws").getAsString())) {
                ws.get(ws.size() - 1).getAsJsonObject().addProperty("ws", NEW_LINE +
                        this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            }

            if (node.has("endpointNodes")) {
                JsonArray endpointNodes = node.getAsJsonArray("endpointNodes");
                for (int i = 0; i < endpointNodes.size(); i++) {
                    endpointNodes.get(i).getAsJsonObject().getAsJsonObject("position")
                            .addProperty("startColumn",
                                    node.getAsJsonObject("position").get("startColumn").getAsInt()
                                            + this.getWhiteSpaceCount(SPACE_TAB));
                }
            }

            if (node.has("annotationAttachments")) {
                JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                for (int i = 0; i < annotationAttachments.size(); i++) {
                    annotationAttachments.get(i).getAsJsonObject().getAsJsonObject("position")
                            .addProperty("startColumn", node.getAsJsonObject("position").get("startColumn")
                                    .getAsInt());
                }
            }
        }
    }

    /**
     * format Worker Receive node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWorkerReceiveNode(JsonObject node) {
        // Not implemented.
    }

    /**
     * format Worker Send node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWorkerSendNode(JsonObject node) {
        // Not implemented.
    }

    // End Formatting utils
}
