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
define(['log', 'lodash', 'require'],
    function (log, _, require) {

        /**
         * @class Connector
         * @augments
         * @param args {Object} - args.name: name of the package
         * @constructor
         */
        var Connector = function (args) {
            this._name = _.get(args, 'name', '');
            this._actions = _.get(args, 'actions', []);
            this._title = _.get(args, 'title', '');
            this.BallerinaEnvFactory = require('./ballerina-env-factory');
        };

        Connector.prototype.setName = function (name) {
            this._name = name;
        };

        Connector.prototype.getName = function () {
            return this._name;
        };

        Connector.prototype.setTitle = function (title) {
            this._title = title;
        };

        Connector.prototype.getTitle = function () {
            return this._title;
        };

        Connector.prototype.addAction = function (action) {
            this._actions.push(action);
        };

        Connector.prototype.getActions = function (action) {
            return this._actions;
        };

        Connector.prototype.initFromJson = function (jsonNode) {
            var self = this;

            this.setName(jsonNode.name);
            this.setTitle(jsonNode.name);

            _.each(jsonNode.actions, function (actionNode) {
                var action = self.BallerinaEnvFactory.createConnectorAction();
                action.initFromJson(actionNode);
                self.addAction(action);
            });
        };

        return Connector;
    });