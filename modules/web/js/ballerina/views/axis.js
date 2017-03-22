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
import _ from 'lodash';
import EventChannel from 'event_channel';

/**
 * @class Axis
 * @augments EventChannel
 * @param position position
 * @param isHorizontal indicate whether a vertical or horizontal axis
 * @constructor
 */
class Axis extends EventChannel {
 constructor(position, isHorizontal) {
     super();
     this._isHorizontal = isHorizontal || false;
     this._position = position || 0;
 }

 /**
  * Indicate Whether a horizontal or vertical
  * @returns {boolean}
  */
 isHorizontal() {
    return this._isHorizontal;
 }

 /**
  * set position
  * @param {number} position
  */
 setPosition(position) {
    var offset = position - this._position;
    this._position = position;
    this.trigger("moved", offset);
 }

 /**
  * get position
  */
 getPosition() {
    return this._position;
 }
}

export default Axis;
