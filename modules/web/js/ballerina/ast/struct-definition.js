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
import _ from 'lodash';
import log from 'log';
import ASTNode from './node';
import constants from 'constants';
import CommonUtils from '../utils/common-utils';
var STRUCT_DEFINITION_ATTRIBUTES_ARRAY_NAME = constants.STRUCT_DEFINITION_ATTRIBUTES_ARRAY_NAME;
var STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_NAME = constants.STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_NAME;
var STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_TYPE = constants.STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_TYPE;
var STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTIES = constants.STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTIES;

class StructDefinition extends ASTNode {
    constructor(args) {
        super('StructDefinition');
        this._structName = _.get(args, 'structName');
    }

    /**
     * setter for struct name
     * @param structName - name of the struct
     */
    setStructName(structName, options) {
        this.setAttribute('_structName', structName, options);
    }

    /**
     * getter for struct name
     * @returns {string} struct name
     */
    getStructName() {
        return this._structName;
    }

    /**
     * Gets all the variables definition statements residing in the struct.
     * @return {VariableDefinitionStatement[]} - The variable definition statements.
     */
    getVariableDefinitionStatements() {
        var variableDefinitions = [];

        _.forEach(this.filterChildren(this.getFactory().isVariableDefinitionStatement), function (child) {
            variableDefinitions.push(child);
        });
        return variableDefinitions;
    }

    /**
     * Adds new variable definition statement.
     * @param {string} bType - The ballerina type of the variable.
     * @param {string} identifier - The identifier of the variable
     * @param {string} defaultValue - The default value of the variable
     */
    addVariableDefinitionStatement(bType, identifier, defaultValue) {

        // if identifier is empty
        if (_.isEmpty(identifier)) {
            var errorString = "Identifier cannot be empty";
            log.error(errorString);
            throw errorString;
        }

        // Check if already variable definition exists with same identifier.
        var identifierAlreadyExists = _.findIndex(this.getVariableDefinitionStatements(), function (variableDefinitionStatement) {
            return variableDefinitionStatement.getIdentifier() === identifier;
        }) !== -1;

        // If variable definition with the same identifier exists, then throw an error. Else create the new variable
        // definition.
        if (identifierAlreadyExists) {
            var errorString = "A variable with identifier '" + identifier + "' already exists.";
            log.error(errorString);
            throw errorString;
        } else {
            // Creating new variable definition.
            var newVariableDefinitionStatement = this.getFactory().createVariableDefinitionStatement();
            newVariableDefinitionStatement.setLeftExpression(bType + ' ' + identifier);
            newVariableDefinitionStatement.setRightExpression(defaultValue);

            // Get the index of the last definition.
            var index = this.findLastIndexOfChild(this.getFactory().isVariableDefinitionStatement);

            this.addChild(newVariableDefinitionStatement, index + 1);
        }
    }

    /**
     * Removes new variable definition statement.
     * @param {string} modelID - The model ID of the variable.
     */
    removeVariableDefinitionStatement(modelID) {
        this.removeChildById(modelID);
    }

    /**
     * Initialize StructDefinition from json object
     * @param {Object} jsonNode - JSON object for initialization.
     * @param {string} jsonNode.struct_name - Name of the struct definition.
     * @param {VariableDefinitioon[]} jsonNode.children - Variables of the struct definition.
     */
    initFromJson(jsonNode) {
        var self = this;
        this.setStructName(jsonNode.struct_name, {doSilently: true});

        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    canBeParentOf(node) {
        return this.getFactory().isVariableDefinition(node);
    }

    /**
     * return attributes list as a json object
     * @returns {Object} attributes array
     */
    getAttributesArray() {
        var attributesArray = {};
        attributesArray[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_NAME] = this.getStructName();
        attributesArray[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTIES]= [];
        _.each(this.getVariableDefinitionStatements(), function(variableDefinition) {
            var tempAttr = {};
            tempAttr[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_NAME] = variableDefinition.getIdentifier();
            tempAttr[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_TYPE] = variableDefinition.getBType();
            attributesArray[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTIES].push(tempAttr);
        });
        return attributesArray;
    }

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
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
    }
}

export default StructDefinition;
    
