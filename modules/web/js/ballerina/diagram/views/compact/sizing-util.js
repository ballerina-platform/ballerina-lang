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
import SimpleBBox from '../../../ast/simple-bounding-box';
import ASTFactory from '../../../ast/ast-factory';

class SizingUtil {
    /**
     * Constructor for SizingUtil
     * @param {object} options - util options
     */
    constructor(options) {
        const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('style', 'border: 1px solid black');
        svg.setAttribute('width', '600');
        svg.setAttribute('height', '250');
        svg.setAttributeNS('http://www.w3.org/2000/xmlns/', 'xmlns:xlink', 'http://www.w3.org/1999/xlink');
        this.textElement = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        svg.appendChild(this.textElement);
        document.body.appendChild(svg);
        this.designer = options.designer;
    }

    /**
     * Get width of a given text and processed text
     * considering provided min and max widths.
     * @param {string} text
     * @param {number} minWidth
     * @param {number} maxWidth
     * @return {object} {width,text}
     * */
    getTextWidth(text, minWidth = this.designer.statement.width, maxWidth = this.designer.statement.maxWidth) {
        this.textElement.innerHTML = _.escape(text);
        let width = this.designer.statement.padding.left +
            this.textElement.getComputedTextLength() + this.designer.statement.padding.right;
        // if the width is more then max width crop the text
        if (width <= minWidth) {
            // set the width to minimam width
            width = minWidth;
        } else if (width > minWidth && width <= maxWidth) {
            // do nothing
        } else {
            // We need to truncate displayText and show an ellipses at the end.
            const ellipses = '...';
            let possibleCharactersCount = 0;
            for (let i = (text.length - 1); i > 1; i--) {
                if ((this.designer.statement.padding.left +
                    this.textElement.getSubStringLength(0, i) + this.designer.statement.padding.right) < maxWidth) {
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
            text,
        };
    }

    /**
     * Returns the width of a given string when rendered as svg text according to given options
     * Unlike `getTextWidth` this method does not try to to truncate the given text depending on its length.
     * @param {string} text - The string of which the length is measured
     * @param {Object} options - Options to be used for the rendering
     * @param {number} options.fontSize - Font size the text should be rendered for measuring width
     * @return {number} Width of the text in pixels
     * */
    getOnlyTextWidth(text, options = {}) {
        const { fontSize } = options;
        this.textElement.innerHTML = _.escape(text);
        const currentFZ = this.textElement.style.fontSize;
        this.textElement.style.fontSize = fontSize;
        const tl = this.textElement.getComputedTextLength();
        this.textElement.style.fontSize = currentFZ;
        return tl;
    }

    populateSimpleStatementBBox(expression, viewState) {
        const textViewState = this.getTextWidth(expression);
        const dropZoneHeight = this.designer.statement.gutter.v;
        viewState.components['drop-zone'] = new SimpleBBox();
        // Set statement box as an opaque element to prevent conflicts with arrows.
        viewState.components['statement-box'] = new SimpleBBox();
        viewState.components['drop-zone'].h = dropZoneHeight + (viewState.offSet || 0);
        viewState.components['drop-zone'].w = textViewState.w;
        viewState.components['statement-box'].h = this.designer.statement.height;
        viewState.components['statement-box'].w = textViewState.w;
        // set the component as a vertical block.
        // the following value will be used by arrow conflict resolver.
        viewState.components['statement-box'].setOpaque(true);

        viewState.bBox.w = textViewState.w;
        viewState.bBox.h = this.designer.statement.height + viewState.components['drop-zone'].h;

        viewState.expression = textViewState.text;
        viewState.fullExpression = expression;
        return viewState;
    }

    getHighestStatementContainer(workers) {
        const sortedWorkers = _.sortBy(workers, worker => worker.viewState.components.statementContainer.h);
        return sortedWorkers.length > 0 ?
            sortedWorkers[sortedWorkers.length - 1].getViewState().components.statementContainer.h : -1;
    }

    populateCompoundStatementChild(node, expression = undefined) {
        const viewState = node.getViewState();
        const components = viewState.components;
        components.statementContainer = new SimpleBBox();
        const statementChildren = node.filterChildren(ASTFactory.isStatement);
        const connectorDeclarationChildren = node.filterChildren(ASTFactory.isConnectorDeclaration);
        let statementContainerWidth = 0;
        let statementContainerHeight = 0;
        let widthExpansion = 0;
        let statementContainerWidthExpansion = 0;
        // Iterate over statement children
        _.forEach(statementChildren, (child) => {
            statementContainerHeight += child.viewState.bBox.h;
            if (child.viewState.bBox.w > statementContainerWidth) {
                statementContainerWidth = child.viewState.bBox.w;
            }
            if (child.viewState.bBox.expansionW > statementContainerWidthExpansion) {
                statementContainerWidthExpansion = child.viewState.bBox.expansionW;
                widthExpansion = child.viewState.bBox.expansionW;
            }
        });
        // Iterate over connector declaration children
        _.forEach(connectorDeclarationChildren, (child) => {
            statementContainerHeight += this.designer.statement.height + this.designer.statement.gutter.v;
            widthExpansion += (child.viewState.bBox.w + this.designer.blockStatement.heading.width);
            if (child.viewState.components.statementViewState.bBox.w > statementContainerWidth) {
                statementContainerWidth = child.viewState.components.statementViewState.bBox.w;
            }
        });
        // Iterating to set the height of the connector
        if (connectorDeclarationChildren.length > 0) {
            if (statementContainerHeight > (this.designer.statementContainer.height)) {
                _.forEach(connectorDeclarationChildren, (child) => {
                    child.viewState.components.statementContainer.h = statementContainerHeight - (
                        this.designer.lifeLine.head.height + this.designer.lifeLine.footer.height +
                        (this.designer.variablesPane.leftRightPadding * 2)) - this.designer.statement.padding.bottom
                        + this.designer.statement.height;
                });
            } else {
                statementContainerHeight = this.designer.statementContainer.height + (
                    this.designer.lifeLine.head.height + this.designer.lifeLine.footer.height +
                    (this.designer.variablesPane.leftRightPadding * 2)) + this.designer.statement.padding.bottom;
            }
        }

        /**
         * Add the left padding and right padding for the statement container and
         * add the additional gutter height to the statement container height, in order to keep the gap between the
         * last statement and the block statement bottom margin
         */
        statementContainerHeight += (statementContainerHeight > 0 ? this.designer.statement.gutter.v :
        this.designer.blockStatement.body.height - this.designer.blockStatement.heading.height);

        statementContainerWidth += (statementContainerWidth > 0 ?
            (this.designer.blockStatement.body.padding.left +
            this.designer.blockStatement.body.padding.right) : this.designer.blockStatement.width);

        // for compound statement like if , while we need to render condition expression
        // we will calculate the width of the expression and adjest the block statement
        if (expression !== undefined) {
            // see how much space we have to draw the condition
            const available = statementContainerWidth - this.designer.blockStatement.heading.width - 10;
            components.expression = this.getTextWidth(expression, 0, available);
        }

        components.statementContainer.h = statementContainerHeight + this.designer.statement.height;
        components.statementContainer.w = statementContainerWidth;
        viewState.bBox.h = statementContainerHeight +
            this.designer.blockStatement.heading.height + this.designer.statement.height;
        viewState.bBox.expansionW = widthExpansion;
        viewState.bBox.expansionH = statementContainerHeight + this.designer.statement.height;
        // Set the block headder as an opaque box to prevent conflicts with arrows.
        components['block-header'] = new SimpleBBox();
        components['block-header'].setOpaque(true);
        components['block-header'].h = this.designer.blockStatement.heading.height;

        viewState.bBox.w = statementContainerWidth;
        components.statementContainer.expansionW = statementContainerWidthExpansion;
    }

    addParamDimenstion(viewState, expression, param, offset) {
        const components = viewState.components;
        const paramW = util.getTextWidth(param, 3);
        components.param = new SimpleBBox(0, 0, paramW.w, 0);
        components.param.text = paramW.text;
        const joinTypeW = util.getTextWidth(expression, 3);
        const widthOfText = paramW.w + joinTypeW.w + offset +
            this.designer.blockStatement.heading.paramSeparatorOffsetX +
            this.designer.blockStatement.heading.paramSeparatorOffsetX +
            this.designer.blockStatement.heading.paramEndOffsetX;
        viewState.bBox.w = Math.max(viewState.bBox.w, widthOfText);
    }

    // Populate functions
    populatePanelDecoratorBBox(node, name) {
        const viewState = node.getViewState();
        const components = viewState.components;

        const textWidth = util.getTextWidth(name);
        viewState.titleWidth = textWidth.w + this.designer.panel.heading.title.margin.right
            + this.designer.panelHeading.iconSize.width;
        viewState.trimmedTitle = textWidth.text;

        components.heading = new SimpleBBox();
        components.heading.h = this.designer.panel.heading.height;

        components.annotation = new SimpleBBox();

        if (_.isUndefined(node.viewState.showAnnotationContainer)) {
            node.viewState.showAnnotationContainer = true;
        }

        if (!node.viewState.showAnnotationContainer || (node.isLambda && node.isLambda())) {
            components.annotation.h = 0;
        } else {
            components.annotation.h = this.getAnnotationHeight(node, 40);
        }

        components.statementContainer = new SimpleBBox();
        components.workerScopeContainer = new SimpleBBox();

        let connectorOffset = 0;
        const statementChildren = node.filterChildren(
            child => ASTFactory.isStatement(child) || ASTFactory.isConnectorDeclaration(child));
        const statementContainerWidthPadding = this.designer.statementContainer.padding.left +
            this.designer.statementContainer.padding.right;
        let statementWidth = this.designer.statementContainer.width + statementContainerWidthPadding;
        let statementHeight = 0;
        let connectorsForWorker = 0;
        _.forEach(statementChildren, (child) => {
            let childW = 0;
            let childH = 0;
            if (ASTFactory.isConnectorDeclaration(child)) {
                childW = child.viewState.components.statementViewState.bBox.w;
                childH = child.viewState.components.statementViewState.bBox.h;
                connectorsForWorker += child.viewState.bBox.w + this.designer.blockStatement.heading.width;
            } else {
                childW = child.viewState.bBox.w;
                childH = child.viewState.bBox.h;
            }
            statementHeight += childH;
            if ((childW + statementContainerWidthPadding) > statementWidth) {
                statementWidth = childW + statementContainerWidthPadding;
                if (child.viewState.bBox.expansionW > connectorOffset) {
                    connectorOffset = child.viewState.bBox.expansionW;
                }
            }
        });
        components.statementContainer.expansionW = connectorOffset;
        /**
         * We add an extra gap to the statement container height, in order to maintain the gap between the
         * last statement's bottom margin and the default worker bottom rect's top margin
         */
        statementHeight += this.designer.statement.gutter.v;

        if (statementHeight < this.designer.statementContainer.height) {
            statementHeight = this.designer.statementContainer.height;
        }

        components.statementContainer.h = statementHeight;
        components.statementContainer.w = statementWidth;
        components.body = new SimpleBBox();

        const workerChildren = node.filterChildren(child => ASTFactory.isWorkerDeclaration(child));

        const connectorChildren = node.filterChildren(
            child => ASTFactory.isConnectorDeclaration(child)
            || (ASTFactory.isAssignmentStatement(child)
            && ASTFactory.isConnectorInitExpression(child.getChildren()[1])));

        const highestStatementContainerHeight = util.getHighestStatementContainer(workerChildren);

        /**
         * If the current default worker's statement container height is less than
         * the highest worker's statement container
         * we set the default statement container height to the highest statement container's height
         */
        components.statementContainer.h = _.max([components.statementContainer.h, highestStatementContainerHeight]);

        const defaultWorkerLifeLineHeight = components.statementContainer.h + (this.designer.lifeLine.head.height * 2);
        // If more than one worker is present, then draw the worker scope container boundary around the workers
        if ((node.filterChildren(ASTFactory.isWorkerDeclaration)).length >= 1) {
            components.workerScopeContainer.w = statementWidth;
            components.workerScopeContainer.expansionW = connectorOffset + connectorsForWorker;
            components.workerScopeContainer.h = components.statementContainer.h + this.designer.canvas.padding.top +
                this.designer.canvas.padding.bottom + this.designer.statement.padding.top
                + this.designer.statement.padding.bottom;
        }

        let lifeLineWidth = 0;
        _.forEach(workerChildren.concat(connectorChildren), (child) => {
            let childViewState;
            if (ASTFactory.isAssignmentStatement(child)
                && ASTFactory.isConnectorInitExpression(child.getChildren()[1])) {
                childViewState = child.getViewState().connectorDeclViewState;
            } else {
                childViewState = child.getViewState();
            }
            lifeLineWidth += childViewState.bBox.w + childViewState.bBox.expansionW +
                this.designer.lifeLine.gutter.h;
            childViewState.bBox.h = _.max([components.statementContainer.h, highestStatementContainerHeight]) +
                (this.designer.lifeLine.head.height * 2);
            childViewState.components.statementContainer.h = _.max([components.statementContainer.h,
                highestStatementContainerHeight]);
            if (ASTFactory.isWorkerDeclaration(child)) {
                childViewState.components.workerScopeContainer.h = components.statementContainer.h +
                    this.designer.canvas.padding.top +
                    this.designer.canvas.padding.bottom + this.designer.statement.padding.top
                    + this.designer.statement.padding.bottom;
            }
            // Set the connector height of the worker's children
            const connectorChildrenOfWorker = child.filterChildren(innerChild => ASTFactory
            .isConnectorDeclaration(innerChild));
            if (connectorChildrenOfWorker.length > 0) {
                _.forEach(connectorChildrenOfWorker, (innerChild) => {
                    innerChild.getViewState().components.statementContainer.h = _.max([components.statementContainer.h,
                        highestStatementContainerHeight]);
                });
            }
        });
        if (node.viewState.collapsed) {
            components.body.h = 0;
        } else {
            components.body.h = ((this.designer.panel.body.height < defaultWorkerLifeLineHeight) ?
                    defaultWorkerLifeLineHeight : this.designer.panel.body.height)
                + this.designer.panel.body.padding.top + this.designer.panel.body.padding.bottom;
        }

        components.body.w = components.statementContainer.w + this.designer.panel.body.padding.right +
            this.designer.panel.body.padding.left + lifeLineWidth + components.statementContainer.expansionW;
        components.annotation.w = components.body.w;

        viewState.bBox.h = components.heading.h + components.body.h + components.annotation.h;

        components.parametersPrefixContainer = {};
        components.parametersPrefixContainer.w = util.getTextWidth('Parameters: ').w;
        this.populateHeadingWidth(node);
    }

    populateOuterPanelDecoratorBBox(node, name) {
        const viewState = node.getViewState();
        const components = {};
        let totalResourceHeight = 0;
        let connectorStatementContainerHeight = 0;
        const resources = node.filterChildren(child => ASTFactory.isResourceDefinition(child) ||
                ASTFactory.isConnectorAction(child));
        const connectors = node.filterChildren(child => ASTFactory.isConnectorDeclaration(child));
        let maxResourceWidth = 0;
        // Initial statement height include panel heading and panel padding.
        let bodyHeight = this.designer.panel.body.padding.top + this.designer.panel.body.padding.bottom;
        // Set the width initial value to the padding left and right
        let bodyWidth = this.designer.panel.body.padding.left + this.designer.panel.body.padding.right;

        const textWidth = this.getTextWidth(name);
        viewState.titleWidth = textWidth.w + this.designer.panel.heading.title.margin.right
            + this.designer.panelHeading.iconSize.width;
        viewState.trimmedTitle = textWidth.text;

        const variableDefinitionsHeight = this.getConnectorLevelVariablesHeight(node);

        /**
         * If there are service level connectors, their height depends on the heights of the resources
         */
        _.forEach(resources, (resource) => {
            totalResourceHeight += resource.getViewState().bBox.h;
            if (maxResourceWidth < resource.getViewState().bBox.w) {
                maxResourceWidth = resource.getViewState().bBox.w;
            }
        });

        totalResourceHeight += this.designer.panel.body.padding.top;

        /**
         * Set the max resource width to the resources
         */
        _.forEach(resources, (resource) => {
            resource.getViewState().bBox.w = maxResourceWidth;
            resource.getViewState().components.body.w = maxResourceWidth;
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
                connectorStatementContainerHeight = this.designer.statementContainer.height;
            } else {
                // Here we add additional gutter height to balance the gaps from top and bottom
                connectorStatementContainerHeight = totalResourceHeight +
                    (this.designer.panel.wrapper.gutter.v * (resources.length - 2));
            }
            /**
             * Adjust the height of the connectors and adjust the service's body width with the connector widths
             */
            _.forEach(connectors, (connector) => {
                connector.getViewState().bBox.h = connectorStatementContainerHeight +
                    (this.designer.lifeLine.head.height * 2);
                connector.getViewState().components.statementContainer.h = connectorStatementContainerHeight;
                bodyWidth += (connector.getViewState().components.statementContainer.w +
                this.designer.lifeLine.gutter.h);
            });

            bodyHeight = connectorStatementContainerHeight + this.designer.lifeLine.head.height +
                this.designer.panel.body.padding.top + this.designer.panel.body.padding.bottom;
        } else if (totalResourceHeight > 0) {
            bodyHeight = totalResourceHeight + this.designer.panel.body.padding.top +
                this.designer.panel.body.padding.bottom +
                (this.designer.panel.wrapper.gutter.v * (resources.length - 2));
        } else if (ASTFactory.isStructDefinition(node)) {
            bodyHeight = this.designer.structDefinition.body.height;
        } else {
            // There are no connectors as well as resources, since we set the default height
            bodyHeight = this.designer.innerPanel.body.height;
        }

        /**
         * Add the total variable definitions height to the total height
         */
        bodyHeight += variableDefinitionsHeight + this.designer.panel.body.padding.top;

        components.heading = new SimpleBBox();
        components.body = new SimpleBBox();
        components.annotation = new SimpleBBox();
        components.variablesPane = new SimpleBBox();
        components.transportLine = new SimpleBBox();
        components.heading.h = this.designer.panel.heading.height;
        if (node.viewState.collapsed) {
            components.body.h = 0;
        } else {
            components.body.h = bodyHeight;
        }

        if (_.isUndefined(node.viewState.showAnnotationContainer)) {
            node.viewState.showAnnotationContainer = true;
        }

        if (!node.viewState.showAnnotationContainer) {
            components.annotation.h = 0;
        } else {
            components.annotation.h = this.getAnnotationHeight(node, 40);
        }

        components.variablesPane.h = variableDefinitionsHeight;

        components.body.w = bodyWidth;
        components.annotation.w = bodyWidth;

        // transport line height calculations.
        // FIXME
        components.transportLine.h = totalResourceHeight;

        viewState.bBox.h = components.heading.h + components.body.h + components.annotation.h;
        viewState.components = components;
        this.populateHeadingWidth(node);
    }

    getStatementHeightBefore(statement) {
        const parent = statement.getParent();
        const statements = parent.filterChildren(ASTFactory.isStatement);
        const currentStatementIndex = _.indexOf(statements, statement);
        const statementsBefore = _.slice(statements, 0, currentStatementIndex);

        let height = 0;
        _.forEach(statementsBefore, (stmt) => {
            height += stmt.getViewState().bBox.h;
        });

        return height;
    }

    getDefaultStatementHeight() {
        return this.designer.statement.height + this.designer.statement.gutter.v;
    }

    /**
     *
     * @param {ASTNode} parent - parent node
     * @param {ASTNode} childNode - child node, upto which we need to calculate the total height
     * @returns {number}
     */
    getTotalHeightUpto(parent, childNode) {
        const self = this;

        const statementChildren = _.filter(parent.getChildren(), child => ASTFactory.isStatement(child));
        const nodeIndex = _.findIndex(statementChildren, child => child.id === childNode.id);

        const slicedChildren = _.slice(statementChildren, 0, nodeIndex);
        let totalHeight = 0;

        _.forEach(slicedChildren, (child) => {
            const dimensionSynced = child.getViewState().dimensionsSynced;
            if (ASTFactory.isWorkerInvocationStatement(child) && !dimensionSynced) {
                if (!child.getViewState().dimensionsSynced) {
                    self.syncWorkerInvocationDimension(child);
                }
            } else if (ASTFactory.isWorkerReplyStatement(child) && !dimensionSynced) {
                if (!child.getViewState().dimensionsSynced) {
                    self.syncWorkerReplyDimension(child);
                }
            }

            totalHeight += child.getViewState().bBox.h;
        });

        // add any connector declaration heights.
        const connectors = [];
        parent.children.forEach((child) => {
            if (childNode.getNodeIndex() > child.getNodeIndex() &&
                ASTFactory.isConnectorDeclaration(child)) {
                connectors.push(child);
            }
        });
        // here we will add any connector declaration statement height.
        _.forEach(connectors, (stmt) => {
            totalHeight += stmt.getViewState().components.statementViewState.bBox.h;
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
        const destinationWorkerName = node.getWorkerName();
        const topLevelParent = node.getTopLevelParent();
        const workersParent = ASTFactory.isWorkerDeclaration(topLevelParent) ?
            topLevelParent.getParent() : topLevelParent;

        let workerDeclaration;
        if (destinationWorkerName === 'default') {
            workerDeclaration = workersParent;
        } else {
            workerDeclaration = _.find(workersParent.getChildren(), (child) => {
                if (ASTFactory.isWorkerDeclaration(child)) {
                    return child.getWorkerName() === destinationWorkerName;
                }

                return false;
            });
        }

        if (!_.isNil(workerDeclaration)) {
            /**
             * Check whether there is a worker reply at destination, receiving the invocation
             */
            const workerName = ASTFactory.isWorkerDeclaration(topLevelParent) ?
                topLevelParent.getWorkerName() : 'default';
            const workerReplyStatement = this.getWorkerReplyStatementTo(workerDeclaration, workerName);

            if (!_.isNil(workerReplyStatement)) {
                const upToReplyHeight = util.getTotalHeightUpto(workerDeclaration, workerReplyStatement);
                const upToInvocationTotalHeight = util.getTotalHeightUpto(topLevelParent, node);

                if (upToReplyHeight > upToInvocationTotalHeight) {
                    node.getViewState().components['drop-zone'].h += upToReplyHeight - upToInvocationTotalHeight;
                    node.getViewState().bBox.h += upToReplyHeight - upToInvocationTotalHeight;
                } else {
                    workerReplyStatement.getViewState().components['drop-zone'].h += upToInvocationTotalHeight -
                        upToReplyHeight;
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
        const destinationWorkerName = node.getWorkerName();
        const topLevelParent = node.getTopLevelParent();
        const workersParent = ASTFactory.isWorkerDeclaration(topLevelParent) ?
            topLevelParent.getParent() : topLevelParent;
        let workerDeclaration;
        if (destinationWorkerName === 'default') {
            workerDeclaration = workersParent;
        } else {
            workerDeclaration = _.find(workersParent.getChildren(), (child) => {
                if (ASTFactory.isWorkerDeclaration(child)) {
                    return child.getWorkerName() === destinationWorkerName;
                }

                return false;
            });
        }
        if (!_.isNil(workerDeclaration)) {
            /**
             * Check whether there is a worker reply at destination, receiving the invocation
             */
            const workerName = ASTFactory.isWorkerDeclaration(topLevelParent) ?
                topLevelParent.getWorkerName() : 'default';
            const workerInvocationStatement = this.getWorkerInvocationStatementFrom(workerDeclaration, workerName);

            if (!_.isNil(workerInvocationStatement)) {
                const upToInvocationHeight = util.getTotalHeightUpto(workerDeclaration, workerInvocationStatement);
                const upToReplyTotalHeight = util.getTotalHeightUpto(topLevelParent, node);

                if (upToInvocationHeight > upToReplyTotalHeight) {
                    node.getViewState().components['drop-zone'].h += upToInvocationHeight - upToReplyTotalHeight;
                    node.getViewState().bBox.h += upToInvocationHeight - upToReplyTotalHeight;
                } else {
                    workerInvocationStatement.getViewState().components['drop-zone'].h += upToReplyTotalHeight -
                        upToInvocationHeight;
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
            childNodes = _.filter(parentNode.getChildren(), (child) => {
                if (ASTFactory.isWorkerReplyStatement(child)) {
                    return child.getWorkerName() === workerName;
                }
                return false;
            });
        }

        if (_.isNil(childNodes)) {
            return undefined;
        }
        return childNodes[0];
    }

    /**
     * Get the worker reply statement, which send the request from the given top level
     * node (ie resource, action, function, worker
     * @param {String} workerName
     * @param {ASTNode} parentNode - node which encapsulate the children need to be iterated (worker, function, etc)
     * @returns {undefined|ASTNode}
     */
    getWorkerInvocationStatementFrom(parentNode, workerName) {
        const childNodes = _.filter(parentNode.getChildren(), (child) => {
            if (ASTFactory.isWorkerInvocationStatement(child)) {
                return child.getWorkerName() === workerName;
            }
            return false;
        });

        if (_.isNil(childNodes)) {
            return undefined;
        }
        return childNodes[0];
    }

    /**
     * Calculates the height of the annotations of a given node.
     *
     * @param {ASTNode} node The node with annotations.
     * @param {number} [defaultHeight=0] The height value to start from.
     * @param {number} [annotationLineHeight=18.75] The default height of an annotation or annotation entry when
     * contained to a single line.
     * @returns {number} The total height needed for the annotations.
     *
     * @memberof SizingUtil
     */
    getAnnotationHeight(node, defaultHeight = 0, annotationLineHeight = 18.75) {
        let height = defaultHeight;
        if (ASTFactory.isServiceDefinition(node) || ASTFactory.isResourceDefinition(node) ||
            ASTFactory.isFunctionDefinition(node) || ASTFactory.isConnectorDefinition(node) ||
            ASTFactory.isConnectorAction(node) || ASTFactory.isAnnotationDefinition(node) ||
            ASTFactory.isStructDefinition(node)) {
            for (const annotation of node.getChildrenOfType(ASTFactory.isAnnotationAttachment)) {
                height += this.getAnnotationHeight(annotation) + 10;
            }
        } else if (!_.isNil(node) && ASTFactory.isAnnotationAttachment(node)) {
            const annotationAttachment = node;
            // Considering the start line of the annotation.
            height += annotationLineHeight;
            if (!annotationAttachment.getViewState().collapsed) {
                if (annotationAttachment.getChildren().length > 0) {
                    annotationAttachment.getChildren().forEach((annotationAttribute) => {
                        height += this.getAnnotationHeight(annotationAttribute);
                    });
                }
            }
        } else if (!_.isNil(node) && ASTFactory.isAnnotationAttribute(node)) {
            const annotationAttribute = node;
            // If the annotation entry a simple native type value
            if (annotationAttribute.getValue().isBValue()) {
                height += annotationLineHeight;
            } else if (annotationAttribute.getValue().isAnnotation()) {
                if (!annotationAttribute.getViewState().collapsed) {
                    // If the annotation entry value an annotation
                    height += this.getAnnotationHeight(annotationAttribute.getValue().getChildren()[0]);
                } else {
                    // When collapsed we have to consider attribute as a line.
                    height += annotationLineHeight;
                }
            } else if (annotationAttribute.getValue().isArray()) {
                // If the annotation entry value an array
                height += annotationLineHeight;
                if (!annotationAttribute.getViewState().collapsed) {
                    // Calculating the height for the array children.
                    annotationAttribute.getValue().getChildren().forEach((childAnnotationAttribute) => {
                        childAnnotationAttribute.getChildren().forEach((arrayChild) => {
                            height += this.getAnnotationHeight(arrayChild);
                        });
                    });
                }
            }
        } else if (!_.isNil(node) && ASTFactory.isBValue(node)) {
            height += annotationLineHeight;
        }

        return height;
    }

    getConnectorLevelVariablesHeight(node) {
        const variablesPaneDefaults = this.designer.variablesPane;
        let height = variablesPaneDefaults.topBarHeight + variablesPaneDefaults.inputHeight;

        if (!node.viewState.variablesExpanded) {
            return variablesPaneDefaults.headerHeight;
        }

        const variables = node.filterChildren(child => ASTFactory.isVariableDefinitionStatement(child));

        if (!_.isEmpty(variables)) {
            height += (variables.length * 30);
        }

        return height;
    }

    populateHeadingWidth(node) {
        const viewState = node.getViewState();
        const isLambda = (node.isLambda && node.isLambda());
        // // Creating components for parameters
        if (node.getArguments) {
            // Creating component for opening bracket of the parameters view.
            viewState.components.openingParameter = {};
            viewState.components.openingParameter.w = util.getTextWidth('(', 0).w;

            // Creating component for closing bracket of the parameters view.
            viewState.components.closingParameter = {};
            viewState.components.closingParameter.w = util.getTextWidth(')', 0).w;

            viewState.components.heading.w += viewState.components.openingParameter.w
                + viewState.components.closingParameter.w
                + this.getParameterTypeWidth(node) + 120;
        }

        // // Creating components for attachment points of the annotation
        if (node.getAttachmentPoints) {
            // Creating component for opening bracket of the parameters view.
            viewState.components.openingParameter = {};
            viewState.components.openingParameter.w = util.getTextWidth('(', 0).w;

            // Creating component for closing bracket of the parameters view.
            viewState.components.closingParameter = {};
            viewState.components.closingParameter.w = util.getTextWidth(')', 0).w;

            viewState.components.heading.w = viewState.components.openingParameter.w
                + viewState.components.closingParameter.w
                + this.annotationAttachmentPointWidth(node) + 140;
        }

        // // Creating components for return types
        if (node.getReturnTypes) {
            // Creating component for the Return type text.
            viewState.components.returnTypesIcon = {};
            viewState.components.returnTypesIcon.w = util.getTextWidth('returns', 0).w;

            // Creating component for opening bracket of the return types view.
            viewState.components.openingReturnType = {};
            viewState.components.openingReturnType.w = util.getTextWidth('(', 0).w;

            // Creating component for closing bracket of the return types view.
            viewState.components.closingReturnType = {};
            viewState.components.closingReturnType.w = util.getTextWidth(')', 0).w;

            viewState.components.heading.w += viewState.components.returnTypesIcon.w
                + viewState.components.openingReturnType.w
                + viewState.components.closingReturnType.w
                + this.getReturnTypeWidth(node) + (isLambda ? 0 : 120);
        }

        viewState.components.heading.w += viewState.titleWidth + (isLambda ? 0 : 100);

        // Get the largest among component heading width and component body width.
        const componentWidth = viewState.components.heading.w > viewState.components.body.w
            ? viewState.components.heading.w : viewState.components.body.w;

        viewState.bBox.w = componentWidth + (isLambda ? 0 : (this.designer.panel.wrapper.gutter.h * 2));
    }

    /**
     * Calculate Parameters' text width for the node.
     * @param {ASTNode} node
     * @return {number} width - return sum of widths of parameter texts.
     * */
    getParameterTypeWidth(node) {
        let width = 0;
        if (node.getArguments().length > 0) {
            for (let i = 0; i < node.getArguments().length; i++) {
                // 21 is delete button and separator widths.
                width += util.getTextWidth(node.getArguments()[i].getParameterDefinitionAsString(), 0).w + 21;
            }
        }

        return width;
    }

    /**
     * Calculate Return Parameters' text width for node.
     * @param {ASTNode} node
     * @return {number} width - return sum of widths of return parameter texts.
     * */
    getReturnTypeWidth(node) {
        let width = 0;
        if (node.getReturnTypes().length > 0) {
            for (let i = 0; i < node.getReturnTypes().length; i++) {
                width += util.getTextWidth(node.getReturnTypes()[i].getParameterDefinitionAsString(), 0).w + 21;
            }
        }
        return width;
    }

    /**
     * Calculate Attachment point text width for annotation attachments.
     * @param {AnnotationDefinition} node - Annotation Definition Node.
     * @return {number} width - return sum of the widths of attachment texts.
     * */
    annotationAttachmentPointWidth(node) {
        let width = 0;
        if (node.getAttachmentPoints().length > 0) {
            for (let i = 0; i < node.getAttachmentPoints().length; i++) {
                width += util.getTextWidth(node.getAttachmentPoints()[i], 0).w + 21;
            }
        }

        return width;
    }
}

export default SizingUtil;
