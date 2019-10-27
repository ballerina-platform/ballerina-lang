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
package org.ballerinalang.langserver.compiler.format;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;

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
            boolean isPublic = node.has(Tokens.PUBLIC)
                    && node.get(Tokens.PUBLIC).getAsBoolean();
            boolean isConst = node.has("constant") && node.get("constant").getAsBoolean();

            String indentation = this.getIndentation(formatConfig, false);

            this.preserveHeight(ws, indentation);

            // Iterate and update whitespaces of the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (text.equals(Tokens.PUBLIC)) {
                        // If annotation attachments exists add only one new line.
                        // Else add given number of new lines.
                        String whiteSpace = ((node.has("annotationAttachments") &&
                                node.getAsJsonArray("annotationAttachments").size() > 0))
                                ? (FormattingConstants.NEW_LINE + indentation)
                                : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
                        currentWS.addProperty(FormattingConstants.WS, whiteSpace);
                    } else if (text.equals(Tokens.CONST)) {
                        if (!isPublic) {
                            // If annotation attachments exists add only one new line.
                            // Else add given number of new lines.
                            String whiteSpace = ((node.has("annotationAttachments") &&
                                    node.getAsJsonArray("annotationAttachments").size() > 0))
                                    ? (FormattingConstants.NEW_LINE + indentation)
                                    : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                    .getAsInt()) + indentation);
                            currentWS.addProperty(FormattingConstants.WS, whiteSpace);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    } else if (text.equals(Tokens.ANNOTATION)) {
                        if (!isPublic && !isConst) {
                            // If annotation attachments exists add only one new line.
                            // Else add given number of new lines.
                            String whiteSpace = ((node.has("annotationAttachments") &&
                                    node.getAsJsonArray("annotationAttachments").size() > 0))
                                    ? (FormattingConstants.NEW_LINE + indentation)
                                    : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                    .getAsInt()) + indentation);
                            currentWS.addProperty(FormattingConstants.WS, whiteSpace);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    } else if (text.equals(Tokens.SEMICOLON) || text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Update whitespaces for type node.
            if (node.has(FormattingConstants.TYPE_NODE)) {
                JsonObject typeNode = node.getAsJsonObject(FormattingConstants.TYPE_NODE);
                JsonObject typeNodeFormattingConfig = this.getFormattingConfig(0, 1,
                        0, false, this.getWhiteSpaceCount(indentation), false);
                typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeNodeFormattingConfig);
            }

            // Update annotation attachment formatting.
            modifyAnnotationAttachments(node, formatConfig, indentation);
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
            String indentation = this.getIndentation(formatConfig, false);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve any comments or new lines that already available.
            this.preserveHeight(node.getAsJsonArray(FormattingConstants.WS), useParentIndentation
                    ? indentWithParentIndentation : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.AT)) {
                        if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                            indentWithParentIndentation);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        }
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Update whitespace for expression.
            if (node.has(FormattingConstants.EXPRESSION) && node.getAsJsonObject(FormattingConstants.EXPRESSION)
                    .has(FormattingConstants.WS)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1,
                        this.getWhiteSpaceCount(indentation), false,
                        this.getWhiteSpaceCount(indentWithParentIndentation), false);
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }
        }
    }

    /**
     * format annotation access expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatAnnotAccessExpressionNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve already available new lines.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and update whitespaces in the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            }

            // Handle whitespaces for the expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION)
                        .add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
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
            String indentation = this.getIndentation(formatConfig, false);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update opening bracket whitespace.
            JsonObject openingBracketWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(openingBracketWS.get(FormattingConstants.WS).getAsString())) {
                openingBracketWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                        .get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                        + this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt())
                        + indentation);
            }

            // Update whitespace for the separator.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                String text = wsItem.get(FormattingConstants.TEXT).getAsString();
                if (text.equals(Tokens.COMMA)
                        && this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
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
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);
            String indentation = this.getIndentation(formatConfig, false);

            // Update whitespace for element type.
            if (node.has("elementType")) {
                if (node.has(FormattingConstants.GROUPED) && node.get(FormattingConstants.GROUPED).getAsBoolean()) {
                    JsonObject elementTypeFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                            this.getWhiteSpaceCount(indentWithParentIndentation), false);
                    node.getAsJsonObject("elementType").add(FormattingConstants.FORMATTING_CONFIG,
                            elementTypeFormatConfig);
                } else {
                    node.getAsJsonObject("elementType").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
                }
            }

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                        ? indentWithParentIndentation : indentation);

                for (int i = 0; i < ws.size(); i++) {
                    JsonObject wsItem = ws.get(i).getAsJsonObject();
                    if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                        String text = wsItem.get(FormattingConstants.TEXT).getAsString();
                        if (text.equals(Tokens.OPENING_PARENTHESES) && i == 0) {
                            // Update grouped opening parentheses whitespace.
                            if (node.has(FormattingConstants.GROUPED) &&
                                    node.get(FormattingConstants.GROUPED).getAsBoolean()) {
                                wsItem.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                        .get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                        .getAsInt()) + indentation);
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
            String indentation = this.getIndentation(formatConfig, false);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

            // Preserve comments and new lines that already available.
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
                if (node.has(FormattingConstants.VARIABLE)) {
                    JsonObject variable = node.getAsJsonObject(FormattingConstants.VARIABLE);
                    JsonObject variableFormatConfig = this.getFormattingConfig(0, 1,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false,
                            this.getWhiteSpaceCount(indentation), false);
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
                if (node.has(FormattingConstants.VARIABLE)) {
                    JsonObject variable = node.getAsJsonObject(FormattingConstants.VARIABLE);
                    JsonObject variableFormatConfig =
                            this.getFormattingConfig(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                    0, formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                    formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                                    this.getWhiteSpaceCount(indentation), false);
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
            if (node.has(FormattingConstants.EXPRESSION) && node.getAsJsonObject(FormattingConstants.EXPRESSION)
                    .has(FormattingConstants.WS)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1,
                        0, false,
                        this.getWhiteSpaceCount(indentation), true);
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
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

            // Preserve line separations that already available.
            this.preserveHeight(ws, indentWithParentIndentation);

            for (JsonElement wsItem : ws) {
                JsonObject arrowExprWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(arrowExprWS.get(FormattingConstants.WS).getAsString())) {
                    String text = arrowExprWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        arrowExprWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }

                    if (text.equals(Tokens.COMMA)) {
                        arrowExprWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    if (text.equals(Tokens.CLOSING_PARENTHESES)) {
                        arrowExprWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    if (text.equals(Tokens.EQUAL_GT)) {
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
                                    0, false, this.getWhiteSpaceCount(indentWithParentIndentation),
                                    false);
                        } else {
                            paramFormatConfig = this.getFormattingConfig(0,
                                    formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                    0, false, this.getWhiteSpaceCount(indentWithParentIndentation),
                                    false);
                        }
                    } else {
                        paramFormatConfig = this.getFormattingConfig(0, 1,
                                0, false, this.getWhiteSpaceCount(indentWithParentIndentation),
                                false);
                    }

                    param.add(FormattingConstants.FORMATTING_CONFIG, paramFormatConfig);
                }
            }

            // Update whitespace of expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentWithParentIndentation), false);
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }
        }
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
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

            // Preserve line separations that already available.
            this.preserveHeight(ws, indentWithParentIndentation);

            // Update the operator symbol whitespace.
            JsonObject operatorSymbolWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(operatorSymbolWS.get(FormattingConstants.WS).getAsString())) {
                operatorSymbolWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
            }

            // Handle left expression whitespaces.
            if (node.has("leftExpression")) {
                node.getAsJsonObject("leftExpression").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
                node.getAsJsonObject("leftExpression").addProperty(FormattingConstants.IS_EXPRESSION, true);
            }

            // Handle right expression whitespaces.
            if (node.has("rightExpression")) {
                JsonObject rightExpression = node.getAsJsonObject("rightExpression");
                JsonObject rightExprFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentWithParentIndentation), true);
                rightExpression.add(FormattingConstants.FORMATTING_CONFIG, rightExprFormatConfig);
                rightExpression.addProperty(FormattingConstants.IS_EXPRESSION, true);
            }
        }
    }

    /**
     * format Block node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatBlockNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Update the statements whitespaces.
            for (JsonElement child : node.getAsJsonArray(FormattingConstants.STATEMENTS)) {
                child.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // If this is a else block continue to following.
            if (node.has(FormattingConstants.WS) && node.getAsJsonArray(FormattingConstants.WS).get(0).getAsJsonObject()
                    .get(FormattingConstants.TEXT).getAsString().equals(Tokens.ELSE)) {

                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                // Whitespaces for else block should indent as to the parent's start column.
                String indentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());

                // Preserve available line breaks.
                this.preserveHeight(ws, indentation);

                // Iterate and format whitespaces for else node.
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                        if (text.equals(Tokens.ELSE)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else if (text.equals(Tokens.OPENING_BRACE)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else if (text.equals(Tokens.CLOSING_BRACE)) {
                            if (node.getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                        indentation + FormattingConstants.NEW_LINE + indentation);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                        indentation);
                            }
                        }
                    }
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Get the indentation for the node.
            String indentation = this.getIndentation(formatConfig, true);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                        indentation);
                    } else if (text.equals(Tokens.CLOSING_PARENTHESES) || text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Update expressions' whitespaces.
            modifyExpressions(node, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);
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
            String indentation = this.getIndentation(formatConfig, true);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve the line separators that already available.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Update the ref type whitespace.
            JsonObject refTypeWhitespace = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(refTypeWhitespace.get(FormattingConstants.WS).getAsString())) {
                refTypeWhitespace.addProperty(FormattingConstants.WS,
                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                indentation);
            }
        }
    }

    /**
     * format Check Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatCheckExprNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG) && node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            boolean isExpression = node.has(FormattingConstants.IS_EXPRESSION)
                    && node.get(FormattingConstants.IS_EXPRESSION).getAsBoolean();

            // Get the indentation for the node.
            String indentation = this.getIndentation(formatConfig, false);

            // Get the indentation for the node.
            String indentationWithParent = this.getParentIndentation(formatConfig);

            if (isExpression) {
                this.preserveHeight(ws, indentationWithParent);
            } else {
                this.preserveHeight(ws, indentation);
            }

            // Update whitespaces for check.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.CHECK)) {
                        if (isExpression) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                    .get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) + indentation);
                        }
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Handle whitespace for expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(isExpression ? indentationWithParent : indentation),
                                false));
            }
        }
    }

    /**
     * format Check Panic node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatCheckPanicExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            boolean isExpression = node.has(FormattingConstants.IS_EXPRESSION)
                    && node.get(FormattingConstants.IS_EXPRESSION).getAsBoolean();

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and update whitespaces for the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.CHECK_PANIC)) {
                        if (isExpression) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                            .getAsInt()));
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                            + indentation);
                        }
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Handle expression whitespaces.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation),
                                true));
            }
        }
    }

    /**
     * format Compilation Unit node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatCompilationUnitNode(JsonObject node) {
        // Update whitespaces for top level nodes.
        JsonArray topLevelNodes = node.get("topLevelNodes").getAsJsonArray();

        // Handle import sorting according to the alphabetical order.
        int i, j;
        boolean swapped;
        for (i = 0; i < topLevelNodes.size() - 1; i++) {
            swapped = false;
            for (j = 0; j < topLevelNodes.size() - i - 1; j++) {
                if ((topLevelNodes.get(j).getAsJsonObject()
                        .get(FormattingConstants.KIND).getAsString().equals("Import")
                        && topLevelNodes.get(j).getAsJsonObject().has(FormattingConstants.WS))
                        && (topLevelNodes.get(j + 1).getAsJsonObject()
                        .get(FormattingConstants.KIND).getAsString().equals("Import")
                        && topLevelNodes.get(j + 1).getAsJsonObject().has(FormattingConstants.WS))) {
                    String refImportName = topLevelNodes.get(j).getAsJsonObject()
                            .get("orgName").getAsJsonObject().get(FormattingConstants.VALUE).getAsString() + "/"
                            + topLevelNodes.get(j).getAsJsonObject().get("packageName")
                            .getAsJsonArray().get(0).getAsJsonObject().get(FormattingConstants.VALUE).getAsString();

                    String compImportName = topLevelNodes.get(j + 1).getAsJsonObject()
                            .get("orgName").getAsJsonObject().get(FormattingConstants.VALUE).getAsString() + "/"
                            + topLevelNodes.get(j + 1).getAsJsonObject().get("packageName")
                            .getAsJsonArray().get(0).getAsJsonObject().get(FormattingConstants.VALUE).getAsString();

                    int comparisonResult = refImportName.compareTo(compImportName);
                    // Swap if the comparison value is positive.
                    if (comparisonResult > 0) {
                        // Swap ws to keep the formatting in level.
                        String refWS = topLevelNodes.get(j).getAsJsonObject().get(FormattingConstants.WS)
                                .getAsJsonArray().get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString();

                        String compWS = topLevelNodes.get(j + 1).getAsJsonObject().get(FormattingConstants.WS)
                                .getAsJsonArray().get(0).getAsJsonObject().get(FormattingConstants.WS).getAsString();

                        JsonElement tempLowNode = topLevelNodes.get(j);
                        JsonElement tempTopNode = topLevelNodes.get(j + 1);

                        // Swap whitespaces of the nodes.
                        FormattingSourceGen.swapWSIndexes(tempTopNode.getAsJsonObject(),
                                tempLowNode.getAsJsonObject());

                        tempLowNode.getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray().get(0)
                                .getAsJsonObject().addProperty(FormattingConstants.WS, compWS);

                        tempTopNode.getAsJsonObject().get(FormattingConstants.WS).getAsJsonArray()
                                .get(0).getAsJsonObject().addProperty(FormattingConstants.WS, refWS);

                        topLevelNodes.set(j, tempTopNode);
                        topLevelNodes.set(j + 1, tempLowNode);

                        swapped = true;
                    }
                }
            }
            // If not swapped, break.
            if (!swapped) {
                break;
            }
        }

        int movedFirstIndex = 0;
        for (int index = 0; index < topLevelNodes.size(); index++) {
            JsonObject child = topLevelNodes.get(index).getAsJsonObject();
            JsonObject formatConfig;

            // Skip any anon types available in the top level before picking the first top level block.
            movedFirstIndex = child.has(FormattingConstants.WS) && !child.has("skip")
                    ? movedFirstIndex : ++movedFirstIndex;

            if (index == movedFirstIndex) {
                formatConfig = this.getFormattingConfig(0, 0,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false);
                movedFirstIndex = 0;
            } else if (child.has(FormattingConstants.KIND) &&
                    child.get(FormattingConstants.KIND).getAsString().equals("Import")) {
                formatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false);
            } else {
                formatConfig = this.getFormattingConfig(2, 0,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false,
                        this.getWhiteSpaceCount(FormattingConstants.EMPTY_SPACE), false);
            }
            child.add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
        }

        // Update EOF whitespace.
        if (node.has(FormattingConstants.WS) && topLevelNodes.size() > 0) {
            JsonArray ws = node.get(FormattingConstants.WS).getAsJsonArray();

            // preserve comment available before EOF.
            this.preserveHeight(ws, null);

            // Handle adding a new line at the EOF.
            JsonObject eofWS = ws.get(ws.size() - 1).getAsJsonObject();
            if (this.noHeightAvailable(eofWS.get(FormattingConstants.WS).getAsString())) {
                eofWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE);
            } else if (this.noNewLine(eofWS.get(FormattingConstants.WS).getAsString()
                    .charAt(eofWS.get(FormattingConstants.WS).getAsString().length() - 1) + "")) {
                eofWS.addProperty(FormattingConstants.WS, (eofWS.get(FormattingConstants.WS).getAsString() +
                        FormattingConstants.NEW_LINE));
            }
        }
    }

    /**
     * format Compound Assignment node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatCompoundAssignmentNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String compoundOperator = node.get("compoundOperator").getAsString();
            String indentation = this.getIndentation(formatConfig, false);

            // Preserve line separations that already available.
            this.preserveHeight(ws, indentation);

            // Iterate and format compound assignment whitespaces.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(compoundOperator)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.SEMICOLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle variable whitespaces.
            if (node.has(FormattingConstants.VARIABLE)) {
                node.getAsJsonObject(FormattingConstants.VARIABLE).add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            // Handle expression whitespaces.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0,
                                false, this.getWhiteSpaceCount(indentation), false));
            }
        }
    }

    /**
     * format constant node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatConstantNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            boolean isPublic = node.has(Tokens.PUBLIC)
                    && node.get(Tokens.PUBLIC).getAsBoolean();

            // Preserve line separators that already available.
            this.preserveHeight(ws, indentation);

            // Iterate and update whitespaces for constant node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.PUBLIC)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.CONST)) {
                        if (isPublic) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                            + indentation);
                        }
                    } else if (text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.SEMICOLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            modifyAnnotationAttachments(node, formatConfig, indentation);

            if (node.has(FormattingConstants.TYPE_NODE)) {
                node.getAsJsonObject(FormattingConstants.TYPE_NODE).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentation), false));
            }

            if (node.has("initialExpression")) {
                node.getAsJsonObject("initialExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentation), false));
            }
        }
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
            String indentation = this.getIndentation(formatConfig, true);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean isGrouped = node.has("grouped") && node.get("grouped").getAsBoolean();

            // Preserve line separations that already available.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Update whitespace for type node.
            if (node.has(FormattingConstants.TYPE)) {
                JsonObject typeFormatConfig = formatConfig;
                if (isGrouped) {
                    typeFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                            this.getWhiteSpaceCount(formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                    .getAsBoolean() ? indentationOfParent : indentation),
                            formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean());
                }
                node.getAsJsonObject(FormattingConstants.TYPE).add(FormattingConstants.FORMATTING_CONFIG,
                        typeFormatConfig);
            }

            // Update whitespace for constraint.
            if (node.has("constraint")) {
                JsonObject constraintFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                        this.getWhiteSpaceCount(formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                .getAsBoolean() ? indentationOfParent : indentation), true);
                node.getAsJsonObject("constraint").add(FormattingConstants.FORMATTING_CONFIG, constraintFormatConfig);
            }

            // Update whitespace for open constraint symbol.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    String text = wsItem.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_PARENTHESES) && isGrouped) {
                        if (formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt() > 0) {
                            wsItem.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        } else if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                            wsItem.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                            + indentation);
                        } else {
                            wsItem.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    } else if (text.equals(Tokens.LESS_THAN) || text.equals(Tokens.GREATER_THAN)
                            || text.equals(Tokens.CLOSING_BRACE) || text.equals(Tokens.OBJECT)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.TYPEDESC)) {
                        wsItem.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    } else if (text.equals(Tokens.CLOSING_PARENTHESES) && isGrouped) {
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
     * format documentation parameter node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatDocumentationParameterNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Calculate indentation for the documentation parameter.
            String indentation = this.getParentIndentation(formatConfig);

            // Save new lines of a whitespace if available.
            this.preserveHeight(ws, indentation);

            // Iterate through whitespaces and update accordingly.
            for (int i = 0; i < ws.size(); i++) {
                JsonObject currentWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    String[] splitText = text.split(" ");

                    // If text equals `#` or has `#` and `+` in order or has `#`, `+`, `return` and `-`
                    // Change the whitespace for the current whitespace.
                    if (text.equals("#")
                            || (splitText.length == 2 && splitText[0].equals("#") && splitText[1].equals("+"))
                            || (splitText.length == 4 && splitText[0].equals("#")
                            && splitText[1].equals("+") && splitText[2].equals(Tokens.RETURN)
                            && splitText[3].equals("-"))) {
                        currentWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                .get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) + indentation);
                    }
                }
            }
        }
    }

    /**
     * format markdown documentation node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMarkdownDocumentationNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Calculate indentation for the documentation parameter.
            String indentation = this.getParentIndentation(formatConfig);

            // Save new lines of a whitespace if available.
            this.preserveHeight(ws, indentation);

            // Iterate through whitespaces and update accordingly.
            for (int i = 0; i < ws.size(); i++) {
                JsonObject currentWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals("#")) {
                        currentWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                .get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) + indentation);
                    }
                }
            }

            if (node.has("parameters")
                    && node.getAsJsonArray("parameters").size() > 0) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                for (JsonElement parameter : parameters) {
                    parameter.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
                }
            }

            if (node.has("returnParameter")) {
                JsonObject returnParameter = node.getAsJsonObject("returnParameter");
                returnParameter.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }
        }
    }

    /**
     * format Elvis Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatElvisExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, indentationOfParent);

            // Iterate and update whitespaces for elvis.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.ELVIS)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Handle left hand side expression whitespaces.
            if (node.has("leftExpression")) {
                node.getAsJsonObject("leftExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                                formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt(),
                                true));
            }

            // Handle right hand side expression whitespaces.
            if (node.has("rightExpression")) {
                node.getAsJsonObject("rightExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0,
                                false, this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format Error Constructor node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatErrorConstructorNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve already available line separators.
            this.preserveHeight(ws, indentationOfParent);

            // Iterate available ws items in error constructor.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.ERROR)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    } else if (text.equals(Tokens.OPENING_PARENTHESES) || text.equals(Tokens.COMMA)
                            || text.equals(Tokens.CLOSING_PARENTHESES)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle reason expression whitespaces.
            if (node.has("reasonExpression")) {
                node.getAsJsonObject("reasonExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            // Handle details expression whitespaces.
            if (node.has("detailsExpression")) {
                node.getAsJsonObject("detailsExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format Error Destructure node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatErrorDestructureNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and update whitespaces for error destructure node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.SEMICOLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle error variable reference formatting.
            if (node.has("varRef")) {
                node.getAsJsonObject("varRef").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // Handle expression formatting.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentation), true));
            }
        }
    }

    /**
     * format Error type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatErrorTypeNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve comments and new line that already available.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.ERROR)) {
                        if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                            + indentation);
                        } else if (formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt() > 0) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    } else if (text.equals(Tokens.LESS_THAN) || text.equals(Tokens.COMMA)
                            || text.equals(Tokens.GREATER_THAN) || text.equals(Tokens.QUESTION_MARK)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle formatting for reason type node.
            if (node.has("reasonTypeNode")) {
                node.getAsJsonObject("reasonTypeNode").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                        .getAsBoolean() ? indentationOfParent : indentation), true));
            }

            // Handle formatting for details type node.
            if (node.has("detailsTypeNode")) {
                node.getAsJsonObject("detailsTypeNode").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                        .getAsBoolean() ? indentationOfParent : indentation), true));
            }
        }
    }

    /**
     * format Error Variable node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatErrorVariableNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);


            // Iterate and update whitespaces for error variable node.
            boolean reasonWSUpdated = false;
            boolean isEllipsisAvailable = false;
            boolean isVarAvailable = false;
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (text.equals(Tokens.VAR)) {
                        if (!isEllipsisAvailable) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                            + indentation);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                        isVarAvailable = true;
                    } else if (text.equals(Tokens.ERROR)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        isEllipsisAvailable = false;
                    } else if (text.equals(Tokens.OPENING_PARENTHESES) || text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        isEllipsisAvailable = false;
                    } else if (text.equals(Tokens.ELLIPSIS)) {
                        isEllipsisAvailable = true;
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.COMMA) || text.equals(Tokens.CLOSING_PARENTHESES)
                            || text.equals(Tokens.SEMICOLON)) {
                        isEllipsisAvailable = false;
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else {
                        if (reasonWSUpdated) {
                            if (isEllipsisAvailable && isVarAvailable) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            } else if (isEllipsisAvailable) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }
                        } else {
                            if (isEllipsisAvailable && isVarAvailable) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            } else if (isEllipsisAvailable) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                                reasonWSUpdated = true;
                            }
                        }
                        isEllipsisAvailable = false;
                    }
                } else {
                    if (text.equals(Tokens.VAR) && !isEllipsisAvailable) {
                        isVarAvailable = true;
                    } else if (text.equals(Tokens.ELLIPSIS)) {
                        isEllipsisAvailable = true;
                    } else {
                        isEllipsisAvailable = false;
                    }
                }
            }

            // Handle type node formatting
            if (node.has(FormattingConstants.TYPE_NODE)) {
                if (isVarAvailable) {
                    node.getAsJsonObject(FormattingConstants.TYPE_NODE).add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 1, 0, false,
                                    this.getWhiteSpaceCount(formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                            .getAsBoolean() ? indentationOfParent : indentation), true));
                } else {
                    node.getAsJsonObject(FormattingConstants.TYPE_NODE).add(FormattingConstants.FORMATTING_CONFIG,
                            formatConfig);
                }
            }

            if (node.has("reason")) {
                node.getAsJsonObject("reason").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                        .getAsBoolean() ? indentationOfParent : indentation), true));
            }

            // Handle detail node formatting
            if (node.has("detail")) {
                boolean noReason = !node.has("reason") || (node.has("reason")
                        && !isReasonAvailable(node.getAsJsonObject("reason")));
                JsonArray details = node.getAsJsonArray("detail");
                for (int i = 0; i < details.size(); i++) {
                    JsonObject detail = details.get(i).getAsJsonObject();
                    if (i == 0 && noReason) {
                        detail.add(FormattingConstants.FORMATTING_CONFIG,
                                this.getFormattingConfig(0, 0, 0, false,
                                        this.getWhiteSpaceCount(formatConfig
                                                .get(FormattingConstants.USE_PARENT_INDENTATION)
                                                .getAsBoolean() ? indentationOfParent : indentation), true));
                    } else {
                        detail.add(FormattingConstants.FORMATTING_CONFIG,
                                this.getFormattingConfig(0, 1, 0, false,
                                        this.getWhiteSpaceCount(formatConfig
                                                .get(FormattingConstants.USE_PARENT_INDENTATION)
                                                .getAsBoolean() ? indentationOfParent : indentation), true));
                    }
                }
            }

            // Handle initial expression's formatting
            if (node.has("initialExpression")) {
                node.getAsJsonObject("initialExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                        .getAsBoolean() ? indentationOfParent : indentation), true));
            }
        }
    }

    /**
     * format Error Variable Ref node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatErrorVariableRefNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and update whitespaces for error variable reference.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.ERROR)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.OPENING_PARENTHESES) || text.equals(Tokens.ELLIPSIS)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.COMMA) || text.equals(Tokens.CLOSING_PARENTHESES)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            if (node.has(FormattingConstants.TYPE_NODE)) {
                node.getAsJsonObject(FormattingConstants.TYPE_NODE).add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            // Handle whitespaces for reason.
            if (node.has("reason")) {
                node.getAsJsonObject("reason").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentation), true));
            }

            // Handle whitespaces for detail.
            if (node.has("detail")) {
                boolean noReason = !node.has("reason") || (node.has("reason")
                        && !isReasonAvailable(node.getAsJsonObject("reason")));
                JsonArray details = node.getAsJsonArray("detail");
                for (int i = 0; i < details.size(); i++) {
                    JsonObject detail = details.get(i).getAsJsonObject();
                    if (i == 0 && noReason) {
                        detail.add(FormattingConstants.FORMATTING_CONFIG,
                                this.getFormattingConfig(0, 0, 0, false,
                                        this.getWhiteSpaceCount(indentation), true));
                    } else {
                        detail.add(FormattingConstants.FORMATTING_CONFIG,
                                this.getFormattingConfig(0, 1, 0, false,
                                        this.getWhiteSpaceCount(indentation), true));
                    }
                }
            }

            if (node.has("restVar")) {
                node.getAsJsonObject("restVar").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentation), true));
            }
        }
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
            String indentation = this.getIndentation(formatConfig, false);

            // Preserve comments and new lines that already available.
            this.preserveHeight(ws, indentation);

            // Update whitespaces for expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
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
            String indentation = this.getIndentation(formatConfig, false);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

            // Preserve line separations that already available.
            this.preserveHeight(ws, indentWithParentIndentation);

            // Update the expression whitespaces.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);

                if (node.has(FormattingConstants.IS_EXPRESSION) && node.get(FormattingConstants.IS_EXPRESSION)
                        .getAsBoolean()) {
                    expression.addProperty(FormattingConstants.IS_EXPRESSION, true);
                }

                JsonObject expressionFormatConfig = this.getFormattingConfig(formatConfig
                                .get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                        formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                        formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                        this.getWhiteSpaceCount(indentation.isEmpty() ? indentWithParentIndentation : indentation),
                        formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean());

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
            String indentation = this.getIndentation(formatConfig, false);

            // Preserve line separation that already available.
            this.preserveHeight(ws, indentation);

            // Update whitespace
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
                            false, this.getWhiteSpaceCount(indentation), false);
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
            String indentation = this.getIndentation(formatConfig, false);

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
                    if (text.equals(Tokens.FOREACH)) {
                        wsItem.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentWithParentIndentation);
                    } else if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.COMMA)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.IN)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.CLOSING_PARENTHESES)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.OPENING_BRACE)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.CLOSING_BRACE)) {
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
                    } else {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
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
                                this.getWhiteSpaceCount(indentWithParentIndentation), false);
                    } else {
                        variableFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentWithParentIndentation), false);
                    }

                    variable.add(FormattingConstants.FORMATTING_CONFIG, variableFormatConfig);
                }
            }

            // Handle whitespace for collection.
            if (node.has("collection")) {
                JsonObject collection = node.getAsJsonObject("collection");
                JsonObject collectionFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentWithParentIndentation), false);
                collection.add(FormattingConstants.FORMATTING_CONFIG, collectionFormatConfig);
            }

            if (node.has("variableDefinitionNode")) {
                node.getAsJsonObject("variableDefinitionNode").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentWithParentIndentation), true));
            }

            if (node.has(FormattingConstants.BODY)) {
                modifyConstructBody(node.getAsJsonObject(FormattingConstants.BODY),
                        indentation, indentWithParentIndentation);
            }
        }
    }

    /**
     * format Forever node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatForeverNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);

            // Preserve available line separators.
            this.preserveHeight(ws, indentation);

            // Update whitespaces for forever node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.FOREVER)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.OPENING_BRACE)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.CLOSING_BRACE)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                FormattingConstants.NEW_LINE + indentation);
                    }
                }
            }

            // Handle streaming query statements' whitespaces
            if (node.has("streamingQueryStatements")) {
                JsonArray queryStmts = node.getAsJsonArray("streamingQueryStatements");
                for (JsonElement queryStmtItem : queryStmts) {
                    JsonObject queryStmtFormatConfig = this.getFormattingConfig(1, 0,
                            this.getWhiteSpaceCount(indentation), true, this.getWhiteSpaceCount(indentation),
                            true);
                    queryStmtItem.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, queryStmtFormatConfig);
                }
            }
        }
    }

    /**
     * format Fork Join node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatForkJoinNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve available new lines.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and update whitespaces for fork node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.FORK)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.OPENING_BRACE)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.CLOSING_BRACE)) {
                        if (node.has(FormattingConstants.WORKERS)
                                && node.getAsJsonArray(FormattingConstants.WORKERS).size() <= 0) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
                        }
                    }
                }
            }

            // Handle worker formatting in fork.
            if (node.has(FormattingConstants.WORKERS)) {
                JsonArray workers = node.getAsJsonArray(FormattingConstants.WORKERS);
                for (JsonElement workerItem : workers) {
                    workerItem.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(1, 0, this.getWhiteSpaceCount(indentation),
                                    true, this.getWhiteSpaceCount(indentation), false));
                }
            }
        }
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
            boolean isWorker = node.has("worker") && node.get("worker").getAsBoolean();
            String indentation = this.getIndentation(formatConfig, false);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);
            String functionName = "";
            if (node.has(FormattingConstants.NAME)) {
                functionName = node.getAsJsonObject(FormattingConstants.NAME)
                        .get(FormattingConstants.VALUE).getAsString();
            }

            // Update the function node's start column.
            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentWithParentIndentation));

            // Preserve the new lines and characters available in node's whitespaces.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentWithParentIndentation : indentation);

            boolean differentFirstKeyword = false;
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if (text.equals(Tokens.PUBLIC) || text.equals(Tokens.PRIVATE) || text.equals(Tokens.REMOTE)
                        || text.equals(Tokens.WORKER) || text.equals(Tokens.RESOURCE)) {
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        if (differentFirstKeyword) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else {
                            // If annotation or documentation attachments exists add only one new line.
                            // Else add given number of new lines.
                            String whiteSpace = ((node.has("annotationAttachments") &&
                                    node.getAsJsonArray("annotationAttachments").size() > 0) ||
                                    node.has("markdownDocumentationAttachment") ||
                                    (node.has("deprecatedAttachments") &&
                                            node.getAsJsonArray("deprecatedAttachments").size() > 0))
                                    ? (FormattingConstants.NEW_LINE + indentation)
                                    : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                    + indentation);

                            currentWS.addProperty(FormattingConstants.WS, whiteSpace);
                        }
                    }
                    differentFirstKeyword = true;
                }
            }

            for (int i = 0; i < ws.size(); i++) {
                JsonObject functionWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(functionWS.get(FormattingConstants.WS).getAsString())) {
                    String wsText = functionWS.get(FormattingConstants.TEXT).getAsString();

                    if (wsText.equals(Tokens.FUNCTION)) {
                        // If function is a lambda and not a worker, add spaces.
                        if (isLambda && !isWorker) {
                            functionWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        } else if (differentFirstKeyword) {
                            functionWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else {
                            // If annotation or documentation attachments exists add only one new line.
                            // Else add given number of new lines.
                            String whiteSpace = ((node.has("annotationAttachments") &&
                                    node.getAsJsonArray("annotationAttachments").size() > 0) ||
                                    node.has("markdownDocumentationAttachment") ||
                                    (node.has("deprecatedAttachments") &&
                                            node.getAsJsonArray("deprecatedAttachments").size() > 0))
                                    ? (FormattingConstants.NEW_LINE + indentation)
                                    : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                    + indentation);

                            functionWS.addProperty(FormattingConstants.WS, whiteSpace);
                        }
                    } else if (wsText.equals(functionName)) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (wsText.equals(Tokens.OPENING_PARENTHESES)) {
                        if (!isLambda) {
                            functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    } else if (wsText.equals(Tokens.COMMA)) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (wsText.equals(Tokens.EQUAL_GT)) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (wsText.equals(Tokens.CLOSING_PARENTHESES)) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (wsText.equals(Tokens.RETURNS)) {
                        // Update whitespace for returns keyword.
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (wsText.equals(Tokens.OPENING_BRACE)) {
                        // Update whitespaces for the opening brace.
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (wsText.equals(Tokens.CLOSING_BRACE)) {
                        // Update whitespaces for closing brace of the function.
                        if (node.has(FormattingConstants.BODY)
                                && node.getAsJsonObject(FormattingConstants.BODY)
                                .getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0
                                && node.getAsJsonArray("endpointNodes").size() <= 0
                                && node.getAsJsonArray("workers").size() <= 0) {

                            if (this.noHeightAvailable(functionWS.get(FormattingConstants.WS).getAsString())) {
                                functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            }
                        } else if (this.noHeightAvailable(functionWS.get(FormattingConstants.WS).getAsString())) {
                            functionWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE +
                                    indentWithParentIndentation);
                        }
                    } else if (wsText.equals(Tokens.SEMICOLON)) {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else {
                        functionWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Update whitespaces of parameters.
            if (node.has("parameters")) {
                JsonArray allParameters = node.getAsJsonArray("parameters");
                iterateAndFormatMembers(indentation.isEmpty() ? indentWithParentIndentation : indentation,
                        allParameters);
            }

            // Update whitespaces of endpoint.
            modifyEndpoints(node, indentation);

            // Update whitespaces of workers.
            modifyWorkers(node, indentation);

            // Update whitespaces of annotation attachments.
            modifyAnnotationAttachments(node, formatConfig, indentation);

            // Update whitespaces of markdown documentation attachments.
            modifyMarkdownDocumentation(node, formatConfig, indentation);

            // Update whitespaces for rest parameters.
            if (node.has("restParameters")) {
                JsonObject restParam = node.getAsJsonObject("restParameters");
                JsonObject restParamFormatConfig;
                if (node.has("parameters") && node.getAsJsonArray("parameters").size() > 0) {
                    restParamFormatConfig = this.getFormattingConfig(0, 1,
                            0, false,
                            this.getWhiteSpaceCount(indentation.isEmpty() ? indentWithParentIndentation : indentation),
                            true);
                } else {
                    restParamFormatConfig = this.getFormattingConfig(0, 0,
                            0, false,
                            this.getWhiteSpaceCount(indentation.isEmpty() ? indentWithParentIndentation : indentation),
                            true);
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
                        0, false,
                        this.getWhiteSpaceCount(indentation.isEmpty() ? indentWithParentIndentation : indentation),
                        true);
                returnTypeNode.add(FormattingConstants.FORMATTING_CONFIG, returnTypeFormatConfig);
            }

            // Update whitespaces for return type annotation attachments.
            modifyReturnTypeAnnotations(node, indentation);

            if (node.has("externalAnnotationAttachments")) {
                JsonArray externalAnnotationAttachments = node.getAsJsonArray("externalAnnotationAttachments");
                for (int i = 0; i < externalAnnotationAttachments.size(); i++) {
                    JsonObject externalAnnotationAttachment = externalAnnotationAttachments.get(i).getAsJsonObject();
                    JsonObject annotationFormattingConfig;
                    if (i == 0) {
                        annotationFormattingConfig = this.getFormattingConfig(
                                0, 1, 0,
                                false, this.getWhiteSpaceCount(indentation), true);
                    } else {
                        annotationFormattingConfig = this.getFormattingConfig(1, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                false, this.getWhiteSpaceCount(indentation), true);
                    }

                    externalAnnotationAttachment.add(FormattingConstants.FORMATTING_CONFIG, annotationFormattingConfig);
                }
            }

            if (node.has(FormattingConstants.BODY)) {
                modifyConstructBody(node.getAsJsonObject(FormattingConstants.BODY),
                        indentation, indentWithParentIndentation);
            }
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
            boolean isGrouped = node.has(FormattingConstants.GROUPED)
                    && node.get(FormattingConstants.GROUPED).getAsBoolean();
            boolean returnKeywordExists = node.has("returnKeywordExists") &&
                    node.get("returnKeywordExists").getAsBoolean();
            String indentation = this.getIndentation(formatConfig, true);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentWithParentIndentation : indentation);

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
                    if (text.equals(Tokens.COMMA)) {
                        functionTypeWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }

                    // Update whitespace for returns keyword.
                    if (text.equals(Tokens.RETURNS)) {
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
                        0, false, this.getWhiteSpaceCount(indentation), false);
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve available line separations.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate through and format node's whitespaces.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.GROUP)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.BY)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Handle variable formatting.
            if (node.has("variables")) {
                JsonArray variables = node.getAsJsonArray("variables");
                for (JsonElement variableItem : variables) {
                    variableItem.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 1, 0, false,
                                    this.getWhiteSpaceCount(indentationOfParent), true));
                }
            }
        }
    }

    /**
     * format group expression node.
     *
     * @param node {@link JsonObject} node as a json object
     */
    public void formatGroupExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve already available new lines.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and update whitespaces.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                            + indentation);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        }
                    } else if (text.equals(Tokens.CLOSING_PARENTHESES)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle expression formatting.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).addProperty(FormattingConstants.GROUPED, true);
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, this.getWhiteSpaceCount(indentation),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                                this.getWhiteSpaceCount(indentationOfParent),
                                formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()));
            }
        }
    }

    /**
     * format Having node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatHavingNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve available line separators.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.HAVING)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }
                }
            }

            // Handle formatting for expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format Identifier node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatIdentifierNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve available new lines.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and update whitespaces for the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }
                }
            }
        }
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
            String indentation = this.getIndentation(formatConfig, false);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

            // Set the start column to the position of the node.
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
            modifyBlockClosingBrace(node, indentWithParentIndentation, closingBraceWS, FormattingConstants.BODY, false);

            if (node.has("elseStatement")
                    && !node.getAsJsonObject("elseStatement").get(FormattingConstants.KIND)
                    .getAsString().equals("Block")) {
                JsonObject elseStatement = node.getAsJsonObject("elseStatement");
                JsonObject elseStatementFormatConfig = this.getFormattingConfig(0, 1,
                        this.getWhiteSpaceCount(indentation), false,
                        this.getWhiteSpaceCount(indentWithParentIndentation), false);
                elseStatement.add(FormattingConstants.FORMATTING_CONFIG, elseStatementFormatConfig);
            } else if (node.has("elseStatement")
                    && node.getAsJsonObject("elseStatement").has("isElseBlock")
                    && node.getAsJsonObject("elseStatement").get("isElseBlock").getAsBoolean()) {
                modifyConstructBody(node.getAsJsonObject("elseStatement"), indentation, indentWithParentIndentation);
            }

            if (node.has("condition")) {
                JsonObject conditionWs = node.getAsJsonObject("condition");
                JsonObject conditionFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentWithParentIndentation), true);
                conditionWs.add(FormattingConstants.FORMATTING_CONFIG, conditionFormatConfig);
            }

            if (node.has(FormattingConstants.BODY)) {
                modifyConstructBody(node.getAsJsonObject(FormattingConstants.BODY),
                        indentation, indentWithParentIndentation);
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
            String indentation = this.getIndentation(formatConfig, false);
            String orgName = "";
            String packageVersion = "";
            List<String> packageNames = new ArrayList<>();

            if (node.has("orgName")) {
                orgName = node.getAsJsonObject("orgName").get(FormattingConstants.VALUE).getAsString();
            }

            if (node.has("packageVersion")) {
                packageVersion = node.getAsJsonObject("packageVersion").get(FormattingConstants.VALUE).getAsString();
            }

            if (node.has("packageName")) {
                for (JsonElement packageNameItem : node.getAsJsonArray("packageName")) {
                    JsonObject packageName = packageNameItem.getAsJsonObject();
                    packageNames.add(packageName.get(FormattingConstants.VALUE).getAsString());
                }
            }

            this.preserveHeight(ws, indentation);

            boolean forwardSlashAvailable = false;
            boolean dotAvailable = false;
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.IMPORT)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                        this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                                .getAsInt()) + indentation);
                    } else if (text.equals(Tokens.DIV)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        forwardSlashAvailable = true;
                    } else if (packageNames.contains(text)) {
                        if (forwardSlashAvailable || dotAvailable) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            forwardSlashAvailable = false;
                            dotAvailable = false;
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    } else if (text.equals(orgName)
                            || text.equals(Tokens.AS)
                            || text.equals(Tokens.VERSION)
                            || text.equals(packageVersion)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.SEMICOLON) || text.equals(Tokens.DOT)) {
                        dotAvailable = true;
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else {
                        if (dotAvailable) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            dotAvailable = false;
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    }
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and update index based access expr.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_BRACKET) || text.equals(Tokens.CLOSING_BRACKET)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle formatting for the expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            // Handle formatting for the index.
            if (node.has("index")) {
                node.getAsJsonObject("index").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                        .getAsBoolean() ? indentationOfParent : indentation), true));
            }
        }
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
        // TODO: Refactor checking the expression name to remove unnecessary arguments.
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);
            boolean isExpressionAvailable = node.has(FormattingConstants.EXPRESSION);
            boolean isAsync = false;
            boolean isCheck = false;
            boolean isActionOrFieldInvocation = false;
            boolean annotationAvailable = node.has("annotationAttachments")
                    && node.getAsJsonArray("annotationAttachments").size() > 0;
            JsonObject identifierWhitespace = null;
            int expressionId = 0;

            // Preserve new lines and comments that already available.
            this.preserveHeight(ws, indentWithParentIndentation);

            if (isExpressionAvailable) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                if (expression.has("id")) {
                    expressionId = expression.get("id").getAsInt();
                }
            }

            for (int i = 0; i < ws.size(); i++) {
                JsonObject invocationWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(invocationWS.get(FormattingConstants.WS).getAsString())) {
                    String text = invocationWS.get(FormattingConstants.TEXT).getAsString();

                    if (text.equals(Tokens.CHECK) || text.equals(Tokens.CHECK_PANIC)) {
                        invocationWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                        .getAsInt())
                                        + indentation);
                        isCheck = true;
                    } else if (text.equals(Tokens.START) && !isActionOrFieldInvocation) {
                        if (isCheck || annotationAvailable) {
                            invocationWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else {
                            invocationWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                    .get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                    + this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt())
                                    + indentation);
                        }
                        isAsync = true;
                    } else if (text.equals(Tokens.DOT) || text.equals(Tokens.RIGHT_ARROW)) {
                        invocationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        isActionOrFieldInvocation = true;
                    } else if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        invocationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.COMMA)) {
                        invocationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.CLOSING_PARENTHESES)) {
                        invocationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.COLON)) {
                        // Handle colon whitespaces.
                        invocationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);

                        // Handle package alias.
                        JsonObject packageAliasWhitespace = ws.get(i - 1).getAsJsonObject();
                        if (this.noHeightAvailable(packageAliasWhitespace.get(FormattingConstants.WS).getAsString())) {
                            if (isAsync || isCheck) {
                                packageAliasWhitespace.addProperty(FormattingConstants.WS,
                                        FormattingConstants.SINGLE_SPACE);
                            } else {
                                packageAliasWhitespace.addProperty(FormattingConstants.WS,
                                        this.getNewLines(formatConfig
                                                .get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                                + this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                                .getAsInt())
                                                + indentation);
                            }
                        }

                        // Identifier whitespace.
                        identifierWhitespace = ws.get(i + 1).getAsJsonObject();
                        if (this.noHeightAvailable(identifierWhitespace.get(FormattingConstants.WS).getAsString())) {
                            identifierWhitespace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    } else if (text.equals(Tokens.ERROR)) {
                        if (formatConfig
                                .get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                            invocationWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                    .get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                    + indentation);
                        } else if (formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt() > 0) {
                            invocationWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        } else {
                            invocationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    } else if (identifierWhitespace == null &&
                            !ws.get(i + 1).getAsJsonObject().get(FormattingConstants.TEXT)
                                    .getAsString().equals(Tokens.COLON)) {
                        if (isAsync || isCheck) {
                            invocationWS.addProperty(FormattingConstants.WS,
                                    FormattingConstants.SINGLE_SPACE);
                        } else if (isActionOrFieldInvocation) {
                            invocationWS.addProperty(FormattingConstants.WS,
                                    FormattingConstants.EMPTY_SPACE);
                        } else {
                            if (formatConfig
                                    .get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                                invocationWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                        .get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                            } else if (formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt() > 0) {
                                invocationWS.addProperty(FormattingConstants.WS,
                                        this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                                .getAsInt()));
                            } else {
                                invocationWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            }
                        }
                    }
                }
            }

            // Update argument expressions whitespaces.
            if (node.has("argumentExpressions")) {
                JsonArray argumentExpressions = node.getAsJsonArray("argumentExpressions");
                if (expressionId != 0) {
                    boolean foundMatch = false;
                    JsonObject matchedArgument = null;
                    for (JsonElement argument : argumentExpressions) {
                        int argumentID = argument.getAsJsonObject().get("id").getAsInt();
                        if (argumentID == expressionId) {
                            matchedArgument = argument.getAsJsonObject();
                            foundMatch = true;
                            break;
                        }
                    }

                    if (foundMatch && matchedArgument != null) {
                        argumentExpressions.remove(matchedArgument);
                    }
                }

                iterateAndFormatMembers(indentation.isEmpty() ? indentWithParentIndentation : indentation,
                        argumentExpressions);
            }

            if (node.has("requiredArgs")) {
                JsonArray argumentExpressions = node.getAsJsonArray("requiredArgs");
                if (expressionId != 0) {
                    boolean foundMatch = false;
                    JsonObject matchedArgument = null;
                    for (JsonElement argument : argumentExpressions) {
                        int argumentID = argument.getAsJsonObject().get("id").getAsInt();
                        if (argumentID == expressionId) {
                            matchedArgument = argument.getAsJsonObject();
                            foundMatch = true;
                            break;
                        }
                    }

                    if (foundMatch && matchedArgument != null) {
                        argumentExpressions.remove(matchedArgument);
                    }
                }

                iterateAndFormatMembers(indentation.isEmpty() ? indentWithParentIndentation : indentation,
                        argumentExpressions);
            }

            if (node.has("annotationAttachments")) {
                JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                for (JsonElement annotationAttachment : annotationAttachments) {
                    annotationAttachment.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                    formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                    false, this.getWhiteSpaceCount(indentWithParentIndentation),
                                    true));
                }
            }

            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                expression.add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }
        }
    }

    /**
     * format Join Streaming Input node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatJoinStreamingInputNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve already existing line separators.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            boolean firstKeywordUpdated = false;
            // Iterate through whitespaces and format.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();

                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if (text.equals(Tokens.LEFT)
                        || text.equals(Tokens.RIGHT)
                        || text.equals(Tokens.FULL)
                        || text.equals(Tokens.OUTER)
                        || text.equals(Tokens.INNER)
                        || text.equals(Tokens.JOIN)) {
                    if (firstKeywordUpdated
                            && this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {

                        currentWS.addProperty(FormattingConstants.WS,
                                FormattingConstants.SINGLE_SPACE);

                    } else {
                        if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                            .getAsInt()) + indentation);
                        }
                        firstKeywordUpdated = true;
                    }
                } else if (text.equals(Tokens.UNIDIRECTIONAL)) {
                    if (node.get("unidirectionalAfterJoin").getAsBoolean()
                            && this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (node.get("unidirectionalBeforeJoin").getAsBoolean()) {
                        if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                            .getAsInt()) + indentation);
                        }
                        firstKeywordUpdated = true;
                    }
                } else if (text.equals(Tokens.ON)
                        && this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            }

            // Handles streaming input formatting.
            if (node.has("streamingInput")) {
                node.getAsJsonObject("streamingInput").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            // Handles on expression's formatting.
            if (node.has("onExpression")) {
                node.getAsJsonObject("onExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve available line separation.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and update node's whitespaces.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.LIMIT)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }
        }
    }

    /**
     * format list constructor expression node.
     *
     * @param node {@link JsonObject} node as a json object
     */
    public void formatListConstructorExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();
            boolean isGrouped = node.has(FormattingConstants.GROUPED)
                    && node.get(FormattingConstants.GROUPED).getAsBoolean();

            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        if (formatConfig.get(FormattingConstants.SPACE_COUNT)
                                .getAsInt() > 0) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                            .getAsInt()));
                        } else if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                            currentWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                    .get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) + indentation);
                        }
                    } else if (text.equals(Tokens.OPENING_BRACKET)) {
                        if (isGrouped) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    FormattingConstants.EMPTY_SPACE);
                        } else {
                            if (formatConfig.get(FormattingConstants.SPACE_COUNT)
                                    .getAsInt() > 0) {
                                currentWS.addProperty(FormattingConstants.WS,
                                        this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                                .getAsInt()));
                            } else if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                                currentWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                        .get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) + indentation);
                            }
                        }
                    } else if (text.equals(Tokens.CLOSING_BRACKET) || text.equals(Tokens.COMMA)
                            || text.equals(Tokens.CLOSING_PARENTHESES)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            if (node.has(FormattingConstants.EXPRESSIONS)) {
                JsonArray expressions = node.getAsJsonArray(FormattingConstants.EXPRESSIONS);
                this.iterateAndFormatMembers(indentationOfParent, expressions);
            }
        }
    }

    /**
     * format Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatLiteralNode(JsonObject node) {
        this.modifyLiteralNode(node);
    }

    /**
     * format Lock node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatLockNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve new lines added by the user.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and format whitespaces of the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.LOCK)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.OPENING_BRACE)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.CLOSING_BRACE)) {
                        modifyBlockClosingBrace(node, indentation, currentWS, FormattingConstants.BODY, true);
                    }
                }
            }

            // Handle the formatting for body.
            if (node.has(FormattingConstants.BODY)) {
                node.getAsJsonObject(FormattingConstants.BODY).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(1, 0, this.getWhiteSpaceCount(indentation),
                                true, this.getWhiteSpaceCount(indentation), true));
            }
        }
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
            String indentation = this.getIndentation(formatConfig, false);
            this.preserveHeight(ws, indentation);

            // Update match whitespaces.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    String text = wsItem.get(FormattingConstants.TEXT).getAsString();

                    // Update match keyword whitespaces.
                    if (text.equals(Tokens.MATCH)) {
                        wsItem.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }

                    // Update opening bracket whitespace.
                    if (text.equals(Tokens.OPENING_BRACE)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update opening bracket whitespace.
                    if (text.equals(Tokens.CLOSING_BRACE)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
                    }
                }
            }

            // Update expression whitespace.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentation), false);
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
            }

            // Update pattern clauses whitespace.
            modifyPatternClauses(node, indentation);
        }
    }

    /**
     * format Match structured pattern clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchStructuredPatternClauseNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            boolean withCurlies = node.has("withCurlies") && node.get("withCurlies").getAsBoolean();

            // Get the indentation for the node.
            String indentation = this.getIndentation(formatConfig, false);

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, indentation);

            // Handle whitespace for
            if (node.has("variableNode")) {
                if (ws.get(0).getAsJsonObject().get(FormattingConstants.TEXT).getAsString().equals("var")) {
                    node.getAsJsonObject("variableNode").add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 1, 0, false,
                                    this.getWhiteSpaceCount(indentation), true));
                } else {
                    node.getAsJsonObject("variableNode").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
                }
            }

            // Update the match pattern whitespace.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    String text = wsItem.get(FormattingConstants.TEXT).getAsString();

                    // Update the => whitespace.
                    if (text.equals(Tokens.EQUAL_GT)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update the opening brace whitespace.
                    if (text.equals(Tokens.OPENING_BRACE)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update the closing brace whitespace.
                    if (text.equals(Tokens.CLOSING_BRACE)) {
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

            if (node.has("statement")) {
                modifyConstructBody(node.getAsJsonObject("statement"), indentation, indentation);
            }
        }
    }

    /**
     * format Match static pattern clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatMatchStaticPatternClauseNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            boolean withCurlies = node.has("withCurlies") && node.get("withCurlies").getAsBoolean();

            // Get the indentation for the node.
            String indentation = this.getIndentation(formatConfig, false);

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, indentation);

            // Handle whitespace for
            if (node.has("literal")) {
                node.getAsJsonObject("literal").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // Update the match pattern whitespace.
            for (JsonElement item : ws) {
                JsonObject wsItem = item.getAsJsonObject();
                if (this.noHeightAvailable(wsItem.get(FormattingConstants.WS).getAsString())) {
                    String text = wsItem.get(FormattingConstants.TEXT).getAsString();

                    // Update the => whitespace.
                    if (text.equals(Tokens.EQUAL_GT)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update the opening brace whitespace.
                    if (text.equals(Tokens.OPENING_BRACE)) {
                        wsItem.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }

                    // Update the closing brace whitespace.
                    if (text.equals(Tokens.CLOSING_BRACE)) {
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

            if (node.has("statement")) {
                modifyConstructBody(node.getAsJsonObject("statement"), indentation, indentation);
            }
        }
    }

    /**
     * format Named Args Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatNamedArgsExprNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG) && node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getIndentation(formatConfig, true);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve available new lines for whitespaces in this node.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and update whitespaces for the named arg.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }
                }
            }

            // Update whitespaces for the expression
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent),
                                formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()));
            }
        }
    }

    /**
     * format Numeric Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatNumericLiteralNode(JsonObject node) {
        this.modifyLiteralNode(node);
    }

    /**
     * format Object Type node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatObjectTypeNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            JsonArray fields = node.getAsJsonArray(FormattingConstants.FIELDS);
            JsonArray functions = node.getAsJsonArray("functions");
            boolean isAnonType = node.has(FormattingConstants.IS_ANON_TYPE)
                    && node.get(FormattingConstants.IS_ANON_TYPE).getAsBoolean();

            boolean lineSeparationAvailable = false;
            if (isAnonType) {
                lineSeparationAvailable = this.isMemberOnNewLine(fields);
                if (functions.size() > 0 || node.has("initFunction")) {
                    lineSeparationAvailable = true;
                }
            }

            if (node.has(FormattingConstants.FIELDS)) {
                for (int i = 0; i < fields.size(); i++) {
                    JsonObject fieldFormatConfig;
                    if (isAnonType) {
                        if (lineSeparationAvailable) {
                            fieldFormatConfig = this.getFormattingConfig(1, 0,
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt() > 0
                                            ? formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()
                                            : formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt(),
                                    true,
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt() > 0
                                            ? formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()
                                            : formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt(),
                                    true);
                        } else if (i == 0) {
                            fieldFormatConfig = this.getFormattingConfig(0, 0,
                                    0, false,
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt() > 0
                                            ? formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()
                                            : formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt(),
                                    true);
                        } else {
                            fieldFormatConfig = this.getFormattingConfig(0, 1,
                                    0, false,
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt() > 0
                                            ? formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()
                                            : formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt(),
                                    true);
                        }
                    } else {
                        fieldFormatConfig = this.getFormattingConfig(1, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt() > 0
                                        ? formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()
                                        : formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt(),
                                true,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt() > 0
                                        ? formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()
                                        : formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt(),
                                true);
                    }
                    fields.get(i).getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, fieldFormatConfig);
                }
            }

            if (node.has("initFunction")) {
                JsonObject initFunction = node.getAsJsonObject("initFunction");
                JsonObject functionFormatConfig = this.getFormattingConfig(2, 0,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                initFunction.add(FormattingConstants.FORMATTING_CONFIG, functionFormatConfig);
            }

            if (node.has("functions")) {
                for (int i = 0; i < functions.size(); i++) {
                    JsonObject functionFormatConfig = this.getFormattingConfig(2, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    functions.get(i).getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, functionFormatConfig);
                }
            }

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                String indentation = this.getWhiteSpaces(formatConfig
                        .get(FormattingConstants.START_COLUMN).getAsInt());
                this.preserveHeight(ws, indentation);
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        if (text.equals(Tokens.OPENING_BRACE)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else if (text.equals(Tokens.CLOSING_BRACE)) {
                            if (node.getAsJsonArray(FormattingConstants.FIELDS).size() <= 0
                                    && node.getAsJsonArray("functions").size() <= 0
                                    && !node.has("initFunction")) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                                        + indentation);
                            }
                        }
                    }
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
        if (node.has(FormattingConstants.FORMATTING_CONFIG) && node.has(FormattingConstants.WS)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.ORDER)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.BY)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle order by variable formatting.
            if (node.has("variables")) {
                JsonArray variables = node.getAsJsonArray("variables");
                for (JsonElement variableElement : variables) {
                    variableElement.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 1, 0, false,
                                    this.getWhiteSpaceCount(indentationOfParent), true));
                }
            }
        }
    }

    /**
     * format Order By Variable node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatOrderByVariableNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Handle variable reference formatting.
            if (node.has("variableReference")) {
                node.getAsJsonObject("variableReference").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            // If whitespaces available iterate and update whitespaces.
            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                        ? indentationOfParent : indentation);

                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())
                            && text.equals(node.get("typeString").getAsString())) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }
        }

    }

    /**
     * format Output Rate Limit node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatOutputRateLimitNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OUTPUT)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }
        }
    }

    /**
     * format panic node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatPanicNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and update the whitespaces of the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.PANIC)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.SEMICOLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle the expression formatting.
            if (node.has(FormattingConstants.EXPRESSIONS)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSIONS).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, this.getWhiteSpaceCount(indentation),
                                false, this.getWhiteSpaceCount(indentationOfParent),
                                formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()));
            }
        }
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
     * format Record destructor node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRecordDestructureNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve already available new lines.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and update whitespaces of the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.SEMICOLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Update whitespaces for record variable ref.
            if (node.has("variableRefs")) {
                node.getAsJsonObject("variableRefs").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // Update whitespaces for expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation),
                                true));
            }
        }
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
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);
            String parentKind = node.getAsJsonObject(FormattingConstants.PARENT).get(FormattingConstants.KIND)
                    .getAsString();
            boolean isTable = parentKind.equals("Table");

            // Preserve line separation that already available.
            this.preserveHeight(ws, indentWithParentIndentation);

            // Has at least one line separation in records.
            boolean lineSeparationAvailable = false;
            if (node.has("keyValuePairs")) {
                lineSeparationAvailable = this.isMemberOnNewLine(node.getAsJsonArray("keyValuePairs"));
            }

            // Iterate and update Whitespaces for the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                // Update whitespace for opening brace.
                if (text.equals(Tokens.OPENING_BRACE)
                        && this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (isTable) {
                        currentWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                .get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) + indentWithParentIndentation);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }
                }

                // Update whitespace for closing brace.
                if (text.equals(Tokens.CLOSING_BRACE)
                        && this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (lineSeparationAvailable) {
                        if (node.has("keyValuePairs")
                                && node.getAsJsonArray("keyValuePairs").size() <= 0) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    FormattingConstants.NEW_LINE + indentWithParentIndentation);
                        }
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }

                // Update whitespaces for the key value pair separator , or ;.
                if (text.equals(Tokens.COMMA) ||
                        currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.SEMICOLON)) {
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        currentWS.addProperty(FormattingConstants.WS,
                                FormattingConstants.EMPTY_SPACE);
                    } else {
                        this.preserveHeightForWS(currentWS, indentWithParentIndentation
                                + FormattingConstants.SPACE_TAB);
                        lineSeparationAvailable = true;
                    }
                }
            }

            // Update the key value pair of a record.
            if (node.has("keyValuePairs")) {
                JsonArray keyValuePairs = node.getAsJsonArray("keyValuePairs");
                for (int i = 0; i < keyValuePairs.size(); i++) {
                    JsonObject keyValue = keyValuePairs.get(i).getAsJsonObject();
                    JsonObject keyValueFormatting;
                    if (lineSeparationAvailable) {
                        keyValueFormatting = this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentWithParentIndentation), true,
                                this.getWhiteSpaceCount(indentWithParentIndentation), false);
                    } else if (i == 0) {
                        keyValueFormatting = this.getFormattingConfig(0, 0,
                                0, false,
                                this.getWhiteSpaceCount(indentWithParentIndentation), true);
                    } else {
                        keyValueFormatting = this.getFormattingConfig(0, 1,
                                0, false,
                                this.getWhiteSpaceCount(indentWithParentIndentation), true);
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
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                // Update whitespace for colon of the record literal key value pair.
                this.preserveHeight(ws, indentation);

                boolean colonVisited = false;
                boolean calculatedKey = false;

                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();

                    if (text.equals(Tokens.COLON)) {
                        colonVisited = true;
                    } else if (text.equals(Tokens.OPENING_BRACKET) && !colonVisited) {
                        calculatedKey = true;
                    }

                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        if (text.equals(Tokens.COLON) || text.equals(Tokens.CLOSING_BRACKET)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else if (text.equals(Tokens.OPENING_BRACKET)) {
                            currentWS.addProperty(FormattingConstants.WS, this.getWhiteSpaces(formatConfig
                                    .get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        }
                    }
                }

                // Update whitespace for key value of record literal.
                if (node.has("key")) {
                    JsonObject keyNode = node.getAsJsonObject("key");
                    if (calculatedKey) {
                        keyNode.add(FormattingConstants.FORMATTING_CONFIG,
                                this.getFormattingConfig(0, 0, 0, false,
                                        formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt(),
                                        formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()));
                    } else {
                        keyNode.add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
                    }
                }
            }

            // Update whitespace for value of record literal.
            if (node.has(FormattingConstants.VALUE)) {
                JsonObject valueNode = node.getAsJsonObject(FormattingConstants.VALUE);
                JsonObject valueNodeFormatConfig = formatConfig;
                if (node.has(FormattingConstants.WS)) {
                    valueNodeFormatConfig = this.getFormattingConfig(0, 1,
                            0, false, this.getWhiteSpaceCount(indentation), true);
                }
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
            JsonArray fields = node.getAsJsonArray(FormattingConstants.FIELDS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean isAnonType = node.has(FormattingConstants.IS_ANON_TYPE)
                    && node.get(FormattingConstants.IS_ANON_TYPE).getAsBoolean();
            boolean lineSeparationAvailable = false;

            if (isAnonType) {
                // Check whether fields have placed on new lines.
                lineSeparationAvailable = this.isMemberOnNewLine(fields);
            }

            // If whitespaces are available for the node
            // Update each whitespace with given whitespace values.
            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

                this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                        ? indentationOfParent : indentation);

                if (isAnonType) {
                    // If rest param ellipsis symbol has a new line
                    // consider it as a line separation is available.
                    for (JsonElement wsItem : ws) {
                        JsonObject currentWS = wsItem.getAsJsonObject();
                        String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                        if (text.equals(Tokens.ELLIPSIS)
                                && !noNewLine(currentWS.get(FormattingConstants.WS).getAsString())) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    FormattingConstants.NEW_LINE + (this.getWhiteSpaceCount(indentation) > 0
                                            ? indentation : indentationOfParent) + FormattingConstants.SPACE_TAB);
                            lineSeparationAvailable = true;
                            break;
                        }
                    }

                    // If rest param simple var ref has new line
                    // consider it as a line separation is available.
                    if (node.has("restFieldType")
                            && node.get("restFieldType").getAsJsonObject().has(FormattingConstants.WS)
                            && !lineSeparationAvailable) {
                        JsonObject restParam = node.getAsJsonObject("restFieldType");
                        List<JsonObject> sortedWSForRestParam = FormattingSourceGen.extractWS(restParam);
                        for (JsonObject wsForRestParam : sortedWSForRestParam) {
                            String currentWS = wsForRestParam.get(FormattingConstants.WS).getAsString();
                            if (!noNewLine(currentWS)) {
                                lineSeparationAvailable = true;
                                break;
                            }
                        }
                    }
                }

                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                        if (text.equals(Tokens.RECORD)) {
                            if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                                currentWS.addProperty(FormattingConstants.WS,
                                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                                .getAsInt()) + (this.getWhiteSpaceCount(indentation) > 0
                                                ? indentation : indentationOfParent));
                            } else if (formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt() > 0) {
                                currentWS.addProperty(FormattingConstants.WS,
                                        this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                                .getAsInt()));
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            }
                        } else if (text.equals(Tokens.OPENING_BRACE) || text.equals(Tokens.SEAL_OPENING)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else if (text.equals(Tokens.ELLIPSIS)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else if (text.equals(Tokens.CLOSING_BRACE) || text.equals(Tokens.SEAL_CLOSING)) {
                            if (isAnonType) {
                                if (lineSeparationAvailable) {
                                    currentWS.addProperty(FormattingConstants.WS,
                                            FormattingConstants.NEW_LINE + (this.getWhiteSpaceCount(indentation) > 0
                                                    ? indentation : indentationOfParent));
                                } else {
                                    currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                                }
                            } else {
                                if (fields.size() <= 0 && !node.has("restFieldType")) {
                                    currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                                } else {
                                    currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE
                                            + (this.getWhiteSpaceCount(indentation) > 0
                                            ? indentation : indentationOfParent));
                                }
                            }
                        } else if (text.equals(Tokens.SEMICOLON)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    }
                }

                // Update the restField whitespace.
                if (node.has("restFieldType") &&
                        node.get("restFieldType").getAsJsonObject().has(FormattingConstants.WS)) {
                    JsonObject restFieldType = node.getAsJsonObject("restFieldType");
                    JsonObject restFieldTypeFormatConfig;
                    if (isAnonType) {
                        if (lineSeparationAvailable) {
                            restFieldTypeFormatConfig = this.getFormattingConfig(1, 0,
                                    (this.getWhiteSpaceCount(indentation) > 0
                                            ? this.getWhiteSpaceCount(indentation)
                                            : this.getWhiteSpaceCount(indentationOfParent)), true,
                                    (this.getWhiteSpaceCount(indentation) > 0
                                            ? this.getWhiteSpaceCount(indentation)
                                            : this.getWhiteSpaceCount(indentationOfParent)), false);
                        } else {
                            restFieldTypeFormatConfig = this.getFormattingConfig(0,
                                    fields.size() <= 0 ? 0 : 1, 0, false,
                                    (this.getWhiteSpaceCount(indentation) > 0
                                            ? this.getWhiteSpaceCount(indentation)
                                            : this.getWhiteSpaceCount(indentationOfParent)), true);
                        }
                    } else {
                        restFieldTypeFormatConfig = this.getFormattingConfig(1, 0,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    }


                    restFieldType.add(FormattingConstants.FORMATTING_CONFIG, restFieldTypeFormatConfig);
                }
            }

            // Update the fields whitespace.
            if (isAnonType) {
                for (int i = 0; i < fields.size(); i++) {
                    JsonObject child = fields.get(i).getAsJsonObject();
                    JsonObject childFormatConfig;
                    if (lineSeparationAvailable) {
                        childFormatConfig = this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentation) > 0
                                        ? this.getWhiteSpaceCount(indentation)
                                        : this.getWhiteSpaceCount(indentationOfParent),
                                true, this.getWhiteSpaceCount(indentation) > 0
                                        ? this.getWhiteSpaceCount(indentation)
                                        : this.getWhiteSpaceCount(indentationOfParent),
                                false);
                    } else if (i == 0) {
                        childFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentation) > 0
                                        ? this.getWhiteSpaceCount(indentation)
                                        : this.getWhiteSpaceCount(indentationOfParent), true);
                    } else {
                        childFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentation) > 0
                                        ? this.getWhiteSpaceCount(indentation)
                                        : this.getWhiteSpaceCount(indentationOfParent), true);
                    }

                    child.add(FormattingConstants.FORMATTING_CONFIG, childFormatConfig);
                }
            } else {
                for (int i = 0; i < fields.size(); i++) {
                    JsonObject child = fields.get(i).getAsJsonObject();
                    JsonObject childFormatConfig = this.getFormattingConfig(1, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), true,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false);
                    child.add(FormattingConstants.FORMATTING_CONFIG, childFormatConfig);
                }
            }
        }
    }

    /**
     * format Record Variable node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRecordVariableNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG) && node.has(FormattingConstants.WS)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, useParentIndentation ? indentWithParentIndentation : indentation);

            // Has at least one line separation in records.
            boolean lineSeparationAvailable = false;
            if (node.has("variables")) {
                lineSeparationAvailable = isMemberOnNewLine(node.getAsJsonArray("variables"));
            }

            // Update whitespaces of the variable first keyword.
            boolean frontedWithKeyword = false;
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if ((text.equals(Tokens.FINAL) || text.equals(Tokens.PUBLIC)
                        || text.equals(Tokens.VAR) || text.equals(Tokens.CLIENT)
                        || text.equals(Tokens.LISTENER) || text.equals(Tokens.ABSTRACT)
                        || text.equals(Tokens.CHANNEL) || text.equals(Tokens.CONST))) {
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                        .getAsInt()) + indentation);
                    }
                    frontedWithKeyword = true;
                }
            }

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_BRACE)) {
                        if (frontedWithKeyword || node.has(FormattingConstants.TYPE_NODE)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                            .getAsInt())
                                            + this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                            .getAsInt()) + indentation);
                        }
                    } else if (text.equals(Tokens.CLOSING_BRACE)) {
                        if (lineSeparationAvailable) {
                            if (node.has("variables")
                                    && node.getAsJsonArray("variables").size() <= 0) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS,
                                        FormattingConstants.NEW_LINE + indentWithParentIndentation);
                            }
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    } else if (text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            if (node.has(FormattingConstants.TYPE_NODE) && !frontedWithKeyword) {
                JsonObject typeNode = node.getAsJsonObject(FormattingConstants.TYPE_NODE);
                JsonObject typeFormatConfig;

                if (!(node.has("annotationAttachments")
                        && node.getAsJsonArray("annotationAttachments").size() > 0)) {
                    typeFormatConfig = this.getFormattingConfig(
                            formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                            formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                            formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                            this.getWhiteSpaceCount(indentation), false);
                    typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeFormatConfig);
                }
            }

            if (node.has("variables")) {
                JsonArray variables = node.getAsJsonArray("variables");
                int i = 0;
                for (JsonElement variableItem : variables) {
                    JsonObject variable = variableItem.getAsJsonObject();
                    if (variable.has(FormattingConstants.WS)) {
                        JsonObject variableFormatConfig;
                        if (lineSeparationAvailable) {
                            variableFormatConfig = this.getFormattingConfig(1, 0,
                                    this.getWhiteSpaceCount(indentWithParentIndentation), true,
                                    this.getWhiteSpaceCount(useParentIndentation ? indentWithParentIndentation
                                            : indentation), true);
                        } else if (i == 0) {
                            variableFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                                    this.getWhiteSpaceCount(useParentIndentation
                                            ? indentWithParentIndentation : indentation), true);
                            ++i;
                        } else {
                            variableFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                                    this.getWhiteSpaceCount(useParentIndentation
                                            ? indentWithParentIndentation : indentation), true);
                        }

                        variable.add(FormattingConstants.FORMATTING_CONFIG, variableFormatConfig);
                    }
                }
            }
        }
    }

    /**
     * format Record Variable Ref node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatRecordVariableRefNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve available new lines.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Has at least one line separation in records.
            boolean lineSeparationAvailable = false;

            // If rest param ellipsis symbol has a new line
            // consider it as a line separation is available.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if (text.equals(Tokens.ELLIPSIS) && !noNewLine(currentWS.get(FormattingConstants.WS).getAsString())) {
                    currentWS.addProperty(FormattingConstants.WS,
                            FormattingConstants.NEW_LINE + (this.getWhiteSpaceCount(indentation) > 0
                                    ? indentation : indentationOfParent) + FormattingConstants.SPACE_TAB);
                    lineSeparationAvailable = true;
                    break;
                }
            }

            // If rest param simple var ref has new line
            // consider it as a line separation is available.
            if (node.has("restParam")) {
                JsonObject restParam = node.getAsJsonObject("restParam");
                List<JsonObject> sortedWSForRestParam = FormattingSourceGen.extractWS(restParam);
                for (JsonObject wsForRestParam : sortedWSForRestParam) {
                    String currentWS = wsForRestParam.get(FormattingConstants.WS).getAsString();
                    if (!noNewLine(currentWS)) {
                        lineSeparationAvailable = true;
                        break;
                    }
                }
            }

            // If record reference fields has new line
            // Consider it as a line separation is available.
            if (node.has("recordRefFields") && !lineSeparationAvailable) {
                lineSeparationAvailable = this.isMemberOnNewLine(node.getAsJsonArray("recordRefFields"));
            }

            // Iterate and update whitespaces of the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_BRACE)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                        .getAsInt()) + indentation);
                    } else if (text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.ELLIPSIS)) {
                        if (lineSeparationAvailable) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    FormattingConstants.NEW_LINE + (this.getWhiteSpaceCount(indentation) > 0
                                            ? indentation : indentationOfParent) + FormattingConstants.SPACE_TAB);
                        } else if (node.has("recordRefFields") && node.has("restParam")
                                && node.getAsJsonArray("recordRefFields").size() <= 0) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    } else if (text.equals(Tokens.CLOSING_BRACE)) {
                        if (lineSeparationAvailable) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    FormattingConstants.NEW_LINE + (this.getWhiteSpaceCount(indentation) > 0
                                            ? indentation : indentationOfParent));
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    }
                }
            }

            // Update whitespaces of record ref fields.
            if (node.has("recordRefFields")) {
                JsonArray recordRefFields = node.getAsJsonArray("recordRefFields");
                int i = 0;
                for (JsonElement recordRefFieldItem : recordRefFields) {
                    JsonObject recordRefField = recordRefFieldItem.getAsJsonObject();
                    if (recordRefField.has(FormattingConstants.WS)) {
                        JsonObject recordRefFieldFormatConfig;
                        if (lineSeparationAvailable) {
                            recordRefFieldFormatConfig = this.getFormattingConfig(1, 0,
                                    this.getWhiteSpaceCount(indentation) > 0 ? this.getWhiteSpaceCount(indentation)
                                            : this.getWhiteSpaceCount(indentationOfParent), true,
                                    this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation),
                                    true);
                        } else if (i == 0) {
                            recordRefFieldFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                                    this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation),
                                    true);
                            ++i;
                        } else {
                            recordRefFieldFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                                    this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation),
                                    true);
                        }
                        recordRefField.add(FormattingConstants.FORMATTING_CONFIG, recordRefFieldFormatConfig);
                    }
                }
            }

            // Update whitespaces of rest param.
            if (node.has("restParam")) {
                node.getAsJsonObject("restParam").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation),
                                true));
            }
        }
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
            String indentation = this.getIndentation(formatConfig, false);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

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
                                this.getWhiteSpaceCount(indentWithParentIndentation), false);
                    } else {
                        endpointFormatConfig = this.getFormattingConfig(2, 0,
                                this.getWhiteSpaceCount(indentation), true,
                                this.getWhiteSpaceCount(indentWithParentIndentation), false);
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, true);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve available whitespaces.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate through whitespaces and format.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.ELLIPSIS)) {
                        currentWS.addProperty(FormattingConstants.WS, indentation);
                    }
                }
            }

            // Update expression whitespaces.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
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
            String indentation = this.getIndentation(formatConfig, false);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update return keyword.
            JsonObject returnKeywordWhitespace = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(returnKeywordWhitespace.get(FormattingConstants.WS).getAsString())) {
                if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                    returnKeywordWhitespace.addProperty(FormattingConstants.WS,
                            this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                    indentation);
                } else if (formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt() > 0) {
                    returnKeywordWhitespace.addProperty(FormattingConstants.WS,
                            this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                }
            }

            // Update expression whitespaces.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentation), true);
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.SELECT)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.STAR)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            if (node.has("selectExpressions")) {
                JsonArray selectExpressions = node.getAsJsonArray("selectExpressions");
                for (JsonElement expr : selectExpressions) {
                    JsonObject selectExpression = expr.getAsJsonObject();
                    selectExpression.add(FormattingConstants.FORMATTING_CONFIG, this.getFormattingConfig(0,
                            1, 0, false, this.getWhiteSpaceCount(formatConfig
                                    .get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                                    ? indentationOfParent : indentation), true));
                }
            }

            if (node.has("groupBy")) {
                node.getAsJsonObject("groupBy").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentation), false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            if (node.has("having")) {
                node.getAsJsonObject("having").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentation), false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format Select Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatSelectExpressionNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // If whitespaces available iterate and update whitespaces.
            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                        ? indentationOfParent : indentation);

                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }
        }
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
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            if (node.has("anonymousService") && node.get("anonymousService").getAsBoolean()) {
                // Update whitespaces for service definition.
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        if (text.equals(Tokens.SERVICE)) {
                            String whiteSpace = (node.has("annotationAttachments") &&
                                    node.getAsJsonArray("annotationAttachments").size() > 0)
                                    ? (FormattingConstants.SINGLE_SPACE)
                                    : (this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                    .getAsInt()));

                            currentWS.addProperty(FormattingConstants.WS, whiteSpace);
                        } else {
                            // Update service identifier whitespaces and on keyword.
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    }
                }

                // Update whitespaces for body.
                if (node.has("typeDefinition")
                        && node.getAsJsonObject("typeDefinition").has(FormattingConstants.TYPE_NODE)) {
                    JsonObject typeNode = node.getAsJsonObject("typeDefinition")
                            .getAsJsonObject(FormattingConstants.TYPE_NODE);

                    JsonObject typeDefFormatConfig = this.getFormattingConfig(0, 1,
                            this.getWhiteSpaceCount(indentationOfParent), true,
                            this.getWhiteSpaceCount(indentationOfParent), false);
                    typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeDefFormatConfig);
                }

                // Update whitespaces for resources.
                if (node.has("resources")) {
                    JsonArray resources = node.getAsJsonArray("resources");
                    iterateAndFormatBlockStatements(indentationOfParent, indentationOfParent, resources);
                }

                // Update whitespaces of the annotation attachments.
                if (node.has("annotationAttachments")) {
                    JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                    for (int i = 0; i < annotationAttachments.size(); i++) {
                        JsonObject annotationAttachment = annotationAttachments.get(i).getAsJsonObject();
                        JsonObject annotationAttachmentFormattingConfig;
                        annotationAttachmentFormattingConfig = this.getFormattingConfig(0, 1,
                                0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true);
                        annotationAttachment.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG,
                                annotationAttachmentFormattingConfig);
                    }
                }
            } else {
                // Update whitespaces for service definition.
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        if (text.equals(Tokens.SERVICE)) {
                            String whiteSpace = ((node.has("annotationAttachments") &&
                                    node.getAsJsonArray("annotationAttachments").size() > 0) ||
                                    (node.has("documentationAttachments") &&
                                            node.getAsJsonArray("documentationAttachments").size() > 0) ||
                                    (node.has("deprecatedAttachments") &&
                                            node.getAsJsonArray("deprecatedAttachments").size() > 0))
                                    ? (FormattingConstants.NEW_LINE + indentation)
                                    : (this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                    + indentation);

                            currentWS.addProperty(FormattingConstants.WS, whiteSpace);
                        } else if (text.equals(Tokens.COMMA)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            // Update service identifier whitespaces and on keyword.
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    }
                }

                // Update whitespaces for body.
                if (node.has("typeDefinition")
                        && node.getAsJsonObject("typeDefinition").has(FormattingConstants.TYPE_NODE)) {
                    JsonObject typeNode = node.getAsJsonObject("typeDefinition")
                            .getAsJsonObject(FormattingConstants.TYPE_NODE);
                    String typeNodeIndentation = this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                            .getAsInt())
                            + (formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                            ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                            FormattingConstants.SPACE_TAB)
                            : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()));

                    JsonObject typeDefFormatConfig = this.getFormattingConfig(1, 0,
                            this.getWhiteSpaceCount(typeNodeIndentation), true,
                            this.getWhiteSpaceCount(typeNodeIndentation), false);
                    typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeDefFormatConfig);
                }

                // Update whitespaces for resources.
                if (node.has("resources")) {
                    JsonArray resources = node.getAsJsonArray("resources");
                    iterateAndFormatBlockStatements(this.getWhiteSpaces(formatConfig
                            .get(FormattingConstants.START_COLUMN).getAsInt()), indentation, resources);
                }

                // Update whitespaces of markdown documentation attachments.
                modifyMarkdownDocumentation(node, formatConfig, indentation);

                // Update whitespaces of the annotation attachments.
                modifyAnnotationAttachments(node, formatConfig, indentation);

                // Handles formatting for attached expressions.
                if (node.has("attachedExprs")) {
                    JsonArray attachedExprs = node.getAsJsonArray("attachedExprs");
                    for (JsonElement attachedExpr : attachedExprs) {
                        JsonObject memberFormatConfig = this.getFormattingConfig(0, 1,
                                0, false, this.getWhiteSpaceCount(indentation), true);
                        attachedExpr.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG, memberFormatConfig);
                    }
                }
            }
        }
    }

    /**
     * format Service Constructor node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatServiceConstructorNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG) && node.has(FormattingConstants.WS)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.SERVICE)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }
                }
            }

            // Handle formatting for service node.
            if (node.has("serviceNode")) {
                node.getAsJsonObject("serviceNode").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
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
            String indentation = this.getIndentation(formatConfig, false);

            // Preserve comments and new line that already available.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? this.getParentIndentation(formatConfig) : indentation);

            String packageAlias = "";
            if (node.has("packageAlias")) {
                packageAlias = node.getAsJsonObject("packageAlias").get("value").getAsString();
            }

            int colonIndex = 0;
            boolean packageAliasAvailableBeforeColon = false;
            for (int i = 0; i < ws.size(); i++) {
                JsonObject currentWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();

                    if (i == 0 && !text.equals(Tokens.COLON)) {
                        if (text.equals(packageAlias)) {
                            packageAliasAvailableBeforeColon = true;
                        }
                        if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                            + indentation);
                        } else if (formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt() > 0) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    } else if (text.equals(Tokens.COLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        ++colonIndex;
                    } else {
                        if (colonIndex == 1) {
                            if (packageAliasAvailableBeforeColon) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }
                        } else if (colonIndex > 1) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        } else {
                            if (ws.size() > 1) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * format Stream Action node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStreamActionNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve existing line separation.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate through ws and update whitespace.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.EQUAL_GT)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.CLOSING_PARENTHESES)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.OPENING_BRACE)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.CLOSING_BRACE)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }
                }
            }

            // Handle invokable body formatting
            if (node.has("invokableBody")) {
                node.getAsJsonObject("invokableBody").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }
        }
    }

    /**
     * format Streaming Input node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStreamingInputNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            // Handle stream reference formatting.
            if (node.has("streamReference")) {
                node.getAsJsonObject("streamReference").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            if (node.has("beforeStreamingCondition")) {
                node.getAsJsonObject("beforeStreamingCondition").add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            if (node.has("preFunctionInvocations")) {
                node.getAsJsonObject("preFunctionInvocations").add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            if (node.has("windowClause")) {
                node.getAsJsonObject("windowClause").add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            if (node.has("postFunctionInvocations")) {
                node.getAsJsonObject("postFunctionInvocations").add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            if (node.has("afterStreamingCondition")) {
                node.getAsJsonObject("afterStreamingCondition").add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            if (node.has("alias")) {
                if (node.has(FormattingConstants.WS)) {
                    JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                    String indentationOfParent = this.getParentIndentation(formatConfig);
                    this.preserveHeight(ws, indentationOfParent);

                    for (JsonElement wsItem : ws) {
                        JsonObject currentWS = wsItem.getAsJsonObject();
                        if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                            String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                            if (text.equals(Tokens.AS)) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }
                        }
                    }
                }

                node.getAsJsonObject("alias").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }
        }
    }

    /**
     * format Streaming Query node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStreamingQueryNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve available line separation.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and update node's whitespaces.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.FROM)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }
                }
            }

            // Handle streaming input formatting.
            if (node.has("streamingInput")) {
                JsonObject streamingInput = node.getAsJsonObject("streamingInput");
                JsonObject streamingInputFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentation), true);
                streamingInput.add(FormattingConstants.FORMATTING_CONFIG, streamingInputFormatConfig);
            }

            if (node.has("joiningInput")) {
                JsonObject joiningInput = node.getAsJsonObject("joiningInput");
                JsonObject joiningInputFormatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(indentation), false,
                        this.getWhiteSpaceCount(indentationOfParent),
                        true);
                joiningInput.add(FormattingConstants.FORMATTING_CONFIG, joiningInputFormatConfig);
            }

            // Handle pattern clause formatting.
            if (node.has("patternClause")) {
                JsonObject patternClause = node.getAsJsonObject("patternClause");
                JsonObject patternClauseFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                        this.getWhiteSpaceCount(indentation), false);
                patternClause.add(FormattingConstants.FORMATTING_CONFIG, patternClauseFormatConfig);
            }

            // Handle select clause formatting.
            if (node.has("selectClause")) {
                JsonObject selectClause = node.getAsJsonObject("selectClause");
                JsonObject selectClauseFormatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(indentation), false,
                        this.getWhiteSpaceCount(indentationOfParent),
                        true);
                selectClause.add(FormattingConstants.FORMATTING_CONFIG, selectClauseFormatConfig);
            }

            // Handle order by clause formatting.
            if (node.has("orderbyClause")) {
                JsonObject orderbyClause = node.getAsJsonObject("orderbyClause");
                JsonObject orderbyClauseFormatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(indentation), false,
                        this.getWhiteSpaceCount(indentationOfParent),
                        true);
                orderbyClause.add(FormattingConstants.FORMATTING_CONFIG, orderbyClauseFormatConfig);
            }

            // Handle output rate limit node formatting.
            if (node.has("outputRateLimitNode")) {
                JsonObject outputRateLimitNode = node.getAsJsonObject("outputRateLimitNode");
                JsonObject outputRateLimitNodeFormatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(indentation), false,
                        this.getWhiteSpaceCount(indentationOfParent),
                        true);
                outputRateLimitNode.add(FormattingConstants.FORMATTING_CONFIG, outputRateLimitNodeFormatConfig);
            }

            if (node.has("streamingAction")) {
                JsonObject streamingAction = node.getAsJsonObject("streamingAction");
                JsonObject streamingActionFormatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(indentation), false,
                        this.getWhiteSpaceCount(indentationOfParent),
                        true);
                streamingAction.add(FormattingConstants.FORMATTING_CONFIG, streamingActionFormatConfig);
            }
        }
    }

    /**
     * format String Template Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatStringTemplateLiteralNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate through whitespace and format.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if (text.contains("string") && text.contains("`")) {
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }
                    // Since template literal start is in a single text formatting it to the correct format
                    // is done by replacing the text.
                    currentWS.addProperty(FormattingConstants.TEXT, "string `");
                } else if (text.equals("`")
                        && this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            }

            // Handle expressions format
            if (node.has(FormattingConstants.EXPRESSIONS)) {
                JsonArray expressions = node.getAsJsonArray(FormattingConstants.EXPRESSIONS);
                // Every three expression is related to
                // Start of the template expression
                // expression of the template expression
                // end brace of the template expression
                for (JsonElement expressionItem : expressions) {
                    JsonObject expression = expressionItem.getAsJsonObject();
                    expression.add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 0, 0, false,
                                    this.getWhiteSpaceCount(indentationOfParent), true));

                }
            }
        }
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
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

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
                JsonObject identifierWS = ws.get(0).getAsJsonObject();
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
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);
            String indentation = indentWithParentIndentation + FormattingConstants.SPACE_TAB;

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentWithParentIndentation));

            this.preserveHeight(ws, indentWithParentIndentation);

            // Iterate and update whitespaces for the node.
            int openBracesCount = 0;
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                // Update whitespace for table keyword.
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.TABLE)) {
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }

                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.COMMA)) {
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
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.OPENING_BRACE) ||
                        currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.OPENING_BRACKET)) {
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
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.CLOSING_BRACE)) {
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
                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.CLOSING_BRACKET)) {
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


                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.OPENING_BRACE)
                        || currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.OPENING_BRACKET)) {
                    if (openBracesCount > 0) {
                        openBracesCount++;
                    } else {
                        openBracesCount = 1;
                    }
                }

                if (currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.CLOSING_BRACE)
                        || currentWS.get(FormattingConstants.TEXT).getAsString().equals(Tokens.CLOSING_BRACKET)) {
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
                            this.getWhiteSpaceCount(indentation), false);
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
     * format Table Query node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTableQueryNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getIndentation(formatConfig, true);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve available line separators.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and update whitespaces for the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.FROM)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    }
                }
            }

            // Handle select clause node formatting.
            if (node.has("selectClauseNode")) {
                node.getAsJsonObject("selectClauseNode").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentationOfParent), false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            // Handle streaming input node formatting.
            if (node.has("streamingInput")) {
                node.getAsJsonObject("streamingInput").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1,
                                0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            // Handle join streaming input formatting.
            if (node.has("joinStreamingInput")) {
                node.getAsJsonObject("joinStreamingInput").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentationOfParent), false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            // Handle order by node formatting.
            if (node.has("orderByNode")) {
                node.getAsJsonObject("orderByNode").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentationOfParent), false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            // Handle order by node formatting.
            if (node.has("limitClause")) {
                node.getAsJsonObject("limitClause").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentationOfParent), false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format Table Query Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTableQueryExpressionNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            if (node.has("tableQuery")) {
                if (node.has("isExpression")) {
                    node.getAsJsonObject("tableQuery").addProperty("isExpression",
                            node.get("isExpression").getAsBoolean());
                }

                node.getAsJsonObject("tableQuery").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }
        }
    }

    /**
     * format Ternary Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTernaryExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate through whitespaces and format.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.QUESTION_MARK) || text.equals(Tokens.COLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Handle condition's formatting.
            if (node.has("condition")) {
                node.getAsJsonObject("condition").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            // Handle then expression's formatting.
            if (node.has("thenExpression")) {
                node.getAsJsonObject("thenExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            // Handle else expression's formatting.
            if (node.has("elseExpression")) {
                node.getAsJsonObject("elseExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
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
            String indentation = this.getIndentation(formatConfig, true);

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, indentation);

            // Update transaction and retry whitespaces.
            boolean isRetryBody = false;
            boolean isCommittedBody = false;
            boolean isAbortedBody = false;
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.TRANSACTION)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.ONRETRY)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        isRetryBody = true;
                        isAbortedBody = false;
                        isCommittedBody = false;
                    } else if (text.equals(Tokens.ABORTED)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        isRetryBody = false;
                        isAbortedBody = true;
                        isCommittedBody = false;
                    } else if (text.equals(Tokens.COMMITTED)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        isRetryBody = false;
                        isAbortedBody = false;
                        isCommittedBody = true;
                    } else if (text.equals(Tokens.OPENING_BRACE)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.CLOSING_BRACE)) {
                        if (isRetryBody) {
                            modifyBlockClosingBrace(node, indentation, currentWS, "onRetryBody", false);
                        } else if (isAbortedBody) {
                            modifyBlockClosingBrace(node, indentation, currentWS, "abortedBody", false);
                        } else if (isCommittedBody) {
                            modifyBlockClosingBrace(node, indentation, currentWS, "committedBody", false);
                        } else {
                            modifyBlockClosingBrace(node, indentation, currentWS, "transactionBody", false);
                        }
                    } else if (text.equals(Tokens.WITH)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.RETRIES)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.ONABORT)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.ONCOMMIT)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            if (node.has("transactionBody")) {
                modifyConstructBody(node.getAsJsonObject("transactionBody"), indentation, indentation);
            }

            if (node.has("onRetryBody")) {
                modifyConstructBody(node.getAsJsonObject("onRetryBody"), indentation, indentation);
            }

            if (node.has("committedBody")) {
                modifyConstructBody(node.getAsJsonObject("committedBody"), indentation, indentation);
            }

            if (node.has("abortedBody")) {
                modifyConstructBody(node.getAsJsonObject("abortedBody"), indentation, indentation);
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
     * format Trap Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTrapExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, indentationOfParent);

            // Update the whitespace for trap keyword.
            JsonObject trapKeywordWS = ws.get(0).getAsJsonObject();
            if (this.noHeightAvailable(trapKeywordWS.get(FormattingConstants.WS).getAsString())) {
                String text = trapKeywordWS.get(FormattingConstants.TEXT).getAsString();
                if (text.equals(Tokens.TRAP)) {
                    trapKeywordWS.addProperty(FormattingConstants.WS,
                            this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                }
            }

            // Handle the whitespaces for expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent),
                                formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()));
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
            String indentation = this.getIndentation(formatConfig, true);
            boolean declaredWithVar = node.has("declaredWithVar")
                    && node.get("declaredWithVar").getAsBoolean();

            this.preserveHeight(ws, indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.VAR)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                        indentation);
                    } else if (text.equals(Tokens.OPENING_PARENTHESES) || text.equals(Tokens.OPENING_BRACKET)) {
                        if (declaredWithVar) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                            indentation);
                        }
                    } else if (text.equals(Tokens.ELLIPSIS)) {
                        if (node.has("variableRefs")
                                && node.getAsJsonArray("variableRefs").size() > 0) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    } else if (text.equals(Tokens.CLOSING_PARENTHESES) || text.equals(Tokens.SEMICOLON)
                            || text.equals(Tokens.CLOSING_BRACKET) || text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Update variable references' whitespaces.
            if (node.has("variableRefs")) {
                JsonArray varRefs = node.getAsJsonArray("variableRefs");
                modifyVariableReferences(formatConfig, indentation, varRefs);
            }

            // Update rest variable.
            if (node.has("restParam")) {
                JsonObject restParamFormatConfig = this.getFormattingConfig(0, 0, 0,
                        false, this.getWhiteSpaceCount(indentation), true);
                node.getAsJsonObject("restParam").add(FormattingConstants.FORMATTING_CONFIG, restParamFormatConfig);
            }

            // Update whitespace for expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                JsonObject expressionFormatConfig = this.getFormattingConfig(0, 1,
                        formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(), false,
                        this.getWhiteSpaceCount(indentation), true);
                expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
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
            String indentation = this.getIndentation(formatConfig, true);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();
            boolean isGrouped = node.has(FormattingConstants.GROUPED)
                    && node.get(FormattingConstants.GROUPED).getAsBoolean();

            // Preserve available new lines and indent.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and update whitespaces for the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                        indentation);
                    } else if (text.equals(Tokens.OPENING_BRACKET)) {
                        if (isGrouped) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                            indentation);
                        }
                    } else if (text.equals(Tokens.CLOSING_BRACKET) || text.equals(Tokens.COMMA)
                            || text.equals(Tokens.CLOSING_PARENTHESES) || text.equals(Tokens.ELLIPSIS)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            JsonArray memberTypeNodes = null;
            // Update the whitespaces for member types in tuple.
            if (node.has("memberTypeNodes")) {
                memberTypeNodes = node.getAsJsonArray("memberTypeNodes");
                JsonObject memberFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                        this.getWhiteSpaceCount(indentationOfParent), true);
                modifyVariableReferences(memberFormatConfig, indentation, memberTypeNodes);
            }

            if (node.has("restParamType")) {
                JsonObject restParamType = node.getAsJsonObject("restParamType");
                JsonObject restParamFormatConfig;
                if (memberTypeNodes != null && memberTypeNodes.size() > 0) {
                    restParamFormatConfig = this.getFormattingConfig(0, 1, 0, false,
                            this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation), true);
                } else {
                    restParamFormatConfig = this.getFormattingConfig(0, 0, 0, false,
                            this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation), true);
                }
                restParamType.add(FormattingConstants.FORMATTING_CONFIG, restParamFormatConfig);
            }
        }
    }

    /**
     * format Tuple variable node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTupleVariableNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                String indentation = this.getIndentation(formatConfig, true);
                String indentWithParentIndentation = this.getParentIndentation(formatConfig);
                boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                        .getAsBoolean();

                node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                        this.getWhiteSpaceCount(indentation));

                this.preserveHeight(ws, useParentIndentation ? indentWithParentIndentation : indentation);

                // Update the record or public keyword whitespaces.
                JsonObject firstKeywordWS = ws.get(0).getAsJsonObject();
                boolean isFirstKeywordExist = false;

                // Format type node
                if (node.has(FormattingConstants.TYPE_NODE)) {
                    JsonObject typeNode = node.getAsJsonObject(FormattingConstants.TYPE_NODE);
                    JsonObject typeFormatConfig;

                    if ((node.has(Tokens.FINAL) &&
                            node.get(Tokens.FINAL).getAsBoolean())
                            || (node.has(Tokens.PUBLIC) &&
                            node.get(Tokens.PUBLIC).getAsBoolean() &&
                            firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                    .equals(Tokens.PUBLIC))
                            || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                            .equals(Tokens.CONST)
                            || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                            .equals(Tokens.VAR)) {
                        isFirstKeywordExist = true;

                        if (this.noHeightAvailable(firstKeywordWS.get(FormattingConstants.WS).getAsString())) {

                            firstKeywordWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                            .getAsInt()) + indentation);
                        }
                    } else {
                        typeFormatConfig = this.getFormattingConfig(
                                formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                                this.getWhiteSpaceCount(indentation), false);
                        typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeFormatConfig);

                    }
                } else {
                    if ((node.has(Tokens.FINAL) &&
                            node.get(Tokens.FINAL).getAsBoolean())
                            || (node.has(Tokens.PUBLIC) &&
                            node.get(Tokens.PUBLIC).getAsBoolean() &&
                            firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                    .equals(Tokens.PUBLIC))
                            || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                            .equals("const")
                            || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                            .equals("var")) {
                        isFirstKeywordExist = true;
                        if (this.noHeightAvailable(firstKeywordWS.get(FormattingConstants.WS).getAsString())) {
                            firstKeywordWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                            .getAsInt()) + indentation);
                        }
                    }
                }

                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                        if (text.equals(Tokens.OPENING_BRACKET)) {
                            if (isFirstKeywordExist
                                    || (node.has(FormattingConstants.IS_VAR_EXISTS)
                                    && node.get(FormattingConstants.IS_VAR_EXISTS).getAsBoolean())) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS,
                                        this.getWhiteSpaces(formatConfig
                                                .get(FormattingConstants.SPACE_COUNT).getAsInt()));
                            }
                        } else if (text.equals(Tokens.CLOSING_BRACKET) || text.equals(Tokens.COMMA)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    }
                }

                JsonArray variables = null;
                // Update whitespaces of parameters.
                if (node.has("variables")) {
                    variables = node.getAsJsonArray("variables");
                    iterateAndFormatMembers(indentation, variables);
                }

                if (node.has("annotationAttachments")) {
                    JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                    for (JsonElement annotationAttachment : annotationAttachments) {
                        JsonObject annotationAttachmentFormattingConfig = this.getFormattingConfig(1, 0,
                                this.getWhiteSpaceCount(indentation), false,
                                this.getWhiteSpaceCount(indentation), false);
                        annotationAttachment.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG,
                                annotationAttachmentFormattingConfig);
                    }
                }

                if (node.has("restVariable")) {
                    JsonObject restVariable = node.getAsJsonObject("restVariable");
                    JsonObject restParamFormatConfig;
                    if (variables != null && variables.size() > 0) {
                        restParamFormatConfig = this.getFormattingConfig(0, 1,
                                this.getWhiteSpaceCount(indentation), false,
                                this.getWhiteSpaceCount(useParentIndentation
                                        ? indentWithParentIndentation : indentation), true);
                    } else {
                        restParamFormatConfig = this.getFormattingConfig(0, 0,
                                this.getWhiteSpaceCount(indentation), false,
                                this.getWhiteSpaceCount(useParentIndentation
                                        ? indentWithParentIndentation : indentation), true);
                    }
                    restVariable.add(FormattingConstants.FORMATTING_CONFIG, restParamFormatConfig);
                }
            } else if (node.has(FormattingConstants.TYPE_NODE)) {
                node.getAsJsonObject(FormattingConstants.TYPE_NODE)
                        .add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }
        }
    }

    /**
     * format Tuple Variable Ref node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTupleVariableRefNode(JsonObject node) {
        // TODO: fix formatting for Tuple Variable Ref.
        this.skipFormatting(node, true);
    }

    /**
     * format Type Conversion Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeConversionExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, true);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean isAnnotationAvailable = false;

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.LESS_THAN)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                        + indentation);
                    } else if (text.equals(Tokens.GREATER_THAN)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Handle annotation attachments' formatting.
            if (node.has(FormattingConstants.ANNOTATION_ATTACHMENTS)) {
                JsonArray annotations = node.getAsJsonArray(FormattingConstants.ANNOTATION_ATTACHMENTS);

                if (annotations.size() > 0) {
                    isAnnotationAvailable = true;
                }

                for (int i = 0; i < annotations.size(); i++) {
                    JsonObject annotationAttachment = annotations.get(i).getAsJsonObject();
                    JsonObject annotationAttachmentFormattingConfig;
                    if (i == 0) {
                        annotationAttachmentFormattingConfig = this.getFormattingConfig(0, 0,
                                this.getWhiteSpaceCount(indentation), false,
                                this.getWhiteSpaceCount(indentationOfParent), true);
                    } else {
                        annotationAttachmentFormattingConfig = this.getFormattingConfig(0, 1,
                                this.getWhiteSpaceCount(indentation), false,
                                this.getWhiteSpaceCount(indentationOfParent), true);
                    }

                    annotationAttachment.add(FormattingConstants.FORMATTING_CONFIG,
                            annotationAttachmentFormattingConfig);
                }
            }

            // Handle whitespaces for expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            // Handle whitespaces for typeNode.
            if (node.has(FormattingConstants.TYPE_NODE)) {
                if (isAnnotationAvailable) {
                    node.getAsJsonObject(FormattingConstants.TYPE_NODE).add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 1, 0, false,
                                    this.getWhiteSpaceCount(indentationOfParent), true));
                } else {
                    node.getAsJsonObject(FormattingConstants.TYPE_NODE).add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 0, 0, false,
                                    this.getWhiteSpaceCount(indentationOfParent), true));
                }
            }
        }
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
            String indentation = this.getIndentation(formatConfig, true);

            this.preserveHeight(ws, indentation);
            boolean isEnum = true;

            // Handles whitespace for type def.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();

                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();

                    // Update the type or public keywords whitespace.
                    if (text.equals(Tokens.PUBLIC)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                        indentation);
                    } else if (text.equals(FormattingConstants.TYPE)) {
                        if (node.has(Tokens.PUBLIC)
                                && node.get(Tokens.PUBLIC).getAsBoolean()) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                            indentation);
                        }
                    } else if (text.equals(Tokens.OBJECT) || text.equals(Tokens.RECORD)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.OPENING_BRACE)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        isEnum = false;
                    } else if (text.equals(Tokens.CLOSING_BRACE)) {
                        if (node.has(FormattingConstants.TYPE_NODE)
                                && node.getAsJsonObject(FormattingConstants.TYPE_NODE)
                                .getAsJsonArray(FormattingConstants.FIELDS).size() <= 0) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
                        }
                    } else if (text.equals(Tokens.SEMICOLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Handle the whitespace for type node.
            if (node.has(FormattingConstants.TYPE_NODE)) {
                if (isEnum) {
                    JsonObject typeNodeFormatConfig = this.getFormattingConfig(0, 1,
                            0, false, this.getWhiteSpaceCount(indentation), false);
                    node.getAsJsonObject(FormattingConstants.TYPE_NODE)
                            .add(FormattingConstants.FORMATTING_CONFIG, typeNodeFormatConfig);
                } else {
                    JsonObject typeNodeFormatConfig = this.getFormattingConfig(1, 0,
                            this.getWhiteSpaceCount(indentation), true, this.getWhiteSpaceCount(indentation),
                            false);
                    node.getAsJsonObject(FormattingConstants.TYPE_NODE)
                            .add(FormattingConstants.FORMATTING_CONFIG, typeNodeFormatConfig);
                }
            }

            // Update whitespaces of annotation attachments.
            modifyAnnotationAttachments(node, formatConfig, indentation);

            // Update whitespaces of markdown documentation attachments.
            modifyMarkdownDocumentation(node, formatConfig, indentation);
        }
    }

    /**
     * format Typedesc Expression node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypedescExpressionNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG) && node.has(FormattingConstants.TYPE_NODE)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            if (node.has(FormattingConstants.WS) && node.has("isObject")
                    && node.get("isObject").getAsBoolean()) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                String indentation = this.getIndentation(formatConfig, false);
                String indentationOfParent = this.getParentIndentation(formatConfig);
                boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                        .getAsBoolean();

                this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

                // Check whether fields have placed on new lines.
                boolean lineSeparationAvailable = false;

                if (node.getAsJsonObject(FormattingConstants.TYPE_NODE)
                        .get(FormattingConstants.IS_ANON_TYPE).getAsBoolean()) {
                    JsonObject anonType = node.getAsJsonObject(FormattingConstants.TYPE_NODE)
                            .getAsJsonObject(FormattingConstants.ANON_TYPE);
                    JsonArray fields = anonType.getAsJsonArray(FormattingConstants.FIELDS);
                    JsonArray functions = anonType.getAsJsonArray("functions");

                    lineSeparationAvailable = this.isMemberOnNewLine(fields);
                    if (functions.size() > 0 || anonType.has("initFunction")) {
                        lineSeparationAvailable = true;
                    }
                }

                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                        if (text.equals(Tokens.OBJECT)) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        } else if (text.equals(Tokens.OPENING_BRACE)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else if (text.equals(Tokens.CLOSING_BRACE)) {
                            if (lineSeparationAvailable) {
                                currentWS.addProperty(FormattingConstants.WS,
                                        FormattingConstants.NEW_LINE + indentationOfParent);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            }
                        }
                    }
                }
            }

            node.getAsJsonObject(FormattingConstants.TYPE_NODE)
                    .add(FormattingConstants.FORMATTING_CONFIG, formatConfig);

        }
    }

    /**
     * format Type Init Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeInitExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve available new lines in node.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and update whitespaces for node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.NEW)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    } else if (text.equals(Tokens.OPENING_PARENTHESES)) {
                        if (node.has(FormattingConstants.TYPE)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }
                    } else if (text.equals(Tokens.COMMA) || text.equals(Tokens.CLOSING_PARENTHESES)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Iterate and format expressions.
            if (node.has(FormattingConstants.EXPRESSIONS)) {
                JsonArray expressions = node.getAsJsonArray(FormattingConstants.EXPRESSIONS);
                iterateAndFormatMembers(indentation.isEmpty() ? indentationOfParent : indentation,
                        expressions);
            }

            // Handle type name formatting.
            if (node.has(FormattingConstants.TYPE)) {
                node.getAsJsonObject(FormattingConstants.TYPE).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format Type Test Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatTypeTestExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate through whitespaces for this node and update.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.IS)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            // Handle expression's formatting.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            // Handle typeNode's formatting.
            if (node.has(FormattingConstants.TYPE_NODE)) {
                node.getAsJsonObject(FormattingConstants.TYPE_NODE).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format Unary Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatUnaryExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve available new lines.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and update whitespaces.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.TYPEOF) || text.equals(Tokens.BIT_COMPLEMENT) || text.equals(Tokens.ADD)
                            || text.equals(Tokens.SUB) || text.equals(Tokens.UNTAINT) || text.equals(Tokens.NOT)) {
                        if (formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                            + indentation);
                        } else {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        }
                    }
                }
            }

            // Handle expression formatting.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                String operatorKind = node.get("operatorKind").getAsString();

                JsonObject expressionFormatConfig = null;
                // If operator kind is +, -, !, ~ then no space added to the expression.
                // Else if operator kind is untaint, typeof then single space added to the expression.
                if (operatorKind.equals(Tokens.ADD) || operatorKind.equals(Tokens.SUB)
                        || operatorKind.equals(Tokens.NOT) || operatorKind.equals(Tokens.BIT_COMPLEMENT)) {
                    expressionFormatConfig = this.getFormattingConfig(0, 0, 0,
                            false, this.getWhiteSpaceCount(indentationOfParent), true);
                } else if (operatorKind.equals(Tokens.UNTAINT) || operatorKind.equals(Tokens.TYPEOF)) {
                    expressionFormatConfig = this.getFormattingConfig(0, 1, 0,
                            false, this.getWhiteSpaceCount(indentationOfParent), true);
                }

                if (expressionFormatConfig != null) {
                    expression.add(FormattingConstants.FORMATTING_CONFIG, expressionFormatConfig);
                }
            }
        }
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
            String indentation = this.getIndentation(formatConfig, true);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

                // Iterate through WS to update horizontal whitespaces.
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                        // Update opening parentheses whitespace.
                        if (text.equals(Tokens.OPENING_PARENTHESES)) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                            indentation);
                        } else if (text.equals("|")) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else if (text.equals(Tokens.QUESTION_MARK)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else if (currentWS.get(FormattingConstants.TEXT).getAsString()
                                .equals(Tokens.CLOSING_PARENTHESES)) {
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
                            .get(FormattingConstants.KIND).getAsString().equals("TypeDefinition")) {
                        if (isGrouped) {
                            memberTypeFormatConfig = this.getFormattingConfig(0, 0,
                                    0, false,
                                    this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation),
                                    true);
                        } else {
                            memberTypeFormatConfig = this.getFormattingConfig(
                                    formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(),
                                    formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                                    formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean(),
                                    this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation),
                                    false);
                        }
                    } else {
                        memberTypeFormatConfig = this.getFormattingConfig(0, 1,
                                0, false,
                                this.getWhiteSpaceCount(useParentIndentation ? indentationOfParent : indentation),
                                true);
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
                String indentation = this.getIndentation(formatConfig, false);

                node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                        this.getWhiteSpaceCount(indentation +
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt())));

                this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean() ?
                        getParentIndentation(formatConfig) : indentation);

                boolean isPackageAliasExist = false;
                boolean isGrouped = node.has("grouped") && node.get("grouped").getAsBoolean();
                for (int i = 0; i < ws.size(); i++) {
                    JsonObject currentWS = ws.get(i).getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                        if (i == 0) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                            .getAsInt()) + indentation + this.getWhiteSpaces(formatConfig
                                            .get(FormattingConstants.SPACE_COUNT).getAsInt()));
                        } else {
                            if (text.equals(Tokens.COLON)) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                                isPackageAliasExist = true;
                            } else if (text.equals(Tokens.QUESTION_MARK) || (text.equals(Tokens.CLOSING_PARENTHESES)
                                    && isGrouped)) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            } else {
                                if (isPackageAliasExist) {
                                    currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                                    isPackageAliasExist = false;
                                } else if (isGrouped) {
                                    currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                                } else {
                                    currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                                }
                            }
                        }
                    }
                }
            } else if (node.has(FormattingConstants.IS_ANON_TYPE) &&
                    node.get(FormattingConstants.IS_ANON_TYPE).getAsBoolean()) {
                JsonObject anonType = node.getAsJsonObject(FormattingConstants.ANON_TYPE);
                anonType.add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
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
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            this.preserveHeight(ws, useParentIndentation
                    ? indentationOfParent : indentation);

            for (int i = 0; i < ws.size(); i++) {
                JsonObject currentWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (i == 0) {
                        currentWS.addProperty(FormattingConstants.WS, this.getNewLines(formatConfig
                                .get(FormattingConstants.NEW_LINE_COUNT).getAsInt()) +
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                                indentation);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS,
                                FormattingConstants.EMPTY_SPACE);
                    }
                }
            }
        }
    }

    /**
     * format Variable Def node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatVariableDefNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            boolean isVarExists = false;

            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                String indentation = this.getIndentation(formatConfig, false);

                this.preserveHeight(ws, indentation);
                JsonObject firstKeyword = ws.get(0).getAsJsonObject();
                if (firstKeyword.get(FormattingConstants.TEXT).getAsString().equals(Tokens.VAR)) {
                    isVarExists = true;
                }


                // Iterate and update whitespaces for variable def.
                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        if (text.equals("var")) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                            .getAsInt()) + indentation);
                        }

                        if (text.equals(Tokens.EQUAL)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        }

                        if (text.equals(Tokens.SEMICOLON)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    }
                }
            }

            if (node.has(FormattingConstants.VARIABLE)) {
                JsonObject variable = node.getAsJsonObject(FormattingConstants.VARIABLE);
                if (node.has("param")) {
                    variable.add("param", node.get("param"));
                }
                variable.add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
                if (isVarExists) {
                    variable.addProperty(FormattingConstants.IS_VAR_EXISTS, true);
                }
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
                String indentation = this.getIndentation(formatConfig, true);
                String indentWithParentIndentation = this.getParentIndentation(formatConfig);

                node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                        this.getWhiteSpaceCount(indentation));

                this.preserveHeight(ws, indentWithParentIndentation);

                // Check whether fields have placed on new lines.
                boolean lineSeparationAvailable = false;
                if (node.has("symbolType")
                        && node.get("symbolType").getAsString().equals("object")
                        && node.has(FormattingConstants.IS_ANON_TYPE)
                        && node.get(FormattingConstants.IS_ANON_TYPE).getAsBoolean()) {
                    JsonObject anonType = node.getAsJsonObject(FormattingConstants.TYPE_NODE)
                            .getAsJsonObject(FormattingConstants.ANON_TYPE);
                    JsonArray fields = anonType.getAsJsonArray(FormattingConstants.FIELDS);
                    JsonArray functions = anonType.getAsJsonArray("functions");

                    lineSeparationAvailable = this.isMemberOnNewLine(fields);
                    if (functions.size() > 0 || anonType.has("initFunction")) {
                        lineSeparationAvailable = true;
                    }
                }

                // Update the record or public keyword whitespaces.
                boolean hasFirstKeyword = false;
                JsonObject firstKeywordWS = ws.get(0).getAsJsonObject();
                String firstKeyword = firstKeywordWS.get(FormattingConstants.TEXT).getAsString();
                if (firstKeyword.equals(Tokens.FINAL)
                        || firstKeyword.equals(Tokens.PUBLIC)
                        || firstKeyword.equals(Tokens.PRIVATE)
                        || firstKeyword.equals(Tokens.CONST)
                        || firstKeyword.equals(Tokens.VAR)
                        || firstKeyword.equals(Tokens.CLIENT)
                        || firstKeyword.equals(Tokens.LISTENER)
                        || firstKeyword.equals(Tokens.ABSTRACT)
                        || firstKeyword.equals(Tokens.CHANNEL)
                        || firstKeyword.equals(Tokens.OBJECT)) {
                    hasFirstKeyword = true;
                }

                boolean updatedFirstKeyword = false;
                boolean isColonAvailable = false;
                for (int i = 0; i < ws.size(); i++) {
                    JsonObject currentWS = ws.get(i).getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                        if (text.equals(Tokens.FINAL)
                                || text.equals(Tokens.PUBLIC)
                                || text.equals(Tokens.PRIVATE)
                                || text.equals(Tokens.CONST)
                                || text.equals(Tokens.VAR)
                                || text.equals(Tokens.CLIENT)
                                || text.equals(Tokens.LISTENER)
                                || text.equals(Tokens.ABSTRACT)
                                || text.equals(Tokens.CHANNEL)
                                || text.equals(Tokens.OBJECT)) {
                            if (updatedFirstKeyword) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            } else if (text.equals(firstKeywordWS.get(FormattingConstants.TEXT).getAsString())) {
                                currentWS.addProperty(FormattingConstants.WS,
                                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                                .getAsInt()) + indentation);
                                updatedFirstKeyword = true;
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            }
                        } else if (text.equals(Tokens.SEMICOLON) || text.equals(Tokens.QUESTION_MARK)
                                || text.equals(Tokens.COMMA)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        } else if (text.equals(Tokens.OPENING_BRACE)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else if (text.equals(Tokens.CLOSING_BRACE)) {
                            if (!lineSeparationAvailable) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            } else {
                                currentWS.addProperty(FormattingConstants.WS,
                                        FormattingConstants.NEW_LINE + indentation);
                            }
                        } else if (text.equals(Tokens.ELLIPSIS)) {
                            if (!node.has(FormattingConstants.TYPE_NODE) && firstKeyword.equals(Tokens.ELLIPSIS)) {
                                currentWS.addProperty(FormattingConstants.WS, this.getWhiteSpaces(formatConfig
                                        .get(FormattingConstants.SPACE_COUNT).getAsInt()));
                            } else {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            }
                        } else if (text.equals(Tokens.EQUAL)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else if (text.equals(Tokens.COLON)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            isColonAvailable = true;
                        } else {
                            if (!node.has(FormattingConstants.TYPE_NODE) && firstKeyword.equals(Tokens.ELLIPSIS)) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                            } else if (node.has(FormattingConstants.IS_ANON_TYPE)
                                    && node.get(FormattingConstants.IS_ANON_TYPE).getAsBoolean()) {
                                currentWS.addProperty(FormattingConstants.WS,
                                        FormattingConstants.SINGLE_SPACE);
                            } else if (i == 0 && formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt() > 0
                                    && !node.has(FormattingConstants.TYPE_NODE)) {
                                currentWS.addProperty(FormattingConstants.WS,
                                        this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                                .getAsInt()) + indentation);
                            } else if (isColonAvailable) {
                                currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                            } else if ((!node.has(FormattingConstants.TYPE_NODE)
                                    && !hasFirstKeyword)
                                    || (node.has("arrowExprParam")
                                    && node.get("arrowExprParam").getAsBoolean())) {
                                currentWS.addProperty(FormattingConstants.WS,
                                        this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                                .getAsInt()));
                            } else {
                                currentWS.addProperty(FormattingConstants.WS,
                                        formatConfig
                                                .get(FormattingConstants.SPACE_COUNT).getAsInt() > 0
                                                ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT)
                                                .getAsInt())
                                                : FormattingConstants.SINGLE_SPACE);
                            }
                        }
                    }
                }

                // Format type node
                if (node.has(FormattingConstants.TYPE_NODE)) {
                    JsonObject typeNode = node.getAsJsonObject(FormattingConstants.TYPE_NODE);
                    JsonObject typeFormatConfig;

                    if (node.has(FormattingConstants.IS_ANON_TYPE)
                            && node.get(FormattingConstants.IS_ANON_TYPE).getAsBoolean()) {
                        // Update type node whitespace.
                        typeFormatConfig = this.getFormattingConfig(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                        .getAsInt(), formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt(),
                                this.getWhiteSpaceCount(indentation), false,
                                this.getWhiteSpaceCount(formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                        .getAsBoolean() ? indentWithParentIndentation : indentation),
                                formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                        .getAsBoolean());
                        typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeFormatConfig);
                    } else {
                        if ((node.has(Tokens.FINAL) &&
                                node.get(Tokens.FINAL).getAsBoolean())
                                || (node.has(Tokens.PUBLIC) &&
                                node.get(Tokens.PUBLIC).getAsBoolean() &&
                                firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                        .equals(Tokens.PUBLIC))
                                || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                .equals(Tokens.PRIVATE)
                                || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                .equals(Tokens.CONST)
                                || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                .equals(Tokens.VAR)
                                || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                .equals(Tokens.CLIENT)
                                || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                .equals(Tokens.LISTENER)
                                || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                .equals(Tokens.ABSTRACT)
                                || firstKeywordWS.get(FormattingConstants.TEXT).getAsString()
                                .equals(Tokens.CHANNEL)) {
                            typeFormatConfig = this.getFormattingConfig(0, 1, 0,
                                    false, this.getWhiteSpaceCount(formatConfig.get(FormattingConstants
                                            .USE_PARENT_INDENTATION).getAsBoolean() ? indentWithParentIndentation :
                                            indentation), formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                                            .getAsBoolean());
                            typeNode.add(FormattingConstants.FORMATTING_CONFIG, typeFormatConfig);
                        } else {
                            if (!(node.has("annotationAttachments")
                                    && node.getAsJsonArray("annotationAttachments").size() > 0)) {
                                typeNode.add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
                            }
                        }
                    }
                }

                if (node.has("initialExpression")) {
                    JsonObject initialExprFormattingConfig = this.getFormattingConfig(0, 1,
                            0, false, this.getWhiteSpaceCount(indentWithParentIndentation),
                            true);
                    node.getAsJsonObject("initialExpression").add(FormattingConstants.FORMATTING_CONFIG,
                            initialExprFormattingConfig);
                }

                if (node.has("annotationAttachments")) {
                    JsonArray annotationAttachments = node.getAsJsonArray("annotationAttachments");
                    for (int i = 0; i < annotationAttachments.size(); i++) {
                        JsonObject annotationAttachment = annotationAttachments.get(i).getAsJsonObject();
                        JsonObject annotationAttachmentFormattingConfig;
                        if (node.has("param") && node.get("param").getAsBoolean()) {
                            if (i == 0) {
                                annotationAttachmentFormattingConfig = this.getFormattingConfig(0, 0,
                                        this.getWhiteSpaceCount(indentation), false,
                                        this.getWhiteSpaceCount(indentWithParentIndentation), true);
                            } else {
                                annotationAttachmentFormattingConfig = this.getFormattingConfig(0, 1,
                                        this.getWhiteSpaceCount(indentation), false,
                                        this.getWhiteSpaceCount(indentWithParentIndentation), true);
                            }
                        } else {
                            annotationAttachmentFormattingConfig = this.getFormattingConfig(1, 0,
                                    this.getWhiteSpaceCount(indentation), false,
                                    this.getWhiteSpaceCount(indentWithParentIndentation), false);
                        }
                        annotationAttachment.getAsJsonObject().add(FormattingConstants.FORMATTING_CONFIG,
                                annotationAttachmentFormattingConfig);
                    }
                }
            } else if (node.has(FormattingConstants.TYPE_NODE)) {
                node.getAsJsonObject(FormattingConstants.TYPE_NODE).add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            } else if (node.has("worker") && node.get("worker").getAsBoolean()) {
                if (node.has("initialExpression")
                        && node.getAsJsonObject("initialExpression").get(FormattingConstants.KIND)
                        .getAsString().equals("Lambda")) {
                    node.getAsJsonObject("initialExpression").add(FormattingConstants.FORMATTING_CONFIG,
                            formatConfig);
                }
            }
        }

    }

    /**
     * format Wait Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWaitExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentWithParentIndentation = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, indentWithParentIndentation);

            // Update whitespaces for wait.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.WAIT) || text.equals(Tokens.OPENING_BRACE)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.CLOSING_BRACE) || text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            // Update key value pairs' whitespaces.
            if (node.has("keyValuePairs")) {
                JsonArray keyValuePairs = node.getAsJsonArray("keyValuePairs");
                this.iterateAndFormatMembers(indentWithParentIndentation, keyValuePairs);
            }

            // Update the expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                JsonObject expression = node.getAsJsonObject(FormattingConstants.EXPRESSION);
                expression.add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentWithParentIndentation), true));
            }
        }
    }

    /**
     * format Wait Literal Key Value node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWaitLiteralKeyValueNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG) && node.has(FormattingConstants.WS)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            String keyName = "";
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            if (node.has("key")) {
                JsonObject key = node.getAsJsonObject("key");
                keyName = key.get("value").getAsString();
            }

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.COLON)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else if (text.equals(keyName)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }
                }
            }

            if (node.has("value")) {
                JsonObject value = node.getAsJsonObject("value");
                value.add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format Where node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWhereNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG) && node.has(FormattingConstants.WS)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            // Preserve available line separators.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            // Iterate and update whitespaces for the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.WHERE)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }
                }
            }

            // Handle expression formatting.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
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
            String indentation = this.getIndentation(formatConfig, true);
            String indentationOfParent = this.getParentIndentation(formatConfig);

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
            modifyBlockClosingBrace(node, indentation, closingBraceWS, FormattingConstants.BODY, true);

            // Update condition whitespace.
            if (node.has("condition")) {
                JsonObject whileCondition = node.getAsJsonObject("condition");
                JsonObject whileConditionFormatConfig = this.getFormattingConfig(0, 1,
                        0, false, this.getWhiteSpaceCount(indentation), true);
                whileCondition.add(FormattingConstants.FORMATTING_CONFIG, whileConditionFormatConfig);
            }

            if (node.has(FormattingConstants.BODY)) {
                modifyConstructBody(node.getAsJsonObject(FormattingConstants.BODY),
                        indentation, indentationOfParent);
            }
        }
    }

    /**
     * format Window Clause node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWindowClauseNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.WINDOW)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    }
                }
            }

            if (node.has("functionInvocation")) {
                node.getAsJsonObject("functionInvocation").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }
        }
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
            String indentation = this.getIndentation(formatConfig, false);

            // Update the position start column of the node as to the indentation.
            node.getAsJsonObject(FormattingConstants.POSITION).addProperty(FormattingConstants.START_COLUMN,
                    this.getWhiteSpaceCount(indentation));

            // Preserve height and comments that already available.
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
                    closingBrace.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                }
            } else if (this.noHeightAvailable(ws.get(ws.size() - 1).getAsJsonObject()
                    .get(FormattingConstants.WS).getAsString())) {
                closingBrace.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
            }
        }
    }

    /**
     * format Worker Flush node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWorkerFlushNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve the already available new lines.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and format the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.FLUSH)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
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
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentationFromParent = this.getParentIndentation(formatConfig);

            this.preserveHeight(ws, indentationFromParent);

            // Iterate through ws accordingly.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.LEFT_ARROW)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    } else if (text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            if (node.has("keyExpression")) {
                node.getAsJsonObject("keyExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentationFromParent), false));
            }
        }
    }

    /**
     * format Worker Send node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWorkerSendNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG) && node.has(FormattingConstants.WS)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);

            this.preserveHeight(ws, indentation);

            // Iterate through ws and update accordingly.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.RIGHT_ARROW)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    } else if (text.equals(Tokens.SEMICOLON) || text.equals(Tokens.COMMA)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                    }
                }
            }

            if (node.has("keyExpression")) {
                node.getAsJsonObject("keyExpression").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 1, 0, false,
                                this.getWhiteSpaceCount(indentation), false));
            }

            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }
        }
    }

    /**
     * format Worker Sync Send node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatWorkerSyncSendNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            // Preserve available new lines.
            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            // Iterate and format the node.
            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                }
            }

            // Handle formatting of the expression.
            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }
        }
    }

    /**
     * format XML Attribute node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatXmlAttributeNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIdentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            this.preserveHeight(ws, useParentIdentation ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.EQUAL)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            if (node.has(FormattingConstants.NAME)) {
                node.getAsJsonObject(FormattingConstants.NAME).add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }

            if (node.has(FormattingConstants.VALUE)) {
                node.getAsJsonObject(FormattingConstants.VALUE).add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format XML Attribute Access Expr node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatXmlAttributeAccessExprNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);

            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                    if (text.equals(Tokens.AT)
                            || text.equals(Tokens.OPENING_BRACKET)
                            || text.equals(Tokens.CLOSING_BRACKET)) {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            if (node.has(FormattingConstants.EXPRESSION)) {
                node.getAsJsonObject(FormattingConstants.EXPRESSION).add(FormattingConstants.FORMATTING_CONFIG,
                        formatConfig);
            }

            if (node.has("index")) {
                node.getAsJsonObject("index").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format XML Comment Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatXmlCommentLiteralNode(JsonObject node) {
        modifyXMLLiteralNode(node);
    }

    /**
     * format XML Element Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatXmlElementLiteralNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            String startLiteral = "";
            if (node.has("startLiteral")) {
                startLiteral = node.get("startLiteral").getAsString();
            }

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if (text.equals(startLiteral)) {
                    currentWS.addProperty(FormattingConstants.TEXT, Tokens.XML_LITERAL_START);
                }
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (text.equals(startLiteral)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            if (node.has("startTagName")) {
                node.getAsJsonObject("startTagName").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            if (node.has("attributes")) {
                JsonArray attributes = node.getAsJsonArray("attributes");
                for (JsonElement attributeItem : attributes) {
                    JsonObject attribute = attributeItem.getAsJsonObject();
                    attribute.add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 1, 0, false,
                                    this.getWhiteSpaceCount(indentationOfParent), true));
                }
            }

            if (node.has("content")) {
                JsonArray contents = node.getAsJsonArray("content");
                for (JsonElement contentItem : contents) {
                    JsonObject content = contentItem.getAsJsonObject();
                    content.add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 0, 0, false,
                                    this.getWhiteSpaceCount(indentationOfParent), true));
                }
            }

            if (node.has("endTagName")) {
                node.getAsJsonObject("endTagName").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }
        }
    }

    /**
     * format XMLNS node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatXmlnsNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            if (node.has(FormattingConstants.WS)) {
                JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
                String indentation = this.getIndentation(formatConfig, false);
                String indentationOfParent = this.getParentIndentation(formatConfig);
                boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION)
                        .getAsBoolean();

                this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

                String prefix = "";
                if (node.has("prefix")) {
                    prefix = node.getAsJsonObject("prefix").get(FormattingConstants.VALUE).getAsString();
                }

                for (JsonElement wsItem : ws) {
                    JsonObject currentWS = wsItem.getAsJsonObject();
                    if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                        String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                        if (text.equals(Tokens.XMLNS)) {
                            currentWS.addProperty(FormattingConstants.WS,
                                    this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt())
                                            + indentation);
                        } else if (text.equals(Tokens.AS)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else if (text.equals(prefix)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.SINGLE_SPACE);
                        } else if (text.equals(Tokens.SEMICOLON)) {
                            currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                        }
                    }
                }

                if (node.has("namespaceURI")) {
                    node.getAsJsonObject("namespaceURI").add(FormattingConstants.FORMATTING_CONFIG,
                            this.getFormattingConfig(0, 1, 0, false,
                                    this.getWhiteSpaceCount(indentation), true));
                }
            } else if (node.has("namespaceDeclaration")) {
                node.getAsJsonObject("namespaceDeclaration").add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
            }
        }
    }

    /**
     * format XML PI Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatXmlPiLiteralNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            String startLiteral = "";
            if (node.has("startLiteral")) {
                startLiteral = node.get("startLiteral").getAsString();
            }

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if (text.equals(startLiteral)) {
                    currentWS.addProperty(FormattingConstants.TEXT, Tokens.XML_LITERAL_START);
                }
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (text.equals(startLiteral)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            if (node.has("target")) {
                node.getAsJsonObject("target").add(FormattingConstants.FORMATTING_CONFIG,
                        this.getFormattingConfig(0, 0, 0, false,
                                this.getWhiteSpaceCount(indentationOfParent), true));
            }

            if (node.has("dataTextFragments")) {
                JsonArray dataTextFragments = node.getAsJsonArray("dataTextFragments");
                for (JsonElement textFragmentItem : dataTextFragments) {
                    JsonObject textFragment = textFragmentItem.getAsJsonObject();
                    textFragment.add(FormattingConstants.FORMATTING_CONFIG, this.getFormattingConfig(0,
                            0, 0, false, this.getWhiteSpaceCount(indentationOfParent),
                            true));
                }
            }
        }
    }

    /**
     * format XML Qname node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatXmlQnameNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            for (int i = 0; i < ws.size(); i++) {
                JsonObject currentWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (i == 0) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }
        }
    }

    /**
     * format XML Quoted String node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatXmlQuotedStringNode(JsonObject node) {
        if (node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            if (node.has("textFragments")) {
                JsonArray textFragments = node.getAsJsonArray("textFragments");
                for (JsonElement textFragmentItem : textFragments) {
                    JsonObject textFragment = textFragmentItem.getAsJsonObject();
                    textFragment.add(FormattingConstants.FORMATTING_CONFIG, formatConfig);
                }
            }
        }
    }

    /**
     * format Xml Sequence Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatXmlSequenceLiteralNode(JsonObject node) {
        // TODO: fix formatting for Xml Sequence Literal.
        this.skipFormatting(node, true);
    }

    /**
     * format XML Text Literal node.
     *
     * @param node {JsonObject} node as json object
     */
    public void formatXmlTextLiteralNode(JsonObject node) {
        modifyXMLLiteralNode(node);
    }

    // --------- Util functions for the modifying node tree --------

    private void modifyConstructBody(JsonObject node, String indentation, String indentWithParentIndentation) {
        node.add(FormattingConstants.FORMATTING_CONFIG,
                this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(this.getWhiteSpaceCount(indentation) == 0
                                ? indentWithParentIndentation : indentation), true,
                        this.getWhiteSpaceCount(indentWithParentIndentation), false));

    }

    private void modifyXMLLiteralNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            String indentation = this.getIndentation(formatConfig, false);
            String indentationOfParent = this.getParentIndentation(formatConfig);
            boolean useParentIndentation = formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean();

            this.preserveHeight(ws, useParentIndentation ? indentationOfParent : indentation);

            String startLiteral = "";
            if (node.has("startLiteral")) {
                startLiteral = node.get("startLiteral").getAsString();
            }

            for (JsonElement wsItem : ws) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if (text.equals(startLiteral)) {
                    currentWS.addProperty(FormattingConstants.TEXT, Tokens.XML_LITERAL_START);
                }
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (text.equals(startLiteral)) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt()));
                    } else {
                        currentWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
                    }
                }
            }

            if (node.has("textFragments")) {
                JsonArray textFragments = node.getAsJsonArray("textFragments");
                for (JsonElement textFragmentItem : textFragments) {
                    JsonObject textFragment = textFragmentItem.getAsJsonObject();
                    textFragment.add(FormattingConstants.FORMATTING_CONFIG, this.getFormattingConfig(0,
                            0, 0, false, this.getWhiteSpaceCount(indentationOfParent),
                            true));
                }
            }
        }
    }

    private void modifyLiteralNode(JsonObject node) {
        if (node.has(FormattingConstants.WS) && node.has(FormattingConstants.FORMATTING_CONFIG)) {
            JsonArray ws = node.getAsJsonArray(FormattingConstants.WS);
            JsonObject formatConfig = node.getAsJsonObject(FormattingConstants.FORMATTING_CONFIG);
            String indentation = this.getIndentation(formatConfig, false);

            // Update whitespace for literal value.
            this.preserveHeight(ws, formatConfig.get(FormattingConstants.USE_PARENT_INDENTATION).getAsBoolean()
                    ? this.getParentIndentation(formatConfig) : indentation);

            for (int i = 0; i < ws.size(); i++) {
                JsonObject currentWS = ws.get(i).getAsJsonObject();
                if (this.noHeightAvailable(currentWS.get(FormattingConstants.WS).getAsString())) {
                    if (i == 0) {
                        currentWS.addProperty(FormattingConstants.WS,
                                this.getNewLines(formatConfig.get(FormattingConstants.NEW_LINE_COUNT)
                                        .getAsInt()) + this.getWhiteSpaces(formatConfig
                                        .get(FormattingConstants.SPACE_COUNT).getAsInt()) +
                                        indentation);
                    } else {
                        currentWS.addProperty(FormattingConstants.WS,
                                FormattingConstants.EMPTY_SPACE);
                    }
                }
            }
        }
    }

    private void modifyReturnTypeAnnotations(JsonObject node, String indentation) {
        if (node.has("returnTypeAnnotationAttachments")) {
            JsonArray returnTypeAnnotations = node.getAsJsonArray("returnTypeAnnotationAttachments");

            for (JsonElement element : returnTypeAnnotations) {
                JsonObject returnTypeAnnotation = element.getAsJsonObject();
                JsonObject memberFormatConfig = this.getFormattingConfig(0, 1,
                        0, false, this.getWhiteSpaceCount(indentation), true);
                returnTypeAnnotation.add(FormattingConstants.FORMATTING_CONFIG, memberFormatConfig);
            }
        }
    }

    private void modifyExpressions(JsonObject node, String indentWithParentIndentation) {
        if (node.has(FormattingConstants.EXPRESSIONS)) {
            JsonArray expressions = node.getAsJsonArray(FormattingConstants.EXPRESSIONS);
            iterateAndFormatMembers(indentWithParentIndentation, expressions);
        }
    }

    private void modifyBlockClosingBrace(JsonObject node, String indentation, JsonObject closingBraceWS, String block,
                                         boolean isSingleLineBlock) {
        if (node.has(block)
                && node.getAsJsonObject(block).getAsJsonArray(FormattingConstants.STATEMENTS).size() <= 0
                && this.noHeightAvailable(closingBraceWS.get(FormattingConstants.WS).getAsString())) {
            if (!isSingleLineBlock) {
                closingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation
                        + FormattingConstants.NEW_LINE + indentation);
            } else {
                closingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.EMPTY_SPACE);
            }
        } else if (this.noHeightAvailable(closingBraceWS.get(FormattingConstants.WS)
                .getAsString())) {
            closingBraceWS.addProperty(FormattingConstants.WS, FormattingConstants.NEW_LINE + indentation);
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
                            false, this.getWhiteSpaceCount(indentation), true);
                } else {
                    annotationFormattingConfig = this.getFormattingConfig(1, 0,
                            formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                            false, this.getWhiteSpaceCount(indentation), true);
                }

                annotationAttachment.add(FormattingConstants.FORMATTING_CONFIG, annotationFormattingConfig);
            }
        }
    }

    private void modifyMarkdownDocumentation(JsonObject node, JsonObject formatConfig, String indentation) {
        if (node.has("markdownDocumentationAttachment")) {
            JsonObject markdownDocumentationAttachment = node.getAsJsonObject("markdownDocumentationAttachment");
            JsonObject markdownDocumentationAttachmentConfig = this.getFormattingConfig(
                    formatConfig.get(FormattingConstants.NEW_LINE_COUNT).getAsInt(), 0,
                    formatConfig.get(FormattingConstants.START_COLUMN).getAsInt(),
                    false,
                    this.getWhiteSpaceCount(indentation), false);
            markdownDocumentationAttachment.add(FormattingConstants.FORMATTING_CONFIG,
                    markdownDocumentationAttachmentConfig);

        }
    }

    private void modifyWorkers(JsonObject node, String indentation) {
        if (node.has("workers")) {
            JsonArray workers = node.getAsJsonArray("workers");
            iterateAndFormatBlockStatements(this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                    .get(FormattingConstants.START_COLUMN).getAsInt()), indentation, workers);
        }
    }

    private void modifyEndpoints(JsonObject node, String indentation) {
        if (node.has("endpointNodes")) {
            JsonArray endpointNodes = node.getAsJsonArray("endpointNodes");
            iterateAndFormatBlockStatements(this.getWhiteSpaces(node.getAsJsonObject(FormattingConstants.POSITION)
                    .get(FormattingConstants.START_COLUMN).getAsInt()), indentation, endpointNodes);
        }
    }

    private void modifyPatternClauses(JsonObject node, String indentation) {
        if (node.has("patternClauses")) {
            JsonArray patternClauses = node.getAsJsonArray("patternClauses");
            for (JsonElement patternClause : patternClauses) {
                JsonObject patternFormatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(indentation), true, this.getWhiteSpaceCount(indentation),
                        false);
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
                        0, false,
                        this.getWhiteSpaceCount(indentation), true);
            } else {
                memberTypeFormatConfig = this.getFormattingConfig(0, 1,
                        0, false,
                        this.getWhiteSpaceCount(indentation), true);
            }

            memberType.add(FormattingConstants.FORMATTING_CONFIG, memberTypeFormatConfig);
        }
    }

    private void iterateAndFormatBlockStatements(String indentation, String indentationOfParent,
                                                 JsonArray blockStatementNodes) {
        for (int i = 0; i < blockStatementNodes.size(); i++) {
            JsonObject endpointNode = blockStatementNodes.get(i).getAsJsonObject();
            JsonObject endpointFormatConfig;
            if (i == 0) {
                endpointFormatConfig = this.getFormattingConfig(1, 0,
                        this.getWhiteSpaceCount(indentation), true,
                        this.getWhiteSpaceCount(indentationOfParent), false);
            } else {
                endpointFormatConfig = this.getFormattingConfig(2, 0,
                        this.getWhiteSpaceCount(indentation), true,
                        this.getWhiteSpaceCount(indentationOfParent), false);
            }

            endpointNode.add(FormattingConstants.FORMATTING_CONFIG, endpointFormatConfig);
        }
    }

    private void iterateAndFormatMembers(String indentation, JsonArray members) {
        int count = 0;
        for (int i = 0; i < members.size(); i++) {
            JsonObject member = members.get(i).getAsJsonObject();
            if (!(member.has(FormattingConstants.SKIP_FORMATTING)
                    && member.get(FormattingConstants.SKIP_FORMATTING).getAsBoolean())) {
                JsonObject memberFormatConfig;
                if (count == 0) {
                    memberFormatConfig = this.getFormattingConfig(0, 0,
                            0, false, this.getWhiteSpaceCount(indentation), true);
                } else {
                    memberFormatConfig = this.getFormattingConfig(0, 1,
                            0, false, this.getWhiteSpaceCount(indentation), true);
                }

                member.add(FormattingConstants.FORMATTING_CONFIG, memberFormatConfig);

                ++count;
            }
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

    private void skipFormatting(JsonObject node, boolean doSkip) {
        // Add skipFormatting field as a property to the node.
        node.addProperty(FormattingConstants.SKIP_FORMATTING, doSkip);

        for (Map.Entry<String, JsonElement> child : node.entrySet()) {
            // If child element is not parent, position or ws continue.
            if (!child.getKey().equals("parent") && !child.getKey().equals(FormattingConstants.POSITION) &&
                    !child.getKey().equals(FormattingConstants.WS)) {
                // If child is a object and has a kind, do skip formatting
                // else if child is a array iterate and skip formatting for child items.
                if (child.getValue().isJsonObject() && child.getValue().getAsJsonObject()
                        .has(FormattingConstants.KIND)) {
                    skipFormatting(child.getValue().getAsJsonObject(), doSkip);
                } else if (child.getValue().isJsonArray()) {
                    for (int i = 0; i < child.getValue().getAsJsonArray().size(); i++) {
                        JsonElement childItem = child.getValue().getAsJsonArray().get(i);
                        if (childItem.isJsonObject() && childItem.getAsJsonObject().has(FormattingConstants.KIND)) {
                            skipFormatting(childItem.getAsJsonObject(), doSkip);
                        }
                    }
                }
            }
        }
    }

    private JsonObject getFormattingConfig(int newLineCount, int spacesCount, int startColumn, boolean doIndent,
                                           int indentedStartColumn, boolean userParentIndentation) {
        JsonObject formattingConfig = new JsonObject();
        formattingConfig.addProperty(FormattingConstants.NEW_LINE_COUNT, newLineCount);
        formattingConfig.addProperty(FormattingConstants.SPACE_COUNT, spacesCount);
        formattingConfig.addProperty(FormattingConstants.START_COLUMN, startColumn);
        formattingConfig.addProperty(FormattingConstants.DO_INDENT, doIndent);
        formattingConfig.addProperty(FormattingConstants.INDENTED_START_COLUMN, indentedStartColumn);
        formattingConfig.addProperty(FormattingConstants.USE_PARENT_INDENTATION, userParentIndentation);
        return formattingConfig;
    }

    private String getIndentation(JsonObject formatConfig, boolean addSpaces) {
        String indentation = formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                ? (this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt()) +
                FormattingConstants.SPACE_TAB)
                : this.getWhiteSpaces(formatConfig.get(FormattingConstants.START_COLUMN).getAsInt());

        // If add space is true add spaces to the
        // indentation as provided by the format config
        return addSpaces ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.SPACE_COUNT).getAsInt())
                + indentation : indentation;
    }

    private String getParentIndentation(JsonObject formatConfig) {
        return formatConfig.get(FormattingConstants.DO_INDENT).getAsBoolean()
                ? this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt()) +
                FormattingConstants.SPACE_TAB
                : this.getWhiteSpaces(formatConfig.get(FormattingConstants.INDENTED_START_COLUMN).getAsInt());
    }

    private boolean isMemberOnNewLine(JsonArray members) {
        boolean lineSeparationAvailable = false;
        for (JsonElement memberItem : members) {
            JsonObject member = memberItem.getAsJsonObject();
            if (member.has(FormattingConstants.WS)) {
                List<JsonObject> sortedWSForMember = FormattingSourceGen.extractWS(member);
                for (JsonObject wsForMember : sortedWSForMember) {
                    String currentWS = wsForMember.get(FormattingConstants.WS).getAsString();
                    if (!noNewLine(currentWS)) {
                        lineSeparationAvailable = true;
                        break;
                    }
                }
            }
        }
        return lineSeparationAvailable;
    }

    private boolean isReasonAvailable(JsonObject reason) {
        boolean reasonAvailable = true;
        if (!reason.has(FormattingConstants.WS)) {
            if (reason.get(FormattingConstants.KIND).getAsString().equals("SimpleVariableRef")
                    && reason.has("variableName")) {
                JsonObject variableName = reason.getAsJsonObject("variableName");
                String value = variableName.get(FormattingConstants.VALUE).getAsString();
                if (value.equals("_")) {
                    reasonAvailable = false;
                }
            } else if (reason.get(FormattingConstants.KIND).getAsString().equals("Variable")
                    && reason.has(FormattingConstants.NAME)) {
                JsonObject name = reason.getAsJsonObject(FormattingConstants.NAME);
                String value = name.get(FormattingConstants.VALUE).getAsString();
                if (value.equals("$reason$") || value.equals("_")) {
                    reasonAvailable = false;
                }
            }
        }
        return reasonAvailable;
    }
}
