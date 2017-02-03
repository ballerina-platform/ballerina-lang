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
        this._selectedTypeStructNameForSource = '';
        this._selectedTypeStructNameForTarget = '';
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
            this.setAttribute('_typeMapperName', typeMapperName);
        } else {
            log.error('Invalid Type Mapper name [' + typeMapperName + '] Provided');
            throw 'Invalid Type Mapper name [' + typeMapperName + '] Provided';
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

    /**
     * Set the already selected type struct name for source
     * @param selectedStructNameForSource
     */
    TypeMapperDefinition.prototype.setSelectedStructNameForSource = function (selectedStructNameForSource) {
        if (!_.isNil(selectedStructNameForSource)) {
            this.setAttribute('_selectedTypeStructNameForSource', selectedStructNameForSource);
        } else {
            log.error('Invalid TypeStructName [' + selectedStructNameForSource + '] Provided');
            throw 'Invalid TypeStructName [' + selectedStructNameForSource + '] Provided';
        }
    };

    /**
     * Returns the selected type struct name for source
     * @returns {string} type struct name for source
     */
    TypeMapperDefinition.prototype.getSelectedStructNameForSource = function () {
        return this._selectedTypeStructNameForSource;
    };

    /**
     * Set the already selected type struct name for target
     * @param selectedStructNameForTarget
     */
    TypeMapperDefinition.prototype.setSelectedStructNameForTarget = function (selectedStructNameForTarget) {
        if (!_.isNil(selectedStructNameForTarget)) {
            this.setAttribute('_selectedTypeStructNameForTarget', selectedStructNameForTarget);
        } else {
            log.error('Invalid TypeStructName [' + selectedStructNameForTarget + '] Provided');
            throw 'Invalid TypeStructName [' + selectedStructNameForTarget + '] Provided';
        }
    };

    /**
     * Returns the selected type struct name for target
     * @returns {string} type struct name for target
     */
    TypeMapperDefinition.prototype.getSelectedStructNameForTarget = function () {
        return this._selectedTypeStructNameForTarget;
    };

    /**
     * removes the already selected child before adding a new child
     * @param type
     */
    TypeMapperDefinition.prototype.removeTypeStructDefinition = function (type) {
        var self = this;
        if (this.getChildren() != 0) {
            var selectedTypeDef = _.find(this.getChildren(), function (child) {
                return self.BallerinaASTFactory.isTypeStructDefinition(child)
                    && child._category === type;
            });
            if (selectedTypeDef) {
                this.removeChild(selectedTypeDef);
            }
        }
    };

    /**
     * removes the already selected child before adding a new child
     * @param sourceProperty
     * @param targetProperty
     */
    TypeMapperDefinition.prototype.removeAssignmentDefinition = function (sourceProperty, targetProperty) {
        //TODO: Get rid of hardcoded x and y
        var self = this;
        if (this.getChildren() != 0) {
            var assignmentStatement = _.find(this.getChildren(), function (child) {
                return self.BallerinaASTFactory.isAssignmentStatement(child) &&
                    (('x.' + targetProperty + ' = ' + 'y.' + sourceProperty) === child.getStatementString());
            });
            if (assignmentStatement) {
                this.removeChild(assignmentStatement);
            }
        }
    };

    /**
     * returns the index of the selected struct
     * @param structArray
     * @param selectedStructName
     */
    TypeMapperDefinition.prototype.getSelectedStructIndex = function (structArray, selectedStructName) {
        return _.findIndex(structArray, function (child) {
            return child._structName === selectedStructName;
        });
    };

    return TypeMapperDefinition;
});