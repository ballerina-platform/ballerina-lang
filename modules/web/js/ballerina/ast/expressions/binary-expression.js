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
import ASTFactory from '../ast-factory.js';

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
            const leftExpression = ASTFactory.createFromJson(jsonNode.children[0]);
            leftExpression.setParent(this, { doSilently: true });
            leftExpression.initFromJson(jsonNode.children[0]);
            this.setLeftExpression(leftExpression, { doSilently: true });
        }
        if (!_.isNil(jsonNode.children[1])) {
            const rightExpression = ASTFactory.createFromJson(jsonNode.children[1]);
            rightExpression.setParent(this, { doSilently: true });
            rightExpression.initFromJson(jsonNode.children[1]);
            this.setRightExpression(rightExpression, { doSilently: true });
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

    getExpressionString(isTemplte) {
        let expressionString = '';
        expressionString += (!_.isNil(this.getLeftExpression()))
            ? this.getLeftExpression().getExpressionString(isTemplte) : '';
        // default tailing whitespace of expressions is emtpy - hence we need to
        // append a space here
        expressionString += (!_.isNil(this.getLeftExpression()) && this.getLeftExpression().whiteSpace.useDefault)
            ? ' ' : '';
        if (!isTemplte) {
            expressionString += this._operator + this.getWSRegion(2);
        }
        expressionString += (!_.isNil(this.getRightExpression()))
            ? this.getRightExpression().getExpressionString(isTemplte) : '';
        return expressionString;
    }

    setOperator(operator, options) {
        this.setAttribute('_operator', operator, options);
    }

    getOperator() {
        return this._operator;
    }

    setLeftExpression(leftExpression, options) {
        this.setAttribute('_leftExpression', leftExpression, options);
    }

    getLeftExpression() {
        return this._leftExpression;
    }

    setRightExpression(rightExpression, options) {
        this.setAttribute('_rightExpression', rightExpression, options);
    }

    getRightExpression() {
        return this._rightExpression;
    }
}

export default BinaryExpression;
