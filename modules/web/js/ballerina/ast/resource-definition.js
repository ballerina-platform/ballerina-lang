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
import _ from 'lodash';
import log from 'log';
import ASTNode from './node';
import CommonUtils from '../utils/common-utils';
const supportedHttpMethodAnnotations = ['POST', 'GET', 'PUT', 'HEAD', 'DELETE', 'PATCH', 'OPTION'];

/**
 * Constructor for ResourceDefinition
 * @param {Object} args - The arguments to create the ServiceDefinition
 * @param {string} [args.resourceName=newResource] - Service name
 * @constructor
 */
class ResourceDefinition extends ASTNode {
    constructor(args) {
        // TODO: All the types should be referred from the global constants
        super('Resource', 'resource {', '}');
        this._resourceName = _.get(args, 'resourceName');

        this.BallerinaASTFactory = this.getFactory();

        // Adding the default worker declaration.
        var defaultWorker = this.BallerinaASTFactory.createWorkerDeclaration({isDefaultWorker: true});
        this.addChild(defaultWorker);
    }

    setResourceName(resourceName, options) {
        if (!_.isNil(resourceName)) {
            this.setAttribute('_resourceName', resourceName, options);
        } else {
            log.error('Invalid Resource name [' + resourceName + '] Provided');
            throw 'Invalid Resource name [' + resourceName + '] Provided';
        }
    }

