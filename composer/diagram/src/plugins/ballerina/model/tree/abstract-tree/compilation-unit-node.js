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

class AbstractCompilationUnitNode extends Node {


    setTopLevelNodes(newValue, silent, title) {
        const oldValue = this.topLevelNodes;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.topLevelNodes = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'topLevelNodes',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getTopLevelNodes() {
        return this.topLevelNodes;
    }


    addTopLevelNodes(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.topLevelNodes.push(node);
            index = this.topLevelNodes.length;
        } else {
            this.topLevelNodes.splice(i, 0, node);
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

    removeTopLevelNodes(node, silent) {
        const index = this.getIndexOfTopLevelNodes(node);
        this.removeTopLevelNodesByIndex(index, silent);
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

    removeTopLevelNodesByIndex(index, silent) {
        this.topLevelNodes.splice(index, 1);
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

    replaceTopLevelNodes(oldChild, newChild, silent) {
        const index = this.getIndexOfTopLevelNodes(oldChild);
        this.topLevelNodes[index] = newChild;
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

    replaceTopLevelNodesByIndex(index, newChild, silent) {
        this.topLevelNodes[index] = newChild;
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

    getIndexOfTopLevelNodes(child) {
        return _.findIndex(this.topLevelNodes, ['id', child.id]);
    }

    filterTopLevelNodes(predicateFunction) {
        return _.filter(this.topLevelNodes, predicateFunction);
    }


    setName(newValue, silent, title) {
        const oldValue = this.name;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.name = newValue;

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



}

export default AbstractCompilationUnitNode;
