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
define(['lodash', './node', './variable-declaration', './connector-declaration'],
    function (_, ASTNode, VariableDeclaration, ConnectorDeclaration) {

    var ServiceDefinition = function (args) {
        this._serviceName = _.get(args, 'serviceName', 'newService');
        this._annotations = _.get(args, 'annotations', []);
        this._resourceDefinitions = _.get(args, 'resourceDefinitions', []);
        this._connectionDeclarations = _.get(args, 'connectionDeclarations', []);

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
    };

    ServiceDefinition.prototype = Object.create(ASTNode.prototype);
    ServiceDefinition.prototype.constructor = ServiceDefinition;

    ServiceDefinition.prototype.setServiceName = function (serviceName) {
        if(!_.isNil(serviceName)){
            this._serviceName = serviceName;
        }
    };

    ServiceDefinition.prototype.setResourceDefinitions = function (resourceDefinitions) {
        if (!_.isNil(resourceDefinitions)) {
            this._resourceDefinitions = resourceDefinitions;
        }
    };

    ServiceDefinition.prototype.setVariableDeclarations = function (variableDeclarations) {
        if (!_.isNil(variableDeclarations)) {
            // TODO : To implement using child array.
            throw "To be Implemented";
        }
    };

    ServiceDefinition.prototype.setConnectionDeclarations = function (connectionDeclarations) {
        if (!_.isNil(connectionDeclarations)) {
            this._connectionDeclarations = connectionDeclarations;
        }
    };

    ServiceDefinition.prototype.getServiceName = function () {
        return this._serviceName;
    };

    ServiceDefinition.prototype.getAnnotations = function () {
        return this._annotations;
    };

    ServiceDefinition.prototype.getResourceDefinitions = function () {
        return this._resourceDefinitions;
    };

    ServiceDefinition.prototype.getVariableDeclarations = function () {
        var variableDeclarations = [];
        _.forEach(this.getChildren(), function (child) {
            if (child instanceof VariableDeclaration) {
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
        // Get the index of the last variable declaration.
        var index = _.findLastIndex(this.getChildren(), function (child) {
            return child instanceof VariableDeclaration;
        });

        // index = -1 when there are not any variable declarations, hence get the index for connector
        // declarations.
        if (index == -1) {
            index = _.findLastIndex(this.getChildren(), function (child) {
                return child instanceof ConnectorDeclaration;
            });
        }

        this.addChild(newVariableDeclaration, index + 1);
    };

    /**
     * Adds new variable declaration.
     */
    ServiceDefinition.prototype.removeVariableDeclaration = function (variableDeclaration) {
        this.removeChild(variableDeclaration)
    };

    /**
     * Adding/Updating an annotation.
     * @param key - Annotation key
     * @param value - Value for the annotation.
     */
    ServiceDefinition.prototype.addAnnotation = function (key, value) {
        var existingAnnotation = _.find(this._annotations, function (annotation) {
            return annotation.key == key;
        });
        if (_.isNil(existingAnnotation)) {
            // If such annotation does not exists, then add a new one.
            this._annotations.push({
                key: key,
                value: value
            });
        } else {
            // Updating existing annotation.
            existingAnnotation.value = value;
        }
    };

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    ServiceDefinition.prototype.canBeParentOf = function (node) {
        var BallerinaASTFactory = this.getFactory();
        return BallerinaASTFactory.isResourceDefinition(node)
            || BallerinaASTFactory.isVariableDeclaration(node);
    };

    /**
     * initialize from json
     * @param jsonNode
     */
    ServiceDefinition.prototype.initFromJson = function (jsonNode) {
        this._serviceName = jsonNode.service_name;
        this._annotations = jsonNode.annotations;

        var self = this;
        var BallerinaASTFactory = this.getFactory();

        _.each(jsonNode.children, function (childNode) {
            var child = BallerinaASTFactory.createFromJson(childNode);
            self.addChild(child);
        });
    };

    return ServiceDefinition;

});
