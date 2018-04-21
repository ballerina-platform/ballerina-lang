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
import Node from '../node';

class AbstractServiceNode extends Node {


    setEndpointNodes(newValue, silent, title) {
        const oldValue = this.endpointNodes;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.endpointNodes = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'endpointNodes',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getEndpointNodes() {
        return this.endpointNodes;
    }


    addEndpointNodes(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.endpointNodes.push(node);
            index = this.endpointNodes.length;
        } else {
            this.endpointNodes.splice(i, 0, node);
        }
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Add ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeEndpointNodes(node, silent) {
        const index = this.getIndexOfEndpointNodes(node);
        this.removeEndpointNodesByIndex(index, silent);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeEndpointNodesByIndex(index, silent) {
        this.endpointNodes.splice(index, 1);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceEndpointNodes(oldChild, newChild, silent) {
        const index = this.getIndexOfEndpointNodes(oldChild);
        this.endpointNodes[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceEndpointNodesByIndex(index, newChild, silent) {
        this.endpointNodes[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    getIndexOfEndpointNodes(child) {
        return _.findIndex(this.endpointNodes, ['id', child.id]);
    }

    filterEndpointNodes(predicateFunction) {
        return _.filter(this.endpointNodes, predicateFunction);
    }


    setVariables(newValue, silent, title) {
        const oldValue = this.variables;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.variables = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'variables',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getVariables() {
        return this.variables;
    }


    addVariables(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.variables.push(node);
            index = this.variables.length;
        } else {
            this.variables.splice(i, 0, node);
        }
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Add ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeVariables(node, silent) {
        const index = this.getIndexOfVariables(node);
        this.removeVariablesByIndex(index, silent);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeVariablesByIndex(index, silent) {
        this.variables.splice(index, 1);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceVariables(oldChild, newChild, silent) {
        const index = this.getIndexOfVariables(oldChild);
        this.variables[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceVariablesByIndex(index, newChild, silent) {
        this.variables[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    getIndexOfVariables(child) {
        return _.findIndex(this.variables, ['id', child.id]);
    }

    filterVariables(predicateFunction) {
        return _.filter(this.variables, predicateFunction);
    }


    setInitFunction(newValue, silent, title) {
        const oldValue = this.initFunction;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.initFunction = newValue;

        this.initFunction.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'initFunction',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getInitFunction() {
        return this.initFunction;
    }



    setServiceTypeStruct(newValue, silent, title) {
        const oldValue = this.serviceTypeStruct;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.serviceTypeStruct = newValue;

        this.serviceTypeStruct.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'serviceTypeStruct',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getServiceTypeStruct() {
        return this.serviceTypeStruct;
    }



    setBoundEndpoints(newValue, silent, title) {
        const oldValue = this.boundEndpoints;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.boundEndpoints = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'boundEndpoints',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getBoundEndpoints() {
        return this.boundEndpoints;
    }


    addBoundEndpoints(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.boundEndpoints.push(node);
            index = this.boundEndpoints.length;
        } else {
            this.boundEndpoints.splice(i, 0, node);
        }
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Add ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeBoundEndpoints(node, silent) {
        const index = this.getIndexOfBoundEndpoints(node);
        this.removeBoundEndpointsByIndex(index, silent);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeBoundEndpointsByIndex(index, silent) {
        this.boundEndpoints.splice(index, 1);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceBoundEndpoints(oldChild, newChild, silent) {
        const index = this.getIndexOfBoundEndpoints(oldChild);
        this.boundEndpoints[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceBoundEndpointsByIndex(index, newChild, silent) {
        this.boundEndpoints[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    getIndexOfBoundEndpoints(child) {
        return _.findIndex(this.boundEndpoints, ['id', child.id]);
    }

    filterBoundEndpoints(predicateFunction) {
        return _.filter(this.boundEndpoints, predicateFunction);
    }


    setAnonymousEndpointBind(newValue, silent, title) {
        const oldValue = this.anonymousEndpointBind;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.anonymousEndpointBind = newValue;

        this.anonymousEndpointBind.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'anonymousEndpointBind',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getAnonymousEndpointBind() {
        return this.anonymousEndpointBind;
    }



    setName(newValue, silent, title) {
        const oldValue = this.name;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.name = newValue;

        this.name.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'name',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getName() {
        return this.name;
    }



    setResources(newValue, silent, title) {
        const oldValue = this.resources;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.resources = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'resources',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getResources() {
        return this.resources;
    }


    addResources(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.resources.push(node);
            index = this.resources.length;
        } else {
            this.resources.splice(i, 0, node);
        }
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Add ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeResources(node, silent) {
        const index = this.getIndexOfResources(node);
        this.removeResourcesByIndex(index, silent);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeResourcesByIndex(index, silent) {
        this.resources.splice(index, 1);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceResources(oldChild, newChild, silent) {
        const index = this.getIndexOfResources(oldChild);
        this.resources[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceResourcesByIndex(index, newChild, silent) {
        this.resources[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    getIndexOfResources(child) {
        return _.findIndex(this.resources, ['id', child.id]);
    }

    filterResources(predicateFunction) {
        return _.filter(this.resources, predicateFunction);
    }


    setFlags(newValue, silent, title) {
        const oldValue = this.flags;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.flags = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'flags',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getFlags() {
        return this.flags;
    }



    setAnnotationAttachments(newValue, silent, title) {
        const oldValue = this.annotationAttachments;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.annotationAttachments = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'annotationAttachments',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getAnnotationAttachments() {
        return this.annotationAttachments;
    }


    addAnnotationAttachments(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.annotationAttachments.push(node);
            index = this.annotationAttachments.length;
        } else {
            this.annotationAttachments.splice(i, 0, node);
        }
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Add ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeAnnotationAttachments(node, silent) {
        const index = this.getIndexOfAnnotationAttachments(node);
        this.removeAnnotationAttachmentsByIndex(index, silent);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeAnnotationAttachmentsByIndex(index, silent) {
        this.annotationAttachments.splice(index, 1);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceAnnotationAttachments(oldChild, newChild, silent) {
        const index = this.getIndexOfAnnotationAttachments(oldChild);
        this.annotationAttachments[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceAnnotationAttachmentsByIndex(index, newChild, silent) {
        this.annotationAttachments[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    getIndexOfAnnotationAttachments(child) {
        return _.findIndex(this.annotationAttachments, ['id', child.id]);
    }

    filterAnnotationAttachments(predicateFunction) {
        return _.filter(this.annotationAttachments, predicateFunction);
    }


    setDeprecatedAttachments(newValue, silent, title) {
        const oldValue = this.deprecatedAttachments;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.deprecatedAttachments = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'deprecatedAttachments',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getDeprecatedAttachments() {
        return this.deprecatedAttachments;
    }


    addDeprecatedAttachments(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.deprecatedAttachments.push(node);
            index = this.deprecatedAttachments.length;
        } else {
            this.deprecatedAttachments.splice(i, 0, node);
        }
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Add ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeDeprecatedAttachments(node, silent) {
        const index = this.getIndexOfDeprecatedAttachments(node);
        this.removeDeprecatedAttachmentsByIndex(index, silent);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeDeprecatedAttachmentsByIndex(index, silent) {
        this.deprecatedAttachments.splice(index, 1);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceDeprecatedAttachments(oldChild, newChild, silent) {
        const index = this.getIndexOfDeprecatedAttachments(oldChild);
        this.deprecatedAttachments[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceDeprecatedAttachmentsByIndex(index, newChild, silent) {
        this.deprecatedAttachments[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    getIndexOfDeprecatedAttachments(child) {
        return _.findIndex(this.deprecatedAttachments, ['id', child.id]);
    }

    filterDeprecatedAttachments(predicateFunction) {
        return _.filter(this.deprecatedAttachments, predicateFunction);
    }


    setDocumentationAttachments(newValue, silent, title) {
        const oldValue = this.documentationAttachments;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.documentationAttachments = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'documentationAttachments',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getDocumentationAttachments() {
        return this.documentationAttachments;
    }


    addDocumentationAttachments(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.documentationAttachments.push(node);
            index = this.documentationAttachments.length;
        } else {
            this.documentationAttachments.splice(i, 0, node);
        }
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Add ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeDocumentationAttachments(node, silent) {
        const index = this.getIndexOfDocumentationAttachments(node);
        this.removeDocumentationAttachmentsByIndex(index, silent);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeDocumentationAttachmentsByIndex(index, silent) {
        this.documentationAttachments.splice(index, 1);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceDocumentationAttachments(oldChild, newChild, silent) {
        const index = this.getIndexOfDocumentationAttachments(oldChild);
        this.documentationAttachments[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceDocumentationAttachmentsByIndex(index, newChild, silent) {
        this.documentationAttachments[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    getIndexOfDocumentationAttachments(child) {
        return _.findIndex(this.documentationAttachments, ['id', child.id]);
    }

    filterDocumentationAttachments(predicateFunction) {
        return _.filter(this.documentationAttachments, predicateFunction);
    }


}

export default AbstractServiceNode;
