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
        this._variableReferenceName = _.get(args, 'variableReferenceName');
        this.setExpression(this.generateExpression(), {doSilently: true});
    }

    /**
     * Setter for Variable Reference
     * @param variableReference
     */
    setVariableReferenceName(variableReferenceName, options) {
        this.setAttribute('_variableReferenceName', variableReferenceName, options);
    }

    /**
     * Getter for VariableReference
     * @returns variableReference
     */
    getVariableReferenceName() {
        return this._variableReferenceName;
    }

    /**
     * initialize VariableReferenceExpression from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.variable_reference_name] - Symbol name of the VariableReferenceExpression
     */
    initFromJson(jsonNode) {
        this.setVariableReferenceName(jsonNode.variable_reference_name, {doSilently: true});
        this.setExpression(this.generateExpression(), {doSilently: true});
    }

    generateExpression() {
        return this.getVariableReferenceName();
    }
}

export default VariableReferenceExpression;

