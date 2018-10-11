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
 * @class Function
 * @augments
 * @param {Object} args - data to create the Function
 * @param {string} args.name - name of function
 * @param {string} args.id - id of function
 * @constructor
 */
class Function {
    constructor(args) {
        this._name = _.get(args, 'name', '');
        this._id = _.get(args, 'id', '');
        this._parameters = _.get(args, 'parameters', []);
        this._returnParams = _.get(args, 'returnParams', []);
        this._fullPackageName = _.get(args, 'fullPackageName', '.');
        this._receiverType = _.get(args, 'receiverType', '');
        this._isPublic = _.get(args, 'isPublic', '');
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
     * @memberof Function
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
    * sets the parameters
    * @param [object] parameters
    */
    setParameters(parameters) {
        this._parameters = parameters;
    }

   /**
    * returns the parameters
    * @returns [object]
    */
    getParameters() {
        return this._parameters;
    }

   /**
    * sets the returnParams
    * @param [object] returnParams
    */
    setReturnParams(returnParams) {
        this._returnParams = returnParams;
    }

   /**
    * returns the returnParams
    * @returns [object]
    */
    getReturnParams() {
        return this._returnParams;
    }

    /**
    * sets the receiverType
    * @param {string} receiverType
    */
    setReceiverType(receiverType) {
        this._receiverType = receiverType;
    }

   /**
    * returns the receiverType
    * @returns {string}
    */
    getReceiverType() {
        return this._receiverType;
    }

    /**
     * sets whether the function is public or not
     * @param {boolean} isPublic
     */
    setPublic(isPublic) {
        this._isPublic = isPublic;
    }

    /**
     * returns if the function is public or not
     * @returns {boolean}
     */
    isPublic() {
        return this._isPublic;
    }

   /**
    * sets values from a json object
    * @param {Object} jsonNode
    */
    initFromJson(jsonNode) {
        this.setName(jsonNode.name);
        this.setId(jsonNode.name);
        this.setParameters(jsonNode.parameters);
        this.setReturnParams(jsonNode.returnParams);
        this.setReceiverType(jsonNode.receiverType);
        this.setPublic(jsonNode.isPublic);
    }
}

export default Function;
