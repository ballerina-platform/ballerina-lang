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

/**
 * A module representing the factory for Ballerina Env
 */
define(['./package', './connector', './connector-action', './function','./type-mapper'],
    function (Package, Connector, ConnectorAction, Function, TypeMapper ) {

        /**
         * @class BallerinaEnvFactory
         */
        var BallerinaEnvFactory = {};

        /**
         * creates Package
         * @param args
         */
        BallerinaEnvFactory.createPackage = function (args) {
            var package = new Package(args);
            return package;
        };

        /**
         * creates Connector
         * @param args
         */
        BallerinaEnvFactory.createConnector = function (args) {
            var connector = new Connector(args);
            return connector;
        };
  
        /**
         * creates ConnectorAction
         * @param args
         */
        BallerinaEnvFactory.createConnectorAction = function (args) {
            var action = new ConnectorAction(args);
            return action;
        };

        /**
         * creates Function
         * @param jsonNode
         */
        BallerinaEnvFactory.createFunction = function (args) {
            var functionDef = new Function(args);
            return functionDef;
        };

        /**
         * creates TypeMapper
         * @param jsonNode
         */
        BallerinaEnvFactory.createTypeMapper = function (args) {
            var typeMapperDef = new TypeMapper(args);
            return typeMapperDef;
        };

        BallerinaEnvFactory.isConnector = function (connector) {
            return (connector instanceof Connector);
        };

        BallerinaEnvFactory.isFunction = function (functionDef) {
            return (functionDef instanceof Function);
        };

        BallerinaEnvFactory.isConnectorAction = function (connectorAction) {
            return (connectorAction instanceof ConnectorAction);
        };

        /**
         * instanceof check for TypeMapper
         * @param {function object} typeMapperDef - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaEnvFactory.isTypeMapper = function (typeMapperDef) {
            return (typeMapperDef instanceof TypeMapper);
        };

        return BallerinaEnvFactory;

    });
