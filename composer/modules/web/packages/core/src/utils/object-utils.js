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

import _ from 'lodash';

/**
 * Make given object imutable
 *
 * @param {Object} object to freeze
 * @return {Object} frozen object
 */
export function makeImutable(o) {
    if (_.isFunction(o)) {
        return o;
    }
    Object.freeze(o);
    Object.preventExtensions(0);
    if (o === undefined) {
        return o;
    }
    Object.getOwnPropertyNames(o).forEach((prop) => {
        if (o[prop] !== null
        && (typeof o[prop] === 'object' || typeof o[prop] === 'function')
        && !Object.isFrozen(o[prop])) {
            makeImutable(o[prop]);
        }
    });
    return o;
}
