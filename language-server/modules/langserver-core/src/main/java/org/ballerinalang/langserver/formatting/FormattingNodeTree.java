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
public class FormattingNodeTree {
    /**
     * format abort node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAbortNode(JsonObject node) {
        modifyBranchingStatement(node);
    }

    /**
     * format annotation node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotationNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            boolean isPublic = node.has(FormattingConstants.PUBLIC)
                    && node.get(FormattingConstants.PUBLIC).getAsBoolean();

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);

            int attachPointCounter = 0;
            for (int i = 0; i < ws.size(); i++) {
                JsonObject annotationWS = ws.get(i).getAsJsonObject();
                String text = annotationWS.get(FormattingConstants.TEXT).getAsString();
                if (this.noHeightAvailable(annotationWS.get(FormattingConstants.WS).getAsString())) {
                    if (i == 0) {
                        // If annotation or documentation attachments exists add only one new line.
                        // Else add given number of new lines.
                        String whiteSpace = ((node.has("annotationAttachments") &&
                                node.getAsJsonArray("annotationAttachments").size() > 0) ||
                                node.has("markdownDocumentationAttachment") ||
                                (node.has("deprecatedAttachments") &&
                                        node.getAsJsonArray("deprecatedAttachments").size() > 0))
                                ? (FormattingConstants.NEW_LINE + indentation)
                                : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
                        annotationWS.addProperty(FormattingConstants.WS, whiteSpace);
                    } else if (isPublic && text.equals("annotation")) {
                        annotationWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals("<")) {
                        annotationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        ++attachPointCounter;
                    } else if (text.equals(",")) {
                        annotationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(">")) {
                        annotationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        attachPointCounter = 0;
                    } else {
                        if (attachPointCounter == 1) {
                            annotationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            ++attachPointCounter;
                        } else if (attachPointCounter > 1) {
                            annotationWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            ++attachPointCounter;
                        } else {
                            if (text.equals(";")) {
                                annotationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            } else {
                                annotationWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }
                        }
                    }
                }
            }

            // Update whitespaces for type node.
            if (node.has("typeNode")) {
                JsonObject typeNode = node.getAsJsonObject("typeNode");
                JsonObject typeNodeFormattingConfig = this.getFormattingConfig(0, 1,
                        0, false, this.getWhiteSpaceCount(indentation));
                typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeNodeFormattingConfig);
            }
        }
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

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            // Preserve any comments or new lines added by user.
            this.preserveHeight(node.getAsJsonArray(FormattingConstants.WS), indentWithParentIndentation);

            // Update whitespace for annotation symbol, @.
            JsonObject annotationSymbolWhitespace = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(annotationSymbolWhitespace.get(FormattingConstants.WS).getAsString())) {
                annotationSymbolWhitespace.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            } else if (this.noNewLine(annotationSymbolWhitespace.get(FormattingConstants.WS)
                    .getAsString().charAt(0) + "")) {
                annotationSymbolWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                        indentation);
            }

            // Update whitespace for annotation identifier.
            JsonObject identifierWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update whitespace for expression.
            if (node.has("expression") && node.getAsJsonObject("expression").has(FormattingConstants.WS)) {
                JsonObject expression = node.getAsJsonObject("expression");
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1,
                        this.getWhiteSpaceCount(indentation), false, this.getWhiteSpaceCount(indentation));
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }
        }
    }

    /**
     * format Array Literal Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatArrayLiteralExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                            FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            String indentWithParentIndentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                    .getAsInt()) + (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update opening bracket whitespace.
            JsonObject openingBracketWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(openingBracketWS.get(FormattingConstants.WS).getAsString())) {
                openingBracketWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                        .get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) + indentation);
            }

            // Update whitespace for the separator.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                String text = wsItem.get(FormattingConstants.TEXT).getAsString();
                if (text.equals(",") && this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    wsItem.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            }

            // Update closing bracket whitespace.
            JsonObject closingBracketWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (this.noHeightAvailable(closingBracketWS.get(FormattingConstants.WS).getAsString())) {
                closingBracketWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update expressions whitespaces.
            modifyExpressions(node, indentWithParentIndentation);
        }
    }

    /**
     * format Array Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatArrayTypeNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            // Update whitespace for element type.
            if (node.has("elementType")) {
                if (node.has(FormattingConstants.GROUPED) && node.get(FormattingConstants.GROUPED).getAsBoolean()) {
                    JsonObject elementTypeFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                            this.getWhiteSpaceCount(indentWithParentIndentation));
                    node.getAsJsonObject("elementType").add(FormattingConstants.FORMATTING_CONFIG,
                            elementTypeFormatConfig);
                } else {
                    node.getAsJsonObject("elementType").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
                }
            }

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                this.preserveHeight(ws, indentWithParentIndentation);

                for (int i = 0; i < ws.size(); i++) {
                    JsonObject wsItem = ws.get(i).getAsJsonObject();
                    if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                        String text = wsItem.get(FormattingConstants.TEXT).getAsString();
                        if (text.equals("(") && i == 0) {
                            // Update grouped opening parentheses whitespace.
                            if (node.has(FormattingConstants.GROUPED) &&
                                    node.get(FormattingConstants.GROUPED).getAsBoolean()) {
                                wsItem.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                        .get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                        indentWithParentIndentation);
                            }
                        } else {
                            // Update rest of the token whitespaces.
                            wsItem.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    }
                }
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

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            // Preserve comments and new lines added by the user.
            this.preserveHeight(ws, indentWithParentIndentation);

            if (node.has("declaredWithVar") && node.get("declaredWithVar").getAsBoolean()) {
                // Update whitespaces for var.
                JsonObject varWhitespace = ws.get(0).getAsJsonObject();
                if (this.noHeightAvailable(varWhitespace.get(FormattingConstants.WS).getAsString())) {
                    // If declared with var add new line
                    varWhitespace.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                    indentation);
                }

                // Update whitespace for variable when declared with var.
                if (node.has("variable")) {
                    JsonObject variable = node.getAsJsonObject("variable");
                    JsonObject variableFormatConfig = this.getFormattingConfig(0, 1,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false,
                            this.getWhiteSpaceCount(indentation));
                    variable.add(FormattingConstants.FORMATTING_CONFIG, variableFormatConfig);
                }

                // Update whitespace for =
                JsonObject equalWhitespace = ws.get(1).getAsJsonObject();
                if (this.noHeightAvailable(equalWhitespace.get(FormattingConstants.WS).getAsString())) {
                    equalWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }

                // Update whitespace for ;
                JsonObject semicolonWhitespace = ws.get(2).getAsJsonObject();
                if (this.noHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
                    semicolonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            } else {
                // Update whitespace for variable when not declared with var.
                if (node.has("variable")) {
                    JsonObject variable = node.getAsJsonObject("variable");
                    JsonObject variableFormatConfig =
                            this.getFormattingConfig(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                    0, formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                    formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                                    this.getWhiteSpaceCount(indentation));
                    variable.add(FormattingConstants.FORMATTING_CONFIG, variableFormatConfig);
                }

                // Update whitespace for =
                JsonObject equalWhitespace = ws.get(0).getAsJsonObject();
                if (this.noHeightAvailable(equalWhitespace.get(FormattingConstants.WS).getAsString())) {
                    equalWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }

                // Update whitespace for ;
                JsonObject semicolonWhitespace = ws.get(1).getAsJsonObject();
                if (this.noHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
                    semicolonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            }

            // Update whitespaces for the expression.
            if (node.has("expression") && node.getAsJsonObject("expression").has(FormattingConstants.WS)) {
                JsonObject expression = node.getAsJsonObject("expression");
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1,
                        0, false,
                        this.getWhiteSpaceCount(indentation));
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }
        }
    }

    /**
     * format arrow expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatArrowExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            this.preserveHeight(ws, indentWithParentIndentation);

            for (JsonElement wsItem : ws) {
                JsonObject arrowExprWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(arrowExprWS.get(FormattingConstants.WS).getAsString())) {
                    String text = arrowExprWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals("(")) {
                        arrowExprWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }

                    if (text.equals(",")) {
                        arrowExprWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    if (text.equals(")")) {
                        arrowExprWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    if (text.equals("=>")) {
                        arrowExprWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Update whitespaces of parameters.
            if (node.has("parameters")) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                boolean hasParentheses = node.has("hasParantheses")
                        && node.get("hasParantheses").getAsBoolean();
                for (int i = 0; i < parameters.size(); i++) {
                    JsonObject param = parameters.get(i).getAsJsonObject();
                    JsonObject paramFormatConfig;
                    // If parentheses available first param should fronted with empty space
                    // Else first param should fronted with space count parent provided.
                    if (i == 0) {
                        if (hasParentheses) {
                            paramFormatConfig = this.getFormattingConfig(0, 0,
                                    0, false, this.getWhiteSpaceCount(indentWithParentIndentation));
                        } else {
                            paramFormatConfig = this.getFormattingConfig(0,
                                    formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                    0, false, this.getWhiteSpaceCount(indentWithParentIndentation));
                        }
                    } else {
                        paramFormatConfig = this.getFormattingConfig(0, 1,
                                0, false, this.getWhiteSpaceCount(indentWithParentIndentation));
                    }

                    param.add(FormattingConstants.FORMATTING_CONFIG, paramFormatConfig);
                }
            }

            // Update whitespace of expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentWithParentIndentation));
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update the operator symbol whitespace.
            JsonObject operatorSymbolWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(operatorSymbolWS.get(FormattingConstants.WS).getAsString())) {
                operatorSymbolWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Handle left expression whitespaces.
            if (node.has("leftExpression")) {
                node.getAsJsonObject("leftExpression").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // Handle right expression whitespaces.
            if (node.has("rightExpression")) {
                JsonObject rightExpression = node.getAsJsonObject("rightExpression");
                JsonObject rightExprFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentWithParentIndentation));
                rightExpression.add(FormattingConstants.FORMATTING_CONFIG, rightExprFormatConfig);
            }
        }
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

        // TODO: revisit code of how block node is handled.
        JsonObject formatConfig = node.has(FormattingConstants.FORMATTING_CONFIG)
                ? node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG) : null;

        // Get the start column of the parent.
        position.addProperty(FormattingConstants.START_COLUMN, node.get("parent").getAsJsonObject()
                .get(FormattingConstants.POSITION)
                .getAsJsonObject().get(FormattingConstants.START_COLUMN).getAsInt());

        // Add block position to be the parent's position.
        node.add(FormattingConstants.POSITION, position);

        // Update the statements whitespaces.
        for (int i = 0; i < node.getAsJsonArray(FormattingConstants.STATEMENTS).size(); i++) {
            JsonElement child = node.getAsJsonArray(FormattingConstants.STATEMENTS).get(i);
            JsonObject childFormatConfig = formatConfig;
            if (formatConfig == null) {
                childFormatConfig = this.getFormattingConfig(1, 0,
                        node.get(FormattingConstants.POSITION).getAsJsonObject().get(FormattingConstants.START_COLUMN)
                                .getAsInt(), true, node.get(FormattingConstants.POSITION).getAsJsonObject()
                                .get(FormattingConstants.START_COLUMN).getAsInt());
            }
            child.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, childFormatConfig);
        }

        // If this is a else block continue to following.
        if (node.has(FormattingConstants.WS) && node.getAsJsonArray(FormattingConstants.WS).get(0).getAsJsonObject()
                .get(FormattingConstants.TEXT).getAsString().equals("else")) {

            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            this.preserveHeight(ws, this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                    .get(FormattingConstants.START_COLUMN).getAsInt()));

            // Update the else keyword whitespace.
            JsonObject elseKeywordWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(elseKeywordWS.get(FormattingConstants.WS).getAsString())) {
                elseKeywordWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update the opening brace whitespace.
            JsonObject openingBraceWS = ws.get(ws.size() - 2).getAsJsonObject();
            if (this.noHeightAvailable(openingBraceWS.get(FormattingConstants.WS).getAsString())) {
                openingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update the closing brace whitespace.
            JsonObject closingBraceWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (node.getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0) {
                if (this.noHeightAvailable(closingBraceWS.get(FormattingConstants.WS).getAsString())) {
                    closingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                            this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                    .get(FormattingConstants.START_COLUMN).getAsInt())
                            + FormattingConstants.NEW_LINE +
                            this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                    .get(FormattingConstants.START_COLUMN).getAsInt()));
                }
            } else if (this.noHeightAvailable(closingBraceWS.get(FormattingConstants.WS).getAsString())) {
                closingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Get the indentation for the node.
            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                            FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            // Update opening parentheses whitespace.
            JsonObject openingParenthesesWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(openingParenthesesWS.get(FormattingConstants.WS).getAsString())) {
                openingParenthesesWS.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            }

            // Update expressions' whitespaces.
            modifyExpressions(node, indentation);

            // Update closing parentheses whitespace.
            JsonObject closingParenthesesWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (this.noHeightAvailable(closingParenthesesWS.get(FormattingConstants.WS).getAsString())) {
                closingParenthesesWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }
        }
    }

    /**
     * format Break node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBreakNode(JsonObject node) {
        modifyBranchingStatement(node);
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
            if (this.noHeightAvailable(refTypeWhitespace.get(FormattingConstants.WS).getAsString())) {
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Get the indentation for the node.
            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentWithParentIndentation));

            this.preserveHeight(ws, indentWithParentIndentation);

            for (JsonElement wsItem : ws) {
                JsonObject catchWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(catchWS.get(FormattingConstants.WS).getAsString())) {
                    String text = catchWS.get(FormattingConstants.TEXT).getAsString();

                    // Update whitespace for catch keyword.
                    if (text.equals("catch")) {
                        catchWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }

                    // Update the whitespace for opening parentheses.
                    if (text.equals("(")) {
                        catchWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update the whitespace for closing parentheses.
                    if (text.equals(")")) {
                        catchWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update the whitespace for opening brace.
                    if (text.equals("{")) {
                        catchWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update the whitespace for closing brace.
                    if (text.equals("}")) {
                        if (node.has(FormattingConstants.BODY) &&
                                node.getAsJsonObject(FormattingConstants.BODY).has(FormattingConstants.STATEMENTS) &&
                                node.getAsJsonObject(FormattingConstants.BODY)
                                        .getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0) {
                            catchWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    indentWithParentIndentation + FormattingConstants.NEW_LINE +
                                    indentWithParentIndentation);
                        } else {
                            catchWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    indentWithParentIndentation);
                        }
                    }
                }
            }
        }
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
     * format Compensate node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatCompensateNode(JsonObject node) {
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
        int movedFirstIndex = 0;
        for (int i = 0; i < topLevelNodes.size(); i++) {
            JsonObject child = topLevelNodes.get(i).getAsJsonObject();
            JsonObject formatConfig;

            // Skip any anon types available in the top level before picking the first top level block.
            movedFirstIndex = child.has(FormattingConstants.WS) ? movedFirstIndex : ++movedFirstIndex;

            if (i == movedFirstIndex) {
                formatConfig = this.getFormattingConfig(0, 0,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE));
                movedFirstIndex = 0;
            } else if (child.has("kind") &&
                    child.get("kind").getAsString().equals("Import")) {
                formatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE));
            } else {
                formatConfig = this.getFormattingConfig(2, 0,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE));
            }
            child.add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
        }

        // Update EOF whitespace.
        if (node.has(FormattingConstants.WS) && topLevelNodes.size() > 0) {
            JsonArray ws = node.get(FormattingConstants.WS).getAsJsonArray();

            // preserve comment available before EOF.
            this.preserveHeight(ws, null);

            // Handle adding a new line at the EOF.
            JsonObject eofWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(eofWS.get(FormattingConstants.WS).getAsString())) {
                eofWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE);
            } else if (this.noNewLine(eofWS.get(FormattingConstants.WS).getAsString()
                    .charAt(eofWS.get(FormattingConstants.WS).getAsString().length() - 1) + "")) {
                eofWS.addProperty(FormattingConstants.WS, (eofWS.get(FormattingConstants.WS).getAsString() +
                        FormattingConstants.NEW_LINE));
            }
        }

        // Handle import sorting according to the alphabetical order.
        int i, j;
        boolean swapped;
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                            FormattingConstants.SPACE_TAB
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);

            // Update whitespace for type node.
            if (node.has("type")) {
                node.getAsJsonObject("type").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // Update whitespace for constraint.
            if (node.has("constraint")) {
                JsonObject constraintFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                        this.getWhiteSpaceCount(indentation));
                node.getAsJsonObject("constraint").add(FormattingConstants.FORMATTING_CONFIG, constraintFormatConfig);
            }

            // Update whitespace for open constraint symbol.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    String text = wsItem.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals("<") || text.equals(">")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

        }
    }

    /**
     * Format continue node.
     *
     * @param node {JsonObject} node as a json object
     */
    public void formatNextNode(JsonObject node) {
        modifyBranchingStatement(node);
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
        // TODO: fix formatting for documentation node.
        this.skipFormatting(node, true);
    }

