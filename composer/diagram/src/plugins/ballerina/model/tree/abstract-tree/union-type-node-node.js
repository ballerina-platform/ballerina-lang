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

class AbstractUnionTypeNodeNode extends Node {


    setMemberTypeNodes(newValue, silent, title) {
        const oldValue = this.memberTypeNodes;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.memberTypeNodes = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'memberTypeNodes',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getMemberTypeNodes() {
        return this.memberTypeNodes;
    }


    addMemberTypeNodes(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.memberTypeNodes.push(node);
            index = this.memberTypeNodes.length;
        } else {
            this.memberTypeNodes.splice(i, 0, node);
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

    removeMemberTypeNodes(node, silent) {
        const index = this.getIndexOfMemberTypeNodes(node);
        this.removeMemberTypeNodesByIndex(index, silent);
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

    removeMemberTypeNodesByIndex(index, silent) {
        this.memberTypeNodes.splice(index, 1);
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

    replaceMemberTypeNodes(oldChild, newChild, silent) {
        const index = this.getIndexOfMemberTypeNodes(oldChild);
        this.memberTypeNodes[index] = newChild;
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

    replaceMemberTypeNodesByIndex(index, newChild, silent) {
        this.memberTypeNodes[index] = newChild;
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

    getIndexOfMemberTypeNodes(child) {
        return _.findIndex(this.memberTypeNodes, ['id', child.id]);
    }

    filterMemberTypeNodes(predicateFunction) {
        return _.filter(this.memberTypeNodes, predicateFunction);
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

export default AbstractUnionTypeNodeNode;
