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

import _ from 'lodash';
import TreeUtil from './../../../model/tree-util';
import SimpleBBox from './../../../ast/simple-bounding-box';

class PositioningUtil {

    setConfig(config) {
        this.config = config;
    }

    positionStatementComponents(node) {
        const viewState = node.viewState;
        const initialExpression = TreeUtil.isVariableDef(node) || TreeUtil.isAssignment(node) ?
            node.variable.initialExpression : undefined;
        if (TreeUtil.isConnectorInitExpr(initialExpression)) {

            // Filter the connector declaration statements
            const connectorDecls = _.filter(node.parent.statements, (statement) => {
                const initExpr = TreeUtil.isVariableDef(statement) || TreeUtil.isAssignment(statement) ?
                    statement.variable.initialExpression : undefined;
                return TreeUtil.isConnectorInitExpr(initExpr);
            });
            const nodeIndex = _.indexOf(connectorDecls, node);
            const bBox = initialExpression.viewState.bBox;
            const parentBlockNode = node.parent;

            // Here we only set the x value, since it is based on the other connector Decls. y position is set at the
            // function node positioning, etc
            let xVal = parentBlockNode.viewState.bBox.x + parentBlockNode.viewState.bBox.w
                + this.config.lifeLine.gutter.h;
            if (nodeIndex > 0) {
                xVal = connectorDecls[nodeIndex - 1].variable.initialExpression.viewState.bBox.x
                    + connectorDecls[nodeIndex - 1].variable.initialExpression.viewState.bBox.w
                    + this.config.lifeLine.gutter.h;
            }
            bBox.x = xVal;
        } else {
            viewState.components['drop-zone'].x = viewState.bBox.x;
            viewState.components['drop-zone'].y = viewState.bBox.y;
            viewState.components['statement-box'].x = viewState.bBox.x;
            viewState.components['statement-box'].y = viewState.bBox.y + viewState.components['drop-zone'].h;
            viewState.components.text.x = viewState.components['statement-box'].x +
                (viewState.components['statement-box'].w / 2);
            viewState.components.text.y = viewState.components['statement-box'].y +
                (viewState.components['statement-box'].h / 2);
        }
    }

    positionCompoundStatementComponents(node) {
        const viewState = node.viewState;
        const x = viewState.bBox.x;
        let y;

        if (TreeUtil.isBlock(node)) {
            y = viewState.bBox.y - viewState.components['block-header'].h;
        } else {
            y = viewState.bBox.y;
        }
        viewState.components['drop-zone'].x = x;
        viewState.components['drop-zone'].y = y;
        viewState.components['statement-box'].x = viewState.bBox.x;
        viewState.components['statement-box'].y = y + viewState.components['drop-zone'].h;
    }


    /**
     * Calculate position of Action nodes.
     *
     * @param {object} node Action object
     */
    positionActionNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Annotation nodes.
     *
     * @param {object} node Annotation object
     */
    positionAnnotationNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of AnnotationAttachment nodes.
     *
     * @param {object} node AnnotationAttachment object
     */
    positionAnnotationAttachmentNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of AnnotationAttribute nodes.
     *
     * @param {object} node AnnotationAttribute object
     */
    positionAnnotationAttributeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Catch nodes.
     *
     * @param {object} node Catch object
     */
    positionCatchNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of CompilationUnit nodes.
     *
     * @param {object} node CompilationUnit object
     */
    positionCompilationUnitNode(node) {
        let width = 0;
        let height = 0;
        // filter out visible children from top level nodes.
        const children = node.filterTopLevelNodes((child) => {
            return TreeUtil.isPackageDeclaration(child) || TreeUtil.isFunction(child) || TreeUtil.isService(child)
                || TreeUtil.isStruct(child);
        });

        children.forEach((child) => {
            // we will get the maximum node's width.
            if (width <= child.viewState.bBox.w) {
                width = child.viewState.bBox.w;
            }
            // for each top level node set x and y.
            child.viewState.bBox.x = this.config.panel.wrapper.gutter.v;
            child.viewState.bBox.y = height + this.config.panel.wrapper.gutter.h;
            height = this.config.panel.wrapper.gutter.h + child.viewState.bBox.h + height;
        });

        height = (height > node.viewState.container.height) ? height : node.viewState.container.height;
        width = (width > node.viewState.container.width) ? width : node.viewState.container.width;

        // re-adjust the width of each node to fill the container.
        children.forEach((child) => {
            child.viewState.bBox.w = width - (this.config.panel.wrapper.gutter.h * 2);
        });

        node.viewState.bBox.h = height;
        node.viewState.bBox.w = width;
    }


