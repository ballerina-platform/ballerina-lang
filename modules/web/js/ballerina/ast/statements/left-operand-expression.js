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
 * Constructor for LeftOperandExpression
 * @param {Object} args - Arguments to create the LeftOperandExpression
 * @constructor
 */
class LeftOperandExpression extends Statement {
    constructor(args) {
        super('LeftOperandExpression');
        this._operand_type = _.get(args, "operandType", "");
        this._left_operand_expression_string = _.get(args, "variableName", "var1");
    }

    /**
     * Get Variable Reference Name
     * @returns {undefined|string}
     */
    getLeftOperandExpressionString() {
        return this._left_operand_expression_string;
    }

    /**
     * Set Variable Reference Name
     * @param {string} leftOperandExpStr left operand expression string
     * @param {Object} options
     */
    setLeftOperandExpressionString(leftOperandExpStr, options) {
        this.setAttribute('_left_operand_expression_string', leftOperandExpStr, options);
    }

    /**
     * Get operand type
     * @return {String} operand type
     * */
    getOperandType() {
        return this._operand_type;
    }

    /**
     * Set operandType
     * @param {String} operandType
     * @param {Object} options
     * */
    setOperandType(operandType, options) {
        this.setAttribute('_operand_type', operandType, options);
    }

    setLeftOperandType(operandType, options) {
        this.setAttribute('_operand_type', operandType.trim(), options);
    }

    generateExpression() {
        var exps = [];
        _.forEach(this.getChildren(), child => {
            exps.push(child.generateExpression());
        });
        let expression = _.join(exps, ',');

        // TODO: This is a temporary fix until the partial expression builder is completed
        if (_.isEmpty(this.getChildren())) {
            expression = this._left_operand_expression_string;
        }
        this._left_operand_expression_string = expression;
        return expression;
    }

    /**
     * setting parameters from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        var self = this;
        var expression = "";
        for (var itr = 0; itr < jsonNode.children.length; itr++) {
            var childJsonNode = jsonNode.children[itr];
            if (childJsonNode.type === 'variable_name') {
                expression += childJsonNode.variable_name;
            } else {
                var child = self.getFactory().createFromJson(childJsonNode);
                self.addChild(child);
                child.initFromJson(childJsonNode);
                expression += child.getExpression();
            }
            if (itr !== jsonNode.children.length - 1) {
                expression += " , ";
            }
        }
        this.setLeftOperandExpressionString(expression, {doSilently: true});
    }
}

export default LeftOperandExpression;

