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
define(['lodash', './node'], function (_, ASTNode) {
    var StructDefinition = function (args) {
        ASTNode.call(this, 'StructDefinition');
        this._structName = _.get(args, 'structName', 'newStruct');
        this.BallerinaASTFactory = this.getFactory();
    };

    StructDefinition.prototype = Object.create(ASTNode.prototype);
    StructDefinition.prototype.constructor = StructDefinition;

    /**
     * setter for struct name
     * @param structName - name of the struct
     */
    StructDefinition.prototype.setStructName = function (structName) {
        this._structName = structName;
    };

    /**
     * getter for struct name
     * @returns {string} struct name
     */
    StructDefinition.prototype.getStructName = function () {
        return this._structName;
    };


    StructDefinition.prototype.getVariableDeclarations = function () {
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
     * Adds new variable declaration.
     */
    StructDefinition.prototype.addVariableDeclaration = function (newVariableDeclaration) {
        var self = this;

        // Get the index of the last variable declaration.
        var index = _.findLastIndex(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child);
        });

        this.addChild(newVariableDeclaration, index + 1);
    };

    /**
     * Removes new variable declaration.
     */
    StructDefinition.prototype.removeVariableDeclaration = function (variableDeclarationIdentifier) {
        var self = this;
        // Removing the variable from the children.
        var variableDeclarationChild = _.filter(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child)
                && child.getIdentifier() === variableDeclarationIdentifier;
        });
        this.removeChild(variableDeclarationChild[0])
    };

    /**
     * initialize StructDefinition from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.struct_name] - Name of the struct definition
     */
    StructDefinition.prototype.initFromJson = function (jsonNode) {
        var self = this;
        this._structName = jsonNode.struct_name;

        _.each(jsonNode.children, function (childNode) {
            var child = self.BallerinaASTFactory.createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return StructDefinition;
});