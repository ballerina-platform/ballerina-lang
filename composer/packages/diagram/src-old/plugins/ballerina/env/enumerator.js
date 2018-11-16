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
 * @class Enumerator
 */
class Enumerator {

    /**
     * Constructor for enumerator
     * @param {Object} args args to create the enumerator
     * @param {string} args.name name of the enumerator
     * @param {string} args.packageName package of the enumerator
     * @constructor
     */
    constructor(args) {
        this._name = _.get(args, 'name', '');
        this._packageName = _.get(args, 'packageName');
    }

   /**
    * sets the name
    * @param {string} name enumerator
    */
    setName(name) {
        this._name = name;
    }

   /**
    * returns the name
    * @returns {string} enumerator
    */
    getName() {
        return this._name;
    }

    /**
    * sets the package name
    * @param {string} packageName enumerator package name
    */
    setPackageName(packageName) {
        this._packageName = packageName;
    }

   /**
    * returns the package value
    * @returns {string} enumerator package name
    */
    getPackageName() {
        return this._packageName;
    }

   /**
    * sets values from a json object
    * @param {Object} jsonNode json object containing enumerator data
    * @param {string} jsonNode.name name of the enumerator
    * @param {string} jsonNode.packageName package name of the enumerator
    */
    initFromJson(jsonNode) {
        this.setName(jsonNode.name);
        this.setPackageName(jsonNode.packageName);
    }
}

export default Enumerator;
