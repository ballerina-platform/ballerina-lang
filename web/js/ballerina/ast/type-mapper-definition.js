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

    var TypeMapperDefinition = function (args) {
        ASTNode.call(this, 'TypeMapperDefinition');
        this._typeMapperName = _.get(args, 'typeMapperName', 'newTypeMapper');
        this.BallerinaASTFactory = this.getFactory();
    };

    TypeMapperDefinition.prototype = Object.create(ASTNode.prototype);
    TypeMapperDefinition.prototype.constructor = TypeMapperDefinition;

    /**
     * Set the type mapper name
     * @param typeMapperName
     */
    TypeMapperDefinition.prototype.setTypeMapperName = function (typeMapperName) {
        if (!_.isNil(typeMapperName)) {
            this._typeMapperName = typeMapperName;
        }
    };

    /**
     * returns the type mapper name
     * @returns {string} type mapper name
     */
    TypeMapperDefinition.prototype.getTypeMapperName = function () {
        return this._typeMapperName;
    };

    /**
     * return variable declarations
     * @returns {Array} variable declarations array
     */
    TypeMapperDefinition.prototype.getVariableDeclarations = function () {
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
    TypeMapperDefinition.prototype.addVariableDeclaration = function (newVariableDeclaration) {
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
    TypeMapperDefinition.prototype.removeVariableDeclaration = function (variableDeclarationIdentifier) {
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
    TypeMapperDefinition.prototype.getReturnType = function () {
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
    TypeMapperDefinition.prototype.getSourceAndIdentifier = function () {
       var sourceAndIdentifier = "";
       var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isTypeStructDefinition(child) && child._category === "SOURCE") {
                sourceAndIdentifier = child.getTypeStructName() + " " + child.getIdentifier();
            }
        });
        return sourceAndIdentifier;
    };

    return TypeMapperDefinition;
});