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

/**
 * @class Struct Field
 * @augments
 * @param {Object} args args to create the struct field
 * @param {string} args.name name of the struct field
 * @param {string} args.type type of the struct field
 * @param {string} args.defaultValue default value of the struct field
 * @constructor
 */
class StructField {

    /**
     * Constructor for struct
     * @param {Object} args args to create the struct field
     * @param {string} args.name name of the struct field
     * @param {string} args.type type of the struct field
     * @param {string} args.defaultValue default value of the struct field
     * @param {string} args.packageName package of the struct field - applicable for struct types
     * @memberof Struct
     */
    constructor(args) {
        this._name = _.get(args, 'name', '');
        this._type = _.get(args, 'type', '');
        this._defaultValue = _.get(args, 'defaultValue');
        this._packageName = _.get(args, 'packageName');
    }

   /**
    * sets the name
    * @param {string} name struct field name
    */
    setName(name) {
        this._name = name;
    }

   /**
    * returns the name
    * @returns {string} struct field
    */
    getName() {
        return this._name;
    }

    /**
    * sets the type
    * @param {string} type struct field type
    */
    setType(type) {
        this._type = type;
    }

   /**
    * returns the type
    * @returns {string} struct field type
    */
    getType() {
        return this._type;
    }

    /**
    * sets the default value
    * @param {string} defaultValue struct field default value
    */
    setDefaultValue(defaultValue) {
        this._defaultValue = defaultValue;
    }

   /**
    * returns the default value
    * @returns {string} struct field default value
    */
    getDefaultValue() {
        return this._defaultValue;
    }

    /**
    * sets the package name
    * @param {string} packageName struct field package name
    */
    setPackageName(packageName) {
        this._packageName = packageName;
    }

   /**
    * returns the package value
    * @returns {string} struct field package name
    */
    getPackageName() {
        return this._packageName;
    }

   /**
    * sets values from a json object
    * @param {Object} jsonNode json object containing struct field data
    * @param {string} jsonNode.name name of the struct field
    * @param {string} jsonNode.type type of the struct field
    * @param {string} jsonNode.defaultValue default value of the struct field
    * @param {string} jsonNode.packageName package name of the struct field
    */
    initFromJson(jsonNode) {
        this.setName(jsonNode.name);
        this.setType(jsonNode.type);
        this.setDefaultValue(jsonNode.defaultValue);
        this.setPackageName(jsonNode.packageName);
    }
}

export default StructField;
