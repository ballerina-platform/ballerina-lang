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

class CompilationUnitNodeAbstract extends Node {


    setTopLevelNodes(newValue, silent, title) {
        let oldValue = this.topLevelNodes;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.topLevelNodes = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'topLevelNodes',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getTopLevelNodes() {
        return this.topLevelNodes;
    }


    addTopLevelNodes(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.topLevelNodes.push(node);
            index = this.topLevelNodes.length;
        } else {
            this.topLevelNodes.splice(i, 0, node);
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

    removeTopLevelNodes(node, silent){
        const index = this.getIndexOfTopLevelNodes(node);
        this.removeTopLevelNodesByIndex(index);
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

    removeTopLevelNodesByIndex(index, silent){
        this.topLevelNodes.splice(index, 1);
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

    replaceTopLevelNodes(oldChild, newChild, silent){
        const index = this.getIndexOfTopLevelNodes(oldChild);
        this.topLevelNodes[index] = newChild;
    }

    getIndexOfTopLevelNodes(child){
        return _.findIndex(this.topLevelNodes, ['id', child.id]);
    }

    filterTopLevelNodes(predicateFunction){
        return _.filter(this.topLevelNodes, predicateFunction);
    }


    setName(newValue, silent, title) {
        let oldValue = this.name;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.name = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'name',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getName() {
        return this.name;
    }



    setWS(newValue, silent, title) {
        let oldValue = this.wS;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.wS = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'wS',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getWS() {
        return this.wS;
    }



    setKind(newValue, silent, title) {
        let oldValue = this.kind;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.kind = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'kind',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getKind() {
        return this.kind;
    }



    setPosition(newValue, silent, title) {
        let oldValue = this.position;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.position = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'position',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getPosition() {
        return this.position;
    }



}

export default CompilationUnitNodeAbstract;
