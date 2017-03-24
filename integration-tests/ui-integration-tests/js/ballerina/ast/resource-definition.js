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
define(['lodash', 'require', 'log', './node', '../utils/common-utils'],
    function (_, require, log, ASTNode, CommonUtils) {

    /**
     * Constructor for ResourceDefinition
     * @param {Object} args - The arguments to create the ServiceDefinition
     * @param {string} [args.resourceName=newResource] - Service name
     * @param {string[]} [args.annotations] - Resource annotations
     * @param {string} [args.annotations.Method] - Resource annotation for Method
     * @param {string} [args.annotations.Path] - Resource annotation for Path
     * @constructor
     */
    var ResourceDefinition = function (args) {
        this._resourceName = _.get(args, 'resourceName');
        this._annotations = _.get(args, 'annotations', []);

        // Adding available annotations and their default values.
        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "Method";
            }))) {
            this._annotations.push({
                key: "Method",
                value: "http:GET"
            });
        }

        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "Path";
            }))) {
            this._annotations.push({
                key: "http:Path",
                value: ""
            });
        }

        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "Consumes";
            }))) {
            this._annotations.push({
                key: "http:Consumes",
                value: ""
            });
        }

        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "Produces";
            }))) {
            this._annotations.push({
                key: "http:Produces",
                value: ""
            });
        }

        // TODO: All the types should be referred from the global constants
        ASTNode.call(this, 'Resource', 'resource {', '}');

        this.BallerinaASTFactory = this.getFactory();

        // Adding the default worker declaration.
        var defaultWorker = this.BallerinaASTFactory.createWorkerDeclaration({isDefaultWorker: true});
        this.addChild(defaultWorker);
    };

    ResourceDefinition.prototype = Object.create(ASTNode.prototype);
    ResourceDefinition.prototype.constructor = ResourceDefinition;

    ResourceDefinition.prototype.setResourceName = function (resourceName, options) {
        if (!_.isNil(resourceName)) {
            this.setAttribute('_resourceName', resourceName, options);
        } else {
            log.error('Invalid Resource name [' + resourceName + '] Provided');
            throw 'Invalid Resource name [' + resourceName + '] Provided';
        }
    };

    ResourceDefinition.prototype.getVariableDefinitionStatements = function () {
        var variableDefinitionStatements = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isVariableDefinitionStatement(child)) {
                variableDefinitionStatements.push(child);
            }
        });
        return variableDefinitionStatements;
    };

    ResourceDefinition.prototype.getParameters = function () {
        var resourceParameters = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isResourceParameter(child)) {
                resourceParameters.push(child);
            }
        });
        return resourceParameters;
    };

    ResourceDefinition.prototype.getResourceName = function () {
        return this._resourceName;
    };

    ResourceDefinition.prototype.getAnnotations = function () {
        return this._annotations;
    };

    /**
     * Adds new variable declaration.
     */
    ResourceDefinition.prototype.addVariableDeclaration = function (newVariableDeclaration) {
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
    ResourceDefinition.prototype.removeVariableDeclaration = function (variableDeclarationIdentifier) {
        var self = this;
        // Removing the variable from the children.
        var variableDeclarationChild = _.find(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child)
                && child.getIdentifier() === variableDeclarationIdentifier;
        });
        this.removeChild(variableDeclarationChild);
    };

    /**
     * Returns the list of parameters as a string separated by commas.
     * @return {string} - Parameters as string.
     */
    ResourceDefinition.prototype.getParametersAsString = function () {
        var paramsAsString = "";
        var params = this.getParameters();

        _.forEach(params, function(parameter, index){
            paramsAsString += parameter.getParameterAsString();
            if (params.length - 1 != index) {
                paramsAsString += ",";
            }
        });

        return paramsAsString;
    };

    /**
     * Adds new parameter to a resource.
     * @param {string|undefined} annotationType - The type of the annotation. Example: @PathParam.
     * @param {string|undefined} annotationText - The value inside the annotation.
     * @param {string} parameterType - The type of the parameter. Example : string, int.
     * @param {string} parameterIdentifier - Identifier for the parameter.
     */
    ResourceDefinition.prototype.addParameter = function (annotationType, annotationText, parameterType, parameterIdentifier) {
        // Check if already parameter exists with same identifier.
        var identifierAlreadyExists = _.findIndex(this.getParameters(), function (parameter) {
                return parameter.getIdentifier() === parameterIdentifier;
            }) !== -1;

        // If parameter with the same identifier exists, then throw an error. Else create the new parameter.
        if (identifierAlreadyExists) {
            var errorString = "A resource parameter with identifier '" + parameterIdentifier + "' already exists.";
            log.error(errorString);
            throw errorString;
        } else {
            // Creating resource parameter
            var newParameter = this.BallerinaASTFactory.createResourceParameter();
            newParameter.setAnnotationType(annotationType);
            newParameter.setAnnotationText(annotationText);
            newParameter.setType(parameterType);
            newParameter.setIdentifier(parameterIdentifier);

            var self = this;

            // Get the index of the last resource parameter declaration.
            var index = _.findLastIndex(this.getChildren(), function (child) {
                return self.BallerinaASTFactory.isResourceParameter(child);
            });

            this.addChild(newParameter, index + 1);
        }
    };

    /**
     * Removes a parameter from the resource definition.
     * @param {string} modelID - The id of the parameter model.
     */
    ResourceDefinition.prototype.removeParameter = function(modelID) {
        var self = this;
        // Deleting the variable from the children.
        var resourceParameter = _.find(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isResourceParameter(child) && child.id === modelID;
        });

        this.removeChild(resourceParameter);
    };

    ResourceDefinition.prototype.resourceParent = function (parent) {
        if (!_.isUndefined(parent)) {
            this.parent = parent;
        } else {
            return this.parent;
        }
    };

    /**
     * Override the super call to addChild
     * @param child
     * @param index
     */
    ResourceDefinition.prototype.addChild = function (child, index) {
        if (this.BallerinaASTFactory.isConnectorDeclaration(child)) {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, 0);
        } else {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, index);
        }
    };
    /**
     * Adding/Updating an annotation.
     * @param key - Annotation key
     * @param value - Value for the annotation.
     */
    ResourceDefinition.prototype.addAnnotation = function (key, value) {
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

    ResourceDefinition.prototype.getConnectionDeclarations = function () {
        var connectorDeclaration = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isConnectorDeclaration(child)) {
                connectorDeclaration.push(child);
            }
        });
        return _.sortBy(connectorDeclaration, [function (connectorDeclaration) {
            return connectorDeclaration.getConnectorVariable();
        }]);
    };

    ResourceDefinition.prototype.getWorkerDeclarations = function () {
        var workerDeclarations = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isWorkerDeclaration(child)) {
                workerDeclarations.push(child);
            }
        });
        return _.sortBy(workerDeclarations, [function (workerDeclaration) {
            return workerDeclaration.getWorkerName();
        }]);
    };

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    ResourceDefinition.prototype.canBeParentOf = function (node) {
        return this.BallerinaASTFactory.isConnectorDeclaration(node)
            || this.BallerinaASTFactory.isVariableDeclaration(node)
            || this.BallerinaASTFactory.isWorkerDeclaration(node)
            || this.BallerinaASTFactory.isStatement(node);
    };

    /**
     * initialize ResourceDefinition from json object
     * @param {Object} jsonNode - to initialize from
     * @param {string} jsonNode.resource_name - Name of the resource definition
     * @param {Object[]} jsonNode.annotations - Annotations of the resource definition
     * @param {string} jsonNode.annotations.annotation_name - The annotation value
     * @param {string} jsonNode.annotations.annotation_value - The text of the annotation.
     * @param {Object[]} jsonNode.children - Children elements of the resource definition.
     */
    ResourceDefinition.prototype.initFromJson = function (jsonNode) {
        this.setResourceName(jsonNode.resource_name, {doSilently: true});
        this._annotations = _.isNil(this._annotations) ? [] : this._annotations;

        var self = this;

        // Check if a annotation of type http method is received from the service.
        var existingMethodAnnotationFromService = _.find(jsonNode.annotations, function (annotation) {
            return annotation.annotation_name === "http:POST" || annotation.annotation_name === "http:GET" ||
                annotation.annotation_name === "http:PUT" || annotation.annotation_name === "http:DELETE" ||
                annotation.annotation_name === "http:HEAD" || annotation.annotation_name === "http:PATCH" ||
                annotation.annotation_name === "http:OPTIONS"
        });

        // Get the method annotation of the model.
        var existingMethodAnnotationInModel = _.find(self._annotations, function (annotation) {
            return annotation.key === "Method"
        });

        // If http method annotation is received from the service.
        if (!_.isNil(existingMethodAnnotationFromService)) {
            // Remove the existing value.
            existingMethodAnnotationInModel.value = "";
            _.forEach(jsonNode.annotations, function (annotation) {
                // Updating the new http method value.
                if (annotation.annotation_name === "http:POST" || annotation.annotation_name === "http:GET" ||
                    annotation.annotation_name === "http:PUT" || annotation.annotation_name === "http:DELETE" ||
                    annotation.annotation_name === "http:HEAD" || annotation.annotation_name === "http:PATCH" ||
                    annotation.annotation_name === "http:OPTIONS") {
                    if (_.isEmpty(existingMethodAnnotationInModel.value)) {
                        existingMethodAnnotationInModel.value = annotation.annotation_name;
                    } else {
                        existingMethodAnnotationInModel.value = existingMethodAnnotationInModel.value + ", " +
                            annotation.annotation_name;
                    }
                }
            });
        }

        // Updating the annotations of the model according to the annotations received from the service that are not
        // related to http methods.
        _.forEach(jsonNode.annotations, function(annotationFromService){
           if (!(annotationFromService.annotation_name === "http:POST" || annotationFromService.annotation_name === "http:GET" ||
               annotationFromService.annotation_name === "http:PUT" || annotationFromService.annotation_name === "http:DELETE" ||
               annotationFromService.annotation_name === "http:HEAD" || annotationFromService.annotation_name === "http:PATCH" ||
               annotationFromService.annotation_name === "http:OPTIONS")) {
               var existingAnnotation = _.find(self._annotations, function (annotationInModel) {
                   return annotationInModel.key === annotationFromService.annotation_name
               });
               if (_.isNil(existingAnnotation)) {
                   self._annotations.push({
                       key: annotationFromService.annotation_name,
                       value: annotationFromService.annotation_value
                   });
               } else {
                   existingAnnotation.value = annotationFromService.annotation_value;
               }
           }
        });

        _.each(jsonNode.children, function (childNode) {
            var child = undefined;
            var childNodeTemp = undefined;
            if (childNode.type === "variable_definition_statement" && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = self.BallerinaASTFactory.createConnectorDeclaration();
                childNodeTemp = childNode;
            } else {
                child = self.BallerinaASTFactory.createFromJson(childNode);
                childNodeTemp = childNode;
            }
            self.addChild(child);
            child.initFromJson(childNodeTemp);
        });
    };

    /**
     * Override the addChild method for ordering the child elements as
     * [Statements, Workers, Connectors]
     * @param {ASTNode} child
     * @param {number|undefined} index
     */
    ResourceDefinition.prototype.addChild = function (child, index) {
        var indexNew;
        var self = this;
        if (self.BallerinaASTFactory.isWorkerDeclaration(child)) {
            indexNew = _.findLastIndex(this.getChildren(), function (node) {
                self.BallerinaASTFactory.isWorkerDeclaration(node);
            });
            indexNew = (indexNew === -1) ? 0 : (indexNew + 1);
        } else if (this.BallerinaASTFactory.isConnectorDeclaration(child)) {
            var firstWorker = _.findIndex(this.getChildren(), function (node) {
                self.BallerinaASTFactory.isWorkerDeclaration(node);
            });
            if (firstWorker !== -1) {
                indexNew = firstWorker - 1;
            }
        } else {
            var firstWorker = _.findIndex(this.getChildren(), function (node) {
                self.BallerinaASTFactory.isWorkerDeclaration(node);
            });
            var firstConnector = _.findIndex(this.getChildren(), function (node) {
                self.BallerinaASTFactory.isWorkerDeclaration(node);
            });

            if (firstConnector !== -1) {
                indexNew = firstConnector - 1;
            } else if (firstWorker !== -1) {
                indexNew = index
            } else {
                indexNew = index
            }
        }

        Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, indexNew);
    };

    /**
     * @inheritDoc
     * @override
     */
    ResourceDefinition.prototype.generateUniqueIdentifiers = function () {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: "newResource",
                setter: this.setResourceName,
                getter: this.getResourceName,
                parents: [{
                    // service definition
                    node: this.parent,
                    getChildrenFunc: this.parent.getResourceDefinitions,
                    getter: this.getResourceName
                }]
            }]
        });
    };

    /**
     * Get the connector by name
     * @param {string} connectorName
     * @return {ConnectorDeclaration}
     */
    ResourceDefinition.prototype.getConnectorByName = function (connectorName) {
        var factory = this.getFactory();
        var connectorReference = _.find(this.getChildren(), function (child) {
            return (factory.isConnectorDeclaration(child) && (child.getConnectorVariable() === connectorName));
        });

        return !_.isNil(connectorReference) ? connectorReference : this.getParent(). getConnectorByName(connectorName);
    };

    return ResourceDefinition;
});
