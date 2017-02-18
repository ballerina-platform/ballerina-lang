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
     * Class for throw statement in ballerina.
     * @param expression one expression for a throw statement.
     * @constructor
     */
    var ThrowStatement = function (args) {
        Statement.call(this, 'ThrowStatement');
        this.type = "ThrowStatement";
    };

    ThrowStatement.prototype = Object.create(Statement.prototype);
    ThrowStatement.prototype.constructor = ThrowStatement;

    ThrowStatement.prototype.setExpression = function (expression, options) {
        if (!_.isNil(expression)) {
            this.setAttribute('_expression', expression, options);
        } else {
            log.error("Cannot set undefined to the throw statement.");
        }
    };

    /**
     * Set the throw statement string
     * @param {string} throwString
     */
    ThrowStatement.prototype.setStatementString = function (statementString, options) {
        this.getChildren()[0].setExpression(statementString.split('throw ')[1]);
    };

    /**
     * Get the throw statement string
     * @return {string} throw statement string
     */
    ThrowStatement.prototype.getStatementString = function () {
        return 'throw ' + this.getChildren()[0].getExpression();
    };

    /**
     * initialize ThrowStatement from json object
     * @param {Object} jsonNode to initialize from
     */
    ThrowStatement.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return ThrowStatement;
});
