/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import SimpleBBox from 'ballerina/model/view/simple-bounding-box';
import TreeUtil from './../../../model/tree-util';
import SemanticErrorRenderingVisitor from './../../../visitors/semantic-errors-rendering-visitor';

class ErrorRenderingUtil {

    getSemanticErrorsOfNode(node) {
        const semanticErrorRender = new SemanticErrorRenderingVisitor();
        node.accept(semanticErrorRender);
        let errors = [];
        if (semanticErrorRender.getErrors()) {
            errors = semanticErrorRender.getErrors();
        }
        return errors;
    }

    setErrorToNode(node, errors, errorBbox, placement) {
        const overlayComponents = {
            kind: 'SemanticErrorPopup',
            props: {
                key: node.getID(),
                model: node,
                bBox: errorBbox,
                errors,
                placement,
            },
        };
        node.viewState.showErrors = true;
        node.viewState.errors = overlayComponents;
    }
    /**
     * Calculate error positions of statement nodes
     * @param node
     */
    placeErrorForStatementComponents(node) {
        if (node.parent &&
            node.parent.parent &&
            node.parent.parent.parent &&
            !TreeUtil.isResource(node.parent.parent) &&
            (node.parent.parent.parent.initFunction)) {
            // Do not show errors in the InitFunction of the service
        } else if (node.parent.kind === 'Service' || node.parent.kind === 'Connector') {
            const viewState = node.viewState;
            // Check for errors in the model
            const errors = this.getSemanticErrorsOfNode(node);
            if (errors.length > 0) {
                if (node.parent.viewState.variablesExpanded) {
                    const errorBbox = new SimpleBBox();
                    errorBbox.x = viewState.bBox.x;
                    errorBbox.y = viewState.bBox.y;
                    this.setErrorToNode(node, errors, errorBbox, 'top');
                }
            }
        } else {
            const viewState = node.viewState;
            // Check for errors in the model
            const errors = this.getSemanticErrorsOfNode(node);
            if (errors.length > 0) {
                const errorBbox = new SimpleBBox();
                if (TreeUtil.isConnectorDeclaration(node)) {
                    errorBbox.x = viewState.bBox.x;
                    errorBbox.y = viewState.bBox.y;
                } else {
                    errorBbox.x = viewState.components['statement-box'].x;
                    errorBbox.y = viewState.components['statement-box'].y;
                }
                this.setErrorToNode(node, errors, errorBbox, 'top');
            }
        }
    }

    /**
     * Calculate error positions of compound statement nodes
     * @param node
     */
    placeErrorForCompoundStatementComponents(node) {
        const viewState = node.viewState;
        // Check for errors in the model
        const errors = this.getSemanticErrorsOfNode(node);
        if (errors.length > 0) {
            const errorBbox = new SimpleBBox();
            errorBbox.x = viewState.components['statement-box'].x;
            errorBbox.y = viewState.components['statement-box'].y;
            this.setErrorToNode(node, errors, errorBbox, 'top');
        }
    }

