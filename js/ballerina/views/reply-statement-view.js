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
define(['lodash', 'log', './ballerina-statement-view', './../ast/reply-statement', 'd3utils', 'd3', './point'],
    function (_, log, BallerinaStatementView, ReplyStatement, D3Utils, d3, Point) {

        /**
         * The view to represent a reply statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ReplyStatement} args.model - The reply statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ReplyStatementView = function (args) {

            BallerinaStatementView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof ReplyStatement)) {
                log.error("Return statement definition is undefined or is of different type." + this._model);
                throw "Return statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for return statement is undefined." + this._container);
                throw "Container for return statement is undefined." + this._container;
            }

            // View options for height and width of the assignment statement box.
            this._viewOptions.height = _.get(args, "viewOptions.height", 30);
            this._viewOptions.width = _.get(args, "viewOptions.width", 120);
            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
        };

        ReplyStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        ReplyStatementView.prototype.constructor = ReplyStatementView;

        ReplyStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ReplyStatement) {
                this._model = model;
            } else {
                log.error("Return statement definition is undefined or is of different type." + model);
                throw "Return statement definition is undefined or is of different type." + model;
            }
        };

        ReplyStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for return statement is undefined." + container);
                throw "Container for return statement is undefined." + container;
            }
        };

        ReplyStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ReplyStatementView.prototype.getModel = function () {
            return this._model;
        };

        ReplyStatementView.prototype.getContainer = function () {
            return this._container;
        };

        ReplyStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * Rendering the view for reply statement.
         * @returns {group} The svg group which contains the elements of the reply statement view.
         */
        ReplyStatementView.prototype.render = function () {
            var width =  this._viewOptions.width;
            var height = this._viewOptions.height;

            var startActionGroup = D3Utils.group(d3.select(this._container));
            var line_start = new Point(this.getBoundingBox().x(), this.getBoundingBox().y() + height/2);
            // FixMe: here 50 is the length of the arrow line. This value has to determine dynamically considering the client lifeline
            var distanceToClient = 50;
            var line_end = new Point(this.getBoundingBox().x() - distanceToClient, this.getBoundingBox().y() + height/2);
            var reply_rect = D3Utils.rect(this.getBoundingBox().x(), this.getBoundingBox().y(), width, height, 0, 0, startActionGroup).classed('statement-rect', true);
            var reply_text = D3Utils.textElement(this.getBoundingBox().x() + width/2, this.getBoundingBox().y() + height/2, 'Reply', startActionGroup).classed('statement-text', true);
            var reply_line = D3Utils.lineFromPoints(line_start,line_end, startActionGroup)
                .classed('message', true);
            var arrowHeadWidth = 5;
            var reply_arrow_head = D3Utils.outputTriangle(line_end.x(), line_end.y(), startActionGroup).classed("action-arrow", true);

            log.info("Rendering the Reply Statement.");

            var self = this;
            this.getBoundingBox().on('top-edge-moved', function(dy){
                reply_rect.attr('y',  parseFloat(reply_rect.attr('y')) + dy);
                reply_text.attr('y',  parseFloat(reply_text.attr('y')) + dy);
                reply_line.attr('y1',  parseFloat(reply_line.attr('y1')) + dy);
                reply_line.attr('y2',  parseFloat(reply_line.attr('y2')) + dy);

                line_end = new Point(self.getBoundingBox().x() - distanceToClient, self.getBoundingBox().y() + height/2);

                reply_arrow_head.remove();
                reply_arrow_head = D3Utils.outputTriangle(line_end.x(), line_end.y(), startActionGroup).classed("action-arrow", true);
            });
            return startActionGroup;
        };



        return ReplyStatementView;
    });