
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
import ASTFactory from './ast-factory';

/**
 * Constructor for ResourceDefinition
 * @param {Object} args - The arguments to create the ServiceDefinition
 * @param {string} [args.resourceName=newResource] - Service name
 * @constructor
 */
class ResourceDefinition extends ASTNode {
    constructor(args) {
        // TODO: All the types should be referred from the global constants
        super('ResourceDefinition');
        this._resourceName = _.get(args, 'resourceName');

        this.whiteSpace.defaultDescriptor.regions = {
            0: ' ',
            1: ' ',
            2: '',
            3: ' ',
            4: '\n',
            5: '\n',
        };
    }

    setResourceName(resourceName, options) {
        if (!_.isNil(resourceName) && resourceName !== undefined) {
            this.setAttribute('_resourceName', resourceName, options);
        } else {
            log.error('Invalid Resource name [' + resourceName + '] Provided');
            throw 'Invalid Resource name [' + resourceName + '] Provided';
        }
    }

    getVariableDefinitionStatements() {
        const variableDefinitionStatements = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (ASTFactory.isVariableDefinitionStatement(child)) {
                variableDefinitionStatements.push(child);
            }
        });
        return variableDefinitionStatements;
    }

    /**
     * Get the namespace declaration statements.
     * @return {ASTNode[]} namespace declaration statements
     * */
    getNamespaceDeclarationStatements() {
        return this.filterChildren(ASTFactory.isNamespaceDeclarationStatement).slice(0);
    }

    getArguments() {
        return this.getArgumentParameterDefinitionHolder().getChildren();
    }

    getResourceName() {
        return this._resourceName;
    }

    /**
     * Adds new variable declaration.
     */
    addVariableDeclaration(newVariableDeclaration) {
        const self = this;
        // Get the index of the last variable declaration.
        let index = _.findLastIndex(this.getChildren(), (child) => {
            return ASTFactory.isVariableDeclaration(child);
        });

        // index = -1 when there are not any variable declarations, hence get the index for connector
        // declarations.
        if (index === -1) {
            index = _.findLastIndex(this.getChildren(), (child) => {
                return ASTFactory.isConnectorDeclaration(child);
            });
        }

        this.addChild(newVariableDeclaration, index + 1);
    }

    /**
     * Adds new variable declaration.
     */
    removeVariableDeclaration(variableDeclarationIdentifier) {
        const self = this;
        // Removing the variable from the children.
        const variableDeclarationChild = _.find(this.getChildren(), (child) => {
            return ASTFactory.isVariableDeclaration(child)
                && child.getIdentifier() === variableDeclarationIdentifier;
        });
        this.removeChild(variableDeclarationChild);
    }

    /**
     * Returns the list of parameters as a string separated by commas.
     * @return {string} - Parameters as string.
     */
    getParametersAsString() {
        let paramsAsString = '';
        const params = this.getArguments();

        _.forEach(params, (parameter, index) => {
            if (index != 0) {
                paramsAsString += ((parameter.whiteSpace.useDefault) ? ', ' 
                            : (',' + parameter.getWSRegion(0)));
            }
            paramsAsString += parameter.getParameterDefinitionAsString();
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
        const identifierAlreadyExists = _.findIndex(this.getParameters(), (parameter) => {
            return parameter.getName() === parameterIdentifier;
        }) !== -1;

        // If parameter with the same identifier exists, then throw an error. Else create the new parameter.
        if (identifierAlreadyExists) {
            const errorString = "A resource parameter with identifier '" + parameterIdentifier + "' already exists.";
            log.error(errorString);
            throw errorString;
        } else {
            // Creating resource parameter
            const newParameter = ASTFactory.createResourceParameter();
            newParameter.setAnnotationType(annotationType);
            newParameter.setAnnotationText(annotationText);
            newParameter.setType(parameterType);
            newParameter.setName(parameterIdentifier);

            const self = this;

            // Get the index of the last resource parameter declaration.
            const index = _.findLastIndex(this.getChildren(), (child) => {
                return ASTFactory.isResourceParameter(child);
            });

            this.addChild(newParameter, index + 1);
        }
    }

    /**
     * Removes a parameter from the resource definition.
     * @param {string} modelID - The id of the parameter model.
     */
    removeParameter(modelID) {
        const self = this;
        // Deleting the variable from the children.
        const resourceParameter = _.find(this.getChildren(), (child) => {
            return ASTFactory.isResourceParameter(child) && child.id === modelID;
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
        const connectorDeclaration = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (ASTFactory.isConnectorDeclaration(child)) {
                connectorDeclaration.push(child);
            }
        });
        return _.sortBy(connectorDeclaration, [function (connectorDeclaration) {
            return connectorDeclaration.getConnectorVariable();
        }]);
    }

    getWorkerDeclarations() {
        const workerDeclarations = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (ASTFactory.isWorkerDeclaration(child)) {
                workerDeclarations.push(child);
            }
        });
        return _.sortBy(workerDeclarations, [function (workerDeclaration) {
            return workerDeclaration.getWorkerName();
        }]);
    }

    getArgumentParameterDefinitionHolder() {
        let argParamDefHolder = this.findChild(ASTFactory.isArgumentParameterDefinitionHolder);
        if (_.isUndefined(argParamDefHolder)) {
            argParamDefHolder = ASTFactory.createArgumentParameterDefinitionHolder();
            this.addChild(argParamDefHolder, undefined, true);
        }
        return argParamDefHolder;
    }

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    canBeParentOf(node) {
        return ASTFactory.isConnectorDeclaration(node)
            || ASTFactory.isVariableDeclaration(node)
            || ASTFactory.isWorkerDeclaration(node)
            || ASTFactory.isStatement(node);
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
        this.setResourceName(jsonNode.resource_name, { doSilently: true });
        const self = this;
        _.each(jsonNode.children, (childNode) => {
            const child = ASTFactory.createFromJson(childNode);
            self.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }

    /**
     * Override the addChild method for ordering the child elements
     * @param {ASTNode} child
     * @param {number|undefined} index
     */
    addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId) {
        if (!ASTFactory.isWorkerDeclaration(child)) {
            const firstWorkerIndex = _.findIndex(
                this.getChildren(), child => ASTFactory.isWorkerDeclaration(child));

            if (firstWorkerIndex > -1 && _.isNil(index)) {
                index = firstWorkerIndex;
            }
        }
        Object.getPrototypeOf(this.constructor.prototype)
        .addChild.call(this, child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
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
                    getter: this.getResourceName,
                }],
            }],
        });
    }

    /**
     * Get the connector by name
     * @param {string} connectorName - name of the connector
     * @return {ConnectorDeclaration} - Connector declaration with the given connector name
     */
    getConnectorByName(connectorName) {
        const factory = ASTFactory;
        const connectorReference = _.find(this.getChildren(), (child) => {
            let connectorVariableName;
            if (factory.isAssignmentStatement(child) && factory.isConnectorInitExpression(child.getChildren()[1])) {
                const variableReferenceList = [];

                _.forEach(child.getChildren()[0].getChildren(), (variableRef) => {
                    variableReferenceList.push(variableRef.getExpressionString());
                });

                connectorVariableName = (_.join(variableReferenceList, ',')).trim();
            } else if (factory.isConnectorDeclaration(child)) {
                connectorVariableName = child.getConnectorVariable();
            }

            return connectorVariableName === connectorName;
        });

        return !_.isNil(connectorReference) ? connectorReference : this.getParent().getConnectorByName(connectorName);
    }

    /**
     * Get all the connector references in the immediate scope
     * @return {ConnectorDeclaration[]} connectorReferences
     */
    getConnectorsInImmediateScope() {
        const factory = ASTFactory;
        const connectorReferences = _.filter(this.getChildren(), (child) => {
            return factory.isConnectorDeclaration(child);
        });

        return !_.isEmpty(connectorReferences) ? connectorReferences : this.getParent().getConnectorsInImmediateScope();
    }

    /**
     * Gets the path value in the @http:resourceConfig annotation.
     *
     * @param {boolean} [ifNotExist=false] Creates if path value doesnt exists when true.
     * @returns {string} The path value.
     * @memberof ResourceDefinition
     */
    getPathAnnotationValue(ifNotExist = false) {
        let pathValue;
        _.forEach(this.getChildrenOfType(ASTFactory.isAnnotationAttachment), (annotationAST) => {
            if (annotationAST.getFullPackageName() === 'ballerina.net.http' &&
                annotationAST.getPackageName() === 'http' &&
                annotationAST.getName() === 'resourceConfig') {
                const resourceConfigAnnotation = annotationAST;

                resourceConfigAnnotation.getChildren().forEach((annotationAttribute) => {
                    if (annotationAttribute.getKey() === 'path') {
                        const pathAnnotationAttributeValue = annotationAttribute.getValue();
                        if (pathAnnotationAttributeValue.getChildren() > 0) {
                            pathValue = pathAnnotationAttributeValue[0].getStringValue();
                        }
                    }
                });
            }
        });

        // Creating GET http method annotation.
        if (pathValue === undefined && ifNotExist) {
            const resourceConfigAnnotation = ASTFactory.createAnnotationAttachment({
                fullPackageName: 'ballerina.net.http',
                packageName: 'http',
                name: 'resourceConfig',
            });
            const pathAttribute = ASTFactory.createAnnotationAttribute({
                key: 'path',
            });

            const pathAttributeValue = ASTFactory.createAnnotationAttributeValue();

            const pathBValue = ASTFactory.createBValue({
                stringValue: '/' + this.getResourceName(),
            });

            pathAttributeValue.addChild(pathBValue);
            pathAttribute.addChild(pathAttributeValue);
            resourceConfigAnnotation.addChild(pathAttribute);
            this.addChild(resourceConfigAnnotation, 0);

            pathValue = '/' + this.getResourceName();
        }

        return pathValue;
    }

    /**
     * Gets the http methods for the resource.
     * @returns {string[]} http methods.
     * @memberof ResourceDefinition
     */
    getHttpMethodValues() {
        const httpMethods = [];
        this.getChildrenOfType(ASTFactory.isAnnotationAttachment).forEach((annotationAST) => {
            if (annotationAST.getFullPackageName() === 'ballerina.net.http' &&
                annotationAST.getPackageName() === 'http' &&
                annotationAST.getName() === 'resourceConfig') {
                const resourceConfigAnnotation = annotationAST;

                resourceConfigAnnotation.getChildren().forEach((annotationAttribute) => {
                    if (annotationAttribute.getKey() === 'methods') {
                        const httpMethodsArray = annotationAttribute.getValue();
                        httpMethodsArray.getChildren().forEach((httpMethod) => {
                            httpMethods.push(httpMethod.getChildren()[0].getStringValue());
                        });
                    }
                });
            }
        });

        // Creating GET http method annotation.
        if (httpMethods.length === 0) {
            let resourceConfigAnnotation;
            this.getChildrenOfType(ASTFactory.isAnnotationAttachment).forEach((annotationAST) => {
                if (annotationAST.getFullPackageName() === 'ballerina.net.http' &&
                    annotationAST.getPackageName() === 'http' &&
                    annotationAST.getName() === 'resourceConfig') {
                    resourceConfigAnnotation = annotationAST;
                }
            });
            if (resourceConfigAnnotation === undefined) {
                resourceConfigAnnotation = ASTFactory.createAnnotationAttachment({
                    fullPackageName: 'ballerina.net.http',
                    packageName: 'http',
                    name: 'resourceConfig',
                });
            }
            const httpMethodAttribute = ASTFactory.createAnnotationAttribute({
                key: 'methods',
            });

            const httpMethodsAttributeValue = ASTFactory.createAnnotationAttributeValue();
            const httpMethodsArray = ASTFactory.createAnnotationAttributeValue();

            const getHttpValue = ASTFactory.createBValue({
                stringValue: 'GET',
            });

            httpMethodsArray.addChild(getHttpValue);
            httpMethodsAttributeValue.addChild(httpMethodsArray);
            httpMethodAttribute.addChild(httpMethodsAttributeValue);
            resourceConfigAnnotation.addChild(httpMethodAttribute);
            this.addChild(resourceConfigAnnotation, 0);

            httpMethods.push('GET');
        }

        return httpMethods;
    }
}

export default ResourceDefinition;
