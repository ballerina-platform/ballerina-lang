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
 * Class for record.
 */
class RecordModel {
    /**
     * Constructor for record class.
     *
     * @param args arguments to be added to the class.
     */
    constructor(args) {
        this._name = _.get(args, 'name', '');
        this._id = _.get(args, 'id', '');
        this._fields = _.get(args, 'fields', []);
        this._fullPackageName = _.get(args, 'packageName', '');
    }

    /**
     * Set the name.
     *
     * @param name name of the record
     */
    setName(name) {
        this._name = name;
    }

    /**
     * Get the name
     *
     * @returns {string} name of the record
     */
    getName() {
        return this._name;
    }

    /**
     * Set the id.
     *
     * @param id id of the record
     */
    setId(id) {
        this._id = id;
    }

    /**
     * Get the id.
     *
     * @returns {id} id of the record
     */
    getId() {
        return this._id;
    }

    /**
     * Set the package name.
     *
     * @param packageName package name of the record
     */
    setFullPackageName(packageName) {
        this._fullPackageName = packageName;
    }

    /**
     * Get the full package name.
     *
     * @returns {string} package name
     */
    getFullPackageName() {
        return this._fullPackageName;
    }

    /**
     * Set the fields.
     *
     * @param fields set record fields
     */
    setFields(fields) {
        this._fields = fields;
    }

    /**
     * Get the fields.
     *
     * @returns {field} record fields
     */
    getFields() {
        return this._fields;
    }

    /**
     * Initialize the record from the json node.
     *
     * @param jsonNode json node
     */
    initFromJson(jsonNode) {
        this.setName(jsonNode.name);
        this.setId(jsonNode.name);
        this.setFullPackageName(jsonNode.packageName);
        if (jsonNode.fields !== undefined) {
            const fields = jsonNode.fields.map((field) => {
                return BallerinaEnvFactory.createObjectField({
                    type: field.type,
                    name: field.name,
                    defaultValue: field.defaultValue,
                    packageName: field.packageName
                });
            });
            this.setFields(fields);
        }
    }
}

export default RecordModel;