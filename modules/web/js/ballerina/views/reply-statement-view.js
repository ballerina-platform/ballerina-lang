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
define(['lodash', 'log', './simple-statement-view', './../ast/reply-statement', 'd3utils', 'd3', './point', './../ast/ballerina-ast-factory'],
    function (_, log, SimpleStatementView, ReplyStatement, D3Utils, d3, Point, BallerinaASTFactory) {

        /**
         * The view to represent a reply statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ReplyStatement} args.model - The reply statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @class ReplyStatementView
         * @constructor
         * @extends SimpleStatementView
         */
        var ReplyStatementView = function (args) {
            SimpleStatementView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof ReplyStatement)) {
                log.error("Return statement definition is undefined or is of different type." + this._model);
                throw "Return statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for return statement is undefined." + this._container);
                throw "Container for return statement is undefined." + this._container;
            }
            this._replyArrow = undefined;
            this._replyLine = undefined;
            this._replyArrowHead = undefined;
            this._distanceToClient = undefined;
        };

        ReplyStatementView.prototype = Object.create(SimpleStatementView.prototype);
        ReplyStatementView.prototype.constructor = ReplyStatementView;

        ReplyStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ReplyStatement) {
                (this.__proto__.__proto__).setModel(model);
            } else {
                log.error("Return statement definition is undefined or is of different type." + model);
                throw "Return statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Rendering the view for reply statement.
         * @returns {group} The svg group which contains the elements of the reply statement view.
         */
        ReplyStatementView.prototype.render = function (diagramRenderingContext) {
            // Calling super class's render function.
            (this.__proto__.__proto__).render.call(this, diagramRenderingContext);
            // Setting display text.
            this.renderDisplayText(this.getModel().getReplyExpression());

            // Drawing arrow.
            var statementGroup = this.getStatementGroup();
            var bBox = this.getBoundingBox();
            var x = bBox.x(), y = bBox.y(), w = bBox.w(), h = bBox.h();
            this._distanceToClient = this.getViewOptions().distanceToClient - (w / 2);

            if (BallerinaASTFactory.isResourceDefinition(this.getModel().getParent())) {
                this._replyArrow = D3Utils.group(statementGroup);
                var replyLineStartPoint = new Point(x, (y + (h / 2)));
                var replyLineEndPoint = new Point((x - this._distanceToClient), (y + (h / 2)));
                this._replyLine = D3Utils.lineFromPoints(replyLineStartPoint, replyLineEndPoint, this._replyArrow)
                    .classed('message', true);
                this._replyArrowHead = D3Utils.outputTriangle(replyLineEndPoint.x(), replyLineEndPoint.y(), this._replyArrow)
                    .classed("action-arrow", true);
                this._replyArrow.arrowLineElement = this._replyLine;
                this._replyArrow.arrowHeadElement = this._replyArrowHead;
            }
            // Creating property pane.
            var model = this.getModel();
            var editableProperty = {
                propertyType: "text",
                key: "Response Message",
                model: model,
                getterMethod: model.getReplyMessage,
                setterMethod: model.setReplyMessage
            };

            this._createDebugIndicator({
                statementGroup: statementGroup
            });

            this._createPropertyPane({
                                         model: model,
                                         statementGroup: statementGroup,
                                         editableProperties: editableProperty
                                     });
            this.listenTo(model, 'update-property-text', this.updateResponseMessage);
            return statementGroup;
        };

        ReplyStatementView.prototype.updateResponseMessage = function (newMessageText, propertyKey) {
            this._model.setReplyMessage(newMessageText);
            var displayText = this._model.getReplyExpression();
            this.renderDisplayText(displayText);
        };

        /**
         * When the top edge move event triggers
         * @override
         */
        ReplyStatementView.prototype.onTopEdgeMovedTrigger = function (dy) {

            if (BallerinaASTFactory.isResourceDefinition(this.getModel().getParent())) {
                this._replyLine.attr('y1', parseFloat(this._replyLine.attr('y1')) + dy);
                this._replyLine.attr('y2', parseFloat(this._replyLine.attr('y2')) + dy);

                this._replyArrowHead.remove();
                var replyLineEndPoint = new Point((this.getBoundingBox().x() - this._distanceToClient),
                    (this.getBoundingBox().y() + (this.getBoundingBox().h() / 2)));
                this._replyArrowHead = D3Utils.outputTriangle(replyLineEndPoint.x(), replyLineEndPoint.y(), this._replyArrow)
                    .classed("action-arrow", true);
                this.getSvgRect().attr('y', parseFloat(this.getSvgRect().attr('y')) + dy);
                this.getSvgText().attr('y', parseFloat(this.getSvgText().attr('y')) + dy);
            } else {
                var self = this;
                var replyReceiver = this.getModel().getParent().getReplyReceiver();

                if (dy > 0) {
                    // Moving the statement down
                    self.getSvgRect().attr('y', parseFloat(self.getSvgRect().attr('y')) + dy);
                    self.getSvgText().attr('y', parseFloat(self.getSvgText().attr('y')) + dy);
                    if (!_.isNil(replyReceiver)) {
                        this.getDiagramRenderingContext().getViewOfModel(replyReceiver).onMoveInitiatedByReply(dy);
                    }
                } else if (dy < 0) {
                    // Moving the statement up
                    if (!_.isNil(replyReceiver)) {
                        self.stopListening(self.getBoundingBox(), 'top-edge-moved');
                        var receiverView = this.getDiagramRenderingContext().getViewOfModel(replyReceiver);
                        if (_.isNil(receiverView) || (!_.isNil(receiverView) && receiverView.canMoveUp(dy))) {
                            self.getSvgRect().attr('y', parseFloat(self.getSvgRect().attr('y')) + dy);
                            self.getSvgText().attr('y', parseFloat(self.getSvgText().attr('y')) + dy);
                            if (!_.isNil(receiverView)) {
                                receiverView.onMoveInitiatedByReply(dy);
                            }
                        } else {
                            self.getBoundingBox().move(0, -dy);
                        }
                        self.listenTo(self.getBoundingBox(), 'top-edge-moved', function (dy) {
                            self.onTopEdgeMovedTrigger(dy);
                        });
                    } else {
                        self.getSvgRect().attr('y', parseFloat(self.getSvgRect().attr('y')) + dy);
                        self.getSvgText().attr('y', parseFloat(self.getSvgText().attr('y')) + dy);
                    }
                }
            }
        };

        /**
         * Check whether the reply statement can move upwards
         * @param {number} dy - delta y distance
         * @return {boolean}
         */
        ReplyStatementView.prototype.canMoveUp = function (dy) {
            var self = this;
            var bBox = this.getBoundingBox();
            var previousStatement = undefined;
            var previousStatementView = undefined;
            var statementContainer = this.getParent().getStatementContainer();
            var innerDropZoneHeight = 30;
            var currentIndex = _.findIndex(statementContainer.getManagedStatements(), function (stmt) {
                return stmt.id === self.getModel().id;
            });
            var newBBoxTop = bBox.getTop() + dy;

            if (currentIndex > 0) {
                previousStatement = statementContainer.getManagedStatements()[currentIndex - 1];
                previousStatementView = self.getDiagramRenderingContext().getViewOfModel(previousStatement);
                return newBBoxTop >= previousStatementView.getBoundingBox().getBottom() + innerDropZoneHeight;
            }
        };

        /**
         * When the reply statement view move is initiated by the reply receiver view
         * @param {number} dy delta y distance
         */
        ReplyStatementView.prototype.onMoveInitiatedByReplyReceiver = function (dy) {
            this.stopListening(this.getBoundingBox(), 'top-edge-moved');
            this.getBoundingBox().move(0, dy);
            this.getSvgRect().attr('y', parseFloat(this.getSvgRect().attr('y')) + dy);
            this.getSvgText().attr('y', parseFloat(this.getSvgText().attr('y')) + dy);
            this.listenTo(this.getBoundingBox(), 'top-edge-moved', function (dy) {
                this.onTopEdgeMovedTrigger(dy);
            });
        };

        return ReplyStatementView;
    });
