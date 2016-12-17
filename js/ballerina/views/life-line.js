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
     * View for a generic lifeline
     * @param args {object} - config
     * @param args.container {SVGGElement} - SVG group element to draw the life line
     * @param args.centerPoint {Point} - center point to draw the life line.
     * @param args.cssClass {object} - css classes for the lifeline
     * @param args.cssClass.group {string} - css class for root group
     * @param args.cssClass.title {string} - css class for the title
     * @param args.title {string} - title
     * @param args.rect {Object} - top and bottom rectangle properties
     * @param args.rect.width {number} - rect width
     * @param args.rect.height {number} - rect height
     * @param args.rect.round {number} - rx and ry
     * @param args.content {Object} - properties for content area
     * @param args.content.width {number} - width size
     * @param args.content.offsetX {number} - offset in X from top and bottom center points
     * @param args.content.offsetY {number} - offset from Y top and bottom center points
     *
     * @class LifeLineView
     * @augments EventChannel
     * @constructor
     */
    var LifeLineView = function (args) {
        BallerinaView.call(this, args);
        this._containerD3 = d3.select(this._container);
        this._viewOptions = args;
        this._topCenter = this._viewOptions.centerPoint.clone();

        _.set(this._viewOptions, 'title',  _.get(this._viewOptions, 'title', 'life-line'));
        _.set(this._viewOptions, 'rect.width', _.get(this._viewOptions, 'rect.width', 120));
        _.set(this._viewOptions, 'rect.height', _.get(this._viewOptions, 'rect.height', 30));
        _.set(this._viewOptions, 'rect.round', _.get(this._viewOptions, 'rect.round', 0));
        _.set(this._viewOptions, 'line.height', _.get(this._viewOptions, 'line.height', 240));
        _.set(this._viewOptions, 'content.width', _.get(this._viewOptions, 'content.width', 140));
        _.set(this._viewOptions, 'content.offset', _.get(this._viewOptions, 'content.offset', {top:50, bottom: 50}));
        _.set(this._viewOptions, 'cssClass.title', _.get(this._viewOptions, 'cssClass.title', 'life-line-title'));
        _.set(this._viewOptions, 'cssClass.group', _.get(this._viewOptions, 'cssClass.group', 'life-line'));

        this._bottomCenter = this._topCenter.clone().move(0, _.get(this._viewOptions, 'line.height' ));

        this._rootGroup = D3Utils.group(this._containerD3)
            .classed(_.get(this._viewOptions, 'cssClass.group'), true);

        this.getBoundingBox()
            .x(this._topCenter.x() -  _.get(this._viewOptions, 'rect.width')/2)
            .y(this._topCenter.y() +  _.get(this._viewOptions, 'rect.height')/2)
            .w(_.get(this._viewOptions, 'rect.width'))
            .h(_.get(this._viewOptions, 'line.height') + _.get(this._viewOptions, 'rect.width'));
    };

    LifeLineView.prototype = Object.create(BallerinaView.prototype);
    LifeLineView.prototype.constructor = LifeLineView;

    LifeLineView.prototype.position = function (x, y) {
        this._rootGroup.attr("transform", "translate(" + x + "," + y + ")");
    };

    LifeLineView.prototype.getMidPoint = function () {
        return this._topCenter.x();
    };

    LifeLineView.prototype.width = function () {
        return this._topPolygon.attr('width');
    };

    LifeLineView.prototype._updateBoundingBox = function () {
        
    }

    LifeLineView.prototype.render = function () {
        this.renderMiddleLine();
        this.renderTopPolygon();
        this.renderBottomPolygon();
        this.renderTitle();
        this.renderContentArea();
        return this;
    };

    LifeLineView.prototype.move = function (dx, dy) {
        this._bottomCenter.move(dx, dy);
        this._topCenter.move(dx, dy);
    };

    LifeLineView.prototype.increaseHeight = function (dy) {
        this._bottomCenter.move(0, dy);
    };

    LifeLineView.prototype.setHeight = function (height) {
        var newY = height - this._topCenter.y();
        this._bottomCenter.y(newY);
    };

    LifeLineView.prototype.getTopCenter = function () {
        return this._topCenter;
    };

    LifeLineView.prototype.getBottomCenter = function () {
        return this._bottomCenter;
    };

    LifeLineView.prototype.renderTopPolygon = function () {
        var self = this;
        this._topPolygon = D3Utils.centeredRect(this._topCenter,
            this._viewOptions.rect.width, this._viewOptions.rect.height, 0, 0, this._rootGroup);

        this._topCenter.on('moved', function (offset) {
            var x = parseFloat(self._topPolygon.attr('x'));
            var y = parseFloat(self._topPolygon.attr('y'));
            self._topPolygonText
                .attr('x', x + offset.dx)
                .attr('y', y + offset.dy)
        });
    };

    LifeLineView.prototype.renderBottomPolygon = function () {
        var self = this;
        this._bottomPolygon = D3Utils.centeredRect(this._bottomCenter,
            this._viewOptions.rect.width, this._viewOptions.rect.height, 0, 0, this._rootGroup);

        this._bottomCenter.on('moved', function (offset) {
            var x = parseFloat(self._bottomPolygon.attr('x'));
            var y = parseFloat(self._bottomPolygon.attr('y'));
            self._bottomPolygon
                .attr('x', x + offset.dx)
                .attr('y', y + offset.dy)
        });
    };

    LifeLineView.prototype.renderTitle = function(){
        var self = this;
        this._topPolygonText = D3Utils.centeredText(this._topCenter,
            this._viewOptions.title, this._rootGroup)
            .classed(this._viewOptions.cssClass.title, true).classed("genericT", true);

        this._topCenter.on('moved', function (offset) {
            var x = parseFloat(self._topPolygonText.attr('x'));
            var y = parseFloat(self._topPolygonText.attr('y'));
            self._topPolygonText
                .attr('x', x + offset.dx)
                .attr('y', y + offset.dy)
        });

        this._bottomPolygonText = D3Utils.centeredText(this._bottomCenter,
            this._viewOptions.title, this._rootGroup)
            .classed(this._viewOptions.cssClass.title, true).classed("genericT", true);

        this._bottomCenter.on('moved', function (offset) {
            var x = parseFloat(self._bottomPolygonText.attr('x'));
            var y = parseFloat(self._bottomPolygonText.attr('y'));
            self._bottomPolygonText
                .attr('x', x + offset.dx)
                .attr('y', y + offset.dy)
        });

    };

    LifeLineView.prototype.renderMiddleLine = function () {
        var self = this;
        this._middleLine = D3Utils.lineFromPoints(this._topCenter, this._bottomCenter, this._rootGroup);

        this._topCenter.on('moved', function (offset) {
            var x1 = parseFloat(self._middleLine.attr('x1'));
            var y1 = parseFloat(self._middleLine.attr('y1'));
            self._middleLine
                .attr('x1', x1 + offset.dx)
                .attr('y1', y1 + offset.dy)
        });

        this._bottomCenter.on('moved', function (offset) {
            var x2 = parseFloat(self._middleLine.attr('x2'));
            var y2 = parseFloat(self._middleLine.attr('y2'));
            self._middleLine
                .attr('x2', x2 + offset.dx)
                .attr('y2', y2 + offset.dy)
        });

    };

    LifeLineView.prototype.renderContentArea = function () {
        this._contentArea = D3Utils.group(this._rootGroup);
    };

    LifeLineView.prototype.getContentArea = function () {
        return this._contentArea;
    };

    LifeLineView.prototype.getContentOffset = function () {
        return _.get(this._viewOptions, 'content.offset');
    };


    return LifeLineView;
});