    getVariableDefinitionStatements() {
        var variableDefinitionStatements = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isVariableDefinitionStatement(child)) {
                variableDefinitionStatements.push(child);
            }
        });
        return variableDefinitionStatements;
    }

    getParameters() {
        var resourceParameters = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isResourceParameter(child)) {
                resourceParameters.push(child);
            }
        });
        return resourceParameters;
    }

    getResourceName() {
        return this._resourceName;
    }

    /**
     * Adds new variable declaration.
     */
    addVariableDeclaration(newVariableDeclaration) {
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
    }

    /**
     * Adds new variable declaration.
     */
    removeVariableDeclaration(variableDeclarationIdentifier) {
        var self = this;
        // Removing the variable from the children.
        var variableDeclarationChild = _.find(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child)
                && child.getIdentifier() === variableDeclarationIdentifier;
        });
        this.removeChild(variableDeclarationChild);
    }

    /**
     * Returns the list of parameters as a string separated by commas.
     * @return {string} - Parameters as string.
     */
    getParametersAsString() {
        var paramsAsString = "";
        var params = this.getParameters();

        _.forEach(params, function(parameter, index){
            paramsAsString += parameter.getParameterAsString();
            if (params.length - 1 != index) {
                paramsAsString += ",";
            }
        });

        return paramsAsString;
    }

    /**
     * Adds new parameter to a resource.
     * @param {string|undefined} annotationType - The type of the annotation. Example: @PathParam.
     * @param {string|undefined} annotationText - The value inside the annotation.
     * @param {string} parameterType - The type of the parameter. Example : string, int.
     * @param {string} parameterIdentifier - Identifier for the parameter.
     */
    addParameter(annotationType, annotationText, parameterType, parameterIdentifier) {
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
    }

    /**
     * Removes a parameter from the resource definition.
     * @param {string} modelID - The id of the parameter model.
     */
    removeParameter(modelID) {
        var self = this;
        // Deleting the variable from the children.
        var resourceParameter = _.find(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isResourceParameter(child) && child.id === modelID;
        });

        this.removeChild(resourceParameter);
    }

    resourceParent(parent) {
        if (!_.isUndefined(parent)) {
            this.parent = parent;
        } else {
            return this.parent;
        }
    }

    getConnectionDeclarations() {
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
    }

    getWorkerDeclarations() {
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
    }

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    canBeParentOf(node) {
        return this.BallerinaASTFactory.isConnectorDeclaration(node)
            || this.BallerinaASTFactory.isVariableDeclaration(node)
            || this.BallerinaASTFactory.isWorkerDeclaration(node)
            || this.BallerinaASTFactory.isStatement(node);
    }

    /**
     * initialize ResourceDefinition from json object
     * @param {Object} jsonNode - to initialize from
     * @param {string} jsonNode.resource_name - Name of the resource definition
     * @param {Object[]} jsonNode.annotations - Annotations of the resource definition
     * @param {string} jsonNode.annotations.annotation_name - The annotation value
     * @param {string} jsonNode.annotations.annotation_value - The text of the annotation.
     * @param {Object[]} jsonNode.children - Children elements of the resource definition.
     */
    initFromJson(jsonNode) {
        this.setResourceName(jsonNode.resource_name, {doSilently: true});
        let self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = undefined;
            var childNodeTemp = undefined;
            if (childNode.type === 'variable_definition_statement' && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = self.BallerinaASTFactory.createConnectorDeclaration();
                childNodeTemp = childNode;
            } else {
                child = self.BallerinaASTFactory.createFromJson(childNode);
                childNodeTemp = childNode;
            }
            self.addChild(child);
            child.initFromJson(childNodeTemp);
        });
    }

    /**
     * Override the addChild method for ordering the child elements as
     * [Statements, Workers, Connectors]
     * @param {ASTNode} child
     * @param {number|undefined} index
     */
    addChild(child, index) {
        if (_.isUndefined(index)) {
            let indexNew;

            let lastAnnotationIndex = _.findLastIndex(this.getChildren(), (child) => {
                return this.BallerinaASTFactory.isAnnotation(child);
            });

            indexNew = lastAnnotationIndex === -1 ? 0 : lastAnnotationIndex + 1;

            if (!this.BallerinaASTFactory.isAnnotation(child)) {
                let lastWorkerDeclarationIndex = _.findLastIndex(this.getChildren(), (child) => {
                    return this.BallerinaASTFactory.isWorkerDeclaration(child);
                });

                indexNew = lastWorkerDeclarationIndex === -1 ? indexNew : lastWorkerDeclarationIndex + 1;

                if (!this.BallerinaASTFactory.isWorkerDeclaration(child)) {
                    let lastConnectorDeclarationIndex = _.findLastIndex(this.getChildren(), (child) => {
                        return this.BallerinaASTFactory.isConnectorDeclaration(child);
                    });

                    indexNew = lastConnectorDeclarationIndex === -1 ? indexNew : lastConnectorDeclarationIndex + 1;
                }
            }

            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, indexNew);
        } else {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, index);
        }
    }

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: 'Resource',
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
    }

    /**
     * Get the connector by name
     * @param {string} connectorName
     * @return {ConnectorDeclaration}
     */
    getConnectorByName(connectorName) {
        var factory = this.getFactory();
        var connectorReference = _.find(this.getChildren(), function (child) {
            return (factory.isConnectorDeclaration(child) && (child.getConnectorVariable() === connectorName));
        });

        return !_.isNil(connectorReference) ? connectorReference : this.getParent(). getConnectorByName(connectorName);
    }

    /**
     * Get all the connector references in the immediate scope
     * @return {ConnectorDeclaration[]} connectorReferences
     */
    getConnectorsInImmediateScope() {
        var factory = this.getFactory();
        var connectorReferences = _.filter(this.getChildren(), function (child) {
            return factory.isConnectorDeclaration(child);
        });

        return !_.isEmpty(connectorReferences) ? connectorReferences : this.getParent().getConnectorsInImmediateScope();
    }

    getPathAnnotation() {
        let pathAnnotation = undefined;
        _.forEach(this.getChildrenOfType(this.getFactory().isAnnotation), annotationAST => {
            if (_.isEqual(annotationAST.getPackageName().toLowerCase(), 'http') && _.isEqual(annotationAST.getIdentifier().toLowerCase(), 'path')) {
                pathAnnotation = annotationAST;
            }
        });

        return pathAnnotation;
    }

    getHttpMethodAnnotation() {
        let httpMethodAnnotation = undefined;
        _.forEach(this.getChildrenOfType(this.getFactory().isAnnotation), annotationAST => {
            if (_.includes(_.map(supportedHttpMethodAnnotations, (e) => {return e.toLowerCase();}), annotationAST.getIdentifier().toLowerCase())) {
                httpMethodAnnotation = annotationAST;
            }
        });

        return httpMethodAnnotation;
    }
}

export default ResourceDefinition;
