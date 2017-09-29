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

    setContainerSize(nodes, viewState, width = 0, height = 0) {
        let stH = 0;
        nodes.forEach((element) => {
            if (element.viewState.bBox.w > width) {
                width = element.viewState.bBox.w;
            }
            stH = element.viewState.bBox.h;
        });
        if (stH > height) {
            height = stH;
        }
        viewState.bBox.w = width;
        viewState.bBox.h = height;
    }

    /**
     * Calculate dimention of Action nodes.
     *
     * @param {object} node
     *
     */
    sizeActionNode(node) {
        // Not implemented.
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
     *
     */
    sizeAnnotationAttributeNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Catch nodes.
     *
     * @param {object} node
     *
     */
    sizeCatchNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of CompilationUnit nodes.
     *
     * @param {object} node
     *
     */
    sizeCompilationUnitNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Connector nodes.
     *
     * @param {object} node
     *
     */
    sizeConnectorNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Enum nodes.
     *
     * @param {object} node
     *
     */
    sizeEnumNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Function nodes.
     *
     * @param {object} node
     *
     */
    sizeFunctionNode(node) {
        const viewState = node.viewState;
        const cmp = viewState.components;

        /* Define the sub components */
        cmp.heading = new SimpleBBox();
        cmp.statementContainer = new SimpleBBox();
        cmp.defaultWorker = new SimpleBBox();
        cmp.body = new SimpleBBox();
        cmp.argParameters = new SimpleBBox();
        cmp.returnParameters = new SimpleBBox();
        cmp.annotation = new SimpleBBox();
        cmp.argParameterHolder = {};
        cmp.returnParameterHolder = {};
        // calculate statement container
        cmp.statementContainer.w = this.config.statement.width;
        cmp.statementContainer.h = this.config.statementContainer.height;
        // calculate defult worker
        cmp.defaultWorker.w = this.config.lifeLine.width;
        cmp.defaultWorker.h = cmp.statementContainer.h + (this.config.lifeLine.head.height * 2);
        // calculate body
        cmp.body.h = cmp.defaultWorker.h + this.config.panel.body.padding.top + this.config.panel.body.padding.bottom;
        // calculate parameters
        cmp.heading.h = this.config.panel.heading.height;
        // calculate annotations

        viewState.bBox.h = cmp.heading.h + cmp.body.h + cmp.annotation.h;

        const textWidth = this.getTextWidth(node.getName().value);
        viewState.titleWidth = textWidth.w + this.config.panel.heading.title.margin.right
            + this.config.panelHeading.iconSize.width;

        cmp.parametersPrefixContainer = {};
        cmp.parametersPrefixContainer.w = this.getTextWidth('Parameters: ').w;

        // Argument parameter definition holder
        for (let i = 0; i < node.getParameters().length; i++) {
            const parameterDefinition = node.getParameters()[i];
            const paramViewState = parameterDefinition.viewState;
            paramViewState.w = this.getTextWidth(parameterDefinition.getSource(), 0).w;
            paramViewState.h = this.config.panelHeading.heading.height - 7;

            // Creating component for delete icon.
            paramViewState.components.deleteIcon = {};
            paramViewState.components.deleteIcon.w = this.config.panelHeading.heading.height - 7;
            paramViewState.components.deleteIcon.h = this.config.panelHeading.heading.height - 7;
        }

        // Return parameter definition holder
        for (let i = 0; i < node.getReturnParameters().length; i++) {
            const parameterDefinition = node.getReturnParameters()[i];
            const paramViewState = parameterDefinition.viewState;

            paramViewState.w = this.getTextWidth(parameterDefinition.getSource(), 0).w;
            paramViewState.h = this.config.panelHeading.heading.height - 7;

            // Creating component for delete icon.
            paramViewState.components.deleteIcon = {};
            paramViewState.components.deleteIcon.w = this.config.panelHeading.heading.height - 7;
            paramViewState.components.deleteIcon.h = this.config.panelHeading.heading.height - 7;
        }

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
                + this.getParameterTypeWidth(node) + 120;
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
                + this.getReturnTypeWidth(node) + 120;
        }
        cmp.heading.w += viewState.titleWidth + 100;

        // Get the largest among component heading width and component body width.
        const componentWidth = cmp.heading.w > cmp.body.w ? cmp.heading.w : cmp.body.w;

        viewState.bBox.w = componentWidth + (this.config.panel.wrapper.gutter.h * 2);

        // Set the width to the parameter definitions
        if (node.getParameters().length > 0) {
            for (let i = 0; i < node.getParameters().length; i++) {
                const parameter = node.getParameters()[i];
                const viewStateOfParam = parameter.viewState;
                // Set the height
                viewStateOfParam.bBox.h = 20;
                // Set the width
                viewStateOfParam.bBox.w = this.getTextWidth(parameter.getSource(), 0).w + 21;
            }
        }

        // Set the width to the parameter definitions
        if (node.getReturnParameters().length > 0) {
            for (let i = 0; i < node.getReturnParameters().length; i++) {
                const parameter = node.getReturnParameters()[i];
                const viewStateOfParam = parameter.viewState;
                // Set the height
                viewStateOfParam.bBox.h = 20;
                // Set the width
                viewStateOfParam.bBox.w = this.getTextWidth(parameter.getSource(), 0).w + 21;
            }
        }
    }

    /**
     * Calculate Parameters' text width for the node.
     * width - return sum of widths of parameter texts.
     * */
    getParameterTypeWidth(node) {
        let width = 0;
        if (node.getParameters().length > 0) {
            for (let i = 0; i < node.getParameters().length; i++) {
                width += this.getTextWidth(node.getParameters()[i].getSource(), 0).w + 21;
            }
        }

        return width;
    }

    /**
     * Calculate Return Parameters' text width for node.
     *
     * */
    getReturnTypeWidth(node) {
        let width = 0;
        if (node.getReturnParameters().length > 0) {
            for (let i = 0; i < node.getReturnParameters().length; i++) {
                width += this.getTextWidth(node.getReturnParameters()[i].getSource(), 0).w + 21;
            }
        }
        return width;
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

        let height = 0;
        const astRoot = node.parent;

        if (viewState.importsExpanded) {
            const imports = astRoot.filterTopLevelNodes({ kind: 'Import' });

            height += topGutter + topBarHeight + importInputHeight +
                (imports.length * this.config.packageDefinition.importDeclaration.itemHeight);
        }

        if (viewState.globalsExpanded) {
            const globals = astRoot.filterTopLevelNodes({ kind: 'Variable' });

            height += topGutter + topBarHeight + importInputHeight +
                (globals.length * this.config.packageDefinition.importDeclaration.itemHeight);
        }

        viewState.bBox.h = height;
        viewState.bBox.w = 0;

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

        const imports = node.parent.filterTopLevelNodes({ kind: 'Import' });
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
        // Not implemented.
    }


    /**
     * Calculate dimention of Retry nodes.
     *
     * @param {object} node
     *
     */
    sizeRetryNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Service nodes.
     *
     * @param {object} node
     *
     */
    sizeServiceNode(node) {
        // Not implemented.
    }

    _calculateChildrenDimensions(children = [], components, bBox, collapsed) {
        children.forEach(() => {
            if (!collapsed) {
                bBox.h += this.config.structDefinitionStatement.height;
            }
        });
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
        const totalResourceHeight = 0;
        // Initial statement height include panel heading and panel padding.
        let bodyHeight = this.config.panel.body.padding.top + this.config.panel.body.padding.bottom;
        // Set the width initial value to the padding left and right
        const bodyWidth = this.config.panel.body.padding.left + this.config.panel.body.padding.right;

        let textWidth = this.getTextWidth(name);
        viewState.titleWidth = textWidth.w + this.config.panel.heading.title.margin.right
            + this.config.panelHeading.iconSize.width;
        viewState.trimmedTitle = textWidth.text;
        // There are no connectors as well as resources, since we set the default height
        bodyHeight = this.config.innerPanel.body.height;
        components.heading = new SimpleBBox();
        components.body = new SimpleBBox();
        components.annotation = new SimpleBBox();
        components.variablesPane = new SimpleBBox();
        components.transportLine = new SimpleBBox();
        components.heading.h = this.config.panel.heading.height;
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
            // components.annotation.h = this.getAnnotationHeight(node, 20);
        }

        components.variablesPane.h = 0;
        components.body.w = bodyWidth;
        components.annotation.w = bodyWidth;
        components.transportLine.h = totalResourceHeight;
        viewState.bBox.h = components.heading.h + components.body.h + components.annotation.h;
        viewState.components = components;
        viewState.components.heading.w += viewState.titleWidth + 100;
        viewState.bBox.w = 600 + (this.config.panel.wrapper.gutter.h * 2);
        viewState.bBox.h -= 190;
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
        // Not implemented.
    }


    /**
     * Calculate dimention of Worker nodes.
     *
     * @param {object} node
     *
     */
    sizeWorkerNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Xmlns nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlnsNode(node) {
        // Not implemented.
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
     * Calculate dimention of XmlAttribute nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlAttributeNode(node) {
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
     * Calculate dimention of XmlElementLiteral nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlElementLiteralNode(node) {
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
     * Calculate dimention of XmlQname nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlQnameNode(node) {
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
     * Calculate dimention of XmlTextLiteral nodes.
     *
     * @param {object} node
     *
     */
    sizeXmlTextLiteralNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Abort nodes.
     *
     * @param {object} node
     *
     */
    sizeAbortNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Assignment nodes.
     *
     * @param {object} node
     *
     */
    sizeAssignmentNode(node) {
        // Not implemented.
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
    }


    /**
     * Calculate dimention of Break nodes.
     *
     * @param {object} node
     *
     */
    sizeBreakNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Continue nodes.
     *
     * @param {object} node
     *
     */
    sizeContinueNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of ExpressionStatement nodes.
     *
     * @param {object} node
     *
     */
    sizeExpressionStatementNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of ForkJoin nodes.
     *
     * @param {object} node
     *
     */
    sizeForkJoinNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of If nodes.
     *
     * @param {object} node
     *
     */
    sizeIfNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of Reply nodes.
     *
     * @param {object} node
     *
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
        // Not implemented.
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
        // Not implemented.
    }


    /**
     * Calculate dimention of Transaction nodes.
     *
     * @param {object} node
     *
     */
    sizeTransactionNode(node) {
        // Not implemented.
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
     * Calculate dimention of Try nodes.
     *
     * @param {object} node
     *
     */
    sizeTryNode(node) {
        // Not implemented.
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
    }


    /**
     * Calculate dimention of While nodes.
     *
     * @param {object} node
     *
     */
    sizeWhileNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of WorkerReceive nodes.
     *
     * @param {object} node
     *
     */
    sizeWorkerReceiveNode(node) {
        // Not implemented.
    }


    /**
     * Calculate dimention of WorkerSend nodes.
     *
     * @param {object} node
     *
     */
    sizeWorkerSendNode(node) {
        // Not implemented.
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
     * Calculate dimention of ValueType nodes.
     *
     * @param {object} node
     *
     */
    sizeValueTypeNode(node) {
        // Not implemented.
    }


}

export default SizingUtil;