    /**
     * Calculate position of Connector nodes.
     *
     * @param {object} node Connector object
     */
    positionConnectorNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Enum nodes.
     *
     * @param {object} node Enum object
     */
    positionEnumNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Function nodes.
     *
     * @param {object} node Function object
     */
    positionFunctionNode(node) {
        const viewState = node.viewState;
        const functionBody = node.body;
        const funcBodyViewState = functionBody.viewState;
        const cmp = viewState.components;

        // position the components.
        funcBodyViewState.bBox.x = viewState.bBox.x + this.config.panel.body.padding.left;
        funcBodyViewState.bBox.y = viewState.bBox.y + cmp.heading.h + this.config.panel.body.padding.top
            + this.config.lifeLine.head.height;

        cmp.defaultWorker.x = funcBodyViewState.bBox.x + ((funcBodyViewState.bBox.w - this.config.lifeLine.width) / 2);
        cmp.defaultWorker.y = funcBodyViewState.bBox.y - this.config.lifeLine.head.height;

        // position the children
        const body = node.getBody();
        body.viewState.bBox.x = viewState.bBox.x + this.config.panel.body.padding.left;
        body.viewState.bBox.y = viewState.bBox.y + cmp.heading.h + this.config.panel.body.padding.top +
                                this.config.lifeLine.head.height;

        // Positioning argument parameters
        if (node.getParameters()) {
            cmp.argParameterHolder.openingParameter.x = viewState.bBox.x + viewState.titleWidth +
                this.config.panel.heading.title.margin.right + this.config.panelHeading.iconSize.width
                + this.config.panelHeading.iconSize.padding;
            cmp.argParameterHolder.openingParameter.y = viewState.bBox.y + cmp.annotation.h;

            // Positioning the resource parameters
            let nextXPositionOfParameter = cmp.argParameterHolder.openingParameter.x
                + cmp.argParameterHolder.openingParameter.w;
            if (node.getParameters().length > 0) {
                for (let i = 0; i < node.getParameters().length; i++) {
                    const argument = node.getParameters()[i];
                    nextXPositionOfParameter = this.createPositionForTitleNode(argument, nextXPositionOfParameter,
                        (viewState.bBox.y + viewState.components.annotation.h));
                }
            }

            // Positioning the closing bracket component of the parameters.
            cmp.argParameterHolder.closingParameter.x = nextXPositionOfParameter + 130;
            cmp.argParameterHolder.closingParameter.y = viewState.bBox.y + cmp.annotation.h;
        }

        // Positioning return types
        if (node.getReturnParameters()) {
            cmp.returnParameterHolder.returnTypesIcon.x = cmp.argParameterHolder.closingParameter.x
                + cmp.argParameterHolder.closingParameter.w + 10;
            cmp.returnParameterHolder.returnTypesIcon.y = viewState.bBox.y + viewState.components.annotation.h + 18;

            // Positioning the opening bracket component of the return types.
            cmp.returnParameterHolder.openingReturnType.x = cmp.returnParameterHolder.returnTypesIcon.x
                + cmp.returnParameterHolder.returnTypesIcon.w;
            cmp.returnParameterHolder.openingReturnType.y = viewState.bBox.y + viewState.components.annotation.h;

            // Positioning the resource parameters
            let nextXPositionOfReturnType = cmp.returnParameterHolder.openingReturnType.x
                + cmp.returnParameterHolder.openingReturnType.w;
            if (node.getReturnParameters().length > 0) {
                for (let i = 0; i < node.getReturnParameters().length; i++) {
                    const returnType = node.getReturnParameters()[i];
                    nextXPositionOfReturnType = this.createPositionForTitleNode(returnType, nextXPositionOfReturnType,
                        (viewState.bBox.y + viewState.components.annotation.h));
                }
            }

            // Positioning the closing bracket component of the parameters.
            cmp.returnParameterHolder.closingReturnType.x = nextXPositionOfReturnType + 130;
            cmp.returnParameterHolder.closingReturnType.y = viewState.bBox.y + viewState.components.annotation.h;

            // Set the y position of the connector declarations
            // Here we only set the y value, since it is based on the default worker position. x position is set at the
            // block node positioning
            const statements = node.body.statements;
            const connectorDecls = _.filter(statements, (statement) => {
                const initialExpression = TreeUtil.isVariableDef(statement) || TreeUtil.isAssignment(statement) ?
                    statement.variable.initialExpression : undefined;
                return initialExpression ? TreeUtil.isConnectorInitExpr(initialExpression) : false;
            });

            connectorDecls.forEach((conNode) => {
                const declaration = conNode.variable.initialExpression;
                declaration.viewState.bBox.w = node.viewState.components.defaultWorker.w;
                declaration.viewState.bBox.h = node.viewState.components.defaultWorker.h;
                declaration.viewState.bBox.y = node.viewState.components.defaultWorker.y;
            });
        }
    }

