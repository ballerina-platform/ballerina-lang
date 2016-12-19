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
define([ 'lodash', 'event_channel', './axis', './point'], function ( _, EventChannel, Axis, Point) {

    /**
     * @class BoundingBox
     * @augments EventChannel
     * @param x
     * @param y
     * @param w
     * @param h
     * @constructor
     */
    var BBox = function (x, y, w, h) {
        this._w = w || 0;
        this._h = h || 0;
        this._leftTop = new Point(x, y);
        this._leftEdge = new Axis(x, false);
        this._rightEdge = new Axis(x + this._w, false);
        this._topEdge = new Axis(y , false);
        this._bottomEdge = new Axis(y+ this._h, false);

        var self = this;
        this._leftTop.on('moved', function(offset){
            self.trigger('moved', offset);
        });

        this._leftEdge.on('moved', function(offset){
            self.trigger('left-edge-moved', offset);
        });

        this._rightEdge.on('moved', function(offset){
            self.trigger('right-edge-moved', offset);
        });

        this._topEdge.on('moved', function(offset){
            self.trigger('top-edge-moved', offset);
        });

        this._bottomEdge.on('moved', function(offset){
            self.trigger('bottom-edge-moved', offset);
        });
    };

    BBox.prototype = Object.create(EventChannel.prototype);
    BBox.prototype.constructor = BBox;

    /**
     * Gets or sets X
     * @returns {number|BBox} X
     */
    BBox.prototype.x =  function (newX) {
        if (newX === undefined) {
            return this._leftTop.x();
        }
        this._leftTop.x(newX);
        this._leftEdge.setPosition(newX);
        this._rightEdge.setPosition(newX + this.w());
        return this;
    };

    /**
     * Gets or sets y
     * @returns {number|BBox} y
     */
    BBox.prototype.y =  function (newY) {
        if (newY === undefined) {
            return this._leftTop.y();
        }
        this._leftTop.y(newY);
        this._topEdge.setPosition(newY);
        this._bottomEdge.setPosition(newY + this.h());
        return this;
    };

    /**
     * Gets or sets w
     * @returns {number|BBox} w
     */
    BBox.prototype.w =  function (newW) {
        if (newW === undefined) {
            return this._w;
        }
        var delta = newW - this._w;
        this._w = newW;
        this._rightEdge.setPosition(this._leftTop.x() + newW);
        this.trigger("width-changed", delta);
        return this;
    };

    /**
     * Gets or sets h
     * @returns {number|BBox} h
     */
    BBox.prototype.h =  function (newH) {
        if (newH === undefined) {
            return this._h;
        }
        var delta = newH - this._h;
        this._h = newH;
        this._bottomEdge.setPosition(this._leftTop.y() + newH);
        this.trigger("height-changed", delta);
        return this;
    };

    /**
     * move
     * @returns {BBox}
     */
    BBox.prototype.move =  function (dx, dy) {
        this.x(this.x() + dx);
        this.y(this.y() + dy);
        return this;
    };


    /**
     * init from a top center
     * @param topCenter {Point}
     * @param w
     * @param h
     */
    BBox.prototype.fromTopCenter =  function (topCenter, w, h) {
        this.x(topCenter.x() - w/2);
        this.y(topCenter.y());
        this.w(w);
        this.h(h);
    };

    /**
     * init from a top left point
     * @param topLeft {Point}
     * @param w
     * @param h
     */
    BBox.prototype.fromTopLeft =  function (topLeft, w, h) {
        this.x(topLeft.x());
        this.y(topLeft.y());
        this.w(w);
        this.h(h);
    };

    BBox.prototype.getTop =  function () {
        return this._topEdge.getPosition();
    };

    BBox.prototype.getBottom =  function () {
        return this._bottomEdge.getPosition();
    };

    BBox.prototype.getLeft =  function () {
        return this._leftEdge.getPosition();
    };

    BBox.prototype.getRight =  function () {
        return this._rightEdge.getPosition();
    };

    BBox.prototype.getTopCenterX = function () {
        return this.getLeft() + this.w()/2;
    }

    return BBox;
});