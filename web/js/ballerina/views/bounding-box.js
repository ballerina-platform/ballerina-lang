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
     * @class BBox
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
        var deltaX = newX - this._x;
        if (deltaX === 0) {
            return this; // X hasn't changed
        }
        var oldCenterX = this.getCenterX();
        this._x = newX;
        this.trigger('moved', {dx: deltaX, dy: 0});
        this.trigger('left-edge-moved', deltaX);
        this.trigger('right-edge-moved', deltaX);
        triggerCenterXChanged(oldCenterX, this.getCenterX(), this);
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
        var deltaY = newY - this._y;
        if (deltaY === 0) {
            return this; // Y hasn't changed
        }
        var oldCenterY = this.getCenterY();
        this._y = newY;
        this.trigger('moved', {dx: 0, dy: deltaY});
        this.trigger('top-edge-moved', deltaY);
        this.trigger('bottom-edge-moved', deltaY);
        triggerCenterYChanged(oldCenterY, this.getCenterY(), this);
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
        var deltaW = newW - this._w;
        if (deltaW === 0) {
            return this; // width hasn't changed
        }
        var oldCenterX = this.getCenterX();
        this._w = newW;
        this.trigger('right-edge-moved', deltaW);
        this.trigger('width-changed', deltaW);
        triggerCenterXChanged(oldCenterX, this.getCenterX(), this);
        return this;
    };

    /**
     * Gets or sets h
     * @returns {number|BBox} h
     */
    BBox.prototype.h = function (newH, silent) {
        if (newH === undefined) {
            return this._h;
        }
        var deltaH = newH - this._h;
        if (deltaH == 0) {
            return this; // height hasn't changed
        }
        var oldCenterY = this.getCenterY();
        this._h = newH;
        if (!silent) {
            this.trigger('bottom-edge-moved', deltaH);
            this.trigger('height-changed', deltaH);
            triggerCenterYChanged(oldCenterY, this.getCenterY(), this);
        }
        return this;
    };

    /**
     * move
     * @returns {BBox}
     */
    BBox.prototype.move =  function (dx, dy, silent) {
        if(silent){
            this._x = this.x() + dx;
            this._y = this.y() + dy;
            return this;
        }
        this.x(this.x() + dx);
        this.y(this.y() + dy);
        return this;
    };

    /**
     * Expand/contract this bounding box to the given width whilst keeping the center still.
     * @param w new width
     * @returns {BBox} this
     */
    BBox.prototype.zoomWidth = function (w) {
        if (this._w !== w) {
            this.x(this._x - ((w - this._w)) / 2);
            this.w(w);
        }
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

    /**
     * Returns the center point of this bounding box.
     * @return {{x: number, y: number}} center poin
     */
    BBox.prototype.getCenter = function () {
        return {x: this.getCenterX(), y: this.getCenterY()};
    };

    /**
     * Returns X coordinate of the center point of this bounding box.
     * @return {number} X coordinate
     */
    BBox.prototype.getCenterX = function () {
        return this._x + (this._w / 2);
    };

    /**
     * Returns Y coordinate of the center point of this bounding box.
     * @return {number} Y coordinate
     */
    BBox.prototype.getCenterY = function () {
        return this._y + (this._h / 2);
    };

    /**
     * Triggers events for center X coordinate changed.
     * @param oldCenterX {number} old X coordinate of the center
     * @param newCenterX {number} new X coordinate of the center
     * @param eventChannel {EventChannel} event channel to trigger events
     */
    function triggerCenterXChanged(oldCenterX, newCenterX, eventChannel) {
        var deltaCenterX = newCenterX - oldCenterX;
        if (deltaCenterX === 0) {
            return; // center X hasn't changed
        }
        eventChannel.trigger('center-moved', {dx: deltaCenterX, dy: 0});
        eventChannel.trigger('center-x-moved', deltaCenterX);
    }

    /**
     * Triggers events for center Y coordinate changed.
     * @param oldCenterY {number} old Y coordinate of the center
     * @param newCenterY {number} new Y coordinate of the center
     * @param eventChannel {EventChannel} event channel to trigger events
     */
    function triggerCenterYChanged(oldCenterY, newCenterY, eventChannel) {
        var deltaCenterY = newCenterY - oldCenterY;
        if (deltaCenterY === 0) {
            return; // center Y hasn't changed
        }
        eventChannel.trigger('center-moved', {dx: 0, dy: deltaCenterY});
        eventChannel.trigger('center-y-moved', deltaCenterY);
    }

    return BBox;
});