    /**
     * Calculate error position of Action nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForActionNode(node) {
        // Here we skip the init action
        if (node.id !== node.parent.initAction.id) {
            this.placeErrorForResourceNode(node);
        }
    }


    /**
     * Calculate error position of Annotation nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAnnotationNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of AnnotationAttachment nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAnnotationAttachmentNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of AnnotationAttribute nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAnnotationAttributeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Catch nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForCatchNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of CompilationUnit nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForCompilationUnitNode(node) {
        // Position imports
        const imports = node.filterTopLevelNodes((child) => {
            return TreeUtil.isImport(child);
        });
        imports.forEach((importDec) => {
            importDec.viewState.errors = {};
            if (node.viewState.importsExpanded) {
                // Check for errors in the globals
                const errors = this.getSemanticErrorsOfNode(importDec);
                if (errors.length > 0) {
                    const errorBbox = new SimpleBBox();
                    errorBbox.x = importDec.viewState.bBox.x;
                    errorBbox.y = importDec.viewState.bBox.y + 9;
                    this.setErrorToNode(importDec, errors, errorBbox, 'top');
                }
            }
        });

        // Position the global variables
        const globals = node.filterTopLevelNodes((child) => {
            return TreeUtil.isVariable(child);
        });
        globals.forEach((globalDec) => {
            globalDec.viewState.errors = {};
            if (node.viewState.globalsExpanded) {
                // Check for errors in the globals
                const errors = this.getSemanticErrorsOfNode(globalDec);
                if (errors.length > 0) {
                    const errorBbox = new SimpleBBox();
                    errorBbox.x = globalDec.viewState.bBox.x;
                    errorBbox.y = globalDec.viewState.bBox.y + 9;
                    this.setErrorToNode(globalDec, errors, errorBbox, 'top');
                }
            }
        });

        // If the globals are not expanded show all the errors of the globals together
        node.viewState.errorsForGlobals = {};
        if (!node.viewState.globalsExpanded || node.viewState.globalsExpanded === undefined) {
            const errorListForGlobals = [];
            globals.forEach((globalDec) => {
                const errors = this.getSemanticErrorsOfNode(globalDec);
                if (errors.length > 0) {
                    errorListForGlobals.push(errors[0]);
                }
            });
            if (errorListForGlobals.length > 0) {
                const errorBbox = new SimpleBBox();
                errorBbox.x = node.viewState.components.globalsBbox.x;
                errorBbox.y = node.viewState.components.globalsBbox.y;
                const overlayComponents = {
                    kind: 'SemanticErrorPopup',
                    props: {
                        key: node.getID(),
                        model: node,
                        bBox: errorBbox,
                        errors: errorListForGlobals,
                        placement: 'bottom',
                    },
                };
                node.viewState.showGlobalErrors = true;
                node.viewState.errorsForGlobals = overlayComponents;
            }
        }

        // If the imports are not expanded show all the errors of the imports together
        node.viewState.errorsForImports = {};
        if (!node.viewState.importsExpanded || node.viewState.importsExpanded === undefined) {
            const errorListForImports = [];
            imports.forEach((importDec) => {
                const errors = this.getSemanticErrorsOfNode(importDec);
                if (errors.length > 0) {
                    errorListForImports.push(errors[0]);
                }
            });
            if (errorListForImports.length > 0) {
                const errorBbox = new SimpleBBox();
                errorBbox.x = node.viewState.components.importsBbox.x;
                errorBbox.y = node.viewState.components.importsBbox.y;
                const overlayComponents = {
                    kind: 'SemanticErrorPopup',
                    props: {
                        key: node.getID(),
                        model: node,
                        bBox: errorBbox,
                        errors: errorListForImports,
                        placement: 'bottom',
                    },
                };
                node.viewState.showImportErrors = true;
                node.viewState.errorsForImports = overlayComponents;
            }
        }
    }


    /**
     * Calculate error position of Connector nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForConnectorNode(node) {
        this.placeErrorForServiceNode(node);
    }


    /**
     * Calculate error position of Enum nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForEnumNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Function nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForFunctionNode(node) {
        if (!(node.parent.initFunction && node.parent.initFunction === node)) {
            const viewState = node.viewState;
            const cmp = viewState.components;
            // Check for errors in the model
            const errors = node.errors;
            if (errors.length > 0) {
                const errorBbox = new SimpleBBox();
                errorBbox.x = viewState.bBox.x;
                errorBbox.y = viewState.bBox.y + cmp.annotation.h;
                this.setErrorToNode(node, errors, errorBbox, 'top');
            }

            // Positioning argument parameters
            if (node.getParameters()) {
                if (node.getParameters().length > 0) {
                    for (let i = 0; i < node.getParameters().length; i++) {
                        const argument = node.getParameters()[i];
                        this.placeErrorForTitleNode(argument);
                    }
                }
            }

            // Positioning return types
            if (node.getReturnParameters()) {
                if (node.getReturnParameters().length > 0) {
                    for (let i = 0; i < node.getReturnParameters().length; i++) {
                        const returnType = node.getReturnParameters()[i];
                        this.placeErrorForTitleNode(returnType);
                    }
                }
            }
        }
    }

    placeErrorForTitleNode(parameter) {
        const viewState = parameter.viewState;
        // Check for errors in the model
        const errors = this.getSemanticErrorsOfNode(parameter);
        if (errors.length > 0) {
            const errorBbox = new SimpleBBox();
            errorBbox.x = viewState.bBox.x;
            errorBbox.y = viewState.bBox.y;
            this.setErrorToNode(parameter, errors, errorBbox, 'top');
        }
    }
    /**
     * Calculate error position of Identifier nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForIdentifierNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Import nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForImportNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Package nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForPackageNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of PackageDeclaration nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForPackageDeclarationNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of RecordLiteralKeyValue nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForRecordLiteralKeyValueNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Resource nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForResourceNode(node) {
        this.placeErrorForFunctionNode(node);
    }


    /**
     * Calculate error position of Retry nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForRetryNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of Service nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForServiceNode(node) {
        const viewState = node.viewState;
        // Check for errors in the model
        const errors = node.errors;
        node.viewState.errors = {};
        if (errors.length > 0) {
            const errorBbox = new SimpleBBox();
            errorBbox.x = viewState.bBox.x;
            errorBbox.y = viewState.bBox.y + viewState.components.annotation.h;
            this.setErrorToNode(node, errors, errorBbox, 'top');
        }
        // If the globals are not expanded show all the errors of the globals together
        node.viewState.errorsForGlobals = {};
        if (!node.viewState.variablesExpanded || node.viewState.variablesExpanded === undefined) {
            const errorListForGlobals = [];
            let variables = [];
            if (TreeUtil.isService(node)) {
                variables = node.filterVariables((statement) => {
                    return !TreeUtil.isConnectorDeclaration(statement);
                });
            } else if (TreeUtil.isConnector(node)) {
                variables = node.filterVariableDefs((statement) => {
                    return !TreeUtil.isConnectorDeclaration(statement);
                });
            }
            variables.forEach((globalDec) => {
                const errorForVar = this.getSemanticErrorsOfNode(globalDec);
                if (errorForVar.length > 0) {
                    errorListForGlobals.push(errorForVar[0]);
                }
            });
            if (errorListForGlobals.length > 0) {
                const errorBbox = new SimpleBBox();
                errorBbox.x = node.viewState.components.globalsBbox.x - 130;
                errorBbox.y = node.viewState.components.globalsBbox.y;
                const overlayComponents = {
                    kind: 'SemanticErrorPopup',
                    props: {
                        key: node.getID(),
                        model: node,
                        bBox: errorBbox,
                        errors: errorListForGlobals,
                        placement: 'bottom',
                    },
                };
                node.viewState.showGlobalErrors = true;
                node.viewState.errorsForGlobals = overlayComponents;
            }
        }
    }


    /**
     * Calculate error position of Struct nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForStructNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Variable nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForVariableNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Worker nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForWorkerNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Xmlns nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlnsNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of AnnotationAttachmentAttribute nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAnnotationAttachmentAttributeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of AnnotationAttachmentAttributeValue nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAnnotationAttachmentAttributeValueNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of ArrayLiteralExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForArrayLiteralExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of BinaryExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForBinaryExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of ConnectorInitExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForConnectorInitExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of FieldBasedAccessExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForFieldBasedAccessExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of IndexBasedAccessExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForIndexBasedAccessExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Invocation nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForInvocationNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Lambda nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForLambdaNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Literal nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of RecordLiteralExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForRecordLiteralExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of SimpleVariableRef nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForSimpleVariableRefNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of StringTemplateLiteral nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForStringTemplateLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of TernaryExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTernaryExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of TypeCastExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTypeCastExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of TypeConversionExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTypeConversionExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of UnaryExpr nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForUnaryExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of XmlQname nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlQnameNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of XmlAttribute nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlAttributeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of XmlQuotedString nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlQuotedStringNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of XmlElementLiteral nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlElementLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of XmlTextLiteral nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlTextLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of XmlCommentLiteral nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlCommentLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of XmlPiLiteral nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForXmlPiLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Abort nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAbortNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of Assignment nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForAssignmentNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of Block nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForBlockNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Break nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForBreakNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of Next nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForNextNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of ExpressionStatement nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForExpressionStatementNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of ForkJoin nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForForkJoinNode(node) {
        const joinStmt = node.getJoinBody();
        const timeoutStmt = node.getTimeoutBody();

        this.placeErrorForCompoundStatementComponents(node);

        if (joinStmt) {
            this.placeErrorForCompoundStatementComponents(joinStmt);
        }

        if (timeoutStmt) {
            this.placeErrorForCompoundStatementComponents(timeoutStmt);
        }
    }


    /**
     * Calculate error position of If nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForIfNode(node) {
        const elseStatement = node.elseStatement;
        this.placeErrorForCompoundStatementComponents(node);
        if (elseStatement) {
            this.placeErrorForCompoundStatementComponents(elseStatement);
        }
    }


    /**
     * Calculate error position of Reply nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForReplyNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Return nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForReturnNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of Comment nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForCommentNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Throw nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForThrowNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of Transaction nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTransactionNode(node) {
        const abortedBody = node.abortedBody;
        const committedBody = node.committedBody;
        const failedBody = node.failedBody;
        const transactionBody = node.transactionBody;

        this.placeErrorForCompoundStatementComponents(node);
        // Set the position of the transaction body
        if (transactionBody) {
            this.placeErrorForCompoundStatementComponents(transactionBody);
        }

        // Set the position of the failed body
        if (failedBody) {
            this.placeErrorForCompoundStatementComponents(failedBody);
        }

        // Set the position of the aborted body
        if (abortedBody) {
            this.placeErrorForCompoundStatementComponents(abortedBody);
        }

        // Set the position of the aborted body
        if (committedBody) {
            this.placeErrorForCompoundStatementComponents(committedBody);
        }
    }


    /**
     * Calculate error position of Transform nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTransformNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of Try nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForTryNode(node) {
        // Position the try node
        this.placeErrorForCompoundStatementComponents(node);

        const catchBlocks = node.catchBlocks;
        const finallyBody = node.finallyBody;

        for (let itr = 0; itr < catchBlocks.length; itr++) {
            // Position the compound statement components of catch block
            this.placeErrorForCompoundStatementComponents(catchBlocks[itr]);
        }

        if (finallyBody) {
            this.placeErrorForCompoundStatementComponents(finallyBody);
        }
    }


    /**
     * Calculate error position of VariableDef nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForVariableDefNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of While nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForWhileNode(node) {
        this.placeErrorForCompoundStatementComponents(node);
    }


    /**
     * Calculate error position of WorkerReceive nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForWorkerReceiveNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of WorkerSend nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForWorkerSendNode(node) {
        this.placeErrorForStatementComponents(node);
    }


    /**
     * Calculate error position of ArrayType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForArrayTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of BuiltInRefType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForBuiltInRefTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of ConstrainedType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForConstrainedTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of FunctionType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForFunctionTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of UserDefinedType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForUserDefinedTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate error position of ValueType nodes.
     *
     * @param {object} node
     *
     */
    placeErrorForValueTypeNode(node) {
        // Not implemented.
    }


}

export default ErrorRenderingUtil;