    /**
     * Sets positioning for a parameter.
     *
     */
    createPositionForTitleNode(parameter, x, y) {
        const viewState = parameter.viewState;
        // Positioning the parameter
        viewState.bBox.x = x;
        viewState.bBox.y = y;

        // Positioning the delete icon
        viewState.components.deleteIcon.x = x + viewState.w;
        viewState.components.deleteIcon.y = y;

        return viewState.components.deleteIcon.x + viewState.components.deleteIcon.w;
    }
    /**
     * Calculate position of Identifier nodes.
     *
     * @param {object} node Identifier object
     */
    positionIdentifierNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Import nodes.
     *
     * @param {object} node Import object
     */
    positionImportNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Package nodes.
     *
     * @param {object} node Package object
     */
    positionPackageNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of PackageDeclaration nodes.
     *
     * @param {object} node PackageDeclaration object
     */
    positionPackageDeclarationNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of RecordLiteralKeyValue nodes.
     *
     * @param {object} node RecordLiteralKeyValue object
     */
    positionRecordLiteralKeyValueNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Resource nodes.
     *
     * @param {object} node Resource object
     */
    positionResourceNode(node) {
        const viewState = node.viewState;
        const functionBody = node.body;
        const funcBodyViewState = functionBody.viewState;
        const cmp = viewState.components;

        // position the components.
        funcBodyViewState.bBox.x = viewState.bBox.x + this.config.panel.body.padding.left;
        funcBodyViewState.bBox.y = viewState.bBox.y + cmp.heading.h + this.config.panel.body.padding.top
            + this.config.lifeLine.head.height;

        cmp.defaultWorker.x = funcBodyViewState.bBox.x + ((funcBodyViewState.bBox.w - this.config.lifeLine.width) / 2);
        cmp.defaultWorker.y = funcBodyViewState.bBox.y - this.config.lifeLine.head.height;

        // position the children
        const body = node.getBody();
        body.viewState.bBox.x = viewState.bBox.x + this.config.panel.body.padding.left;
        body.viewState.bBox.y = viewState.bBox.y + cmp.heading.h + this.config.panel.body.padding.top +
            this.config.lifeLine.head.height;

        // Positioning argument parameters
        if (node.getParameters()) {
            cmp.argParameterHolder.openingParameter.x = viewState.bBox.x + viewState.titleWidth +
                this.config.panel.heading.title.margin.right + this.config.panelHeading.iconSize.width
                + this.config.panelHeading.iconSize.padding;
            cmp.argParameterHolder.openingParameter.y = viewState.bBox.y + cmp.annotation.h;

            // Positioning the resource parameters
            let nextXPositionOfParameter = cmp.argParameterHolder.openingParameter.x
                + cmp.argParameterHolder.openingParameter.w;
            if (node.getParameters().length > 0) {
                for (let i = 0; i < node.getParameters().length; i++) {
                    const argument = node.getParameters()[i];
                    nextXPositionOfParameter = this.createPositionForTitleNode(argument, nextXPositionOfParameter,
                        (viewState.bBox.y + viewState.components.annotation.h));
                }
            }

            // Positioning the closing bracket component of the parameters.
            cmp.argParameterHolder.closingParameter.x = nextXPositionOfParameter + 130;
            cmp.argParameterHolder.closingParameter.y = viewState.bBox.y + cmp.annotation.h;
        }
    }


