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

class AbstractAnnotationAttachmentNode extends Node {


    setAnnotationName(newValue, silent, title) {
        const oldValue = this.annotationName;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.annotationName = newValue;

        this.annotationName.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'annotationName',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getAnnotationName() {
        return this.annotationName;
    }



    setExpression(newValue, silent, title) {
        const oldValue = this.expression;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.expression = newValue;

        this.expression.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'expression',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getExpression() {
        return this.expression;
    }



    setPackageAlias(newValue, silent, title) {
        const oldValue = this.packageAlias;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.packageAlias = newValue;

        this.packageAlias.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'packageAlias',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPackageAlias() {
        return this.packageAlias;
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


    addAttributes(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.attributes.push(node);
            index = this.attributes.length;
        } else {
            this.attributes.splice(i, 0, node);
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

    removeAttributes(node, silent) {
        const index = this.getIndexOfAttributes(node);
        this.removeAttributesByIndex(index, silent);
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

    removeAttributesByIndex(index, silent) {
        this.attributes.splice(index, 1);
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

    replaceAttributes(oldChild, newChild, silent) {
        const index = this.getIndexOfAttributes(oldChild);
        this.attributes[index] = newChild;
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

    replaceAttributesByIndex(index, newChild, silent) {
        this.attributes[index] = newChild;
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

    getIndexOfAttributes(child) {
        return _.findIndex(this.attributes, ['id', child.id]);
    }

    filterAttributes(predicateFunction) {
        return _.filter(this.attributes, predicateFunction);
    }


}

export default AbstractAnnotationAttachmentNode;
