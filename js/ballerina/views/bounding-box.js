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
define([ 'lodash', 'event_channel'], function ( _, EventChannel) {

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
        this._x = x || 0;
        this._y = y || 0;
        this._w = w || 0;
        this._h = h || 0;
    };

    BBox.prototype = Object.create(EventChannel.prototype);
    BBox.prototype.constructor = BBox;

    /**
     * Gets or sets X
     * @returns {number|BBox} X
     */
    BBox.prototype.x =  function (newX) {
        if (newX === undefined) {
            return this._x;
        }
        var offset = newX - this._x;
        this._x = newX;
        this.trigger('moved', {dx: offset, dy: 0});
        this.trigger('left-edge-moved', offset);
        this.trigger('right-edge-moved', offset);
        return this;
    };

    /**
     * Gets or sets y
     * @returns {number|BBox} y
     */
    BBox.prototype.y =  function (newY) {
        if (newY === undefined) {
            return this._y;
        }
        var offset = newY - this._y;
        this._y = newY;
        this.trigger('moved', {dx: 0, dy: offset});
        this.trigger('top-edge-moved', offset);
        this.trigger('bottom-edge-moved', offset);
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
        this.trigger('right-edge-moved', delta);
        this.trigger('width-changed', delta);
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
        this.trigger('bottom-edge-moved', delta);
        this.trigger('height-changed', delta);
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
        return this._y;
    };

    BBox.prototype.getBottom =  function () {
        return this._y + this._h;
    };

    BBox.prototype.getLeft =  function () {
        return this._x;
    };

    BBox.prototype.getRight =  function () {
        return this._x + this._w;
    };

    BBox.prototype.getTopCenterX = function () {
        return this.getLeft() + this.w()/2;
    };

    return BBox;
});