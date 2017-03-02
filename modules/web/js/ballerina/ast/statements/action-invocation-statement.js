/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['lodash', 'log', './statement'], function (_, log, Statement) {

    /**
     * Class to represent a action invocation to ballerina.
     * @param args
     * @constructor
     */
    var ActionInvocationStatement = function (args) {
        Statement.call(this);
        this.type = "ActionInvocationStatement";
    };

    ActionInvocationStatement.prototype = Object.create(Statement.prototype);
    ActionInvocationStatement.prototype.constructor = ActionInvocationStatement;

    /**
     * initialize ActionInvocationStatement from json object
     * @param {Object} jsonNode to initialize from
     */
    ActionInvocationStatement.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    /**
     * Get the statement string
     * @return {string} statement string
     */
    ActionInvocationStatement.prototype.getStatementString = function () {
        if (this.getChildren().length > 0) {
            return this.getChildren()[0].getExpression();
        }
    };

    /**
     * Set the statement string
     * @param {string} statementString
     */
    ActionInvocationStatement.prototype.setStatementString = function (statementString, options) {
        if (this.getChildren().length > 0) {
            this.getChildren()[0].setExpression(statementString);
        }
    };

    return ActionInvocationStatement;

});