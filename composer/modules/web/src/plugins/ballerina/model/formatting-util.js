/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import _ from 'lodash';
import defaultWS from './../../../../source-gen/default-ws';

const TAB = "\t";
const SPACE_TAB = "    ";
const BETWEEN_BLOCK_SPACE = "\r\n\r\n";
const SINGLE_SPACE = " ";
const NEW_LINE = "\r\n";
const EMPTY_SPACE = "";

/**
 * Formatting util to handle formatting for each node with context awareness.
 */
class FormattingUtil {

    /**
     * Get the white spaces for formatting.
     *
     * @param column {number} column number
     * @returns {string}
     */
    getWhiteSpaces(column) {
        let whiteSpaces = "";
        for (let i = 0; i < (column - 1); i++) {
            whiteSpaces += SINGLE_SPACE;
        }
        return whiteSpaces;
    }

    /**
     * Get white space count.
     *
     * @param ws {string} white space string
     * @returns {number} count of white spaces.
     */
    getWhiteSpaceCount(ws) {
        let whiteSpaces = ws.split(SINGLE_SPACE);
        return whiteSpaces.length - 1;
    }

    /**
     * Find the object property by name.
     *
     * @param parent Node's parent node
     * @param name name of the property
     * @returns {*} null|Array
     */
    findObjectPropertyByName(parent, name) {
        if (!parent) {
            return;
        }

        let properties = [];
        if (parent.__proto__ && parent.__proto__.__proto__) {
            let keys = Object.getOwnPropertyNames(parent.__proto__.__proto__);
            for (let i = 0; i < keys.length; i++) {
                if (keys[i].includes(name)) {
                    properties.push(keys[i]);
                }
            }
        }

        return properties;
    }

    /**
     * Find the index of a given node.
     *
     * @param node node which need to find the index of
     * @returns {number} -1 | Index
     */
    findIndex(node) {
        let functions = this.findObjectPropertyByName(node.parent, "getIndexOf");
        for (let i = 0; i < functions.length; i++) {
            if (_.isFunction(node.parent[functions[i]])) {
                let index = node.parent[functions[i]](node);
                if (index >= 0) {
                    return index;
                }
            }
        }
        return -1;
    };

    /**
     * Tokenize given text to capture new lines and characters except whitespaces.
     *
     * @param text text to the be tokenize
     * @returns {Array} token array
     */
    tokenizer(text) {
        let tokens = [];
        let comment = "";
        for (let i = 0; i < text.length; i++) {
            if (/[^\n]/.test(text[i])) {
                comment += text[i];
            } else {
                if (comment && comment.trim() !== "") {
                    tokens.push(comment.trim());
                    comment = "";
                }
                tokens.push(text[i]);
            }

            if (i === (text.length - 1) && comment.trim() !== "") {
                tokens.push(comment.trim());
                comment = "";
            }
        }
        return tokens;
    }

    /**
     * Build the string from the given tokens.
     *
     * @param tokens tokens to be used
     * @param indent indentation to be applied
     * @returns {string} single string
     */
    getTextFromTokens(tokens, indent) {
        let text = "";
        for (let i = 0; i < tokens.length; i++) {
            if (/[^\n]/.test(tokens[i])) {
                text += indent ? indent + tokens[i] : tokens[i];
            } else {
                text += tokens[i];
            }
        }
        return indent ? (text + indent) : text;
    }

    /**
     * Preserve height of given whitespace.
     *
     * @param ws whitespace string to be height preserved
     * @param indent indentation to be applied
     */
    preserveHeight(ws, indent) {
        for (let i = 0; i < ws.length; i++) {
            if (/\S/.test(ws[i].ws)) {
                let tokens = this.tokenizer(ws[i].ws);
                ws[i].ws = this.getTextFromTokens(tokens, indent);
            } else if (/\n/.test(ws[i].ws)) {
                let tokens = this.tokenizer(ws[i].ws);
                ws[i].ws = this.getTextFromTokens(tokens, indent);
            }
        }
    }

    /**
     * Check whether height is available for given whitespace.
     *
     * @param ws whitespace to check
     * @returns {*|boolean} true | false
     */
    isHeightAvailable(ws) {
        return /\S|\n/.test(ws);
    }

    /**
     * Check whether given text has new lines.
     *
     * @param text text to be checked
     * @returns {*|boolean} true | false
     */
    isNewLine(text) {
        return /[\n]/.test(text);
    }

    /**
     * format Action nodes.
     *
     * @param {object} node
     *
     */
    formatActionNode(node) {
        // Not implemented.
    }


    /**
     * format Annotation nodes.
     *
     * @param {object} node
     *
     */
    formatAnnotationNode(node) {
        // Not implemented.
    }


