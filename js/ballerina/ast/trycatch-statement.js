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
     * Class for try-catch statement in ballerina.
     * @constructor
     */
    var TryCatchStatement = function () {
        Statement.call(this);
    };

    TryCatchStatement.prototype = Object.create(Statement.prototype);
    TryCatchStatement.prototype.constructor = TryCatchStatement;

    /**
     * setter for catch block exception
     * @param exception
     */
    TryCatchStatement.prototype.setExceptionType = function (exception) {
        if (!_.isNil(exception)) {
            this._exceptionType = exception;
        } else {
            log.error("Cannot set undefined to the exception.");
        }
    };

    /**
    * getter for catch block exception type
    */
    TryCatchStatement.prototype.getExceptionType = function() {
        return this._exceptionType;
    };

    return TryCatchStatement;
});