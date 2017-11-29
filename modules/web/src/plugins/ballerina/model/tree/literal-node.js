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
import AbstractLiteralNode from './abstract-tree/literal-node';

/**
 * A node to represent a literal value such as strings, boolean values, integers.
 * @class LiteralNode
 * @extends {AbstractLiteralNode}
 */
class LiteralNode extends AbstractLiteralNode {

    /**
     * Sets the value as a string. Mean wrapped with double quotes.
     * @param {string} newValue The new value.
     * @param {boolean} silent Whether to trigger events or not.
     * @param {string} title The title of the event.
     * @memberof LiteralNode
     */
    setValueAsString(newValue, silent, title) {
        const oldValue = this.value;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.value = `"${newValue}"`;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'value',
                    newValue: `"${newValue}"`,
                    oldValue,
                },
            });
        }
    }
}

export default LiteralNode;
