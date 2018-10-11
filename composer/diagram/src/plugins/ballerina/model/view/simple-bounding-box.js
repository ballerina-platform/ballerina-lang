/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
class SimpleBBox {

    /**
     * @param x
     * @param y
     * @param w
     * @param h
     * @param expansionW
     * @param expansionH
     * @constructor
     */
    constructor(x, y, w, h, expansionW, expansionH) {
        this.x = x || 0;
        this.y = y || 0;
        this.w = w || 0;
        this.h = h || 0;
        this.expansionW = expansionW || 0;
        this.expansionH = expansionH || 0;
        this.opaque = false;
        this.leftMargin = 0;
    }

    /**
     * init from a top center
     * @param topCenter {Point}
     * @param w
     * @param h
     */
    fromTopCenter(topCenter, w, h) {
        this.x(topCenter.x() - w / 2);
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
        return this.y;
    }

    getBottom() {
        return this.y + this.h;
    }

    getLeft() {
        return this.x;
    }

    getRight() {
        return this.x + this.w;
    }

    getTopCenterX() {
        return this.getLeft() + this.w() / 2;
    }

    /**
     * Returns the center point of this bounding box.
     * @return {{x: number, y: number}} center poin
     */
    getCenter() {
        return { x: this.getCenterX(), y: this.getCenterY() };
    }

    /**
     * Returns X coordinate of the center point of this bounding box.
     * @return {number} X coordinate
     */
    getCenterX() {
        return this.x + (this.w / 2);
    }

    /**
     * Returns Y coordinate of the center point of this bounding box.
     * @return {number} Y coordinate
     */
    getCenterY() {
        return this.y + (this.h / 2);
    }

    /**
     * Set if the bounding box opaque.
     *
     * @param {boolean} opaque set if bounding box opaque or not.
     * @memberof SimpleBBox
     */
    setOpaque(opaque) {
        this.opaque = opaque;
    }

    /**
     * Get bounding box opaqueness.
     *
     * @returns {boolean} return if the bounding box opaque or not.
     * @memberof SimpleBBox
     */
    getOpaque() {
        return this.opaque;
    }
}

export default SimpleBBox;