    /**
     * Calculate position of Retry nodes.
     *
     * @param {object} node Retry object
     */
    positionRetryNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of Service nodes.
     *
     * @param {object} node Service object
     */
    positionServiceNode(node) {
        const viewState = node.viewState;
        const cmp = viewState.components;

        // Position the components
        cmp.defaultWorker.x = viewState.bBox.x + ((viewState.bBox.w - this.config.lifeLine.width) / 2);
        cmp.defaultWorker.y = viewState.bBox.y - this.config.lifeLine.head.height;

        // Position the transport nodes
        const transportLine = !_.isNil(viewState.components.transportLine) ?
            viewState.components.transportLine : { x: 0, y: 0 };
        transportLine.x = viewState.bBox.x - 5;
        transportLine.y = viewState.bBox.y + 30;

        // Position the resources
        const resources = node.getResources();
        resources.map((resource, index) => {
            const resourcebBox = resources[index].viewState.bBox;
            resourcebBox.x = viewState.bBox.x + 50;
            if (index === 0) {
                // Positioning the first resource
                resourcebBox.y = viewState.bBox.y + 150;
            } else {
                const previousResource = resources[index - 1];
                const previousResourceBBox = previousResource.viewState.bBox;
                // Check if resource is collapsed
                let height = previousResourceBBox.h;
                if (previousResource.viewState.collapsed) {
                    height = 0;
                }
                resourcebBox.y = previousResourceBBox.y + height +
                    this.config.panel.wrapper.gutter.v;
            }
            resourcebBox.w = resource.parent.viewState.bBox.w - 100;
        });
    }


    /**
     * Calculate position of Struct nodes.
     *
     * @param {object} node Struct object
     */
    positionStructNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Variable nodes.
     *
     * @param {object} node Variable object
     */
    positionVariableNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Worker nodes.
     *
     * @param {object} node Worker object
     */
    positionWorkerNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Xmlns nodes.
     *
     * @param {object} node Xmlns object
     */
    positionXmlnsNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of AnnotationAttachmentAttributeValue nodes.
     *
     * @param {object} node AnnotationAttachmentAttributeValue object
     */
    positionAnnotationAttachmentAttributeValueNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ArrayLiteralExpr nodes.
     *
     * @param {object} node ArrayLiteralExpr object
     */
    positionArrayLiteralExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of BinaryExpr nodes.
     *
     * @param {object} node BinaryExpr object
     */
    positionBinaryExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ConnectorInitExpr nodes.
     *
     * @param {object} node ConnectorInitExpr object
     */
    positionConnectorInitExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of FieldBasedAccessExpr nodes.
     *
     * @param {object} node FieldBasedAccessExpr object
     */
    positionFieldBasedAccessExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of IndexBasedAccessExpr nodes.
     *
     * @param {object} node IndexBasedAccessExpr object
     */
    positionIndexBasedAccessExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Invocation nodes.
     *
     * @param {object} node Invocation object
     */
    positionInvocationNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Lambda nodes.
     *
     * @param {object} node Lambda object
     */
    positionLambdaNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Literal nodes.
     *
     * @param {object} node Literal object
     */
    positionLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of RecordLiteralExpr nodes.
     *
     * @param {object} node RecordLiteralExpr object
     */
    positionRecordLiteralExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of SimpleVariableRef nodes.
     *
     * @param {object} node SimpleVariableRef object
     */
    positionSimpleVariableRefNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of StringTemplateLiteral nodes.
     *
     * @param {object} node StringTemplateLiteral object
     */
    positionStringTemplateLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of TernaryExpr nodes.
     *
     * @param {object} node TernaryExpr object
     */
    positionTernaryExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of TypeCastExpr nodes.
     *
     * @param {object} node TypeCastExpr object
     */
    positionTypeCastExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of TypeConversionExpr nodes.
     *
     * @param {object} node TypeConversionExpr object
     */
    positionTypeConversionExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of UnaryExpr nodes.
     *
     * @param {object} node UnaryExpr object
     */
    positionUnaryExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlQname nodes.
     *
     * @param {object} node XmlQname object
     */
    positionXmlQnameNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlAttribute nodes.
     *
     * @param {object} node XmlAttribute object
     */
    positionXmlAttributeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlQuotedString nodes.
     *
     * @param {object} node XmlQuotedString object
     */
    positionXmlQuotedStringNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlElementLiteral nodes.
     *
     * @param {object} node XmlElementLiteral object
     */
    positionXmlElementLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlTextLiteral nodes.
     *
     * @param {object} node XmlTextLiteral object
     */
    positionXmlTextLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlCommentLiteral nodes.
     *
     * @param {object} node XmlCommentLiteral object
     */
    positionXmlCommentLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlPiLiteral nodes.
     *
     * @param {object} node XmlPiLiteral object
     */
    positionXmlPiLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Abort nodes.
     *
     * @param {object} node Abort object
     */
    positionAbortNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of Assignment nodes.
     *
     * @param {object} node Assignment object
     */
    positionAssignmentNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of Block nodes.
     *
     * @param {object} node Block object
     */
    positionBlockNode(node) {
        const viewState = node.viewState;
        const statements = node.getStatements();
        let height = 0;
        statements.forEach((element) => {
            const initialExpression = TreeUtil.isVariableDef(element) || TreeUtil.isAssignment(element) ?
                element.variable.initialExpression : undefined;
            if (initialExpression) {
                if (!TreeUtil.isConnectorInitExpr(initialExpression)) {
                    element.viewState.bBox.x = viewState.bBox.x + ((viewState.bBox.w - element.viewState.bBox.w) / 2);
                    element.viewState.bBox.y = viewState.bBox.y + height;
                    height += element.viewState.bBox.h;
                }
            } else {
                element.viewState.bBox.x = viewState.bBox.x + ((viewState.bBox.w - element.viewState.bBox.w) / 2);
                element.viewState.bBox.y = viewState.bBox.y + height;
                height += element.viewState.bBox.h;
            }
        });
    }


