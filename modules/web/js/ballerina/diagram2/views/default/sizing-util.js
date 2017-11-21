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
import SimpleBBox from './../../../model/view/simple-bounding-box';
import TreeUtil from './../../../model/tree-util';
import { getWorkerMaxHeight } from './../../diagram-util';
import splitVariableDefByLambda from '../../../model/lambda-util';

class SizingUtil {

    constructor() {
        const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('style', 'border: 0px');
        svg.setAttribute('width', '600');
        svg.setAttribute('height', '50');
        svg.setAttributeNS('http://www.w3.org/2000/xmlns/', 'xmlns:xlink', 'http://www.w3.org/1999/xlink');
        this.textElement = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        svg.appendChild(this.textElement);
        document.body.appendChild(svg);
    }

    setConfig(config) {
        this.config = config;
    }

    /**
     * Get width of a given text and processed text
     * considering provided min and max widths.
     * @param {string} text
     * @param {number} minWidth
     * @param {number} maxWidth
     * @return {object} {width,text}
     * */
    getTextWidth(text, minWidth = this.config.statement.width, maxWidth = this.config.statement.maxWidth) {
        this.textElement.innerHTML = _.escape(text);
        let width = this.config.statement.padding.left +
            this.textElement.getComputedTextLength() + this.config.statement.padding.right;
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
                if ((this.config.statement.padding.left + this.textElement.getSubStringLength(0, i)
                    + this.config.statement.padding.right) < maxWidth) {
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

    sizeStatement(expression, viewState) {
        const textViewState = this.getTextWidth(expression);
        const dropZoneHeight = this.config.statement.gutter.v;
        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['statement-box'] = new SimpleBBox();
        viewState.components.text = new SimpleBBox();

        viewState.components['drop-zone'].h = dropZoneHeight + (viewState.offSet || 0);
        viewState.components['drop-zone'].w = textViewState.w;
        viewState.components['statement-box'].h = this.config.statement.height;
        viewState.components['statement-box'].w = textViewState.w;
        viewState.components['statement-box'].w = textViewState.w;

        // set the component as a vertical block.
        // the following value will be used by arrow conflict resolver.
        // Set statement box as an opaque element to prevent conflicts with arrows.
        viewState.components['statement-box'].setOpaque(true);

        viewState.bBox.w = textViewState.w;
        viewState.bBox.h = this.config.statement.height + viewState.components['drop-zone'].h;

        viewState.expression = textViewState.text;
        viewState.fullExpression = expression;
    }

    adjustToLambdaSize(node, viewState) {
        const { sourceFragments, lambdas } = splitVariableDefByLambda(node);
        viewState.sourceFragments = sourceFragments;
        viewState.lambdas = lambdas;
        if (lambdas.length) {
            viewState.expression = sourceFragments.join('\u0192');
            viewState.bBox.h = _.sumBy(lambdas, 'viewState.bBox.h') + viewState.components['drop-zone'].h;
            const maxW = _.maxBy(lambdas, 'viewState.bBox.w').viewState.bBox.w;
            viewState.bBox.w = maxW;
            viewState.components['statement-box'].w = viewState.bBox.w;

            for (const lambda of viewState.lambdas) {
                lambda.viewState.bBox.w = maxW;
            }
        }
    }

    /**
     * Set the container size of a particular node
     * @param {Array} nodes child nodes of the node
     * @param {object} viewState - view state of the node
     * @param {number} width - default width
     * @param {number} height - default height
     */
    setContainerSize(nodes, viewState, width = 0, height = 0) {
        // Set the default block node height
        height = this.config.blockNode.height;
        width = this.config.blockNode.width;
        let stH = this.config.statement.gutter.v;
        nodes.forEach((element) => {
            // if the statement is an endpoint
            if (!TreeUtil.isEndpointTypeVariableDef(element)) {
                stH += element.viewState.bBox.h;
                if (width < element.viewState.bBox.w) {
                    width = element.viewState.bBox.w;
                }
            }
        });
        if (stH >= height) {
            height = stH;
        }
        viewState.bBox.w = width + (this.config.statement.gutter.h * 2);
        viewState.bBox.h = height;
    }

    /**
     * Calculate dimention of Action nodes.
     *
     * @param {object} node
     *
     */
    sizeActionNode(node) {
        // Skip the init action
        if (node.id !== node.parent.initAction.id) {
            // Use the same sizing logic used for the functions.
            // TODO: need to isolate the common logic and plug them out
            this.sizeFunctionNode(node);
        }
    }


    /**
     * Calculate dimention of Annotation nodes.
     *
     * @param {object} node
     *
     */
    sizeAnnotationNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of AnnotationAttachment nodes.
     *
     * @param {object} node
     *
     */
    sizeAnnotationAttachmentNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of AnnotationAttribute nodes.
     *
     * @param {object} node
     */
    sizeAnnotationAttributeNode(node) {
        // Not implemented.
    }

    /**
     * Calculate dimention of Catch nodes.
     *
     * @param {object} node
     */
    sizeCatchNode(node) {
        this.sizeCompoundNode(node, node.getParameter());
    }

    /**
     * Calculate dimension of Finally nodes.
     *
     * @param {object} node - finally node
     */
    sizeFinallyNode(node) {
        this.sizeCompoundNode(node);
    }

    /**
     * Calculate dimention of CompilationUnit nodes.
     *
     * @param {object} node
     *
     */
    sizeCompilationUnitNode(node) {
        // Compilation unit height will be calculated by the postion util.
        this.sizePackageDeclarationNode(node);
    }


    /**
     * Calculate dimention of Connector nodes.
     *
     * @param {object} node
     *
     */
    sizeConnectorNode(node) {
        // We use the same logic used for sizing the service nodes
        this.sizeServiceNode(node);
    }


    /**
     * Calculate dimention of Enum nodes.
     *
     * @param {object} node
     *
     */
    sizeEnumNode(node) {
        const viewState = node.viewState;
        const enumerators = node.getEnumerators();
        const components = {};
        // Initial statement height include panel heading and panel padding and minus 100 as this is a enum.
        const bodyHeight = this.config.enumPanel.height;
        // Set the width initial value to the padding left and right
        const bodyWidth = this.config.panel.body.padding.left + this.config.panel.body.padding.right;

        let textWidth = this.getTextWidth(name);
        viewState.titleWidth = textWidth.w + this.config.panel.heading.title.margin.right
            + this.config.panelHeading.iconSize.width;
        viewState.trimmedTitle = textWidth.text;
        components.heading = new SimpleBBox();
        components.body = new SimpleBBox();
        components.annotation = new SimpleBBox();
        components.heading.h = this.config.panel.heading.height;
        if (node.viewState.collapsed) {
            components.body.h = 0;
        } else {
            components.body.h = bodyHeight;
        }

        if (_.isNil(node.viewState.showAnnotationContainer)) {
            node.viewState.showAnnotationContainer = true;
        }

        components.annotation.h = (!viewState.showAnnotationContainer) ? 0 : this._getAnnotationHeight(node, 35);

        components.body.w = bodyWidth;
        components.annotation.w = bodyWidth;
        viewState.bBox.h = components.heading.h + components.body.h + components.annotation.h;
        viewState.components = components;
        viewState.components.heading.w += viewState.titleWidth + this.config.enumPanel.titleWidthOffset;
        viewState.bBox.w = this.config.enumPanel.width + (this.config.panel.wrapper.gutter.h * 2);
        textWidth = this.getTextWidth(node.getName().value);
        viewState.titleWidth = textWidth.w;
        viewState.trimmedTitle = textWidth.text;
        if (!node.viewState.collapsed) {
            viewState.bBox.h += this.config.panel.body.padding.top;
        }
        const enumMaxWidth = viewState.bBox.w - this.config.panel.body.padding.right
            - this.config.panel.body.padding.left;

        const identifierHeight = viewState.bBox.h - (this.config.panel.body.padding.top
            + this.config.contentOperations.height + 10 + components.heading.h + components.annotation.h);

        if (enumerators && enumerators.length > 0) {
            let previousHeight = 25;
            let previousWidth = 120;
            enumerators.forEach((enumerator) => {
                if (TreeUtil.isEnumerator(enumerator)) {
                    // Adjust the container height as to the enumerator list.
                    if (previousWidth > enumMaxWidth
                        || (previousWidth + enumerator.viewState.w) > enumMaxWidth) {
                        previousHeight += enumerator.viewState.h
                            + this.config.enumIdentifierStatement.padding.top;
                        previousWidth = enumerator.viewState.w
                            + enumerator.viewState.components.deleteIcon.w
                            + this.config.enumIdentifierStatement.padding.left;

                        if (previousHeight >= identifierHeight && !node.viewState.collapsed) {
                            node.viewState.bBox.h += enumerator.viewState.h;
                        }
                    } else {
                        previousWidth += enumerator.viewState.w
                            + enumerator.viewState.components.deleteIcon.w
                            + this.config.enumIdentifierStatement.padding.left;
                    }
                }
            });
        }
    }


    /**
     * Calculate dimention of Enumerator nodes.
     *
     * @param {object} node
     *
     */
    sizeEnumeratorNode(node) {
        // For argument parameters and return types in the panel decorator
        const paramViewState = node.viewState;
        paramViewState.w = this.getTextWidth(node.getSource(), 0).w + 15;
        paramViewState.h = this.config.enumIdentifierStatement.height;
        paramViewState.components.expression = this.getTextWidth(node.getSource(), 0);

        // Creating component for delete icon.
        paramViewState.components.deleteIcon = {};
        paramViewState.components.deleteIcon.w = 15;
        paramViewState.components.deleteIcon.h = 15;
    }


    /**
     * Calculate dimention of Function nodes.
     *
     * @param {object} node function node
     */
    sizeFunctionNode(node) {
        const viewState = node.viewState;
        const functionBodyViewState = node.body.viewState;
        const cmp = viewState.components;
        const workers = node.workers;
        const defaultWorkerHeight = functionBodyViewState.bBox.h + (this.config.lifeLine.head.height * 2);
        let maxWorkerHeight = workers.length > 0 ? getWorkerMaxHeight(workers) : -1;
        maxWorkerHeight = Math.max(maxWorkerHeight, defaultWorkerHeight);

        /* Define the sub components */
        cmp.heading = new SimpleBBox();
        cmp.defaultWorker = new SimpleBBox();
        cmp.defaultWorkerLine = new SimpleBBox();
        cmp.panelBody = new SimpleBBox();
        cmp.argParameters = new SimpleBBox();
        cmp.returnParameters = new SimpleBBox();
        cmp.annotation = new SimpleBBox();
        cmp.argParameterHolder = {};
        cmp.returnParameterHolder = {};
        cmp.receiver = new SimpleBBox();

        // initialize the annotation status.
        if (_.isNil(viewState.showAnnotationContainer)) {
            viewState.showAnnotationContainer = true;
        }
        // calculate the annotation height.
        cmp.annotation.h = (!viewState.showAnnotationContainer) ? 0 : this._getAnnotationHeight(node, 35);

        // calculate default worker
        cmp.defaultWorker.w = workers.length > 0 ? 0 : node.body.viewState.bBox.w;
        cmp.defaultWorker.h = maxWorkerHeight;
        // We add the default worker line as a seperate component.
        cmp.defaultWorkerLine.w = workers.length > 0 ? 0 : this.config.lifeLine.width;
        cmp.defaultWorkerLine.h = cmp.defaultWorker.h;
        // set the max worker height to other workers.
        workers.forEach((worker) => {
            worker.viewState.bBox.h = maxWorkerHeight;
            worker.body.viewState.bBox.h = maxWorkerHeight - this.config.lifeLine.head.height
                                             - this.config.lifeLine.footer.height;
            worker.viewState.components.lifeLine.h = maxWorkerHeight;
            // now add the worker width to panelBody width.
            cmp.panelBody.w += this.config.lifeLine.gutter.h + worker.viewState.bBox.w;
        });
        // reduce the gutter if default worker not exist.
        if (workers.length > 0) {
            cmp.panelBody.w -= this.config.lifeLine.gutter.h;
        }
        // calculate panel body
        cmp.panelBody.h = cmp.defaultWorker.h + this.config.panel.body.padding.top
            + this.config.panel.body.padding.bottom;
        // if function is collapsed set the body height to 0.
        cmp.panelBody.h = (viewState.collapsed) ? 0 : cmp.panelBody.h;
        // calculate parameters
        cmp.heading.h = this.config.panel.heading.height;

        viewState.bBox.h = cmp.heading.h + cmp.panelBody.h + cmp.annotation.h;

        const textWidth = this.getTextWidth(node.getName().value);
        viewState.titleWidth = textWidth.w + this.config.panel.heading.title.margin.right
            + this.config.panelHeading.iconSize.width;

        cmp.parametersPrefixContainer = {};
        cmp.parametersPrefixContainer.w = this.getTextWidth('Parameters: ').w;

        // Creating components for argument parameters
        if (node.getParameters()) {
            // Creating component for opening bracket of the parameters view.
            cmp.argParameterHolder.openingParameter = {};
            cmp.argParameterHolder.openingParameter.w = this.getTextWidth('(', 0).w;

            // Creating component for closing bracket of the parameters view.
            cmp.argParameterHolder.closingParameter = {};
            cmp.argParameterHolder.closingParameter.w = this.getTextWidth(')', 0).w;

            cmp.heading.w += cmp.argParameterHolder.openingParameter.w
                + cmp.argParameterHolder.closingParameter.w
                + this.getParameterTypeWidth(node.getParameters()) + 120;
        }

        // Creating components for return types
        if (node.getReturnParameters()) {
            // Creating component for the Return type text.
            cmp.returnParameterHolder.returnTypesIcon = {};
            cmp.returnParameterHolder.returnTypesIcon.w = this.getTextWidth('returns', 0).w;

            // Creating component for opening bracket of the return types view.
            cmp.returnParameterHolder.openingReturnType = {};
            cmp.returnParameterHolder.openingReturnType.w = this.getTextWidth('(', 0).w;

            // Creating component for closing bracket of the return types view.
            cmp.returnParameterHolder.closingReturnType = {};
            cmp.returnParameterHolder.closingReturnType.w = this.getTextWidth(')', 0).w;

            cmp.heading.w += cmp.returnParameterHolder.returnTypesIcon.w
                + cmp.returnParameterHolder.openingReturnType.w
                + cmp.returnParameterHolder.closingReturnType.w
                + this.getParameterTypeWidth(node.getReturnParameters()) + 120;
        }
        // here we add the public/private falg, remove and hide button width to the header.
        cmp.heading.w += viewState.titleWidth + 280 + (this.config.panel.buttonWidth * 3);

        // Set the size of the connector declarations
        const statements = node.body.statements;
        if (statements instanceof Array) {
            statements.forEach((statement) => {
                if (TreeUtil.isEndpointTypeVariableDef(statement)) {
                    statement.viewState.bBox.w = this.config.lifeLine.width;
                    // add the connector width to panel body width.
                    cmp.panelBody.w += this.config.lifeLine.gutter.h + this.config.lifeLine.width;
                    statement.viewState.bBox.h = node.viewState.components.defaultWorker.h;
                }
            });
        }

        if (TreeUtil.isFunction(node) && !TreeUtil.isMainFunction(node)) {
            if (node.getReceiver()) {
                cmp.receiver.w = this.getTextWidth(node.getReceiver().getTypeNode().getTypeName().value, 0).w + 50;
            }
        }
        // add panel gutter to panelBody
        cmp.panelBody.w += cmp.defaultWorker.w + (this.config.panel.wrapper.gutter.h * 2);

        // Get the largest among component heading width and component body width.
        viewState.bBox.w = cmp.heading.w > cmp.panelBody.w ? cmp.heading.w : cmp.panelBody.w;
    }

    /**
     * Calculate Parameters' text width for the node.
     * width - return sum of widths of parameter texts.
     * */
    getParameterTypeWidth(parameters) {
        let width = 0;
        if (parameters.length > 0) {
            for (let i = 0; i < parameters.length; i++) {
                width += this.getTextWidth(parameters[i].getSource(), 0).w + 21;
            }
        }

        return width;
    }

    /**
     * Calculate parameters' height for the node.
     * height - return sum of height of parameters.
     * */
    getStatementHeight(statements) {
        let height = 0;
        if (statements.length > 0) {
            for (let i = 0; i < statements.length; i++) {
                height += statements[i].viewState.bBox.h;
            }
        }

        return height;
    }

    /**
     * Calculate dimention of Identifier nodes.
     *
     * @param {object} node
     *
     */
    sizeIdentifierNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Import nodes.
     *
     * @param {object} node
     *
     */
    sizeImportNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Package nodes.
     *
     * @param {object} node
     *
     */
    sizePackageNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of PackageDeclaration nodes.
     *
     * @param {object} node
     *
     */
    sizePackageDeclarationNode(node) {
        const viewState = node.viewState;
        const topGutter = 10;
        const topBarHeight = 25;
        const importInputHeight = 40;
        viewState.components.topLevelNodes = new SimpleBBox();

        let height = 0;
        const astRoot = node;

        if (viewState.importsExpanded) {
            const imports = astRoot.filterTopLevelNodes({ kind: 'Import' });

            height += topGutter + topBarHeight + importInputHeight +
                (imports.length * this.config.packageDefinition.importDeclaration.itemHeight);
        }

        if (viewState.globalsExpanded) {
            const globals = astRoot.filterTopLevelNodes({ kind: 'Variable' })
                .concat(astRoot.filterTopLevelNodes({ kind: 'Xmlns' }));
            globals.forEach((global) => {
                const text = this.getTextWidth(global.getSource(), 0, 292).text;
                global.viewState.globalText = text;
            });
            height += topGutter + topBarHeight + importInputHeight +
                (globals.length * this.config.packageDefinition.importDeclaration.itemHeight);
        }

        viewState.components.topLevelNodes.h = height;
        viewState.components.topLevelNodes.w = 0;

        viewState.components = viewState.components || {};
        viewState.components.importDeclaration = this._getImportDeclarationBadgeViewState(node);
        viewState.components.importsExpanded = this._getImportDeclarationExpandedViewState(node);
    }

    _getImportDeclarationExpandedViewState() {
        return {
            importDeclarationHeight: 30,
            importInputHeight: 40,
            topBarHeight: 25,
        };
    }

    _getImportDeclarationBadgeViewState(node) {
        const headerHeight = 35;
        const leftPadding = 10;
        const iconSize = 20;
        const importNoFontSize = 13;
        const noOfImportsLeftPadding = 12;
        const iconLeftPadding = 12;
        const noOfImportsBGHeight = 18;
        const importLabelWidth = 48.37;
        const noOfImportsTextPadding = 10;
        const importDecDecoratorWidth = 3;
        if (TreeUtil.isPackageDeclaration(node)) {
            node = node.parent;
        }
        const imports = node.filterTopLevelNodes({ kind: 'Import' });
        const noOfImports = imports.length;

        const noOfImportsTextWidth = this.getOnlyTextWidth(noOfImports, { fontSize: importNoFontSize });
        const noOfImportsBGWidth = Math.max(noOfImportsTextWidth + noOfImportsTextPadding, noOfImportsBGHeight);

        const badgeWidth = leftPadding + importLabelWidth + noOfImportsLeftPadding + noOfImportsTextWidth +
            iconLeftPadding + iconSize + leftPadding;

        return {
            headerHeight,
            leftPadding,
            iconSize,
            importNoFontSize,
            noOfImportsLeftPadding,
            iconLeftPadding,
            noOfImportsBGHeight,
            importLabelWidth,
            noOfImportsTextPadding,
            noOfImportsTextWidth,
            noOfImportsBGWidth,
            badgeWidth,
            importDecDecoratorWidth,
        };
    }


    /**
     * Calculate dimention of RecordLiteralKeyValue nodes.
     *
     * @param {object} node
     *
     */
    sizeRecordLiteralKeyValueNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Resource nodes.
     *
     * @param {object} node
     *
     */
    sizeResourceNode(node) {
        this.sizeFunctionNode(node);
    }


    /**
     * Calculate dimention of Retry nodes.
     *
     * @param {object} node
     *
     */
    sizeRetryNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
    }


    /**
     * Calculate dimention of Service nodes.
     *
     * @param {object} node
     *
     */
    sizeServiceNode(node) {
        const viewState = node.viewState;
        // There are no connectors as well as resources, since we set the default height
        const cmp = {};
        cmp.heading = new SimpleBBox();
        cmp.body = new SimpleBBox();
        cmp.initFunction = new SimpleBBox();
        cmp.transportLine = new SimpleBBox();
        cmp.connectors = new SimpleBBox();
        cmp.annotation = new SimpleBBox();
        // initialize annotation view if not defined.
        if (_.isNil(viewState.showAnnotationContainer)) {
            viewState.showAnnotationContainer = true;
        }

        // Set the service/connector definition height according to the resources/connector definitions
        // This is due to the logic re-use by the connector nodes as well
        let children = [];
        if (TreeUtil.isService(node)) {
            children = node.getResources();
        } else if (TreeUtil.isConnector(node)) {
            children = node.getActions();
        }
        let variables = [];
        let endpoints = [];
        if (TreeUtil.isService(node)) {
            variables = node.getVariables();
            endpoints = node.filterVariables((statement) => {
                return TreeUtil.isEndpointTypeVariableDef(statement);
            });
        } else if (TreeUtil.isConnector(node)) {
            variables = node.getVariableDefs();
            endpoints = node.filterVariableDefs((statement) => {
                return TreeUtil.isEndpointTypeVariableDef(statement);
            });
        }
        // calculate the annotation height.
        cmp.annotation.h = (!viewState.showAnnotationContainer) ? 0 : this._getAnnotationHeight(node, 35);

        let width = 0;
        // we will start the height with top padding.
        let height = this.config.innerPanel.wrapper.gutter.h;
        children.forEach((child) => {
            // get the max resource width.
            width = (child.viewState.bBox.w > width) ? child.viewState.bBox.w : width;
            // set the x of the resource or action.
            height += child.viewState.bBox.h + this.config.innerPanel.wrapper.gutter.v;
        });
        // add side padding to the service.
        width += (this.config.innerPanel.wrapper.gutter.h * 2);
        // calculate the initFunction for service.
        if (viewState.variablesExpanded) {
            const topGutter = 10;
            const topBarHeight = 25;
            const importInputHeight = 40;
            const globals = variables;
            cmp.initFunction.h = topGutter + topBarHeight + importInputHeight +
                (globals.length * this.config.packageDefinition.importDeclaration.itemHeight);
        } else {
            cmp.initFunction.h = this.config.variablesPane.headerHeight;
        }
        // add the init function height to body.
        height += cmp.initFunction.h;
        // if there are no children set the default height.
        height = (children.length === 0) ? this.config.panel.body.height : height;
        // if service is collapsed hide the body.
        height = (node.viewState.collapsed) ? 0 : height;
        // set the body height and width.
        cmp.body.h = height;
        cmp.body.w = width;
        let connectorHeight = 0;
            // If there are connector declarations add them to service width.
        const statements = variables;
        let connectorWidth = 0;
        connectorHeight = endpoints.length > 0
            ? (this.config.connectorDeclaration.gutter.v + this.config.panel.heading.height) : 0;
        if (statements instanceof Array) {
            statements.forEach((statement) => {
                if (TreeUtil.isEndpointTypeVariableDef(statement)) {
                    statement.viewState.bBox.w = this.config.lifeLine.width;
                        // add the connector width to body width.
                    connectorWidth += this.config.lifeLine.gutter.h + this.config.lifeLine.width;
                    statement.viewState.bBox.h = cmp.body.h - 60;
                }
            });
        }
            // add the connector to width
        width += connectorWidth;
        cmp.connectors.w = connectorWidth;
        // calculate header related components.
        const textWidth = this.getTextWidth(node.getName().value);
        viewState.titleWidth = textWidth.w;
        viewState.trimmedTitle = textWidth.text;
        // set the heading height
        cmp.heading.h = this.config.panel.heading.height;

        viewState.bBox.w = width;

        viewState.bBox.h = cmp.annotation.h + cmp.body.h + cmp.heading.h + connectorHeight +
            (viewState.variablesExpanded ? (variables.length *
                this.config.packageDefinition.importDeclaration.itemHeight) + 35 : 0);

        if (TreeUtil.isConnector(node)) {
            cmp.argParameterHolder = {};
            // Creating components for argument parameters
            if (node.getParameters()) {
                // Creating component for opening bracket of the parameters view.
                cmp.argParameterHolder.openingParameter = {};
                cmp.argParameterHolder.openingParameter.w = this.getTextWidth('(', 0).w;

                // Creating component for closing bracket of the parameters view.
                cmp.argParameterHolder.closingParameter = {};
                cmp.argParameterHolder.closingParameter.w = this.getTextWidth(')', 0).w;

                cmp.heading.w += cmp.argParameterHolder.openingParameter.w
                    + cmp.argParameterHolder.closingParameter.w
                    + this.getParameterTypeWidth(node.getParameters()) + (this.config.panel.buttonWidth * 3);
            }
        }
        // set the components.
        viewState.components = cmp;

        // Set the globals to fit the globals container
        variables.forEach((global) => {
            const text = this.getTextWidth(global.getSource(), 0, 295).text;
            global.viewState.globalText = text;
        });
    }

    _calculateChildrenDimensions(children = [], components, bBox, collapsed) {
        let newTotalStructH = this.config.panel.body.padding.top + this.config.panel.body.padding.top;
        children.forEach(() => {
            newTotalStructH += this.config.structDefinitionStatement.height;
        });

        if (!collapsed && newTotalStructH > components.body.h) {
            components.body.h = newTotalStructH;
            bBox.h += (newTotalStructH - components.body.h);
        }
    }

    /**
     * Calculate dimention of Struct nodes.
     *
     * @param {object} node
     *
     */
    sizeStructNode(node) {
        const viewState = node.viewState;
        const components = {};
        // Initial statement height include panel heading and panel padding.
        const bodyHeight = this.config.innerPanel.body.height;
        // Set the width initial value to the padding left and right
        const bodyWidth = this.config.panel.body.padding.left + this.config.panel.body.padding.right;

        let textWidth = this.getTextWidth(name);
        viewState.titleWidth = textWidth.w + this.config.panel.heading.title.margin.right
            + this.config.panelHeading.iconSize.width;
        viewState.trimmedTitle = textWidth.text;
        components.heading = new SimpleBBox();
        components.body = new SimpleBBox();
        components.annotation = new SimpleBBox();
        components.heading.h = this.config.panel.heading.height;
        if (node.viewState.collapsed) {
            components.body.h = 0;
        } else {
            components.body.h = bodyHeight;
        }

        if (_.isNil(node.viewState.showAnnotationContainer)) {
            node.viewState.showAnnotationContainer = true;
        }

        components.annotation.h = (!viewState.showAnnotationContainer) ? 0 : this._getAnnotationHeight(node, 35);

        components.body.w = bodyWidth;
        components.annotation.w = bodyWidth;
        viewState.bBox.h = components.heading.h + components.body.h + components.annotation.h;
        viewState.components = components;
        viewState.components.heading.w += viewState.titleWidth + 100;
        viewState.bBox.w = 600 + (this.config.panel.wrapper.gutter.h * 2);
        textWidth = this.getTextWidth(node.getName().value);
        viewState.titleWidth = textWidth.w;
        viewState.trimmedTitle = textWidth.text;
        if (!node.viewState.collapsed) {
            viewState.bBox.h += this.config.panel.body.padding.top;
        }
        this._calculateChildrenDimensions(node.getFields(), components, viewState.bBox, node.viewState.collapsed);
    }


    /**
     * Calculate dimention of Variable nodes.
     *
     * @param {object} node
     *
     */
    sizeVariableNode(node) {
        // For argument parameters and return types in the panel decorator
        const paramViewState = node.viewState;
        paramViewState.w = this.getTextWidth(node.getSource(), 0).w;
        paramViewState.h = this.config.panelHeading.heading.height - 7;

            // Creating component for delete icon.
        paramViewState.components.deleteIcon = {};
        paramViewState.components.deleteIcon.w = this.config.panelHeading.heading.height - 7;
        paramViewState.components.deleteIcon.h = this.config.panelHeading.heading.height - 7;
    }


    /**
     * Calculate dimension of Worker nodes.
     * @param {object} node - worker node
     */
    sizeWorkerNode(node) {
        const bBox = node.viewState.bBox;
        const workerBody = node.body;
        bBox.h = workerBody.viewState.bBox.h + this.config.lifeLine.head.height + this.config.lifeLine.footer.height;
        bBox.w = workerBody.viewState.bBox.w;
        // set the size of the lifeline.
        const cmp = node.viewState.components;
        cmp.lifeLine = new SimpleBBox();
        cmp.lifeLine.w = this.config.lifeLine.width;
        cmp.lifeLine.h = bBox.h;
    }


    /**
     * Calculate dimention of Xmlns nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlnsNode(node) {
        this.sizeStatement(node.getSource(), node.viewState);
    }

    /**
     * Calculate dimention of Transformer nodes.
     *
     * @param {object} node
     *
     */
    sizeTransformerNode(node) {
        const viewState = node.viewState;
        const cmp = viewState.components;

        cmp.heading = new SimpleBBox();
        cmp.heading.h = this.config.panel.heading.height;
        viewState.bBox.h = cmp.heading.h;

        const textWidth = this.getTextWidth(node.getSignature());
        viewState.titleWidth = textWidth.w;

        const returnParams = _.join(node.getReturnParameters().map(ret => ret.getSource()), ',');
        const typeText = `< ${node.getSourceParam().getSource()}, ${returnParams} >`;
        const typeTextDetails = this.getTextWidth(typeText, 0);
        viewState.typeText = typeTextDetails.text;
        viewState.typeTextWidth = typeTextDetails.w;

        const params = _.join(node.getParameters().map(ret => ret.getSource()), ',');
        const paramText = node.name.value ? `(${params})` : '';
        const paramTextDetails = this.getTextWidth(paramText, 0);
        viewState.paramText = paramTextDetails.text;
        viewState.paramTextWidth = paramTextDetails.w;

        const nameText = node.getName().getValue();
        let nameTextDetails;

        if (nameText) {
            nameTextDetails = this.getTextWidth(nameText, 0);
            viewState.nameText = nameTextDetails.text;
        } else {
            viewState.defaultNameText = '+ Add name';
            viewState.nameText = '';
            nameTextDetails = this.getTextWidth(viewState.defaultNameText, 0);
        }
        viewState.nameTextWidth = nameTextDetails.w;


        // here we add the expand, remove and publc/private button width to the header.
        cmp.heading.w = viewState.typeTextWidth + viewState.nameTextWidth +
            viewState.paramTextWidth + (this.config.panel.buttonWidth * 3) + 60;
        viewState.bBox.w = cmp.heading.w;
    }


    /**
     * Calculate dimention of AnnotationAttachmentAttributeValue nodes.
     *
     * @param {object} node
     *
     */
    sizeAnnotationAttachmentAttributeValueNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of ArrayLiteralExpr nodes.
     *
     * @param {object} node
     *
     */
    sizeArrayLiteralExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of BinaryExpr nodes.
     *
     * @param {object} node
     *
     */
    sizeBinaryExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of ConnectorInitExpr nodes.
     *
     * @param {object} node
     *
     */
    sizeConnectorInitExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of FieldBasedAccessExpr nodes.
     *
     * @param {object} node
     *
     */
    sizeFieldBasedAccessExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of IndexBasedAccessExpr nodes.
     *
     * @param {object} node
     *
     */
    sizeIndexBasedAccessExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Invocation nodes.
     *
     * @param {object} node
     *
     */
    sizeInvocationNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Lambda nodes.
     *
     * @param {object} node
     *
     */
    sizeLambdaNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Literal nodes.
     *
     * @param {object} node
     *
     */
    sizeLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of RecordLiteralExpr nodes.
     *
     * @param {object} node
     *
     */
    sizeRecordLiteralExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of SimpleVariableRef nodes.
     *
     * @param {object} node
     *
     */
    sizeSimpleVariableRefNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of StringTemplateLiteral nodes.
     *
     * @param {object} node
     *
     */
    sizeStringTemplateLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of TernaryExpr nodes.
     *
     * @param {object} node
     *
     */
    sizeTernaryExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of TypeCastExpr nodes.
     *
     * @param {object} node
     *
     */
    sizeTypeCastExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of TypeConversionExpr nodes.
     *
     * @param {object} node
     *
     */
    sizeTypeConversionExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of UnaryExpr nodes.
     *
     * @param {object} node
     *
     */
    sizeUnaryExprNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of XmlQname nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlQnameNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of XmlAttribute nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlAttributeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of XmlQuotedString nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlQuotedStringNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of XmlElementLiteral nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlElementLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of XmlTextLiteral nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlTextLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of XmlCommentLiteral nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlCommentLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of XmlPiLiteral nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlPiLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Abort nodes.
     *
     * @param {object} node
     *
     */
    sizeAbortNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
    }


