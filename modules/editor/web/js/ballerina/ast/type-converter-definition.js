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

    var TypeConverterDefinition = function (args) {
        ASTNode.call(this, 'TypeConverterDefinition');
        this._typeConverterName = _.get(args, 'typeConverterName', 'newTypeConverter');
        this.BallerinaASTFactory = this.getFactory();
    };

    TypeConverterDefinition.prototype = Object.create(ASTNode.prototype);
    TypeConverterDefinition.prototype.constructor = TypeConverterDefinition;

    /**
     * Set the type converter name
     * @param typeConverterName
     */
    TypeConverterDefinition.prototype.setTypeConverterName = function (typeConverterName) {
        if (!_.isNil(typeConverterName)) {
            this._typeConverterName = typeConverterName;
        }
    };

    /**
     * returns the type converter name
     * @returns {string} type converter name
     */
    TypeConverterDefinition.prototype.getTypeConverterName = function () {
        return this._typeConverterName;
    };

    /**
     * return variable declarations
     * @returns {Array} variable declarations array
     */
    TypeConverterDefinition.prototype.getVariableDeclarations = function () {
        var variableDeclarations = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isVariableDeclaration(child)) {
                variableDeclarations.push(child);
            }
        });
        return variableDeclarations;
    };

    /**
     * Add variable declaration
     * @param newVariableDeclaration
     */
    TypeConverterDefinition.prototype.addVariableDeclaration = function (newVariableDeclaration) {
        // Get the index of the last variable declaration.
        var self = this;

        var index = _.findLastIndex(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child);
        });

        // index = -1 when there are not any variable declarations, hence get the index for connector
        // declarations.
        if (index == -1) {
            index = _.findLastIndex(this.getChildren(), function (child) {
                return self.BallerinaASTFactory.isConnectorDeclaration(child);
            });
        }

        this.addChild(newVariableDeclaration, index + 1);
    };

    /**
     * Remove variable declaration
     * @param variableDeclarationIdentifier
     */
    TypeConverterDefinition.prototype.removeVariableDeclaration = function (variableDeclarationIdentifier) {
        var self = this;
        // Removing the variable from the children.
        var variableDeclarationChild = _.find(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child)
                && child.getIdentifier() === variableDeclarationIdentifier;
        });
        this.removeChild(variableDeclarationChild);
    };

    /**
     * Gets the return type model. A type converter definition can have only one {ReturnType} model.
     * @return {ReturnType|undefined} - The return type model.
     */
    TypeConverterDefinition.prototype.getReturnTypeModel = function() {
        var self = this;
        var returnTypeModel = undefined;
        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isReturnType(child)) {
                returnTypeModel = child;
                // break
                return false;
            }
        });
        return returnTypeModel;
    };

    /**
     * Gets the defined return types.
     * @return {Argument[]} - Array of args.
     */
    TypeConverterDefinition.prototype.getReturnTypes = function () {
        var returnTypeModel = this.getReturnTypeModel();
        return !_.isUndefined(returnTypeModel) ? this.getReturnTypeModel().getChildren().slice(0) : [];
    };

    /**
     * Gets the return type as a string separated by commas.
     * @return {string} - Return types.
     */
    TypeConverterDefinition.prototype.getReturnTypesAsString = function () {
        var returnTypes = [];
        _.forEach(this.getReturnTypes(), function (returnTypeChild) {
            returnTypes.push(returnTypeChild.getArgumentAsString())
        });

        return _.join(returnTypes, ", ");
    };

    /**
     * returns the arguments array
     * @returns {Array} arguments array
     */
    TypeConverterDefinition.prototype.getArguments = function () {
        var functionArgs = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isArgument(child)) {
                functionArgs.push(child);
            }
        });
        return functionArgs;
    };

    /**
     * Returns the list of arguments as a string separated by commas.
     * @return {string} - Arguments as string.
     */
    TypeConverterDefinition.prototype.getArgumentsAsString = function () {
        var argsAsString = "";
        var args = this.getArguments();
        _.forEach(args, function(argument, index){
            argsAsString += argument.type + " ";
            argsAsString += argument.identifier;
            if (args.length - 1 != index) {
                argsAsString += ", ";
            }
        });

        return argsAsString;
    };

    return TypeConverterDefinition;
});