    /**
     * Calculate position of Break nodes.
     *
     * @param {object} node Break object
     */
    positionBreakNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of Continue nodes.
     *
     * @param {object} node Continue object
     */
    positionContinueNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of ExpressionStatement nodes.
     *
     * @param {object} node ExpressionStatement object
     */
    positionExpressionStatementNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ForkJoin nodes.
     *
     * @param {object} node ForkJoin object
     */
    positionForkJoinNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of If nodes.
     *
     * @param {object} node If object
     */
    positionIfNode(node) {
        const viewState = node.viewState;
        const bBox = viewState.bBox;
        const elseStatement = node.elseStatement;

        this.positionCompoundStatementComponents(node);

        node.body.viewState.bBox.x = node.viewState.components['statement-box'].x;
        node.body.viewState.bBox.y = node.viewState.components['statement-box'].y
            + node.viewState.components['block-header'].h;

        // Set the position of the else statement
        const elseX = bBox.x;
        let elseY = viewState.components['statement-box'].y
            + viewState.components['statement-box'].h;

        // Increase the node's components width. Mac width of all the else if nodes and the else node increased in this
        // pass
        const newWidth = node.viewState.bBox.w;
        node.body.viewState.bBox.w = newWidth;
        node.viewState.components['drop-zone'].w = newWidth;
        node.viewState.components['statement-box'].w = newWidth;
        node.viewState.components['block-header'].w = newWidth;

        if (node.viewState.bBox.w > elseStatement.viewState.bBox.w) {
            elseStatement.viewState.bBox.w = newWidth;
            if (TreeUtil.isBlock(elseStatement)) {
                elseStatement.viewState.components['drop-zone'].w = newWidth;
                elseStatement.viewState.components['statement-box'].w = newWidth;
                elseStatement.viewState.components['block-header'].w = newWidth;
            }
        }
        if (TreeUtil.isBlock(elseStatement)) {
            elseY += elseStatement.viewState.components['block-header'].h;
        }
        elseStatement.viewState.bBox.x = elseX;
        elseStatement.viewState.bBox.y = elseY;
        this.positionCompoundStatementComponents(elseStatement);
    }

