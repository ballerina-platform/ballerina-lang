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
import OverlayComponentsRenderingUtil from './../default/components/utils/overlay-component-rendering-util';

class PositioningUtil {

    setConfig(config) {
        this.config = config;
    }

    positionStatementComponents(node) {
        const viewState = node.viewState;
        viewState.components['drop-zone'].x = viewState.bBox.x;
        viewState.components['drop-zone'].y = viewState.bBox.y;
        viewState.components['statement-box'].x = viewState.bBox.x;
        viewState.components['statement-box'].y = viewState.bBox.y + viewState.components['drop-zone'].h;
        viewState.components.text.x = viewState.components['statement-box'].x +
            (viewState.components['statement-box'].w / 2);
        viewState.components.text.y = viewState.components['statement-box'].y +
            (viewState.components['statement-box'].h / 2);

        //
        if (TreeUtil.statementIsInvocation(node)) {
            // Set the view state property to manipulate at the statement decorator
            viewState.isActionInvocation = true;
            const arrowStartBBox = new SimpleBBox();
            const arrowEndBBox = new SimpleBBox();
            const dropDown = new SimpleBBox();
            let variableRefName;
            if (TreeUtil.isVariableDef(node)) {
                variableRefName = node.variable.initialExpression.expression.variableName.value;
            } else if (TreeUtil.isAssignment(node) || TreeUtil.isExpressionStatement(node)) {
                variableRefName = node.expression.expression.variableName.value;
            }
            const allVisibleConnectorDeclarations = TreeUtil.getAllVisibleConnectorDeclarations(node.parent);
            const connectorDeclaration = _.find(allVisibleConnectorDeclarations, (varDef) => {
                return varDef.variable.name.value === variableRefName;
            });
            arrowStartBBox.x = viewState.bBox.x + viewState.bBox.w;
            arrowStartBBox.y = viewState.components['statement-box'].y + 10;

            dropDown.x = arrowStartBBox.x;
            dropDown.y = viewState.components['statement-box'].y
                + (viewState.components['statement-box'].h / 2);
            viewState.components.dropDown = dropDown;
            if (connectorDeclaration) {
                viewState.components.invocation = {
                    start: undefined,
                    end: undefined,
                };
                viewState.components.invocation.start = arrowStartBBox;
                arrowEndBBox.x = connectorDeclaration.viewState.bBox.x + (connectorDeclaration.viewState.bBox.w / 2);
                arrowEndBBox.y = arrowStartBBox.y;
                viewState.components.invocation.end = arrowEndBBox;
                if (connectorDeclaration.viewState.showOverlayContainer) {
                    OverlayComponentsRenderingUtil.showConnectorPropertyWindow(connectorDeclaration);
                }
            }
        } else if (TreeUtil.isConnectorDeclaration(node)) {
            if (node.viewState.showOverlayContainer) {
                OverlayComponentsRenderingUtil.showConnectorPropertyWindow(node);
            }
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
        // Here we skip the init action
        if (node.id !== node.parent.initAction.id) {
            // We use the same logic used for positioning the resources
            // TODO: need to isolate the common logic and plug them out
            this.positionResourceNode(node);
        }
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
        this.positionTopLevelNodes(node);
        let width = 0;
        // Set the height of the toplevel nodes so that the other nodes would be positioned relative to it
        let height = node.viewState.components.topLevelNodes.h + 50;
        // filter out visible children from top level nodes.
        const children = node.filterTopLevelNodes((child) => {
            return TreeUtil.isFunction(child) || TreeUtil.isService(child)
                || TreeUtil.isStruct(child) || TreeUtil.isConnector(child);
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
        // add the padding for the width
        width += this.config.panel.wrapper.gutter.h * 2;
        // add the bottom padding to the canvas height.
        height += this.config.panel.wrapper.gutter.v;
        // if  the view port is taller we take the height of the view port.
        height = (height > node.viewState.container.height) ? height : node.viewState.container.height;

        // re-adjust the width of each node to fill the container.
        if (width < node.viewState.container.width) {
            width = node.viewState.container.width;
        }
        // set all the children to same width.
        children.forEach((child) => {
            child.viewState.bBox.w = width - (this.config.panel.wrapper.gutter.h * 2);
        });

        node.viewState.bBox.h = height;
        node.viewState.bBox.w = width;
    }

    /**
     * Position the packageDec, imports and globals
     * @param node CompilationUnitNode
     */
    positionTopLevelNodes(node) {
        const viewState = node.viewState;
        viewState.components.topLevelNodes.x = this.config.panel.wrapper.gutter.v;
        viewState.components.topLevelNodes.y = this.config.panel.wrapper.gutter.h;
    }

    /**
     * Calculate position of Connector nodes.
     *
     * @param {object} node Connector object
     */
    positionConnectorNode(node) {
        this.positionServiceNode(node);
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
        const workers = node.workers;

        // position the function body.
        funcBodyViewState.bBox.x = viewState.bBox.x + this.config.panel.body.padding.left;
        funcBodyViewState.bBox.y = viewState.bBox.y + cmp.annotation.h + cmp.heading.h + this.config.panel.body.padding.top
            + this.config.lifeLine.head.height;
        // position the default worker.
        cmp.defaultWorker.x = viewState.bBox.x + this.config.panel.body.padding.left;
        cmp.defaultWorker.y = viewState.bBox.y + cmp.annotation.h + cmp.heading.h + this.config.panel.body.padding.top;
        // position default worker line.
        cmp.defaultWorkerLine.x = cmp.defaultWorker.x + ((cmp.defaultWorker.w - cmp.defaultWorkerLine.w) / 2);
        cmp.defaultWorkerLine.y = cmp.defaultWorker.y;

        // position the children
        const body = node.getBody();
        body.viewState.bBox.x = viewState.bBox.x + this.config.panel.body.padding.left;
        body.viewState.bBox.y = cmp.defaultWorker.y + this.config.lifeLine.head.height;

        // ========== Header Positioning ==========
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
        }

        // ========== End of Header ==========

        let xindex = (workers.length > 0) ? cmp.defaultWorker.x :
                        cmp.defaultWorker.x + cmp.defaultWorker.w + this.config.lifeLine.gutter.h;
        // Position Workers
        if (workers instanceof Array) {
            workers.forEach((worker) => {
                worker.viewState.bBox.x = xindex;
                worker.viewState.bBox.y = cmp.defaultWorker.y;
                xindex += worker.viewState.bBox.w + this.config.lifeLine.gutter.h;
            });
        }

        // Position Connectors
        const statements = node.body.statements;
        statements.forEach((statement) => {
            if (TreeUtil.isConnectorDeclaration(statement)) {
                statement.viewState.bBox.x = xindex;
                statement.viewState.bBox.y = cmp.defaultWorker.y;
                xindex += statement.viewState.bBox.w + this.config.lifeLine.gutter.h;
                if (statement.viewState.showOverlayContainer) {
                    OverlayComponentsRenderingUtil.showConnectorPropertyWindow(statement);
                }
            }
        });
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
        this.positionFunctionNode(node);
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

        // Position the transport nodes
        const transportLine = !_.isNil(viewState.components.transportLine) ?
            viewState.components.transportLine : { x: 0, y: 0 };
        transportLine.x = viewState.bBox.x - 5;
        transportLine.y = viewState.bBox.y + viewState.components.annotation.h + viewState.components.heading.h;

        let children = [];
        let connectors = [];
        if (TreeUtil.isService(node)) {
            children = node.getResources();
            connectors = node.filterVariables((statement) => {
                return TreeUtil.isConnectorDeclaration(statement);
            });
        } else if (TreeUtil.isConnector(node)) {
            children = node.getActions();
            connectors = node.filterVariableDefs((statement) => {
                return TreeUtil.isConnectorDeclaration(statement);
            });
        }

        let y = viewState.bBox.y + viewState.components.annotation.h + viewState.components.heading.h;
        // position the initFunction.
        viewState.components.initFunction.y = y + 25;
        viewState.components.initFunction.x = viewState.bBox.x + 100;
        // increase the y with init height;
        y += viewState.components.initFunction.h;
        // lets set the resources x, y.
        children.forEach((child) => {
            // set the x of the resource or action.
            child.viewState.bBox.x = viewState.bBox.x + this.config.innerPanel.wrapper.gutter.h;
            // add the gutter to y.
            y += this.config.innerPanel.wrapper.gutter.v;
            // lets set the y of the resource or action.
            child.viewState.bBox.y = y;
            // increase y with the resource body height.
            // check of the resource is collapsed.
            y += child.viewState.bBox.h;
            // expand the resource to match service.
            child.viewState.bBox.w = viewState.bBox.w - (this.config.innerPanel.wrapper.gutter.h * 2) -
            viewState.components.connectors.w;
        });

                    // Position Connectors
        const widthOffsetForConnectors = children.length > 0 ?
                (viewState.bBox.w - viewState.components.connectors.w) : this.config.innerPanel.wrapper.gutter.h;
        let xIndex = viewState.bBox.x + widthOffsetForConnectors;
        const yIndex = viewState.bBox.y + viewState.components.annotation.h + viewState.components.heading.h +
            viewState.components.initFunction.h + 40;

        if (connectors instanceof Array) {
            connectors.forEach((statement) => {
                statement.viewState.bBox.x = xIndex;
                statement.viewState.bBox.y = yIndex;
                xIndex += this.config.innerPanel.wrapper.gutter.h + statement.viewState.bBox.w;
                if (statement.viewState.showOverlayContainer) {
                    OverlayComponentsRenderingUtil.showConnectorPropertyWindow(statement);
                }
            });
        }
        // Setting the overlay container if its true
        if (viewState.shouldShowConnectorPropertyWindow) {
            viewState.showOverlayContainer = true;
            OverlayComponentsRenderingUtil.showServerConnectorPropertyWindow(node);
        }
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
        node.body.viewState.bBox.x = node.viewState.bBox.x;
        node.body.viewState.bBox.y = node.viewState.bBox.y + this.config.lifeLine.head.height;

        const cmp = node.viewState.components;
        cmp.lifeLine.x = node.viewState.bBox.x + ((node.viewState.bBox.w - cmp.lifeLine.w) / 2);
        cmp.lifeLine.y = node.viewState.bBox.y;
    }


    /**
     * Calculate position of Xmlns nodes.
     *
     * @param {object} node Xmlns object
     */
    positionXmlnsNode(node) {
        this.positionStatementComponents(node);
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
            if (!TreeUtil.isConnectorDeclaration(element)) {
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
     * Calculate position of Next nodes.
     *
     * @param {object} node Next object
     */
    positionNextNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of ExpressionStatement nodes.
     *
     * @param {object} node ExpressionStatement object
     */
    positionExpressionStatementNode(node) {
        this.positionStatementComponents(node);
    }


    /**
     * Calculate position of ForkJoin nodes.
     *
     * @param {object} node ForkJoin object
     */
    positionForkJoinNode(node) {
        const viewState = node.viewState;
        const bBox = viewState.bBox;
        const joinStmt = node.getJoinBody();
        const timeoutStmt = node.getTimeoutBody();

        this.positionCompoundStatementComponents(node);

        // Set the node x and y using statement box.
        node.viewState.bBox.x = node.viewState.components['statement-box'].x;
        node.viewState.bBox.y = node.viewState.components['statement-box'].y
            + node.viewState.components['block-header'].h;

        node.viewState.components['drop-zone'].w = node.viewState.bBox.w;
        node.viewState.components['statement-box'].w = node.viewState.bBox.w;
        node.viewState.components['block-header'].w = node.viewState.bBox.w;
        node.viewState.components['statement-body'].w = node.viewState.bBox.w;

        if (joinStmt) {
            // Calculate join block x and y.
            const joinX = bBox.x;
            let joinY = viewState.components['statement-box'].y
                + viewState.components['statement-box'].h;

            // Create a bbox for parameter of join.
            joinStmt.viewState.components.param =
                new SimpleBBox(joinX + joinStmt.viewState.components.expression.w +
                    joinStmt.viewState.components.titleWidth.w, 0, 0, 0, 0, 0);

            if (node.viewState.bBox.w > joinStmt.viewState.bBox.w) {
                joinStmt.viewState.bBox.w = node.viewState.bBox.w;
                if (TreeUtil.isBlock(joinStmt)) {
                    joinStmt.viewState.components['drop-zone'].w = node.viewState.bBox.w;
                    joinStmt.viewState.components['statement-box'].w = node.viewState.bBox.w;
                    joinStmt.viewState.components['block-header'].w = node.viewState.bBox.w;
                }
            }

            if (joinStmt && TreeUtil.isBlock(joinStmt)) {
                joinY += joinStmt.viewState.components['block-header'].h;
            }

            joinStmt.viewState.components['statement-box'].h += joinStmt.viewState.components['block-header'].h;

            joinStmt.viewState.bBox.y = joinY;
            joinStmt.viewState.bBox.x = joinX;
            this.positionCompoundStatementComponents(joinStmt);
        }

        if (timeoutStmt) {
            // Calculate timeout block x and y.
            const timeoutX = bBox.x;
            const timeoutY = joinStmt ? (joinStmt.viewState.bBox.y
                + joinStmt.viewState.components['statement-box'].h) : (node.viewState.components['statement-box'].y
                + node.viewState.components['statement-box'].h);

            // Create a bbox for parameter of timeout.
            timeoutStmt.viewState.components.param =
                new SimpleBBox(timeoutX + timeoutStmt.viewState.components.expression.w, 0, 0, 0, 0);

            if (node.viewState.bBox.w > timeoutStmt.viewState.bBox.w) {
                timeoutStmt.viewState.bBox.w = node.viewState.bBox.w;
                if (TreeUtil.isBlock(timeoutStmt)) {
                    timeoutStmt.viewState.components['drop-zone'].w = node.viewState.bBox.w;
                    timeoutStmt.viewState.components['statement-box'].w = node.viewState.bBox.w;
                    timeoutStmt.viewState.components['block-header'].w = node.viewState.bBox.w;
                }
            }

            // Add the block header value to the statement box of the timeout statement.
            timeoutStmt.viewState.components['statement-box'].h += timeoutStmt.viewState.components['block-header'].h;

            timeoutStmt.viewState.bBox.y = timeoutY;
            timeoutStmt.viewState.bBox.x = timeoutX;
            this.positionCompoundStatementComponents(timeoutStmt);
        }

        // Position Workers
        let xIndex = bBox.x + this.config.fork.padding.left + this.config.fork.lifeLineGutterH;
        const yIndex = bBox.y + this.config.fork.padding.top;
        if (node.workers instanceof Array) {
            node.workers.forEach((worker) => {
                worker.viewState.bBox.x = xIndex;
                worker.viewState.bBox.y = yIndex;
                xIndex += this.config.fork.lifeLineGutterH + worker.viewState.bBox.w;
            });
        }

        // Add the values of the statement body of the fork node.
        node.viewState.components['statement-body'].h = node.viewState.components['statement-box'].h
            - node.viewState.components['block-header'].h;
        node.viewState.components['statement-body'].y = node.viewState.components['statement-box'].y
            + node.viewState.components['block-header'].h;
        node.viewState.components['statement-body'].x = node.viewState.components['statement-box'].x;

        node.viewState.bBox.h = node.viewState.components['statement-body'].h
            + node.viewState.components['block-header'].h
            + (joinStmt ? joinStmt.viewState.components['statement-box'].h : 0)
            + (timeoutStmt ? timeoutStmt.viewState.components['statement-box'].h : 0);
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

        if (elseStatement && node.viewState.bBox.w > elseStatement.viewState.bBox.w) {
            elseStatement.viewState.bBox.w = newWidth;
            if (TreeUtil.isBlock(elseStatement)) {
                elseStatement.viewState.components['drop-zone'].w = newWidth;
                elseStatement.viewState.components['statement-box'].w = newWidth;
                elseStatement.viewState.components['block-header'].w = newWidth;
            }
        }
        if (elseStatement && TreeUtil.isBlock(elseStatement)) {
            elseY += elseStatement.viewState.components['block-header'].h;
        }
        if (elseStatement) {
            elseStatement.viewState.bBox.x = elseX;
            elseStatement.viewState.bBox.y = elseY;
            this.positionCompoundStatementComponents(elseStatement);
        }
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

        if (finallyBody) {
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
