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
 * @class Enum
 * @augments
 * @param {Object} args data to create the Struct
 * @param {string} args.name name of struct
 * @param {string} args.id id of struct
 * @param {string} args.fullPackageName full package name of the struct
 * @param {[StructField]} args.fields fields of the struct
 * @constructor
 */
class Enum {

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
        this._fullPackageName = _.get(args, 'fullPackageName', '.');
        this._enumerators = _.get(args, 'enumerators', []);
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
     * @param {[Enumerators]} enumerators of the struct
     * @memberof Struct
     */
    setEnumerators(enumerators) {
        this._enumerators = enumerators;
    }

    /**
     * Get struct fields
     * @returns {[StructField]} struct fields
     * @memberof Struct
     */
    getEnumerators() {
        return this._enumerators;
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
        if (jsonNode.enumerators !== undefined) {
            const enumerator = jsonNode.enumerators.map((enumeratorItem) => {
                return BallerinaEnvFactory.createEnumerator(
                    { name: enumeratorItem.name,
                        packageName: enumeratorItem.packageName });
            });
            this.setEnumerators(enumerator);
        }
    }
}

export default Enum;
