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

    private String getWhiteSpaces(int column) {
        StringBuilder whiteSpaces = new StringBuilder();
        for (int i = 0; i <= (column - 1); i++) {
            whiteSpaces.append(" ");
        }

        return whiteSpaces.toString();
    }

    private String getNewLines(int column) {
        StringBuilder newLines = new StringBuilder();
        for (int i = 0; i < column; i++) {
            newLines.append("\n");
        }

        return newLines.toString();
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
            if (ws.get(i).isJsonObject() && ws.get(i).getAsJsonObject().has(FormattingConstants.WS) &&
                    (ws.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsString().trim().length() > 0 ||
                            ws.get(i).getAsJsonObject().get(FormattingConstants.WS).getAsString().contains("\n"))) {
                List<String> tokens = this.tokenizer(ws.get(i).getAsJsonObject()
                        .get(FormattingConstants.WS).getAsString());
                ws.get(i).getAsJsonObject().addProperty(FormattingConstants.WS,
                        this.getTextFromTokens(tokens, indent));
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
            if (entry.getValue().isJsonArray() && !entry.getKey().equals(FormattingConstants.WS)) {
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

    private void skipFormatting(JsonObject node, boolean doSkip) {
        // Add skipFormatting field as a property to the node.
        node.addProperty(FormattingConstants.SKIP_FORMATTING, doSkip);

        for (Map.Entry<String, JsonElement> child : node.entrySet()) {
            // If child element is not parent, position or ws continue.
            if (!child.getKey().equals("parent") && !child.getKey().equals(FormattingConstants.POSITION) &&
                    !child.getKey().equals(FormattingConstants.WS)) {
                // If child is a object and has a kind, do skip formatting
                // else if child is a array iterate and skip formatting for child items.
                if (child.getValue().isJsonObject() && child.getValue().getAsJsonObject().has("kind")) {
                    skipFormatting(child.getValue().getAsJsonObject(), doSkip);
                } else if (child.getValue().isJsonArray()) {
                    for (int i = 0; i < child.getValue().getAsJsonArray().size(); i++) {
                        JsonElement childItem = child.getValue().getAsJsonArray().get(i);
                        if (childItem.isJsonObject() && childItem.getAsJsonObject().has("kind")) {
                            skipFormatting(childItem.getAsJsonObject(), doSkip);
                        }
                    }
                }
            }
        }
    }

    private JsonObject getFormattingConfig(int newLineCount, int spacesCount, int startColumn, boolean doIndent) {
        JsonObject formattingConfig = new JsonObject();
        formattingConfig.addProperty(FormattingConstants.NEW_LINE_COUNT, newLineCount);
        formattingConfig.addProperty(FormattingConstants.SPACE_COUNT, spacesCount);
        formattingConfig.addProperty(FormattingConstants.START_COLUMN, startColumn);
        formattingConfig.addProperty(FormattingConstants.DO_INDENT, doIndent);
        return formattingConfig;
    }

    // Start Formatting utils

    /**
     * format abort node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAbortNode(JsonObject node) {
        // TODO: fix formatting for abort node.
        this.skipFormatting(node, true);
    }

    /**
     * format annotation node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationNode(JsonObject node) {
        // TODO: fix formatting for annotation node.
        this.skipFormatting(node, true);
    }

    /**
     * format annotation attachment node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationAttachmentNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Preserve any comments or new lines added by user.
            this.preserveHeight(node.getAsJsonArray(FormattingConstants.WS), indentation);

            // Update whitespace for annotation symbol, @.
            JsonObject annotationSymbolWhitespace = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(annotationSymbolWhitespace.get(FormattingConstants.WS).getAsString())) {
                annotationSymbolWhitespace.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            } else if (!this.isNewLine(annotationSymbolWhitespace.get(FormattingConstants.WS)
                    .getAsString().charAt(0) + "")) {
                annotationSymbolWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                        indentation);
            }

            // Update whitespace for annotation identifier.
            JsonObject identifierWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (!this.isHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update whitespace for expression.
            if (node.has("expression") && node.getAsJsonObject("expression").has(FormattingConstants.WS)) {
                JsonObject expression = node.getAsJsonObject("expression");
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1,
                        this.getWhiteSpaceCount(indentation), false);
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }
        }
    }

    /**
     * format annotation attribute node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationAttributeNode(JsonObject node) {
        // TODO: fix formatting for annotation attribute.
        this.skipFormatting(node, true);
    }

    /**
     * format annotation attachment attribute node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationAttachmentAttributeNode(JsonObject node) {
        // TODO: fix formatting for annotation attachment attribute.
        this.skipFormatting(node, true);
    }

    /**
     * format annotation attachment attribute value node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationAttachmentAttributeValueNode(JsonObject node) {
        // TODO: fix formatting for annotation attachment attribute value.
        this.skipFormatting(node, true);
    }

    /**
     * format Array Literal Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatArrayLiteralExprNode(JsonObject node) {
        // TODO: fix formatting for array literal.
        this.skipFormatting(node, true);
    }

    /**
     * format Array Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatArrayTypeNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Update whitespace for element type.
            if (node.has("elementType")) {
                node.getAsJsonObject("elementType").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }
        }
    }

    /**
     * format Assignment node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAssignmentNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Get the indentation for the node.
            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Preserve comments and new lines added by the user.
            this.preserveHeight(ws, indentation);

            if (node.has("declaredWithVar") && node.get("declaredWithVar").getAsBoolean()) {
                // Update whitespaces for var.
                JsonObject varWhitespace = ws.get(0).getAsJsonObject();
                if (!this.isHeightAvailable(varWhitespace.get(FormattingConstants.WS).getAsString())) {
                    // If declared with var add new line
                    varWhitespace.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                    indentation);
                }

                // Update whitespace for variable when declared with var.
                if (node.has("variable")) {
                    JsonObject variable = node.getAsJsonObject("variable");
                    JsonObject variableFormatConfig = this.getFormattingConfig(0, 1,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    variable.add(FormattingConstants.FORMATTING_CONFIG, variableFormatConfig);
                }

                // Update whitespace for =
                JsonObject equalWhitespace = ws.get(1).getAsJsonObject();
                if (!this.isHeightAvailable(equalWhitespace.get(FormattingConstants.WS).getAsString())) {
                    equalWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }

                // Update whitespace for ;
                JsonObject semicolonWhitespace = ws.get(2).getAsJsonObject();
                if (!this.isHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
                    semicolonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            } else {
                // Update whitespace for variable when not declared with var.
                if (node.has("variable")) {
                    JsonObject variable = node.getAsJsonObject("variable");
                    JsonObject variableFormatConfig =
                            this.getFormattingConfig(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                    0, formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                    formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                    variable.add(FormattingConstants.FORMATTING_CONFIG, variableFormatConfig);
                }

                // Update whitespace for =
                JsonObject equalWhitespace = ws.get(0).getAsJsonObject();
                if (!this.isHeightAvailable(equalWhitespace.get(FormattingConstants.WS).getAsString())) {
                    equalWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }

                // Update whitespace for ;
                JsonObject semicolonWhitespace = ws.get(1).getAsJsonObject();
                if (!this.isHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
                    semicolonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            }

            // Update whitespaces for the expression.
            if (node.has("expression") && node.getAsJsonObject("expression").has(FormattingConstants.WS)) {
                JsonObject expression = node.getAsJsonObject("expression");
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }
        }
    }

    /**
     * format Await Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAwaitExprNode(JsonObject node) {
        // TODO: fix formatting for await expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Binary Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBinaryExprNode(JsonObject node) {
        // TODO: fix formatting for binary expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Bind node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBindNode(JsonObject node) {
        // TODO: fix formatting for bind.
        this.skipFormatting(node, true);
    }

    /**
     * format Block node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBlockNode(JsonObject node) {
        JsonObject position = new JsonObject();

        // Get the start column of the parent.
        position.addProperty(FormattingConstants.START_COLUMN, node.get("parent").getAsJsonObject()
                .get(FormattingConstants.POSITION)
                .getAsJsonObject().get(FormattingConstants.START_COLUMN).getAsInt());

        // Add block position to be the parent's position.
        node.add(FormattingConstants.POSITION, position);

        // Update the statements whitespaces.
        for (int i = 0; i < node.getAsJsonArray(FormattingConstants.STATEMENTS).size(); i++) {
            JsonElement child = node.getAsJsonArray(FormattingConstants.STATEMENTS).get(i);
            JsonObject formattingConfig = this.getFormattingConfig(1, 0,
                    node.get(FormattingConstants.POSITION).getAsJsonObject().get(FormattingConstants.START_COLUMN)
                            .getAsInt(), true);
            child.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, formattingConfig);
        }

        // If this is a else block continue to following.
        if (node.has(FormattingConstants.WS) && node.getAsJsonArray(FormattingConstants.WS).get(0).getAsJsonObject()
                .get(FormattingConstants.TEXT).getAsString().equals("else")) {
            // TODO: revisit the logic.
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            this.preserveHeight(ws, this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                    .get(FormattingConstants.START_COLUMN).getAsInt()));

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get(FormattingConstants.WS)
                    .getAsString())) {
                ws.get(ws.size() - 2).getAsJsonObject().addProperty(FormattingConstants.WS,
                        FormattingConstants.SINGLE_SPACE);
            }

            if (node.getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0) {
                if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get(FormattingConstants.WS)
                        .getAsString())) {
                    ws.get(ws.size() - 1).getAsJsonObject()
                            .addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                            .get(FormattingConstants.START_COLUMN).getAsInt())
                                    + FormattingConstants.NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                            .get(FormattingConstants.START_COLUMN).getAsInt()));
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get(FormattingConstants.WS)
                    .getAsString())) {
                ws.get(ws.size() - 1).getAsJsonObject().addProperty(FormattingConstants.WS,
                        FormattingConstants.NEW_LINE +
                                this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt()));
            }
        }
    }

    /**
     * format Braced Tuple Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBracedTupleExprNode(JsonObject node) {
        // TODO: fix formatting for braced tuple expressions.
        this.skipFormatting(node, true);
    }

    /**
     * format Break node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBreakNode(JsonObject node) {
        // TODO: fix formatting for break node.
        this.skipFormatting(node, true);
    }

    /**
     * format built in ref type.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBuiltInRefTypeNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Get the indentation for the node.
            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                            FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);

            // Update the ref type whitespace.
            JsonObject refTypeWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (!this.isHeightAvailable(refTypeWhitespace.get(FormattingConstants.WS).getAsString())) {
                refTypeWhitespace.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            }
        }
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
        // TODO: fix formatting for check expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Compilation Unit node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatCompilationUnitNode(JsonObject node) {
        // Update whitespaces for top level nodes.
        JsonArray topLevelNodes = node.get("topLevelNodes").getAsJsonArray();
        for (int i = 0; i < topLevelNodes.size(); i++) {
            JsonElement child = topLevelNodes.get(i);
            JsonObject formatConfig;
            if (i == 0) {
                formatConfig = this.getFormattingConfig(0, 0,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false);
            } else if (child.getAsJsonObject().has("kind") &&
                    child.getAsJsonObject().get("kind").getAsString().equals("Import")) {
                formatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false);
            } else {
                formatConfig = this.getFormattingConfig(2, 0,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false);
            }
            child.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
        }

        // Update EOF whitespace.
        if (node.has(FormattingConstants.WS) && topLevelNodes.size() > 0) {
            JsonArray ws = node.get(FormattingConstants.WS).getAsJsonArray();

            // preserve comment available before EOF.
            this.preserveHeight(ws, null);

            // Handle adding a new line at the EOF.
            JsonObject eofWS = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(eofWS.get(FormattingConstants.WS).getAsString())) {
                eofWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE);
            } else if (!this.isNewLine(eofWS.get(FormattingConstants.WS).getAsString()
                    .charAt(eofWS.get(FormattingConstants.WS).getAsString().length() - 1) + "")) {
                eofWS.addProperty(FormattingConstants.WS, (eofWS.get(FormattingConstants.WS).getAsString() +
                        FormattingConstants.NEW_LINE));
            }
        }

        // Handle import sorting according to the alphabetical order.
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
                        String refWS = topLevelNodes.get(j).getAsJsonObject().get(FormattingConstants.WS)
                                .getAsJsonArray().get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString();

                        String compWS = topLevelNodes.get(j + 1).getAsJsonObject().get(FormattingConstants.WS)
                                .getAsJsonArray().get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString();

                        JsonElement tempNode = topLevelNodes.get(j);
                        topLevelNodes.set(j, topLevelNodes.get(j + 1));
                        tempNode.getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray().get(0)
                                .getAsJsonObject().addProperty(FormattingConstants.WS, compWS);
                        topLevelNodes.get(j).getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray()
                                .get(0).getAsJsonObject().addProperty(FormattingConstants.WS, refWS);
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
        // TODO: fix formatting for compound assignment.
        this.skipFormatting(node, true);
    }

    /**
     * format Constrained Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatConstrainedTypeNode(JsonObject node) {
        // TODO: fix formatting for constrained type.
        this.skipFormatting(node, true);
    }

    /**
     * format Deprecated node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatDeprecatedNode(JsonObject node) {
        // TODO: fix formatting for deprecated.
        this.skipFormatting(node, true);
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Get the indentation for the node.
            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Set start column as to the updated indentation.
            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            // Preserve user added comments and new lines.
            this.preserveHeight(node.getAsJsonArray(FormattingConstants.WS), indentation);

            int endpointIndex = this.findIndex(node);

            // Update whitespace for endpoint/public keyword.
            JsonObject endpointKeyWord = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(endpointKeyWord.get(FormattingConstants.WS).getAsString())) {
                // If annotation or documentation attachments exists add only one new line.
                // Else add given number of new lines.
                String whiteSpace = ((node.has("annotationAttachments") &&
                        node.getAsJsonArray("annotationAttachments").size() > 0) ||
                        (node.has("documentationAttachments") &&
                                node.getAsJsonArray("documentationAttachments").size() > 0))
                        ? (FormattingConstants.NEW_LINE + indentation)
                        : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                        indentation);
                endpointKeyWord.addProperty(FormattingConstants.WS, whiteSpace);
            } else if (!this.isNewLine(endpointKeyWord.get(FormattingConstants.WS).getAsString().charAt(0) + "")
                    && endpointIndex != 0) {
                // TODO: revisit the logic.
                endpointKeyWord.addProperty(FormattingConstants.WS,
                        FormattingConstants.NEW_LINE + endpointKeyWord.get(FormattingConstants.WS).getAsString());
            }


            // Update whitespaces for identifier.
            JsonObject identifierWhitespace = ws.get((ws.size() - 2)).getAsJsonObject();
            if (!this.isHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespaces for semicolon.
            JsonObject semicolonWhitespace = ws.get((ws.size() - 1)).getAsJsonObject();
            if (!this.isHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
                semicolonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update whitespaces for the endpoint type.
            if (node.has("endPointType")) {
                JsonObject endpointType = node.getAsJsonObject("endPointType");
                JsonObject endpointTypeFormatConfig = this.getFormattingConfig(0, 1,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                endpointType.add(FormattingConstants.FORMATTING_CONFIG, endpointTypeFormatConfig);
            }

            // Update whitespaces for the configuration expression.
            if (node.has("configurationExpression")
                    && node.getAsJsonObject("configurationExpression").has(FormattingConstants.WS)) {
                JsonObject configurationExpr = node.getAsJsonObject("configurationExpression");
                JsonObject configurationExprFormatConfig = this.getFormattingConfig(0, 1,
                        this.getWhiteSpaceCount(indentation), false);
                configurationExpr.add(FormattingConstants.FORMATTING_CONFIG, configurationExprFormatConfig);
            }

            // Update whitespaces of annotation attachments.
            if (node.has("annotationAttachments")) {
                JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                for (int i = 0; i < annotationAttachments.size(); i++) {
                    JsonObject annotationAttachment = annotationAttachments.get(i).getAsJsonObject();
                    JsonObject annotationFormattingConfig;
                    if (i == 0) {
                        annotationFormattingConfig = this.getFormattingConfig(formatConfig
                                        .get(FormattingConstants.NEW_LINE_COUNT)
                                        .getAsInt(), 0, this.getWhiteSpaceCount(indentation),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                    } else {
                        annotationFormattingConfig = this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentation),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                    }

                    annotationAttachment.add(FormattingConstants.FORMATTING_CONFIG, annotationFormattingConfig);
                }
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Preserve user added comments and new lines.
            this.preserveHeight(ws, indentation);

            // Update whitespaces for expression.
            if (node.has("expression")) {
                JsonObject expression = node.getAsJsonObject("expression");
                expression.add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // Update whitespace for semicolon.
            JsonObject semicolonWhitespace = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
                semicolonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }
        }
    }

    /**
     * format Field Based Access Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatFieldBasedAccessExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            if (node.has(FormattingConstants.EXPRESSION)) {
                if (node.has("isExpression") && node.get("isExpression").getAsBoolean()) {
                    node.getAsJsonObject(FormattingConstants.EXPRESSION).addProperty("isExpression", true);
                }
                node.getAsJsonObject(FormattingConstants.EXPRESSION).getAsJsonObject(FormattingConstants.POSITION)
                        .addProperty(FormattingConstants.START_COLUMN,
                                node.getAsJsonObject(FormattingConstants.POSITION).get(FormattingConstants.START_COLUMN)
                                        .getAsInt());
            }

            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                            .get(FormattingConstants.START_COLUMN).getAsInt()));
            for (JsonElement jsonElement : ws) {
                if (!this.isHeightAvailable(jsonElement.getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                    jsonElement.getAsJsonObject().addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
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
        // TODO: fix formatting for foreach.
        this.skipFormatting(node, true);
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Update the function node's start column.
            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            // Preserve the new lines and characters available in node's whitespaces.
            this.preserveHeight(ws, indentation);

            // Get the node's index if it is in a list of statements of parent array.
            int functionIndex = this.findIndex(node);

            // Update whitespaces for function/public keyword.
            JsonObject functionKeywordWs = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(functionKeywordWs.get(FormattingConstants.WS).getAsString())) {
                // If function is a lambda, add spaces.
                if (node.getAsJsonObject(FormattingConstants.PARENT)
                        .get("kind").getAsString().equals("Lambda")) {
                    functionKeywordWs.addProperty(FormattingConstants.WS,
                            this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                } else {
                    // If annotation or documentation attachments exists add only one new line.
                    // Else add given number of new lines.
                    String whiteSpace = ((node.has("annotationAttachments") &&
                            node.getAsJsonArray("annotationAttachments").size() > 0) ||
                            (node.has("documentationAttachments") &&
                                    node.getAsJsonArray("documentationAttachments").size() > 0) ||
                            (node.has("deprecatedAttachments") &&
                                    node.getAsJsonArray("deprecatedAttachments").size() > 0))
                            ? (FormattingConstants.NEW_LINE + indentation)
                            : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                            indentation);

                    functionKeywordWs.addProperty(FormattingConstants.WS, whiteSpace);
                }
            } else if (!this.isNewLine(functionKeywordWs
                    .get(FormattingConstants.WS).getAsString().charAt(0) + "") && functionIndex != 0) {
                // TODO: revisit logic.
                functionKeywordWs
                        .addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                functionKeywordWs.get(FormattingConstants.WS).getAsString());
            }

            for (int i = 0; i < ws.size(); i++) {
                JsonObject functionWS = ws.get(i).getAsJsonObject();
                if (!this.isHeightAvailable(functionWS.get(FormattingConstants.WS).getAsString())) {
                    String wsText = functionWS.get(FormattingConstants.TEXT).getAsString();
                    if (wsText.equals("(")) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    if (wsText.equals(",")) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    if (wsText.equals(")")) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update whitespace for returns keyword.
                    if (wsText.equals("returns")) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update whitespaces for the opening brace.
                    if (wsText.equals("{")) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update whitespaces for closing brace of the function.
                    if (wsText.equals("}")) {
                        if (node.has(FormattingConstants.BODY)
                                && node.getAsJsonObject(FormattingConstants.BODY)
                                .getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0
                                && node.getAsJsonArray("endpointNodes").size() <= 0
                                && node.getAsJsonArray("workers").size() <= 0) {

                            if (!this.isHeightAvailable(functionWS.get(FormattingConstants.WS).getAsString())) {
                                functionWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                        indentation + FormattingConstants.NEW_LINE + indentation);
                            }
                        } else if (!this.isHeightAvailable(functionWS.get(FormattingConstants.WS).getAsString())) {
                            functionWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    indentation);
                        }
                    }

                    if (wsText.equals(";")) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Update whitespaces of parameters.
            if (node.has("parameters")) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                boolean firstParam = true;
                for (JsonElement parameter : parameters) {
                    JsonObject formattingConfig;
                    if (firstParam) {
                        formattingConfig = this.getFormattingConfig(0, 0,
                                node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    } else {
                        formattingConfig = this.getFormattingConfig(0, 1,
                                node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    }
                    parameter.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, formattingConfig);
                    firstParam = false;
                }
            }

            // Update whitespaces of defaultable parameters
            if (node.has("defaultableParameters")) {
                JsonArray defaulableParameters = node.getAsJsonArray("defaultableParameters");
                for (JsonElement defaulableParameter : defaulableParameters) {
                    // TODO: fix formatting for defaultable parameters.
                    this.skipFormatting(defaulableParameter.getAsJsonObject(), true);
                }
            }

            // Update whitespaces of endpoint.
            if (node.has("endpointNodes")) {
                JsonArray endpointNodes = node.getAsJsonArray("endpointNodes");
                for (int i = 0; i < endpointNodes.size(); i++) {
                    JsonObject endpointNode = endpointNodes.get(i).getAsJsonObject();
                    JsonObject endpointFormatConfig;
                    if (i == 0) {
                        endpointFormatConfig = this.getFormattingConfig(1, 0,
                                node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    } else {
                        endpointFormatConfig = this.getFormattingConfig(2, 0,
                                node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    }

                    endpointNode.add(FormattingConstants.FORMATTING_CONFIG, endpointFormatConfig);
                }
            }

            // Update whitespaces of workers.
            if (node.has("workers")) {
                JsonArray workers = node.getAsJsonArray("workers");
                for (int i = 0; i < workers.size(); i++) {
                    JsonObject workerNode = workers.get(i).getAsJsonObject();
                    JsonObject workerFormatConfig;
                    if (i == 0) {
                        workerFormatConfig = this.getFormattingConfig(1, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    } else {
                        workerFormatConfig = this.getFormattingConfig(2, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    }

                    workerNode.add(FormattingConstants.FORMATTING_CONFIG, workerFormatConfig);
                }
            }

            // Update whitespaces of annotation attachments.
            if (node.has("annotationAttachments")) {
                JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                for (int i = 0; i < annotationAttachments.size(); i++) {
                    JsonObject annotationAttachment = annotationAttachments.get(i).getAsJsonObject();
                    JsonObject annotationFormattingConfig;
                    if (i == 0) {
                        annotationFormattingConfig = this.getFormattingConfig(formatConfig
                                        .get(FormattingConstants.NEW_LINE_COUNT)
                                        .getAsInt(), 0, formatConfig
                                        .get(FormattingConstants.START_COLUMN).getAsInt(),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                    } else {
                        annotationFormattingConfig = this.getFormattingConfig(1, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                    }

                    annotationAttachment.add(FormattingConstants.FORMATTING_CONFIG, annotationFormattingConfig);
                }
            }

            // Update whitespaces for rest parameters.
            if (node.has("restParameters")) {
                JsonObject restParam = node.getAsJsonObject("restParameters");
                JsonObject restParamFormatConfig;
                if (node.has("parameters") && node.getAsJsonArray("parameters").size() > 0) {
                    restParamFormatConfig = this.getFormattingConfig(0, 1,
                            this.getWhiteSpaceCount(indentation), false);
                } else {
                    restParamFormatConfig = this.getFormattingConfig(0, 0,
                            this.getWhiteSpaceCount(indentation), false);
                }

                restParam.add(FormattingConstants.FORMATTING_CONFIG, restParamFormatConfig);
            }

            // Update whitespaces for return parameters.
            if (node.has("returnTypeNode") &&
                    node.has("hasReturns") &&
                    node.get("hasReturns").getAsBoolean()) {
                // Handle whitespace for return type node.
                JsonObject returnTypeNode = node.getAsJsonObject("returnTypeNode");
                JsonObject returnTypeFormatConfig = this.getFormattingConfig(0, 1,
                        this.getWhiteSpaceCount(indentation), false);
                returnTypeNode.add(FormattingConstants.FORMATTING_CONFIG, returnTypeFormatConfig);
            }

            // Update whitespaces for return type annotation attachments.
            if (node.has("returnTypeAnnotationAttachments")) {
                JsonArray returnTypeAnnotations = node.getAsJsonArray("returnTypeAnnotationAttachments");
                for (int i = 0; i < returnTypeAnnotations.size(); i++) {
                    JsonObject returnTypeAnnotation = returnTypeAnnotations.get(i).getAsJsonObject();
                    JsonObject returnTypeAnnotationFormatConfig;
                    if (i == 0) {
                        returnTypeAnnotationFormatConfig = this.getFormattingConfig(0, 0,
                                this.getWhiteSpaceCount(indentation), false);
                    } else {
                        returnTypeAnnotationFormatConfig = this.getFormattingConfig(0, 1,
                                this.getWhiteSpaceCount(indentation), false);
                    }

                    returnTypeAnnotation.add(FormattingConstants.FORMATTING_CONFIG, returnTypeAnnotationFormatConfig);
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
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                            .get(FormattingConstants.START_COLUMN).getAsInt()));

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                        this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                .get(FormattingConstants.START_COLUMN).getAsInt()));
            }

            if (node.has("paramTypeNode")) {
                JsonArray parameters = node.getAsJsonArray("paramTypeNode");
                for (JsonElement parameter : parameters) {
                    // TODO: fix formatting for function type parameters.
                    this.skipFormatting(parameter.getAsJsonObject(), true);
                }
            }

            if (node.has("returnKeywordExists") && node.get("returnKeywordExists").getAsBoolean()) {
                // TODO: fix formatting for return type node in function type.
                this.skipFormatting(node.getAsJsonObject("returnTypeNode"), true);
            }

            if (node.has("returnTypeAnnotationAttachments")) {
                for (JsonElement annotation : node.getAsJsonArray("returnTypeAnnotationAttachments")) {
                    // TODO: fix formatting for return type annotation in function type.
                    this.skipFormatting(annotation.getAsJsonObject(), true);
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
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                            .get(FormattingConstants.START_COLUMN).getAsInt()));

            if (node.has("isElseIfBlock") && node.get("isElseIfBlock").getAsBoolean()) {
                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                    ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            } else {
                if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                    ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS,
                            FormattingConstants.NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                            .get(FormattingConstants.START_COLUMN).getAsInt()));
                }
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject().get(FormattingConstants.WS)
                    .getAsString())) {
                ws.get(ws.size() - 2).getAsJsonObject().addProperty(FormattingConstants.WS,
                        FormattingConstants.SINGLE_SPACE);
            }

            if (node.has(FormattingConstants.BODY)
                    && node.getAsJsonObject(FormattingConstants.BODY)
                    .getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0) {
                if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get(FormattingConstants.WS)
                        .getAsString())) {
                    ws.get(ws.size() - 1).getAsJsonObject()
                            .addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                            .get(FormattingConstants.START_COLUMN).getAsInt())
                                    + FormattingConstants.NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                            .get(FormattingConstants.START_COLUMN).getAsInt()));
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get(FormattingConstants.WS)
                    .getAsString())) {
                ws.get(ws.size() - 1).getAsJsonObject().addProperty(FormattingConstants.WS,
                        FormattingConstants.NEW_LINE +
                                this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt()));
            }

            if (node.has("elseStatement")
                    && !node.getAsJsonObject("elseStatement").get("kind").getAsString().equals("Block")) {
                node.getAsJsonObject("elseStatement").getAsJsonObject(FormattingConstants.POSITION)
                        .addProperty(FormattingConstants.START_COLUMN,
                                node.getAsJsonObject(FormattingConstants.POSITION).get(FormattingConstants.START_COLUMN)
                                        .getAsInt());
            }

            if (node.has("condition") && node.getAsJsonObject("condition").has(FormattingConstants.WS)) {
                JsonArray conditionWs = node.getAsJsonObject("condition").getAsJsonArray(FormattingConstants.WS);
                this.preserveHeight(conditionWs, this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                        .get(FormattingConstants.START_COLUMN).getAsInt()));

                if (!this.isCommentAvailable(conditionWs.get(0).getAsJsonObject().get(FormattingConstants.WS)
                        .getAsString())) {
                    conditionWs.get(0).getAsJsonObject().addProperty(FormattingConstants.WS,
                            FormattingConstants.SINGLE_SPACE);
                }

                if (!this.isHeightAvailable(conditionWs.get(conditionWs.size() - 1).getAsJsonObject()
                        .get(FormattingConstants.WS).getAsString())) {
                    conditionWs.get(conditionWs.size() - 1).getAsJsonObject().addProperty(FormattingConstants.WS,
                            FormattingConstants.EMPTY_SPACE);
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.get(FormattingConstants.WS).getAsJsonArray();
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);

            // Update whitespaces for import keyword
            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                                indentation);
            }

            // Update whitespace for semicolon
            JsonObject semicolonWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (!this.isHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
                semicolonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }
        }
    }

    /**
     * format Index Based Access Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatIndexBasedAccessExprNode(JsonObject node) {
        // TODO: fix formatting for index based access expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Int Range Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatIntRangeExprNode(JsonObject node) {
        // TODO: fix formatting for int range expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Invocation node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatInvocationNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Preserve user added new lines and comments.
            this.preserveHeight(ws, indentation);

            // If expression is available handle expression whitespaces
            // else handle node's identifier whitespaces.
            if (node.has(FormattingConstants.EXPRESSION) && node.getAsJsonObject(FormattingConstants.EXPRESSION)
                    .has(FormattingConstants.WS)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                expression.add(FormattingConstants.FORMATTING_CONFIG, formatConfig);

                // Update whitespaces for '.' (dot) or '->' (action invocation).
                JsonObject dotActionWhitespace = ws.get(0).getAsJsonObject();
                if (!this.isHeightAvailable(dotActionWhitespace.get(FormattingConstants.WS).getAsString())) {
                    dotActionWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }

                // Update whitespaces for action identifier.
                JsonObject identifierWhitespace = ws.get(1).getAsJsonObject();
                if (!this.isHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                    identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }

                // Update whitespace for open parentheses.
                JsonObject openParenthesesWhitespace = ws.get(2).getAsJsonObject();
                if (!this.isHeightAvailable(openParenthesesWhitespace.get(FormattingConstants.WS).getAsString())) {
                    openParenthesesWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            } else {
                // Update whitespace for identifier or package alias.
                JsonObject identifierOrAliasWhitespace = ws.get(0).getAsJsonObject();
                if (!this.isHeightAvailable(identifierOrAliasWhitespace.get(FormattingConstants.WS).getAsString())) {
                    // If new lines available add new line to the node with indentation.
                    if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                        identifierOrAliasWhitespace.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }

                    // If spaces available add space to the node without indentation.
                    if (formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt() > 0) {
                        identifierOrAliasWhitespace.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }
                }

                // If package alias available ws(1) will be a colon
                // else it will be the invoked functions opening parentheses.
                if (ws.get(1).getAsJsonObject().get(FormattingConstants.TEXT).getAsString().equals(":")) {
                    // Update colon whitespace.
                    JsonObject colonWhitespace = ws.get(1).getAsJsonObject();
                    if (!this.isHeightAvailable(colonWhitespace.get(FormattingConstants.WS).getAsString())) {
                        colonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update whitespace for identifier.
                    JsonObject identifierWhitespace = ws.get(2).getAsJsonObject();
                    if (!this.isHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                        identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update whitespace for opening parentheses.
                    JsonObject openingParenthesesWhitespace = ws.get(3).getAsJsonObject();
                    if (!this.isHeightAvailable(openingParenthesesWhitespace.get(FormattingConstants.WS)
                            .getAsString())) {
                        openingParenthesesWhitespace.addProperty(FormattingConstants.WS,
                                FormattingConstants.EMPTY_SPACE);
                    }

                } else {
                    // Update whitespace for opening parentheses.
                    JsonObject openingParenthesesWhitespace = ws.get(1).getAsJsonObject();
                    if (!this.isHeightAvailable(openingParenthesesWhitespace.get(FormattingConstants.WS)
                            .getAsString())) {
                        openingParenthesesWhitespace.addProperty(FormattingConstants.WS,
                                FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Update whitespace for closing parentheses.
            JsonObject closingParenthesesWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (!this.isHeightAvailable(closingParenthesesWhitespace.get(FormattingConstants.WS).getAsString())) {
                closingParenthesesWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update argument expressions whitespaces.
            if (node.has("argumentExpressions")) {
                JsonArray argumentExpressions = node.getAsJsonArray("argumentExpressions");
                for (JsonElement argumentExpression : argumentExpressions) {
                    // TODO: fix formatting for argument expression in invocation.
                    this.skipFormatting(argumentExpression.getAsJsonObject(), true);
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Update whitespace for literal value.
            this.preserveHeight(ws, indentation);
            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
            }
        }
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
        // TODO: fix formatting for match.
        this.skipFormatting(node, true);
    }

    /**
     * format Match Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchExpressionNode(JsonObject node) {
        // TODO: fix formatting match expression.
        this.skipFormatting(node, true);
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
        // TODO: fix formatting for named argument expressions.
        this.skipFormatting(node, true);
    }

    /**
     * format Object Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatObjectTypeNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            if (node.has(FormattingConstants.FIELDS)) {
                JsonArray fields = node.getAsJsonArray(FormattingConstants.FIELDS);
                for (int i = 0; i < fields.size(); i++) {
                    JsonObject fieldFormatConfig = this.getFormattingConfig(1, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    fields.get(i).getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, fieldFormatConfig);
                }
            }

            if (node.has("initFunction")) {
                JsonObject initFunction = node.getAsJsonObject("initFunction");
                JsonObject functionFormatConfig = this.getFormattingConfig(2, 0,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                initFunction.add(FormattingConstants.FORMATTING_CONFIG, functionFormatConfig);
            }

            if (node.has("functions")) {
                JsonArray functions = node.getAsJsonArray("functions");
                for (int i = 0; i < functions.size(); i++) {
                    JsonObject functionFormatConfig = this.getFormattingConfig(2, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    functions.get(i).getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, functionFormatConfig);
                }
            }
        }
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
        // TODO: fix formatting for order by variable.
        this.skipFormatting(node, true);
    }

    /**
     * format Output Rate Limit node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatOutputRateLimitNode(JsonObject node) {
        // TODO: fix formatting for output rate limit.
        this.skipFormatting(node, true);
    }

    /**
     * format Pattern Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatPatternClauseNode(JsonObject node) {
        // TODO: fix formatting for pattern clause.
        this.skipFormatting(node, true);
    }

    /**
     * format Pattern Streaming Edge Input node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatPatternStreamingEdgeInputNode(JsonObject node) {
        // TODO: fix formatting for pattern streaming edge input.
        this.skipFormatting(node, true);
    }

    /**
     * format Pattern Streaming Input node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatPatternStreamingInputNode(JsonObject node) {
        // TODO: fix formatting for pattern streaming input.
        this.skipFormatting(node, true);
    }

    /**
     * format Post Increment node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatPostIncrementNode(JsonObject node) {
        // TODO: fix formatting for post increment.
        this.skipFormatting(node, true);
    }

    /**
     * format Record Literal Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRecordLiteralExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            String parentKind = node.getAsJsonObject(FormattingConstants.PARENT).get("kind").getAsString();
            if (parentKind.equals("Endpoint") || parentKind.equals("AnnotationAttachment") ||
                    parentKind.equals("Service") || parentKind.equals("Variable")) {

                node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                        indentation);

                this.preserveHeight(ws, indentation);

                // Update whitespace for opening brace.
                JsonObject openingBraceWhitespace = ws.get(0).getAsJsonObject();
                if (!this.isHeightAvailable(openingBraceWhitespace.get(FormattingConstants.WS).getAsString())) {
                    openingBraceWhitespace.addProperty(FormattingConstants.WS,
                            this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                }

                // Update whitespace for closing brace.
                JsonObject closingBraceWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
                if (node.has("keyValuePairs")
                        && node.getAsJsonArray("keyValuePairs").size() <= 0 &&
                        !this.isHeightAvailable(closingBraceWhitespace.get(FormattingConstants.WS).getAsString())) {

                    closingBraceWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                            indentation + FormattingConstants.NEW_LINE +
                            indentation);
                } else if (!this.isHeightAvailable(closingBraceWhitespace.get(FormattingConstants.WS).getAsString())) {
                    closingBraceWhitespace.addProperty(FormattingConstants.WS,
                            FormattingConstants.NEW_LINE + indentation);
                }

                // Update the key value pair of a record.
                if (node.has("keyValuePairs")) {
                    JsonArray keyValuePairs = node.getAsJsonArray("keyValuePairs");
                    for (JsonElement keyValue : keyValuePairs) {
                        JsonObject keyValueFormatting = this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentation), true);
                        keyValue.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, keyValueFormatting);
                    }
                }

                // Update whitespaces for the key value pair separator , or ;.
                for (int j = 0; j < ws.size(); j++) {
                    if (ws.get(j).getAsJsonObject().get(FormattingConstants.TEXT).getAsString().equals(",") ||
                            ws.get(j).getAsJsonObject().get(FormattingConstants.TEXT).getAsString().equals(";")) {
                        ws.get(j).getAsJsonObject().addProperty(FormattingConstants.WS,
                                FormattingConstants.EMPTY_SPACE);
                    }
                }
            }
        }
    }

    /**
     * format record literal key value.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRecordLiteralKeyValueNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Update whitespace for key value of record literal.
            if (node.has("key")) {
                JsonObject keyNode = node.getAsJsonObject("key");
                JsonObject keyNodeFormatConfig = this.getFormattingConfig(
                        formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                        formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                keyNode.add(FormattingConstants.FORMATTING_CONFIG, keyNodeFormatConfig);
            }

            // Update whitespace for colon of the record literal key value pair.
            this.preserveHeight(ws, indentation);
            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update whitespace for value of record literal.
            if (node.has("value")) {
                JsonObject valueNode = node.getAsJsonObject("value");
                JsonObject valueNodeFormatConfig = this.getFormattingConfig(0, 1,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                valueNode.add(FormattingConstants.FORMATTING_CONFIG, valueNodeFormatConfig);
            }
        }
    }

    /**
     * format Record Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRecordTypeNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            // Update the fields whitespace.
            JsonArray fields = node.getAsJsonArray(FormattingConstants.FIELDS);
            for (int i = 0; i < fields.size(); i++) {
                JsonObject child = fields.get(i).getAsJsonObject();
                JsonObject childFormatConfig = this.getFormattingConfig(1, 0,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                child.add(FormattingConstants.FORMATTING_CONFIG, childFormatConfig);
            }

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                // Update the restField whitespace.
                if (node.has("restFieldType") &&
                        node.get("restFieldType").getAsJsonObject().has(FormattingConstants.WS)) {

                    JsonObject restFieldType = node.getAsJsonObject("restFieldType");
                    JsonObject restFieldTypeFormatConfig = this.getFormattingConfig(1, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    restFieldType.add(FormattingConstants.FORMATTING_CONFIG, restFieldTypeFormatConfig);

                    // Update the ... symbol whitespace.
                    JsonObject restWS = ws.get(0).getAsJsonObject();
                    if (!this.isHeightAvailable(restWS.get(FormattingConstants.WS).getAsString())) {
                        restWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }

                // Update the whitespaces for sealed type.
                if (node.has("sealed") &&
                        node.get("sealed").getAsBoolean()) {
                    String indentation = this.getWhiteSpaces(
                            formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                            (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN)
                                    .getAsInt()) + FormattingConstants.SPACE_TAB)
                                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN)
                                    .getAsInt()));

                    this.preserveHeight(ws, indentation);

                    // Update the ! symbol whitespace.
                    JsonObject sealedWS = ws.get(0).getAsJsonObject();
                    if (!this.isHeightAvailable(sealedWS.get(FormattingConstants.WS).getAsString())) {
                        sealedWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
                    }

                    // Update the ... symbol whitespace.
                    JsonObject restWS = ws.get(1).getAsJsonObject();
                    if (!this.isHeightAvailable(restWS.get(FormattingConstants.WS).getAsString())) {
                        restWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
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
        // TODO: fix formatting for reply.
        this.skipFormatting(node, true);
    }

    /**
     * format Resource node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatResourceNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, indentation);

            // Update whitespaces of resource name.
            JsonObject resourceNameWhitespace = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(resourceNameWhitespace.get(FormattingConstants.WS).getAsString())) {
                String whiteSpace = ((node.has("annotationAttachments") &&
                        node.getAsJsonArray("annotationAttachments").size() > 0) ||
                        (node.has("documentationAttachments") &&
                                node.getAsJsonArray("documentationAttachments").size() > 0) ||
                        (node.has("deprecatedAttachments") &&
                                node.getAsJsonArray("deprecatedAttachments").size() > 0))
                        ? (FormattingConstants.NEW_LINE + indentation)
                        : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                        indentation);

                resourceNameWhitespace.addProperty(FormattingConstants.WS, whiteSpace);
            }

            // Update whitespace of opening parentheses.
            JsonObject openingParenthesesWhitespace = ws.get(1).getAsJsonObject();
            if (!this.isHeightAvailable(openingParenthesesWhitespace.get(FormattingConstants.WS).getAsString())) {
                openingParenthesesWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // TODO update whitespaces of parameter separators.

            // Update whitespace of closing parentheses.
            JsonObject closingParenthesesWhitespace = ws.get(ws.size() - 3).getAsJsonObject();
            if (!this.isHeightAvailable(closingParenthesesWhitespace.get(FormattingConstants.WS).getAsString())) {
                closingParenthesesWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update opening bracket whitespaces.
            JsonObject openingBracketWhitespace = ws.get(ws.size() - 2).getAsJsonObject();
            if (!this.isHeightAvailable(openingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                openingBracketWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update closing bracket whitespace.
            JsonObject closingBracketWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (node.has(FormattingConstants.BODY)
                    && node.getAsJsonObject(FormattingConstants.BODY)
                    .getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0
                    && node.getAsJsonArray("workers").size() <= 0
                    && node.getAsJsonArray("endpointNodes").size() <= 0) {
                if (!this.isHeightAvailable(closingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                    closingBracketWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                            indentation + FormattingConstants.NEW_LINE + indentation);
                }
            } else if (!this.isHeightAvailable(closingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                closingBracketWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                        this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                .get(FormattingConstants.START_COLUMN).getAsInt()));
            }

            // update the parameter whitespace in resource.
            if (node.has("parameters")) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                for (int i = 0; i < parameters.size(); i++) {
                    JsonObject parameter = parameters.get(i).getAsJsonObject();
                    JsonObject parameterFormatConfig;
                    if (i == 0) {
                        parameterFormatConfig = this.getFormattingConfig(0, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    } else {
                        parameterFormatConfig = this.getFormattingConfig(0, 1,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    }
                    parameter.add(FormattingConstants.FORMATTING_CONFIG, parameterFormatConfig);
                }
            }

            // Update endpoint whitespaces in resource.
            if (node.has("endpointNodes")) {
                JsonArray endpointNodes = node.getAsJsonArray("endpointNodes");
                for (int i = 0; i < endpointNodes.size(); i++) {
                    JsonObject endpointNode = endpointNodes.get(i).getAsJsonObject();
                    JsonObject endpointFormatConfig;
                    if (i == 0) {
                        endpointFormatConfig = this.getFormattingConfig(1, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    } else {
                        endpointFormatConfig = this.getFormattingConfig(2, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    }

                    endpointNode.add(FormattingConstants.FORMATTING_CONFIG, endpointFormatConfig);
                }
            }

            // Update annotation whitespaces in resource.
            if (node.has("annotationAttachments")) {
                JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                for (int i = 0; i < annotationAttachments.size(); i++) {
                    JsonObject annotationAttachment = annotationAttachments.get(i).getAsJsonObject();
                    JsonObject annotationFormattingConfig;
                    if (i == 0) {
                        annotationFormattingConfig = this.getFormattingConfig(
                                formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(), 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                    } else {
                        annotationFormattingConfig = this.getFormattingConfig(1, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                    }

                    annotationAttachment.add(FormattingConstants.FORMATTING_CONFIG, annotationFormattingConfig);
                }
            }

            // Update workers whitespace in resource.
            if (node.has("workers")) {
                JsonArray workers = node.getAsJsonArray("workers");
                for (int i = 0; i < workers.size(); i++) {
                    JsonObject workerNode = workers.get(i).getAsJsonObject();
                    JsonObject workerFormatConfig;
                    if (i == 0) {
                        workerFormatConfig = this.getFormattingConfig(1, 0,
                                node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    } else {
                        workerFormatConfig = this.getFormattingConfig(2, 0,
                                node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    }

                    workerNode.add(FormattingConstants.FORMATTING_CONFIG, workerFormatConfig);
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
        // TODO: fix formatting for rest arguments expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Retry node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRetryNode(JsonObject node) {
        // TODO: fix formatting for retry.
        this.skipFormatting(node, true);
    }

    /**
     * format Return node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatReturnNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);

            // Update return keyword.
            JsonObject returnKeywordWhitespace = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(returnKeywordWhitespace.get(FormattingConstants.WS).getAsString())) {
                returnKeywordWhitespace.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            }

            // Update expression whitespaces.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }

            // Update semicolon whitespace.
            JsonObject semicolonWhitespace = ws.get(1).getAsJsonObject();
            if (!this.isHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
                semicolonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }
        }
    }

    /**
     * format Select Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatSelectClauseNode(JsonObject node) {
        // TODO: fix formatting for select clause.
        this.skipFormatting(node, true);
    }

    /**
     * format Select Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatSelectExpressionNode(JsonObject node) {
        // TODO: fix formatting for select expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Service node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatServiceNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);
            int serviceIndex = this.findIndex(node);

            JsonObject serviceKeywordWhitespace = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(serviceKeywordWhitespace.get(FormattingConstants.WS).getAsString())) {
                String whiteSpace = ((node.has("annotationAttachments") &&
                        node.getAsJsonArray("annotationAttachments").size() > 0) ||
                        (node.has("documentationAttachments") &&
                                node.getAsJsonArray("documentationAttachments").size() > 0) ||
                        (node.has("deprecatedAttachments") &&
                                node.getAsJsonArray("deprecatedAttachments").size() > 0))
                        ? (FormattingConstants.NEW_LINE + indentation)
                        : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                        indentation);

                serviceKeywordWhitespace.addProperty(FormattingConstants.WS, whiteSpace);
            } else if (!this.isNewLine(serviceKeywordWhitespace.get(FormattingConstants.WS)
                    .getAsString().charAt(0) + "") && serviceIndex != 0) {
                // TODO revisit logic.
                serviceKeywordWhitespace.addProperty(FormattingConstants.WS,
                        FormattingConstants.NEW_LINE +
                                serviceKeywordWhitespace.get(FormattingConstants.WS).getAsString());
            }

            // Update service type whitespaces if available.
            if (node.has("serviceTypeStruct")) {
                // Update whitespace of the less than (<) symbol.
                JsonObject lessThanSymbolWhitespace = ws.get(1).getAsJsonObject();
                if (!this.isHeightAvailable(lessThanSymbolWhitespace.get(FormattingConstants.WS).getAsString())) {
                    lessThanSymbolWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }

                // Update whitespace of the greater than (>) symbol.
                JsonObject greaterThanSymbolWhitespace = ws.get(1).getAsJsonObject();
                if (!this.isHeightAvailable(greaterThanSymbolWhitespace.get(FormattingConstants.WS).getAsString())) {
                    greaterThanSymbolWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            }

            if (node.has("boundEndpoints") &&
                    node.getAsJsonArray("boundEndpoints").size() > 0) {
                // Update whitespaces for service name.
                JsonObject serviceNameWhitespace = ws.get(ws.size() - 4).getAsJsonObject();
                if (!this.isHeightAvailable(serviceNameWhitespace.get(FormattingConstants.WS).getAsString())) {
                    serviceNameWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }

                // Update whitespaces for bind keyword.
                JsonObject bindKeywordWhitespace = ws.get(ws.size() - 3).getAsJsonObject();
                if (!this.isHeightAvailable(bindKeywordWhitespace.get(FormattingConstants.WS).getAsString())) {
                    bindKeywordWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            } else {
                // Update whitespaces for service name.
                JsonObject serviceNameWhitespace = ws.get(ws.size() - 3).getAsJsonObject();
                if (!this.isHeightAvailable(serviceNameWhitespace.get(FormattingConstants.WS).getAsString())) {
                    serviceNameWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            }

            // Update whitespaces for opening bracket.
            JsonObject openingBracketWhitespace = ws.get(ws.size() - 2).getAsJsonObject();
            if (!this.isHeightAvailable(openingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                openingBracketWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespaces for closing bracket.
            JsonObject closingBracketWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (node.getAsJsonArray("resources").size() <= 0
                    && node.getAsJsonArray("variables").size() <= 0
                    && node.getAsJsonArray("endpointNodes").size() <= 0
                    && node.getAsJsonArray("namespaceDeclarations").size() <= 0) {
                if (!this.isHeightAvailable(closingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                    closingBracketWhitespace.addProperty(FormattingConstants.WS,
                            FormattingConstants.NEW_LINE + indentation + FormattingConstants.NEW_LINE +
                                    indentation);
                }
            } else if (!this.isHeightAvailable(closingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                closingBracketWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                        indentation);
            }

            // Update whitespace for endpoints.
            if (node.has("endpointNodes")) {
                JsonArray endpointNodes = node.getAsJsonArray("endpointNodes");
                for (int i = 0; i < endpointNodes.size(); i++) {
                    JsonObject endpointNode = endpointNodes.get(i).getAsJsonObject();
                    JsonObject endpointFormatConfig;
                    if (i == 0) {
                        endpointFormatConfig = this.getFormattingConfig(1, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    } else {
                        endpointFormatConfig = this.getFormattingConfig(2, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    }
                    endpointNode.add(FormattingConstants.FORMATTING_CONFIG, endpointFormatConfig);
                }
            }

            // Update whitespaces for resources.
            if (node.has("resources")) {
                JsonArray resources = node.getAsJsonArray("resources");
                for (int i = 0; i < resources.size(); i++) {
                    JsonObject resource = resources.get(i).getAsJsonObject();
                    JsonObject resourceFormatConfig;
                    if (i == 0) {
                        resourceFormatConfig = this.getFormattingConfig(1, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    } else {
                        resourceFormatConfig = this.getFormattingConfig(2, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    }
                    resource.add(FormattingConstants.FORMATTING_CONFIG, resourceFormatConfig);
                }
            }

            // Update whitespaces for variables.
            if (node.has("variables")) {
                JsonArray variables = node.getAsJsonArray("variables");
                for (int i = 0; i < variables.size(); i++) {
                    JsonObject variable = variables.get(i).getAsJsonObject();
                    JsonObject variableFormatConfig = this.getFormattingConfig(1, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    variable.add(FormattingConstants.FORMATTING_CONFIG, variableFormatConfig);
                }
            }

            if (node.has("annotationAttachments")) {
                JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                for (int i = 0; i < annotationAttachments.size(); i++) {
                    JsonObject annotationAttachment = annotationAttachments.get(i).getAsJsonObject();
                    JsonObject annotationFormattingConfig;
                    if (i == 0) {
                        annotationFormattingConfig = this.getFormattingConfig(
                                formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(), 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                    } else {
                        annotationFormattingConfig = this.getFormattingConfig(1, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                    }

                    annotationAttachment.add(FormattingConstants.FORMATTING_CONFIG, annotationFormattingConfig);
                }
            }

            if (node.has("anonymousEndpointBind")) {
                // TODO: fix formatting for anonymous endpoint bind in service.
                this.skipFormatting(node.getAsJsonObject("anonymousEndpointBind"), true);
            }

            if (node.has("boundEndpoints")) {
                JsonArray boundEndpoints = node.getAsJsonArray("boundEndpoints");
                for (JsonElement boundEndpoint : boundEndpoints) {
                    // TODO: fix formatting for bound endpoint in service.
                    this.skipFormatting(boundEndpoint.getAsJsonObject(), true);
                }
            }

            if (node.has("workers")) {
                JsonArray workers = node.getAsJsonArray("workers");
                for (int i = 0; i < workers.size(); i++) {
                    JsonObject workerNode = workers.get(i).getAsJsonObject();
                    JsonObject workerFormatConfig;
                    if (i == 0) {
                        workerFormatConfig = this.getFormattingConfig(1, 0,
                                node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    } else {
                        workerFormatConfig = this.getFormattingConfig(2, 0,
                                node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt(), true);
                    }

                    workerNode.add(FormattingConstants.FORMATTING_CONFIG, workerFormatConfig);
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Preserve comments and new line added by user.
            this.preserveHeight(ws, indentation);

            // Update reference whitespace.
            JsonObject referenceWS = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(referenceWS.get(FormattingConstants.WS).getAsString())) {
                if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                    referenceWS.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                    + indentation);
                } else if (formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt() > 0) {
                    referenceWS.addProperty(FormattingConstants.WS,
                            this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
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
        // TODO: fix formatting for statement expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Stream Action node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStreamActionNode(JsonObject node) {
        // TODO: fix formatting for stream action.
        this.skipFormatting(node, true);
    }

    /**
     * format Streaming Query node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStreamingQueryNode(JsonObject node) {
        // TODO: fix formatting for streaming query.
        this.skipFormatting(node, true);
    }

    /**
     * format String Template Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStringTemplateLiteralNode(JsonObject node) {
        // TODO: fix formatting for string template literal.
        this.skipFormatting(node, true);
    }

    /**
     * format Ternary Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTernaryExprNode(JsonObject node) {
        // TODO: fix formatting for ternary expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Throw node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatThrowNode(JsonObject node) {
        // TODO: fix formatting for throw.
        this.skipFormatting(node, true);
    }

    /**
     * format Transaction node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTransactionNode(JsonObject node) {
        // TODO: fix formatting for transaction.
        this.skipFormatting(node, true);
    }

    /**
     * format Try node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTryNode(JsonObject node) {
        if (node.has(FormattingConstants.WS)) {
            if (node.has("catchBlocks")) {
                for (JsonElement catchBlock : node.getAsJsonArray("catchBlocks")) {
                    catchBlock.getAsJsonObject().getAsJsonObject(FormattingConstants.POSITION)
                            .addProperty(FormattingConstants.START_COLUMN,
                                    node.getAsJsonObject(FormattingConstants.POSITION)
                                            .get(FormattingConstants.START_COLUMN).getAsInt());
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                            FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);

            if (node.has("declaredWithVar") && node.get("declaredWithVar").getAsBoolean()) {
                JsonObject varWS = ws.get(0).getAsJsonObject();
                JsonObject openingParenthesesWS = ws.get(1).getAsJsonObject();

                if (!this.isHeightAvailable(varWS.get(FormattingConstants.WS).getAsString())) {
                    varWS.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                    indentation);
                }

                if (!this.isHeightAvailable(openingParenthesesWS.get(FormattingConstants.WS).getAsString())) {
                    openingParenthesesWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            } else {
                JsonObject openingParenthesesWS = ws.get(0).getAsJsonObject();
                if (!this.isHeightAvailable(openingParenthesesWS.get(FormattingConstants.WS).getAsString())) {
                    openingParenthesesWS.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                    indentation);
                }
            }

            // Update variable references' whitespaces.
            if (node.has("variableRefs")) {
                JsonArray varRefs = node.getAsJsonArray("variableRefs");
                for (int i = 0; i < varRefs.size(); i++) {
                    JsonObject varRef = varRefs.get(i).getAsJsonObject();
                    JsonObject varRefFormatConfig;

                    if (i == 0) {
                        varRefFormatConfig = this.getFormattingConfig(0, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    } else {
                        varRefFormatConfig = this.getFormattingConfig(0, 1,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    }

                    varRef.add(FormattingConstants.FORMATTING_CONFIG, varRefFormatConfig);
                }
            }

            // Update closing parentheses whitespace.
            JsonObject closingParenWS = ws.get(ws.size() - 3).getAsJsonObject();
            if (!this.isHeightAvailable(closingParenWS.get(FormattingConstants.WS).getAsString())) {
                closingParenWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update assignment sign for tuple destruct.
            JsonObject equalWS = ws.get(ws.size() - 2).getAsJsonObject();
            if (!this.isHeightAvailable(equalWS.get(FormattingConstants.WS).getAsString())) {
                equalWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespace for expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }

            // Update semicolon whitespace
            JsonObject semicolonWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (!this.isHeightAvailable(semicolonWS.get(FormattingConstants.WS).getAsString())) {
                semicolonWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }
        }
    }

    /**
     * format Tuple Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTupleTypeNodeNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                            FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);

            // Update the whitespace for the opening parentheses.
            JsonObject openingParentheses = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(openingParentheses.get(FormattingConstants.WS).getAsString())) {
                openingParentheses.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            }

            // Update the whitespaces for member types in tuple.
            if (node.has("memberTypeNodes")) {
                JsonArray memberTypeNodes = node.getAsJsonArray("memberTypeNodes");
                for (int i = 0; i < memberTypeNodes.size(); i++) {
                    JsonObject memberType = memberTypeNodes.get(i).getAsJsonObject();
                    JsonObject memberTypeFormatConfig;

                    if (i == 0) {
                        memberTypeFormatConfig = this.getFormattingConfig(0, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    } else {
                        memberTypeFormatConfig = this.getFormattingConfig(0, 1,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    }

                    memberType.add(FormattingConstants.FORMATTING_CONFIG, memberTypeFormatConfig);
                }
            }

            // Update the whitespace for closing parentheses.
            JsonObject closingParentheses = ws.get(ws.size() - 1).getAsJsonObject();
            if (!this.isHeightAvailable(closingParentheses.get(FormattingConstants.WS).getAsString())) {
                closingParentheses.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }
        }
    }

    /**
     * format Type Cast Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeCastExprNode(JsonObject node) {
        // TODO: fix formatting for type cast expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Type Conversion Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeConversionExprNode(JsonObject node) {
        // TODO: fix formatting for type conversion expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Type Definition node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeDefinitionNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                            FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);
            int typeDefinitionIndex = this.findIndex(node);

            // Update the type or public keywords whitespace.
            JsonObject publicOrTypeWS = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(publicOrTypeWS.get(FormattingConstants.WS).getAsString())) {
                publicOrTypeWS.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            } else if (!this.isNewLine(publicOrTypeWS.get(FormattingConstants.WS).getAsString().charAt(0) + "")
                    && typeDefinitionIndex != 0) {
                publicOrTypeWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                        publicOrTypeWS.get(FormattingConstants.WS).getAsString());
            }

            if (node.has(FormattingConstants.PUBLIC) && node.get(FormattingConstants.PUBLIC).getAsBoolean()) {
                // Update type keyword whitespace.
                JsonObject typeKeywordWS = ws.get(1).getAsJsonObject();
                if (!this.isHeightAvailable(typeKeywordWS.get(FormattingConstants.WS).getAsString())) {
                    typeKeywordWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            }

            // Update identifier whitespace.
            JsonObject identifierWS = ws.get(ws.size() - 5).getAsJsonObject();
            if (!this.isHeightAvailable(identifierWS.get(FormattingConstants.WS).getAsString())) {
                identifierWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update record or object keyword whitespace.
            JsonObject recordOrObjectWS = ws.get(ws.size() - 4).getAsJsonObject();
            if (!this.isHeightAvailable(recordOrObjectWS.get(FormattingConstants.WS).getAsString())) {
                recordOrObjectWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update opening bracket whitespace.
            JsonObject openingBracketWS = ws.get(ws.size() - 3).getAsJsonObject();
            if (!this.isHeightAvailable(openingBracketWS.get(FormattingConstants.WS).getAsString())) {
                openingBracketWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Handle the whitespace for type node.
            if (node.has("typeNode")) {
                JsonObject typeNodeFormatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(indentation), true);
                node.getAsJsonObject("typeNode").add(FormattingConstants.FORMATTING_CONFIG, typeNodeFormatConfig);
            }

            // Update the closing bracket whitespaces.
            JsonObject closingBracketWS = ws.get(ws.size() - 2).getAsJsonObject();
            if (node.has("typeNode")
                    && node.getAsJsonObject("typeNode").getAsJsonArray(FormattingConstants.FIELDS).size() <= 0) {
                if (!this.isHeightAvailable(closingBracketWS.get(FormattingConstants.WS).getAsString())) {
                    closingBracketWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                            indentation + FormattingConstants.NEW_LINE + indentation);
                }
            } else if (!this.isHeightAvailable(closingBracketWS.get(FormattingConstants.WS).getAsString())) {
                closingBracketWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
            }

            // Update the semicolon whitespace.
            JsonObject semicolonWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (!this.isHeightAvailable(semicolonWS.get(FormattingConstants.WS).getAsString())) {
                semicolonWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }
        }
    }

    /**
     * format Typedesc Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypedescExpressionNode(JsonObject node) {
        // TODO: fix formatting for type desc expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Type Init Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeInitExprNode(JsonObject node) {
        // TODO: fix formatting for type init expression
        this.skipFormatting(node, true);
    }

    /**
     * format Unary Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatUnaryExprNode(JsonObject node) {
        // TODO: fix formatting for unary expression.
        this.skipFormatting(node, true);
    }

    /**
     * format Union Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatUnionTypeNodeNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            boolean isGrouped = node.has(FormattingConstants.GROUPED) &&
                    node.get(FormattingConstants.GROUPED).getAsBoolean();

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                            FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);

            if (isGrouped) {
                // Update opening parentheses whitespace.
                JsonObject openingParenWS = ws.get(0).getAsJsonObject();
                if (!this.isHeightAvailable(openingParenWS.get(FormattingConstants.WS).getAsString())) {
                    openingParenWS.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                    indentation);
                }

                // Update pipe symbol whitespace.
                JsonObject pipeWS = ws.get(ws.size() - 2).getAsJsonObject();
                if (!this.isHeightAvailable(pipeWS.get(FormattingConstants.WS).getAsString())) {
                    pipeWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }

                // Update closing parentheses whitespace.
                JsonObject closingParenWS = ws.get(ws.size() - 1).getAsJsonObject();
                if (!this.isHeightAvailable(closingParenWS.get(FormattingConstants.WS).getAsString())) {
                    closingParenWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            } else {
                // Update pipe symbol whitespace.
                JsonObject pipeWS = ws.get(0).getAsJsonObject();
                if (!this.isHeightAvailable(pipeWS.get(FormattingConstants.WS).getAsString())) {
                    pipeWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            }

            // Update member types whitespaces.
            if (node.has("memberTypeNodes")) {
                JsonArray memberTypeNodes = node.getAsJsonArray("memberTypeNodes");
                for (int i = 0; i < memberTypeNodes.size(); i++) {
                    JsonObject memberType = memberTypeNodes.get(i).getAsJsonObject();
                    JsonObject memberTypeFormatConfig;
                    if (i == 0) {
                        if (isGrouped) {
                            memberTypeFormatConfig = this.getFormattingConfig(0, 0,
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                        } else {
                            memberTypeFormatConfig = this.getFormattingConfig(
                                    formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                    formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                    formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                        }
                    } else {
                        memberTypeFormatConfig = this.getFormattingConfig(0, 1,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    }

                    memberType.add(FormattingConstants.FORMATTING_CONFIG, memberTypeFormatConfig);
                }
            }
        }
    }

    /**
     * format User Defined Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatUserDefinedTypeNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt())
                        + (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                        ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt())
                        + FormattingConstants.SPACE_TAB)
                        : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

                node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                        this.getWhiteSpaceCount(indentation));
                this.preserveHeight(ws, indentation);

                // Handle package alias if available.
                if (ws.size() > 1) {
                    // Update whitespace for package alias.
                    JsonObject packageAliasWhitespace = ws.get(0).getAsJsonObject();
                    if (!this.isHeightAvailable(packageAliasWhitespace.get(FormattingConstants.WS).getAsString())) {
                        packageAliasWhitespace.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }

                    // Update whitespace for package access colon.
                    JsonObject packageColonWhitespace = ws.get(ws.size() - 2).getAsJsonObject();
                    if (!this.isHeightAvailable(packageColonWhitespace.get(FormattingConstants.WS).getAsString())) {
                        packageColonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update whitespace for identifier
                    JsonObject identifierWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
                    if (!this.isHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                        identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                } else {
                    // Update whitespace for identifier
                    JsonObject identifierWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
                    if (!this.isHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                        identifierWhitespace.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }
                }
            } else if (node.has(FormattingConstants.IS_ANON_TYPE) &&
                    node.get(FormattingConstants.IS_ANON_TYPE).getAsBoolean()) {
                JsonObject anonType = node.getAsJsonObject(FormattingConstants.ANON_TYPE);
                JsonObject anonTypeFormatConfig = this.getFormattingConfig(
                        formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                        formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                        formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                anonType.add(FormattingConstants.FORMATTING_CONFIG, anonTypeFormatConfig);
            }
        }
    }

    /**
     * format Value Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatValueTypeNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt())
                    + FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, indentation);

            // Update whitespaces for type.
            JsonObject typeWhitespace = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(typeWhitespace.get(FormattingConstants.WS).getAsString())) {
                typeWhitespace.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                                indentation);
            }
        }
    }

    /**
     * format Variable Def node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatVariableDefNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            // TODO: revisit.
            this.preserveHeight(node.getAsJsonArray(FormattingConstants.WS),
                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                            .get(FormattingConstants.START_COLUMN).getAsInt()));

            node.getAsJsonObject("variable").add(FormattingConstants.FORMATTING_CONFIG,
                    node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG));

            // Update whitespaces for semicolon.
            if (!this.isHeightAvailable(node.getAsJsonArray(FormattingConstants.WS).get(0)
                    .getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                node.getAsJsonArray(FormattingConstants.WS).get(0).getAsJsonObject()
                        .addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }
        }
    }

    /**
     * format Variable node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatVariableNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt())
                    + (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt())
                    + FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(node.getAsJsonArray(FormattingConstants.WS), indentation);

            // Format type node
            if (node.has("typeNode")) {
                JsonObject typeNode = node.getAsJsonObject("typeNode");
                JsonObject typeFormatConfig;

                // Update the record or public keyword whitespaces.
                JsonObject firstKeywordWS = ws.get(0).getAsJsonObject();

                if (node.has(FormattingConstants.IS_ANON_TYPE)
                        && node.get(FormattingConstants.IS_ANON_TYPE).getAsBoolean()) {

                    if (node.has(FormattingConstants.FINAL) && node.get(FormattingConstants.FINAL).getAsBoolean()) {
                        if (!this.isHeightAvailable(firstKeywordWS.get(FormattingConstants.WS).getAsString())) {
                            // Update the record keyword.
                            firstKeywordWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    } else if ((node.has(FormattingConstants.PUBLIC) &&
                            node.get(FormattingConstants.PUBLIC).getAsBoolean() &&
                            firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                    .equals(FormattingConstants.PUBLIC))) {
                        if (!this.isHeightAvailable(firstKeywordWS.getAsJsonObject()
                                .get(FormattingConstants.WS).getAsString())) {
                            // Update the public keyword.
                            firstKeywordWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                            .getAsInt()) + indentation);
                        }

                        // Update the record keyword.
                        JsonObject recordWS = ws.get(1).getAsJsonObject();
                        if (!this.isHeightAvailable(recordWS.get(FormattingConstants.WS).getAsString())) {
                            recordWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }

                    } else {
                        // Update record keyword whitespace.
                        if (!this.isHeightAvailable(firstKeywordWS.getAsJsonObject().get(FormattingConstants.WS)
                                .getAsString())) {
                            firstKeywordWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                            .getAsInt()) + indentation);
                        }
                    }

                    JsonObject openingParenWS;
                    JsonObject closingParentWS;
                    JsonObject identifierWS;

                    // If semicolon or comma is available update whitespaces with semicolon or comma whitespace.
                    // Else update rest of the whitespaces.
                    if (ws.get(ws.size() - 1).getAsJsonObject().get(FormattingConstants.TEXT)
                            .getAsString().equals(";") || ws.get(ws.size() - 1).getAsJsonObject()
                            .get(FormattingConstants.TEXT).getAsString().equals(",")) {
                        openingParenWS = ws.get(ws.size() - 4).getAsJsonObject();
                        closingParentWS = ws.get(ws.size() - 3).getAsJsonObject();
                        identifierWS = ws.get(ws.size() - 2).getAsJsonObject();

                        // Update semicolon or comma whitespace.
                        JsonObject semiColonWS = ws.get(ws.size() - 1).getAsJsonObject();
                        if (!this.isHeightAvailable(semiColonWS.get(FormattingConstants.WS).getAsString())) {
                            semiColonWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    } else {
                        openingParenWS = ws.get(ws.size() - 3).getAsJsonObject();
                        closingParentWS = ws.get(ws.size() - 2).getAsJsonObject();
                        identifierWS = ws.get(ws.size() - 1).getAsJsonObject();
                    }

                    // Update opening parentheses whitespace.
                    if (!this.isHeightAvailable(openingParenWS.get(FormattingConstants.WS).getAsString())) {
                        openingParenWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update type node whitespace.
                    typeFormatConfig = this.getFormattingConfig(1,
                            0,
                            this.getWhiteSpaceCount(indentation),
                            true);
                    typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeFormatConfig);

                    // Update closing parentheses whitespace.
                    if (typeNode.has(FormattingConstants.ANON_TYPE) &&
                            typeNode.getAsJsonObject(FormattingConstants.ANON_TYPE).has(FormattingConstants.FIELDS) &&
                            typeNode.getAsJsonObject(FormattingConstants.ANON_TYPE)
                                    .getAsJsonArray(FormattingConstants.FIELDS).size() <= 0) {
                        if (!this.isHeightAvailable(closingParentWS.get(FormattingConstants.WS).getAsString())) {
                            closingParentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                                    + indentation + FormattingConstants.NEW_LINE + indentation);
                        }
                    } else {
                        if (!this.isHeightAvailable(closingParentWS.get(FormattingConstants.WS).getAsString())) {
                            closingParentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                                    + indentation);
                        }
                    }

                    // Update identifier whitespace.
                    if (!this.isHeightAvailable(identifierWS.get(FormattingConstants.WS).getAsString())) {
                        identifierWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                } else {
                    JsonObject identifierWhitespace;

                    if ((node.has(FormattingConstants.FINAL) &&
                            node.get(FormattingConstants.FINAL).getAsBoolean()) ||
                            (node.has(FormattingConstants.PUBLIC) &&
                                    node.get(FormattingConstants.PUBLIC).getAsBoolean() &&
                                    firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                            .equals(FormattingConstants.PUBLIC))) {

                        // Update public keyword whitespaces.
                        if ((node.has(FormattingConstants.PUBLIC) &&
                                node.get(FormattingConstants.PUBLIC).getAsBoolean() &&
                                firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                        .equals(FormattingConstants.PUBLIC))) {
                            if (!this.isHeightAvailable(firstKeywordWS.get(FormattingConstants.WS).getAsString())) {
                                if (node.has(FormattingConstants.FINAL) &&
                                        node.get(FormattingConstants.FINAL).getAsBoolean()) {
                                    firstKeywordWS.addProperty(FormattingConstants.WS,
                                            FormattingConstants.SINGLE_SPACE);
                                } else {
                                    firstKeywordWS.addProperty(FormattingConstants.WS,
                                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                                    .getAsInt()) + indentation);
                                }
                            }

                            // Set identifier.
                            identifierWhitespace = ws.get(1).getAsJsonObject();
                        } else {
                            // Set identifier.
                            identifierWhitespace = firstKeywordWS;
                        }

                        typeFormatConfig = this.getFormattingConfig(0,
                                1,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                false);
                        typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeFormatConfig);

                    } else {
                        typeFormatConfig = this.getFormattingConfig(
                                formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean());
                        typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeFormatConfig);

                        // Set identifier.
                        identifierWhitespace = firstKeywordWS;
                    }

                    // Update identifier whitespace.
                    if (identifierWhitespace.get(FormattingConstants.TEXT).getAsString().equals("...")) {
                        if (!this.isHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                            identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    } else {
                        if (!this.isHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                            identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    }

                    // If semicolon or comma available, update whitespaces including semicolon or comma.
                    // Else update rest of the whitespaces.
                    if (ws.size() > 1) {
                        JsonObject lastWS = ws.get(ws.size() - 1).getAsJsonObject();
                        JsonObject secondToLastWS = ws.get(ws.size() - 2).getAsJsonObject();

                        if (lastWS.get(FormattingConstants.TEXT).getAsString().equals(";") ||
                                lastWS.get(FormattingConstants.TEXT).getAsString().equals(",")) {
                            if (!this.isHeightAvailable(lastWS.get(FormattingConstants.WS).getAsString())) {
                                lastWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            }

                            // Update equal symbol whitespace.
                            if (secondToLastWS.get(FormattingConstants.TEXT).getAsString().equals("=") &&
                                    !this.isHeightAvailable(secondToLastWS.get(FormattingConstants.WS).getAsString())) {
                                secondToLastWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }
                        } else if (lastWS.get(FormattingConstants.TEXT).getAsString().equals("=")) {
                            if (!this.isHeightAvailable(lastWS.get(FormattingConstants.WS).getAsString())) {
                                lastWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }
                        }
                    }

                    // Update the equal symbol whitespace.
                    if (ws.size() > 1 && ws.get(ws.size() - 1).getAsJsonObject().get(FormattingConstants.TEXT)
                            .getAsString().equals("=")) {
                        JsonObject equalWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
                        if (!this.isHeightAvailable(equalWhitespace.get(FormattingConstants.WS).getAsString())) {
                            equalWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    }
                }
            }

            if (node.has("initialExpression")) {
                JsonObject initialExprFormattingConfig = this.getFormattingConfig(0, 1,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                node.getAsJsonObject("initialExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        initialExprFormattingConfig);
            }

            if (node.has("annotationAttachments")) {
                JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                for (JsonElement annotationAttachment : annotationAttachments) {
                    JsonObject annotationAttachmentFormattingConfig = this.getFormattingConfig(1, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    annotationAttachment.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG,
                            annotationAttachmentFormattingConfig);
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
        if (node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            this.preserveHeight(ws,
                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                            .get(FormattingConstants.START_COLUMN).getAsInt()));

            if (!this.isHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS,
                        FormattingConstants.NEW_LINE + this.getWhiteSpaces(
                                node.getAsJsonObject(FormattingConstants.POSITION)
                                        .get(FormattingConstants.START_COLUMN).getAsInt()));
            }

            if (!this.isHeightAvailable(ws.get(ws.size() - 2).getAsJsonObject()
                    .get(FormattingConstants.WS).getAsString())) {
                ws.get(ws.size() - 2).getAsJsonObject().addProperty(FormattingConstants.WS,
                        FormattingConstants.SINGLE_SPACE);
            }

            if (node.has(FormattingConstants.BODY)
                    && node.getAsJsonObject(FormattingConstants.BODY).getAsJsonArray(
                    FormattingConstants.STATEMENTS).size() <= 0) {
                if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject()
                        .get(FormattingConstants.WS).getAsString())) {
                    ws.get(ws.size() - 1).getAsJsonObject()
                            .addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                            .get(FormattingConstants.START_COLUMN).getAsInt())
                                    + FormattingConstants.NEW_LINE +
                                    this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                            .get(FormattingConstants.START_COLUMN).getAsInt()));
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject().get(FormattingConstants.WS)
                    .getAsString())) {
                ws.get(ws.size() - 1).getAsJsonObject().addProperty(FormattingConstants.WS,
                        FormattingConstants.NEW_LINE + this.getWhiteSpaces(node.getAsJsonObject(
                                FormattingConstants.POSITION).get(FormattingConstants.START_COLUMN).getAsInt()));
            }

            if (node.has("condition") && node.getAsJsonObject("condition").has(FormattingConstants.WS)) {
                JsonArray conditionWs = node.getAsJsonObject("condition").getAsJsonArray(FormattingConstants.WS);
                this.preserveHeight(conditionWs, this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                        .get(FormattingConstants.START_COLUMN).getAsInt()));

                if (!this.isCommentAvailable(conditionWs.get(0).getAsJsonObject().get(FormattingConstants.WS)
                        .getAsString())) {
                    conditionWs.get(0).getAsJsonObject().addProperty(FormattingConstants.WS,
                            FormattingConstants.SINGLE_SPACE);
                }

                if (!this.isHeightAvailable(conditionWs.get(conditionWs.size() - 1).getAsJsonObject()
                        .get(FormattingConstants.WS).getAsString())) {
                    conditionWs.get(conditionWs.size() - 1).getAsJsonObject().addProperty(FormattingConstants.WS,
                            FormattingConstants.EMPTY_SPACE);
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt())
                    + FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Update the position start column of the node as to the indentation.
            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            // Preserve height and comments that user added.
            this.preserveHeight(ws, indentation);

            // Update whitespace for worker keyword.
            JsonObject workerKeywordWhitespace = ws.get(0).getAsJsonObject();
            if (!this.isHeightAvailable(workerKeywordWhitespace.get(FormattingConstants.WS).getAsString())) {
                workerKeywordWhitespace.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                + indentation);
            }

            // Update whitespace for worker identifier.
            JsonObject workerIdentifier = ws.get(ws.size() - 3).getAsJsonObject();
            if (!this.isHeightAvailable(workerIdentifier.get(FormattingConstants.WS).getAsString())) {
                workerIdentifier.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespace for worker opening brace.
            JsonObject openingBrace = ws.get(ws.size() - 2).getAsJsonObject();
            if (!this.isHeightAvailable(openingBrace.get(FormattingConstants.WS).getAsString())) {
                openingBrace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespace for worker closing brace.
            JsonObject closingBrace = ws.get(ws.size() - 1).getAsJsonObject();
            if (node.has(FormattingConstants.BODY)
                    && node.getAsJsonObject(FormattingConstants.BODY).getAsJsonArray(
                    FormattingConstants.STATEMENTS).size() <= 0
                    && node.getAsJsonArray("workers").size() <= 0
                    && node.getAsJsonArray("endpointNodes").size() <= 0) {

                if (!this.isHeightAvailable(closingBrace.get(FormattingConstants.WS).getAsString())) {
                    closingBrace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                            + indentation + FormattingConstants.NEW_LINE + indentation);
                }
            } else if (!this.isHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject()
                    .get(FormattingConstants.WS).getAsString())) {
                closingBrace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
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
