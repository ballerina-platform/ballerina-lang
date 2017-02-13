/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['lodash', 'log', './variable-declaration'], function (_, log, VariableDeclaration) {

    /**
     * Constructor for constant declaration
     * @param {Object} args - Arguments to create the Constant Declaration
     * @constructor
     * @augments VariableDeclaration
     */
    var ConstantDefinition = function(args) {
        VariableDeclaration.call(this, {
            type: "Constant-Declaration",
            bType: _.get(args, "bType"),
            identifier: _.get(args, "identifier")
        });
        this._value = _.get(args, "value");
    };

    ConstantDefinition.prototype = Object.create(VariableDeclaration.prototype);
    ConstantDefinition.prototype.constructor = ConstantDefinition;

    ConstantDefinition.prototype.setValue = function (value, options) {
        if (_.isNil(value) || _.isEmpty(value)) {
            log.error("A constant requires to have a value.");
            throw "A constant requires to have a value.";
        } else {
            this.setAttribute('_value', value, options);
        }
    };

    ConstantDefinition.prototype.getValue = function () {
        return this._value;
    };

    ConstantDefinition.prototype.getConstantDefinitionAsString = function() {
        if (this._type === "string") {
            return "const " + this._type + " " + this._identifier + " = \"" + this._value + "\"";
        } else {
            return "const " + this._type + " " + this._identifier + " = " + this._value;
        }
    };

    /**
     * Initialize ConstantDefinition from json object for parsing.
     * @param {Object} jsonNode - Model of a constant definition for parsing.
     * @param {string} jsonNode.constant_definition_btype - The ballerina type of the constant.
     * @param {string} jsonNode.constant_definition_identifier - The identifier of the constant.
     * @param {string} jsonNode.constant_definition_value - The value of the constant.
     */
    ConstantDefinition.prototype.initFromJson = function (jsonNode) {
        this.setType(jsonNode.constant_definition_btype, {doSilently: true});
        this.setIdentifier(jsonNode.constant_definition_identifier, {doSilently: true});
        this.setValue(jsonNode.constant_definition_value, {doSilently: true});
    };

    return ConstantDefinition;
});
