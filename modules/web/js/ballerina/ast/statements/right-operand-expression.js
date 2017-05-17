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
import Statement from './statement';

/**
 * Constructor for RightOperandExpression
 * @param {Object} args - Arguments to create the RightOperandExpression
 * @constructor
 */
class RightOperandExpression extends Statement {
    constructor(args) {
        super('RightOperandExpression');
        this._right_operand_expression_string = undefined;
    }

    generateExpression() {
        var expression = '';
        _.forEach(this.getChildren(), child => {
            expression += child.generateExpression();
        });
        this._right_operand_expression_string = expression;
        return expression;
    }

    /**
     * Get Right Operand Expression String
     * @returns {string} - The expression
     */
    getRightOperandExpressionString() {
        return this._right_operand_expression_string;
    }

    /**
     * Set Right Operand Expression String
     * @param {string} rightOperandExpStr - The expression
     */
    setRightOperandExpressionString(rightOperandExpStr, options) {
        this.setAttribute('_right_operand_expression_string', rightOperandExpStr, options);
    }

    /**
     * Override the removeChild function
     * @param {ASTNode} child - child node
     */
    removeChild(child) {
        this.getParent().removeChild(this);
    }

    /**
     * setting parameters from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
            self.setRightOperandExpressionString(child.getExpression(), {doSilently: true});
        });
    }
}

export default RightOperandExpression;

