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
define(['lodash', 'log', './statement', './expression'], function (_, log, Statement, Expression) {

    /**
     * Class to represent a condition to ballerina.
     * @param condition The condition of a conditional statement.
     * @param statements The statements of a conditional statement.
     * @constructor
     */
    var ConditionalStatement = function (condition, statements) {
        Statement.call(this);
        this._condition = condition || "true";
        this._statments = statements || [];
        this.type = "ConditionalStatement";
    };

    ConditionalStatement.prototype = Object.create(Statement.prototype);
    ConditionalStatement.prototype.constructor = ConditionalStatement;

    ConditionalStatement.prototype.setCondition = function (condition, options) {
        if (!_.isNil(condition) && condition instanceof Expression) {
            this.setAttribute('_condition', condition, options);
        } else {
            log.error("Invalid value for condition received: " + condition);
        }
    };

    ConditionalStatement.prototype.getCondition = function () {
        return this._condition;
    };

    ConditionalStatement.prototype.setStatements = function (statements, options) {
        // There should be atleast one statement.
        if (!_.isNil(statements)) {
            this.setAttribute('_statments', statements, options);
        } else {
            log.error("Cannot set undefined array of statements.");
        }
    };

    ConditionalStatement.prototype.getStatements = function () {
        return this._statments;
    };

    return ConditionalStatement;

});
