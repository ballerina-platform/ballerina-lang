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
import _ from 'lodash';
import ConditionalStatement from './conditional-statement';

/**
 * Class for catch statement in ballerina.
 * @class CatchStatement
 * @constructor
 * @extends ConditionalStatement
 */
class CatchStatement extends ConditionalStatement {
    /**
     * Constructor for catch statement
     * @param {object} args constructor arguments
     */
    constructor(args) {
        super();
        this._parameter = _.get(args, 'parameter');
        this.type = 'CatchStatement';
        this._parameterDefString = _.get(args, 'parameterDefString', 'exception e');
        this.whiteSpace.defaultDescriptor.regions = {
            0: ' ',
            1: ' ',
            2: '',
            3: ' ',
            4: '',
            5: ' ',
            6: '\n',
            7: '\n',
        };
    }

    /**
     * Set parameter
     * @param {string} parameter parameter instance
     * @param {options} options set attribute options
     * @returns {void}
     */
    setParameter(parameter, options) {
        if (!_.isNil(parameter)) {
            this.setAttribute('_parameter', parameter, options);
        }
    }

    /**
     * Get parameter
     * @return {object} parameter
     */
    getParameter() {
        return this._parameter;
    }

    /**
     * Get the parameter definition string
     * @returns {string} parameter definition string
     */
    getParameterDefString() {
        return this._parameterDefString;
    }

    /**
     * Set parameter definition string
     * @param {string} paramDef parameter instance
     * @param {options} options set attribute options
     * @returns {void}
     */
    setParameterDefString(paramDef, options) {
        this.setAttribute('_parameterDefString', paramDef, options);
    }

    /**
     * initialize from json
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        const self = this;
        const parameterDef = jsonNode.parameter_definition;
        const paramDefNode = self.getFactory().createFromJson(parameterDef[0]);
        paramDefNode.initFromJson(parameterDef[0]);
        this.setParameter(paramDefNode, { doSilently: true });
        this.setParameterDefString(paramDefNode.getParameterDefinitionAsString(), { doSilently: true });

        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }
}

export default CatchStatement;
