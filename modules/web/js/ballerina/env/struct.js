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
 * @class Struct
 * @augments
 * @param {Object} args - data to create the Struct
 * @param {string} args.name - name of struct
 * @param {string} args.id - id of struct
 * @constructor
 */
class Struct {
    constructor(args) {
        this._name = _.get(args, 'name', '');
        this._id = _.get(args, 'id', '');
        this._fullPackageName = _.get(args, 'fullPackageName', '.');
    }

   /**
    * sets the name
    * @param {string} name
    */
    setName(name) {
        this._name = name;
    }

   /**
    * returns the name
    * @returns {string}
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
    * @returns {string}
    */
    getFullPackageName() {
        return this._fullPackageName;
    }

    /**
     * get package name
     * @returns {string}
     * @memberof Struct
     */
    getPackageName() {
        return _.last(_.split(this.getFullPackageName(), '.'));
    }


   /**
    * sets the id
    * @param {string} id
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
    */
    initFromJson(jsonNode) {
        this.setName(jsonNode.name);
        this.setId(jsonNode.name);
    }
}

export default Struct;
