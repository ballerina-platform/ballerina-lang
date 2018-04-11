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
import * as DesignerDefaults from './designer-defaults';
import splitVariableDefByLambda from '../../../model/lambda-util';

const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
svg.setAttribute('style', 'border: 0px');
svg.setAttribute('width', '600');
svg.setAttribute('height', '50');
svg.setAttributeNS('http://www.w3.org/2000/xmlns/', 'xmlns:xlink', 'http://www.w3.org/1999/xlink');
const textElement = document.createElementNS('http://www.w3.org/2000/svg', 'text');
svg.appendChild(textElement);
document.body.appendChild(svg);

class SizingUtil {

    constructor() {
        this.textElement = textElement;
        this.config = DesignerDefaults;
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
    getTextWidth(text, minWidth = this.config ? this.config.statement.width : 0,
                                                        maxWidth = this.config ? this.config.statement.maxWidth : 0) {
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
        const viewState = node.viewState;
        const components = viewState.components;
        const dropZoneHeight = TreeUtil.isBlock(node.parent) ? this.config.statement.gutter.v : 0;
        const nodeBodyViewState = node.body.viewState;

        // flow chart if width and height is different to normal block node width and height
        nodeBodyViewState.bBox.w = (nodeBodyViewState.bBox.w < this.config.compoundStatement.width) ?
                                    this.config.compoundStatement.width : nodeBodyViewState.bBox.w;
        nodeBodyViewState.bBox.h += this.config.statement.gutter.v;

        components.body = new SimpleBBox();

        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['statement-box'] = new SimpleBBox();
        // Set the block header as an opaque box to prevent conflicts with arrows.
        components['block-header'] = new SimpleBBox();
        viewState.components.text = new SimpleBBox();

        const bodyWidth = nodeBodyViewState.bBox.w;
        const bodyHeight = nodeBodyViewState.bBox.h;

        components['block-header'].h = this.config.compoundStatement.heading.height
                                        + this.config.compoundStatement.padding.top;

        viewState.components['drop-zone'].h = dropZoneHeight + (viewState.offSet || 0);
        viewState.components['drop-zone'].w = bodyWidth;
        viewState.components['statement-box'].h = bodyHeight;
        viewState.components['statement-box'].w = bodyWidth;
        viewState.bBox.h = viewState.components['statement-box'].h
                            + viewState.components['drop-zone'].h
                            + components['block-header'].h;
        viewState.bBox.w = bodyWidth;

        components['block-header'].setOpaque(true);

        // for compound statement like if , while we need to render condition expression
        // we will calculate the width of the expression and adjust the block statement
        const expression = node.getParameter();
        if (expression) {
            components.expression = this.getTextWidth(expression.getSource(true), 0,
                                        this.config.compoundStatement.heading.width);
        }
    }

    /**
     * Calculate dimension of Finally nodes.
     *
     * @param {object} node - finally node
     */
    sizeFinallyNode(node) {
    }

    /**
     * Calculate dimention of CompilationUnit nodes.
     *
     * @param {object} node
     *
     */
    sizeCompilationUnitNode(node) {

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
        // Do nothing
    }


    /**
     * Calculate dimention of Enumerator nodes.
     *
     * @param {object} node
     *
     */
    sizeEnumeratorNode(node) {
        // Do nothing
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
        const defaultWorkerHeight = functionBodyViewState.bBox.h + (this.config.lifeLine.head.height * 2)
            + (this.config.statement.height * 2);
        let maxWorkerHeight = workers.length > 0 ? this.getWorkerMaxHeight(workers) : -1;
        maxWorkerHeight = Math.max(maxWorkerHeight, defaultWorkerHeight);

        /* Define the sub components */
        cmp.heading = new SimpleBBox();
        cmp.defaultWorker = new SimpleBBox();
        cmp.defaultWorkerLine = new SimpleBBox();
        cmp.panelBody = new SimpleBBox();
        cmp.receiver = new SimpleBBox();
        cmp.client = new SimpleBBox();

        // calculate client line
        cmp.client.w = this.config.clientLine.width;
        cmp.client.h = maxWorkerHeight;
        cmp.client.arrowLine = (cmp.client.w / 2);
        const paramText = node.parameters.filter((param) => {
            // skip if the param is service endpoint.
            return !param.serviceEndpoint;
        }).map((param) => {
            return param.name.value;
        }).join(', ');
        const paramTextWidth = this.getTextWidth(paramText, 0,
            (this.config.clientLine.width + this.config.lifeLine.gutter.h));
        cmp.client.text = paramTextWidth.text;
        cmp.client.fullText = paramText;
        cmp.client.title = this.getTextWidth(node.getClientTitle(), 0, (this.config.clientLine.head.width)).text;

        // calculate default worker
        cmp.defaultWorker.w = workers.length > 0 ? 0 : node.body.viewState.bBox.w;
        cmp.defaultWorker.h = maxWorkerHeight;
        // We add the default worker line as a seperate component.
        cmp.defaultWorkerLine.w = workers.length > 0 ? 0 : this.config.lifeLine.width;
        cmp.defaultWorkerLine.h = cmp.defaultWorker.h;

        let defaultWorkerWidth = 0;
        if (workers.length === 0) {
            defaultWorkerWidth = cmp.defaultWorker.w + this.config.lifeLine.gutter.h;
        }

        // set the max worker height to other workers.
        let workersWidth = 0;
        workers.forEach((worker) => {
            worker.viewState.bBox.h = maxWorkerHeight;
            worker.body.viewState.bBox.h = maxWorkerHeight - this.config.lifeLine.head.height
                                             - (this.config.lifeLine.footer.height * 2);
            worker.viewState.components.lifeLine.h = maxWorkerHeight;
            // now add the worker width to panelBody width.
            workersWidth += this.config.lifeLine.gutter.h + worker.viewState.bBox.w;
        });

        // calculate panel body
        cmp.panelBody.h = cmp.defaultWorker.h + this.config.panel.body.padding.top
            + this.config.panel.body.padding.bottom;
        // if function is collapsed set the body height to 0.
        cmp.panelBody.h = (viewState.collapsed) ? 0 : cmp.panelBody.h;
        // calculate parameters
        cmp.heading.h = this.config.panel.heading.height;

        viewState.bBox.h = cmp.heading.h + cmp.panelBody.h;

        const textWidth = this.getTextWidth(node.getName().value);
        viewState.titleWidth = textWidth.w + this.config.panel.heading.title.margin.right
            + this.config.panelHeading.iconSize.width;

        // here we add the public/private falg, remove and hide button width to the header.
        cmp.heading.w += viewState.titleWidth + (this.config.panel.buttonWidth * 3);

        // Set the size of the endpoint declarations
        const endpoints = node.endpointNodes;
        let endpointWidth = 0;
        if (endpoints instanceof Array) {
            endpoints.forEach((endpoint) => {
                endpoint.viewState.bBox.w = this.config.lifeLine.width;
                // add the endpoint width to panel body width.
                endpointWidth += this.config.lifeLine.gutter.h + this.config.lifeLine.width;
                endpoint.viewState.bBox.h = node.viewState.components.defaultWorker.h;
                endpoint.viewState.endpointIdentifier =
                    this.getTextWidth(endpoint.name.value, 0, endpointWidth).text;
            });
        }

        if (TreeUtil.isFunction(node) && !TreeUtil.isMainFunction(node)) {
            if (node.getReceiver()) {
                cmp.receiver.w = this.getTextWidth(node.getReceiver().getTypeNode().getTypeName().value, 0).w + 50;
            }
        }
        // add panel gutter to panelBody
        cmp.panelBody.w = this.config.panel.body.padding.left + cmp.client.w + defaultWorkerWidth
                        + workersWidth + endpointWidth
                        + this.config.panel.body.padding.right;

        // Get the largest among component heading width and component body width.
        if (cmp.heading.w > cmp.panelBody.w) {
            viewState.bBox.w = cmp.heading.w;
            cmp.panelBody.w = cmp.heading.w;
        } else {
            viewState.bBox.w = cmp.panelBody.w;
            cmp.heading.w = cmp.panelBody.w;
        }
    }

    /**
     * Calculate Parameters' text width for the node.
     * width - return sum of widths of parameter texts.
     * */
    getParameterTypeWidth(parameters) {
        let width = 0;
        if (parameters.length > 0) {
            for (let i = 0; i < parameters.length; i++) {
                width += this.getTextWidth(parameters[i].getSource(true), 0).w + 21;
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

        // calculate http methods dimensions
        let annotationAttachment = node.filterAnnotationAttachments((attachment) => {
            return attachment.getAnnotationName().value === 'resourceConfig';
        })[0];
        annotationAttachment = annotationAttachment || {};
        annotationAttachment.attributes = annotationAttachment.attributes || [];
        const pathAtributeNode = _.filter(annotationAttachment.attributes, (atribute) => {
            return atribute.getName().value === 'methods';
        })[0];
        node.viewState.components.httpMethods = {
            bBox: new SimpleBBox(),
        };

        if (pathAtributeNode && pathAtributeNode.getValue() && pathAtributeNode.getValue().getValueArray()) {
            pathAtributeNode.getValue().getValueArray().forEach((method) => {
                const textWidth = this.getTextWidth(method.getValue().unescapedValue, 15, 80);
                node.viewState.components.httpMethods[textWidth.text] = {};
                node.viewState.components.httpMethods[textWidth.text].w = textWidth.w;
                node.viewState.components.httpMethods[textWidth.text].offset =
                    node.viewState.components.httpMethods.bBox.w;
                node.viewState.components.httpMethods.bBox.w += textWidth.w + 10;
            });
        }
    }


    /**
     * Calculate dimention of Retry nodes.
     *
     * @param {object} node
     *
     */
    sizeRetryNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(true), viewState);
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
        cmp.title = new SimpleBBox();
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

        viewState.bBox.h = cmp.annotation.h + cmp.body.h + cmp.heading.h + connectorHeight;

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
            const text = this.getTextWidth(global.getSource(true), 0, 295).text;
            global.viewState.globalText = text;
        });
    }

    /**
     * Calculate dimention of Struct nodes.
     *
     * @param {object} node
     *
     */
    sizeStructNode(node) {

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
        paramViewState.w = this.getTextWidth(node.getSource(true), 0).w;
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
        bBox.h = workerBody.viewState.bBox.h
            + this.config.lifeLine.head.height + this.config.lifeLine.footer.height;

        if (node.parent && !TreeUtil.isForkJoin(node.parent)) {
            bBox.h += (this.config.statement.height * 2); // Top gap for client invoke line
        }

        bBox.w = workerBody.viewState.bBox.w;
        if (workerBody.viewState.leftMargin) {
            bBox.w += workerBody.viewState.leftMargin;
        }
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
        this.sizeStatement(node.getSource(true), node.viewState);
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

        const returnParams = _.join(node.getReturnParameters().map(ret => ret.getSource(true)), ',');
        const typeText = `< ${node.getSource().getSource(true)}, ${returnParams} >`;
        const typeTextDetails = this.getTextWidth(typeText, 0);
        viewState.typeText = typeTextDetails.text;
        viewState.typeTextWidth = typeTextDetails.w;

        const params = _.join(node.getParameters().map(ret => ret.getSource(true)), ',');
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
        // Invocation nodes are sized in sizeExpressionStatementNode
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
     * Calculate dimention of TypeofExpression nodes.
     *
     * @param {object} node
     *
     */
    sizeTypeofExpressionNode(node) {
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
     * Calculate dimention of MatchExpression nodes.
     *
     * @param {object} node
     *
     */
    sizeMatchExpressionNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of MatchExpressionPatternClause nodes.
     *
     * @param {object} node
     *
     */
    sizeMatchExpressionPatternClauseNode(node) {
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
        this.sizeStatement(node.getSource(true), viewState);
    }


    /**
     * Calculate dimention of Assignment nodes.
     *
     * @param {object} node
     *
     */
    sizeAssignmentNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(true), viewState);
        this.adjustToLambdaSize(node, viewState);
        this.sizeActionInvocationStatement(node);
        this.sizeClientResponderStatement(node);
    }

    /**
     * Calculate dimention of Bind nodes.
     *
     * @param {object} node
     *
     */
    sizeBindNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(true), viewState);
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
        this.calcLeftMargin(viewState.bBox, statements, false, false);
        if (viewState.compound) {
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
        this.sizeStatement(node.getSource(true), viewState);
    }

    /**
     * Calculate dimention of Next nodes.
     *
     * @param {object} node
     *
     */
    sizeNextNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(true), viewState);
    }


    /**
     * Calculate dimention of ExpressionStatement nodes.
     *
     * @param {object} node
     *
     */
    sizeExpressionStatementNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(true), viewState);
        this.sizeActionInvocationStatement(node);
        this.sizeClientResponderStatement(node);
    }


    /**
     * Calculate dimention of Foreach nodes.
     *
     * @param {object} node
     *
     */
    sizeForeachNode(node) {
        this.sizeWhileNode(node);
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

        // Calculate the dimensions of workers.
        let forkHeight = 0;
        let forkWidth = 0;
        node.workers.forEach((worker) => {
            if (worker.viewState.bBox.h > forkHeight) {
                forkHeight = worker.viewState.bBox.h;
            }
            forkWidth += worker.viewState.bBox.w;
        });
        // TODO Keep the padding for fork.
        forkHeight += 80;

        // Set the whole fork join compound box dimensions.
        const cmp = {};
        cmp.forkContainer = new SimpleBBox();
        cmp.joinHeader = new SimpleBBox();
        cmp.timeoutHeader = new SimpleBBox();

        const joinBBox = (joinStmt) ? joinStmt.viewState.bBox : new SimpleBBox();
        const timeoutBBox = (timeoutStmt) ? timeoutStmt.viewState.bBox : new SimpleBBox();

        cmp.forkContainer.w = forkWidth;
        cmp.forkContainer.h = forkHeight;

        // Fork lines
        forkHeight += (this.config.statement.height * 3);

        // Add join statement
        forkHeight += _.max([joinBBox.h, timeoutBBox.h]);

        // Size join and timeout headers
        cmp.joinHeader.w = 120;
        cmp.joinHeader.h = (this.config.statement.height * 2);

        cmp.timeoutHeader.w = 100;
        cmp.timeoutHeader.h = (this.config.statement.height * 2);

        // Add height to accomodate join and timeout headers.
        forkHeight += cmp.joinHeader.h;

        const joinAndTimeOut = joinBBox.w + timeoutBBox.w;
        forkWidth = _.max([forkWidth, joinAndTimeOut]);

        node.viewState.bBox.h = forkHeight;
        node.viewState.bBox.w = forkWidth - (this.config.lifeLine.width / 2);
        node.viewState.components = cmp;
    }

    /**
     * Calculate dimension of If nodes.
     *
     * @param {object} node If Object
     */
    sizeIfNode(node) {
        // if block sizing
        const expression = node.getCondition();
        const viewState = node.viewState;
        const components = viewState.components;
        const dropZoneHeight = TreeUtil.isBlock(node.parent) ? this.config.statement.gutter.v : 0;
        const nodeBodyViewState = node.body.viewState;

        // flow chart if width and height is different to normal block node width and height
        nodeBodyViewState.bBox.w = (nodeBodyViewState.bBox.w < this.config.flowChartControlStatement.width) ?
                                    this.config.flowChartControlStatement.width : nodeBodyViewState.bBox.w;
        nodeBodyViewState.bBox.h += this.config.statement.gutter.v;

        components.body = new SimpleBBox();

        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['statement-box'] = new SimpleBBox();
        // Set the block header as an opaque box to prevent conflicts with arrows.
        components['block-header'] = new SimpleBBox();
        viewState.components.text = new SimpleBBox();

        const bodyWidth = nodeBodyViewState.bBox.w;
        const bodyHeight = nodeBodyViewState.bBox.h;

        components['block-header'].h = this.config.flowChartControlStatement.heading.height
                                        + this.config.flowChartControlStatement.padding.top
                                        + this.config.flowChartControlStatement.heading.gap;

        viewState.components['drop-zone'].h = dropZoneHeight + (viewState.offSet || 0);
        viewState.components['drop-zone'].w = bodyWidth;
        viewState.components['statement-box'].h = bodyHeight;
        viewState.components['statement-box'].w = bodyWidth;
        viewState.bBox.h = viewState.components['statement-box'].h
                            + viewState.components['drop-zone'].h
                            + components['block-header'].h;
        viewState.bBox.w = bodyWidth;

        components['block-header'].setOpaque(true);

        // calculate left margin from the lifeline centre
        this.calcLeftMargin(node.viewState.bBox, node.body.statements);

        // for compound statement like if , while we need to render condition expression
        // we will calculate the width of the expression and adjust the block statement
        if (expression) {
            components.expression = this.getTextWidth(expression.getSource(true), 0,
                                        this.config.flowChartControlStatement.heading.width - 5);
        }

        // end of if block sizing

        let nodeHeight = viewState.bBox.h;
        let nodeWidth = node.viewState.bBox.w;

        const elseStmt = node.elseStatement;
        if (elseStmt && (!(TreeUtil.isBlock(elseStmt) && elseStmt.statements.length === 0))) {
            const elseHeight = elseStmt.viewState.bBox.h;
            nodeHeight += elseHeight;
            nodeWidth += elseStmt.viewState.bBox.w;
        }
        node.viewState.bBox.h = nodeHeight;
        node.viewState.bBox.w = nodeWidth;

        // if the statement right before if statement end is one with
        // a separator, arrow must not be drawn to it. It should only be a line.
        // e.g. :
        // if (true) {
        //     while (true) {
        //     }
        // }
        if ((node.body.statements.length > 0 && TreeUtil.isWhile(_.last(node.body.statements)))
                && (!elseStmt || (TreeUtil.isBlock(elseStmt) && elseStmt.statements.length === 0))) {
            node.viewState.isLastPathLine = true;
        }
    }

    /**
     * Calculate dimention of Match nodes.
     *
     * @param {object} node
     *
     */
    sizeMatchNode(node) {
        const components = node.viewState.components;
        let height = this.config.statement.height;
        let width = 0;

        node.patternClauses.forEach((element) => {
            height += element.viewState.bBox.h;
            if (width < element.viewState.bBox.w) {
                width = element.viewState.bBox.w;
            }
        });

        // Calculate the left margin.
        this.calcLeftMargin(node.viewState.bBox, node.patternClauses, true);

        node.viewState.bBox.h = height;
        node.viewState.bBox.w = (this.config.statement.width < width) ? width :
            100;
        components.expression = this.getTextWidth(node.expression.getSource(true), 0,
            node.viewState.bBox.w / 2);
    }


    /**
     * Calculate dimention of MatchPatternClause nodes.
     *
     * @param {object} node
     *
     */
    sizeMatchPatternClauseNode(node) {
        const components = node.viewState.components;
        node.viewState.bBox.h = node.statement.viewState.bBox.h + this.config.statement.height;
        node.viewState.bBox.w = node.statement.viewState.bBox.w;
        components.expression = this.getTextWidth(node.variableNode.getSource(true), 0,
            node.viewState.bBox.w / 2);

        node.viewState.bBox.h += 10;
        // Calculate the left margin.
        this.calcLeftMargin(node.viewState.bBox, [node.statement], false, false);
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
        this.sizeStatement(node.getSource(true), viewState);
        this.adjustToLambdaSize(node, viewState);
        this.sizeClientResponderStatement(node);
        if (viewState.displayText === '()') {
            viewState.displayText = '';
        }
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
        this.sizeStatement(node.getSource(true), viewState);
    }


    /**
     * Calculate dimention of Transaction nodes.
     *
     * @param {object} node
     *
     */
    sizeTransactionNode(node) {
        // size transaction node
        const viewState = node.viewState;
        const components = viewState.components;
        const dropZoneHeight = TreeUtil.isBlock(node.parent) ? this.config.statement.gutter.v : 0;
        const nodeBodyViewState = node.transactionBody.viewState;

        // flow chart if width and height is different to normal block node width and height
        nodeBodyViewState.bBox.w = (nodeBodyViewState.bBox.w < this.config.compoundStatement.width) ?
                                    this.config.compoundStatement.width : nodeBodyViewState.bBox.w;
        nodeBodyViewState.bBox.h += this.config.statement.gutter.v;

        components.body = new SimpleBBox();

        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['statement-box'] = new SimpleBBox();
        // Set the block header as an opaque box to prevent conflicts with arrows.
        components['block-header'] = new SimpleBBox();
        viewState.components.text = new SimpleBBox();

        const bodyWidth = nodeBodyViewState.bBox.w;
        const bodyHeight = nodeBodyViewState.bBox.h;

        components['block-header'].h = this.config.statement.height;

        viewState.components['drop-zone'].h = dropZoneHeight + (viewState.offSet || 0);
        viewState.components['drop-zone'].w = bodyWidth;
        viewState.components['statement-box'].h = bodyHeight;
        viewState.components['statement-box'].w = bodyWidth;

        viewState.bBox.h = viewState.components['statement-box'].h
                            + viewState.components['drop-zone'].h
                            + components['block-header'].h;
        viewState.bBox.w = bodyWidth;

        components['block-header'].setOpaque(true);

        // calculate left margin from the lifeline centre
        this.calcLeftMargin(node.viewState.bBox, node.transactionBody.statements);

        // end of try block sizing

        let nodeHeight = viewState.bBox.h;
        let nodeWidth = viewState.bBox.w;

        if (node.onRetryBody) {
            nodeHeight += node.onRetryBody.viewState.components['statement-box'].h;
            nodeHeight -= this.config.compoundStatement.padding.top;
            nodeWidth += node.onRetryBody.viewState.bBox.w;
        }

        node.viewState.bBox.h = nodeHeight;
        node.viewState.bBox.w = nodeWidth;
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
        const catchStmts = node.catchBlocks || [];

        // size try node
        const viewState = node.viewState;
        const components = viewState.components;
        const dropZoneHeight = TreeUtil.isBlock(node.parent) ? this.config.statement.gutter.v : 0;
        const nodeBodyViewState = node.body.viewState;

        // flow chart if width and height is different to normal block node width and height
        nodeBodyViewState.bBox.w = (nodeBodyViewState.bBox.w < this.config.compoundStatement.width) ?
                                    this.config.compoundStatement.width : nodeBodyViewState.bBox.w;
        nodeBodyViewState.bBox.h += this.config.statement.gutter.v;

        components.body = new SimpleBBox();

        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['statement-box'] = new SimpleBBox();
        // Set the block header as an opaque box to prevent conflicts with arrows.
        components['block-header'] = new SimpleBBox();
        viewState.components.text = new SimpleBBox();

        let bodyWidth = nodeBodyViewState.bBox.w;
        const bodyHeight = nodeBodyViewState.bBox.h;

        components['block-header'].h = this.config.statement.height + this.config.compoundStatement.padding.top;

        viewState.components['drop-zone'].h = dropZoneHeight + (viewState.offSet || 0);
        viewState.components['drop-zone'].w = bodyWidth;
        viewState.components['statement-box'].h = bodyHeight;
        viewState.components['statement-box'].w = bodyWidth;

        if (catchStmts.length > 0) {
            // add an additional gap to allow the catch clauses in try
            components['block-header'].h += (3 * this.config.compoundStatement.padding.top);
            // for the connector line of the first catch clause
            bodyWidth += this.config.compoundStatement.gap.left;
        }

        viewState.bBox.h = viewState.components['statement-box'].h
                            + viewState.components['drop-zone'].h
                            + components['block-header'].h;
        viewState.bBox.w = bodyWidth;

        components['block-header'].setOpaque(true);

        // calculate left margin from the lifeline centre
        this.calcLeftMargin(viewState.bBox, node.body.statements);

        // end of try block sizing

        let nodeHeight = viewState.bBox.h;
        let nodeWidth = viewState.bBox.w;

        // try catch sizing
        catchStmts.forEach((catchStmt) => {
            const catchHeight = catchStmt.body.viewState.bBox.h;
            nodeHeight += catchHeight;
            nodeWidth += catchStmt.viewState.bBox.w;
        });

        // finally sizing
        const finallyBody = node.finallyBody;
        if (finallyBody) {
            // since finally is a block node, the sizing of the finally title has to be done within try block
            // rendering will also be done in try decorator
            node.viewState.components['finally-block'] = new SimpleBBox();
            const finallyViewState = node.viewState.components['finally-block'];
            finallyViewState.components = {};
            finallyViewState.components['block-header'] = new SimpleBBox();
            finallyViewState.components['block-header'].h = this.config.statement.height;
            finallyViewState.components['block-header'].w = finallyBody.viewState.bBox.w;
            finallyViewState.components['statement-box'] = finallyBody.viewState.bBox;
            finallyViewState.w = finallyBody.viewState.bBox.w;
            finallyViewState.h = finallyViewState.components['block-header'].h +
                                    finallyViewState.components['statement-box'].h;

            // calculate left margin from the lifeline centre
            this.calcLeftMargin(finallyViewState, finallyBody.statements);

            nodeHeight += finallyViewState.h;
        }

        if ((catchStmts.length === 0) && finallyBody) {
            // there are no catch statements the try and finally blocks interlock
            // remove additional gap between try and finally

            const finallyViewState = node.viewState.components['finally-block'];
            const widthDiff = finallyViewState.w - viewState.components['statement-box'].w;
            if (widthDiff > 0) {
                // resize catch block component width
                viewState.components['statement-box'].w += widthDiff;
                node.body.viewState.bBox.w += widthDiff;
                viewState.bBox.w += widthDiff;
                viewState.bBox.leftMargin = finallyViewState.leftMargin;
            } else if (widthDiff < 0) {
                // resize finally block component width
                finallyViewState.w += (-widthDiff);
                finallyViewState.components['statement-box'].w += (-widthDiff);
                finallyViewState.leftMargin = viewState.bBox.leftMargin;
            }
        }

        node.viewState.bBox.h = nodeHeight;
        node.viewState.bBox.w = nodeWidth;
    }

    /**
     * Calculate dimention of VariableDef nodes.
     *
     * @param {object} node
     *
     */
    sizeVariableDefNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(true), viewState);
        this.adjustToLambdaSize(node, viewState);

        // Truncate the endpoint name to fit the statement box
        if (TreeUtil.isEndpointTypeVariableDef(node)) {
            const endpointWdth = 90;
            viewState.endpointIdentifier = this.getTextWidth(node.getVariable().getName().value, 0, endpointWdth).text;
        }
        this.sizeActionInvocationStatement(node);
        this.sizeClientResponderStatement(node);
    }

    /**
     * Size statements containing action invocation statements
     * @param {node} node node to size
     */
    sizeActionInvocationStatement(node) {
        // This function gets called by statements containing action invocation expressions
        if (TreeUtil.statementIsInvocation(node)) {
            const viewState = node.viewState;
            viewState.bBox.h = this.config.actionInvocationStatement.height;
            viewState.components['statement-box'].h = this.config.actionInvocationStatement.height;
            viewState.alias = 'InvocationNode';
        }
    }

    /**
     * Size statements containing client responding actions
     * @param {node} node node to size
     */
    sizeClientResponderStatement(node) {
        // This function gets called by statements that perform client responding
        if (TreeUtil.statementIsClientResponder(node)) {
            const viewState = node.viewState;
            viewState.bBox.w = this.config.actionInvocationStatement.width;
            viewState.components['statement-box'].w = this.config.actionInvocationStatement.width;
            viewState.alias = 'ClientResponderNode';
            if (TreeUtil.isReturn(node)) {
                const paramText = node.expression.getSource(true);
                const displayText = this.getTextWidth(paramText, 0,
                    (this.config.clientLine.width + this.config.lifeLine.gutter.h));
                viewState.displayText = displayText.text;
            }
            if (TreeUtil.isAssignment(node)) {
                const exp = node.getExpression();
                const argExpSource = exp.getArgumentExpressions().map((arg) => {
                    return arg.getSource();
                }).join(', ');
                const displayText = this.getTextWidth(argExpSource, 0,
                    (this.config.clientLine.width + this.config.lifeLine.gutter.h));
                viewState.displayText = displayText.text;
            }
            if (TreeUtil.isVariableDef(node)) {
                const exp = node.variable.getInitialExpression();
                const argExpSource = exp.getArgumentExpressions().map((arg) => {
                    return arg.getSource();
                }).join(', ');
                const displayText = this.getTextWidth(argExpSource, 0,
                    (this.config.clientLine.width + this.config.lifeLine.gutter.h));
                viewState.displayText = displayText.text;
            }
            if (TreeUtil.isExpressionStatement(node)) {
                const exp = node.getExpression();
                const argExpSource = exp.getArgumentExpressions().map((arg) => {
                    return arg.getSource();
                }).join(', ');
                const displayText = this.getTextWidth(argExpSource, 0,
                    (this.config.clientLine.width + this.config.lifeLine.gutter.h));
                viewState.displayText = displayText.text;
            }
        }
    }


    /**
     * Calculate dimention of While nodes.
     *
     * @param {object} node
     *
     */
    sizeWhileNode(node) {
        const expression = (TreeUtil.isWhile(node)) ? node.getCondition() : node.getCollection();
        const viewState = node.viewState;
        const components = viewState.components;
        const dropZoneHeight = TreeUtil.isBlock(node.parent) ? this.config.statement.gutter.v : 0;
        const nodeBodyViewState = node.body.viewState;

        // flow chart while width and height is different to normal block node width and height
        nodeBodyViewState.bBox.w = (nodeBodyViewState.bBox.w < this.config.flowChartControlStatement.width) ?
                                    this.config.flowChartControlStatement.width : nodeBodyViewState.bBox.w;
        nodeBodyViewState.bBox.h += this.config.statement.gutter.v;

        components.body = new SimpleBBox();

        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['statement-box'] = new SimpleBBox();
        // Set the block header as an opaque box to prevent conflicts with arrows.
        components['block-header'] = new SimpleBBox();
        viewState.components.text = new SimpleBBox();

        const bodyWidth = nodeBodyViewState.bBox.w;
        const bodyHeight = nodeBodyViewState.bBox.h;

        components['block-header'].h = this.config.flowChartControlStatement.heading.height
                                        + this.config.flowChartControlStatement.padding.top
                                        + this.config.flowChartControlStatement.heading.gap;

        viewState.components['drop-zone'].h = dropZoneHeight + (viewState.offSet || 0);
        viewState.components['drop-zone'].w = bodyWidth;
        viewState.components['statement-box'].h = bodyHeight;
        viewState.components['statement-box'].w = bodyWidth;
        viewState.bBox.h = viewState.components['statement-box'].h
                            + viewState.components['drop-zone'].h
                            + this.config.flowChartControlStatement.gutter.h // for the lower separator line
                            + viewState.components['block-header'].h;
        viewState.bBox.w = bodyWidth;

        // calculate left margin from the lifeline centre
        this.calcLeftMargin(viewState.bBox, node.body.statements, true);

        components['block-header'].setOpaque(true);

        // for compound statement like if , while we need to render condition expression
        // we will calculate the width of the expression and adjust the block statement
        if (expression) {
            components.expression = this.getTextWidth(expression.getSource(true), 0,
                                        this.config.flowChartControlStatement.heading.width - 5);
        }
    }

    /**
     * Calculate left margin total for the given nodes.
     * The nodes should be immediate children of a block node. Nested calculations are handled by each of
     * the childrens in their respective calculations.
     * At the moment, left margin is only needed by while node.
     * @param {*} nodes nodes to calculate left margin from
     */
    calcLeftMargin(bBox, children, acumilate = true, setDefault = true) {
        let leftMargin = 0;
        children.forEach((child) => {
            if (child.viewState.bBox.leftMargin > leftMargin) {
                leftMargin = child.viewState.bBox.leftMargin;
            }
        });
        const defaultGap = (setDefault) ? this.config.flowChartControlStatement.gap.left : 0;
        bBox.leftMargin = (leftMargin === 0) ? defaultGap :
            (leftMargin + ((acumilate) ? this.config.flowChartControlStatement.padding.left : 0));
    }

    /**
     * Calculate dimention of WorkerReceive nodes.
     *
     * @param {object} node
     *
     */
    sizeWorkerReceiveNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(true), viewState);
    }


    /**
     * Calculate dimention of WorkerSend nodes.
     *
     * @param {object} node
     *
     */
    sizeWorkerSendNode(node) {
        const viewState = node.viewState;
        this.sizeStatement(node.getSource(true), viewState);
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
        let nodeBodyViewState;
        if (TreeUtil.isBlock(node)) {
            nodeBodyViewState = node.viewState;
        } else if (TreeUtil.isTransaction(node)) {
            nodeBodyViewState = node.transactionBody.viewState;
        } else if (TreeUtil.isForkJoin(node)) {
            nodeBodyViewState = node.viewState;
        } else {
            nodeBodyViewState = node.body.viewState;
        }

        components.body = new SimpleBBox();

        viewState.components['drop-zone'] = new SimpleBBox();
        viewState.components['statement-box'] = new SimpleBBox();
        // Set the block header as an opaque box to prevent conflicts with arrows.
        components['block-header'] = new SimpleBBox();
        viewState.components.text = new SimpleBBox();

        const bodyWidth = nodeBodyViewState.bBox.w;
        const bodyHeight = nodeBodyViewState.bBox.h;

        components['block-header'].h = this.config.compoundStatement.heading.height
                                        + this.config.compoundStatement.heading.gap;

        viewState.components['drop-zone'].h = dropZoneHeight + (viewState.offSet || 0);
        viewState.components['drop-zone'].w = bodyWidth;
        viewState.components['statement-box'].h = bodyHeight + components['block-header'].h;
        viewState.components['statement-box'].w = bodyWidth;
        viewState.bBox.h = viewState.components['statement-box'].h + viewState.components['drop-zone'].h
                            + this.config.statement.gutter.h;
        viewState.bBox.w = bodyWidth;
        components.body.w = bodyWidth;

        components['block-header'].setOpaque(true);

        // for compound statement like if , while we need to render condition expression
        // we will calculate the width of the expression and adjust the block statement
        if (expression) {
            // see how much space we have to draw the condition
            const available = bodyWidth - this.config.compoundStatement.heading.width - 10;
            components.expression = this.getTextWidth(expression.getSource(true), 0, available);
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
        // TODO: implement annotations for new design view
        return 0;
    }

    /**
     * Get the max height among the workers
     * @param {Array} workers - array of workers
     * @returns {number} maximum worker height
     */
    getWorkerMaxHeight(workers) {
        const workerNode = _.maxBy(workers, (worker) => {
            return worker.viewState.bBox.h;
        });

        return workerNode.viewState.bBox.h;
    }
}

export default SizingUtil;
