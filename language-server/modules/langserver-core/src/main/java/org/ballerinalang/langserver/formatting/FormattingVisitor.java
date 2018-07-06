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
 * Visitor for formatting source.
 */
public class FormattingVisitor {

    private static final String TAB = "\t";
    private static final String SPACE_TAB = "   ";
    private static final String BETWEEN_BLOCK_SPACE = "\r\n\r\n";
    private static final String SINGLE_SPACE = " ";
    private static final String NEW_LINE = "\r\n";
    private static final String EMPTY_SPACE = "";

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

    private void formatCompilationUnitNode(JsonObject node) {
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
                ws.get(0).getAsJsonObject().addProperty("ws", "\n");
            } else if (!this.isNewLine(ws.get(0).getAsJsonObject().get("ws").getAsString()
                    .charAt(ws.get(0).getAsJsonObject().get("ws").getAsString().length() - 1) + "")) {
                ws.get(0).getAsJsonObject().addProperty("ws",
                        (ws.get(0).getAsJsonObject().get("ws").getAsString() + "\n"));
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

    private void formatImportNode(JsonObject node) {
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

    private void formatBlockNode(JsonObject node) {
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
    }

    private void formatVariableDefNode(JsonObject node) {
        if (node.has("ws")) {
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

    private void formatVariableNode(JsonObject node) {
        if (node.has("ws")) {
            String parentKind = node.getAsJsonObject("parent").get("kind").getAsString();


            this.preserveHeight(node.getAsJsonArray("ws"),
                    this.getWhiteSpaces(node.getAsJsonObject("position").get("startColumn").getAsInt()));
            int variableIndex = this.findIndex(node);

            if (parentKind.equals("VariableDef") || parentKind.equals("CompilationUnit")
                    || parentKind.equals("RecordType")) {

                if (node.has("typeNode")) {
                    JsonObject typeNode = node.getAsJsonObject("typeNode");
                    typeNode.getAsJsonObject("position").addProperty("startColumn",
                            node.getAsJsonObject("position").get("startColumn").getAsInt());
                    if (typeNode.has("ws")) {
                        this.preserveHeight(typeNode.getAsJsonArray("ws"),
                                this.getWhiteSpaces(node.getAsJsonObject("position")
                                        .get("startColumn").getAsInt()));

                        if (variableIndex > 0 || variableIndex < 0) {
                            if (!this.isHeightAvailable(typeNode.getAsJsonArray("ws").get(0)
                                    .getAsJsonObject().get("ws").getAsString())) {
                                typeNode.getAsJsonArray("ws").get(0).getAsJsonObject()
                                        .addProperty("ws", NEW_LINE
                                                + this.getWhiteSpaces(node.getAsJsonObject("position")
                                                .get("startColumn").getAsInt()));
                            }
                        } else {
                            if (!this.isHeightAvailable(typeNode.getAsJsonArray("ws").get(0)
                                    .getAsJsonObject().get("ws").getAsString())) {
                                if (parentKind.equals("RecordType")) {
                                    typeNode.getAsJsonArray("ws").get(0).getAsJsonObject()
                                            .addProperty("ws", NEW_LINE +
                                                    this.getWhiteSpaces(node.getAsJsonObject("position")
                                                            .get("startColumn").getAsInt()));
                                } else {
                                    typeNode.getAsJsonArray("ws").get(0).getAsJsonObject()
                                            .addProperty("ws", EMPTY_SPACE);
                                }
                            }
                        }
                    } else if (node.has("isAnonType")
                            && node.get("isAnonType").getAsBoolean()
                            && node.getAsJsonArray("ws").size() > 3) {
                        if (!this.isHeightAvailable(node.getAsJsonArray("ws").get(0)
                                .getAsJsonObject().get("ws").getAsString())) {
                            node.getAsJsonArray("ws").get(0).getAsJsonObject().addProperty("ws",
                                    NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject("position")
                                            .get("startColumn").getAsInt()));
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
                }
            }
        }
    }

    private void formatAnnotationAttachmentNode(JsonObject node) {
        if (node.has("ws")) {
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

    private void formatEndpointNode(JsonObject node) {
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

    private void formatFunctionNode(JsonObject node) {
        if (node.has("ws")) {
            JsonArray ws = node.getAsJsonArray("ws");
            this.preserveHeight(ws, this.getWhiteSpaces(node.getAsJsonObject("position")
                    .get("startColumn").getAsInt()));
            int functionIndex = this.findIndex(node);

            if (!this.isHeightAvailable(ws.get(0)
                    .getAsJsonObject().get("ws").getAsString())) {
                String whiteSpace = (functionIndex > 0 || node.getAsJsonObject("parent")
                        .get("kind").getAsString().equals("ObjectType"))
                        ? (BETWEEN_BLOCK_SPACE + this.getWhiteSpaces(node.getAsJsonObject("position")
                        .get("startColumn").getAsInt()))
                        : EMPTY_SPACE;
                ws.get(0).getAsJsonObject().addProperty("ws", whiteSpace);
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
                for (int i = 0; i < node.getAsJsonArray("parameters").size(); i++) {
                    if (node.getAsJsonArray("parameters")
                            .get(i).getAsJsonObject().has("typeNode")) {
                        if (i == 0) {
                            node.getAsJsonArray("parameters").get(i)
                                    .getAsJsonObject().get("typeNode").getAsJsonObject().get("ws")
                                    .getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                        } else {
                            node.getAsJsonArray("parameters").get(i)
                                    .getAsJsonObject().get("typeNode").getAsJsonObject().get("ws")
                                    .getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                        }
                    }
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
        }
    }

    private void formatResourceNode(JsonObject node) {
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
                    if (parameters.get(i).getAsJsonObject().has("typeNode")) {
                        if (i == 0) {
                            parameters.get(i).getAsJsonObject().get("typeNode").getAsJsonObject()
                                    .getAsJsonArray("ws").get(0)
                                    .getAsJsonObject().addProperty("ws", EMPTY_SPACE);
                        } else {
                            parameters.get(i).getAsJsonObject().get("typeNode").getAsJsonObject()
                                    .getAsJsonArray("ws").get(0)
                                    .getAsJsonObject().addProperty("ws", SINGLE_SPACE);
                        }
                    }
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
        }
    }

    private void formatServiceNode(JsonObject node) {
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
        }
    }

    // End Formatting utils

    /**
     * Begin the visit (top to bottom).
     *
     * @param node ballerina node as a json object
     */
    public void beginVisit(JsonObject node) {
        switch (node.get("kind").getAsString()) {
            case "CompilationUnit":
                formatCompilationUnitNode(node);
                break;
            case "Import":
                formatImportNode(node);
                break;
            case "Block":
                formatBlockNode(node);
                break;
            case "VariableDef":
                formatVariableDefNode(node);
                break;
            case "Variable":
                formatVariableNode(node);
                break;
            case "AnnotationAttachment":
                formatAnnotationAttachmentNode(node);
                break;
            case "Endpoint":
                formatEndpointNode(node);
                break;
            case "Function":
                formatFunctionNode(node);
                break;
            case "Resource":
                formatResourceNode(node);
                break;
            case "Service":
                formatServiceNode(node);
                break;
            default:
                break;
        }
    }

    /**
     * End the visit (bottom to top).
     *
     * @param node ballerina node as a json object
     */
    public void endVisit(JsonObject node) {
        // No Implementation
    }
}
