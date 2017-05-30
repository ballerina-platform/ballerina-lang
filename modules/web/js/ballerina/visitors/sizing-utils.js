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

import {statement} from './../configs/designer-defaults';
import {blockStatement} from './../configs/designer-defaults';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import SimpleBBox from './../ast/simple-bounding-box';
import * as DesignerDefaults from './../configs/designer-defaults';
import ASTFactory from './../ast/ballerina-ast-factory';
import _ from 'lodash';

class SizingUtil {
    constructor() {
        let svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svg.setAttribute('style', 'border: 1px solid black');
        svg.setAttribute('width', '600');
        svg.setAttribute('height', '250');
        svg.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");
        this.textElement = document.createElementNS("http://www.w3.org/2000/svg", 'text');
        svg.appendChild(this.textElement);
        document.body.appendChild(svg);
    }

    /**
     * Get width of a given text and processed text
     * considering provided min and max widths.
     * @param {string} text
     * @param {number} minWidth
     * @param {number} maxWidth
     * @return {object} {width,text}
     * */
    getTextWidth(text, minWidth = statement.width, maxWidth = statement.maxWidth) {
        this.textElement.innerHTML = _.escape(text);
        let width = statement.padding.left + this.textElement.getComputedTextLength() + statement.padding.right;
        // if the width is more then max width crop the text
        if (width <= minWidth) {
            //set the width to minimam width
            width = minWidth;
        } else if (width > minWidth && width <= maxWidth) {
            // do nothing
        } else {
            // We need to truncate displayText and show an ellipses at the end.
            let ellipses = '...';
            let possibleCharactersCount = 0;
            for (let i = (text.length - 1); 1 < i; i--) {
                if ((statement.padding.left + this.textElement.getSubStringLength(0, i) + statement.padding.right) < maxWidth) {
                    possibleCharactersCount = i;
                    break;
                }
            }
            // We need room for the ellipses as well, hence removing 'ellipses.length' no. of characters.
            text = text.substring(0, (possibleCharactersCount - ellipses.length)) + ellipses; // Appending ellipses.

            width = maxWidth;
        }
        return {
            w: width,
            text: text
        };
    }

    getOnlyTextWidth(text, options={}) {
        const {fontSize} = options;
        this.textElement.innerHTML = _.escape(text);
        const currentFZ = this.textElement.style.fontSize;
        this.textElement.style.fontSize = fontSize;
        const tl = this.textElement.getComputedTextLength();
        this.textElement.style.fontSize = currentFZ;
        return tl;
    }

    populateSimpleStatementBBox(expression, viewState) {
        let textViewState = util.getTextWidth(expression);
        let dropZoneHeight = statement.gutter.v;
        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['drop-zone'].h = dropZoneHeight;

        viewState.bBox.w = textViewState.w;
        viewState.bBox.h = statement.height + viewState.components['drop-zone'].h;

        viewState.expression = textViewState.text;
        viewState.fullExpression = expression;
        return viewState;
    }

    getHighestStatementContainer(workers) {
        const sortedWorkers = _.sortBy(workers, function (worker) {
            return worker.viewState.components.statementContainer.h;
        });
        return sortedWorkers.length > 0 ? sortedWorkers[sortedWorkers.length - 1].getViewState().components.statementContainer.h : -1;
    }

    populateCompoundStatementChild(node, expression = undefined ) {
        let viewState = node.getViewState();
        let components = {};
        components['statementContainer'] = new SimpleBBox();
        let statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        let statementContainerWidth = 0;
        let statementContainerHeight = 0;

        _.forEach(statementChildren, function (child) {
            statementContainerHeight += child.viewState.bBox.h;
            if (child.viewState.bBox.w > statementContainerWidth) {
                statementContainerWidth = child.viewState.bBox.w;
            }
        });

        /**
         * Add the left padding and right padding for the statement container and
         * add the additional gutter height to the statement container height, in order to keep the gap between the
         * last statement and the block statement bottom margin
         */
        statementContainerHeight += (statementContainerHeight > 0 ? statement.gutter.v :
            blockStatement.body.height - blockStatement.heading.height);

        statementContainerWidth += (statementContainerWidth > 0 ?
            (blockStatement.body.padding.left + blockStatement.body.padding.right) : blockStatement.width);

        // for compound statement like if , while we need to render condition expression
        // we will calculate the width of the expression and adjest the block statement
        if(expression != undefined){
            // see how much space we have to draw the condition
            let available = statementContainerWidth - blockStatement.heading.width - 10;
            components['expression'] = this.getTextWidth(expression,0,available);
        }

        components['statementContainer'].h = statementContainerHeight;
        components['statementContainer'].w = statementContainerWidth;

        viewState.bBox.h = statementContainerHeight + blockStatement.heading.height;
        viewState.bBox.w = statementContainerWidth;

        viewState.components = components;
    }

