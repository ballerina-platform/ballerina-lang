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
import SimpleBBox from 'plugins/ballerina/model/view/simple-bounding-box';
import TreeUtil from './../../../model/tree-util';
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
        viewState.components.text.x = viewState.components['statement-box'].x + this.config.statement.gutter.h;
        viewState.components.text.y = viewState.components['statement-box'].y +
            (viewState.components['statement-box'].h / 2);

        if (TreeUtil.statementIsInvocation(node)) {
            // Set the view state property to manipulate at the action invocation decorator
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
            const allVisibleEndpoints = TreeUtil.getAllVisibleEndpoints(node.parent);
            const endpoint = _.find(allVisibleEndpoints, (endpoint) => {
                return endpoint.name.value === variableRefName;
            });

            // Move the x cordinates to centre align the action invocation statement
            viewState.components['statement-box'].x -= (this.config.actionInvocationStatement.width / 2);

            arrowStartBBox.x = viewState.bBox.x;
            arrowStartBBox.y = viewState.components['statement-box'].y
                                + this.config.actionInvocationStatement.text.height;

            dropDown.x = arrowStartBBox.x;
            dropDown.y = viewState.components['statement-box'].y
                + (viewState.components['statement-box'].h / 2);
            viewState.components.dropDown = dropDown;
            if (endpoint) {
                viewState.components.invocation = {
                    start: undefined,
                    end: undefined,
                };
                viewState.components.invocation.start = arrowStartBBox;
                arrowEndBBox.x = endpoint.viewState.bBox.x
                                + (endpoint.viewState.bBox.w / 2)
                                - (this.config.actionInvocationStatement.width / 2);
                arrowEndBBox.y = arrowStartBBox.y;
                viewState.components.invocation.end = arrowEndBBox;
                if (endpoint.viewState.showOverlayContainer) {
                    OverlayComponentsRenderingUtil.showConnectorPropertyWindow(endpoint);
                }
            }
        }
        if (TreeUtil.statementIsClientResponder(node)) {
            // Set the view state property to manipulate at the action invocation decorator
            viewState.isClientResponder = true;
            const arrowStartBBox = new SimpleBBox();
            const arrowEndBBox = new SimpleBBox();

            const parentConstructNode = TreeUtil.getClientInvokableParentNode(node);

            arrowStartBBox.x = viewState.components['statement-box'].x;
            arrowStartBBox.y = viewState.components['statement-box'].y
                                + this.config.actionInvocationStatement.text.height;

            if (!TreeUtil.isReturn(node)) {
                arrowStartBBox.x += 3;
            }

            if (parentConstructNode) {
                viewState.components.invocation = {
                    start: undefined,
                    end: undefined,
                };
                viewState.components.invocation.start = arrowStartBBox;
                arrowEndBBox.x = parentConstructNode.viewState.components.client.x
                                    + (this.config.clientLine.head.width / 2);
                arrowEndBBox.y = arrowStartBBox.y;
                viewState.components.invocation.end = arrowEndBBox;
            }
        }

        if (node.viewState.lambdas) {
            let y = node.viewState.bBox.y + node.viewState.components['statement-box'].h - 9;
            for (const lambda of node.viewState.lambdas) {
                lambda.viewState.bBox.x = node.viewState.bBox.x;
                lambda.viewState.bBox.y = y;
                y += lambda.viewState.bBox.h - 33;
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
        let width = 0;
        // Set the height of the toplevel nodes so that the other nodes would be positioned relative to it
        let height = this.config.canvas.padding.top;
        // filter out visible children from top level nodes.
        const children = node.filterTopLevelNodes((child) => {
            return TreeUtil.isFunction(child) || TreeUtil.isService(child);
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
        // Do nothing
    }

    /**
     * Calculate position of Enumerator nodes.
     *
     * @param {object} node Enumerator object
     */
    positionEnumeratorNode(node) {
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
        funcBodyViewState.bBox.y = viewState.bBox.y + cmp.heading.h +
            this.config.panel.body.padding.top + this.config.lifeLine.head.height;

        cmp.client.x = viewState.bBox.x + this.config.panel.body.padding.left;
        cmp.client.y = viewState.bBox.y + cmp.heading.h + this.config.panel.body.padding.top;

        // position the default worker.
        cmp.defaultWorker.x = cmp.client.x + cmp.client.w + this.config.lifeLine.gutter.h;
        cmp.defaultWorker.y = viewState.bBox.y + cmp.heading.h + this.config.panel.body.padding.top;
        // position default worker line.
        cmp.defaultWorkerLine.x = cmp.defaultWorker.x;
        cmp.defaultWorkerLine.y = cmp.defaultWorker.y;

        // position the children
        const body = node.getBody();
        body.viewState.bBox.x = cmp.client.x + cmp.client.w + this.config.panel.body.padding.left
            + (cmp.defaultWorkerLine.w / 2);
        body.viewState.bBox.y = cmp.defaultWorker.y + this.config.lifeLine.head.height
            + this.config.statement.height;

        // ========== End of Header ==========

        let xindex = (workers.length > 0) ? cmp.defaultWorker.x :
                        cmp.defaultWorker.x + cmp.defaultWorker.w + this.config.lifeLine.gutter.h;

        let maxLifeLineHeight = cmp.defaultWorker.h;
        // Set the client invocation arrow end x coordinates.
        cmp.client.arrowLine = cmp.defaultWorkerLine.x + (cmp.defaultWorkerLine.w / 2);
        // Position Workers
        if (workers instanceof Array && !_.isEmpty(workers)) {
            const maxHeightWorker = _.maxBy(workers, (worker) => {
                return worker.viewState.components.lifeLine.h;
            });
            maxLifeLineHeight = Math.max(maxLifeLineHeight, maxHeightWorker.viewState.components.lifeLine.h);
            cmp.defaultWorker.h = maxLifeLineHeight;
            workers.forEach((worker) => {
                worker.viewState.bBox.x = xindex;
                worker.viewState.bBox.y = cmp.defaultWorker.y;
                xindex += worker.viewState.bBox.w + this.config.lifeLine.gutter.h;
                worker.viewState.components.lifeLine.h = maxLifeLineHeight;
                // increase the arrow length of the client invokation line.
                cmp.client.arrowLine = worker.viewState.bBox.x + (this.config.lifeLine.width / 2);
            });
        }

        // Position Endpoints
        const endpoints = node.endpointNodes;
        endpoints.forEach((endpointNode) => {
            endpointNode.viewState.bBox.x = xindex;
            endpointNode.viewState.bBox.y = cmp.defaultWorker.y;
            xindex += endpointNode.viewState.bBox.w + this.config.lifeLine.gutter.h;
            if (endpointNode.viewState.showOverlayContainer) {
                OverlayComponentsRenderingUtil.showConnectorPropertyWindow(endpointNode);
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
        let endpoints = [];
        let variables = [];
        if (TreeUtil.isService(node)) {
            children = node.getResources();
            endpoints = node.filterVariables((statement) => {
                return TreeUtil.isEndpointTypeVariableDef(statement);
            });
            variables = node.filterVariables((statement) => {
                return !TreeUtil.isEndpointTypeVariableDef(statement);
            });
        } else if (TreeUtil.isConnector(node)) {
            children = node.getActions();
            endpoints = node.filterVariableDefs((statement) => {
                return TreeUtil.isEndpointTypeVariableDef(statement);
            });
            variables = node.filterVariableDefs((statement) => {
                return !TreeUtil.isEndpointTypeVariableDef(statement);
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

        if (endpoints instanceof Array) {
            endpoints.forEach((statement) => {
                statement.viewState.bBox.x = xIndex;
                statement.viewState.bBox.y = yIndex;
                xIndex += this.config.innerPanel.wrapper.gutter.h + statement.viewState.bBox.w;
                if (statement.viewState.showOverlayContainer) {
                    OverlayComponentsRenderingUtil.showConnectorPropertyWindow(statement);
                }
            });
        }

        // Position the variables which are not connector declarations
        // Globals
        node.viewState.components.globalsBbox = new SimpleBBox();
        node.viewState.components.globalsBbox.x = viewState.components.initFunction.x
            + 115 + 15;
        node.viewState.components.globalsBbox.y = viewState.components.initFunction.y;
        // Globals Expanded
        node.viewState.components.globalsExpandedBbox = new SimpleBBox();
        node.viewState.components.globalsExpandedBbox.x = viewState.components.initFunction.x;
        node.viewState.components.globalsExpandedBbox.y = viewState.components.initFunction.y;

        let lastGlobalElementY = node.viewState.components.globalsExpandedBbox.y + 35;
        variables.forEach((globalDec) => {
            globalDec.viewState.bBox.x = node.viewState.components.globalsExpandedBbox.x;
            globalDec.viewState.bBox.y = lastGlobalElementY;
            globalDec.viewState.bBox.h = 30;
            globalDec.viewState.bBox.w = 310;
            lastGlobalElementY += 30;
        });
        // Setting the overlay container if its true
        if (viewState.shouldShowConnectorPropertyWindow) {
            viewState.showOverlayContainer = true;
            OverlayComponentsRenderingUtil.showServerConnectorPropertyWindow(node);
        }
        if (TreeUtil.isConnector(node)) {
            let publicPrivateFlagoffset = 0;
            if (node.public) {
                publicPrivateFlagoffset = 40;
            }
            // Positioning argument parameters
            if (node.getParameters()) {
                viewState.components.argParameterHolder.openingParameter.x = viewState.bBox.x + viewState.titleWidth +
                    this.config.panel.heading.title.margin.right + this.config.panelHeading.iconSize.width
                    + this.config.panelHeading.iconSize.padding + publicPrivateFlagoffset;
                viewState.components.argParameterHolder.openingParameter.y = viewState.bBox.y +
                    viewState.components.annotation.h;

                // Positioning the connector parameters
                let nextXPositionOfParameter = viewState.components.argParameterHolder.openingParameter.x
                    + viewState.components.argParameterHolder.openingParameter.w;
                if (node.getParameters().length > 0) {
                    for (let i = 0; i < node.getParameters().length; i++) {
                        const argument = node.getParameters()[i];
                        nextXPositionOfParameter = this.createPositionForTitleNode(argument, nextXPositionOfParameter,
                            (viewState.bBox.y + viewState.components.annotation.h));
                    }
                }

                // Positioning the closing bracket component of the parameters.
                viewState.components.argParameterHolder.closingParameter.x = nextXPositionOfParameter + 130;
                viewState.components.argParameterHolder.closingParameter.y = viewState.bBox.y +
                    viewState.components.annotation.h;
            }
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
        const cmp = node.viewState.components;
        cmp.lifeLine.x = node.viewState.bBox.x;
        cmp.lifeLine.y = node.viewState.bBox.y;

        node.body.viewState.bBox.x = node.viewState.bBox.x + (cmp.lifeLine.w / 2);
        node.body.viewState.bBox.y = node.viewState.bBox.y + this.config.lifeLine.head.height;

        if (node.parent && !TreeUtil.isForkJoin(node.parent)) {
            node.body.viewState.bBox.y += this.config.statement.height;
        }
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
     * Calculate position of Transformer nodes.
     *
     * @param {object} node Transformer object
     */
    positionTransformerNode(node) {
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
     * Calculate position of TypeofExpression nodes.
     *
     * @param {object} node TypeofExpression object
     */
    positionTypeofExpressionNode(node) {
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
     * Calculate position of MatchExpression nodes.
     *
     * @param {object} node MatchExpression object
     */
    positionMatchExpressionNode(node) {
        // Not implemented.
    }


    /**
     * Calculate position of MatchExpressionPatternClause nodes.
     *
     * @param {object} node MatchExpressionPatternClause object
     */
    positionMatchExpressionPatternClauseNode(node) {
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
     * Calculate position of Bind nodes.
     *
     * @param {object} node Bind object
     */
    positionBindNode(node) {
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
            if (!TreeUtil.isEndpointTypeVariableDef(element)) {
                element.viewState.bBox.x = viewState.bBox.x;
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
     * Calculate position of Foreach nodes.
     *
     * @param {object} node Foreach object
     */
    positionForeachNode(node) {
        this.positionWhileNode(node);
    }


    /**
     * Calculate position of ForkJoin nodes.
     *
     * @param {object} node ForkJoin object
     */
    positionForkJoinNode(node) {
        const viewState = node.viewState;
        const bBox = viewState.bBox;
        bBox.x -= (this.config.lifeLine.width / 2);
        const joinStmt = node.getJoinBody();
        const timeoutStmt = node.getTimeoutBody();
        const joinBBox = (joinStmt) ? joinStmt.viewState.bBox : new SimpleBBox();
        const timeoutBBox = (timeoutStmt) ? timeoutStmt.viewState.bBox : new SimpleBBox();

        const cmp = node.viewState.components;
        cmp.forkContainer.x = bBox.x;
        cmp.forkContainer.y = bBox.y + this.config.statement.height;

        // Position Workers
        let xIndex = bBox.x;
        const yIndex = cmp.forkContainer.y + 40/* worker top padding */;
        if (node.workers instanceof Array && !_.isEmpty(node.workers)) {
            node.workers.forEach((worker) => {
                worker.viewState.bBox.x = xIndex;
                worker.viewState.bBox.y = yIndex;
                xIndex += worker.viewState.bBox.w;
            });
        }

        // Size join and timeout headers
        cmp.joinHeader.x = cmp.forkContainer.x + (this.config.lifeLine.width / 2);
        cmp.joinHeader.y = cmp.forkContainer.y + cmp.forkContainer.h;

        cmp.timeoutHeader.x = cmp.forkContainer.x + _.max([joinBBox.w, cmp.joinHeader.w])
                            + (cmp.timeoutHeader.w / 2)
                            + this.config.lifeLine.gutter.h;
        cmp.timeoutHeader.y = cmp.forkContainer.y + cmp.forkContainer.h;

        joinBBox.x = cmp.joinHeader.x;
        joinBBox.y = cmp.joinHeader.y + cmp.joinHeader.h;

        timeoutBBox.x = cmp.timeoutHeader.x;
        timeoutBBox.y = cmp.timeoutHeader.y + cmp.timeoutHeader.h;

        // Center the headers
        cmp.joinHeader.x -= (cmp.joinHeader.w / 2);
        cmp.timeoutHeader.x -= (cmp.timeoutHeader.w / 2);
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

        this.positionFlowControlCompoundStatementComponents(node);

        node.body.viewState.bBox.x = node.viewState.components['statement-box'].x;
        node.viewState.components['statement-box'].y += node.viewState.components['block-header'].h;
        node.body.viewState.bBox.y = node.viewState.components['statement-box'].y;

        // Set the position of the else statement
        const elseX = bBox.x + node.viewState.components['statement-box'].w;
        const elseY = viewState.components['statement-box'].y
                     + viewState.components['statement-box'].h;

        if (elseStatement) {
            elseStatement.viewState.bBox.x = elseX;
            elseStatement.viewState.bBox.y = elseY;
        }
    }

    /**
     * Position components of a flow control statement.
     * @node flow control parent node.
     */
    positionFlowControlCompoundStatementComponents(node) {
        const viewState = node.viewState;
        const x = viewState.bBox.x;
        let y;

        if (TreeUtil.isBlock(node)) {
            y = viewState.bBox.y + viewState.components['block-header'].h;
        } else {
            y = viewState.bBox.y;
        }
        viewState.components['drop-zone'].x = x;
        viewState.components['drop-zone'].y = y;
        viewState.components['statement-box'].x = viewState.bBox.x;
        viewState.components['statement-box'].y = y + viewState.components['drop-zone'].h;
    }


    /**
     * Calculate position of Match nodes.
     *
     * @param {object} node Match object
     */
    positionMatchNode(node) {
        let y = node.viewState.bBox.y + this.config.statement.height;
        const x = node.viewState.bBox.x;
        node.patternClauses.forEach((element) => {
            element.viewState.bBox.x = x;
            element.viewState.bBox.y = y;
            y += element.viewState.bBox.h;
        });
    }


    /**
     * Calculate position of MatchPatternClause nodes.
     *
     * @param {object} node MatchPatternClause object
     */
    positionMatchPatternClauseNode(node) {
        node.statement.viewState.bBox.x = node.viewState.bBox.x;
        node.statement.viewState.bBox.y = node.viewState.bBox.y + this.config.statement.height;
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
        const onRetryBody = node.onRetryBody;
        const transactionBody = node.transactionBody;
        const viewState = node.viewState;
        const bBox = viewState.bBox;

        this.positionCompoundStatementComponents(node);

        let nextComponentY = node.viewState.components['drop-zone'].y
            + node.viewState.components['drop-zone'].h;

        // Set the position of the transaction body
        if (transactionBody) {
            transactionBody.viewState.bBox.x = bBox.x;
            transactionBody.viewState.bBox.y = nextComponentY + transactionBody.viewState.components['block-header'].h;
            this.positionCompoundStatementComponents(transactionBody);
            nextComponentY += transactionBody.viewState.components['statement-box'].h;
        }

        // Set the position of the retry body
        if (onRetryBody) {
            onRetryBody.viewState.bBox.x = bBox.x + this.config.compoundStatement.gap.left +
                transactionBody.viewState.bBox.w;
            onRetryBody.viewState.bBox.y = transactionBody.viewState.bBox.y +
                transactionBody.viewState.components['statement-box'].h - this.config.compoundStatement.padding.top;
            this.positionCompoundStatementComponents(onRetryBody);
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
    positionNode(node) {
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
        this.positionCompoundStatementComponents(node);

        const catchBlocks = node.catchBlocks || [];
        const finallyBody = node.finallyBody;

        // position the try node
        node.body.viewState.bBox.x = node.viewState.components['statement-box'].x;
        node.viewState.components['statement-box'].y += node.viewState.components['block-header'].h;
        node.body.viewState.bBox.y = node.viewState.components['statement-box'].y;

        // position catch nodes
        const catchStartX = node.viewState.bBox.x + this.config.compoundStatement.gap.left;
        const catchStartY = node.viewState.bBox.y + this.config.compoundStatement.padding.top;

        let catchStmtWidth = node.viewState.components['statement-box'].w;
        let catchHeight = node.viewState.components['statement-box'].h;

        catchBlocks.forEach((catchStmt) => {
            catchStmt.viewState.bBox.x = catchStartX + catchStmtWidth;
            catchStmt.viewState.bBox.y = catchStartY;
            catchStmt.body.viewState.bBox.x = catchStmt.viewState.bBox.x;
            catchStmt.body.viewState.bBox.y = catchStartY
                + catchStmt.viewState.components['block-header'].h + catchHeight;
            catchStmt.viewState.components['statement-box'].y = catchStmt.body.viewState.bBox.y;
            catchStmt.viewState.components['statement-box'].x = catchStmt.body.viewState.bBox.x;
            catchStmtWidth += catchStmt.viewState.bBox.w;
            catchHeight += catchStmt.body.viewState.bBox.h;
        });

        // make all catch statements same height
        catchBlocks.forEach((catchStmt) => {
            catchStmt.viewState.bBox.h = catchHeight + catchStmt.viewState.components['block-header'].h;
        });

        // position finally block
        if (finallyBody) {
            const finallyX = node.viewState.bBox.x;
            let finallyY;
            if (catchBlocks.length > 0) {
                finallyY = catchBlocks[0].viewState.bBox.y + catchBlocks[0].viewState.bBox.h;
            } else {
                finallyY = node.viewState.components['statement-box'].y + node.viewState.components['statement-box'].h;
            }

            // Position the finally block
            const finallyViewState = node.viewState.components['finally-block'];
            finallyBody.viewState.bBox.x = finallyX;
            const finallyStatementBBoxY = finallyY + finallyViewState.components['block-header'].h;
            finallyBody.viewState.bBox.y = finallyStatementBBoxY;
            this.positionBlockNode(finallyBody);
            finallyViewState.components['statement-box'].y = finallyStatementBBoxY;
            finallyViewState.components['statement-box'].x = finallyX;

            // since finally is a block node, the positioning of the finally title has to be done within try block
            // rendering will also be done in try decorator
            node.viewState.components['finally-block'].x = finallyX;
            node.viewState.components['finally-block'].y = finallyY;
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
        this.positionFlowControlCompoundStatementComponents(node);
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
     * Calculate position of EndpointType nodes.
     *
     * @param {object} node EndpointType object
     */
    positionEndpointTypeNode(node) {
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
