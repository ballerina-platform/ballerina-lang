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
import VariableDefinition from './variable-definition';

class ParameterDefinition extends VariableDefinition {
    /**
     * Constructor for ParameterDefinition
     * @param {Object} [args] - The arguments to create the ParameterDefinition.
     * @param {string} [args.type=undefined] - Type of the parameter definition.
     * @param {string} [args.name=undefined] - Identifier of the parameter definition.
     * contains {Annotation} as children
     * @class ParameterDefinition
     * @constructor
     * @extends VariableDefinition
     */
    constructor(args){
        super(args);
        //since there are ParameterDefinitions without names (return types) we set this to undefined
        this._name = _.get(args, 'name', undefined);
        this.type = 'ParameterDefinition';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: ''
        };
    }

    /**
     * set the name of the parameter definition
     * @param name
     * @param options
     */
    setName (name, options) {
        //name can be null for parameter definitions (e.g. : named and unnamed return types)
        this.setAttribute('_name', name, options);
    }

    getParameterDefinitionAsString() {
        let argAsString = "";
        argAsString += this.getWSRegion(0) + this.getTypeName();
        argAsString += !_.isNil(this.getName()) ? this.getWSRegion(1) + this.getName() : "";
        argAsString += this.getWSRegion(2);
        return argAsString;
    }

    addAnnotation(annotation) {
        this.addChild(annotation);
    }

    initFromJson(jsonNode) {
        this.setTypeName(jsonNode.parameter_type, {doSilently: true});
        this.setName(jsonNode.parameter_name, {doSilently: true});

        // As of now we only support one annotation.
        if (_.isEqual(_.size(jsonNode.children), 1) && _.isEqual(jsonNode.children[0].type, "annotation")) {
            let annotationJson = jsonNode.children[0];
            let child = this.getFactory().createFromJson(annotationJson);
            this.addChild(child);
        }
    }
}

export default ParameterDefinition;
