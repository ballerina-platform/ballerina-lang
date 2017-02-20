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
define(['lodash', 'log', './conditional-statement', './argument'], function (_, log, ConditionalStatement, Argument) {

    /**
     * Class for catch statement in ballerina.
     * @class CatchStatement
     * @constructor
     * @extends ConditionalStatement
     */
    var CatchStatement = function (args) {
        ConditionalStatement.call(this);
        this._parameter = _.get(args, "parameter", "exception e");

        this.type = "CatchStatement";
    };

    CatchStatement.prototype = Object.create(ConditionalStatement.prototype);
    CatchStatement.prototype.constructor = CatchStatement;

    CatchStatement.prototype.setParameter = function (parameter, options) {
        if (!_.isNil(parameter)) {
            this.setAttribute('_parameter', parameter, options);
        }
    };

    CatchStatement.prototype.getParameter = function () {
        return this._parameter;
    };

    CatchStatement.prototype.initFromJson = function (jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return CatchStatement;
});
