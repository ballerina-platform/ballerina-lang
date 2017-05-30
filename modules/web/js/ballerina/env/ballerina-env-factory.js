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
import Package from './package';
import Connector from './connector';
import ConnectorAction from './connector-action';
import Function from './function';
import Annotation from './annotation';
import TypeMapper from './type-mapper';

/**
 * @class BallerinaEnvFactory
 */
var BallerinaEnvFactory = {};

/**
 * creates Package
 * @param args
 */
BallerinaEnvFactory.createPackage = function (args) {
    var pckg = new Package(args);
    return pckg;
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
 * creates Annotation
 * @param jsonNode
 */
BallerinaEnvFactory.createAnnotation = function (args) {
    var annotationDef = new Annotation(args);
    return annotationDef;
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
 * instanceof check for Annotation
 * @param {function object} annotationDef - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
BallerinaEnvFactory.isAnnotation = function (annotationDef){
    return (annotationDef instanceof Annotation);
};

/**
 * instanceof check for TypeMapper
 * @param {function object} typeMapperDef - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
BallerinaEnvFactory.isTypeMapper = function (typeMapperDef) {
    return (typeMapperDef instanceof TypeMapper);
};

export default BallerinaEnvFactory;
