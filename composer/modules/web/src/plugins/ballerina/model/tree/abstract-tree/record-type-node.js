/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

class AbstractRecordTypeNode extends Node {


    setFields(newValue, silent, title) {
        const oldValue = this.fields;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.fields = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'fields',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getFields() {
        return this.fields;
    }


    addFields(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.fields.push(node);
            index = this.fields.length;
        } else {
            this.fields.splice(i, 0, node);
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

    removeFields(node, silent) {
        const index = this.getIndexOfFields(node);
        this.removeFieldsByIndex(index, silent);
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

    removeFieldsByIndex(index, silent) {
        this.fields.splice(index, 1);
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

    replaceFields(oldChild, newChild, silent) {
        const index = this.getIndexOfFields(oldChild);
        this.fields[index] = newChild;
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

    replaceFieldsByIndex(index, newChild, silent) {
        this.fields[index] = newChild;
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

    getIndexOfFields(child) {
        return _.findIndex(this.fields, ['id', child.id]);
    }

    filterFields(predicateFunction) {
        return _.filter(this.fields, predicateFunction);
    }



    isNullable() {
        return this.nullable;
    }

    setNullable(newValue, silent, title) {
        const oldValue = this.nullable;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.nullable = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'nullable',
                    newValue,
                    oldValue,
                },
            });
        }
    }


    isGrouped() {
        return this.grouped;
    }

    setGrouped(newValue, silent, title) {
        const oldValue = this.grouped;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.grouped = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'grouped',
                    newValue,
                    oldValue,
                },
            });
        }
    }

}

export default AbstractRecordTypeNode;
