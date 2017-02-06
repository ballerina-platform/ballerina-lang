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
define(['log', 'lodash', 'event_channel'],
    function (log, _, EventChannel) {

        /**
         * @class Function
         * @augments
         * @param {Object} args - data to create the Function
         * @param {string} args.name - name of function
         * @param {string} args.id - id of function
         * @param {string} args.title - title of function
         * @constructor
         */
        var Function = function (args) {
            EventChannel.call(this, args);
            this._name = _.get(args, 'name', '');
            this._id = _.get(args, 'id', '');
            this._title = _.get(args, 'title', '');
        };

        Function.prototype = Object.create(EventChannel.prototype);
        Function.prototype.constructor = Function;

        /**
         * sets the name
         * @param {string} name
         */
        Function.prototype.setName = function (name) {
            this._name = name;
        };

        /**
         * returns the name
         * @returns {string}
         */
        Function.prototype.getName = function () {
            return this._name;
        };

        /**
         * sets the id
         * @param {string} id
         */
        Function.prototype.setId = function (id) {
            this._id = id;
        };

        /**
         * returns the id
         * @returns {string}
         */
        Function.prototype.getId = function () {
            return this._id;
        };

        /**
         * sets the title
         * @param {string} title
         */
        Function.prototype.setTitle = function (title) {
            this._title = title;
        };

        /**
         * returns the title
         * @returns {string}
         */
        Function.prototype.getTitle = function () {
            return this._title;
        };

        /**
         * sets values from a json object
         * @param {Object} jsonNode
         */
        Function.prototype.initFromJson = function (jsonNode) {
            this.setName(jsonNode.name);
            this.setId(jsonNode.name);
            this.setTitle(jsonNode.name);
        };

        return Function;
    });