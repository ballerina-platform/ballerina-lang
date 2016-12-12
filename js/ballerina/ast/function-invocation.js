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
define(['lodash', './node'], function (_, ASTNode) {

    /**
     * Class to represent an assignment in ballerina.
     * @constructor
     */
    var Assignment = function (args) {
        this._packageName = _.get(args, 'package', 'default');
        this._functionName = _.get(args, 'function', 'default');
        this._params = _.get(args, 'params', '');
        ASTNode.call(this, 'Assignment');
    };

    Assignment.prototype = Object.create(ASTNode.prototype);
    Assignment.prototype.constructor = Assignment;

    Assignment.prototype.setPackage = function (packageName) {
        this._packageName = packageName;
    };

    Assignment.prototype.setFunctionName = function (functionName) {
        this._functionName = functionName;
    };

    Assignment.prototype.setParams = function (params) {
        this._params = params;
    };

    Assignment.prototype.getPackageName = function () {
        return this._packageName;
    };

    Assignment.prototype.getFunctionName = function () {
        return this._functionName;
    };

    Assignment.prototype.getParams = function () {
        return this._params;
    };

    return Assignment;
});
