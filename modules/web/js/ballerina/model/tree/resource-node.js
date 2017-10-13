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
import _ from 'lodash';
import NodeFactory from 'ballerina/model/node-factory';
import AbstractResourceNode from './abstract-tree/resource-node';
import TreeUtil from './../tree-util';

/**
 * Node for a resource definition.
 * @class ResourceNode
 * @extends {AbstractResourceNode}
 */
class ResourceNode extends AbstractResourceNode {
    /**
     * Gets the path value in the @http:resourceConfig annotation.
     *
     * @param {boolean} [createIfNotExist=false] Creates if path value doesnt exists when true.
     * @param {string} [httpPackageAlias='http'] The ballerina.net.http package alias.
     * @returns {string} The path value.
     * @memberof ResourceNode
     */
    getPathAnnotationValue(createIfNotExist = false, httpPackageAlias = 'http') {
        let pathValue;
        this.getAnnotationAttachments().forEach((annotationNode) => {
            if (annotationNode.getPackageAlias().getValue() === httpPackageAlias &&
                annotationNode.getAnnotationName().getValue() === 'resourceConfig') {
                annotationNode.getAttributes().forEach((annotationAttribute) => {
                    if (annotationAttribute.getName() === 'path') {
                        const pathAnnotationAttributeValue = annotationAttribute.getValue();
                        pathValue = pathAnnotationAttributeValue.getValue().getValue();
                    }
                });
            }
        });

        // Creating GET http method annotation.
        if (createIfNotExist && pathValue === undefined) {
            const pathValueLiteral = NodeFactory.createLiteral({
                value: '/' + this.getResourceName(),
            });
            const pathAttributeValue = NodeFactory.createAnnotationAttributeValue({
                value: pathValueLiteral,
            });

            const pathAttribute = NodeFactory.createAnnotationAttachmentAttribute({
                name: NodeFactory.createLiteral({ name: 'path' }),
                value: pathAttributeValue,
            });

            let resourceConfigAnnotation;
            this.getAnnotationAttachments().forEach((annotationNode) => {
                if (annotationNode.getPackageAlias().getValue() === httpPackageAlias &&
                    annotationNode.getAnnotationName().getValue() === 'resourceConfig') {
                    resourceConfigAnnotation = annotationNode;
                }
            });
            if (resourceConfigAnnotation === undefined) {
                resourceConfigAnnotation = NodeFactory.createAnnotationAttachment({
                    packageAlias: NodeFactory.createLiteral({
                        value: 'http',
                    }),
                    annotationName: NodeFactory.createLiteral({
                        value: 'resourceConfig',
                    }),
                });
                this.addAnnotationAttachments(resourceConfigAnnotation);
            }

            resourceConfigAnnotation.addAttributes(pathAttribute);

            pathValue = '/' + this.getResourceName();
        }

        return pathValue;
    }

    /**
     * Gets the http methods for the resource.
     * @param {boolean} [createIfNotExist=false] Creates if path value doesnt exists when true.
     * @param {string} [httpPackageAlias='http'] The ballerina.net.http package alias.
     * @returns {string[]} http methods.
     * @memberof ResourceDefinition
     */
    getHttpMethodValues(createIfNotExist = false, httpPackageAlias = 'http') {
        const httpMethods = [];
        this.getAnnotationAttachments().forEach((annotationNode) => {
            if (annotationNode.getPackageAlias().getValue() === httpPackageAlias &&
                annotationNode.getAnnotationName().getValue() === 'resourceConfig') {
                annotationNode.getAttributes().forEach((annotationAttribute) => {
                    if (annotationAttribute.getName() === 'methods') {
                        const httpMethodsArray = annotationAttribute.getValue();
                        httpMethodsArray.getValueArray().forEach((httpMethod) => {
                            httpMethods.push(httpMethod.getValue().getValue());
                        });
                    }
                });
            }
        });

        // Creating GET http method annotation.
        if (createIfNotExist && httpMethods.length === 0) {
            const getHttpValueLiteral = NodeFactory.createLiteral({
                value: 'GET',
            });
            const getHttpValue = NodeFactory.createAnnotationAttributeValue({
                value: getHttpValueLiteral,
            });

            const methodsArrayValue = NodeFactory.createAnnotationAttributeValue({
                arrayValue: [getHttpValue],
            });

            const httpMethodAttribute = NodeFactory.createAnnotationAttachmentAttribute({
                name: NodeFactory.createLiteral({ name: 'methods' }),
                value: methodsArrayValue,
            });


            let resourceConfigAnnotation;
            this.getAnnotationAttachments().forEach((annotationNode) => {
                if (annotationNode.getPackageAlias().getValue() === httpPackageAlias &&
                    annotationNode.getAnnotationName().getValue() === 'resourceConfig') {
                    resourceConfigAnnotation = annotationNode;
                }
            });
            if (resourceConfigAnnotation === undefined) {
                resourceConfigAnnotation = NodeFactory.createAnnotationAttachment({
                    packageAlias: NodeFactory.createLiteral({
                        value: 'http',
                    }),
                    annotationName: NodeFactory.createLiteral({
                        value: 'resourceConfig',
                    }),
                });
                this.addAnnotationAttachments(resourceConfigAnnotation);
            }
            resourceConfigAnnotation.addAttributes(httpMethodAttribute);
            httpMethods.push('GET');
        }

        return httpMethods;
    }

