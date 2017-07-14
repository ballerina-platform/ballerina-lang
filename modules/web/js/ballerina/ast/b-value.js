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
import ASTNode from './node';

/**
 * AST Node for a bValue.
 *
 * @class BValue
 * @extends {ASTNode}
 */
class BValue extends ASTNode {
    /**
     * Creates an instance of BValue.
     * @param {Object} args arugments for creating an instance.
     * @param {string} args.type The type. Example: 'string'.
     * @param {string} args.stringValue The value as a string.
     * @memberof BValue
     */
    constructor(args) {
        super('BValue');
        this._bType = _.get(args, 'type', 'string');
        this._stringValue = _.get(args, 'stringValue');
    }

    // eslint-disable-next-line require-jsdoc
    setBType(bType, options) {
        this.setAttribute('_bType', bType, options);
    }

    // eslint-disable-next-line require-jsdoc
    getBType() {
        return this._bType;
    }

    // eslint-disable-next-line require-jsdoc
    setStringValue(stringValue, options) {
        this.setAttribute('_stringValue', stringValue, options);
    }

    // eslint-disable-next-line require-jsdoc
    getStringValue() {
        return this._stringValue;
    }

    /**
     * Initializes object using json object.
     * @param {Object} jsonNode Object for creation.
     * @param {string} jsonNode.bvalue_type The type.
     * @param {string} jsonNode.bvalue_string_value The value as a string.
     */
    initFromJson(jsonNode) {
        this.setBType(jsonNode.bvalue_type, { doSilently: true });
        this.setStringValue(jsonNode.bvalue_string_value, { doSilently: true });
    }
}

export default BValue;

