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

class AbstractTransformerNode extends Node {


    setSource(newValue, silent, title) {
        const oldValue = this.source;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.source = newValue;

        this.source.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'source',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getSource() {
        return this.source;
    }


    setReturnParameters(newValue, silent, title) {
        const oldValue = this.returnParameters;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.returnParameters = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'returnParameters',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getReturnParameters() {
        return this.returnParameters;
    }


    addReturnParameters(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.returnParameters.push(node);
            index = this.returnParameters.length;
        } else {
            this.returnParameters.splice(i, 0, node);
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

    removeReturnParameters(node, silent) {
        const index = this.getIndexOfReturnParameters(node);
        this.removeReturnParametersByIndex(index, silent);
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

    removeReturnParametersByIndex(index, silent) {
        this.returnParameters.splice(index, 1);
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

    replaceReturnParameters(oldChild, newChild, silent) {
        const index = this.getIndexOfReturnParameters(oldChild);
        this.returnParameters[index] = newChild;
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

    replaceReturnParametersByIndex(index, newChild, silent) {
        this.returnParameters[index] = newChild;
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

    getIndexOfReturnParameters(child) {
        return _.findIndex(this.returnParameters, ['id', child.id]);
    }

    filterReturnParameters(predicateFunction) {
        return _.filter(this.returnParameters, predicateFunction);
    }


    setBody(newValue, silent, title) {
        const oldValue = this.body;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.body = newValue;

        this.body.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'body',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getBody() {
        return this.body;
    }


    setWorkers(newValue, silent, title) {
        const oldValue = this.workers;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.workers = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'workers',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getWorkers() {
        return this.workers;
    }


    addWorkers(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.workers.push(node);
            index = this.workers.length;
        } else {
            this.workers.splice(i, 0, node);
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

    removeWorkers(node, silent) {
        const index = this.getIndexOfWorkers(node);
        this.removeWorkersByIndex(index, silent);
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

    removeWorkersByIndex(index, silent) {
        this.workers.splice(index, 1);
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

    replaceWorkers(oldChild, newChild, silent) {
        const index = this.getIndexOfWorkers(oldChild);
        this.workers[index] = newChild;
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

    replaceWorkersByIndex(index, newChild, silent) {
        this.workers[index] = newChild;
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

    getIndexOfWorkers(child) {
        return _.findIndex(this.workers, ['id', child.id]);
    }

    filterWorkers(predicateFunction) {
        return _.filter(this.workers, predicateFunction);
    }


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


    setParameters(newValue, silent, title) {
        const oldValue = this.parameters;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.parameters = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'parameters',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getParameters() {
        return this.parameters;
    }


    addParameters(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.parameters.push(node);
            index = this.parameters.length;
        } else {
            this.parameters.splice(i, 0, node);
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

    removeParameters(node, silent) {
        const index = this.getIndexOfParameters(node);
        this.removeParametersByIndex(index, silent);
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

    removeParametersByIndex(index, silent) {
        this.parameters.splice(index, 1);
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

    replaceParameters(oldChild, newChild, silent) {
        const index = this.getIndexOfParameters(oldChild);
        this.parameters[index] = newChild;
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

    replaceParametersByIndex(index, newChild, silent) {
        this.parameters[index] = newChild;
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

    getIndexOfParameters(child) {
        return _.findIndex(this.parameters, ['id', child.id]);
    }

    filterParameters(predicateFunction) {
        return _.filter(this.parameters, predicateFunction);
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


}

export default AbstractTransformerNode;
