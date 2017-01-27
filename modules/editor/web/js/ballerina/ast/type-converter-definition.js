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
     * Gets the return type
     * @return {string} - Return type.
     */
    TypeConverterDefinition.prototype.getReturnType = function () {
        var returnType = "";
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isTypeStructDefinition(child) && child._category === "TARGET") {
                returnType = child.getTypeStructName();
            }
        });
        return returnType;
    };

    /**
     * returns the argument
     * @returns {String} argument
     */
    TypeConverterDefinition.prototype.getSourceAndIdentifier = function () {
       var sourceAndIdentifier = "";
       var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isTypeStructDefinition(child) && child._category === "SOURCE") {
                sourceAndIdentifier = child.getTypeStructName() + " " + child.getIdentifier();
            }
        });
        return sourceAndIdentifier;
    };

    return TypeConverterDefinition;
});