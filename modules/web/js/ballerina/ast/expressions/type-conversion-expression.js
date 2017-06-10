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
import log from 'log';
import FragmentUtils from '../../utils/fragment-utils';

class TypeConversionExpression extends Expression {
    constructor(args) {
        super('TypeConversionExpression');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '',
            2: ' ',
            3: ' '
        };
        this._targetType = _.get(args, 'targetType');
    }

    setTargetType(targetType, options) {
        if (!_.isNil(targetType)) {
            this.setAttribute('_targetType', targetType, options);
        }
    }

    getTargetType() {
        return this._targetType;
    }

    initFromJson(jsonNode) {
        this.children = [];
        let targetType = this.getFactory().createFromJson(jsonNode.target_type);
        targetType.initFromJson(jsonNode.target_type);
        this.setTargetType(targetType, {doSilently: true});
        _.each(jsonNode.children, (childNode) => {
            var child = this.getFactory().createFromJson(childNode);
            child.initFromJson(childNode);
            this.addChild(child, undefined, true, true);
        });
    }

    /**
     * Set the expression from the expression string
     * @param {string} expressionString
     * @override
     */
    setExpressionFromString(expression, callback) {
        if(!_.isNil(expression)){
            let fragment = FragmentUtils.createExpressionFragment(expression);
            let parsedJson = FragmentUtils.parseFragment(fragment);
            if ((!_.has(parsedJson, 'error')
                   || !_.has(parsedJson, 'syntax_errors'))
                   && _.isEqual(parsedJson.type, 'type_conversion_expression')) {
                this.initFromJson(parsedJson);
                if (_.isFunction(callback)) {
                    callback({isValid: true});
                }
            } else {
                if (_.isFunction(callback)) {
                    callback({isValid: false, response: parsedJson});
                }
            }
        }
    }

    getExpressionString() {
        var expString = '';
        expString += '<' + this.getWSRegion(1) + this.getTargetType().toString() + '>'
                + this.getWSRegion(2) + this.children[0].getExpressionString();
        return expString;
    }
}

export default TypeConversionExpression;