    /**
     * Position the Else node
     * @param {object} node - else node
     */
    positionElseNode(node) {
        // Not Implemented
    }

    /**
     * Calculate position of Reply nodes.
     *
     * @param {object} node Reply object
     */
    positionReplyNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Return nodes.
     *
     * @param {object} node Return object
     */
    positionReturnNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of Comment nodes.
     *
     * @param {object} node Comment object
     */
    positionCommentNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Throw nodes.
     *
     * @param {object} node Throw object
     */
    positionThrowNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of Transaction nodes.
     *
     * @param {object} node Transaction object
     */
    positionTransactionNode(node) {
        const abortedBody = node.abortedBody;
        const committedBody = node.committedBody;
        const failedBody = node.failedBody;
        const transactionBody = node.transactionBody;
        const viewState = node.viewState;
        const bBox = viewState.bBox;
        const newWidth = node.viewState.bBox.w;

        this.positionCompoundStatementComponents(node);

        node.viewState.components['drop-zone'].w = newWidth;
        node.viewState.components['statement-box'].w = newWidth;
        node.viewState.components['block-header'].w = newWidth;

        let nextComponentY = node.viewState.components['drop-zone'].y
            + node.viewState.components['drop-zone'].h;

        // Set the position of the transaction body
        if (transactionBody) {
            transactionBody.viewState.bBox.x = bBox.x;
            transactionBody.viewState.bBox.y = nextComponentY + transactionBody.viewState.components['block-header'].h;
            this.positionCompoundStatementComponents(transactionBody);
            nextComponentY += transactionBody.viewState.components['statement-box'].h;
            this.increaseNodeComponentWidth(transactionBody, newWidth);
        }

        // Set the position of the failed body
        if (failedBody) {
            failedBody.viewState.bBox.x = bBox.x;
            failedBody.viewState.bBox.y = nextComponentY + failedBody.viewState.components['block-header'].h;
            this.positionCompoundStatementComponents(failedBody);
            nextComponentY += failedBody.viewState.components['statement-box'].h;
            this.increaseNodeComponentWidth(failedBody, newWidth);
        }

        // Set the position of the aborted body
        if (abortedBody) {
            abortedBody.viewState.bBox.x = bBox.x;
            abortedBody.viewState.bBox.y = nextComponentY + abortedBody.viewState.components['block-header'].h;
            this.positionCompoundStatementComponents(abortedBody);
            nextComponentY += abortedBody.viewState.components['statement-box'].h;
            this.increaseNodeComponentWidth(abortedBody, newWidth);
        }

        // Set the position of the aborted body
        if (committedBody) {
            committedBody.viewState.bBox.x = bBox.x;
            committedBody.viewState.bBox.y = nextComponentY + committedBody.viewState.components['block-header'].h;
            this.positionCompoundStatementComponents(committedBody);
            this.increaseNodeComponentWidth(committedBody, newWidth);
        }
    }

    increaseNodeComponentWidth(component, newWidth) {
        component.viewState.bBox.w = newWidth;
        component.viewState.components['drop-zone'].w = newWidth;
        component.viewState.components['statement-box'].w = newWidth;
        component.viewState.components['block-header'].w = newWidth;
    }

    /**
     * Calculate position of Transaction Failed nodes.
     *
     * @param {object} node Transaction Failed object
     */
    positionFailedNode(node) {
        // Not implemented.
    }

    /**
     * Calculate position of Transaction Aborted nodes.
     *
     * @param {object} node Transaction Aborted object
     */
    positionAbortedNode(node) {
        // Not implemented.
    }

