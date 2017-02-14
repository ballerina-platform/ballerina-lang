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
         * Compound statement contains one or more block statements as its children.
         * @param args {*} arguments for the creating view
         * @class CompoundStatementView
         * @constructor
         * @extends BallerinaStatementView
         * @see {@link BlockStatementView}
         */
        var CompoundStatementView = function (args) {
            BallerinaStatementView.call(this, args);

            var viewOptions = this.getViewOptions();
            viewOptions.width = _.get(args, "viewOptions.width", 140);
            viewOptions.height = _.get(args, "viewOptions.height", 0);

            this.getBoundingBox().fromTopCenter(this.getTopCenter(), viewOptions.width, viewOptions.height);
        };

        CompoundStatementView.prototype = Object.create(BallerinaStatementView.prototype);

        CompoundStatementView.prototype.constructor = CompoundStatementView;

        CompoundStatementView.prototype.render = function (renderingContext) {
            this.setDiagramRenderingContext(renderingContext);
            var bBox = this.getBoundingBox();
            var model = this.getModel();

            // Creating statement group.
            var statementGroup = D3Utils.group(d3.select(this.getContainer()));
            // "id" is prepend with a "_" to be compatible with HTML4
            statementGroup.attr("id", "_" + model.id);
            this.setStatementGroup(statementGroup);

            /* If the top-edge-moved event triggered we only need to move the first child statement view. Because other
             child statements are listening to it's previous sibling and accordingly move. */
            bBox.on('top-edge-moved', function (offset) {
                var firstChildStatementView = this._childrenViewsList[0];
                if (!_.isUndefined(firstChildStatementView)) {
                    this._pendingContainerMove = true;
                    firstChildStatementView.getBoundingBox().move(0, offset, false);
                }
            }, this);

            model.accept(this);
        };

        /**
         * Renders the specified child statement of this compound statement.
         * @param childStatement {Statement} child statement to be rendered
         * @return {BlockStatementView} statement view of he child statement
         */
        CompoundStatementView.prototype.renderChildStatement = function (childStatement) {
            var boundingBox = this.getBoundingBox();
            var topCenter = this.getTopCenter();
            var renderingContext = this.getDiagramRenderingContext();
            var childStatementViews = this.getChildrenViewsList();

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
                viewOptions: {},
                parent: this,
                topCenter: childStatementViewTopCenter,
                messageManager: this.messageManager,
                toolPalette: this.getToolPalette()
            };
            var StatementViewFactory = require('./statement-view-factory');
            var statementViewFactory = new StatementViewFactory();
            /** @type BlockStatementView */
            var childStatementView = statementViewFactory.getStatementView(childStatementViewArgs);

            // If there are previously added child statements, then get the last one.
            var lastChildStatementView = _.last(childStatementViews);
            if (!_.isUndefined(lastChildStatementView)) {
                // When the last child statement's height change, adding child statement should move accordingly.
                lastChildStatementView.getBoundingBox().on('bottom-edge-moved', function (offset) {
                    childStatementView.getBoundingBox().move(0, offset, false);
                });
                this.stopListening(lastChildStatementView.getBoundingBox(), 'bottom-edge-moved');
            }

            childStatementViews.push(childStatementView);
            renderingContext.getViewModelMap()[childStatement.id] = childStatementView;

            childStatementView.render(renderingContext);

            var childStatementViewBBox = childStatementView.getBoundingBox();
            boundingBox.x(Math.min(boundingBox.x(), childStatementViewBBox.x()))
                       .w(Math.max(boundingBox.w(), childStatementViewBBox.w()))
                       .h(boundingBox.h() + childStatementViewBBox.h());

            // When adding child statement's height changes, height of the bounding box should change accordingly.
            this.listenTo(childStatementView.getBoundingBox(), 'bottom-edge-moved', function (dy) {
                if (!this._pendingContainerMove) {
                    this.getBoundingBox().h(this.getBoundingBox().h() + dy);
                } else {
                    this._pendingContainerMove = false;
                }
            });
            this.listenTo(childStatementView.getBoundingBox(), 'width-changed', function (dw) {
                var widestChildStatementView = _.maxBy(this.getChildrenViewsList(), function (statementView) {
                    return statementView.getStatementContainer().getBoundingBox().w();
                }.bind(this));
                this.getBoundingBox().zoomWidth(widestChildStatementView.getBoundingBox().w());
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

        CompoundStatementView.prototype.onBeforeModelRemove = function () {
            _.forEach(this.getChildrenViewsList(), function (childStatementView) {
                childStatementView.stopListening();
            });
            this.getStatementGroup().node().remove();
            this.getBoundingBox().w(0).h(0);
        };

        return CompoundStatementView;
    });
