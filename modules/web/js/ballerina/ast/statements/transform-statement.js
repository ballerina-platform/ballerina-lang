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
 * Class to represent an Assignment statement.
 * @constructor
 */
class TransformStatement extends Statement {
    constructor(args) {
        super(args);
        this.type = 'TransformStatement';
    }

    /**
     * initialize AssignmentStatement from json object
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        var self = this;

        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * Override the removeChild function
     * @param {ASTNode} child - child node
     */
    removeChild(child) {
        this.getParent().removeChild(child);
    }

    /**
     * Get the assignment statement string
     * @return {string} assignment statement string
     */
    getStatementString() {
        return 'transform';
    }

    // /**
    //  * Set the assignment statement string
    //  * @param {string} statementString
    //  */
    // setStatementString(statementString, options) {
    //     var equalIndex = _.indexOf(statementString, '=');
    //     var leftOperand = statementString.substring(0, equalIndex);
    //     var rightOperand = statementString.substring(equalIndex + 11); //'= transform'
    //     this.getChildren()[0].setLeftOperandExpressionString(_.isNil(leftOperand) ? 'leftExpression' : leftOperand, options);
    //     this.getChildren()[1].setRightOperandExpressionString(_.isNil(rightOperand) ? 'rightExpression' : rightOperand, options);
    // }
}

export default TransformStatement;
