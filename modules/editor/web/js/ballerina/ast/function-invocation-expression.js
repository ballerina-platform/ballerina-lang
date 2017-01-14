/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
     * Constructor for FunctionInvocationExpression
     * @param {Object} args - Arguments to create the FunctionInvocationExpression
     * @constructor
     */
    var FunctionInvocationExpression = function (args) {
        Statement.call(this, 'FunctionInvocationExpression');
        this._functionName = _.get(args, 'functionName', 'newFunction');
    }

    FunctionInvocationExpression.prototype = Object.create(Statement.prototype);
    FunctionInvocationExpression.prototype.constructor = FunctionInvocationExpression;

    FunctionInvocationExpression.prototype.setFunctionName = function (functionName) {
        this._functionName = functionName;
    };

    FunctionInvocationExpression.prototype.getFunctionName = function () {
        return this._functionName;
    };

    /**
     * setting parameters from json
     * @param jsonNode
     */
    FunctionInvocationExpression.prototype.initFromJson = function (jsonNode) {
        var functionNameSplit = jsonNode.function_name.split(":");
        var params = "";
        this.getParent().setFunctionName(functionNameSplit[1]);
        this.getParent().setPackageName(functionNameSplit[0]);
        this.setFunctionName(jsonNode.function_name);

        for (var itr = 0; itr < jsonNode.children.length; itr ++) {
            params += jsonNode.children[itr].variable_reference_name;
            if (itr !== jsonNode.children.length - 1) {
                params += ",";
            }
        }
        this.getParent().setParams(params);
    };

    return FunctionInvocationExpression;
});
