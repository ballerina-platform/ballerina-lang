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

define(
    ['require', 'lodash', 'log', './ballerina-statement-view', 'd3utils', 'd3', 'ballerina/ast/ballerina-ast-factory'],
    function (require, _, log, BallerinaStatementView, D3Utils, d3, BallerinaASTFactory) {

        /**
         * Super view class for all block statements e.g. while, if, else etc.
         * @param args {*} arguments for the creating view
         * @class BlockStatementView
         * @constructor
         * @extends BallerinaStatementView
         */
        var BlockStatementView = function (args) {
            BallerinaStatementView.call(this, args);

            var viewOptions = this.getViewOptions();
            viewOptions.height = _.get(args, "viewOptions.height", 100);
            viewOptions.width = _.get(args, "viewOptions.width", 140); // starting width
            viewOptions.minWidth = _.get(args, "viewOptions.minWidth", 140); // minimum width
            _.set(viewOptions, "title.text", _.get(args, "viewOptions.title.text", "Statement"));
            _.set(viewOptions, "title.width", _.get(args, "viewOptions.title.width", 40));
            _.set(viewOptions, "title.height", _.get(args, "viewOptions.title.height", 25));
            _.set(viewOptions, "padding.left", _.get(args, "viewOptions.padding.left", 7));
            _.set(viewOptions, "padding.right", _.get(args, "viewOptions.padding.right", 7));
            _.set(viewOptions, "padding.top", _.get(args, "viewOptions.padding.top", viewOptions.title.height));
            _.set(viewOptions, "padding.bottom", _.get(args, "viewOptions.padding.bottom", 10));
            _.set(viewOptions, 'contentOffset', _.get(viewOptions, 'contentOffset', {top: 10, bottom: 10}));

            this.getBoundingBox().fromTopCenter(
                this.getTopCenter(), (viewOptions.padding.left + viewOptions.width + viewOptions.padding.right),
                viewOptions.height);
        };

        BlockStatementView.prototype = Object.create(BallerinaStatementView.prototype);

        BlockStatementView.prototype.constructor = BlockStatementView;

        BlockStatementView.prototype.render = function (renderingContext) {
            this.setDiagramRenderingContext(renderingContext);
            var bBox = this.getBoundingBox();
            var model = this.getModel();

            // Creating statement group.
            var statementGroup = D3Utils.group(d3.select(this.getContainer()));
            // "id" is prepend with a "_" to be compatible with HTML4
            statementGroup.attr("id", "_" + model.id);

            var outerRect = D3Utils.rect(bBox.x(), bBox.y(), bBox.w(), bBox.h(), 0, 0, statementGroup)
                                   .classed('background-empty-rect', true);
            statementGroup.outerRect = outerRect;
            // Creating title.
            var titleViewOptions = this.getViewOptions().title;
            var titleGroup = D3Utils.group(statementGroup);
            statementGroup.title = titleGroup;
            var titleRect = D3Utils.rect(bBox.x(), bBox.y(), bBox.w(), 25, 0, 0, statementGroup)
                                   .classed('statement-title-rect', true);
            this._titleRect = titleRect;
            var titleText = D3Utils.textElement(bBox.x() + 20, bBox.y() + 12, titleViewOptions.text, statementGroup)
                                   .classed('statement-text', true);
            var titleTextWrapperPolyline = D3Utils.polyline(getTitlePolyLinePoints(bBox, titleViewOptions),
                                                            statementGroup)
                                                  .classed('statement-title-polyline', true);
            titleGroup.titleRect = titleRect;
            titleGroup.titleText = titleText;
            titleGroup.titleTextWrapperPolyline = titleTextWrapperPolyline;
            this.setStatementGroup(statementGroup);

            // Registering event listeners.
            bBox.on('moved', function (offset) {
                outerRect.attr("x", parseFloat(outerRect.attr('x')) + offset.dx);
                outerRect.attr("y", parseFloat(outerRect.attr('y')) + offset.dy);

                titleRect.attr("x", parseFloat(titleRect.attr('x')) + offset.dx);
                titleRect.attr("y", parseFloat(titleRect.attr('y')) + offset.dy);

                titleText.attr("y", parseFloat(titleText.attr('y')) + offset.dy);
                titleText.attr("x", parseFloat(titleText.attr('x')) + offset.dx);
                titleTextWrapperPolyline.attr("points", getTitlePolyLinePoints(bBox, titleViewOptions));
            });
            bBox.on('width-changed', function (dw) {
                outerRect.attr("width", bBox.w());
                titleRect.attr("width", bBox.w());
            });
            bBox.on('height-changed', function (dh) {
                outerRect.attr("height", bBox.h());
            });

            this.renderStatementContainer();
        };

        BlockStatementView.prototype.renderStatementContainer = function () {
            var viewOptions = this.getViewOptions();
            var model = this.getModel();
            var boundingBox = this.getBoundingBox();
            var topCenter = this.getTopCenter();

            // Creating view options for the new statement container.
            var statementContainerOpts = {};
            _.set(statementContainerOpts, 'model', model);
            _.set(statementContainerOpts, 'topCenter', topCenter.clone().move(0, viewOptions.padding.top));
            _.set(statementContainerOpts, 'bottomCenter', topCenter.clone().move(0, viewOptions.height));
            _.set(statementContainerOpts, 'width', boundingBox.w());
            _.set(statementContainerOpts, 'minWidth', statementContainerOpts.width);
            _.set(statementContainerOpts, 'height', (boundingBox.h() - viewOptions.padding.top));
            _.set(statementContainerOpts, 'minHeight', statementContainerOpts.height);
            _.set(statementContainerOpts, 'padding.left', viewOptions.padding.left);
            _.set(statementContainerOpts, 'padding.right', viewOptions.padding.right);
            _.set(statementContainerOpts, 'offset', {top: viewOptions.title.height, bottom: 30});
            _.set(statementContainerOpts, 'parent', this);
            _.set(statementContainerOpts, 'container', this.getStatementGroup().node());
            _.set(statementContainerOpts, 'toolPalette', this.getToolPalette());

            // Creating new statement container.
            var StatementContainer = require('./statement-container');
            var statementContainer = new StatementContainer(statementContainerOpts);
            this.setStatementContainer(statementContainer);

            // Registering event handlers.
            this.listenTo(statementContainer.getBoundingBox(), 'height-changed', function (dh) {
                boundingBox.h(boundingBox.h() + dh);
            });
            boundingBox.on('top-edge-moved', function (dy) {
                statementContainer.isOnWholeContainerMove = true;
                statementContainer.getBoundingBox().y(statementContainer.getBoundingBox().y() + dy);
            }, this);
            this.listenTo(statementContainer.getBoundingBox(), 'width-changed', function (dw) {
                boundingBox.zoomWidth(Math.max((boundingBox.w() + dw), viewOptions.minWidth));
            });

            statementContainer.render(this.getDiagramRenderingContext());

            /* Removing all the registered 'child-added' event listeners for this model. This is needed because we
             are not un-registering registered event while the diagram element deletion. Due to that, sometimes we
             are having two or more view elements listening to the 'child-added' event of same model. */
            model.off('child-added');
            model.on('child-added', function (child) {
                this.visit(child);
            }, this);
            model.accept(this);
        };

        //TODO : rename as visitStatement to avoid conflicts with generic visit
        BlockStatementView.prototype.visit = function (statement) {
            var args = {
                model: statement,
                container: this.getStatementGroup().node(),
                viewOptions: {},
                toolPalette: this.getToolPalette(),
                messageManager: this.messageManager,
                parent: this
            };
            this.getStatementContainer().renderStatement(statement, args);
        };

        BlockStatementView.prototype.setModel = function (model) {
            if (_.isNil(model)) {
                var message = "Model of a block statement cannot be null.";
                log.error(message);
                throw new Error(message);
            } else {
                this._model = model;
            }
        };

        BlockStatementView.prototype.getModel = function () {
            return this._model;
        };

        BlockStatementView.prototype.setContainer = function (container) {
            if (_.isNil(container)) {
                var message = "Container of a block statement cannot be null.";
                log.error(message);
                throw new Error(message);
            } else {
                this._container = container;
            }
        };

        BlockStatementView.prototype.getContainer = function () {
            return this._container;
        };

        BlockStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        BlockStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        BlockStatementView.prototype.setStatementContainer = function (statementContainer) {
            this._statementContainer = statementContainer;
        };

        BlockStatementView.prototype.getStatementContainer = function () {
            return this._statementContainer;
        };

        BlockStatementView.prototype.getToolPalette = function () {
            return this.toolPalette;
        };

        BlockStatementView.prototype.showDebugHit = function () {
            this._titleRect.classed('highlight-statement', true);
        };

        BlockStatementView.prototype.clearDebugHit = function () {
            this._titleRect.classed('highlight-statement', false);
        };


        /**
         * Overrides the child remove callback from BallerinaStatementView.
         * @param child {ASTNode} removed child
         */
        BlockStatementView.prototype.childRemovedCallback = function (child) {
            if (BallerinaASTFactory.isStatement(child)) {
                this.getStatementContainer().childStatementRemovedCallback(child);
            }
        };

        /**
         * Returns the points for the title's poly line.
         * @param boundingBox {BBox} bounding box
         * @param titleViewOptions {{width: number, height: number}} title's view options
         * @return {string} pints of thr poly line
         */
        function getTitlePolyLinePoints(boundingBox, titleViewOptions) {
            var x = boundingBox.x(), y = boundingBox.y();
            var titleWidth = titleViewOptions.width, titleHeight = titleViewOptions.height;
            var offset = 10;
            return "" + x + "," + (y + titleHeight) + " " +
                   (x + titleWidth) + "," + (y + titleHeight) + " " +
                   (x + titleWidth + offset) + "," + y;
        }

        return BlockStatementView;
    });