    /**
     * Calculate position of Transaction Committed nodes.
     *
     * @param {object} node Transaction Committed object
     */
    positionCommittedNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Transform nodes.
     *
     * @param {object} node Transform object
     */
    positionTransformNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Try nodes.
     *
     * @param {object} node Try object
     */
    positionTryNode(node) {
        // Position the try node
        this.positionCompoundStatementComponents(node);

        const catchBlocks = node.catchBlocks;
        const finallyBody = node.finallyBody;
        // New width is set at the sizing util (Propagate the max width to the try node)
        const newWidth = node.viewState.bBox.w;
        const x = node.viewState.bBox.x;

        // set the new width of try node's components
        node.body.viewState.bBox.w = newWidth;
        node.viewState.components['drop-zone'].w = newWidth;
        node.viewState.components['statement-box'].w = newWidth;
        node.viewState.components['block-header'].w = newWidth;

        // Position the try node
        node.body.viewState.bBox.x = x;
        node.body.viewState.bBox.y = node.viewState.components['statement-box'].y
            + node.viewState.components['block-header'].h;

        for (let itr = 0; itr < catchBlocks.length; itr++) {
            const catchBlockBBox = (catchBlocks[itr]).viewState.bBox;
            let y;

            if (itr === 0) {
                // If the catch block is the first block, we position it with respect to the try node
                y = node.viewState.components['statement-box'].y + node.viewState.components['statement-box'].h;
            } else {
                // Position the catch block, with respect to the previous catch block
                y = (catchBlocks[itr - 1]).viewState.components['statement-box'].y
                    + (catchBlocks[itr - 1]).viewState.components['statement-box'].h;
            }
            // Position the catch block
            catchBlockBBox.x = x;
            catchBlockBBox.y = y;

            // increase the catch block's components' width
            this.increaseNodeComponentWidth(catchBlocks[itr], newWidth);

            // Position the compound statement components of catch block
            this.positionCompoundStatementComponents(catchBlocks[itr]);

            // Position the catch block's body and set new width
            catchBlocks[itr].body.viewState.bBox.w = newWidth;
            catchBlocks[itr].body.viewState.bBox.x = x;
            catchBlocks[itr].body.viewState.bBox.y = y + catchBlocks[itr].viewState.components['block-header'].h;
        }

        const finallyX = node.viewState.bBox.x;
        let finallyY;
        // If there are no catch blocks, position the finally block wrt the try node
        if (catchBlocks.length) {
            // Position based on the last catch block
            finallyY = (catchBlocks[catchBlocks.length - 1]).viewState.components['statement-box'].y
                + (catchBlocks[catchBlocks.length - 1]).viewState.components['statement-box'].h;
        } else {
            finallyY = node.viewState.components['statement-box'].y + node.viewState.components['statement-box'].h;
        }

        // Position the finally block
        finallyBody.viewState.bBox.x = finallyX;
        finallyBody.viewState.bBox.y = finallyY + finallyBody.viewState.components['block-header'].h;
        this.increaseNodeComponentWidth(finallyBody, newWidth);
        this.positionCompoundStatementComponents(finallyBody);
    }


    /**
     * Calculate position of VariableDef nodes.
     *
     * @param {object} node VariableDef object
     */
    positionVariableDefNode(node) {
        // Not implemented.
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of While nodes.
     *
     * @param {object} node While object
     */
    positionWhileNode(node) {
        this.positionCompoundStatementComponents(node);
        // Position the while node
        node.body.viewState.bBox.x = node.viewState.bBox.x;
        node.body.viewState.bBox.y = node.viewState.components['statement-box'].y
            + node.viewState.components['block-header'].h;
    }


    /**
     * Calculate position of WorkerReceive nodes.
     *
     * @param {object} node WorkerReceive object
     */
    positionWorkerReceiveNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of WorkerSend nodes.
     *
     * @param {object} node WorkerSend object
     */
    positionWorkerSendNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of ArrayType nodes.
     *
     * @param {object} node ArrayType object
     */
    positionArrayTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of BuiltInRefType nodes.
     *
     * @param {object} node BuiltInRefType object
     */
    positionBuiltInRefTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ConstrainedType nodes.
     *
     * @param {object} node ConstrainedType object
     */
    positionConstrainedTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of FunctionType nodes.
     *
     * @param {object} node FunctionType object
     */
    positionFunctionTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of UserDefinedType nodes.
     *
     * @param {object} node UserDefinedType object
     */
    positionUserDefinedTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ValueType nodes.
     *
     * @param {object} node ValueType object
     */
    positionValueTypeNode(node) {
        // Not implemented.
    }

    /**
     * Calculate the position of Compound nodes. (If, Else, ElseIf, etc..)
     * @param {object} node compound node
     */
    positionCompoundNode(node) {

    }


}

export default PositioningUtil;