    /**
     * Calculate dimention of Assignment nodes.
     *
     * @param {object} node
     *
     */
    sizeAssignmentNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
        this.adjustToLambdaSize(node, viewState);
    }

    /**
     * Calculate dimention of Bind nodes.
     *
     * @param {object} node
     *
     */
    sizeBindNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
    }

    /**
     * Calculate dimention of Block nodes.
     *
     * @param {object} node
     *
     */
    sizeBlockNode(node) {
        const viewState = node.viewState;
        const statements = node.getStatements();
        this.setContainerSize(statements, viewState, this.config.statement.width);
        if (viewState.alias) {
            this.sizeCompoundNode(node);
        }
    }

    /**
     * Calculate dimention of Break nodes.
     *
     * @param {object} node
     */
    sizeBreakNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
    }

    /**
     * Calculate dimention of Next nodes.
     *
     * @param {object} node
     *
     */
    sizeNextNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
    }


    /**
     * Calculate dimention of ExpressionStatement nodes.
     *
     * @param {object} node
     *
     */
    sizeExpressionStatementNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
    }


    /**
     * Calculate dimension of ForkJoin nodes.
     *
     * @param {object} node
     *
     */
    sizeForkJoinNode(node) {
        // Get the sub blocks, join and timeout blocks
        const joinStmt = node.getJoinBody();
        const timeoutStmt = node.getTimeoutBody();
        // Set default width and height to Node;
        node.viewState.bBox.h = this.config.blockNode.height;
        node.viewState.bBox.w = this.config.blockNode.width;

        // Set the compound node default sizing values.
        this.sizeCompoundNode(node);

        // Set the node height as available in the node if not set the default.
        const nodeHeight = node.viewState.bBox.h + node.viewState.components['block-header'].h;

        // Set the node width to default.
        let nodeWidth = node.viewState.bBox.w;

        if (joinStmt) {
            // Calculate join keyword, parameter expression, expression lengths;
            joinStmt.viewState.components.titleWidth = this.getTextWidth('join');
            joinStmt.viewState.components.expression = this.getTextWidth(node.getJoinConditionString());
            joinStmt.viewState.components.parameter = this.getTextWidth(node.getJoinResultVar().getSource());

            if (joinStmt.viewState.bBox.w > node.viewState.bBox.w) {
                nodeWidth = joinStmt.viewState.bBox.w;
            }
        }

        if (timeoutStmt) {
            // Calculate timeout expression, parameter expression, keyword lengths.
            timeoutStmt.viewState.components.expression = this.getTextWidth(node.getTimeOutExpression().getSource());
            timeoutStmt.viewState.components.titleWidth = this.getTextWidth('timeout');
            timeoutStmt.viewState.components.parameter = this.getTextWidth(node.getTimeOutVariable().getSource());
            if (timeoutStmt.viewState.bBox.w > nodeWidth) {
                nodeWidth = timeoutStmt.viewState.bBox.w;
            }
        }

        // Get the total of join and timeout block heights.
        let joinTimeoutBlockHeight = 0;

        if (TreeUtil.isBlock(node.parent)) {
            if (joinStmt) {
                joinTimeoutBlockHeight += joinStmt.viewState.components['statement-box'].h;
            }
            if (timeoutStmt) {
                joinTimeoutBlockHeight += timeoutStmt.viewState.components['statement-box'].h;
            }
        }
        // Get the condition box max width for join and timeout blocks.
        let conditionBoxWidth = 0;

        if (joinStmt && timeoutStmt &&
            joinStmt.viewState.components.parameter.w > timeoutStmt.viewState.components.parameter.w) {
            conditionBoxWidth += joinStmt.viewState.components.parameter.w;
        } else {
            conditionBoxWidth += timeoutStmt ? timeoutStmt.viewState.components.parameter.w :
                (joinStmt ? joinStmt.viewState.components.parameter.w : 0);
        }

        if (joinStmt && timeoutStmt &&
            joinStmt.viewState.components.expression.w > timeoutStmt.viewState.components.expression.w) {
            conditionBoxWidth += joinStmt.viewState.components.expression.w;
        } else {
            conditionBoxWidth += timeoutStmt ? timeoutStmt.viewState.components.expression.w :
                (joinStmt ? joinStmt.viewState.components.expression.w : 0);
        }

        conditionBoxWidth += timeoutStmt ? timeoutStmt.viewState.components.titleWidth.w :
            (joinStmt ? joinStmt.viewState.components.titleWidth.w : 0);

        // Get the forkJoin node width
        nodeWidth = nodeWidth > conditionBoxWidth ? nodeWidth : conditionBoxWidth;

        // Calculate the dimensions of workers.
        let maxHeightOfWorkers = 0;
        let maxWidthOfWorkers = 0;
        node.workers.forEach((worker) => {
            if (worker.viewState.bBox.h > maxHeightOfWorkers) {
                maxHeightOfWorkers = worker.viewState.bBox.h + this.config.fork.padding.top
                    + this.config.fork.padding.bottom + node.viewState.components['block-header'].h;
            }

            maxWidthOfWorkers += worker.viewState.bBox.w + this.config.fork.lifeLineGutterH;
        });

        // Set the statement box height.
        node.viewState.components['statement-box'].h = maxHeightOfWorkers === 0 ? nodeHeight : maxHeightOfWorkers;

        node.viewState.components['statement-body'] =
            new SimpleBBox(0, 0, 0, node.viewState.components['statement-box'].h, 0, 0);

        // Set the whole fork join compound box dimensions.
        node.viewState.bBox.h = node.viewState.components['statement-box'].h + joinTimeoutBlockHeight
            + this.config.fork.padding.top + this.config.fork.padding.bottom
            + node.viewState.components['block-header'].h;
        node.viewState.bBox.w = (nodeWidth > maxWidthOfWorkers ? nodeWidth : maxWidthOfWorkers)
            + this.config.fork.padding.left + this.config.fork.padding.right;
    }

    /**
     * Calculate dimension of If nodes.
     *
     * @param {object} node If Object
     */
    sizeIfNode(node) {
        this.sizeCompoundNode(node, node.getCondition());
        // If the parent of the if node is a block node, then it is only a if statement. Otherwise it is an else-if
        let nodeHeight = node.viewState.bBox.h;
        let elseStmt = node.elseStatement;
        let proceed = true;

        // If the else statement's width is greater than the node's width, we increase the node width
        // Eventually the top most if node ends up with the max width. During the positioning, increase the width to the
        // bottom most node
        if (elseStmt && elseStmt.viewState.bBox.w > node.viewState.bBox.w) {
            node.viewState.bBox.w = elseStmt.viewState.bBox.w;
        }
        if (TreeUtil.isBlock(node.parent)) {
            while (elseStmt && proceed) {
                nodeHeight += elseStmt.viewState.bBox.h;
                // If the current else statement is for an else if only, we proceed
                if (TreeUtil.isBlock(elseStmt)) {
                    proceed = false;
                } else {
                    elseStmt = elseStmt.elseStatement;
                }
            }
        }

        // Need to make the width of all the components (if, else, else if) equal
        node.viewState.bBox.h = nodeHeight;
    }

    /**
     * Calculate dimention of Reply nodes.
     *
     * @param {object} node
     */
    sizeReplyNode(node) {
        // Not implemented.
    }

    /**
     * Calculate dimention of Return nodes.
     *
     * @param {object} node
     *
     */
    sizeReturnNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
        this.adjustToLambdaSize(node, viewState);
    }


    /**
     * Calculate dimention of Comment nodes.
     *
     * @param {object} node
     *
     */
    sizeCommentNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Throw nodes.
     *
     * @param {object} node
     *
     */
    sizeThrowNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
    }


    /**
     * Calculate dimention of Transaction nodes.
     *
     * @param {object} node
     *
     */
    sizeTransactionNode(node) {
        this.sizeCompoundNode(node);
        node.viewState.components['statement-box'].h = 0;

        // We ignore the previously calculated node height and re calculate it based on the component heights
        if (node.transactionBody) {
            node.transactionBody.viewState.components.titleWidth = this.getTextWidth('Transaction');
            node.viewState.components['statement-box'].h
                += node.transactionBody.viewState.components['statement-box'].h;
            node.viewState.bBox.w = Math.max(node.viewState.bBox.w, node.transactionBody.viewState.bBox.w);
        }
        if (node.failedBody) {
            node.failedBody.viewState.components.titleWidth = this.getTextWidth('Failed');
            node.failedBody.viewState.components['statement-box'].h
                += node.failedBody.viewState.components['block-header'].h;
            node.viewState.components['statement-box'].h += node.failedBody.viewState.components['statement-box'].h;
            node.viewState.bBox.w = Math.max(node.viewState.bBox.w, node.failedBody.viewState.bBox.w);
        }
        if (node.abortedBody) {
            node.abortedBody.viewState.components.titleWidth = this.getTextWidth('Aborted');
            node.abortedBody.viewState.components['statement-box'].h
                += node.abortedBody.viewState.components['block-header'].h;
            node.viewState.components['statement-box'].h += node.abortedBody.viewState.components['statement-box'].h;
            node.viewState.bBox.w = Math.max(node.viewState.bBox.w, node.abortedBody.viewState.bBox.w);
        }
        if (node.committedBody) {
            node.committedBody.viewState.components.titleWidth = this.getTextWidth('Committed');
            node.committedBody.viewState.components['statement-box'].h
                += node.committedBody.viewState.components['block-header'].h;
            node.viewState.components['statement-box'].h += node.committedBody.viewState.components['statement-box'].h;
            node.viewState.bBox.w = Math.max(node.viewState.bBox.w, node.committedBody.viewState.bBox.w);
        }
        node.viewState.bBox.h = node.viewState.components['statement-box'].h + node.viewState.components['drop-zone'].h
            + node.viewState.components['block-header'].h;
        node.viewState.bBox.w += Math.max(node.transactionBody.viewState.components.titleWidth.w,
            (node.failedBody ? node.failedBody.viewState.components.titleWidth.w : 0),
            (node.abortedBody ? node.abortedBody.viewState.components.titleWidth.w : 0),
            (node.committedBody ? node.committedBody.viewState.components.titleWidth.w : 0));
    }

    /**
     * Calculate dimention of Transform nodes.
     *
     * @param {object} node
     *
     */
    sizeTransformNode(node) {
        const viewState = node.viewState;
        this.sizeStatement('Transform', viewState);
    }


    /**
     * Calculate dimension of Try nodes.
     *
     * @param {object} node - try node
     */
    sizeTryNode(node) {
        this.sizeCompoundNode(node);
        const catchBlocks = node.catchBlocks || [];
        let height = node.viewState.bBox.h;
        const finallyBody = node.finallyBody;
        let maxWidth = node.body.viewState.bBox.w;

        // Here we check for the max width. Consider each block's body and set the max width to the try node's width
        // During the position calculation iteration, we increase the each corresponding component's width accordingly
        catchBlocks.forEach((catchBlock) => {
            height += catchBlock.viewState.bBox.h;
            maxWidth = Math.max(maxWidth, catchBlock.body.viewState.bBox.w);
        });

        if (finallyBody) {
            height += finallyBody.viewState.bBox.h;
            maxWidth = Math.max(maxWidth, finallyBody.viewState.bBox.w);
        }

        node.viewState.bBox.h = height;
        node.viewState.bBox.w = maxWidth;
    }

    /**
     * Calculate dimention of VariableDef nodes.
     *
     * @param {object} node
     *
     */
    sizeVariableDefNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
        this.adjustToLambdaSize(node, viewState);
    }


    /**
     * Calculate dimention of While nodes.
     *
     * @param {object} node
     *
     */
    sizeWhileNode(node) {
        this.sizeCompoundNode(node, node.getCondition());
    }


    /**
     * Calculate dimention of WorkerReceive nodes.
     *
     * @param {object} node
     *
     */
    sizeWorkerReceiveNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
    }


    /**
     * Calculate dimention of WorkerSend nodes.
     *
     * @param {object} node
     *
     */
    sizeWorkerSendNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(), viewState);
    }


    /**
     * Calculate dimention of ArrayType nodes.
     *
     * @param {object} node
     *
     */
    sizeArrayTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of BuiltInRefType nodes.
     *
     * @param {object} node
     *
     */
    sizeBuiltInRefTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of ConstrainedType nodes.
     *
     * @param {object} node
     *
     */
    sizeConstrainedTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of FunctionType nodes.
     *
     * @param {object} node
     *
     */
    sizeFunctionTypeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of UserDefinedType nodes.
     *
     * @param {object} node
     *
     */
    sizeUserDefinedTypeNode(node) {
        // Not implemented.
    }

    /**
     * Calculate dimention of EndpointType nodes.
     *
     * @param {object} node
     *
     */
    sizeEndpointTypeNode(node) {
        // Not implemented.
    }

    /**
     * Calculate dimention of ValueType nodes.
     *
     * @param {object} node
     *
     */
    sizeValueTypeNode(node) {
        // Not implemented.
    }

    /**
     * Set the sizing of the compound statement node (eg: IF, ElseIF, Try, Catch, etc.)
     * @param {object} node compound statement node
     * @param {string} node expression
     */
    sizeCompoundNode(node, nodeExpression) {
        const expression = nodeExpression;
        const viewState = node.viewState;
        const components = viewState.components;
        const dropZoneHeight = TreeUtil.isBlock(node.parent) ? this.config.statement.gutter.v : 0;
        const nodeBodyViewState = TreeUtil.isBlock(node) ? node.viewState :
            (TreeUtil.isTransaction(node) ? node.transactionBody.viewState :
                (TreeUtil.isForkJoin(node) ? node.viewState : node.body.viewState));
        components.body = new SimpleBBox();

        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['statement-box'] = new SimpleBBox();
        // Set the block header as an opaque box to prevent conflicts with arrows.
        components['block-header'] = new SimpleBBox();
        viewState.components.text = new SimpleBBox();

        const bodyWidth = nodeBodyViewState.bBox.w;
        const bodyHeight = nodeBodyViewState.bBox.h;

        components['block-header'].h = this.config.blockStatement.heading.height;

        viewState.components['drop-zone'].h = dropZoneHeight + (viewState.offSet || 0);
        viewState.components['drop-zone'].w = bodyWidth;
        viewState.components['statement-box'].h = bodyHeight + this.config.blockStatement.heading.height;
        viewState.components['statement-box'].w = bodyWidth;
        viewState.bBox.h = viewState.components['statement-box'].h + viewState.components['drop-zone'].h;
        viewState.bBox.w = bodyWidth;
        components.body.w = bodyWidth;

        components['block-header'].setOpaque(true);

        // for compound statement like if , while we need to render condition expression
        // we will calculate the width of the expression and adjust the block statement
        if (expression) {
            // see how much space we have to draw the condition
            const available = bodyWidth - this.config.blockStatement.heading.width - 10;
            components.expression = this.getTextWidth(expression.getSource(), 0, available);
        }
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
    _getAnnotationHeight(node, defaultHeight = 0, annotationLineHeight = 18) {
        let height = defaultHeight;
        if (TreeUtil.isService(node) || TreeUtil.isResource(node) ||
            TreeUtil.isFunction(node) || TreeUtil.isConnector(node) ||
            TreeUtil.isAction(node) || TreeUtil.isAnnotation(node) ||
            TreeUtil.isStruct(node)) {
            for (const annotation of node.getAnnotationAttachments()) {
                height += this._getAnnotationHeight(annotation) + 10;
            }
        } else if (!_.isNil(node) && TreeUtil.isAnnotationAttachment(node)) {
            const annotationAttachment = node;

            // Considering the start line of the annotation.
            height += annotationLineHeight;
            if (_.isNil(annotationAttachment.viewState.addingEmptyAttribute)) {
                annotationAttachment.viewState.addingEmptyAttribute = false;
            } else if (annotationAttachment.viewState.addingEmptyAttribute === true) {
                height += annotationLineHeight;
            }
            if (!annotationAttachment.viewState.collapsed) {
                if (annotationAttachment.getAttributes().length > 0) {
                    annotationAttachment.getAttributes().forEach((annotationAttribute) => {
                        height += this._getAnnotationHeight(annotationAttribute);
                    });
                }
            }
        } else if (!_.isNil(node) && TreeUtil.isAnnotationAttachmentAttribute(node)) {
            const annotationAttachmentAttribute = node;
            if (_.isNil(annotationAttachmentAttribute.viewState.addingEmptyAttribute)) {
                annotationAttachmentAttribute.viewState.addingEmptyAttribute = false;
            } else if (annotationAttachmentAttribute.viewState.addingEmptyAttribute === true) {
                height += annotationLineHeight;
            }
            const annotationAttachmentAttributeValue = annotationAttachmentAttribute.getValue();
            // If the annotation entry a simple native type value
            if (annotationAttachmentAttributeValue.isValueLiteral()) {
                height += annotationLineHeight;
            } else if (annotationAttachmentAttributeValue.isValueAnnotationAttachment()) {
                if (!annotationAttachmentAttribute.viewState.collapsed) {
                    // If the annotation entry value an annotation
                    height += this._getAnnotationHeight(annotationAttachmentAttributeValue.getValue());
                } else {
                    // When collapsed we have to consider attribute as a line.
                    height += annotationLineHeight;
                }
            } else if (annotationAttachmentAttributeValue.isValueArray()) {
                // If the annotation entry value an array
                height += annotationLineHeight;
                if (!annotationAttachmentAttribute.viewState.collapsed) {
                    // Calculating the height for the array children.
                    annotationAttachmentAttributeValue.getValueArray().forEach((childNode) => {
                        height += this._getAnnotationHeight(childNode.getValue());
                    });
                }
            }
        } else if (!_.isNil(node) && TreeUtil.isLiteral(node)) {
            height += annotationLineHeight;
        }

        return height;
    }
}

export default SizingUtil;
