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
 * Constructor for BinaryExpression
 * @param {Object} args - Arguments to create the BinaryExpression
 * @constructor
 * @augments Expression
 */
class BinaryExpression extends Expression {
    constructor(args) {
        super('BinaryExpression');
        this._operator = _.get(args, 'operator');
        this._leftExpression = _.get(args, 'leftExpression');
        this._rightExpression = _.get(args, 'rightExpression');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: ' ',
            3: ' ',
        };
    }

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        if (!_.isNil(jsonNode.children[0])) {
            const leftExpression = this.getFactory().createFromJson(jsonNode.children[0]);
            leftExpression.setParent(this, {doSilently: true});
            leftExpression.initFromJson(jsonNode.children[0]);
            this._leftExpression = leftExpression;
        }
        if (!_.isNil(jsonNode.children[1])) {
            const rightExpression = this.getFactory().createFromJson(jsonNode.children[1]);
            rightExpression.setParent(this, {doSilently: true});
            rightExpression.initFromJson(jsonNode.children[1]);
            this._rightExpression = rightExpression;
        }
    }

    setExpressionFromString(expression, callback) {
        if (!_.isNil(expression)) {
            const fragment = FragmentUtils.createExpressionFragment(expression);
            const parsedJson = FragmentUtils.parseFragment(fragment);
            if ((!_.has(parsedJson, 'error')
                    || !_.has(parsedJson, 'syntax_errors'))) {
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
        let expressionString = '';
        expressionString += (!_.isNil(this.getLeftExpression()))
                ? this.getLeftExpression().getExpressionString() : '';
        expressionString += this._operator + this.getWSRegion(2);
        expressionString += (!_.isNil(this.getRightExpression()))
                ? this.getRightExpression().getExpressionString() : '';
        return expressionString;
    }

    setOperator(operator) {
        this._operator = operator;
    }

    getOperator() {
        return this._operator;
    }

    setLeftExpression(leftExpression) {
        this._leftExpression = leftExpression;
    }

    getLeftExpression() {
        return this._leftExpression;
    }

    setRightExpression(rightExpression) {
        this._rightExpression = rightExpression;
    }

    getRightExpression() {
        return this._rightExpression;
    }
}

export default BinaryExpression;