    /**
     * format Documentation Attribute node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatDocumentationAttributeNode(JsonObject node) {
        // TODO: fix formatting for Documentation attribute node.
        this.skipFormatting(node, true);
    }

    /**
     * format documentation description node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatDocumentationDescriptionNode(JsonObject node) {
        // TODO: fix formatting for documentation node.
        this.skipFormatting(node, true);
    }

    /**
     * format documentation parameter node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatDocumentationParameterNode(JsonObject node) {
        // TODO: fix formatting for documentation node.
        this.skipFormatting(node, true);
    }

    /**
     * format markdown documentation node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMarkdownDocumentationNode(JsonObject node) {
        // TODO: fix formatting for documentation node.
        this.skipFormatting(node, true);
    }

    /**
     * format Done node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatDoneNode(JsonObject node) {
        modifyBranchingStatement(node);
    }

    /**
     * format Elvis Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatElvisExprNode(JsonObject node) {
        // TODO: fix formatting for elvis expr node.
        this.skipFormatting(node, true);
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

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            // Set start column as to the updated indentation.
            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentWithParentIndentation));

            // Preserve user added comments and new lines.
            this.preserveHeight(node.getAsJsonArray(FormattingConstants.WS), indentWithParentIndentation);

            int endpointIndex = this.findIndex(node);

            // Update whitespace for endpoint/public keyword.
            JsonObject endpointKeyWord = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(endpointKeyWord.get(FormattingConstants.WS).getAsString())) {
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
            } else if (this.noNewLine(endpointKeyWord.get(FormattingConstants.WS).getAsString().charAt(0) + "")
                    && endpointIndex != 0) {
                // TODO: revisit the logic.
                endpointKeyWord.addProperty(FormattingConstants.WS,
                        FormattingConstants.NEW_LINE + endpointKeyWord.get(FormattingConstants.WS).getAsString());
            }


            // Update whitespaces for identifier.
            JsonObject identifierWhitespace = ws.get((ws.size() - 2)).getAsJsonObject();
            if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespaces for semicolon.
            JsonObject semicolonWhitespace = ws.get((ws.size() - 1)).getAsJsonObject();
            if (this.noHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
                semicolonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update whitespaces for the endpoint type.
            if (node.has("endPointType")) {
                JsonObject endpointType = node.getAsJsonObject("endPointType");
                JsonObject endpointTypeFormatConfig = this.getFormattingConfig(0, 1,
                        0, false,
                        this.getWhiteSpaceCount(indentation));
                endpointType.add(FormattingConstants.FORMATTING_CONFIG, endpointTypeFormatConfig);
            }

            // Update whitespaces for the configuration expression.
            if (node.has("configurationExpression")
                    && node.getAsJsonObject("configurationExpression").has(FormattingConstants.WS)) {
                JsonObject configurationExpr = node.getAsJsonObject("configurationExpression");
                JsonObject configurationExprFormatConfig = this.getFormattingConfig(0, 1,
                        this.getWhiteSpaceCount(indentation), false, this.getWhiteSpaceCount(indentation));
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
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                                this.getWhiteSpaceCount(indentation));
                    } else {
                        annotationFormattingConfig = this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentation),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                                this.getWhiteSpaceCount(indentation));
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
        // TODO: fix formatting for endpoint type node.
        this.skipFormatting(node, true);
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
            if (this.noHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update the expression whitespaces.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);

                if (node.has("isExpression") && node.get("isExpression").getAsBoolean()) {
                    expression.addProperty("isExpression", true);
                }

                JsonObject expressionFormatConfig = this.getFormattingConfig(formatConfig
                                .get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                        formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                        formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                        this.getWhiteSpaceCount(indentation));

                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }

            // Update rest of the whitespaces.
            for (JsonElement jsonElement : ws) {
                if (this.noHeightAvailable(jsonElement.getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                    jsonElement.getAsJsonObject().addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            }
        }
    }

    /**
     * format Finite type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatFiniteTypeNodeNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.get(FormattingConstants.WS).getAsJsonArray();
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("|")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            if (node.has("valueSet")) {
                JsonArray valueSet = node.getAsJsonArray("valueSet");
                for (JsonElement valueItem : valueSet) {
                    JsonObject value = valueItem.getAsJsonObject();
                    JsonObject valueFormatConfig = this.getFormattingConfig(0, 1, 0,
                            false, this.getWhiteSpaceCount(indentation));
                    value.add(FormattingConstants.FORMATTING_CONFIG, valueFormatConfig);
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentWithParentIndentation));

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update whitespace for the foreach signature.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    String text = wsItem.get(FormattingConstants.TEXT).getAsString();

                    // Update whitespace for the foreach keyword.
                    if (text.equals("foreach")) {
                        wsItem.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentWithParentIndentation);
                    }

                    // Update whitespace for the opening parentheses.
                    if (text.equals("(")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update whitespace for the param separator.
                    if (text.equals(",")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update the whitespace for in keyword.
                    if (text.equals("in")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update whitespace for the closing parentheses.
                    if (text.equals(")")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update the whitespace for opening bracket.
                    if (text.equals("{")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update the whitespace for closing bracket.
                    if (text.equals("}")) {
                        if (node.has(FormattingConstants.BODY) &&
                                node.getAsJsonObject(FormattingConstants.BODY).has(FormattingConstants.STATEMENTS) &&
                                node.getAsJsonObject(FormattingConstants.BODY)
                                        .getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0) {
                            wsItem.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    indentWithParentIndentation + FormattingConstants.NEW_LINE +
                                    indentWithParentIndentation);
                        } else {
                            wsItem.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    indentWithParentIndentation);
                        }
                    }
                }
            }

            // Handle whitespace for variables
            if (node.has("variables")) {
                JsonArray variables = node.getAsJsonArray("variables");
                for (int i = 0; i < variables.size(); i++) {
                    JsonObject variable = variables.get(i).getAsJsonObject();
                    JsonObject variableFormatConfig;
                    if (i == 0 && node.has("withParantheses") &&
                            node.get("withParantheses").getAsBoolean()) {
                        variableFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentWithParentIndentation));
                    } else {
                        variableFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentWithParentIndentation));
                    }

                    variable.add(FormattingConstants.FORMATTING_CONFIG, variableFormatConfig);
                }
            }

            // Handle whitespace for collection.
            if (node.has("collection")) {
                JsonObject collection = node.getAsJsonObject("collection");
                JsonObject collectionFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentWithParentIndentation));
                collection.add(FormattingConstants.FORMATTING_CONFIG, collectionFormatConfig);
            }
        }
    }

    /**
     * format Forever node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatForeverNode(JsonObject node) {
        // TODO: fix formatting for forever node.
        this.skipFormatting(node, true);
    }

    /**
     * format Fork Join node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatForkJoinNode(JsonObject node) {
        // TODO: fix formatting for fork join node.
        this.skipFormatting(node, true);
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
            boolean isLambda = node.has("lambda") && node.get("lambda").getAsBoolean();

            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            // Update the function node's start column.
            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentWithParentIndentation));

            // Preserve the new lines and characters available in node's whitespaces.
            this.preserveHeight(ws, indentWithParentIndentation);

            // Get the node's index if it is in a list of statements of parent array.
            int functionIndex = this.findIndex(node);

            // Update whitespaces for function/public keyword.
            JsonObject functionKeywordWs = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(functionKeywordWs.get(FormattingConstants.WS).getAsString())) {
                // If function is a lambda, add spaces.
                if (isLambda) {
                    functionKeywordWs.addProperty(FormattingConstants.WS,
                            this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                } else {
                    // If annotation or documentation attachments exists add only one new line.
                    // Else add given number of new lines.
                    String whiteSpace = ((node.has("annotationAttachments") &&
                            node.getAsJsonArray("annotationAttachments").size() > 0) ||
                            node.has("markdownDocumentationAttachment") ||
                            (node.has("deprecatedAttachments") &&
                                    node.getAsJsonArray("deprecatedAttachments").size() > 0))
                            ? (FormattingConstants.NEW_LINE + indentation)
                            : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                            indentation);

                    functionKeywordWs.addProperty(FormattingConstants.WS, whiteSpace);
                }
            } else if (this.noNewLine(functionKeywordWs
                    .get(FormattingConstants.WS).getAsString().charAt(0) + "") && functionIndex != 0) {
                // TODO: revisit logic.
                functionKeywordWs
                        .addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                functionKeywordWs.get(FormattingConstants.WS).getAsString());
            }

            for (int i = 0; i < ws.size(); i++) {
                JsonObject functionWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(functionWS.get(FormattingConstants.WS).getAsString())) {
                    String wsText = functionWS.get(FormattingConstants.TEXT).getAsString();
                    if (wsText.equals("(")) {
                        if (!isLambda) {
                            functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    }

                    if (wsText.equals(",")) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    if (wsText.equals("=>")) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
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

                            if (this.noHeightAvailable(functionWS.get(FormattingConstants.WS).getAsString())) {
                                functionWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                        indentWithParentIndentation + FormattingConstants.NEW_LINE +
                                        indentWithParentIndentation);
                            }
                        } else if (this.noHeightAvailable(functionWS.get(FormattingConstants.WS).getAsString())) {
                            functionWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    indentWithParentIndentation);
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
                iterateAndFormatMembers(indentation, parameters);
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
            modifyEndpoints(node, indentation);

            // Update whitespaces of workers.
            modifyWorkers(node, indentation);

            // Update whitespaces of annotation attachments.
            modifyAnnotationAttachments(node, formatConfig, indentation);

            // Update whitespaces for rest parameters.
            if (node.has("restParameters")) {
                JsonObject restParam = node.getAsJsonObject("restParameters");
                JsonObject restParamFormatConfig;
                if (node.has("parameters") && node.getAsJsonArray("parameters").size() > 0) {
                    restParamFormatConfig = this.getFormattingConfig(0, 1,
                            0, false, this.getWhiteSpaceCount(indentation));
                } else {
                    restParamFormatConfig = this.getFormattingConfig(0, 0,
                            0, false, this.getWhiteSpaceCount(indentation));
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
                        0, false, this.getWhiteSpaceCount(indentation));
                returnTypeNode.add(FormattingConstants.FORMATTING_CONFIG, returnTypeFormatConfig);
            }

            // Update whitespaces for return type annotation attachments.
            modifyReturnTypeAnnotations(node, indentation);
        }
    }

    /**
     * format Function Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatFunctionClauseNode(JsonObject node) {
        // TODO: fix formatting for function clause node.
        this.skipFormatting(node, true);
    }

    /**
     * format Function Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatFunctionTypeNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            boolean isGrouped = node.has("grouped") && node.get("grouped").getAsBoolean();
            boolean returnKeywordExists = node.has("returnKeywordExists") &&
                    node.get("returnKeywordExists").getAsBoolean();

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt())
                    + (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            String indentWithParentIndentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                    .getAsInt()) + this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN)
                    .getAsInt());

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update whitespace for function keyword or parentheses.
            JsonObject firstKeywordWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(firstKeywordWS.get(FormattingConstants.WS).getAsString())) {
                firstKeywordWS.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            }

            JsonObject openingParenthesesWS;
            JsonObject closingParenthesesWS;

            if (isGrouped) {
                // Update function keyword.
                JsonObject functionKeywordWS = ws.get(1).getAsJsonObject();
                if (this.noHeightAvailable(functionKeywordWS.get(FormattingConstants.WS).getAsString())) {
                    functionKeywordWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }

                // Set opening parentheses whitespaces.
                openingParenthesesWS = ws.get(2).getAsJsonObject();

                // Set closing parentheses whitespaces.
                if (returnKeywordExists) {
                    closingParenthesesWS = ws.get(ws.size() - 3).getAsJsonObject();
                } else {
                    closingParenthesesWS = ws.get(ws.size() - 2).getAsJsonObject();
                }

                // Update group closing parentheses whitespace.
                JsonObject closeGroupParenWS = ws.get(ws.size() - 1).getAsJsonObject();
                if (this.noHeightAvailable(closingParenthesesWS.get(FormattingConstants.WS).getAsString())) {
                    closeGroupParenWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            } else {
                // Set opening parentheses whitespaces.
                openingParenthesesWS = ws.get(1).getAsJsonObject();

                // Set closing parentheses whitespace.
                if (returnKeywordExists) {
                    closingParenthesesWS = ws.get(ws.size() - 2).getAsJsonObject();
                } else {
                    closingParenthesesWS = ws.get(ws.size() - 1).getAsJsonObject();
                }
            }

            // Update opening parentheses whitespaces.
            if (this.noHeightAvailable(openingParenthesesWS.get(FormattingConstants.WS).getAsString())) {
                openingParenthesesWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update closing parentheses whitespaces.
            if (this.noHeightAvailable(closingParenthesesWS.get(FormattingConstants.WS).getAsString())) {
                closingParenthesesWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update whitespaces for the function type.
            for (int i = 0; i < ws.size(); i++) {
                JsonObject functionTypeWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(functionTypeWS.get(FormattingConstants.WS).getAsString())) {
                    String text = functionTypeWS.get(FormattingConstants.TEXT).getAsString();

                    // Update whitespace for parameter separator and closing parentheses.
                    if (text.equals(",")) {
                        functionTypeWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update whitespace for returns keyword.
                    if (text.equals("returns")) {
                        functionTypeWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Update whitespaces for the parameters.
            if (node.has("params")) {
                JsonArray parameters = node.getAsJsonArray("params");
                iterateAndFormatMembers(indentation, parameters);
            }

            if (returnKeywordExists) {
                JsonObject returnType = node.getAsJsonObject("returnTypeNode");
                JsonObject returnTypeFormatConfig = this.getFormattingConfig(0, 1,
                        0, false, this.getWhiteSpaceCount(indentation));
                returnType.add(FormattingConstants.FORMATTING_CONFIG, returnTypeFormatConfig);

                modifyReturnTypeAnnotations(node, indentation);
            }
        }
    }

    /**
     * format Group By node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatGroupByNode(JsonObject node) {
        // TODO: fix formatting for group by node.
        this.skipFormatting(node, true);
    }

    /**
     * format Having node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatHavingNode(JsonObject node) {
        // TODO: fix formatting for having node.
        this.skipFormatting(node, true);
    }

    /**
     * format Is assignable expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatIsAssignableExprNode(JsonObject node) {
        // TODO: fix formatting for having node.
        this.skipFormatting(node, true);
    }

    /**
     * format Identifier node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatIdentifierNode(JsonObject node) {
        // TODO: fix formatting for identifier node.
        this.skipFormatting(node, true);
    }

    /**
     * format If node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatIfNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentWithParentIndentation));

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update if or else if keyword.
            JsonObject firstKeywordWS = ws.get(0).getAsJsonObject();
            if (node.has("isElseIfBlock") && node.get("isElseIfBlock").getAsBoolean()) {
                if (this.noHeightAvailable(firstKeywordWS.get(FormattingConstants.WS).getAsString())) {
                    firstKeywordWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }

                // Update if keyword whitespace.
                JsonObject ifKeywordWS = ws.get(1).getAsJsonObject();
                if (this.noHeightAvailable(ifKeywordWS.get(FormattingConstants.WS).getAsString())) {
                    ifKeywordWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            } else {
                if (this.noHeightAvailable(firstKeywordWS.get(FormattingConstants.WS).getAsString())) {
                    firstKeywordWS.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                    indentWithParentIndentation);
                }
            }

            // Update opening brace whitespace.
            JsonObject openingBraceWS = ws.get(ws.size() - 2).getAsJsonObject();
            if (this.noHeightAvailable(openingBraceWS.get(FormattingConstants.WS).getAsString())) {
                openingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update closing brace whitespace
            JsonObject closingBraceWS = ws.get(ws.size() - 1).getAsJsonObject();
            modifyBlockBody(node, indentWithParentIndentation, closingBraceWS, FormattingConstants.BODY);

            if (node.has("elseStatement")
                    && !node.getAsJsonObject("elseStatement").get("kind").getAsString().equals("Block")) {
                JsonObject elseStatement = node.getAsJsonObject("elseStatement");
                JsonObject elseStatementFormatConfig = this.getFormattingConfig(0, 1,
                        this.getWhiteSpaceCount(indentation), false,
                        this.getWhiteSpaceCount(indentWithParentIndentation));
                elseStatement.add(FormattingConstants.FORMATTING_CONFIG, elseStatementFormatConfig);
            }

            if (node.has("condition")) {
                JsonObject conditionWs = node.getAsJsonObject("condition");
                JsonObject conditionFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentWithParentIndentation));
                conditionWs.add(FormattingConstants.FORMATTING_CONFIG, conditionFormatConfig);
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
            if (this.noHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                                indentation);
            }

            // Update whitespace for semicolon
            JsonObject semicolonWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (this.noHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
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

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            // Preserve user added new lines and comments.
            this.preserveHeight(ws, indentWithParentIndentation);

            // If expression is available handle expression whitespaces
            // else handle node's identifier whitespaces.
            if (node.has(FormattingConstants.EXPRESSION) && node.getAsJsonObject(FormattingConstants.EXPRESSION)
                    .has(FormattingConstants.WS)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                expression.add(FormattingConstants.FORMATTING_CONFIG, formatConfig);

                // Update whitespaces for '.' (dot) or '->' (action invocation).
                JsonObject dotActionWhitespace = ws.get(0).getAsJsonObject();
                if (this.noHeightAvailable(dotActionWhitespace.get(FormattingConstants.WS).getAsString())) {
                    dotActionWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }

                // Update whitespaces for action identifier.
                JsonObject identifierWhitespace = ws.get(1).getAsJsonObject();
                if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                    identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }

                // Update whitespace for open parentheses.
                JsonObject openParenthesesWhitespace = ws.get(2).getAsJsonObject();
                if (this.noHeightAvailable(openParenthesesWhitespace.get(FormattingConstants.WS).getAsString())) {
                    openParenthesesWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            } else {
                // Update whitespace for identifier or package alias.
                JsonObject identifierOrAliasWhitespace = ws.get(0).getAsJsonObject();
                if (this.noHeightAvailable(identifierOrAliasWhitespace.get(FormattingConstants.WS).getAsString())) {
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
                    if (this.noHeightAvailable(colonWhitespace.get(FormattingConstants.WS).getAsString())) {
                        colonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update whitespace for identifier.
                    JsonObject identifierWhitespace = ws.get(2).getAsJsonObject();
                    if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                        identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update whitespace for opening parentheses.
                    JsonObject openingParenthesesWhitespace = ws.get(3).getAsJsonObject();
                    if (this.noHeightAvailable(openingParenthesesWhitespace.get(FormattingConstants.WS)
                            .getAsString())) {
                        openingParenthesesWhitespace.addProperty(FormattingConstants.WS,
                                FormattingConstants.EMPTY_SPACE);
                    }

                } else {
                    // Update whitespace for opening parentheses.
                    JsonObject openingParenthesesWhitespace = ws.get(1).getAsJsonObject();
                    if (this.noHeightAvailable(openingParenthesesWhitespace.get(FormattingConstants.WS)
                            .getAsString())) {
                        openingParenthesesWhitespace.addProperty(FormattingConstants.WS,
                                FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Update whitespace for closing parentheses.
            JsonObject closingParenthesesWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (this.noHeightAvailable(closingParenthesesWhitespace.get(FormattingConstants.WS).getAsString())) {
                closingParenthesesWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update argument expressions whitespaces.
            if (node.has("argumentExpressions")) {
                JsonArray argumentExpressions = node.getAsJsonArray("argumentExpressions");
                iterateAndFormatMembers(indentation, argumentExpressions);
            }
        }
    }

    /**
     * format Lambda node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatLambdaNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG) && node.has("functionNode")) {
            JsonObject functionNode = node.getAsJsonObject("functionNode");
            functionNode.add(FormattingConstants.FORMATTING_CONFIG,
                    node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG));
        }
    }

    /**
     * format Limit node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatLimitNode(JsonObject node) {
        // TODO: fix formatting for limit node.
        this.skipFormatting(node, true);
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
            if (this.noHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                                indentation);
            }
        }
    }

    /**
     * format Lock node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatLockNode(JsonObject node) {
        // TODO: fix formatting for lock node.
        this.skipFormatting(node, true);
    }

    /**
     * format Match node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Get the indentation for the node.
            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            this.preserveHeight(ws, indentation);

            // Update match whitespaces.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    String text = wsItem.get(FormattingConstants.TEXT).getAsString();

                    // Update match keyword whitespaces.
                    if (text.equals("match")) {
                        wsItem.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }

                    // Update opening bracket whitespace.
                    if (text.equals("{")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update opening bracket whitespace.
                    if (text.equals("}")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
                    }
                }
            }

            // Update expression whitespace.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentation));
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }

            // Update pattern clauses whitespace.
            modifyPatternClauses(node, indentation);
        }
    }

    /**
     * format Match Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchExpressionNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Get the indentation for the node.
            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            this.preserveHeight(ws, indentWithParentIndentation);

            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            // Update match whitespaces.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    String text = wsItem.get(FormattingConstants.TEXT).getAsString();

                    // Update match keyword whitespaces.
                    if (text.equals("but")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update opening bracket whitespace.
                    if (text.equals("{")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update expression separator whitespace.
                    if (text.equals(",")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update opening bracket whitespace.
                    if (text.equals("}")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                indentWithParentIndentation);
                    }
                }
            }

            // Update pattern clauses whitespace.
            modifyPatternClauses(node, indentWithParentIndentation);
        }
    }

    /**
     * format Match Expression Pattern Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchExpressionPatternClauseNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Get the indentation for the node.
            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, indentation);

            // Handle whitespace for variable node.
            if (node.has("variableNode")) {
                node.getAsJsonObject("variableNode").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // Update the arrow whitespace.
            JsonObject arrowWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(arrowWS.get(FormattingConstants.WS).getAsString())) {
                arrowWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespace for statement.
            if (node.has("statement")) {
                JsonObject statementFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentation));
                node.getAsJsonObject("statement").add(FormattingConstants.FORMATTING_CONFIG, statementFormatConfig);
            }
        }
    }

    /**
     * format Match Pattern Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchPatternClauseNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            boolean withCurlies = node.has("withCurlies") && node.get("withCurlies").getAsBoolean();

            // Get the indentation for the node.
            String indentation = (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB)
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, indentation);

            // Handle whitespace for
            if (node.has("variableNode")) {
                node.getAsJsonObject("variableNode").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // Update the match pattern whitespace.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    String text = wsItem.get(FormattingConstants.TEXT).getAsString();

                    // Update the => whitespace.
                    if (text.equals("=>")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update the opening brace whitespace.
                    if (text.equals("{")) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update the closing brace whitespace.
                    if (text.equals("}")) {
                        if (node.has("statement") && node.getAsJsonObject("statement")
                                .has(FormattingConstants.STATEMENTS) && node.getAsJsonObject("statement")
                                .getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0) {
                            wsItem.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation
                                    + FormattingConstants.NEW_LINE + indentation);
                        } else {
                            wsItem.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    indentation);
                        }
                    }
                }
            }

            if (node.has("statement") && !withCurlies) {
                JsonObject statementFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentation));
                node.getAsJsonObject("statement").add(FormattingConstants.FORMATTING_CONFIG, statementFormatConfig);
            }
        }
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
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());
                    fields.get(i).getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, fieldFormatConfig);
                }
            }

            if (node.has("initFunction")) {
                JsonObject initFunction = node.getAsJsonObject("initFunction");
                JsonObject functionFormatConfig = this.getFormattingConfig(2, 0,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());
                initFunction.add(FormattingConstants.FORMATTING_CONFIG, functionFormatConfig);
            }

            if (node.has("functions")) {
                JsonArray functions = node.getAsJsonArray("functions");
                for (int i = 0; i < functions.size(); i++) {
                    JsonObject functionFormatConfig = this.getFormattingConfig(2, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());
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
        // TODO: fix formatting for order by node.
        this.skipFormatting(node, true);
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

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            String parentKind = node.getAsJsonObject(FormattingConstants.PARENT).get("kind").getAsString();

            boolean isTable = parentKind.equals("Table");
            boolean isExpression = parentKind.equals("Endpoint") || parentKind.equals("AnnotationAttachment") ||
                    parentKind.equals("Service") || parentKind.equals("Variable") || parentKind.equals("Invocation");

            if (isExpression) {
                node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                        indentation);
            }

            this.preserveHeight(ws, indentWithParentIndentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();

                // Update whitespace for opening brace.
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("{")
                        && this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (isExpression) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    } else if (isTable) {
                        currentWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                .get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) + indentWithParentIndentation);
                    }
                }

                // Update whitespace for closing brace.
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("}")
                        && this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (isExpression) {
                        if (node.has("keyValuePairs")
                                && node.getAsJsonArray("keyValuePairs").size() <= 0) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    indentWithParentIndentation + FormattingConstants.NEW_LINE +
                                    indentWithParentIndentation);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    FormattingConstants.NEW_LINE + indentWithParentIndentation);
                        }
                    } else if (isTable) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }

                // Update whitespaces for the key value pair separator , or ;.
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(",") ||
                        currentWS.get(FormattingConstants.TEXT).getAsString().equals(";")) {
                    currentWS.addProperty(FormattingConstants.WS,
                            FormattingConstants.EMPTY_SPACE);
                }
            }

            // Update the key value pair of a record.
            if (node.has("keyValuePairs")) {
                JsonArray keyValuePairs = node.getAsJsonArray("keyValuePairs");
                for (int i = 0; i < keyValuePairs.size(); i++) {
                    JsonObject keyValue = keyValuePairs.get(i).getAsJsonObject();
                    JsonObject keyValueFormatting;
                    if (i == 0 && isTable) {
                        keyValueFormatting = this.getFormattingConfig(0, 0,
                                this.getWhiteSpaceCount(indentWithParentIndentation
                                        + FormattingConstants.SPACE_TAB), false,
                                this.getWhiteSpaceCount(indentWithParentIndentation));
                    } else if (isExpression) {
                        keyValueFormatting = this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentWithParentIndentation), true,
                                this.getWhiteSpaceCount(indentWithParentIndentation));
                    } else {
                        keyValueFormatting = this.getFormattingConfig(0, 1,
                                this.getWhiteSpaceCount(indentWithParentIndentation
                                        + FormattingConstants.SPACE_TAB), false,
                                this.getWhiteSpaceCount(indentWithParentIndentation));
                    }

                    keyValue.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, keyValueFormatting);
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
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());
                keyNode.add(FormattingConstants.FORMATTING_CONFIG, keyNodeFormatConfig);
            }

            // Update whitespace for colon of the record literal key value pair.
            this.preserveHeight(ws, indentation);
            if (this.noHeightAvailable(ws.get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString())) {
                ws.get(0).getAsJsonObject().addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update whitespace for value of record literal.
            if (node.has("value")) {
                JsonObject valueNode = node.getAsJsonObject("value");
                JsonObject valueNodeFormatConfig = this.getFormattingConfig(0, 1,
                        0, false,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());
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
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());
                child.add(FormattingConstants.FORMATTING_CONFIG, childFormatConfig);
            }

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                // Update the restField whitespace.
                if (node.has("restFieldType") &&
                        node.get("restFieldType").getAsJsonObject().has(FormattingConstants.WS)) {

                    JsonObject restFieldType = node.getAsJsonObject("restFieldType");
                    JsonObject restFieldTypeFormatConfig = this.getFormattingConfig(1, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());
                    restFieldType.add(FormattingConstants.FORMATTING_CONFIG, restFieldTypeFormatConfig);

                    // Update the ... symbol whitespace.
                    JsonObject restWS = ws.get(0).getAsJsonObject();
                    if (this.noHeightAvailable(restWS.get(FormattingConstants.WS).getAsString())) {
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
                    if (this.noHeightAvailable(sealedWS.get(FormattingConstants.WS).getAsString())) {
                        sealedWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
                    }

                    // Update the ... symbol whitespace.
                    JsonObject restWS = ws.get(1).getAsJsonObject();
                    if (this.noHeightAvailable(restWS.get(FormattingConstants.WS).getAsString())) {
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

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentWithParentIndentation));

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update whitespaces of resource name.
            JsonObject resourceNameWhitespace = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(resourceNameWhitespace.get(FormattingConstants.WS).getAsString())) {
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
            if (this.noHeightAvailable(openingParenthesesWhitespace.get(FormattingConstants.WS).getAsString())) {
                openingParenthesesWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // TODO update whitespaces of parameter separators.

            // Update whitespace of closing parentheses.
            JsonObject closingParenthesesWhitespace = ws.get(ws.size() - 3).getAsJsonObject();
            if (this.noHeightAvailable(closingParenthesesWhitespace.get(FormattingConstants.WS).getAsString())) {
                closingParenthesesWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update opening bracket whitespaces.
            JsonObject openingBracketWhitespace = ws.get(ws.size() - 2).getAsJsonObject();
            if (this.noHeightAvailable(openingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                openingBracketWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update closing bracket whitespace.
            JsonObject closingBracketWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (node.has(FormattingConstants.BODY)
                    && node.getAsJsonObject(FormattingConstants.BODY)
                    .getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0
                    && node.getAsJsonArray("workers").size() <= 0
                    && node.getAsJsonArray("endpointNodes").size() <= 0) {
                if (this.noHeightAvailable(closingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                    closingBracketWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                            indentation + FormattingConstants.NEW_LINE + indentation);
                }
            } else if (this.noHeightAvailable(closingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                closingBracketWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                        this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                                .get(FormattingConstants.START_COLUMN).getAsInt()));
            }

            // update the parameter whitespace in resource.
            if (node.has("parameters")) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                iterateAndFormatMembers(indentation, parameters);
            }

            // Update endpoint whitespaces in resource.
            if (node.has("endpointNodes")) {
                JsonArray endpointNodes = node.getAsJsonArray("endpointNodes");
                for (int i = 0; i < endpointNodes.size(); i++) {
                    JsonObject endpointNode = endpointNodes.get(i).getAsJsonObject();
                    JsonObject endpointFormatConfig;
                    if (i == 0) {
                        endpointFormatConfig = this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentation), true,
                                this.getWhiteSpaceCount(indentWithParentIndentation));
                    } else {
                        endpointFormatConfig = this.getFormattingConfig(2, 0,
                                this.getWhiteSpaceCount(indentation), true,
                                this.getWhiteSpaceCount(indentWithParentIndentation));
                    }

                    endpointNode.add(FormattingConstants.FORMATTING_CONFIG, endpointFormatConfig);
                }
            }

            // Update annotation whitespaces in resource.
            modifyAnnotationAttachments(node, formatConfig, indentation);

            // Update workers whitespace in resource.
            modifyWorkers(node, indentation);
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
        modifyBranchingStatement(node);
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

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update return keyword.
            JsonObject returnKeywordWhitespace = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(returnKeywordWhitespace.get(FormattingConstants.WS).getAsString())) {
                returnKeywordWhitespace.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            }

            // Update expression whitespaces.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentation));
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }

            // Update semicolon whitespace.
            JsonObject semicolonWhitespace = ws.get(1).getAsJsonObject();
            if (this.noHeightAvailable(semicolonWhitespace.get(FormattingConstants.WS).getAsString())) {
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
            if (this.noHeightAvailable(serviceKeywordWhitespace.get(FormattingConstants.WS).getAsString())) {
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
            } else if (this.noNewLine(serviceKeywordWhitespace.get(FormattingConstants.WS)
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
                if (this.noHeightAvailable(lessThanSymbolWhitespace.get(FormattingConstants.WS).getAsString())) {
                    lessThanSymbolWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }

                // Update whitespace of the greater than (>) symbol.
                JsonObject greaterThanSymbolWhitespace = ws.get(1).getAsJsonObject();
                if (this.noHeightAvailable(greaterThanSymbolWhitespace.get(FormattingConstants.WS).getAsString())) {
                    greaterThanSymbolWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            }

            if (node.has("boundEndpoints") &&
                    node.getAsJsonArray("boundEndpoints").size() > 0) {
                // Update whitespaces for service name.
                JsonObject serviceNameWhitespace = ws.get(ws.size() - 4).getAsJsonObject();
                if (this.noHeightAvailable(serviceNameWhitespace.get(FormattingConstants.WS).getAsString())) {
                    serviceNameWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }

                // Update whitespaces for bind keyword.
                JsonObject bindKeywordWhitespace = ws.get(ws.size() - 3).getAsJsonObject();
                if (this.noHeightAvailable(bindKeywordWhitespace.get(FormattingConstants.WS).getAsString())) {
                    bindKeywordWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            } else {
                // Update whitespaces for service name.
                JsonObject serviceNameWhitespace = ws.get(ws.size() - 3).getAsJsonObject();
                if (this.noHeightAvailable(serviceNameWhitespace.get(FormattingConstants.WS).getAsString())) {
                    serviceNameWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            }

            // Update whitespaces for opening bracket.
            JsonObject openingBracketWhitespace = ws.get(ws.size() - 2).getAsJsonObject();
            if (this.noHeightAvailable(openingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                openingBracketWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespaces for closing bracket.
            JsonObject closingBracketWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
            if (node.getAsJsonArray("resources").size() <= 0
                    && node.getAsJsonArray("variables").size() <= 0
                    && node.getAsJsonArray("endpointNodes").size() <= 0
                    && node.getAsJsonArray("namespaceDeclarations").size() <= 0) {
                if (this.noHeightAvailable(closingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                    closingBracketWhitespace.addProperty(FormattingConstants.WS,
                            FormattingConstants.NEW_LINE + indentation + FormattingConstants.NEW_LINE +
                                    indentation);
                }
            } else if (this.noHeightAvailable(closingBracketWhitespace.get(FormattingConstants.WS).getAsString())) {
                closingBracketWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                        indentation);
            }

            // Update whitespace for endpoints.
            modifyEndpoints(node, indentation);

            // Update whitespaces for resources.
            if (node.has("resources")) {
                JsonArray resources = node.getAsJsonArray("resources");
                iterateAndFormatBlockStatements(formatConfig, indentation, resources);
            }

            // Update whitespaces for variables.
            if (node.has("variables")) {
                JsonArray variables = node.getAsJsonArray("variables");
                for (int i = 0; i < variables.size(); i++) {
                    JsonObject variable = variables.get(i).getAsJsonObject();
                    JsonObject variableFormatConfig = this.getFormattingConfig(1, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                            this.getWhiteSpaceCount(indentation));
                    variable.add(FormattingConstants.FORMATTING_CONFIG, variableFormatConfig);
                }
            }

            modifyAnnotationAttachments(node, formatConfig, indentation);

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

            modifyWorkers(node, indentation);
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
            if (this.noHeightAvailable(referenceWS.get(FormattingConstants.WS).getAsString())) {
                if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                    referenceWS.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                    + indentation);
                } else if (formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt() > 0) {
                    referenceWS.addProperty(FormattingConstants.WS,
                            this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                } else {
                    referenceWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
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
     * format Table Column node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTableColumnNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            this.preserveHeight(ws, indentWithParentIndentation);

            if (ws.size() > 1) {
                JsonObject primaryKeyWS = ws.get(0).getAsJsonObject();
                if (this.noHeightAvailable(primaryKeyWS.get(FormattingConstants.WS).getAsString())) {
                    primaryKeyWS.addProperty(FormattingConstants.WS, this.getWhiteSpaces(formatConfig
                            .get(FormattingConstants.SPACE_COUNT).getAsInt()));
                }

                JsonObject identifierWS = ws.get(1).getAsJsonObject();
                if (this.noHeightAvailable(identifierWS.get(FormattingConstants.WS).getAsString())) {
                    identifierWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            } else {
                JsonObject identifierWS = ws.get(1).getAsJsonObject();
                if (this.noHeightAvailable(identifierWS.get(FormattingConstants.WS).getAsString())) {
                    identifierWS.addProperty(FormattingConstants.WS, this.getWhiteSpaces(formatConfig
                            .get(FormattingConstants.SPACE_COUNT).getAsInt()));
                }
            }
        }
    }

    /**
     * format table literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTableNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            String indentation = indentWithParentIndentation + FormattingConstants.SPACE_TAB;

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentWithParentIndentation));

            this.preserveHeight(ws, indentWithParentIndentation);

            int openBracesCount = 0;
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                // Update whitespace for table keyword.
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("table")) {
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }

                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(",")) {
                    if (openBracesCount == 1) {
                        if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            this.preserveHeightForWS(currentWS, indentation);
                        }
                    } else {
                        if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            this.preserveHeightForWS(currentWS, indentation + FormattingConstants.SPACE_TAB);
                        }
                    }
                }

                // Update whitespace for opening brace or bracket.
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("{") ||
                        currentWS.get(FormattingConstants.TEXT).getAsString().equals("[")) {
                    if (openBracesCount > 0) {
                        if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
                        } else {
                            this.preserveHeightForWS(currentWS, indentation);
                        }
                    } else {
                        if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    }
                }

                // Update whitespace for closing brace.
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("}")) {
                    if (openBracesCount == 1) {
                        if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                            if (node.has("dataRows")
                                    && node.has("tableColumns")
                                    && node.getAsJsonArray("dataRows").size() <= 0
                                    && node.getAsJsonArray("tableColumns").size() <= 0) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                                        + indentWithParentIndentation + FormattingConstants.NEW_LINE
                                        + indentWithParentIndentation);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                                        + indentWithParentIndentation);
                            }
                        }
                    } else {
                        if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            preserveHeightForWS(currentWS, indentation);
                        }
                    }
                }

                // Update whitespace for closing bracket.
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("]")) {
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        if (node.has("dataRows")
                                && node.getAsJsonArray("dataRows").size() <= 0) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                                    + indentation);
                        }
                    } else {
                        this.preserveHeightForWS(currentWS, indentation);
                    }
                }


                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("{")
                        || currentWS.get(FormattingConstants.TEXT).getAsString().equals("[")) {
                    if (openBracesCount > 0) {
                        openBracesCount++;
                    } else {
                        openBracesCount = 1;
                    }
                }

                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("}")
                        || currentWS.get(FormattingConstants.TEXT).getAsString().equals("]")) {
                    if (openBracesCount > 1) {
                        openBracesCount--;
                    }
                }
            }

            if (node.has("dataRows")) {
                JsonArray dataRows = node.getAsJsonArray("dataRows");
                for (JsonElement dataRowItem : dataRows) {
                    JsonObject dataRow = dataRowItem.getAsJsonObject();
                    JsonObject rowFormatConfig = this.getFormattingConfig(1, 0,
                            this.getWhiteSpaceCount(indentation), true,
                            this.getWhiteSpaceCount(indentation));
                    dataRow.add(FormattingConstants.FORMATTING_CONFIG, rowFormatConfig);
                }
            }

            if (node.has("tableColumns")) {
                JsonArray tableColumns = node.getAsJsonArray("tableColumns");
                this.iterateAndFormatMembers(indentation + FormattingConstants.SPACE_TAB, tableColumns);
            }
        }
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt())
                            + FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, indentation);

            // Update transaction and retry whitespaces.
            boolean isRetryBody = false;
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();

                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("transaction")) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("onretry")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        isRetryBody = true;
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("{")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("}")) {
                        if (isRetryBody) {
                            modifyBlockBody(node, indentation, currentWS, "onRetryBody");
                        } else {
                            modifyBlockBody(node, indentation, currentWS, "transactionBody");
                        }
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("with")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("retries")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("=")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(",")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("onabort")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("oncommit")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Update whitespaces for retryCount.
            if (node.has("retryCount")) {
                this.skipFormatting(node.getAsJsonObject("retryCount"), true);
            }

            // Update whitespaces for onAbort function.
            if (node.has("onAbortFunction")) {
                this.skipFormatting(node.getAsJsonObject("onAbortFunction"), true);
            }

            // Update whitespaces for onCommit function.
            if (node.has("onCommitFunction")) {
                this.skipFormatting(node.getAsJsonObject("onCommitFunction"), true);
            }
        }
    }

    /**
     * format Try node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTryNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentWithParentIndentation));

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update whitespaces for try keyword.
            JsonObject tryKeywordWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(tryKeywordWS.get(FormattingConstants.WS).getAsString())) {
                tryKeywordWS.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentWithParentIndentation);
            }

            // Update try block opening brace whitespace.
            JsonObject tryBlockOpeningBraceWS = ws.get(1).getAsJsonObject();
            if (this.noHeightAvailable(tryBlockOpeningBraceWS.get(FormattingConstants.WS).getAsString())) {
                tryBlockOpeningBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update try block closing brace whitespace.
            JsonObject tryBlockClosingBraceWS = ws.get(2).getAsJsonObject();
            if (this.noHeightAvailable(tryBlockClosingBraceWS.get(FormattingConstants.WS).getAsString())) {
                if (node.has(FormattingConstants.BODY) &&
                        node.getAsJsonObject(FormattingConstants.BODY).has(FormattingConstants.STATEMENTS) &&
                        node.getAsJsonObject(FormattingConstants.BODY).getAsJsonArray(FormattingConstants.STATEMENTS)
                                .size() <= 0) {
                    tryBlockClosingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                            indentWithParentIndentation + FormattingConstants.NEW_LINE + indentWithParentIndentation);
                } else {
                    tryBlockClosingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                            indentWithParentIndentation);
                }
            }

            // Update whitespace for finally body.
            if (node.has("finallyBody")) {
                // Update finally keyword whitespace.
                JsonObject finallyKeywordWS = ws.get(ws.size() - 3).getAsJsonObject();
                if (this.noHeightAvailable(finallyKeywordWS.get(FormattingConstants.WS).getAsString())) {
                    finallyKeywordWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }

                // Update whitespaces for opening brace of finally block.
                JsonObject finallyBlockOpeningBraceWS = ws.get(ws.size() - 2).getAsJsonObject();
                if (this.noHeightAvailable(finallyBlockOpeningBraceWS.get(FormattingConstants.WS).getAsString())) {
                    finallyBlockOpeningBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }

                // Update whitespaces for closing brace of finally block.
                JsonObject finallyBlockClosingBraceWS = ws.get(ws.size() - 1).getAsJsonObject();
                if (this.noHeightAvailable(finallyBlockClosingBraceWS.get(FormattingConstants.WS).getAsString())) {
                    JsonObject finallyBody = node.getAsJsonObject("finallyBody");
                    if (finallyBody.has(FormattingConstants.BODY) &&
                            finallyBody.getAsJsonObject(FormattingConstants.BODY).has(FormattingConstants.STATEMENTS) &&
                            finallyBody.getAsJsonObject(FormattingConstants.BODY)
                                    .getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0) {
                        finallyBlockClosingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                indentWithParentIndentation + FormattingConstants.NEW_LINE +
                                indentWithParentIndentation);
                    } else {
                        finallyBlockClosingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                indentWithParentIndentation);
                    }
                }
            }

            // Handle whitespace of catch blocks.
            if (node.has("catchBlocks")) {
                for (JsonElement catchBlock : node.getAsJsonArray("catchBlocks")) {
                    catchBlock.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 1, 0, false,
                                    this.getWhiteSpaceCount(indentWithParentIndentation)));
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

                if (this.noHeightAvailable(varWS.get(FormattingConstants.WS).getAsString())) {
                    varWS.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                    indentation);
                }

                if (this.noHeightAvailable(openingParenthesesWS.get(FormattingConstants.WS).getAsString())) {
                    openingParenthesesWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            } else {
                JsonObject openingParenthesesWS = ws.get(0).getAsJsonObject();
                if (this.noHeightAvailable(openingParenthesesWS.get(FormattingConstants.WS).getAsString())) {
                    openingParenthesesWS.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                    indentation);
                }
            }

            // Update variable references' whitespaces.
            if (node.has("variableRefs")) {
                JsonArray varRefs = node.getAsJsonArray("variableRefs");
                modifyVariableReferences(formatConfig, indentation, varRefs);
            }

            // Update closing parentheses whitespace.
            JsonObject closingParenWS = ws.get(ws.size() - 3).getAsJsonObject();
            if (this.noHeightAvailable(closingParenWS.get(FormattingConstants.WS).getAsString())) {
                closingParenWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }

            // Update assignment sign for tuple destruct.
            JsonObject equalWS = ws.get(ws.size() - 2).getAsJsonObject();
            if (this.noHeightAvailable(equalWS.get(FormattingConstants.WS).getAsString())) {
                equalWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespace for expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false,
                        this.getWhiteSpaceCount(indentation));
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }

            // Update semicolon whitespace
            JsonObject semicolonWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (this.noHeightAvailable(semicolonWS.get(FormattingConstants.WS).getAsString())) {
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
            if (this.noHeightAvailable(openingParentheses.get(FormattingConstants.WS).getAsString())) {
                openingParentheses.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            }

            // Update the whitespaces for member types in tuple.
            if (node.has("memberTypeNodes")) {
                JsonArray memberTypeNodes = node.getAsJsonArray("memberTypeNodes");
                modifyVariableReferences(formatConfig, indentation, memberTypeNodes);
            }

            // Update the whitespace for closing parentheses.
            JsonObject closingParentheses = ws.get(ws.size() - 1).getAsJsonObject();
            if (this.noHeightAvailable(closingParentheses.get(FormattingConstants.WS).getAsString())) {
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
            boolean isEnum = true;

            // Handles whitespace for type def.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();

                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    // Update the type or public keywords whitespace.
                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("public")) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                        indentation);
                    }

                    // Update type keyword whitespace.
                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("type")) {
                        if (node.has(FormattingConstants.PUBLIC)
                                && node.get(FormattingConstants.PUBLIC).getAsBoolean()) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                            indentation);
                        }
                    }

                    // Update record or object keyword whitespace.
                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("object") ||
                            currentWS.get(FormattingConstants.TEXT).getAsString().equals("record")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update identifier whitespace.
                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(
                            node.getAsJsonObject("name").get("valueWithBar").getAsString())) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update opening bracket whitespace.
                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("{")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        isEnum = false;
                    }

                    // Update the closing bracket whitespaces.
                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("}")) {
                        if (node.has("typeNode")
                                && node.getAsJsonObject("typeNode")
                                .getAsJsonArray(FormattingConstants.FIELDS).size() <= 0) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    indentation + FormattingConstants.NEW_LINE + indentation);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
                        }
                    }

                    // Update the semicolon whitespace.
                    if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(";")) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle the whitespace for type node.
            if (node.has("typeNode")) {
                if (isEnum) {
                    JsonObject typeNodeFormatConfig = this.getFormattingConfig(0, 1,
                            0, false, this.getWhiteSpaceCount(indentation));
                    node.getAsJsonObject("typeNode").add(FormattingConstants.FORMATTING_CONFIG, typeNodeFormatConfig);
                } else {
                    JsonObject typeNodeFormatConfig = this.getFormattingConfig(1, 0,
                            this.getWhiteSpaceCount(indentation), true, this.getWhiteSpaceCount(indentation));
                    node.getAsJsonObject("typeNode").add(FormattingConstants.FORMATTING_CONFIG, typeNodeFormatConfig);
                }
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
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            boolean isGrouped = node.has(FormattingConstants.GROUPED) &&
                    node.get(FormattingConstants.GROUPED).getAsBoolean();

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                            FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                this.preserveHeight(ws, indentation);

                // Iterate through WS to update horizontal whitespaces.
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        // Update opening parentheses whitespace.
                        if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("(")) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                            indentation);
                        }

                        // Update pipe symbol whitespace.
                        if (currentWS.get(FormattingConstants.TEXT).getAsString().equals("|")) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }

                        // Update closing parentheses whitespace.
                        if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(")")) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    }
                }
            }

            // Update member types whitespaces.
            if (node.has("memberTypeNodes")) {
                JsonArray memberTypeNodes = node.getAsJsonArray("memberTypeNodes");
                for (int i = 0; i < memberTypeNodes.size(); i++) {
                    JsonObject memberType = memberTypeNodes.get(i).getAsJsonObject();
                    JsonObject memberTypeFormatConfig;
                    if (i == 0 && !node.getAsJsonObject(FormattingConstants.PARENT)
                            .get("kind").getAsString().equals("TypeDefinition")) {
                        if (isGrouped) {
                            memberTypeFormatConfig = this.getFormattingConfig(0, 0,
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false,
                                    this.getWhiteSpaceCount(indentation));
                        } else {
                            memberTypeFormatConfig = this.getFormattingConfig(
                                    formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                    formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                    formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                                    this.getWhiteSpaceCount(indentation));
                        }
                    } else {
                        memberTypeFormatConfig = this.getFormattingConfig(0, 1,
                                0, false,
                                this.getWhiteSpaceCount(indentation));
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

            if (node.has(FormattingConstants.WS) && !node.has(FormattingConstants.IS_ANON_TYPE)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                String indentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                        ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt())
                        + FormattingConstants.SPACE_TAB)
                        : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());

                node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                        this.getWhiteSpaceCount(indentation +
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt())));
                this.preserveHeight(ws, indentation);

                // Handle package alias if available.
                if (ws.size() > 1) {
                    // Update whitespace for package alias.
                    JsonObject packageAliasWhitespace = ws.get(0).getAsJsonObject();
                    if (this.noHeightAvailable(packageAliasWhitespace.get(FormattingConstants.WS).getAsString())) {
                        packageAliasWhitespace.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation + this.getWhiteSpaces(formatConfig
                                        .get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }

                    // Update whitespace for package access colon.
                    JsonObject packageColonWhitespace = ws.get(ws.size() - 2).getAsJsonObject();
                    if (this.noHeightAvailable(packageColonWhitespace.get(FormattingConstants.WS).getAsString())) {
                        packageColonWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update whitespace for identifier
                    JsonObject identifierWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
                    if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                        identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                } else {
                    // Update whitespace for identifier
                    JsonObject identifierWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
                    if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                        identifierWhitespace.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation + this.getWhiteSpaces(formatConfig
                                        .get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }
                }
            } else if (node.has(FormattingConstants.IS_ANON_TYPE) &&
                    node.get(FormattingConstants.IS_ANON_TYPE).getAsBoolean()) {
                JsonObject anonType = node.getAsJsonObject(FormattingConstants.ANON_TYPE);
                JsonObject anonTypeFormatConfig = this.getFormattingConfig(
                        formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                        formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                        formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                        formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());
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
            if (this.noHeightAvailable(typeWhitespace.get(FormattingConstants.WS).getAsString())) {
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
            if (this.noHeightAvailable(node.getAsJsonArray(FormattingConstants.WS).get(0)
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
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt())
                        + (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                        ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt())
                        + FormattingConstants.SPACE_TAB)
                        : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

                String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                        ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                        FormattingConstants.SPACE_TAB
                        : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

                node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                        this.getWhiteSpaceCount(indentation));

                this.preserveHeight(ws, indentWithParentIndentation);

                // Format type node
                if (node.has("typeNode")) {
                    JsonObject typeNode = node.getAsJsonObject("typeNode");
                    JsonObject typeFormatConfig;

                    // Update the record or public keyword whitespaces.
                    JsonObject firstKeywordWS = ws.get(0).getAsJsonObject();

                    if (node.has(FormattingConstants.IS_ANON_TYPE)
                            && node.get(FormattingConstants.IS_ANON_TYPE).getAsBoolean()) {

                        if (node.has(FormattingConstants.FINAL) && node.get(FormattingConstants.FINAL).getAsBoolean()) {
                            if (this.noHeightAvailable(firstKeywordWS.get(FormattingConstants.WS).getAsString())) {
                                // Update the record keyword.
                                firstKeywordWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }
                        } else if ((node.has(FormattingConstants.PUBLIC) &&
                                node.get(FormattingConstants.PUBLIC).getAsBoolean() &&
                                firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                        .equals(FormattingConstants.PUBLIC))) {
                            if (this.noHeightAvailable(firstKeywordWS.getAsJsonObject()
                                    .get(FormattingConstants.WS).getAsString())) {
                                // Update the public keyword.
                                firstKeywordWS.addProperty(FormattingConstants.WS,
                                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                                .getAsInt()) + indentation);
                            }

                            // Update the record keyword.
                            JsonObject recordWS = ws.get(1).getAsJsonObject();
                            if (this.noHeightAvailable(recordWS.get(FormattingConstants.WS).getAsString())) {
                                recordWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }

                        } else {
                            // Update record keyword whitespace.
                            if (this.noHeightAvailable(firstKeywordWS.getAsJsonObject().get(FormattingConstants.WS)
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
                            if (this.noHeightAvailable(semiColonWS.get(FormattingConstants.WS).getAsString())) {
                                semiColonWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            }
                        } else {
                            openingParenWS = ws.get(ws.size() - 3).getAsJsonObject();
                            closingParentWS = ws.get(ws.size() - 2).getAsJsonObject();
                            identifierWS = ws.get(ws.size() - 1).getAsJsonObject();
                        }

                        // Update opening parentheses whitespace.
                        if (this.noHeightAvailable(openingParenWS.get(FormattingConstants.WS).getAsString())) {
                            openingParenWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }

                        // Update type node whitespace.
                        typeFormatConfig = this.getFormattingConfig(1,
                                0,
                                this.getWhiteSpaceCount(indentation),
                                true, this.getWhiteSpaceCount(indentation));
                        typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeFormatConfig);

                        // Update closing parentheses whitespace.
                        if (typeNode.has(FormattingConstants.ANON_TYPE) &&
                                typeNode.getAsJsonObject(FormattingConstants.ANON_TYPE)
                                        .has(FormattingConstants.FIELDS) &&
                                typeNode.getAsJsonObject(FormattingConstants.ANON_TYPE)
                                        .getAsJsonArray(FormattingConstants.FIELDS).size() <= 0) {
                            if (this.noHeightAvailable(closingParentWS.get(FormattingConstants.WS).getAsString())) {
                                closingParentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                                        + indentation + FormattingConstants.NEW_LINE + indentation);
                            }
                        } else {
                            if (this.noHeightAvailable(closingParentWS.get(FormattingConstants.WS).getAsString())) {
                                closingParentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                                        + indentation);
                            }
                        }

                        // Update identifier whitespace.
                        if (this.noHeightAvailable(identifierWS.get(FormattingConstants.WS).getAsString())) {
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
                                if (this.noHeightAvailable(firstKeywordWS.get(FormattingConstants.WS).getAsString())) {
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
                                    false, this.getWhiteSpaceCount(indentation));
                            typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeFormatConfig);

                        } else {
                            typeFormatConfig = this.getFormattingConfig(
                                    formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                    formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                    formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                                    this.getWhiteSpaceCount(indentation));
                            typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeFormatConfig);

                            // Set identifier.
                            identifierWhitespace = firstKeywordWS;
                        }

                        // Update identifier whitespace.
                        if (identifierWhitespace.get(FormattingConstants.TEXT).getAsString().equals("...")) {
                            if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS)
                                    .getAsString())) {
                                identifierWhitespace.addProperty(FormattingConstants.WS,
                                        FormattingConstants.EMPTY_SPACE);
                            }
                        } else {
                            if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS)
                                    .getAsString())) {
                                if (!typeNode.has(FormattingConstants.WS)) {
                                    identifierWhitespace.addProperty(FormattingConstants.WS,
                                            this.getWhiteSpaces(formatConfig
                                                    .get(FormattingConstants.SPACE_COUNT).getAsInt()));
                                } else {
                                    identifierWhitespace.addProperty(FormattingConstants.WS,
                                            FormattingConstants.SINGLE_SPACE);
                                }
                            }
                        }

                        // If semicolon or comma available, update whitespaces including semicolon or comma.
                        // Else update rest of the whitespaces.
                        if (ws.size() > 1) {
                            JsonObject lastWS = ws.get(ws.size() - 1).getAsJsonObject();
                            JsonObject secondToLastWS = ws.get(ws.size() - 2).getAsJsonObject();

                            if (lastWS.get(FormattingConstants.TEXT).getAsString().equals(";") ||
                                    lastWS.get(FormattingConstants.TEXT).getAsString().equals(",")) {
                                if (this.noHeightAvailable(lastWS.get(FormattingConstants.WS).getAsString())) {
                                    lastWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                                }

                                // Update equal symbol whitespace.
                                if (secondToLastWS.get(FormattingConstants.TEXT).getAsString().equals("=") &&
                                        this.noHeightAvailable(secondToLastWS.get(FormattingConstants.WS)
                                                .getAsString())) {
                                    secondToLastWS.addProperty(FormattingConstants.WS,
                                            FormattingConstants.SINGLE_SPACE);
                                }
                            } else if (lastWS.get(FormattingConstants.TEXT).getAsString().equals("=")) {
                                if (this.noHeightAvailable(lastWS.get(FormattingConstants.WS).getAsString())) {
                                    lastWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                                }
                            }
                        }

                        // Update the equal symbol whitespace.
                        if (ws.size() > 1 && ws.get(ws.size() - 1).getAsJsonObject().get(FormattingConstants.TEXT)
                                .getAsString().equals("=")) {
                            JsonObject equalWhitespace = ws.get(ws.size() - 1).getAsJsonObject();
                            if (this.noHeightAvailable(equalWhitespace.get(FormattingConstants.WS).getAsString())) {
                                equalWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }
                        }
                    }
                }

                if (node.has("initialExpression")) {
                    JsonObject initialExprFormattingConfig = this.getFormattingConfig(0, 1,
                            0, false, this.getWhiteSpaceCount(indentWithParentIndentation));
                    node.getAsJsonObject("initialExpression").add(FormattingConstants.FORMATTING_CONFIG,
                            initialExprFormattingConfig);
                }

                if (node.has("annotationAttachments")) {
                    JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                    for (JsonElement annotationAttachment : annotationAttachments) {
                        JsonObject annotationAttachmentFormattingConfig = this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentation), false, this.getWhiteSpaceCount(indentation));
                        annotationAttachment.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG,
                                annotationAttachmentFormattingConfig);
                    }
                }
            } else if (node.has("typeNode")) {
                node.getAsJsonObject("typeNode").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }
        }
    }

    /**
     * format Where node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWhereNode(JsonObject node) {
        // TODO: fix formatting for where node.
        this.skipFormatting(node, true);
    }

    /**
     * format While node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWhileNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                    (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt())
                            + FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, indentation);

            // Update while keyword whitespace.
            JsonObject whileKeywordWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(whileKeywordWS.get(FormattingConstants.WS).getAsString())) {
                whileKeywordWS.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            }

            // Update opening brace whitespace.
            JsonObject openingBraceWS = ws.get(ws.size() - 2).getAsJsonObject();
            if (this.noHeightAvailable(openingBraceWS.get(FormattingConstants.WS).getAsString())) {
                openingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update closing brace whitespace.
            JsonObject closingBraceWS = ws.get(ws.size() - 1).getAsJsonObject();
            modifyBlockBody(node, indentation, closingBraceWS, FormattingConstants.BODY);

            // Update condition whitespace.
            if (node.has("condition")) {
                JsonObject whileCondition = node.getAsJsonObject("condition");
                JsonObject whileConditionFormatConfig = this.getFormattingConfig(0, 1,
                        0, false,
                        this.getWhiteSpaceCount(indentation));
                whileCondition.add(FormattingConstants.FORMATTING_CONFIG, whileConditionFormatConfig);
            }
        }
    }

    /**
     * format Window Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWindowClauseNode(JsonObject node) {
        // TODO: fix formatting for Window clause node.
        this.skipFormatting(node, true);
    }

    /**
     * format Within node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWithinNode(JsonObject node) {
        // TODO: fix formatting for Within node.
        this.skipFormatting(node, true);
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
            if (this.noHeightAvailable(workerKeywordWhitespace.get(FormattingConstants.WS).getAsString())) {
                workerKeywordWhitespace.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                + indentation);
            }

            // Update whitespace for worker identifier.
            JsonObject workerIdentifier = ws.get(ws.size() - 3).getAsJsonObject();
            if (this.noHeightAvailable(workerIdentifier.get(FormattingConstants.WS).getAsString())) {
                workerIdentifier.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespace for worker opening brace.
            JsonObject openingBrace = ws.get(ws.size() - 2).getAsJsonObject();
            if (this.noHeightAvailable(openingBrace.get(FormattingConstants.WS).getAsString())) {
                openingBrace.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Update whitespace for worker closing brace.
            JsonObject closingBrace = ws.get(ws.size() - 1).getAsJsonObject();
            if (node.has(FormattingConstants.BODY)
                    && node.getAsJsonObject(FormattingConstants.BODY).getAsJsonArray(
                    FormattingConstants.STATEMENTS).size() <= 0
                    && node.getAsJsonArray("workers").size() <= 0
                    && node.getAsJsonArray("endpointNodes").size() <= 0) {

                if (this.noHeightAvailable(closingBrace.get(FormattingConstants.WS).getAsString())) {
                    closingBrace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                            + indentation + FormattingConstants.NEW_LINE + indentation);
                }
            } else if (this.noHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject()
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
        // TODO: fix formatting for Worker receiver node.
        this.skipFormatting(node, true);
    }

    /**
     * format Worker Send node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWorkerSendNode(JsonObject node) {
        // TODO: fix formatting for worker send node.
        this.skipFormatting(node, true);
    }

    // --------- Util functions for the modifying node tree --------

    private void modifyReturnTypeAnnotations(JsonObject node, String indentation) {
        if (node.has("returnTypeAnnotationAttachments")) {
            JsonArray returnTypeAnnotations = node.getAsJsonArray("returnTypeAnnotationAttachments");
            iterateAndFormatMembers(indentation, returnTypeAnnotations);
        }
    }

    private void modifyExpressions(JsonObject node, String indentWithParentIndentation) {
        if (node.has("expressions")) {
            JsonArray expressions = node.getAsJsonArray("expressions");
            iterateAndFormatMembers(indentWithParentIndentation, expressions);
        }
    }

    private void modifyBlockBody(JsonObject node, String indentation, JsonObject closingBraceWS, String block) {
        if (node.has(block)
                && node.getAsJsonObject(block).getAsJsonArray(
                FormattingConstants.STATEMENTS).size() <= 0) {
            if (this.noHeightAvailable(closingBraceWS
                    .get(FormattingConstants.WS).getAsString())) {
                closingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation
                        + FormattingConstants.NEW_LINE + indentation);
            }
        } else if (this.noHeightAvailable(closingBraceWS.get(FormattingConstants.WS)
                .getAsString())) {
            closingBraceWS.addProperty(FormattingConstants.WS,
                    FormattingConstants.NEW_LINE + indentation);
        }
    }

    private void modifyBranchingStatement(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentWithParentIndentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                    ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                    FormattingConstants.SPACE_TAB
                    : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update done keyword whitespace.
            JsonObject doneWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(doneWS.get(FormattingConstants.WS).getAsString())) {
                doneWS.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentWithParentIndentation);
            }

            // Update semicolon whitespace.
            JsonObject semicolonWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (this.noHeightAvailable(semicolonWS.get(FormattingConstants.WS).getAsString())) {
                semicolonWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }
        }
    }

    private void modifyAnnotationAttachments(JsonObject node, JsonObject formatConfig, String indentation) {
        if (node.has("annotationAttachments")) {
            JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
            for (int i = 0; i < annotationAttachments.size(); i++) {
                JsonObject annotationAttachment = annotationAttachments.get(i).getAsJsonObject();
                JsonObject annotationFormattingConfig;
                if (i == 0) {
                    annotationFormattingConfig = this.getFormattingConfig(
                            formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(), 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                            formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                            this.getWhiteSpaceCount(indentation));
                } else {
                    annotationFormattingConfig = this.getFormattingConfig(1, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                            formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                            this.getWhiteSpaceCount(indentation));
                }

                annotationAttachment.add(FormattingConstants.FORMATTING_CONFIG, annotationFormattingConfig);
            }
        }
    }

    private void modifyWorkers(JsonObject node, String indentation) {
        if (node.has("workers")) {
            JsonArray workers = node.getAsJsonArray("workers");
            iterateAndFormatBlockStatements(node.getAsJsonObject(FormattingConstants.POSITION), indentation, workers);
        }
    }

    private void modifyEndpoints(JsonObject node, String indentation) {
        if (node.has("endpointNodes")) {
            JsonArray endpointNodes = node.getAsJsonArray("endpointNodes");
            iterateAndFormatBlockStatements(node.getAsJsonObject(FormattingConstants.POSITION),
                    indentation, endpointNodes);
        }
    }

    private void modifyPatternClauses(JsonObject node, String indentation) {
        if (node.has("patternClauses")) {
            JsonArray patternClauses = node.getAsJsonArray("patternClauses");
            for (JsonElement patternClause : patternClauses) {
                JsonObject patternFormatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(indentation), true, this.getWhiteSpaceCount(indentation));
                patternClause.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, patternFormatConfig);
            }
        }
    }

    private void modifyVariableReferences(JsonObject formatConfig, String indentation, JsonArray variableReferences) {
        for (int i = 0; i < variableReferences.size(); i++) {
            JsonObject memberType = variableReferences.get(i).getAsJsonObject();
            JsonObject memberTypeFormatConfig;

            if (i == 0) {
                memberTypeFormatConfig = this.getFormattingConfig(0, 0,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false,
                        this.getWhiteSpaceCount(indentation));
            } else {
                memberTypeFormatConfig = this.getFormattingConfig(0, 1,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false,
                        this.getWhiteSpaceCount(indentation));
            }

            memberType.add(FormattingConstants.FORMATTING_CONFIG, memberTypeFormatConfig);
        }
    }

    private void iterateAndFormatBlockStatements(JsonObject formatConfig, String indentation,
                                                 JsonArray blockStatementNodes) {
        for (int i = 0; i < blockStatementNodes.size(); i++) {
            JsonObject endpointNode = blockStatementNodes.get(i).getAsJsonObject();
            JsonObject endpointFormatConfig;
            if (i == 0) {
                endpointFormatConfig = this.getFormattingConfig(1, 0,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                        this.getWhiteSpaceCount(indentation));
            } else {
                endpointFormatConfig = this.getFormattingConfig(2, 0,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                        this.getWhiteSpaceCount(indentation));
            }

            endpointNode.add(FormattingConstants.FORMATTING_CONFIG, endpointFormatConfig);
        }
    }

    private void iterateAndFormatMembers(String indentation, JsonArray members) {
        for (int i = 0; i < members.size(); i++) {
            JsonObject member = members.get(i).getAsJsonObject();
            JsonObject memberFormatConfig;
            if (i == 0) {
                memberFormatConfig = this.getFormattingConfig(0, 0,
                        0, false, this.getWhiteSpaceCount(indentation));
            } else {
                memberFormatConfig = this.getFormattingConfig(0, 1,
                        0, false, this.getWhiteSpaceCount(indentation));
            }

            member.add(FormattingConstants.FORMATTING_CONFIG, memberFormatConfig);
        }
    }

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
        StringBuilder comment = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            String character = text.charAt(i) + "";
            if (!character.contains("\n")) {
                comment.append(text.charAt(i));
            } else {
                if (!comment.toString().trim().equals("")) {
                    tokens.add(comment.toString().trim());
                    comment = new StringBuilder();
                }
                tokens.add(character);
            }

            if (i == (text.length() - 1) && !comment.toString().trim().equals("")) {
                tokens.add(comment.toString().trim());
                comment = new StringBuilder();
            }
        }
        return tokens;
    }

    private String getTextFromTokens(List<String> tokens, String indent) {
        StringBuilder text = new StringBuilder();
        for (String token : tokens) {
            if (!token.contains("\n")) {
                text.append(indent != null ? indent + token : token);
            } else {
                text.append(token);
            }
        }

        return indent != null ? (text + indent) : text.toString();
    }

    private void preserveHeight(JsonArray ws, String indent) {
        for (int i = 0; i < ws.size(); i++) {
            if (ws.get(i).isJsonObject()) {
                preserveHeightForWS(ws.get(i).getAsJsonObject(), indent);
            }
        }
    }

    private void preserveHeightForWS(JsonObject ws, String indent) {
        if (ws.has(FormattingConstants.WS) &&
                (ws.get(FormattingConstants.WS).getAsString().trim().length() > 0 ||
                        ws.get(FormattingConstants.WS).getAsString().contains("\n"))) {
            List<String> tokens = this.tokenizer(ws.get(FormattingConstants.WS).getAsString());
            ws.addProperty(FormattingConstants.WS,
                    this.getTextFromTokens(tokens, indent));
        }
    }

    private boolean noHeightAvailable(String ws) {
        return ws.trim().length() <= 0 && !ws.contains("\n");
    }

    private boolean noNewLine(String text) {
        return !text.contains("\n");
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

    private JsonObject getFormattingConfig(int newLineCount, int spacesCount, int startColumn, boolean doIndent,
                                           int indentedStartColumn) {
        JsonObject formattingConfig = new JsonObject();
        formattingConfig.addProperty(FormattingConstants.NEW_LINE_COUNT, newLineCount);
        formattingConfig.addProperty(FormattingConstants.SPACE_COUNT, spacesCount);
        formattingConfig.addProperty(FormattingConstants.START_COLUMN, startColumn);
        formattingConfig.addProperty(FormattingConstants.DO_INDENT, doIndent);
        formattingConfig.addProperty(FormattingConstants.INDENTED_START_COLUMN, indentedStartColumn);
        return formattingConfig;
    }
}
