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
import BallerinaEnvFactory from './ballerina-env-factory';

/**
 * @class Struct
 * @augments
 * @param {Object} args data to create the Struct
 * @param {string} args.name name of struct
 * @param {string} args.id id of struct
 * @param {string} args.fullPackageName full package name of the struct
 * @param {[StructField]} args.fields fields of the struct
 * @constructor
 */
class Struct {

    /**
     * Constructor for struct
     * @param {Object} args - data to create the Struct
     * @param {int} args.id id of the struct
     * @param {string} args.name name of the struct
     * @param {string} args.fullPackageName full package name of the struct
     * @param {[StructField]} args.fields fields of the struct
     * @memberof Struct
     */
    constructor(args) {
        this._name = _.get(args, 'name', '');
        this._id = _.get(args, 'id', '');
        this._fullPackageName = _.get(args, 'packageName', '.');
        this._fields = _.get(args, 'fields', []);
        this._fields = _.get(args, 'functions', []);
    }

   /**
    * sets the name
    * @param {string} name struct name
    */
    setName(name) {
        this._name = name;
    }

   /**
    * returns the name
    * @returns {string} struct name
    */
    getName() {
        return this._name;
    }

    /**
    * sets the package name
    * @param {string} name
    */
    setFullPackageName(fullPackageName) {
        this._fullPackageName = fullPackageName;
    }

   /**
    * returns the full package name
    * @returns {string} full package name
    */
    getFullPackageName() {
        return this._fullPackageName;
    }

    /**
     * get package name
     * @returns {string} package identifier
     * @memberof Struct
     */
    getPackageName() {
        return _.last(_.split(this.getFullPackageName(), '.'));
    }

    /**
     * set struct fields
     * @param {[StructField]} structFields fields of the struct
     * @memberof Struct
     */
    setFields(structFields) {
        this._fields = structFields;
    }

    /**
     * Get struct fields
     * @returns {[StructField]} struct fields
     * @memberof Struct
     */
    getFields() {
        return this._fields;
    }

   /**
    * sets the id
    * @param {string} id id of the struct
    */
    setId(id) {
        this._id = id;
    }

   /**
    * returns the id
    * @returns {string}
    */
    getId() {
        return this._id;
    }

   /**
    * sets values from a json object
    * @param {Object} jsonNode
    * @param {string} jsonNode.name name of the struct
    * @param {int} jsonNode.id id of the struct
    */
    initFromJson(jsonNode) {
        this.setName(jsonNode.name);
        this.setId(jsonNode.name);
        if (jsonNode.structFields !== undefined) {
            const structFields = jsonNode.structFields.map((structField) => {
                return BallerinaEnvFactory.createStructField(
                    { name: structField.name,
                        type: structField.type,
                        defaultValue: structField.defaultValue,
                        packageName: structField.packageName });
            });
            this.setFields(structFields);
        }
    }
}

export default Struct;