    /**
     * format AnnotationAttachment nodes.
     *
     * @param {object} node
     *
     */
    formatAnnotationAttachmentNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));

        if (!this.isHeightAvailable(node.ws[0].ws)) {
            node.ws[0].ws = BETWEEN_BLOCK_SPACE + this.getWhiteSpaces(node.position.startColumn);
        } else if (!this.isNewLine(node.ws[0].ws[0])) {
            node.ws[0].ws = NEW_LINE + node.ws[0].ws;
        }

        if (node.expression && node.expression.ws) {
            node.expression.position.startColumn = node.position.startColumn;
        }
    }


    /**
     * format AnnotationAttribute nodes.
     *
     * @param {object} node
     *
     */
    formatAnnotationAttributeNode(node) {
        // Not implemented.
    }


    /**
     * format Catch nodes.
     *
     * @param {object} node
     *
     */
    formatCatchNode(node) {
        // Not implemented.
    }


    /**
     * format CompilationUnit nodes.
     *
     * @param {object} node
     *
     */
    formatCompilationUnitNode(node) {
        // Add the start column and sorted index.
        for (let i = 0; i < node.topLevelNodes.length; i++) {
            let child = node.topLevelNodes[i];
            child.position.startColumn = 1;
        }

        if (node.ws) {
            this.preserveHeight(node.ws);
            if (!this.isHeightAvailable(node.ws[0].ws)) {
                node.ws[0].ws = NEW_LINE;
            } else if (!this.isNewLine(node.ws[0].ws[node.ws[0].ws.length - 1])) {
                node.ws[0].ws = node.ws[0].ws + NEW_LINE;
            }
        }

        // Sort imports
        let i, j, swapped = false;
        for (i = 0; i < node.topLevelNodes.length - 1; i++) {
            swapped = false;
            for (j = 0; j < node.topLevelNodes.length - i - 1; j++) {
                if (node.topLevelNodes[j].kind === 'Import' && node.topLevelNodes[j + 1].kind === 'Import') {
                    let refImportName = node.topLevelNodes[j].orgName.value +
                        "/" + node.topLevelNodes[j].packageName[0].value;

                    let compImportName = node.topLevelNodes[j + 1].orgName.value +
                        "/" + node.topLevelNodes[j + 1].packageName[0].value;

                    let comparisonResult = refImportName.localeCompare(compImportName);

                    // Swap if the comparison value is positive.
                    if (comparisonResult > 0) {
                        // Swap ws to keep the formatting in level.
                        let refWS = node.topLevelNodes[j].ws[0].ws;
                        let compWS = node.topLevelNodes[j + 1].ws[0].ws;

                        let tempNode = node.topLevelNodes[j];
                        node.topLevelNodes[j] = node.topLevelNodes[j + 1];
                        tempNode.ws[0].ws = compWS;
                        node.topLevelNodes[j].ws[0].ws = refWS;
                        node.topLevelNodes[j + 1] = tempNode;

                        swapped = true;
                    }
                }
            }

            // If not swapped break;
            if (!swapped) {
                break;
            }
        }
    }


    /**
     * format Deprecated nodes.
     *
     * @param {object} node
     *
     */
    formatDeprecatedNode(node) {
        // Not implemented.
    }


    /**
     * format Documentation nodes.
     *
     * @param {object} node
     *
     */
    formatDocumentationNode(node) {
        // Not implemented.
    }


    /**
     * format Endpoint nodes.
     *
     * @param {object} node
     *
     */
    formatEndpointNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));

        let endpointIndex = this.findIndex(node);

        if (!this.isHeightAvailable(node.ws[0].ws)) {
            node.ws[0].ws = (endpointIndex > 0 || node.parent.kind !== 'CompilationUnit')
                ? (BETWEEN_BLOCK_SPACE + this.getWhiteSpaces(node.position.startColumn))
                : EMPTY_SPACE;
        } else if (!this.isNewLine(node.ws[0].ws[0]) && endpointIndex !== 0) {
            node.ws[0].ws = NEW_LINE + node.ws[0].ws;
        }

        if (node.configurationExpression && node.configurationExpression.ws) {
            node.configurationExpression.position.startColumn = node.position.startColumn;
        }
    }


    /**
     * format Enum nodes.
     *
     * @param {object} node
     *
     */
    formatEnumNode(node) {
        // Not implemented.
    }


    /**
     * format Function nodes.
     *
     * @param {object} node
     *
     */
    formatFunctionNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));

        let functionIndex = this.findIndex(node);
        if (!this.isHeightAvailable(node.ws[0].ws)) {
            node.ws[0].ws = (functionIndex > 0 || node.parent.kind === 'ObjectType')
                ? (BETWEEN_BLOCK_SPACE + this.getWhiteSpaces(node.position.startColumn))
                : EMPTY_SPACE;
        } else if (!this.isNewLine(node.ws[0].ws[0]) && functionIndex !== 0) {
            node.ws[0].ws = NEW_LINE + node.ws[0].ws;
        }

        if (!this.isHeightAvailable(node.ws[node.ws.length - 2].ws)) {
            node.ws[node.ws.length - 2].ws = SINGLE_SPACE;
        }

        if (node.body && node.body.statements.length <= 0 && node.endpointNodes.length <= 0 &&
            node.workers.length <= 0) {
            if (!this.isHeightAvailable(node.ws[node.ws.length - 1].ws)) {
                node.ws[node.ws.length - 1].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn) + NEW_LINE
                    + this.getWhiteSpaces(node.position.startColumn);
            }
        } else {
            if (!this.isHeightAvailable(node.ws[node.ws.length - 1].ws)) {
                node.ws[node.ws.length - 1].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
            }
        }

        if (node.parameters) {
            for (let i = 0; i < node.parameters.length; i++) {
                if (node.parameters[i].typeNode) {
                    if (i === 0) {
                        node.parameters[i].typeNode.ws[0].ws = EMPTY_SPACE;
                    } else {
                        node.parameters[i].typeNode.ws[0].ws = SINGLE_SPACE;
                    }
                }
            }
        }

        if (node.endpointNodes) {
            for (let i = 0; i < node.endpointNodes.length; i++) {
                node.endpointNodes[i].position.startColumn = node.position.startColumn +
                    this.getWhiteSpaceCount(SPACE_TAB);
            }
        }
    }

    /**
     * format Identifier nodes.
     *
     * @param {object} node
     *
     */
    formatIdentifierNode(node) {
        // Not implemented.
    }


    /**
     * format Import nodes.
     *
     * @param {object} node
     *
     */
    formatImportNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));
        let importIndex = this.findIndex(node);

        if (importIndex > 0) {
            if (!this.isHeightAvailable(node.ws[0].ws)) {
                node.ws[0].ws = NEW_LINE;
            }
        } else {
            if (!this.isHeightAvailable(node.ws[0].ws)) {
                node.ws[0].ws = EMPTY_SPACE;
            }
        }
    }


    /**
     * format Package nodes.
     *
     * @param {object} node
     *
     */
    formatPackageNode(node) {
        // Not implemented.
    }


    /**
     * format PackageDeclaration nodes.
     *
     * @param {object} node
     *
     */
    formatPackageDeclarationNode(node) {
        // Not implemented.
    }


    /**
     * format Resource nodes.
     *
     * @param {object} node
     *
     */
    formatResourceNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));

        if (!this.isHeightAvailable(node.ws[0].ws)) {
            node.ws[0].ws = BETWEEN_BLOCK_SPACE + this.getWhiteSpaces(node.position.startColumn);
        }

        if (!this.isHeightAvailable(node.ws[node.ws.length - 2].ws)) {
            node.ws[node.ws.length - 2].ws = SINGLE_SPACE;
        }

        if (node.body && node.body.statements.length <= 0 && node.workers.length <= 0 &&
            node.endpointNodes.length <= 0) {
            if (!this.isHeightAvailable(node.ws[node.ws.length - 1].ws)) {
                node.ws[node.ws.length - 1].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn) + NEW_LINE +
                    this.getWhiteSpaces(node.position.startColumn);
            }
        } else {
            if (!this.isHeightAvailable(node.ws[node.ws.length - 1].ws)) {
                node.ws[node.ws.length - 1].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
            }
        }

        if (node.parameters) {
            for (let i = 0; i < node.parameters.length; i++) {
                if (node.parameters[i].typeNode) {
                    if (i === 0) {
                        node.parameters[i].typeNode.ws[0].ws = EMPTY_SPACE;
                    } else {
                        node.parameters[i].typeNode.ws[0].ws = SINGLE_SPACE;
                    }
                }
            }
        }

        if (node.endpointNodes) {
            for (let i = 0; i < node.endpointNodes.length; i++) {
                node.endpointNodes[i].position.startColumn = node.position.startColumn +
                    this.getWhiteSpaceCount(SPACE_TAB);
            }
        }
    }


    /**
     * format Service nodes.
     *
     * @param {object} node
     *
     */
    formatServiceNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));
        let serviceIndex = this.findIndex(node);

        if (!this.isHeightAvailable(node.ws[0].ws)) {
            node.ws[0].ws = serviceIndex > 0 ? BETWEEN_BLOCK_SPACE : EMPTY_SPACE;
        } else if (!this.isNewLine(node.ws[0].ws[0]) && serviceIndex !== 0) {
            node.ws[0].ws = NEW_LINE + node.ws[0].ws;
        }

        if (!this.isHeightAvailable(node.ws[node.ws.length - 2].ws)) {
            node.ws[node.ws.length - 2].ws = SINGLE_SPACE;
        }

        if (node.resources.length <= 0 && node.variables.length <= 0 && node.endpointNodes.length <= 0 &&
            node.namespaceDeclarations.length <= 0) {
            if (!this.isHeightAvailable(node.ws[node.ws.length - 1].ws)) {
                node.ws[node.ws.length - 1].ws = NEW_LINE + SPACE_TAB + NEW_LINE;
            }
        } else {
            if (!this.isHeightAvailable(node.ws[node.ws.length - 1].ws)) {
                node.ws[node.ws.length - 1].ws = NEW_LINE;
            }
        }

        if (node.endpointNodes) {
            for (let i = 0; i < node.endpointNodes.length; i++) {
                node.endpointNodes[i].position.startColumn = node.position.startColumn +
                    this.getWhiteSpaceCount(SPACE_TAB);
            }
        }

        if (node.resources) {
            for (let i = 0; i < node.resources.length; i++) {
                node.resources[i].position.startColumn = node.position.startColumn +
                    this.getWhiteSpaceCount(SPACE_TAB);
            }
        }

        if (node.variables) {
            for (let i = 0; i < node.variables.length; i++) {
                node.variables[i].position.startColumn = node.position.startColumn +
                    this.getWhiteSpaceCount(SPACE_TAB);
            }
        }

        if (node.annotationAttachments) {
            for (let i = 0; i < node.annotationAttachments.length; i++) {
                node.annotationAttachments[i].position.startColumn = node.position.startColumn;
            }
        }
    }


    /**
     * format TypeDefinition nodes.
     *
     * @param {object} node
     *
     */
    formatTypeDefinitionNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));
        let typeDefinitionIndex = this.findIndex(node);

        if (!this.isHeightAvailable(node.ws[0].ws)) {
            node.ws[0].ws = typeDefinitionIndex > 0
                ? BETWEEN_BLOCK_SPACE
                : EMPTY_SPACE;
        } else if (!this.isNewLine(node.ws[0].ws[0]) && typeDefinitionIndex !== 0) {
            node.ws[0].ws = NEW_LINE + node.ws[0].ws;
        }

        if (!this.isHeightAvailable(node.ws[node.ws.length - 3].ws)) {
            node.ws[node.ws.length - 3].ws = SINGLE_SPACE;
        }

        if (node.typeNode && node.typeNode.fields.length <= 0) {
            if (!this.isHeightAvailable(node.ws[node.ws.length - 2].ws)) {
                node.ws[node.ws.length - 2].ws = NEW_LINE + SPACE_TAB + NEW_LINE;
            }
        } else {
            if (!this.isHeightAvailable(node.ws[node.ws.length - 2].ws)) {
                node.ws[node.ws.length - 2].ws = NEW_LINE;
            }
        }

        if (node.typeNode) {
            node.typeNode.position.startColumn = node.position.startColumn
        }
    }


    /**
     * format Variable nodes.
     *
     * @param {object} node
     *
     */
    formatVariableNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));
        let variableIndex = this.findIndex(node);

        if (node.parent.kind === 'VariableDef' || node.parent.kind === "CompilationUnit" ||
            node.parent.kind === 'RecordType') {
            if (node.typeNode && node.typeNode.kind !== 'ArrayType') {
                node.typeNode.position.startColumn = node.position.startColumn;
                if (node.typeNode.ws) {
                    this.preserveHeight(node.typeNode.ws, this.getWhiteSpaces(node.position.startColumn));

                    if ((variableIndex > 0 || variableIndex < 0)) {
                        if (!this.isHeightAvailable(node.typeNode.ws[0].ws)) {
                            node.typeNode.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
                        }
                    } else {
                        if (!this.isHeightAvailable(node.typeNode.ws[0].ws)) {
                            if (node.parent.kind === 'RecordType') {
                                node.typeNode.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
                            } else {
                                node.typeNode.ws[0].ws = EMPTY_SPACE;
                            }
                        }
                    }
                } else if (node.isAnonType && node.ws && node.ws.length > 3) {
                    if (!this.isHeightAvailable(node.ws[0].ws)) {
                        node.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
                    }

                    if (!this.isHeightAvailable(node.ws[1].ws)) {
                        node.ws[1].ws = SINGLE_SPACE;
                    }

                    if (node.typeNode && node.typeNode.anonType && node.typeNode.anonType.fields &&
                        node.typeNode.anonType.fields.length <= 0) {
                        if (!this.isHeightAvailable(node.ws[2].ws)) {
                            node.ws[2].ws = NEW_LINE + SPACE_TAB + NEW_LINE;
                        }
                    } else {
                        if (!this.isHeightAvailable(node.ws[2].ws)) {
                            node.ws[2].ws = NEW_LINE;
                        }
                    }
                }
            } else if(node.typeNode) {
                node.typeNode.position.startColumn = node.position.startColumn;
                if (node.typeNode.ws) {
                    this.preserveHeight(node.typeNode.ws, this.getWhiteSpaces(node.position.startColumn));
                    if (!this.isHeightAvailable(node.typeNode.ws[0].ws)) {
                        node.typeNode.ws[0].ws = EMPTY_SPACE;
                    }
                }
            }
        }
    }


    /**
     * format Worker nodes.
     *
     * @param {object} node
     *
     */
    formatWorkerNode(node) {
        // Not implemented.
    }


    /**
     * format DocumentationAttribute nodes.
     *
     * @param {object} node
     *
     */
    formatDocumentationAttributeNode(node) {
        // Not implemented.
    }


    /**
     * format AnnotationAttachmentAttribute nodes.
     *
     * @param {object} node
     *
     */
    formatAnnotationAttachmentAttributeNode(node) {
        // Not implemented.
    }


    /**
     * format AnnotationAttachmentAttributeValue nodes.
     *
     * @param {object} node
     *
     */
    formatAnnotationAttachmentAttributeValueNode(node) {
        // Not implemented.
    }


    /**
     * format ArrayLiteralExpr nodes.
     *
     * @param {object} node
     *
     */
    formatArrayLiteralExprNode(node) {
        // Not implemented.
    }


    /**
     * format BinaryExpr nodes.
     *
     * @param {object} node
     *
     */
    formatBinaryExprNode(node) {
        // Not implemented.
    }


    /**
     * format ElvisExpr nodes.
     *
     * @param {object} node
     *
     */
    formatElvisExprNode(node) {
        // Not implemented.
    }


    /**
     * format BracedTupleExpr nodes.
     *
     * @param {object} node
     *
     */
    formatBracedTupleExprNode(node) {
        // Not implemented.
    }


    /**
     * format TypeInitExpr nodes.
     *
     * @param {object} node
     *
     */
    formatTypeInitExprNode(node) {
        // Not implemented.
    }


    /**
     * format FieldBasedAccessExpr nodes.
     *
     * @param {object} node
     *
     */
    formatFieldBasedAccessExprNode(node) {
        // Not implemented.
    }


    /**
     * format IndexBasedAccessExpr nodes.
     *
     * @param {object} node
     *
     */
    formatIndexBasedAccessExprNode(node) {
        // Not implemented.
    }


    /**
     * format IntRangeExpr nodes.
     *
     * @param {object} node
     *
     */
    formatIntRangeExprNode(node) {
        // Not implemented.
    }


    /**
     * format Invocation nodes.
     *
     * @param {object} node
     *
     */
    formatInvocationNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));

        if (node.parent.kind === 'ExpressionStatement') {
            if (node.expression && node.expression.ws) {
                this.preserveHeight(node.expression.ws, this.getWhiteSpaces(node.position.startColumn));
                if (!this.isHeightAvailable(node.expression.ws[0].ws)) {
                    node.expression.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
                }
            } else {
                if (!this.isHeightAvailable(node.ws[0].ws)) {
                    node.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.parent.position.startColumn);
                }
            }
        } else {
            if (node.expression && node.expression.ws) {
                this.preserveHeight(node.expression.ws, this.getWhiteSpaces(node.position.startColumn));
                if (!this.isHeightAvailable(node.expression.ws[0].ws)) {
                    node.expression.ws[0].ws = SINGLE_SPACE;
                }
            } else {
                if (!this.isHeightAvailable(node.ws[0].ws)) {
                    node.ws[0].ws = SINGLE_SPACE;
                }
            }
        }

        if (node.argumentExpressions) {
            for (let i = 0; i < node.argumentExpressions.length; i++) {
                if (node.argumentExpressions[i]) {
                    if (i === 0) {
                        node.argumentExpressions[i].ws[0].ws = EMPTY_SPACE;
                    } else {
                        node.argumentExpressions[i].ws[0].ws = SINGLE_SPACE;
                    }
                }
            }
        }
    }


    /**
     * format Lambda nodes.
     *
     * @param {object} node
     *
     */
    formatLambdaNode(node) {
        // Not implemented.
    }


    /**
     * format Literal nodes.
     *
     * @param {object} node
     *
     */
    formatLiteralNode(node) {
        // Not implemented.
    }


    /**
     * format RecordLiteralExpr nodes.
     *
     * @param {object} node
     *
     */
    formatRecordLiteralExprNode(node) {
        if (!node.ws) {
            return;
        }

        if (node.parent.kind === 'Endpoint' || node.parent.kind === 'AnnotationAttachment' ||
            node.parent.kind === "Service") {

            this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));

            if (!this.isHeightAvailable(node.ws[0].ws)) {
                node.ws[0].ws = SINGLE_SPACE;
            }
            if (node.keyValuePairs.length <= 0) {
                if (!this.isHeightAvailable(node.ws[node.ws.length - 1].ws)) {
                    node.ws[node.ws.length - 1].ws =
                        NEW_LINE + this.getWhiteSpaces(node.position.startColumn) + NEW_LINE +
                        this.getWhiteSpaces(node.position.startColumn);
                }
            } else {
                if (!this.isHeightAvailable(node.ws[node.ws.length - 1].ws)) {
                    node.ws[node.ws.length - 1].ws =
                        NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
                }
            }


            let indentedStartColumn = node.position.startColumn +
                this.getWhiteSpaceCount(SPACE_TAB);

            for (let i = 0; i < node.keyValuePairs.length; i++) {
                this.preserveHeight(node.keyValuePairs[i].key.ws, this.getWhiteSpaces(indentedStartColumn));
                this.preserveHeight(node.keyValuePairs[i].value.ws, this.getWhiteSpaces(indentedStartColumn));
                this.preserveHeight(node.keyValuePairs[i].ws, this.getWhiteSpaces(indentedStartColumn));

                if (!this.isHeightAvailable(node.keyValuePairs[i].key.ws[0].ws)) {
                    node.keyValuePairs[i].key.ws[0].ws = NEW_LINE + this.getWhiteSpaces(indentedStartColumn);
                }

                if (!this.isHeightAvailable(node.keyValuePairs[i].value.ws[0].ws)) {
                    node.keyValuePairs[i].value.ws[0].ws = SINGLE_SPACE;
                }

                if (!this.isHeightAvailable(node.keyValuePairs[i].ws)) {
                    node.keyValuePairs[i].ws = EMPTY_SPACE;
                }
            }

            for (let j = 0; j < node.ws.length; j++) {
                if (node.ws[j].text === ',') {
                    node.ws[j].ws = EMPTY_SPACE;
                }
            }
        }
    }


    /**
     * format SimpleVariableRef nodes.
     *
     * @param {object} node
     *
     */
    formatSimpleVariableRefNode(node) {

    }


    /**
     * format StringTemplateLiteral nodes.
     *
     * @param {object} node
     *
     */
    formatStringTemplateLiteralNode(node) {
        // Not implemented.
    }


    /**
     * format TernaryExpr nodes.
     *
     * @param {object} node
     *
     */
    formatTernaryExprNode(node) {
        // Not implemented.
    }


    /**
     * format AwaitExpr nodes.
     *
     * @param {object} node
     *
     */
    formatAwaitExprNode(node) {
        // Not implemented.
    }


    /**
     * format TypedescExpression nodes.
     *
     * @param {object} node
     *
     */
    formatTypedescExpressionNode(node) {
        // Not implemented.
    }


    /**
     * format TypeCastExpr nodes.
     *
     * @param {object} node
     *
     */
    formatTypeCastExprNode(node) {
        // Not implemented.
    }


    /**
     * format TypeConversionExpr nodes.
     *
     * @param {object} node
     *
     */
    formatTypeConversionExprNode(node) {
        // Not implemented.
    }


    /**
     * format UnaryExpr nodes.
     *
     * @param {object} node
     *
     */
    formatUnaryExprNode(node) {
        // Not implemented.
    }


    /**
     * format RestArgsExpr nodes.
     *
     * @param {object} node
     *
     */
    formatRestArgsExprNode(node) {
        // Not implemented.
    }


    /**
     * format NamedArgsExpr nodes.
     *
     * @param {object} node
     *
     */
    formatNamedArgsExprNode(node) {
        // Not implemented.
    }


    /**
     * format StatementExpression nodes.
     *
     * @param {object} node
     *
     */
    formatStatementExpressionNode(node) {
        // Not implemented.
    }


    /**
     * format MatchExpression nodes.
     *
     * @param {object} node
     *
     */
    formatMatchExpressionNode(node) {
        // Not implemented.
    }


    /**
     * format MatchExpressionPatternClause nodes.
     *
     * @param {object} node
     *
     */
    formatMatchExpressionPatternClauseNode(node) {
        // Not implemented.
    }


    /**
     * format CheckExpr nodes.
     *
     * @param {object} node
     *
     */
    formatCheckExprNode(node) {
        // Not implemented.
    }


    /**
     * format SelectExpression nodes.
     *
     * @param {object} node
     *
     */
    formatSelectExpressionNode(node) {
        // Not implemented.
    }


    /**
     * format Abort nodes.
     *
     * @param {object} node
     *
     */
    formatAbortNode(node) {
        // Not implemented.
    }


    /**
     * format Done nodes.
     *
     * @param {object} node
     *
     */
    formatDoneNode(node) {
        // Not implemented.
    }


    /**
     * format Retry nodes.
     *
     * @param {object} node
     *
     */
    formatRetryNode(node) {
        // Not implemented.
    }


    /**
     * format Assignment nodes.
     *
     * @param {object} node
     *
     */
    formatAssignmentNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));

        if (node.variable && node.variable.ws) {
            this.preserveHeight(node.variable.ws, this.getWhiteSpaces(node.position.startColumn));
            if (!this.isHeightAvailable(node.variable.ws[0].ws)) {
                node.variable.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
            }
        }

        if (node.expression && node.expression.ws) {
            this.preserveHeight(node.expression.ws, this.getWhiteSpaces(node.position.startColumn));
            if (!this.isHeightAvailable(node.expression.ws[0].ws)) {
                node.expression.ws[0].ws = SINGLE_SPACE;
            }
        }

        if (!this.isHeightAvailable(node.ws[0].ws)) {
            node.ws[0].ws = SINGLE_SPACE;
        }

        if (!this.isHeightAvailable(node.ws[1].ws)) {
            node.ws[1].ws = EMPTY_SPACE;
        }
    }


    /**
     * format CompoundAssignment nodes.
     *
     * @param {object} node
     *
     */
    formatCompoundAssignmentNode(node) {
        // Not implemented.
    }


    /**
     * format PostIncrement nodes.
     *
     * @param {object} node
     *
     */
    formatPostIncrementNode(node) {
        // Not implemented.
    }


    /**
     * format Bind nodes.
     *
     * @param {object} node
     *
     */
    formatBindNode(node) {
        // Not implemented.
    }


    /**
     * format Block nodes.
     *
     * @param {object} node
     *
     */
    formatBlockNode(node) {
        node.position = {
            startColumn: node.parent.position.startColumn,
        };

        // Add the start column and sorted index.
        for (let i = 0; i < node.statements.length; i++) {
            let child = node.statements[i];
            child.position.startColumn = node.position.startColumn + this.getWhiteSpaceCount(SPACE_TAB);
        }
    }


    /**
     * format Break nodes.
     *
     * @param {object} node
     *
     */
    formatBreakNode(node) {
        // Not implemented.
    }


    /**
     * format ExpressionStatement nodes.
     *
     * @param {object} node
     *
     */
    formatExpressionStatementNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));
        node.expression.position.startColumn = node.position.startColumn;

        if (!this.isHeightAvailable(node.ws[0].ws)) {
            node.ws[0].ws = EMPTY_SPACE;
        }
    }


    /**
     * format Foreach nodes.
     *
     * @param {object} node
     *
     */
    formatForeachNode(node) {
        // Not implemented.
    }


    /**
     * format ForkJoin nodes.
     *
     * @param {object} node
     *
     */
    formatForkJoinNode(node) {
        // Not implemented.
    }


    /**
     * format If nodes.
     *
     * @param {object} node
     *
     */
    formatIfNode(node) {
        // Not implemented.
    }


    /**
     * format Match nodes.
     *
     * @param {object} node
     *
     */
    formatMatchNode(node) {
        // Not implemented.
    }


    /**
     * format MatchPatternClause nodes.
     *
     * @param {object} node
     *
     */
    formatMatchPatternClauseNode(node) {
        // Not implemented.
    }


    /**
     * format Reply nodes.
     *
     * @param {object} node
     *
     */
    formatReplyNode(node) {
        // Not implemented.
    }


    /**
     * format Return nodes.
     *
     * @param {object} node
     *
     */
    formatReturnNode(node) {
        // Not implemented.
    }


    /**
     * format Throw nodes.
     *
     * @param {object} node
     *
     */
    formatThrowNode(node) {
        // Not implemented.
    }


    /**
     * format Transaction nodes.
     *
     * @param {object} node
     *
     */
    formatTransactionNode(node) {
        // Not implemented.
    }


    /**
     * format Try nodes.
     *
     * @param {object} node
     *
     */
    formatTryNode(node) {
        // Not implemented.
    }


    /**
     * format TupleDestructure nodes.
     *
     * @param {object} node
     *
     */
    formatTupleDestructureNode(node) {
        // Not implemented.
    }


    /**
     * format VariableDef nodes.
     *
     * @param {object} node
     *
     */
    formatVariableDefNode(node) {
        if (!node.ws) {
            return;
        }

        this.preserveHeight(node.ws, this.getWhiteSpaces(node.position.startColumn));
        node.variable.position.startColumn = node.position.startColumn;

        if (!this.isHeightAvailable(node.ws[0].ws)) {
            node.ws[0].ws = EMPTY_SPACE;
        }
    }


    /**
     * format While nodes.
     *
     * @param {object} node
     *
     */
    formatWhileNode(node) {
        // Not implemented.
    }


    /**
     * format Lock nodes.
     *
     * @param {object} node
     *
     */
    formatLockNode(node) {
        // Not implemented.
    }


    /**
     * format WorkerReceive nodes.
     *
     * @param {object} node
     *
     */
    formatWorkerReceiveNode(node) {
        // Not implemented.
    }


    /**
     * format WorkerSend nodes.
     *
     * @param {object} node
     *
     */
    formatWorkerSendNode(node) {
        // Not implemented.
    }


    /**
     * format ArrayType nodes.
     *
     * @param {object} node
     *
     */
    formatArrayTypeNode(node) {
        // Not implemented.
    }


    /**
     * format UnionTypeNode nodes.
     *
     * @param {object} node
     *
     */
    formatUnionTypeNodeNode(node) {
        // Not implemented.
    }


    /**
     * format TupleTypeNode nodes.
     *
     * @param {object} node
     *
     */
    formatTupleTypeNodeNode(node) {
        // Not implemented.
    }


    /**
     * format ConstrainedType nodes.
     *
     * @param {object} node
     *
     */
    formatConstrainedTypeNode(node) {
        // Not implemented.
    }


    /**
     * format FunctionType nodes.
     *
     * @param {object} node
     *
     */
    formatFunctionTypeNode(node) {
        // Not implemented.
    }


    /**
     * format UserDefinedType nodes.
     *
     * @param {object} node
     *
     */
    formatUserDefinedTypeNode(node) {
        if (node.isAnonType && node.anonType) {
            node.anonType.position.startColumn = node.position.startColumn;
        }
    }


    /**
     * format EndpointType nodes.
     *
     * @param {object} node
     *
     */
    formatEndpointTypeNode(node) {
        // Not implemented.
    }


    /**
     * format ValueType nodes.
     *
     * @param {object} node
     *
     */
    formatValueTypeNode(node) {
        // Not implemented.
    }


    /**
     * format RecordType nodes.
     *
     * @param {object} node
     *
     */
    formatRecordTypeNode(node) {
        // Add the start column and sorted index.
        for (let i = 0; i < node.fields.length; i++) {
            let child = node.fields[i];
            child.position.startColumn = node.position.startColumn + this.getWhiteSpaceCount(SPACE_TAB);
        }
    }


    /**
     * format ObjectType nodes.
     *
     * @param {object} node
     *
     */
    formatObjectTypeNode(node) {
        // Not implemented.
    }


    /**
     * format OrderBy nodes.
     *
     * @param {object} node
     *
     */
    formatOrderByNode(node) {
        // Not implemented.
    }


    /**
     * format OrderByVariable nodes.
     *
     * @param {object} node
     *
     */
    formatOrderByVariableNode(node) {
        // Not implemented.
    }


    /**
     * format Limit nodes.
     *
     * @param {object} node
     *
     */
    formatLimitNode(node) {
        // Not implemented.
    }


    /**
     * format GroupBy nodes.
     *
     * @param {object} node
     *
     */
    formatGroupByNode(node) {
        // Not implemented.
    }


    /**
     * format Having nodes.
     *
     * @param {object} node
     *
     */
    formatHavingNode(node) {
        // Not implemented.
    }


    /**
     * format SelectClause nodes.
     *
     * @param {object} node
     *
     */
    formatSelectClauseNode(node) {
        // Not implemented.
    }


    /**
     * format Where nodes.
     *
     * @param {object} node
     *
     */
    formatWhereNode(node) {
        // Not implemented.
    }


    /**
     * format FunctionClause nodes.
     *
     * @param {object} node
     *
     */
    formatFunctionClauseNode(node) {
        // Not implemented.
    }


    /**
     * format WindowClause nodes.
     *
     * @param {object} node
     *
     */
    formatWindowClauseNode(node) {
        // Not implemented.
    }


    /**
     * format StreamAction nodes.
     *
     * @param {object} node
     *
     */
    formatStreamActionNode(node) {
        // Not implemented.
    }


    /**
     * format PatternStreamingEdgeInput nodes.
     *
     * @param {object} node
     *
     */
    formatPatternStreamingEdgeInputNode(node) {
        // Not implemented.
    }


    /**
     * format PatternStreamingInput nodes.
     *
     * @param {object} node
     *
     */
    formatPatternStreamingInputNode(node) {
        // Not implemented.
    }


    /**
     * format StreamingQuery nodes.
     *
     * @param {object} node
     *
     */
    formatStreamingQueryNode(node) {
        // Not implemented.
    }


    /**
     * format Within nodes.
     *
     * @param {object} node
     *
     */
    formatWithinNode(node) {
        // Not implemented.
    }


    /**
     * format PatternClause nodes.
     *
     * @param {object} node
     *
     */
    formatPatternClauseNode(node) {
        // Not implemented.
    }


    /**
     * format OutputRateLimit nodes.
     *
     * @param {object} node
     *
     */
    formatOutputRateLimitNode(node) {
        // Not implemented.
    }


    /**
     * format Forever nodes.
     *
     * @param {object} node
     *
     */
    formatForeverNode(node) {
        // Not implemented.
    }

}

export default FormattingUtil;
