/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Module for BallerinaASTRoot
 */
define(['lodash', 'log', './node', './import-declaration'], function (_, log, ASTNode, ImportDeclaration) {

    /**
     * Constructs BallerinaASTRoot
     * @class BallerinaASTRoot
     * @augments ASTNode
     * @param args
     * @constructor
     */
    var BallerinaASTRoot = function (args) {
        this.packageDefinition = _.get(args, 'packageDefinition');
        this.importDeclarations = _.get(args, 'importDeclarations', []);
        this.serviceDefinitions = _.get(args, 'serviceDefinitions', []);
        this.functionDefinitions = _.get(args, 'functionDefinitions', []);
        this.connectorDefinitions = _.get(args, 'connectorDefinitions', []);
        this.typeDefinitions = _.get(args, 'typeDefinitions', []);
        this.typeConvertorDefinitions = _.get(args, 'typeConvertorDefinitions', []);
        this.constantDefinitions = _.get(args, 'constantDefinitions', []);
        ASTNode.call(this);
    };

    BallerinaASTRoot.prototype = Object.create(ASTNode.prototype);
    BallerinaASTRoot.prototype.constructor = BallerinaASTRoot;

    /**
     * Setter function for PackageDefinition 
     * @param packageDefinition
     */
    BallerinaASTRoot.prototype.setPackageDefinition = function (packageDefinition){
        if(!_.isNil(packageDefinition)){
            packageDefinition.setParent(this);
            this.packageDefinition  = packageDefinition;
        }
    };

    /**
     * Setter for ImportDeclarations
     * @param importDeclarations
     */
    BallerinaASTRoot.prototype.setImportDeclarations = function (importDeclarations) {
        if(!_.isNil(importDeclarations)){
            this.importDeclarations = importDeclarations;
        }
    };
    
    /**
     * Setter function for ServiceDefinition
     * @param serviceDefinitions
     */
    BallerinaASTRoot.prototype.setServiceDefinitions = function (serviceDefinitions) {
        if (!_.isNil(serviceDefinitions)) {
            this.serviceDefinitions = serviceDefinitions;
            var self = this;
            _.forEach(serviceDefinitions, function (serviceDefinition) {
                serviceDefinition.setParent(self);
            });
        }
    };

    /**
     * Setter function for ConnectorDefinition
     * @param connectorDefinitions
     */
    BallerinaASTRoot.prototype.setConnectorDefinitions = function (connectorDefinitions) {
        if (!_.isNil(connectorDefinitions)) {
            this.connectorDefinitions = connectorDefinitions;
            var self = this;
            _.forEach(connectorDefinitions, function (connectorDefinition) {
                connectorDefinition.setParent(self);
            });
        }
    };

    /**
     * Setter function for FunctionDefinition
     * @param functionDefinitions
     */
    BallerinaASTRoot.prototype.setFunctionDefinitions = function (functionDefinitions) {
        if (!_.isNil(functionDefinitions)) {
            this.functionDefinitions = functionDefinitions;
            var self = this;
            _.forEach(functionDefinitions, function (functionDefinition) {
                functionDefinition.setParent(self);
            });
        }
    };

    /**
     * Getter function for PackageDefinition
     * @return {*}
     */
    BallerinaASTRoot.prototype.getPackageDefinition = function () {
        return this.packageDefinition;
    };

    /**
     * Getter function for PackageDefinition
     * @return {*}
     */
    BallerinaASTRoot.prototype.getImportDeclarations = function () {
        return this.importDeclarations;
    };

    /**
     * Getter function for ServiceDefinition
     * @returns {Array}
     */
    BallerinaASTRoot.prototype.getServiceDefinitions = function () {
        return this.serviceDefinitions;
    };

    /**
     * Getter function for ServiceDefinition
     * @returns {Array}
     */
    BallerinaASTRoot.prototype.getConnectorDefinitions = function () {
        return this.connectorDefinitions;
    };

    /**
     * Getter function for FunctionDefinition
     * @returns {Array}
     */
    BallerinaASTRoot.prototype.getFunctionDefinitions = function () {
        return this.functionDefinitions;
    };

    /**
     * This is the root element of ballerina ast - so cannot be a child of anyone.
     * @param node
     * @return {boolean}
     */
    BallerinaASTRoot.prototype.canBeAChildOf = function (node) {
        return false;
    };

    /**
     * Deletes an import with given package name.
     */
    BallerinaASTRoot.prototype.deleteImport = function (packageName) {
        _.remove(this.importDeclarations, function (importDeclaration) {
            return importDeclaration.getPackageName() == packageName;
        });
    };

    /**
     * Adds new import declaration.
     * @param {ImportDeclaration} importDeclaration - New import declaration.
     */
    BallerinaASTRoot.prototype.addImport = function (importDeclaration) {
        if (!_.isNil(importDeclaration) && importDeclaration instanceof ImportDeclaration) {
            this.importDeclarations.push(importDeclaration);
        } else {
            log.error("Invalid import declaration received for AST root." + importDeclaration);
            throw "Invalid import declaration received for AST root." + importDeclaration;
        }
    };

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    BallerinaASTRoot.prototype.canBeParentOf = function (node) {
        var BallerinaASTFactory = this.getFactory();
        return BallerinaASTFactory.isServiceDefinition(node)
            || BallerinaASTFactory.isFunctionDefinition(node)
            || BallerinaASTFactory.isConnectorDefinition(node)
            || BallerinaASTFactory.isTypeDefinition(node)
            || BallerinaASTFactory.isTypeConverterDefinition(node);
    };


    return BallerinaASTRoot;
});