    populatePanelDecoratorBBox(node, name) {
        let viewState = node.getViewState();
        let components = {};

        components['heading'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;

        /**
         * calculate the height of annotation view
         */
        let annotationHeight = this.getAnnotationHeight(node);

        components['annotation'] = new SimpleBBox();

        if (node.viewState.annotationViewCollapsed) {
            components['annotation'].h = 0;
        } else {
            components['annotation'].h = annotationHeight;
        }

        components['statementContainer'] = new SimpleBBox();
        let statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        const statementContainerWidthPadding = DesignerDefaults.statementContainer.padding.left +
            DesignerDefaults.statementContainer.padding.right;
        let statementWidth = DesignerDefaults.statementContainer.width + statementContainerWidthPadding;
        let statementHeight = 0;

        _.forEach(statementChildren, function (child) {
            statementHeight += child.viewState.bBox.h;
            if ((child.viewState.bBox.w + statementContainerWidthPadding) > statementWidth) {
                statementWidth = child.viewState.bBox.w + statementContainerWidthPadding;
            }
        });

        /**
         * We add an extra gap to the statement container height, in order to maintain the gap between the
         * last statement's bottom margin and the default worker bottom rect's top margin
         */
        statementHeight += DesignerDefaults.statement.gutter.v;

        if (statementHeight < DesignerDefaults.statementContainer.height) {
            statementHeight = DesignerDefaults.statementContainer.height;
        }

        components['statementContainer'].h = statementHeight;
        components['statementContainer'].w = statementWidth;

        components['body'] = new SimpleBBox();

        let workerChildren = node.filterChildren(function (child) {
            return BallerinaASTFactory.isWorkerDeclaration(child);
        });

        let connectorChildren = node.filterChildren(function (child) {
            return BallerinaASTFactory.isConnectorDeclaration(child);
        });

        const highestStatementContainerHeight = util.getHighestStatementContainer(workerChildren);

        /**
         * If the current default worker's statement container height is less than the highest worker's statement container
         * we set the default statement container height to the highest statement container's height
         */
        components['statementContainer'].h = _.max([components['statementContainer'].h, highestStatementContainerHeight]);

        const defaultWorkerLifeLineHeight = components['statementContainer'].h + DesignerDefaults.lifeLine.head.height * 2;

        let lifeLineWidth = 0;
        _.forEach(workerChildren.concat(connectorChildren), function (child) {
            lifeLineWidth += child.viewState.bBox.w + DesignerDefaults.lifeLine.gutter.h;
            child.getViewState().bBox.h = _.max([components['statementContainer'].h, highestStatementContainerHeight]) +
                DesignerDefaults.lifeLine.head.height * 2;
            child.getViewState().components.statementContainer.h = _.max([components['statementContainer'].h,
                highestStatementContainerHeight]);
        });

        if (node.viewState.collapsed) {
            components['body'].h = 0;
        } else {
            components['body'].h = ((DesignerDefaults.panel.body.height < defaultWorkerLifeLineHeight) ? defaultWorkerLifeLineHeight : DesignerDefaults.panel.body.height)
                + DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        }

        components['body'].w = components['statementContainer'].w + DesignerDefaults.panel.body.padding.right +
            DesignerDefaults.panel.body.padding.left + lifeLineWidth;
        components['heading'].w = components['body'].w;
        components['annotation'].w = components['body'].w;

        viewState.bBox.h = components['heading'].h + components['body'].h + components['annotation'].h;
        viewState.bBox.w = components['body'].w;

        const textWidth = util.getTextWidth(name);
        viewState.titleWidth = textWidth.w;
        viewState.trimmedTitle = textWidth.text;

        components['parametersPrefixContainer'] = {};
        components['parametersPrefixContainer'].w = util.getTextWidth('Parameters: ').w;

        viewState.components = components;
    }

    populateOuterPanelDecoratorBBox(node) {
        let viewState = node.getViewState();
        let components = {};
        let totalResourceHeight = 0;
        let connectorStatementContainerHeight = 0;
        let resources = node.filterChildren(function (child) {
            return ASTFactory.isResourceDefinition(child) ||
                ASTFactory.isConnectorAction(child);
        });
        let connectors = node.filterChildren(function (child) {
            return ASTFactory.isConnectorDeclaration(child);
        });
        let maxResourceWidth = 0;
        //Initial statement height include panel heading and panel padding.
        let bodyHeight = DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        // Set the width initial value to the padding left and right
        let bodyWidth = DesignerDefaults.panel.body.padding.left + DesignerDefaults.panel.body.padding.right;

        const variableDefinitionsHeight = this.getConnectorLevelVariablesHeight(node);

        /**
         * If there are service level connectors, their height depends on the heights of the resources
         */
        _.forEach(resources, function (resource) {
            totalResourceHeight += resource.getViewState().bBox.h;
            if (maxResourceWidth < resource.getViewState().bBox.w) {
                maxResourceWidth = resource.getViewState().bBox.w;
            }
        });

        /**
         * Add the total variable definitions height to the total height
         */
        totalResourceHeight += variableDefinitionsHeight + DesignerDefaults.panel.body.padding.top;

        /**
         * Set the max resource width to the resources
         */
        _.forEach(resources, function (resource) {
            resource.getViewState().bBox.w = maxResourceWidth;
            resource.getViewState().components.body.w = maxResourceWidth;
            resource.getViewState().components.heading.w = maxResourceWidth;
        });

        // Add the max resource width to the body width
        bodyWidth += maxResourceWidth;

        /**
         * Set the connector statement container height and the connectors' height accordingly only if there are service
         * level connectors
         */
        if (connectors.length > 0) {
            if (totalResourceHeight <= 0) {
                // There are no resources in the service
                connectorStatementContainerHeight = DesignerDefaults.statementContainer.height;
            } else {
                // Here we add additional gutter height to balance the gaps from top and bottom
                connectorStatementContainerHeight = totalResourceHeight +
                    DesignerDefaults.panel.wrapper.gutter.v * (resources.length + 1);
            }
            /**
             * Adjust the height of the connectors and adjust the service's body width with the connector widths
             */
            _.forEach(connectors, function (connector) {
                connector.getViewState().bBox.h = connectorStatementContainerHeight +
                    DesignerDefaults.lifeLine.head.height * 2;
                connector.getViewState().components.statementContainer.h = connectorStatementContainerHeight;
                bodyWidth += (connector.getViewState().components.statementContainer.w + DesignerDefaults.lifeLine.gutter.h);
            });

            bodyHeight = connectorStatementContainerHeight + DesignerDefaults.lifeLine.head.height * 2 +
                DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        } else if (totalResourceHeight > 0) {
            bodyHeight = totalResourceHeight + DesignerDefaults.panel.body.padding.top +
                DesignerDefaults.panel.body.padding.bottom + DesignerDefaults.panel.wrapper.gutter.v * (resources.length - 1);
        } else if(ASTFactory.isStructDefinition(node)){
            bodyHeight = DesignerDefaults.structDefinition.body.height;
        } else {
            // There are no connectors as well as resources, since we set the default height
            bodyHeight = DesignerDefaults.innerPanel.body.height;
        }


        /**
         * calculate the height of annotation view
         */
        let annotationHeight = this.getAnnotationHeight(node);

        components['heading'] = new SimpleBBox();
        components['body'] = new SimpleBBox();
        components['annotation'] = new SimpleBBox();
        components['variablesPane'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;
        if (node.viewState.collapsed) {
            components['body'].h = 0;
        } else {
            components['body'].h = bodyHeight;
        }
        if (node.viewState.annotationViewCollapsed) {
            components['annotation'].h = 0;
        } else {
            components['annotation'].h = annotationHeight;
        }
        components['variablesPane'].h = variableDefinitionsHeight;

        components['body'].w = bodyWidth;
        components['heading'].w = bodyWidth;
        components['annotation'].w = bodyWidth;

        viewState.bBox.h = components['heading'].h + components['body'].h + components['annotation'].h;
        viewState.bBox.w = components['body'].w;

        viewState.components = components;
    }

    getStatementHeightBefore(statement) {
        let parent = statement.getParent();
        let statements = parent.filterChildren(BallerinaASTFactory.isStatement);
        let currentStatementIndex = _.indexOf(statements, statement);
        let statementsBefore = _.slice(statements, 0, currentStatementIndex);

        let height = 0;
        _.forEach(statementsBefore, function (stmt) {
            height += stmt.getViewState().bBox.h;
        });

        return height;
    }

    getDefaultStatementHeight() {
        return statement.height + statement.gutter.v;
    }

    /**
     *
     * @param {ASTNode} parent - parent node
     * @param {ASTNode} childNode - child node, upto which we need to calculate the total height
     * @returns {number}
     */
    getTotalHeightUpto(parent, childNode) {
        const self = this;

        const statementChildren = _.filter(parent.getChildren(), function(child) {
            return BallerinaASTFactory.isStatement(child);
        });
        const nodeIndex = _.findIndex(statementChildren, function(child){
            return child.id === childNode.id;
        });

        const slicedChildren = _.slice(statementChildren, 0, nodeIndex);
        let totalHeight = 0;

        _.forEach(slicedChildren, function (child) {
            const dimensionSynced = child.getViewState().dimensionsSynced;
            if (BallerinaASTFactory.isWorkerInvocationStatement(child) && !dimensionSynced) {
                if (!child.getViewState().dimensionsSynced) {
                    self.syncWorkerInvocationDimension(child);
                }
            } else if (BallerinaASTFactory.isWorkerReplyStatement(child) && !dimensionSynced) {
                if (!child.getViewState().dimensionsSynced) {
                    self.syncWorkerReplyDimension(child);
                }
            }

            totalHeight += child.getViewState().bBox.h;
        });

        totalHeight += childNode.getViewState().bBox.h;

        return totalHeight;
    }

    /**
     * When given a worker invocation statement, this would sync the dimensions, to make the invocation statement
     * and the reply statement linear
     * @param {ASTNode} node - worker invocation node
     */
    syncWorkerInvocationDimension(node) {
        let destinationWorkerName = node.getWorkerName();
        let topLevelParent = node.getTopLevelParent();
        const workersParent = BallerinaASTFactory.isWorkerDeclaration(topLevelParent) ?
            topLevelParent.getParent() : topLevelParent;

        let workerDeclaration;
        if (destinationWorkerName === 'default') {
            workerDeclaration = workersParent;
        } else {
            workerDeclaration = _.find(workersParent.getChildren(), function (child) {
                if (BallerinaASTFactory.isWorkerDeclaration(child)) {
                    return child.getWorkerName() === destinationWorkerName;
                }

                return false;
            });
        }

        if (!_.isNil(workerDeclaration)) {
            /**
             * Check whether there is a worker reply at destination, receiving the invocation
             */
            const workerName = BallerinaASTFactory.isWorkerDeclaration(topLevelParent) ?
                topLevelParent.getWorkerName() : 'default';
            const workerReplyStatement = this.getWorkerReplyStatementTo(workerDeclaration, workerName);

            if (!_.isNil(workerReplyStatement)) {
                const upToReplyHeight = util.getTotalHeightUpto(workerDeclaration, workerReplyStatement);
                const upToInvocationTotalHeight = util.getTotalHeightUpto(topLevelParent, node);

                if (upToReplyHeight > upToInvocationTotalHeight) {
                    node.getViewState().components['drop-zone'].h += upToReplyHeight - upToInvocationTotalHeight;
                    node.getViewState().bBox.h += upToReplyHeight - upToInvocationTotalHeight;
                } else {
                    workerReplyStatement.getViewState().components['drop-zone'].h += upToInvocationTotalHeight - upToReplyHeight;
                    workerReplyStatement.getViewState().bBox.h += upToInvocationTotalHeight - upToReplyHeight;
                }
                workerReplyStatement.getViewState().dimensionsSynced = true;
            }

            node.getViewState().dimensionsSynced = true;
        }
    }

    /**
     * When given a worker reply statement, this would sync the dimensions in order to make the invocation statement
     * and the reply statement linear
     * @param {ASTNode} node - worker reply statement
     */
    syncWorkerReplyDimension(node) {
        let destinationWorkerName = node.getWorkerName();
        let topLevelParent = node.getTopLevelParent();
        const workersParent = BallerinaASTFactory.isWorkerDeclaration(topLevelParent) ?
            topLevelParent.getParent() : topLevelParent;
        let workerDeclaration;
        if (destinationWorkerName === 'default') {
            workerDeclaration = workersParent;
        } else {
            workerDeclaration = _.find(workersParent.getChildren(), function (child) {
                if (BallerinaASTFactory.isWorkerDeclaration(child)) {
                    return child.getWorkerName() === destinationWorkerName;
                }

                return false;
            });
        }
        if (!_.isNil(workerDeclaration)) {
            /**
             * Check whether there is a worker reply at destination, receiving the invocation
             */
            const workerName = BallerinaASTFactory.isWorkerDeclaration(topLevelParent) ?
                topLevelParent.getWorkerName() : 'default';
            const workerInvocationStatement = this.getWorkerInvocationStatementFrom(workerDeclaration, workerName);

            if (!_.isNil(workerInvocationStatement)) {
                const upToInvocationHeight = util.getTotalHeightUpto(workerDeclaration, workerInvocationStatement);
                const upToReplyTotalHeight = util.getTotalHeightUpto(topLevelParent, node);

                if (upToInvocationHeight > upToReplyTotalHeight) {
                    node.getViewState().components['drop-zone'].h += upToInvocationHeight - upToReplyTotalHeight;
                    node.getViewState().bBox.h += upToInvocationHeight - upToReplyTotalHeight;
                } else {
                    workerInvocationStatement.getViewState().components['drop-zone'].h += upToReplyTotalHeight - upToInvocationHeight;
                    workerInvocationStatement.getViewState().bBox.h += upToReplyTotalHeight - upToInvocationHeight;
                }
                workerInvocationStatement.getViewState().dimensionsSynced = true;
            }
            node.getViewState().dimensionsSynced = true;
        }
    }

    /**
     * Get the worker reply statement, which send the reply to the given top level
     * node (ie resource, action, function, worker
     * @param {String} workerName
     * @param {ASTNode} parentNode - node which encapsulate the children need to be iterated (worker, function, etc)
     * @returns {undefined|ASTNode}
     */
    getWorkerReplyStatementTo(parentNode, workerName) {
        let childNodes;
        if (!_.isNil(parentNode)) {
            childNodes = _.filter(parentNode.getChildren(), function (child) {
                if (BallerinaASTFactory.isWorkerReplyStatement(child)) {
                    return child.getWorkerName() === workerName;
                } else {
                    return false;
                }
            });
        }

        if (_.isNil(childNodes)) {
            return undefined;
        } else {
            return childNodes[0];
        }
    }

    /**
     * Get the worker reply statement, which send the request from the given top level
     * node (ie resource, action, function, worker
     * @param {String} workerName
     * @param {ASTNode} parentNode - node which encapsulate the children need to be iterated (worker, function, etc)
     * @returns {undefined|ASTNode}
     */
    getWorkerInvocationStatementFrom(parentNode, workerName) {
        const childNodes = _.filter(parentNode.getChildren(), function (child) {
            if (BallerinaASTFactory.isWorkerInvocationStatement(child)) {
                return child.getWorkerName() === workerName;
            } else {
                return false;
            }
        });

        if (_.isNil(childNodes)) {
            return undefined;
        } else {
            return childNodes[0];
        }
    }

    getAnnotationHeight(node){
        let height = 0;
        let annotations = node.filterChildren((child) => {
            return ASTFactory.isAnnotation(child);
        });

        _.forEach(annotations, (annotation) => {
            if(annotation.children.length == 0 ){
                height = height + 20;
            }else{
                height = height + ( annotation.children.length * 20 );
            }
        });

        //add padding
        if(annotations.length > 0){
            height = height + 7 * 2;
        }
        // add a gap for add new annotation.
        //height = height + 25;

        return height;
    }

    getConnectorLevelVariablesHeight(node) {
        let height = 65;

        if(!node.viewState.variablesExpanded){
            return 35;
        }

        const variables = node.filterChildren(function (child) {
            return ASTFactory.isVariableDefinitionStatement(child);
        });

        if (!_.isEmpty(variables)) {
            height = height + ( variables.length * 30 );
        }

        return height;
    }
}


export let util = new SizingUtil();
