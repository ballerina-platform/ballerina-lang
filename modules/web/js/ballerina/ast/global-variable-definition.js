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
import BallerinaAstFactory from './ballerina-ast-factory';

/**
 * Constructor GlobalVariableDefinition
 * @constructor
 */
class GlobalVariableDefinition extends ASTNode {
    constructor(args) {
        super({type: 'Global-Varialbe-Definition'});
        this.whiteSpace.defaultDescriptor.regions = {
            0: ' ',
            1: ' ',
            2: ' ',
            3: '',
            4: '\n',
        };
    }

    getGlobalVariableDefinitionAsString() {
        const variableDef = this.getChildren()[0];
        const expression = this.getChildren()[1];

        let defString = variableDef.getPkgName() ? variableDef.getPkgName() + ':': '';
        defString += variableDef.getTypeName();

        if(variableDef.isArray()){
            defString += '[]'
        }

        defString += this.getWSRegion(0) + variableDef.getName();

        if (expression) {
            defString += this.getWSRegion(1) + '=' + this.getWSRegion(2) + expression.getExpressionString();
        } else {
            defString += this.getWSRegion(3);
        }

        return defString;
    }

    _createVarDef(jsonNode){
        const args = {
            name: jsonNode.global_variable_definition_identifier,
            typeName: jsonNode.global_variable_definition_btype,
            pkgName: jsonNode.package_name,
            isArray: jsonNode.is_array_type,
        }
        return this.getFactory().createVariableDefinition(args);
    }

    /**
     * Initialize GlobalVariableDefinition from json object for parsing.
     * @param {Object} jsonNode - Model of a constant definition for parsing.
     * @param {string} jsonNode.constant_definition_btype - The ballerina type of the constant.
     * @param {string} jsonNode.constant_definition_identifier - The identifier of the constant.
     * @param {string} jsonNode.constant_definition_value - The value of the constant.
     */
    initFromJson(jsonNode) {
        if (!_.isNil(jsonNode.whitespace_descriptor)) {
            this.whiteSpace.currentDescriptor = jsonNode.whitespace_descriptor;
            this.whiteSpace.useDefault = false;
        }

        this.addChild(this._createVarDef(jsonNode));

        for (const childNode of jsonNode.children) {
            const child = this.getFactory().createFromJson(childNode);
            this.addChild(child);
            child.initFromJson(childNode);
        }
    }
}

export default GlobalVariableDefinition;
