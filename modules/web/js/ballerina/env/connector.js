/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['log', 'lodash', 'require', 'event_channel'],
    function (log, _, require, EventChannel) {

        /**
         * @class Connector
         * @augments
         * @param args {Object} - args.name: name of the package
         * @constructor
         */
        var Connector = function (args) {
            EventChannel.call(this, args);
            this._name = _.get(args, 'name', '');
            this._id = _.get(args, 'id', '');
            this._actions = _.get(args, 'actions', []);
            this.BallerinaEnvFactory = require('./ballerina-env-factory');
        };

        Connector.prototype = Object.create(EventChannel.prototype);
        Connector.prototype.constructor = Connector;

        Connector.prototype.setName = function (name) {
            var oldName = this._name;
            this._name = name;
            this.trigger("name-modified", name, oldName);
        };

        Connector.prototype.getName = function () {
            return this._name;
        };

        /**
         * sets the id
         * @param {string} id
         */
        Connector.prototype.setId = function (id) {
            this._id = id;
        };

        /**
         * returns the id
         * @returns {string}
         */
        Connector.prototype.getId = function () {
            return this._id;
        };

        Connector.prototype.addAction = function (action) {
            this._actions.push(action);
            this.trigger("connector-action-added", action);
        };

        Connector.prototype.getActions = function (action) {
            return this._actions;
        };

        /**
         * returns the action by name
         * @param {string} actionName - name of the action
         */
        Connector.prototype.getActionByName = function (actionName) {
            return _.find(this.getActions(), function (action) {
                return _.isEqual(action.getName(),actionName);
            });
        };

        Connector.prototype.initFromJson = function (jsonNode) {
            var self = this;

            this.setName(jsonNode.name);

            _.each(jsonNode.actions, function (actionNode) {
                var action = self.BallerinaEnvFactory.createConnectorAction();
                action.initFromJson(actionNode);
                self.addAction(action);
            });
        };

        return Connector;
    });