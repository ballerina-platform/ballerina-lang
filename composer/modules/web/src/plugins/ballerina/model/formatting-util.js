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

const TAB = "\t";
const SPACE_TAB = "   ";
const TOPLEVEL_BLOCK_SPACE = "\r\n\r\n";
const SINGLE_SPACE = " ";
const NEW_LINE = "\r\n";
const EMPTY_SPACE = "";

class FormattingUtil {

    getWhiteSpaces(column) {
        let whiteSpaces = "";
        for (let i = 0; i < column; i++) {
            whiteSpaces += SINGLE_SPACE;
        }
        return whiteSpaces;
    }

    getWhiteSpaceCount(ws) {
        let whiteSpaces = ws.split(SINGLE_SPACE);
        return whiteSpaces.length - 1;
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
        // Not implemented.
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
        if (node.ws) {
            node.ws[0].ws = NEW_LINE;
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

        if (node.parent.kind === 'CompilationUnit') {
            let findIndex = function (topLevelNode) {
                return topLevelNode.id === node.id;
            };
            let endpointIndex = node.parent.topLevelNodes.findIndex(findIndex);

            // Add ws according to the position and sorted order.
            if (endpointIndex > 0) {
                node.position.endColumn = 1;
                node.position.startColumn = 1;

                node.ws[0].ws = TOPLEVEL_BLOCK_SPACE;
                if (node.configurationExpression) {
                    node.configurationExpression.ws[0].ws = SINGLE_SPACE;
                    if (node.configurationExpression.keyValuePairs.length <= 0) {
                        node.configurationExpression.ws[node.configurationExpression.ws.length - 1].ws =
                            NEW_LINE + SPACE_TAB + NEW_LINE;
                    } else {
                        node.configurationExpression.ws[node.configurationExpression.ws.length - 1].ws = NEW_LINE;
                    }
                }
            } else {
                node.position.endColumn = 1;
                node.position.startColumn = 1;

                node.ws[0].ws = EMPTY_SPACE;
                if (node.configurationExpression) {
                    node.configurationExpression.ws[0].ws = SINGLE_SPACE;
                    if (node.configurationExpression.keyValuePairs.length <= 0) {
                        node.configurationExpression.ws[node.configurationExpression.ws.length - 1].ws =
                            NEW_LINE + SPACE_TAB + NEW_LINE;
                    } else {
                        node.configurationExpression.ws[node.configurationExpression.ws.length - 1].ws = NEW_LINE;
                    }
                }
            }
        } else {
            node.position.startColumn = node.parent.position.startColumn + this.getWhiteSpaceCount(SPACE_TAB);

            node.ws[0].ws = TOPLEVEL_BLOCK_SPACE + this.getWhiteSpaces(node.position.startColumn);
            if (node.configurationExpression) {
                node.configurationExpression.ws[0].ws = SINGLE_SPACE;

                if (node.configurationExpression.keyValuePairs.length <= 0) {
                    node.configurationExpression.ws[node.configurationExpression.ws.length - 1].ws =
                        NEW_LINE + this.getWhiteSpaces(node.position.startColumn) + NEW_LINE +
                        this.getWhiteSpaces(node.position.startColumn);
                } else {
                    node.configurationExpression.ws[node.configurationExpression.ws.length - 1].ws =
                        NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
                }
            }
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

        let findIndex = function (topLevelNode) {
            return topLevelNode.id === node.id;
        };

        if (node.parent.kind === 'CompilationUnit') {
            let functionIndex = node.parent.topLevelNodes.findIndex(findIndex);

            // Add ws according to the position and sorted order.
            if (functionIndex > 0) {
                node.position.endColumn = 1;
                node.position.startColumn = 1;

                node.ws[0].ws = TOPLEVEL_BLOCK_SPACE;
                node.ws[node.ws.length - 2].ws = SINGLE_SPACE;

                if (node.body.statements.length <= 0 && node.endpointNodes.length <= 0 && node.workers.length <= 0) {
                    node.ws[node.ws.length - 1].ws = NEW_LINE + SPACE_TAB + NEW_LINE;
                } else {
                    node.ws[node.ws.length - 1].ws = NEW_LINE;
                }
            } else {
                node.position.endColumn = 1;
                node.position.startColumn = 1;

                node.ws[0].ws = EMPTY_SPACE;
                node.ws[node.ws.length - 2].ws = SINGLE_SPACE;

                if (node.body.statements.length <= 0 && node.endpointNodes.length <= 0 && node.workers.length <= 0) {
                    node.ws[node.ws.length - 1].ws = NEW_LINE + SPACE_TAB + NEW_LINE;
                } else {
                    node.ws[node.ws.length - 1].ws = NEW_LINE;
                }
            }
        } else {
            node.position.startColumn = node.parent.position.startColumn + this.getWhiteSpaceCount(SPACE_TAB);

            node.ws[0].ws = TOPLEVEL_BLOCK_SPACE + this.getWhiteSpaces(node.position.startColumn);
            node.ws[node.ws.length - 2].ws = SINGLE_SPACE;

            if (node.body.statements.length <= 0 && node.endpointNodes.length <= 0 && node.workers.length <= 0) {
                node.ws[node.ws.length - 1].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn) + NEW_LINE +
                    this.getWhiteSpaces(node.position.startColumn);
            } else {
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

        let findIndex = function (topLevelNode) {
            return topLevelNode.id === node.id;
        };

        let importIndex = node.parent.topLevelNodes.findIndex(findIndex);

        if (importIndex > 0) {
            node.ws[0].ws = NEW_LINE;
        } else {
            node.ws[0].ws = EMPTY_SPACE;
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

        node.position.startColumn = node.parent.position.startColumn + this.getWhiteSpaceCount(SPACE_TAB);

        node.ws[0].ws = TOPLEVEL_BLOCK_SPACE + this.getWhiteSpaces(node.position.startColumn);
        node.ws[node.ws.length - 2].ws = SINGLE_SPACE;

        if (node.body.statements.length <= 0 && node.workers.length <= 0 && node.endpointNodes.length <= 0) {
            node.ws[node.ws.length - 1].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn) + NEW_LINE +
                this.getWhiteSpaces(node.position.startColumn);
        } else {
            node.ws[node.ws.length - 1].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
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

        let findIndex = function (topLevelNode) {
            return topLevelNode.id === node.id;
        };

        let serviceIndex = node.parent.topLevelNodes.findIndex(findIndex);

        // Add ws according to the position and sorted order.
        if (serviceIndex > 0) {
            node.position.endColumn = 1;
            node.position.startColumn = 1;

            node.ws[0].ws = TOPLEVEL_BLOCK_SPACE;
            node.ws[node.ws.length - 2].ws = SINGLE_SPACE;

            if (node.resources.length <= 0 && node.variables.length <= 0 && node.endpointNodes.length <= 0 &&
                node.namespaceDeclarations.length <= 0) {
                node.ws[node.ws.length - 1].ws = NEW_LINE + SPACE_TAB + NEW_LINE;
            } else {
                node.ws[node.ws.length - 1].ws = NEW_LINE;
            }
        } else {
            node.position.endColumn = 1;
            node.position.startColumn = 1;

            node.ws[0].ws = EMPTY_SPACE;
            node.ws[node.ws.length - 2].ws = SINGLE_SPACE;

            if (node.resources.length <= 0 && node.variables.length <= 0 && node.endpointNodes.length <= 0 &&
                node.namespaceDeclarations.length <= 0) {
                node.ws[node.ws.length - 1].ws = NEW_LINE + SPACE_TAB + NEW_LINE;
            } else {
                node.ws[node.ws.length - 1].ws = NEW_LINE;
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
        // Not implemented.
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

        if (node.parent.kind === 'VariableDef') {
            if (node.typeNode && node.typeNode.ws) {
                node.typeNode.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.parent.position.startColumn);
            }
        } else if (node.parent.kind === "CompilationUnit") {
            let findIndex = function (topLevelNode) {
                return topLevelNode.id === node.id;
            };
            let variableIndex = node.parent.topLevelNodes.findIndex(findIndex);
            if (variableIndex > 0) {
                node.typeNode.ws[0].ws = NEW_LINE;
            } else {
                node.typeNode.ws[0].ws = EMPTY_SPACE;
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

        if (node.parent.kind === 'ExpressionStatement') {
            if (node.expression && node.expression.ws) {
                node.expression.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.parent.position.startColumn);
            } else {
                node.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.parent.position.startColumn);
            }
        } else {
            if (node.expression && node.expression.ws) {
                node.expression.ws[0].ws = SINGLE_SPACE;
            } else {
                node.ws[0].ws = SINGLE_SPACE;
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

        if (node.parent.kind === 'Endpoint') {
            node.position.startColumn = node.parent.position.startColumn + this.getWhiteSpaceCount(SPACE_TAB);
            for (let i = 0; i < node.keyValuePairs.length; i++) {
                node.keyValuePairs[i].key.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
                node.keyValuePairs[i].value.ws[0].ws = SINGLE_SPACE;
                node.keyValuePairs[i].ws = EMPTY_SPACE;
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

        node.position.startColumn = node.parent.position.startColumn + this.getWhiteSpaceCount(SPACE_TAB);

        if (node.variable && node.variable.ws) {
            node.variable.ws[0].ws = NEW_LINE + this.getWhiteSpaces(node.position.startColumn);
        }

        if (node.expression && node.variable.ws) {
            node.expression.ws[0].ws = SINGLE_SPACE;
        }

        if (node.ws) {
            node.ws[0].ws = SINGLE_SPACE;
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
            endColumn: node.parent.position.endColumn,
            startColumn: node.parent.position.startColumn,
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

        node.position.startColumn = node.parent.position.startColumn + this.getWhiteSpaceCount(SPACE_TAB);

        if (node.ws) {
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

        if (node.parent.kind !== 'CompilationUnit') {
            node.position.startColumn = node.parent.position.startColumn + this.getWhiteSpaceCount(SPACE_TAB);
        } else {
            node.position.endColumn = 1;
            node.position.startColumn = 1;
        }

        if (node.ws) {
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
        // Not implemented.
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
        // Not implemented.
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
