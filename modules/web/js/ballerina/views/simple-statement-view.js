/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import log from 'log';
import BallerinaStatementView from './ballerina-statement-view';
import D3Utils from 'd3utils';
import * as d3 from 'd3';

/**
 * Super view class for all simple statements e.g. assignment, variable definition, functional invocation etc.
 * @param args {*} arguments for the creating view
 * @class SimpleStatementView
 * @constructor
 * @extends BallerinaStatementView
 */
class SimpleStatementView extends BallerinaStatementView {
    constructor(args) {
        super(args);

        var viewOptions = this.getViewOptions();
        viewOptions.height = _.get(args, "viewOptions.height", 30);
        viewOptions.width = _.get(args, "viewOptions.width", 120); // starting width
        viewOptions.minWidth = _.get(args, "viewOptions.minWidth", 120); // minimum width
        viewOptions.maxWidth = _.get(args, "viewOptions.maxWidth", 300); // maximum width
        viewOptions.textPadding = _.get(args, "viewOptions.textPadding", {left: 5, right: 5, top: 0, bottom: 0});

        this.getBoundingBox().fromTopCenter(this.getTopCenter(), viewOptions.width, viewOptions.height);
        this._svgRect = undefined;
        this._svgText = undefined
    }

    render(renderingContext) {
        this.setDiagramRenderingContext(renderingContext);
        var bBox = this.getBoundingBox();
        var self = this;

        // Creating statement group.
        var statementGroup = D3Utils.group(d3.select(this._container));
        // "id" is prepend with a "_" to be compatible with HTML4
        statementGroup.attr("id", "_" + this.getModel().id);
        this._svgRect = D3Utils.rect(bBox.getLeft(), bBox.getTop(), bBox.w(), bBox.h(), 0, 0, statementGroup)
                             .classed('statement-rect', true);
        this._svgText = D3Utils.textElement(bBox.getCenterX(), bBox.getCenterY(), "", statementGroup)
                             .classed('statement-text', true);
        statementGroup.outerRectElement = this._svgRect;
        statementGroup.displayTextElement = this._svgText;
        this.setStatementGroup(statementGroup);

        // Registering event listeners.
        this.listenTo(bBox, 'top-edge-moved', function (dy) {
            self.onTopEdgeMovedTrigger(dy);
        });
        bBox.on('width-changed', function (dw) {
            self._svgRect.attr('width', parseFloat(self._svgRect.attr('width')) + dw);
            self._svgText.attr('x', parseFloat(self._svgText.attr('x')) + dw/2);
        });
        bBox.on('left-edge-moved', function (dx) {
            self._svgRect.attr('x', parseFloat(self._svgRect.attr('x')) + dx);
            self._svgText.attr('x', parseFloat(self._svgText.attr('x')) + dx);
        });
        this.getDiagramRenderingContext().setViewOfModel(this.getModel(), this);
    }

    /**
     * Renders the display text of this simple statement.
     * @param displayText {string} text to be rendered
     */
    renderDisplayText(displayText) {
        var boundingBox = this.getBoundingBox();
        var viewOptions = this.getViewOptions();
        var parentStatementContainerBBox = this.getParent().getStatementContainer().getBoundingBox();
        var minWidth = viewOptions.minWidth, maxWidth = viewOptions.maxWidth;
        var leftTextPadding = viewOptions.textPadding.left, rightTextPadding = viewOptions.textPadding.right;
        var textElement = this.getStatementGroup().displayTextElement.node();
        textElement.textContent = displayText;

        /*
         +------------------------------------------------------------+
         |<--leftTextPadding--><--displayText--><--rightTextPadding-->|
         +------------------------------------------------------------+
         */
        var displayTextWidth = leftTextPadding + textElement.getComputedTextLength() + rightTextPadding;
        if (displayTextWidth < minWidth) {
            // Text hasn't exceeded the minimum width of the bounding box. Hence no need to increase width.
            boundingBox.w(minWidth);
        } else {
            if (displayTextWidth < maxWidth) {
                // We can safely expand width as the text length is still less than maximum width of the box.
                boundingBox.w(displayTextWidth);
            } else {
                // We need to truncate displayText and show an ellipses at the end.
                var ellipses = "...";
                var possibleCharactersCount = 0;
                for (var i = (displayText.length - 1); 1 < i; i--) {
                    if ((leftTextPadding + textElement.getSubStringLength(0, i) + rightTextPadding) < maxWidth) {
                        possibleCharactersCount = i;
                        break;
                    }
                }
                // We need room for the ellipses as well, hence removing 'ellipses.length' no. of characters.
                textElement.textContent = displayText.substring(0, (possibleCharactersCount - ellipses.length))
                                          + ellipses; // Appending ellipses.
                boundingBox.w(maxWidth);
            }
        }
        if (parentStatementContainerBBox.w() > boundingBox.w()) {
            boundingBox.x(parentStatementContainerBBox.x() + (parentStatementContainerBBox.w() - boundingBox.w())/2);
        }
    }

    setModel(model) {
        if (_.isNil(model)) {
            var message = "Model of a simple statement cannot be null.";
            log.error(message);
            throw new Error(message);
        } else {
            this._model = model;
        }
    }

    getModel() {
        return this._model;
    }

    setContainer(container) {
        if (_.isNil(container)) {
            var message = "Container of a simple statement cannot be null.";
            log.error(message);
            throw new Error(message);
        } else {
            this._container = container;
        }
    }

    getContainer() {
        return this._container;
    }

    setViewOptions(viewOptions) {
        this._viewOptions = viewOptions;
    }

    getViewOptions() {
        return this._viewOptions;
    }

    showDebugHit() {
        this.getSvgRect().classed('highlight-statement', true);
    }

    clearDebugHit() {
        this.getSvgRect().classed('highlight-statement', false);
    }

    /**
     * When the top edge move event triggered
     * @param {number} dy - delta y distance
     */
    onTopEdgeMovedTrigger(dy) {
        this._svgRect.attr('y', parseFloat(this._svgRect.attr('y')) + dy);
        this._svgText.attr('y', parseFloat(this._svgText.attr('y')) + dy);
    }

    getSvgRect() {
        return this._svgRect;
    }

    getSvgText() {
        return this._svgText;
    }

    /**
     * Remove statement view callback
     * @param {ASTNode} parent - Parent model
     * @param {ASTNode} child - child model
     */
    onBeforeModelRemove() {
        d3.select("#_" +this.getModel().getID()).remove();
        // resize the bounding box in order to the other objects to resize
        var gap = this.getParent().getStatementContainer().getInnerDropZoneHeight();
        this.getBoundingBox().move(0, -this.getBoundingBox().h() - gap).w(0);
    }
}

export default SimpleStatementView;