    /**
     * Generates the URL for the resource
     * @returns {string} The url.
     * @memberof ResourceNode
     */
    compileURL() {
        let url = '/';
        // Setting basepath using service node.
        let basePath = this.parent.getBasePathAnnotationValue();
        if (basePath === undefined || basePath.trim() === '') {
            basePath = this.parent.getName().getValue();
        }

        url += basePath;

        // Setting path using this resource node.
        let path = this.getPathAnnotationValue();
        if (path === undefined || path.trim() === '') {
            path = _.trim(this.getName().getValue(), '"');
        } else {
            path = _.trim(path, '"/');
        }

        url += '/' + path;

        // Generating query params.
        let queryParams = '';
        this.getParameters().forEach((currentParam, currentIndex) => {
            if (currentParam.getAnnotationAttachments().length > 0) {
                const annotation = currentParam.getAnnotationAttachments()[0];
                if (annotation.getAnnotationName().getValue() === 'QueryParam' &&
                                                            annotation.getAttributes().length > 0) {
                    const attribute = annotation.getAttributes()[0];
                    const attributeValue = attribute.getValue();
                    const literal = attributeValue.getValue();
                    if (currentIndex === 2) {
                        queryParams += `?${_.trim(literal.getValue(), '"')}=`;
                    } else {
                        queryParams += `&${_.trim(literal.getValue(), '"')}=`;
                    }
                }
            }
        });

        return url + queryParams;
    }

    /**
     * Indicates whether the given instance of node can be accepted when dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     * @returns {Boolean} True if can be acceped.
     */
    canAcceptDrop(node) {
        return TreeUtil.isWorker(node) || TreeUtil.isConnectorDeclaration(node);
    }

    /**
     * Accept a node which is dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     * @param {Node} dropBefore Drop before given node
     *
     */
    acceptDrop(node, dropBefore) {
        if (TreeUtil.isWorker(node)) {
            // if a worker does not exist we need to create aanother worker for default.
            if (this.getWorkers().length === 0) {
                const defaultWorker = node.meta;
                delete node.meta;
                const connectors = this.getBody().getStatements()
                        .filter((statement) => { return TreeUtil.isConnectorDeclaration(statement); });
                const statements = this.getBody().getStatements()
                        .filter((statement) => { return !TreeUtil.isConnectorDeclaration(statement); });
                this.getBody().setStatements(connectors, true);
                defaultWorker.getBody().setStatements(statements);
                this.addWorkers(defaultWorker, -1, true);
            }
            const index = !_.isNil(dropBefore) ? this.getIndexOfWorkers(dropBefore) : -1;
            this.addWorkers(node, index);
        } else if (TreeUtil.isConnectorDeclaration(node)) {
            TreeUtil.getNewTempVarName(this.getBody(), '__endpoint')
                .then((varNames) => {
                    node.getVariable().getName().setValue(varNames[0]);
                    this.getBody().addStatements(node, 0);
                });
        }
    }
}

export default ResourceNode;
