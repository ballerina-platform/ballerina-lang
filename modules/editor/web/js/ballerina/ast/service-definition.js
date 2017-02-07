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
define(['lodash', './node', 'log'],
    function (_, ASTNode, log) {

    /**
     * Constructor for ServiceDefinition
     * @param {Object} args - The arguments to create the ServiceDefinition
     * @param {string} [args.serviceName=newService] - Service name
     * @param {string[]} [args.annotations] - Service annotations
     * @param {string} [args.annotations.BasePath] - Service annotation for BasePath
     * @constructor
     */
    var ServiceDefinition = function (args) {
        this._serviceName = _.get(args, 'serviceName', 'newService');
        this._annotations = _.get(args, 'annotations', []);

        // Adding available annotations and their default values.
        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "BasePath";
            }))) {
            this._annotations.push({
                key: "BasePath",
                value: ""
            });
        }

        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "Source:interface";
            }))) {
            this._annotations.push({
                key: "Source:interface",
                value: ""
            });
        }

        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "Service:description";
            }))) {
            this._annotations.push({
                key: "Service:description",
                value: ""
            });
        }

        // TODO: All the types should be referred from the global constants
        ASTNode.call(this, 'Service');
        this.BallerinaASTFactory = this.getFactory();
    };

    ServiceDefinition.prototype = Object.create(ASTNode.prototype);
    ServiceDefinition.prototype.constructor = ServiceDefinition;

    ServiceDefinition.prototype.setServiceName = function (serviceName, options) {
        if (!_.isNil(serviceName) && ASTNode.isValidIdentifier(serviceName)) {
            this.setAttribute('_serviceName', serviceName, options);
        } else {
            var errorString = "Invalid name for the service name: " + serviceName;
            log.error(errorString);
            throw errorString;
        }
    };

    ServiceDefinition.prototype.getServiceName = function () {
        return this._serviceName;
    };

    ServiceDefinition.prototype.getAnnotations = function () {
        return this._annotations;
    };

    ServiceDefinition.prototype.getVariableDeclarations = function () {
        var variableDeclarations = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isVariableDeclaration(child)) {
                variableDeclarations.push(child);
            }
        });
        return variableDeclarations;
    };

    ServiceDefinition.prototype.getConnectionDeclarations = function () {
        return this._connectionDeclarations;
    };

    /**
     * Adds new variable declaration.
     */
    ServiceDefinition.prototype.addVariableDeclaration = function (newVariableDeclaration) {
        var self = this;

        // Get the index of the last variable declaration.
        var index = _.findLastIndex(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child);
        });

        // index = -1 when there are not any variable declarations, hence get the index for connector
        // declarations.
        if (index == -1) {
            index = _.findLastIndex(this.getChildren(), function (child) {
                return self.BallerinaASTFactory.isConnectorDeclaration(child);
            });
        }

        this.addChild(newVariableDeclaration, index + 1);
    };

    /**
     * Adds new variable declaration.
     */
    ServiceDefinition.prototype.removeVariableDeclaration = function (variableDeclarationIdentifier) {
        var self = this;
        // Removing the variable from the children.
        var variableDeclarationChild = _.find(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child)
                && child.getIdentifier() === variableDeclarationIdentifier;
        });
        this.removeChild(variableDeclarationChild);
    };

    /**
     * Adding/Updating an annotation.
     * @param key - Annotation key
     * @param value - Value for the annotation.
     */
    ServiceDefinition.prototype.addAnnotation = function (key, value) {
        if (!_.isNil(key) && !_.isNil(value)) {
            var options = {
              predicate: {key: key}
            }
            this.pushToArrayAttribute('_annotations', {key: key, value: value}, options);
        } else {
            var errorString = "Cannot add annotation @" + key + "(\"" + value + "\").";
            log.error(errorString);
            throw errorString;
        }
    };

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    ServiceDefinition.prototype.canBeParentOf = function (node) {
        return this.BallerinaASTFactory.isResourceDefinition(node)
            || this.BallerinaASTFactory.isVariableDeclaration(node)
            || this.BallerinaASTFactory.isConnectorDeclaration(node);
    };

    /**
     * initialize ServiceDefinition from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.service_name] - Name of the service definition
     * @param {string} [jsonNode.annotations] - Annotations of the function definition
     */
    ServiceDefinition.prototype.initFromJson = function (jsonNode) {
        var self = this;
        this.setServiceName(jsonNode.service_name, {doSilently: true});

        // Populate the annotations array
        for (var itr = 0; itr < this._annotations.length; itr ++) {
            var key = this._annotations[itr].key;
            for (var itrInner = 0; itrInner < jsonNode.annotations.length; itrInner ++) {
                if (jsonNode.annotations[itrInner].annotation_name === key) {
                    this._annotations[itr].value = jsonNode.annotations[itrInner].annotation_value;
                }
            }
        }
        _.each(jsonNode.children, function (childNode) {
            var child = self.BallerinaASTFactory.createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    /**
     * Override the super call to addChild
     * @param {ASTNode} child
     * @param {number} index
     */
    ServiceDefinition.prototype.addChild = function (child, index) {
        var self = this;
        var newIndex = index;
        // Always the connector declarations should be the first children
        if (this.BallerinaASTFactory.isConnectorDeclaration(child)) {
            newIndex = _.findLastIndex(this.getChildren(), function (node) {
                return self.BallerinaASTFactory.isConnectorDeclaration(node);
            });
        }
        if (newIndex === -1) {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, 0);
        } else {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, newIndex);
        }
    };

    return ServiceDefinition;

});
