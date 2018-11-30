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
import FunctionDefinition from './function';
import StructDefinition from './struct';
import StructField from './struct-field';
import Enum from './enum';
import Enumerator from './enumerator';
import AnnotationDefinition from './annotation-definition';
import AnnotationAttributeDefinition from './annotation-attribute-definition';
import AnnotationAttachment from './annotation-attachment';
import ObjectModel from "./object";
import RecordModel from "./record";
import ObjectField from "./object-field";
import Endpoint from "./endpoint";

/**
 * @class BallerinaEnvFactory
 */
const BallerinaEnvFactory = {};

/**
 * creates Package
 * @param args
 */
BallerinaEnvFactory.createPackage = function (args) {
    const pckg = new Package(args);
    return pckg;
};

/**
 * creates Connector
 * @param args
 */
BallerinaEnvFactory.createConnector = function (args) {
    const connector = new Connector(args);
    return connector;
};

/**
 * creates ConnectorAction
 * @param args
 */
BallerinaEnvFactory.createConnectorAction = function (args) {
    const action = new ConnectorAction(args);
    return action;
};

/**
 * creates Function
 * @param jsonNode
 */
BallerinaEnvFactory.createFunction = function (args) {
    const functionDef = new FunctionDefinition(args);
    return functionDef;
};

/**
 * creates Struct
 * @param args data for creating struct
 */
BallerinaEnvFactory.createStruct = function (args) {
    const structDef = new StructDefinition(args);
    return structDef;
};

/**
 * creates Struct Field
 * @param args data for creating the struct node
 */
BallerinaEnvFactory.createStructField = function (args) {
    const structField = new StructField(args);
    return structField;
};

/**
 * creates Enum
 * @param args data for creating the enum
 */
BallerinaEnvFactory.createEnum = function (args) {
    const enumNode = new Enum(args);
    return enumNode;
};

/**
 * creates Enumerator
 * @param args data for creating the enumerator node
 */
BallerinaEnvFactory.createEnumerator = function (args) {
    const enumerator = new Enumerator(args);
    return enumerator;
};
/**
 * creates Annotation
 * @param jsonNode
 */
BallerinaEnvFactory.createAnnotationDefinition = function (args) {
    const annotationDef = new AnnotationDefinition(args);
    return annotationDef;
};

/**
 * Creates AnnotationAttributeDefinition.
 * @param args
 */
BallerinaEnvFactory.createAnnotationAttributeDefinition = function (args) {
    const annotationAttributeDefinition = new AnnotationAttributeDefinition(args);
    return annotationAttributeDefinition;
};

/**
 * Creates AnnotationAttachment.
 * @param args
 */
BallerinaEnvFactory.createAnnotationAttachment = function (args) {
    const annotationAttachment = new AnnotationAttachment(args);
    return annotationAttachment;
};

/**
 * Creates endpoint.
 *
 * @param args arguments to be added
 * @returns {Endpoint} new endpoint
 */
BallerinaEnvFactory.createEndpoint = function(args) {
    return new Endpoint(args);
};

/**
 * Creates object model.
 *
 * @param args arguments to be added
 * @returns {ObjectModel} new ObjectModel
 */
BallerinaEnvFactory.createObject = function(args) {
    return new ObjectModel(args);
};

/**
 * Creates record model.
 *
 * @param args arguments to be added
 * @returns {RecordModel} new RecordModel
 */
BallerinaEnvFactory.createRecord = function(args) {
    return new RecordModel(args);
};

/**
 * Creates object field.
 *
 * @param args arguments to be added
 * @returns {ObjectField} new object field
 */
BallerinaEnvFactory.createObjectField = function (args) {
    return new ObjectField(args);
};

BallerinaEnvFactory.isPackage = function (pkg) {
    return (pkg instanceof Package);
};

BallerinaEnvFactory.isConnector = function (connector) {
    return (connector instanceof Connector);
};

BallerinaEnvFactory.isFunction = function (functionDef) {
    return (functionDef instanceof FunctionDefinition);
};

BallerinaEnvFactory.isStruct = function (structDef) {
    return (structDef instanceof StructDefinition);
};

BallerinaEnvFactory.isStructField = function (structField) {
    return (structField instanceof StructField);
};

BallerinaEnvFactory.isEnum = function (enumNode) {
    return (enumNode instanceof Enum);
};

BallerinaEnvFactory.isEnumerator = function (enumerator) {
    return (enumerator instanceof Enumerator);
};

BallerinaEnvFactory.isConnectorAction = function (connectorAction) {
    return (connectorAction instanceof ConnectorAction);
};

BallerinaEnvFactory.isEndpoint = function (endpoint) {
    return (endpoint instanceof Endpoint);
};

BallerinaEnvFactory.isObject = function(object) {
    return (object instanceof ObjectModel);
};

BallerinaEnvFactory.isObjectField = function (objectField) {
    return (objectField instanceof ObjectField);
};

/**
 * instanceof check for Annotation
 * @param {function object} annotationDef - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
BallerinaEnvFactory.isAnnotationDefinition = function (annotationDef) {
    return (annotationDef instanceof AnnotationDefinition);
};

/**
 * instanceof check for AnnotationAttributeDefinition
 * @param {object} annotationAttributeDefinition - Object for instanceof check
 * @returns {boolean} - true if same type, else false.
 */
BallerinaEnvFactory.isAnnotationAttributeDefinition = function (annotationAttributeDefinition) {
    return (annotationAttributeDefinition instanceof AnnotationAttributeDefinition);
};

export default BallerinaEnvFactory;
