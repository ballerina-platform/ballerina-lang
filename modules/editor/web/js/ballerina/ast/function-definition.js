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
define(['lodash', './callable-definition', './connector-declaration'], function (_, CallableDefinition, ConnectorDeclaration) {

    var FunctionDefinition = function (args) {
        this.id = autoGenerateId();
        CallableDefinition.call(this, 'Function');
        this._functionName = _.get(args, 'functionName') || 'newFunction';
        this._functionArguments = _.get(args, "functionArgs", []);
    };

    FunctionDefinition.prototype = Object.create(CallableDefinition.prototype);
    FunctionDefinition.prototype.constructor = FunctionDefinition;

    // Auto generated Id for service definitions (for accordion views)
    function autoGenerateId(){
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    }

    FunctionDefinition.prototype.setFunctionName = function(name){
        if(!_.isNil(name)){
            this._functionName = name;
        }
    };

    FunctionDefinition.prototype.setFunctionArguments = function (args) {
        if (!_.isNil(name)) {
            this._functionArguments = args;
        }
    };

    FunctionDefinition.prototype.getFunctionName = function () {
        return this._functionName;
    };

    FunctionDefinition.prototype.getFunctionArguments = function () {
        return this._functionArguments;
    };

    FunctionDefinition.prototype.getFunctionArgumentsAsString = function () {
        var functionArgsAsString = "";
        var functionArgs = this._functionArguments;
        _.forEach(this._functionArguments, function(argument, index){
            functionArgsAsString += argument.type + " ";
            functionArgsAsString += argument.identifier;
            if (functionArgs.length - 1 != index) {
                functionArgsAsString += ", ";
            }
        });

        return functionArgsAsString;
    };

    FunctionDefinition.prototype.addFunctionArgument = function(type, identifier) {
        this._functionArguments.push({
            type: type,
            identifier: identifier
        })
    };

    FunctionDefinition.prototype.removeFunctionArgument = function(identifier) {
        _.remove(this._functionArguments, function(functionArg) {
            return functionArg.identifier === identifier;
        });
    };

    /**
     * Override the super call to addChild
     * @param child
     * @param index
     */
    FunctionDefinition.prototype.addChild = function (child, index) {
        if (child instanceof ConnectorDeclaration) {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, 0);
        } else {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, index);
        }
    };
    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    FunctionDefinition.prototype.canBeParentOf = function (node) {
        var BallerinaASTFactory = this.getFactory();
        return BallerinaASTFactory.isConnectorDeclaration(node)
            || BallerinaASTFactory.isVariableDeclaration(node)
            || BallerinaASTFactory.isWorkerDeclaration(node)
            || BallerinaASTFactory.isStatement(node);
    };

    return FunctionDefinition;

});