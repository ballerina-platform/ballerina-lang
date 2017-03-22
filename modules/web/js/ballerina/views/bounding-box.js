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
import EventChannel from 'event_channel';

/**
 * @class BBox
 * @extends EventChannel
 */
class BBox extends EventChannel {
    /**
     * @param x
     * @param y
     * @param w
     * @param h
     * @constructor
     */
    constructor(x, y, w, h) {
        super();
        this._x = x || 0;
        this._y = y || 0;
        this._w = w || 0;
        this._h = h || 0;
    }

    /**
     * Gets or sets X
     * @returns {number|BBox} X
     */
    x(newX) {
        if (newX === undefined) {
            return this._x;
        }
        var offset = newX - this._x;
        this._x = newX;
        this.trigger('moved', {dx: offset, dy: 0});
        this.trigger('left-edge-moved', offset);
        this.trigger('right-edge-moved', offset);
        return this;
    }

    /**
     * Gets or sets y
     * @returns {number|BBox} y
     */
    y(newY) {
        if (newY === undefined) {
            return this._y;
        }
        var offset = newY - this._y;
        this._y = newY;
        this.trigger('moved', {dx: 0, dy: offset});
        this.trigger('top-edge-moved', offset);
        this.trigger('bottom-edge-moved', offset);
        return this;
    }

    /**
     * Gets or sets w
     * @returns {number|BBox} w
     */
    w(newW) {
        if (newW === undefined) {
            return this._w;
        }
        var deltaW = newW - this._w;
        this._w = newW;
        this.trigger('right-edge-moved', deltaW);
        this.trigger('width-changed', deltaW);
        return this;
    }

    /**
     * Gets or sets h
     * @returns {number|BBox} h
     */
    h(newH, silent) {
        if (newH === undefined) {
            return this._h;
        }
        var deltaH = newH - this._h;
        this._h = newH;
        if (!silent) {
            this.trigger('bottom-edge-moved', deltaH);
            this.trigger('height-changed', deltaH);
        }
        return this;
    }

    /**
     * move
     * @returns {BBox}
     */
    move(dx, dy, silent) {
        if(silent){
            this._x = this.x() + dx;
            this._y = this.y() + dy;
            return this;
        }
        this.x(this.x() + dx);
        this.y(this.y() + dy);
        return this;
    }

    /**
     * init from a top center
     * @param topCenter {Point}
     * @param w
     * @param h
     */
    fromTopCenter(topCenter, w, h) {
        this.x(topCenter.x() - w/2);
        this.y(topCenter.y());
        this.w(w);
        this.h(h);
    }

    /**
     * init from a top left point
     * @param topLeft {Point}
     * @param w
     * @param h
     */
    fromTopLeft(topLeft, w, h) {
        this.x(topLeft.x());
        this.y(topLeft.y());
        this.w(w);
        this.h(h);
    }

    getTop() {
        return this._y;
    }

    getBottom() {
        return this._y + this._h;
    }

    getLeft() {
        return this._x;
    }

    getRight() {
        return this._x + this._w;
    }

    getTopCenterX() {
        return this.getLeft() + this.w()/2;
    }

    /**
     * Returns the center point of this bounding box.
     * @return {{x: number, y: number}} center poin
     */
    getCenter() {
        return {x: this.getCenterX(), y: this.getCenterY()};
    }

    /**
     * Returns X coordinate of the center point of this bounding box.
     * @return {number} X coordinate
     */
    getCenterX() {
        return this._x + (this._w / 2);
    }

    /**
     * Returns Y coordinate of the center point of this bounding box.
     * @return {number} Y coordinate
     */
    getCenterY() {
        return this._y + (this._h / 2);
    }
}

export default BBox;
