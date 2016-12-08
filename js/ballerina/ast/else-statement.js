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
     * Class for else node in ballerina.
     * @param elseStatements The 'else' statements of an IF condition.
     * @constructor
     */
    var ElseStatement = function (statements) {
        this._elseStatements = statements || [];
    };

    ElseStatement.prototype = Object.create(Statement.prototype);
    ElseStatement.prototype.constructor = ElseStatement;

//    ConditionalStatement.prototype.setElseStatements = function (elseStatements) {
//        if (!_.isNil(elseStatements)) {
//            this._elseStatements = elseStatements;
//        } else {
//            log.error("Cannot set undefined array of else statements.");
//        }
//    };
//
//    ConditionalStatement.prototype.getElseStatements = function () {
//        return this._elseStatements;
//    };
//
//    ConditionalStatement.prototype.setElseIfStatements = function (elseIfStatements) {
//        if (!_.isNil(elseIfStatements)) {
//            this._elseIfStatements = elseIfStatements;
//        } else {
//            log.error("Cannot set undefined array of else if statements.");
//        }
//    };
//
//    ConditionalStatement.prototype.getElseIfStatements = function () {
//        return this._elseIfStatements;
//    };

    return ElseStatement;
});