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
        this._isPublic = _.get(args, "isPublic") || false;
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

    FunctionDefinition.prototype.setArguments = function (args) {
        if (!_.isNil(name)) {
            this._arguments = args;
        }
    };

    FunctionDefinition.prototype.setIsPublic = function(isPublic){
        if(!_.isNil(isPublic)){
            this._isPublic = isPublic;
        }
    };

    FunctionDefinition.prototype.getFunctionName = function () {
        return this._functionName;
    };

    FunctionDefinition.prototype.getArguments = function () {
        return this._arguments;
    };

    FunctionDefinition.prototype.getIsPublic = function () {
        return this._isPublic;
    };

    /**
     * Returns the list of arguments as a string separated by commas.
     * @return {string} - Arguments as string.
     */
    FunctionDefinition.prototype.getArgumentsAsString = function () {
        var argsAsString = "";
        var args = this._arguments;
        _.forEach(this._arguments, function(argument, index){
            argsAsString += argument.type + " ";
            argsAsString += argument.identifier;
            if (args.length - 1 != index) {
                argsAsString += ", ";
            }
        });

        return argsAsString;
    };

    /**
     * Adds new argument to the function definition.
     * @param type - The type of the argument.
     * @param identifier - The identifier of the argument.
     */
    FunctionDefinition.prototype.addArgument = function(type, identifier) {
        this._arguments.push({
            type: type,
            identifier: identifier
        })
    };

    /**
     * Removes an argument from a function definition.
     * @param identifier - The identifier of the argument.
     * @return {Array} - The removed argument.
     */
    FunctionDefinition.prototype.removeArgument = function(identifier) {
       return  _.remove(this._arguments, function(functionArg) {
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

    /**
     * initialize from json
     * @param jsonNode
     */
    FunctionDefinition.prototype.initFromJson = function (jsonNode) {
        this._functionName = jsonNode.function_name;
        this._annotations = jsonNode.annotations;
        this._isPublic = jsonNode.is_public_function;

        var self = this;
        var BallerinaASTFactory = this.getFactory();

        _.each(jsonNode.children, function (childNode) {
            var child = BallerinaASTFactory.createFromJson(childNode);
            self.addChild(child);
        });
    };

    return FunctionDefinition;

});