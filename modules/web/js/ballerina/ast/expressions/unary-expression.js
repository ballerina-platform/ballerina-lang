/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import _ from 'lodash';
import Expression from './expression';
import FragmentUtils from '../../utils/fragment-utils';

/**
 * Construct for UnaryExpression
 * @param {Object} args - Arguments to create the Unary Expression
 * @constructor
 * @augments Expression
 * */
class UnaryExpression extends Expression {
    constructor(args) {
        super('UnaryExpression');
        this._operator = _.get(args, 'operator');
    }

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     * */
    initFromJson(jsonNode) {
        if(!_.isEmpty(jsonNode.children)) {
            jsonNode.children.forEach((childNode) => {
                var child = this.getFactory().createFromJson(childNode);
                child.initFromJson(childNode);
                this.addChild(child);
            });
        }
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
                   || !_.has(parsedJson, 'syntax_errors'))) {
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
        let expString = this.getOperator();
        expString += (!_.isEmpty(this.children)) ? this.children[0].getExpressionString() : '';
        return expString;
    }

    /**
     * Get the operator.
     * @return {String} Operator
     * */
    getOperator() {
        return this._operator;
    }
}

export default UnaryExpression;
