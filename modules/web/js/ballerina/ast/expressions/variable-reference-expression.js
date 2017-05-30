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
import Expression from './expression';

/**
 * Constructor for VariableReferenceExpression
 * @param {Object} args - Arguments to create the VariableReferenceExpression
 * @constructor
 */
class VariableReferenceExpression extends Expression {
    constructor(args) {
        super('VariableReferenceExpression');
        this.setVariableName(_.get(args, 'variableName'));
        this.setExpression(this.generateExpression(), {doSilently: true});
    }

    /**
     * Setter for Variable Name
     * @param variableName
     */
    setVariableName(variableName, options) {
        this.setAttribute('_variableName', variableName, options);
    }

    /**
     * Getter for Variable Name
     * @returns variableName
     */
    getVariableName() {
        return this._variableName;
    }

    /**
     * initialize VariableReferenceExpression from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.variable_reference_name] - Variable name of the VariableReferenceExpression
     */
    initFromJson(jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
        this.setVariableName(jsonNode.variable_reference_name, {doSilently: true});
        this.setExpression(this.generateExpression(), {doSilently: true});
    }

    generateExpression() {
        var varDef = this.findChild(this.getFactory().isVariableDefinition);
        if (!_.isNil(varDef)) {
            return (!_.isNil(varDef.getPkgPath()) ?
                varDef.getPkgPath() + ":" : "") + varDef.getTypeName() + " " + varDef.getName();
        } else {
            return this.getVariableName();
        }
    }
}

export default VariableReferenceExpression;
