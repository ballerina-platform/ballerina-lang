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

export function processFrames(message) {
    message.frames = _.uniqWith(message.frames, (obj, other) => {
        if (_.isEqual(obj.frameName, other.frameName) && _.isEqual(obj.packageName, other.packageName)) {
            return true;
        }
        return false;
    });

    // reverse order
    const newFrames = _.reverse(message.frames);

    newFrames.map((frame) => {
        frame.variables.map((item) => {
            switch (item.type) {
                case 'BBoolean':
                    item.type = 'boolean';
                    break;
                case 'BInteger':
                    item.type = 'int';
                    break;
                case 'BFloat':
                    item.type = 'float';
                    break;
                case 'BLong':
                    item.type = 'long';
                    break;
                case 'BDouble':
                    item.type = 'double';
                    break;
                case 'BString':
                    item.type = 'string';
                    break;
                case 'BJSON':
                    item.type = 'json';
                    break;
                case 'BArray':
                    item.type = 'array';
                    break;
                case 'BMessage':
                    item.type = 'message';
                    break;
                case 'BConnector':
                    item.type = 'connector';
                    break;
                case 'BDataTable':
                    item.type = 'datatable';
                    break;
                case 'BXML':
                    item.type = 'xml';
                    break;
                case 'BValue':
                    item.type = 'value';
                    break;
                case 'BMap':
                    item.type = 'map';
                    break;
                case 'BValueType':
                    item.type = 'valuetype';
                    break;
                case 'BStruct':
                    item.type = 'struct';
                    break;
                case 'BException':
                    item.type = 'exception';
                    break;
                case 'BRefType':
                    item.type = 'reftype';
                    break;
                default:

            }
            return item;
        });
        return frame;
    });

    message.frames = newFrames;
    return message;
}
