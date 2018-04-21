/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import BallerinaEnvFactory from './ballerina-env-factory';

/**
 * Class for endpoint.
 */
class Endpoint {
    /**
     * Constructor for the endpoint class.
     *
     * @param args argument to be added to endpoint
     */
    constructor(args) {
        this._name = _.get(args, 'name', '');
        this._id = _.get(args, 'id', '');
        this._fullPackageName = _.get(args, 'packageName', '');
        this._actions = _.get(args, 'actions', []);
        this._fields = _.get(args, 'fields', []);
    }

    /**
     * Set the name
     *
     * @param {string} name endpoint name
     */
    setName(name) {
        this._name = name;
    }

    /**
     * Get the name.
     *
     * @returns {string} endpoint name
     */
    getName() {
        return this._name;
    }

    /**
     * Set the id.
     *
     * @param id id of the endpoint
     */
    setId(id) {
        this._id = id;
    }

    /**
     * Get the id.
     *
     * @returns {id} id of the endpoint
     */
    getId() {
        return this._id;
    }

    /**
     * Set the package name.
     *
     * @param {string} fullPackageName package name
     */
    setFullPackageName(fullPackageName) {
        this._fullPackageName = fullPackageName;
    }

    /**
     * Get the full package name
     *
     * @returns {string} full package name
     */
    getFullPackageName() {
        return this._fullPackageName;
    }

    /**
     * Set the actions for the endpoint.
     *
     * @param {function} actions list of actions
     */
    setActions(actions) {
        this._actions = actions;
    }

    /**
     * Get the actions.
     *
     * @returns {function} actions of endpoint
     */
    getActions() {
        return this._actions;
    }

    /**
     * Set the fields.
     *
     * @param {StructField} fields field of the endpoint
     */
    setFields(fields) {
        this._fields = fields;
    }

    /**
     * Get the fields.
     *
     * @returns {StructField} fields of endpoint
     */
    getFields() {
        return this._fields;
    }

    /**
     * Initialize from the json node.
     *
     * @param {json} jsonNode json node of endpoint
     */
    initFromJson(jsonNode) {
        this.setName(jsonNode.name);
        this.setId(jsonNode.name);
        this.setFullPackageName(jsonNode.packageName);
        if (jsonNode.fields !== undefined) {
            const fields = jsonNode.fields.map((field) => {
                return BallerinaEnvFactory.createStructField({
                    name: field.name,
                    type:field.type,
                    defaultValue: field.defaultValue,
                    packageName: field.packageName
                });
            });
            this.setFields(fields);
        }

        if (jsonNode.actions !== undefined) {
            const actions = jsonNode.actions.map((func) => {
                return BallerinaEnvFactory.createFunction({
                    name: func.name,
                    id: func.name,
                    parameters: func.parameters,
                    returnParams: func.returnParams,
                    fullPackageName: func.fullPackageName,
                    receiverType: func.receiverType,
                    isPublic: func.isPublic
                })
            });
            this.setActions(actions);
        }
    }
}

export default Endpoint;