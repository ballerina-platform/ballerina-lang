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

define(['lodash', 'jquery', 'd3', 'log', 'd3utils', './point', './ballerina-view'], function (_, $, d3, log, D3Utils, Point, BallerinaView) {

    /**
     * View for a generic Message
     * @param args {object} - config
     * @param args.container {SVGGElement} - SVG group element to draw the Message
     * @param args.start {Point} - start point to draw the Message.
     * @param args.end {Point} - end point to draw the Message.
     * @param args.cssClass.line {object} - css classes for the Message
     * @param args.cssClass.group {object} - css classes for the Message
     *
     * @class MessageView
     * @augments BallerinaView
     * @constructor
     */
    var MessageView = function (args) {
        BallerinaView.call(this, args);
        this._containerD3 = d3.select(this._container);
        this._viewOptions = args;
        this._start = this._viewOptions.start.clone();
        this._end = this._viewOptions.end.clone();
        this._isInputArrow = _.get(args, 'isInputArrow', true);
        this._arrowHead = undefined;

        _.set(this._viewOptions, 'cssClass.line',  _.get(this._viewOptions, 'cssClass.line', 'message'));
        _.set(this._viewOptions, 'cssClass.group',  _.get(this._viewOptions, 'cssClass.message', 'message-container'));

        this._rootGroup = D3Utils.group(this._containerD3)
            .classed(_.get(this._viewOptions, 'cssClass.group'), true);
    };

    MessageView.prototype = Object.create(BallerinaView.prototype);
    MessageView.prototype.constructor = MessageView;

    MessageView.prototype.translate = function (x, y) {
        this._rootGroup.attr("transform", "translate(" + x + "," + y + ")");
    };

    MessageView.prototype.getStart = function () {
        return this._start;
    };

    MessageView.prototype.getEnd = function () {
        return this._end;
    };

    MessageView.prototype.render = function () {
        var self = this;
        this._line = D3Utils.lineFromPoints(this._start, this._end, this._rootGroup)
            .classed(_.get(this._viewOptions, 'cssClass.line'), true);
        var arrowHeadWidth = 5;

        if (this._isInputArrow) {
            this._arrowHead = D3Utils.inputTriangle(this._end.x() - arrowHeadWidth, this._end.y(), this._rootGroup).classed("action-arrow", true);
        } else {
            this._arrowHead = D3Utils.outputTriangle(this._end.x() - arrowHeadWidth, this._end.y(), this._rootGroup).classed("action-arrow", true);
        }

        this._start.on('moved', function(offset){
            var x1 = parseFloat(self._line.attr('x1'));
            var y1 = parseFloat(self._line.attr('y1'));

            self._line.attr('x1', x1 + offset.dx)
                .attr('y1', y1 + offset.dy);

        });

        this._end.on('moved', function(offset){
            var x2 = parseFloat(self._line.attr('x2'));
            var y2 = parseFloat(self._line.attr('y2'));

            self._line.attr('x2', x2 + offset.dx)
                .attr('y2', y2 + offset.dy);

            x2 = parseFloat(self._line.attr('x2'));
            y2 = parseFloat(self._line.attr('y2'));
            var points = "" + x2 + "," + (y2 - 5) + " " + (x2 + 5) + "," + (y2) + " " + x2 + "," + (y2 + 5);
            self._arrowHead.attr('points', points);

        });

        return this;
    };

    /**
     * Remove the root arrow group
     */
    MessageView.prototype.removeArrow = function () {
        this._rootGroup.node().remove();
    };

    /**
     * Move the message
     * @param {number} dx - delta x distance
     * @param {number} dy - delta y distance
     */
    MessageView.prototype.move = function (dx, dy) {
        if (!_.isNil(dx)) {
            this._start.move(dx, 0);
            this._end.move(dx, 0);
        }
        if (!_.isNil(dy)) {
            this._start.move(0, dy);
            this._end.move(0, dy);
        }
    };

    return MessageView;
});