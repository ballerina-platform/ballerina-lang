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
define(['lodash', 'log', 'd3', 'jquery', 'd3utils', './point'],
    function (_, log, d3, $, D3utils, Point) {
        /**
         * The view to represent an action processor
         * @param {Object} args - Arguments for creating the view.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */

        var ActionProcessorView = function (args) {
            this._viewOptions = _.get(args, 'viewOptions', {});
            this._viewOptions.parent = _.get(args, "parent", "undefined");
            this._viewOptions.width = _.get(args, "processorWidth", "undefined");
            this._viewOptions.height = _.get(args, "processorHeight", "undefined");
            this._viewOptions.centerPoint = _.get(args, "centerPoint", {});
            this._viewOptions.centerPoint.x = _.get(args, "centerPoint.x", "undefined");
            this._viewOptions.centerPoint.y = _.get(args, "centerPoint.y", "undefined");

            this._viewOptions.sourcePoint = _.get(args, "sourcePoint", {});
            this._viewOptions.sourcePoint.x = _.get(args, "sourcePoint.x", "undefined");
            this._viewOptions.sourcePoint.y = _.get(args, "sourcePoint.y", "undefined");

            this._viewOptions.destinationPoint = _.get(args, "destinationPoint", {});
            this._viewOptions.destinationPoint.x = _.get(args, "destinationPoint.x", "undefined");
            this._viewOptions.destinationPoint.y = _.get(args, "destinationPoint.y", "undefined");

            this._viewOptions.inArrow = _.get(args, "inArrow", false);
            this._viewOptions.outArrow = _.get(args, "outArrow", false);
            this._viewOptions.arrowX = _.get(args, "arrowX", "undefined");
            this._viewOptions.arrowY = _.get(args, "arrowY", "undefined");
            this._viewOptions.action = _.get(args, "action", "undefined");

            this._width = 0;
            this._height = 0;
            this._xPosition = 0;
            this._yPosition = 0;
        };

        ActionProcessorView.prototype.constructor = ActionProcessorView;
        ActionProcessorView.prototype.createPoint = function (x, y) {
            this._x = x || 0;
            this._y = y || 0;

        };

        ActionProcessorView.prototype.render = function () {

            var lineGap = 8;
            var centerTextXGap = 40;
            var centerTextYGap = 20;
            //TODO: move css to classes
            var processorRect = D3utils.centeredRect(new Point(this._viewOptions.centerPoint.x, this._viewOptions.centerPoint.y), this._viewOptions.width
                , this._viewOptions.height, 0, 0, this._viewOptions.parent).classed("action-rect", true);
            var processorConnector = D3utils.line(this._viewOptions.sourcePoint.x, this._viewOptions.sourcePoint.y, this._viewOptions.destinationPoint.x,
                this._viewOptions.destinationPoint.y, this._viewOptions.parent).classed("action-line", true);
            //TODO: center text
            var processorText = D3utils.textElement((this._viewOptions.centerPoint.x + centerTextXGap - this._viewOptions.width / 2), (this._viewOptions.centerPoint.y + centerTextYGap - (this._viewOptions.height / 2)),
                this._viewOptions.action, this._viewOptions.parent).classed("action-text", true);
            if (this._viewOptions.inArrow) {
                var arrowHead = D3utils.inputTriangle(this._viewOptions.arrowX, this._viewOptions.arrowY, this._viewOptions.parent).classed("action-arrow", true);
            }
            if (this._viewOptions.outArrow) {
                var processorConnector2 = D3utils.line(this._viewOptions.sourcePoint.x, this._viewOptions.sourcePoint.y + lineGap, this._viewOptions.destinationPoint.x,
                    this._viewOptions.destinationPoint.y + lineGap, this._viewOptions.parent).classed("action-line", true);
                D3utils.outputTriangle(this._viewOptions.sourcePoint.x, this._viewOptions.sourcePoint.y + lineGap, this._viewOptions.parent).classed("action-arrow", true);
            }

            this._width = this._viewOptions.width;
            this._height = this._viewOptions.height;
            this._xPosition = parseInt(processorRect.attr('x'));
            this._yPosition = parseInt(processorRect.attr('y'));

        };

        ActionProcessorView.prototype.setWidth = function (newWidth) {
            this._width = newWidth;
        };

        ActionProcessorView.prototype.setHeight = function (newHeight) {
            this._height = newHeight;
        };

        ActionProcessorView.prototype.setXPosition = function (newXPosition) {
            this._xPosition = newXPosition;
        };

        ActionProcessorView.prototype.setYPosition = function (newYPosition) {
            this._yPosition = newYPosition;
        };

        ActionProcessorView.prototype.getWidth = function () {
            return this._width;
        };

        ActionProcessorView.prototype.getHeight = function () {
            return this._height;
        };

        ActionProcessorView.prototype.getXPosition = function () {
            return this._xPosition;
        };

        ActionProcessorView.prototype.getYPosition = function () {
            return this._yPosition;
        };

        return ActionProcessorView;

    });