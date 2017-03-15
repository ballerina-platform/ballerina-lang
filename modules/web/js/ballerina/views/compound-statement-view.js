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
    ['require', 'lodash', 'log', './ballerina-statement-view', 'd3utils', 'd3', './point'],
    function (require, _, log, BallerinaStatementView, D3Utils, d3, Point) {

        /**
         * Super view class for all compound statements e.g. if-else, try-catch etc.
         * @param args {*} arguments for the creating view
         * @class CompoundStatementView
         * @constructor
         * @extends BallerinaStatementView
         */
        var CompoundStatementView = function (args) {
            BallerinaStatementView.call(this, args);

            /**
             * Real width of the child statements of this compound statement.
             * @type {number[]}
             * @private
             */
            this._childrenViewsActualWidths = [];

            var viewOptions = this.getViewOptions();
            viewOptions.width = _.get(args, "viewOptions.width", 120);
            viewOptions.height = _.get(args, "viewOptions.height", 0);
            this._childStatementDefaultWidth = 120;
            this._onWholeContainerMove = false;

            this.getBoundingBox().fromTopCenter(this.getTopCenter(), viewOptions.width, viewOptions.height);
        };

        CompoundStatementView.prototype = Object.create(BallerinaStatementView.prototype);

        CompoundStatementView.prototype.constructor = CompoundStatementView;

        CompoundStatementView.prototype.render = function (renderingContext) {
            this.setDiagramRenderingContext(renderingContext);
            var bBox = this.getBoundingBox();
            var model = this.getModel();
            var parentStatementContainerBBox = this.getParent().getStatementContainer().getBoundingBox();

            // Creating statement group.
            var statementGroup = D3Utils.group(d3.select(this.getContainer()));
            // "id" is prepend with a "_" to be compatible with HTML4
            statementGroup.attr("id", "_" + model.id);
            this.setStatementGroup(statementGroup);

            this.listenTo(bBox, 'width-changed', function (dw) {
                if (parentStatementContainerBBox.w() > bBox.w()) {
                    bBox.x(parentStatementContainerBBox.x() + (parentStatementContainerBBox.w() - bBox.w())/2);
                }
            });

            this.getDiagramRenderingContext().setViewOfModel(this.getModel(), this);
            model.accept(this);
        };

        /**
         *
         * @param childStatement
         * @return {BlockStatementView}
         */
        CompoundStatementView.prototype.visitChildStatement = function (childStatement) {
            var boundingBox = this.getBoundingBox();
            var topCenter = this.getTopCenter();
            var renderingContext = this.getDiagramRenderingContext();
            /** @type {BlockStatementView[]} */
            var childStatementViews = this.getChildrenViewsList();
            var childrenViewsActualWidths = this._childrenViewsActualWidths;
            var self = this;

            // Creating child statement view.
            var childStatementViewTopCenter;
            if (_.isEmpty(childStatementViews)) {
                childStatementViewTopCenter = new Point(topCenter.x(), topCenter.y());
            } else {
                childStatementViewTopCenter =
                    new Point(topCenter.x(), _.last(childStatementViews).getBoundingBox().getBottom());
            }
            var childStatementViewArgs = {
                model: childStatement,
                container: this.getStatementGroup().node(),
                viewOptions: {'width' : boundingBox.w()},
                parent: this,
                topCenter: childStatementViewTopCenter,
                messageManager: this.messageManager,
                toolPalette: this.getToolPalette(),
                isChildOfWorker: childStatement.parent._isChildOfWorker
            };
            var StatementViewFactory = require('./statement-view-factory');
            var statementViewFactory = new StatementViewFactory();
            /** @type {BlockStatementView} */
            var childStatementView = statementViewFactory.getStatementView(childStatementViewArgs);

            childStatementView.listenTo(boundingBox, 'left-edge-moved', function (offset) {
                childStatementView.getBoundingBox().x(boundingBox.x());
            });

            // If there are previously added child statements, then get the last one.
            var lastChildStatementView = _.last(childStatementViews);
            if (!_.isUndefined(lastChildStatementView)) {
                // When the last child statement's height change, adding child statement should move accordingly.
                childStatementView.listenTo(lastChildStatementView.getBoundingBox(), 'bottom-edge-moved', function (offset) {
                    if (!self._onWholeContainerMove) {
                        childStatementView.getBoundingBox().move(0, offset);
                    }
                });
            }

            this.listenTo(childStatementView.getBoundingBox(), 'height-changed', function (dh) {
                boundingBox.h(boundingBox.h() + dh);
            });

            this.listenTo(childStatementView.getBoundingBox(), 'width-changed', function (dw) {
                if (!childStatementView.onForcedWidthChanged()) {
                    var widestChildOfChildBlock = self.getWidestChildOfChildBlock();
                    var newWidth = !_.isNil(widestChildOfChildBlock) ? widestChildOfChildBlock.getBoundingBox().w() +
                        childStatementView.getStatementContainer().getWidthGap() : self._childStatementDefaultWidth;
                    if (newWidth !== self.getBoundingBox().w()) {
                        _.forEach(childStatementViews, function (childView) {
                            childView.onForcedWidthChanged(true);
                            childView.getStatementContainer().getBoundingBox().w(newWidth);
                        });
                        self.getBoundingBox().w(newWidth);
                    } else {
                        childStatementView.onForcedWidthChanged(true);
                        childStatementView.getStatementContainer().getBoundingBox().w(newWidth);
                    }
                } else {
                    childStatementView.onForcedWidthChanged(false);
                }
            });

            childStatementViews.push(childStatementView);
            childStatementView.render(renderingContext);

            childrenViewsActualWidths.push(childStatementView.getBoundingBox().w());
            renderingContext.getViewModelMap()[childStatement.id] = childStatementView;

            if (childStatementViews.length === 1) {
                // This is the very first child statement.
                boundingBox.h(childStatementView.getBoundingBox().h());
            } else {
                boundingBox.h(boundingBox.h() + childStatementView.getBoundingBox().h());
            }

            childStatementView.listenTo(boundingBox, 'moved', function (offset) {
                self._onWholeContainerMove = true;
                childStatementView.getBoundingBox().move(offset.dx, offset.dy);
                self._onWholeContainerMove = false;
            });

            return childStatementView;
        };

        CompoundStatementView.prototype.setModel = function (model) {
            if (_.isNil(model)) {
                var message = "Model of a compound statement cannot be null.";
                log.error(message);
                throw new Error(message);
            } else {
                this._model = model;
            }
        };

        CompoundStatementView.prototype.getModel = function () {
            return this._model;
        };

        CompoundStatementView.prototype.setContainer = function (container) {
            if (_.isNil(container)) {
                var message = "Container of a compound statement cannot be null.";
                log.error(message);
                throw new Error(message);
            } else {
                this._container = container;
            }
        };

        CompoundStatementView.prototype.getContainer = function () {
            return this._container;
        };

        CompoundStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        CompoundStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        CompoundStatementView.prototype.setStatementContainer = function (statementContainer) {
            this._statementContainer = statementContainer;
        };

        CompoundStatementView.prototype.getStatementContainer = function () {
            return this._statementContainer;
        };

        CompoundStatementView.prototype.getToolPalette = function () {
            return this.toolPalette;
        };

        CompoundStatementView.prototype.removeAllChildStatements = function () {
            _.forEach(this.getChildrenViewsList(), function (childStatementView) {
                childStatementView.stopListening();
            });
            this.getStatementGroup().node().remove();
            // resize the bounding box in order to the other objects to resize
            var gap = this.getParent().getStatementContainer().getInnerDropZoneHeight();
            this.getBoundingBox().move(0, -this.getBoundingBox().h() - gap).w(0);
        };

        CompoundStatementView.prototype.showDebugHit = function () {
            this.getChildrenViewsList()[0]._titleRect.classed('highlight-statement', true)
        };

        CompoundStatementView.prototype.clearDebugHit = function () {
            this.getChildrenViewsList()[0]._titleRect.classed('highlight-statement', false)
        };

        /**
         * Get the widest child of a child block
         * @return {BallerinaStatementView}
         */
        CompoundStatementView.prototype.getWidestChildOfChildBlock = function () {
            var childrenViews = this.getChildrenViewsList();
            var blockStatementWidestChildren = [];
            _.forEach(childrenViews, function (child) {
                blockStatementWidestChildren.push(child.getStatementContainer().getWidestStatementView());
            });
            var sortedChildren = _.sortBy(blockStatementWidestChildren, function (child) {
                if (_.isNil(child)) {
                    return -1;
                } else {
                    return child.getBoundingBox().w();
                }
            });

            return _.last(sortedChildren);
        };

        return CompoundStatementView;
    });
