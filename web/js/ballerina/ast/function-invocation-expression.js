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
define(['lodash', './expression', './function-invocation'], function (_, Expression, FunctionInvocation) {

    /**
     * Constructor for FunctionInvocationExpression
     * @param {Object} args - Arguments to create the FunctionInvocationExpression
     * @constructor
     * @augments Expression
     */
    var FunctionInvocationExpression = function (args) {
        Expression.call(this, 'FunctionInvocationExpression');
        this._functionName = _.get(args, 'functionName', 'newFunction');
    }

    FunctionInvocationExpression.prototype = Object.create(Expression.prototype);
    FunctionInvocationExpression.prototype.constructor = FunctionInvocationExpression;

    FunctionInvocationExpression.prototype.setFunctionName = function (functionName) {
        this._functionName = functionName;
    };

    FunctionInvocationExpression.prototype.getFunctionName = function () {
        return this._functionName;
    };

    /**
     * Creating the function invocation statement which invoked by the parsed code.
     * @param {Object} jsonNode - A node explaining the structure of a function invocation.
     * @param {string} jsonNode.type - The type of this current node. The value would be
     * "function_invocation_expression";
     * @param {string} jsonNode.function_name - The body of the function information. Example : "system:println".
     * @param {Object[]} jsonNode.children - The arguments of the function invocation.
     */
    FunctionInvocationExpression.prototype.initFromJson = function (jsonNode) {
        var functionNameSplit = jsonNode.function_name.split(":");
        var argsString = "";
        this.setFunctionName(jsonNode.function_name);
        argsString += this._generateArgsString(jsonNode, argsString, ", ");

        // TODO : need to remove following if/else by delegating this logic to parent(FunctionInvocation)
        if( this.getParent() instanceof FunctionInvocation){
            if(functionNameSplit.length < 2){
                //there is only a function name
                this.getParent().setFunctionName(functionNameSplit[0]);
            } else {
                this.getParent().setFunctionName(functionNameSplit[1]);
                this.getParent().setPackageName(functionNameSplit[0]);
            }
            this.getParent().setParams(argsString);
        }else{
            this.setExpression(jsonNode.function_name + '(' + argsString +')');
        }

    };

    /**
     * Generates the arguments passed to a function as a string.
     * @param {Object} jsonNode - A node explaining the structure function argument.
     * @param {string} argsString - The argument string. This string is used for appending the generated arg strings.
     * @param {string} separator - The separator between args.
     * @return {string} - Arguments as a string.
     * @private
     */
    FunctionInvocationExpression.prototype._generateArgsString = function (jsonNode, argsString, separator) {
        var self = this;

        for (var itr = 0; itr < jsonNode.children.length; itr++) {
            var childJsonNode = jsonNode.children[itr];
            if (childJsonNode.type == "basic_literal_expression") {
                if(childJsonNode.basic_literal_type == "string") {
                    // Adding double quotes if it is a string.
                    argsString += "\"" + childJsonNode.basic_literal_value + "\"";
                } else {
                    argsString += childJsonNode.basic_literal_value;
                }
            } else if (childJsonNode.type == "variable_reference_expression") {
                argsString += childJsonNode.variable_reference_name;
            } else if (childJsonNode.type == "add_expression") {
                argsString += self._generateArgsString(childJsonNode, argsString, " + ");
            } else if (childJsonNode.type == "subtract_expression") {
                argsString += self._generateArgsString(childJsonNode, argsString, " - ");
            } else if (childJsonNode.type == "function_invocation_expression") {
                var child = self.getFactory().createFromJson(childJsonNode);
                child.initFromJson(childJsonNode);
                argsString += self._generateArgsString(childJsonNode, child.getExpression(), " - ");
            }

            if (itr !== jsonNode.children.length - 1) {
                argsString += separator;
            }
        }
        return argsString;
    };

    return FunctionInvocationExpression;
});