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
     * @class Axis
     * @augments EventChannel
     * @param position position
     * @param isHorizontal indicate whether a vertical or horizontal axis
     * @constructor
     */
    var Axis = function (position, isHorizontal) {
        this._isHorizontal = isHorizontal || false;
        this._position = position || 0;
    };

    Axis.prototype = Object.create(EventChannel.prototype);
    Axis.prototype.constructor = Axis;

    /**
     * Indicate Whether a horizontal or vertical
     * @returns {boolean}
     */
    Axis.prototype.isHorizontal =  function () {
       return this._isHorizontal;
    };

    /**
     * set position
     * @param {number} position
     */
    Axis.prototype.setPosition =  function (position) {
       var offset = position - this._position;
       this._position = position;
       this.trigger("moved", offset);
    };

    /**
     * get position
     */
    Axis.prototype.getPosition =  function () {
       return this._position;
    };

    return Axis;
});