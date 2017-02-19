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
define(['log', 'lodash', 'require', 'event_channel', './../ast/ballerina-ast-factory'],
    function (log, _, require, EventChannel, BallerinaASTFactory) {

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
            this._params = _.get(args, 'params', []);
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

        /**
         * remove the provided action item from the actions array
         * @param {ConnectorActionDefinition} actionDef - ConnectorActionDefinition to be removed
         */
        Connector.prototype.removeAction = function (actionDef) {
            _.remove(this._actions, function (action) {
                return _.isEqual(action.getName(), actionDef.getActionName());
            });
            this.trigger("connector-action-removed", actionDef);
        };

        /**
         * remove all the action items of the provided connector definition
         * @param {ConnectorDefinition} connectorDef - ConnectorDefinition whose children need to be removed
         */
        Connector.prototype.removeAllActions = function (connectorDef) {
            var self = this;
            _.each(connectorDef.getChildren(), function (child) {
                if(BallerinaASTFactory.isConnectorAction(child)){
                    self.removeAction(child);
                }
            });
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

        Connector.prototype.addParam = function (param) {
            this._params.push(param);
            this.trigger("param-added", param);
        };

        Connector.prototype.getParams = function (param) {
            return this._params;
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
