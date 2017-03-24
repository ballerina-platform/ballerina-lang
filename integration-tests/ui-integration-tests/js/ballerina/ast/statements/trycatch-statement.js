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
define(['lodash', 'log', './statement', './try-statement', './catch-statement'],
       function (_, log, Statement, TryStatement, CatchStatement) {

    /**
     * Class for try-catch statement in ballerina.
     * @constructor
     */
    var TryCatchStatement = function (args) {
        Statement.call(this);
        this.type = "TryCatchStatement";
    };

    TryCatchStatement.prototype = Object.create(Statement.prototype);
    TryCatchStatement.prototype.constructor = TryCatchStatement;

    /**
     * setter for catch block exception
     * @param exception
     */
    TryCatchStatement.prototype.setExceptionType = function (exception, options) {
        if (!_.isNil(exception)) {
            this.setAttribute('_exceptionType', exception, options);
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


    TryCatchStatement.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return TryCatchStatement;
});
