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

define(['lodash', 'jquery', 'd3', 'log', 'd3utils', './point', './life-line'], function (_, $, d3, log, D3Utils, Point, LifeLine) {

    /**
     * View for  the Client lifeline
     * @param args {object} - config
     * @param args.container {SVGGElement} - SVG group element to draw the life line
     * @param args.centerPoint {Point} - center point to draw the life line.
     * @param args.cssClass {object} - css classes for the lifeline
     * @param args.cssClass.group {string} - css class for root group
     * @param args.cssClass.title {string} - css class for the title
     * @param args.title {string} - title
     * @class ClientLifeLineView
     * @augments LifeLineView
     * @constructor
     */
    var ClientLifeLineView = function (args) {
        _.set(args, 'title',  _.get(args, 'title', 'client'));
        _.set(args, 'line.height', _.get(args, 'line.height', 290));
        _.set(args, 'cssClass.group',  _.get(args, 'cssClass.group', 'client-life-line'));
        LifeLine.call(this, args);
    };

    ClientLifeLineView.prototype = Object.create(LifeLine.prototype);
    ClientLifeLineView.prototype.constructor = ClientLifeLineView;

    ClientLifeLineView.prototype.renderTopPolygon = function () {
        var self = this;
        this._topPolygon = D3Utils.polygon(this._calculatePolygonPoints(this._topCenter), this._rootGroup);
        this._topPolygon.attr("stroke-linejoin", "round");

        this._topCenter.on('moved', function (offset) {
           self._topPolygon.attr('points',  self._calculatePolygonPoints(self._topCenter));
        });
    };

    ClientLifeLineView.prototype.renderBottomPolygon = function () {
        var self = this;
        this._bottomPolygon = D3Utils.polygon(this._calculatePolygonPoints(this._bottomCenter), this._rootGroup);
        this._bottomPolygon.attr("stroke-linejoin", "round");

        this._bottomCenter.on('moved', function (offset) {
           self._bottomPolygon.attr('points',  self._calculatePolygonPoints(self._bottomCenter));
        });
    };

    /**
     *
     * @param point {Point}
     * @return {string}
     * @private
     */
    ClientLifeLineView.prototype._calculatePolygonPoints = function(point){
        var polygonYOffset = 32,
            polygonXOffset = 32;
        var topPolygonPoints =
            // Bottom point of the polygon.
            " " + point.x() + "," + (point.y() + polygonYOffset) +
                // Right point of the polygon
            " " + (point.x() + polygonXOffset) + "," + point.y() +
                // Top point of the polygon.
            " " + point.x() + "," + (point.y() - polygonYOffset) +
                // Left point of the polygon.
            " " + (point.x() - polygonXOffset) + "," + point.y();

        return topPolygonPoints;

    };

    return ClientLifeLineView;
});