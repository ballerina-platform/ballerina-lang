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
import _ from 'lodash';
import log from 'log';
import VariableDeclaration from './variable-declaration';

/**
 * Constructor for constant declaration
 * @param {Object} args - Arguments to create the Constant Declaration
 * @constructor
 * @augments VariableDeclaration
 */
class ConstantDefinition extends VariableDeclaration {
    constructor(args) {
        super({
            type: "Constant-Declaration",
            bType: _.get(args, "bType"),
            identifier: _.get(args, "identifier")
        });
        this._value = _.get(args, "value");
    }

    setValue(value, options) {
        if (_.isNil(value) || _.isEmpty(value)) {
            log.error("A constant requires to have a value.");
            throw "A constant requires to have a value.";
        } else {
            this.setAttribute('_value', value, options);
        }
    }

    getValue() {
        return this._value;
    }

    getConstantDefinitionAsString() {
        if (this._bType === "string") {
            return "const " + this._bType + " " + this._identifier + " = \"" + this._value + "\"";
        } else {
            return "const " + this._bType + " " + this._identifier + " = " + this._value;
        }
    }

    /**
     * Initialize ConstantDefinition from json object for parsing.
     * @param {Object} jsonNode - Model of a constant definition for parsing.
     * @param {string} jsonNode.constant_definition_btype - The ballerina type of the constant.
     * @param {string} jsonNode.constant_definition_identifier - The identifier of the constant.
     * @param {string} jsonNode.constant_definition_value - The value of the constant.
     */
    initFromJson(jsonNode) {
        this.setBType(jsonNode.constant_definition_btype, {doSilently: true});
        this.setIdentifier(jsonNode.constant_definition_identifier, {doSilently: true});
        this.setValue(jsonNode.constant_definition_value, {doSilently: true});
    }
}

export default ConstantDefinition;

