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
import Expression from './expression';
import FragmentUtils from '../../utils/fragment-utils';

/**
 * Constructor for BasicLiteralExpression
 * @param {Object} args - Arguments to create the BasicLiteralExpression
 * @constructor
 */
class BasicLiteralExpression extends Expression {
    constructor(args) {
        super('BasicLiteralExpression');
        this._basicLiteralType = _.get(args, 'basicLiteralType', '');
        this._basicLiteralValue = _.get(args, 'basicLiteralValue', '');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '',
        };
    }

    /**
     * setting parameters from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        this._basicLiteralType = jsonNode.basic_literal_type;
        this._basicLiteralValue = jsonNode.basic_literal_value;
    }

    setExpressionFromString(expression, callback) {
        if (!_.isNil(expression)) {
            const fragment = FragmentUtils.createExpressionFragment(expression);
            const parsedJson = FragmentUtils.parseFragment(fragment);
            if ((!_.has(parsedJson, 'error')
                    || !_.has(parsedJson, 'syntax_errors'))
                    && _.isEqual(parsedJson.type, 'basic_literal_expression')) {
                this.initFromJson(parsedJson);
                if (_.isFunction(callback)) {
                    callback({ isValid: true });
                }
            } else if (_.isFunction(callback)) {
                callback({ isValid: false, response: parsedJson });
            }
        }
    }

    getExpressionString() {
        if (this._basicLiteralType === 'string') {
            // Adding double quotes if it is a string.
            return '"' + this._basicLiteralValue + '"' + this.getWSRegion(1);
        }
        return this._basicLiteralValue + this.getWSRegion(1);
    }

    getBasicLiteralValue() {
        return this._basicLiteralValue;
    }

    getBasicLiteralType() {
        return this._basicLiteralType;
    }
}

export default BasicLiteralExpression;
