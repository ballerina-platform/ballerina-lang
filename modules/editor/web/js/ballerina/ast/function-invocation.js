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
define(['lodash', './statement'], function (_, Statement) {

    /**
     * Class to represent a function invocation in ballerina.
     * @constructor
     */
    var FunctionInvocation = function (args) {
        this._packageName = _.get(args, 'package', 'pkg');
        this._functionName = _.get(args, 'function', 'default');
        this._params = _.get(args, 'params');
        Statement.call(this, 'FunctionInvocation');
    };

    FunctionInvocation.prototype = Object.create(Statement.prototype);
    FunctionInvocation.prototype.constructor = FunctionInvocation;

    FunctionInvocation.prototype.setPackageName = function (packageName) {
        this._packageName = packageName;
    };

    FunctionInvocation.prototype.setFunctionName = function (functionName) {
        this._functionName = functionName;
    };

    FunctionInvocation.prototype.setParams = function (params) {
        this._params = params;
    };

    FunctionInvocation.prototype.getPackageName = function () {
        return this._packageName;
    };

    FunctionInvocation.prototype.getFunctionName = function () {
        return this._functionName;
    };

    FunctionInvocation.prototype.getParams = function () {
        var params;
        if(!_.isUndefined(this._params)) {
            params = this._params.split(',');
            return params;
        }
        else params = "";
        return params;
    };

    return FunctionInvocation;
});
