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
import Expression from './../expressions/expression';
import FragmentUtils from '../../utils/fragment-utils';

/**
 * Constructor for RightOperandExpression
 * @param {Object} args - Arguments to create the RightOperandExpression
 * @constructor
 */
class RightOperandExpression extends Expression {
    constructor(args) {
        super('RightOperandExpression');
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
                   && _.isEqual(parsedJson.type, 'right_operand_expression')) {
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
        var expression = '';
        _.forEach(this.getChildren(), child => {
            expression += child.getExpressionString();
        });
        return expression;
    }

    /**
     * setting parameters from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        if (!_.isEmpty(jsonNode.children)) {
            jsonNode.children.forEach((childJsonNode) => {
                let child = this.getFactory().createFromJson(childJsonNode);
                this.addChild(child, undefined, true, true);
                child.initFromJson(childJsonNode);
            });
        }
    }
}

export default RightOperandExpression;
