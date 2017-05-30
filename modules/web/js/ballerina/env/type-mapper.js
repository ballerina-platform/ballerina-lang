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
import log from 'log';
import _ from 'lodash';
import EventChannel from 'event_channel';

/**
 * @class TypeMapper
 * @augments
 * @param {Object} args - data to create the TypeMapper
 * @param {string} args.name - name of TypeMapper
 * @param {string} args.id - id of TypeMapper
 * @param {string} args.title - title of TypeMapper
 * @constructor
 */
class TypeMapper extends EventChannel {
    constructor(args) {
        super(args);
        this._name = _.get(args, 'name', '');
        this._id = _.get(args, 'id', '');
        this._title = _.get(args, 'title', '');
        this._returnType = _.get(args, 'returnType', '');
        this._sourceAndIdentifier = _.get(args, 'sourceAndIdentifier', '');
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
    * sets the returnType
    * @param {string} returnType
    */
    setReturnType(returnType) {
        this._returnType = returnType;
    }

   /**
    * returns the returnType
    * @returns {string}
    */
    getReturnType() {
        return this._returnType;
    }

   /**
    * sets the sourceAndIdentifier
    * @param {string} sourceAndIdentifier
    */
    setSourceAndIdentifier(sourceAndIdentifier) {
        this._sourceAndIdentifier = sourceAndIdentifier;
    }

   /**
    * returns the sourceAndIdentifier
    * @returns {string}
    */
    getSourceAndIdentifier() {
        return this._sourceAndIdentifier;
    }

   /**
    * sets the title
    * @param {string} title
    */
    setTitle(title) {
        this._title = title;
    }

   /**
    * returns the title
    * @returns {string}
    */
    getTitle() {
        return this._title;
    }

   /**
    * sets values from a json object
    * @param {Object} jsonNode
    */
    initFromJson(jsonNode) {
        this.setName(jsonNode.name);
        this.setId(jsonNode.name);
        this.setTitle(jsonNode.name);
    }
}

export default TypeMapper;
