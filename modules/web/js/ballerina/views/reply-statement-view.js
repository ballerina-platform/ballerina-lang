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
define(['lodash', 'log', './simple-statement-view', './../ast/reply-statement', 'd3utils', 'd3', './point'],
    function (_, log, SimpleStatementView, ReplyStatement, D3Utils, d3, Point) {

        /**
         * The view to represent a reply statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ReplyStatement} args.model - The reply statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
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
            var distanceToClient = this.getViewOptions().distanceToClient - (w / 2);
            var replyArrow = D3Utils.group(statementGroup);
            var replyLineStartPoint = new Point(x, (y + (h / 2)));
            var replyLineEndPoint = new Point((x - distanceToClient), (y + (h / 2)));
            var replyLine = D3Utils.lineFromPoints(replyLineStartPoint, replyLineEndPoint, replyArrow)
                                   .classed('message', true);
            var replyArrowHead = D3Utils.outputTriangle(replyLineEndPoint.x(), replyLineEndPoint.y(), replyArrow)
                                        .classed("action-arrow", true);
            replyArrow.arrowLineElement = replyLine;
            replyArrow.arrowHeadElement = replyArrowHead;

            // Creating property pane.
            var model = this.getModel();
            var editableProperty = {
                propertyType: "text",
                key: "Response Message",
                model: model,
                getterMethod: model.getReplyMessage,
                setterMethod: model.setReplyMessage
            };
            this._createPropertyPane({
                                         model: model,
                                         statementGroup: statementGroup,
                                         editableProperties: editableProperty
                                     });
            this.listenTo(model, 'update-property-text', this.updateResponseMessage);

            bBox.on('top-edge-moved', function (dy) {
                replyLine.attr('y1', parseFloat(replyLine.attr('y1')) + dy);
                replyLine.attr('y2', parseFloat(replyLine.attr('y2')) + dy);

                replyArrowHead.remove();
                var replyLineEndPoint = new Point((bBox.x() - distanceToClient), (bBox.y() + (bBox.h() / 2)));
                replyArrowHead = D3Utils.outputTriangle(replyLineEndPoint.x(), replyLineEndPoint.y(), replyArrow)
                                        .classed("action-arrow", true);
            });
            return statementGroup;
        };

        ReplyStatementView.prototype.updateResponseMessage = function (newMessageText, propertyKey) {
            this._model.setReplyMessage(newMessageText);
            var displayText = this._model.getReplyExpression();
            this.renderDisplayText(displayText);
        };

        return ReplyStatementView;
    });
