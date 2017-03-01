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
define(['lodash', './statement'], function (_, Statement) {
    /**
     * Class to represent an Assignment statement.
     * @constructor
     */
    var AssignmentStatement = function (args) {
        Statement.call(this, 'AssignmentStatement');
        this._variableAccessor = _.get(args, 'accessor', 'var1');
        this._fullPackageName = _.get(args, 'fullPackageName', '');
    };

    AssignmentStatement.prototype = Object.create(Statement.prototype);
    AssignmentStatement.prototype.constructor = AssignmentStatement;

    /**
     * initialize AssignmentStatement from json object
     * @param {Object} jsonNode to initialize from
     */
    AssignmentStatement.prototype.initFromJson = function (jsonNode) {
        var self = this;

        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    /**
     * Override the removeChild function
     * @param {ASTNode} child - child node
     */
    AssignmentStatement.prototype.removeChild = function (child) {
        this.getParent().removeChild(this);
    };

    /**
     * Get the assignment statement string
     * @return {string} assignment statement string
     */
    AssignmentStatement.prototype.getStatementString = function () {
        return (!_.isNil(this.getChildren()[0].getLeftOperandExpressionString())
                ? this.getChildren()[0].getLeftOperandExpressionString() : "leftExpression") + " = " +
            (!_.isNil(this.getChildren()[1].getRightOperandExpressionString())
                ? this.getChildren()[1].getRightOperandExpressionString() : "rightExpression");
    };

    /**
     * Set the assignment statement string
     * @param {string} statementString
     */
    AssignmentStatement.prototype.setStatementString = function (statementString, options) {
        var equalIndex = _.indexOf(statementString, '=');
        var leftOperand = statementString.substring(0, equalIndex);
        var rightOperand = statementString.substring(equalIndex + 1);
        this.getChildren()[0].setLeftOperandExpressionString(_.isNil(leftOperand) ? "leftExpression" : leftOperand, options);
        this.getChildren()[1].setRightOperandExpressionString(_.isNil(rightOperand) ? "rightExpression" : rightOperand, options);
    };

    /**
     * Set the full package name.
     * @param {String} fullPkgName full package name
     * @param {Object} options
     * */
    AssignmentStatement.prototype.setFullPackageName = function (fullPkgName, options) {
        this.setAttribute('_fullPackageName', fullPkgName, options);
    };

    /**
     * Get full package name.
     * @return {String} full package name
     * */
    AssignmentStatement.prototype.getFullPackageName = function () {
        return this._fullPackageName;
    };

    return AssignmentStatement;
});
