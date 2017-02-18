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
define(['lodash', 'log', './node', 'constants', '../utils/common-utils'],
    function (_, log, ASTNode, constants, CommonUtils) {
        var StructDefinition = function (args) {
            ASTNode.call(this, 'StructDefinition');
            this._structName = _.get(args, 'structName');
            this.BallerinaASTFactory = this.getFactory();
        };

        StructDefinition.prototype = Object.create(ASTNode.prototype);
        StructDefinition.prototype.constructor = StructDefinition;

        /**
         * setter for struct name
         * @param structName - name of the struct
         */
        StructDefinition.prototype.setStructName = function (structName, options) {
            this.setAttribute('_structName', structName, options);
        };

        /**
         * getter for struct name
         * @returns {string} struct name
         */
        StructDefinition.prototype.getStructName = function () {
            return this._structName;
        };

        /**
         * Gets all the variables definitions residing in the struct.
         * @return {VariableDefinition[]} - The variable definitions.
         */
        StructDefinition.prototype.getVariableDefinitions = function () {
            var variableDefinitions = [];
            var self = this;

            _.forEach(this.getChildren(), function (child) {
                if (self.BallerinaASTFactory.isVariableDefinition(child)) {
                    variableDefinitions.push(child);
                }
            });
            return variableDefinitions;
        };

        /**
         * Adds new variable definition.
         * @param {string} bType - The ballerina type of the variable.
         * @param {string} identifier - The identifier of the variable
         */
        StructDefinition.prototype.addVariableDefinition = function (bType, identifier) {
            // Check if already variable definition exists with same identifier.
            var identifierAlreadyExists = _.findIndex(this.getVariableDefinitions(), function (variableDefinition) {
                return variableDefinition.getName() === identifier;
            }) !== -1;

            // If variable definition with the same identifier exists, then throw an error. Else create the new variable
            // definition.
            if (identifierAlreadyExists) {
                var errorString = "A variable with identifier '" + identifier + "' already exists.";
                log.error(errorString);
                throw errorString;
            } else {
                // Creating new variable definition.
                var newVariableDefinition = this.getFactory().createVariableDefinition();

                newVariableDefinition.setTypeName(bType);
                newVariableDefinition.setName(identifier);

                var self = this;

                // Get the index of the last definition.
                var index = _.findLastIndex(this.getChildren(), function (child) {
                    return self.getFactory().isVariableDefinition(child);
                });

                this.addChild(newVariableDefinition, index + 1);
            }
        };

        /**
         * Removes new variable definition.
         * @param {string} modelID - The model ID of the variable.
         */
        StructDefinition.prototype.removeVariableDefinition = function (modelID) {
            var self = this;
            // Removing the variable from the children.
            var variableDefinitionChild = _.find(this.getChildren(), function (child) {
                return self.BallerinaASTFactory.isVariableDefinition(child)
                    && child.getID() === modelID;
            });
            this.removeChild(variableDefinitionChild);
        };

        /**
         * Initialize StructDefinition from json object
         * @param {Object} jsonNode - JSON object for initialization.
         * @param {string} jsonNode.struct_name - Name of the struct definition.
         * @param {VariableDefinitioon[]} jsonNode.children - Variables of the struct definition.
         */
        StructDefinition.prototype.initFromJson = function (jsonNode) {
            var self = this;
            this.setStructName(jsonNode.struct_name, {doSilently: true});

            _.each(jsonNode.children, function (childNode) {
                var child = self.BallerinaASTFactory.createFromJson(childNode);
                self.addChild(child);
                child.initFromJson(childNode);
            });
        };

        /**
         * Validates possible immediate child types.
         * @override
         * @param node
         * @return {boolean}
         */
        StructDefinition.prototype.canBeParentOf = function (node) {
            return this.BallerinaASTFactory.isVariableDefinition(node)
        };
        /**
         * return attributes list as a json object
         * @returns {Object} attributes array
         */
        StructDefinition.prototype.getAttributesArray = function () {
            var attributesArray = {};
            attributesArray[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_NAME] = this.getStructName();
            attributesArray[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTIES]= [];
            _.each(this.getVariableDefinitions(), function(variableDefinition) {
                var tempAttr = {};
                tempAttr[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_NAME] = variableDefinition.getName();
                tempAttr[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_TYPE] = variableDefinition.getTypeName();
                attributesArray[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTIES].push(tempAttr);
            });
            return attributesArray;
        };
        /**
         * @inheritDoc
         * @override
         */
        StructDefinition.prototype.generateUniqueIdentifiers = function () {
            CommonUtils.generateUniqueIdentifier({
                node: this,
                attributes: [{
                    defaultValue: "newStruct",
                    setter: this.setStructName,
                    getter: this.getStructName,
                    parents: [{
                        // ballerina-ast-root
                        node: this.parent,
                        getChildrenFunc: this.parent.getStructDefinitions,
                        getter: this.getStructName
                    }]
                }]
            });
        };

        return StructDefinition;
    });
