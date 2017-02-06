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
define(['lodash', 'log', './conditional-statement'], function (_, log, ConditionalStatement) {

    /**
     * Class for if conditions in ballerina. Extended from Conditional-Statement
     * @param {Object} args - Argument object for creating an if statement.
     * @param {string} [args.condition="true"] - The condition for "if".
     * @param {Statement} [args.statements="[]] - Statements of the "if".
     * @constructor
     * @augments ConditionalStatement
     */
    var IfStatement = function (args) {
        ConditionalStatement.call(this);
        this._condition = _.get(args, "condition", "true");
        this._statements = _.get(args, "statements", []);
        this.type = "IfStatement";
    };

    IfStatement.prototype = Object.create(ConditionalStatement.prototype);
    IfStatement.prototype.constructor = IfStatement;

    IfStatement.prototype.setCondition = function(condition, options){
        if(!_.isNil(condition)){
            this.setAttribute('_condition', condition, options);
        }
    };

    IfStatement.prototype.getCondition = function() {
        return this._condition;
    };

    return IfStatement;
});
