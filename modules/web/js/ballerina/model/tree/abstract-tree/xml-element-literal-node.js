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

import Node from '../node';
import _ from 'lodash';

class XmlElementLiteralNodeAbstract extends Node {


    setStartTagName(newValue, silent, title) {
        const oldValue = this.startTagName;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.startTagName = newValue;

        this.startTagName.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'startTagName',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getStartTagName() {
        return this.startTagName;
    }



    setEndTagName(newValue, silent, title) {
        const oldValue = this.endTagName;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.endTagName = newValue;

        this.endTagName.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'endTagName',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getEndTagName() {
        return this.endTagName;
    }



    setNamespaces(newValue, silent, title) {
        const oldValue = this.namespaces;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.namespaces = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'namespaces',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getNamespaces() {
        return this.namespaces;
    }


    addNamespaces(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.namespaces.push(node);
            index = this.namespaces.length;
        } else {
            this.namespaces.splice(i, 0, node);
        }
        if(!silent) {
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

    removeNamespaces(node, silent){
        const index = this.getIndexOfNamespaces(node);
        this.removeNamespacesByIndex(index);
        if(!silent) {
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

    removeNamespacesByIndex(index, silent){
        this.namespaces.splice(index, 1);
        if(!silent) {
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

    replaceNamespaces(oldChild, newChild, silent) {
        const index = this.getIndexOfNamespaces(oldChild);
        this.namespaces[index] = newChild;
    }

    getIndexOfNamespaces(child) {
        return _.findIndex(this.namespaces, ['id', child.id]);
    }

    filterNamespaces(predicateFunction) {
        return _.filter(this.namespaces, predicateFunction);
    }


    setDefaultNamespaceUri(newValue, silent, title) {
        const oldValue = this.defaultNamespaceUri;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.defaultNamespaceUri = newValue;

        this.defaultNamespaceUri.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'defaultNamespaceUri',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getDefaultNamespaceUri() {
        return this.defaultNamespaceUri;
    }



    setAttributes(newValue, silent, title) {
        const oldValue = this.attributes;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.attributes = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'attributes',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getAttributes() {
        return this.attributes;
    }


    addAttributes(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.attributes.push(node);
            index = this.attributes.length;
        } else {
            this.attributes.splice(i, 0, node);
        }
        if(!silent) {
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

    removeAttributes(node, silent){
        const index = this.getIndexOfAttributes(node);
        this.removeAttributesByIndex(index);
        if(!silent) {
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

    removeAttributesByIndex(index, silent){
        this.attributes.splice(index, 1);
        if(!silent) {
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

    replaceAttributes(oldChild, newChild, silent) {
        const index = this.getIndexOfAttributes(oldChild);
        this.attributes[index] = newChild;
    }

    getIndexOfAttributes(child) {
        return _.findIndex(this.attributes, ['id', child.id]);
    }

    filterAttributes(predicateFunction) {
        return _.filter(this.attributes, predicateFunction);
    }


    setContent(newValue, silent, title) {
        const oldValue = this.content;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.content = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'content',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getContent() {
        return this.content;
    }


    addContent(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.content.push(node);
            index = this.content.length;
        } else {
            this.content.splice(i, 0, node);
        }
        if(!silent) {
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

    removeContent(node, silent){
        const index = this.getIndexOfContent(node);
        this.removeContentByIndex(index);
        if(!silent) {
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

    removeContentByIndex(index, silent){
        this.content.splice(index, 1);
        if(!silent) {
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

    replaceContent(oldChild, newChild, silent) {
        const index = this.getIndexOfContent(oldChild);
        this.content[index] = newChild;
    }

    getIndexOfContent(child) {
        return _.findIndex(this.content, ['id', child.id]);
    }

    filterContent(predicateFunction) {
        return _.filter(this.content, predicateFunction);
    }


}

export default XmlElementLiteralNodeAbstract;
