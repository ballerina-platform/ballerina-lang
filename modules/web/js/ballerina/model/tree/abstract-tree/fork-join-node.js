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

class ForkJoinNodeAbstract extends Node {


    setWorkers(newValue, silent, title) {
        let oldValue = this.workers;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.workers = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'workers',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getWorkers() {
        return this.workers;
    }


    addWorkers(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.workers.push(node);
            index = this.workers.length;
        } else {
            this.workers.splice(i, 0, node);
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

    removeWorkers(node, silent){
        const index = this.getIndexOfWorkers(node);
        this.removeWorkersByIndex(index);
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

    removeWorkersByIndex(index, silent){
        this.workers.splice(index, 1);
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

    replaceWorkers(oldChild, newChild, silent){
        const index = this.getIndexOfWorkers(oldChild);
        this.workers[index] = newChild;
    }

    getIndexOfWorkers(child){
        return _.findIndex(this.workers, ['id', child.id]);
    }

    filterWorkers(predicateFunction){
        return _.filter(this.workers, predicateFunction);
    }


    setJoinedWorkerIdentifiers(newValue, silent, title) {
        let oldValue = this.joinedWorkerIdentifiers;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.joinedWorkerIdentifiers = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'joinedWorkerIdentifiers',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getJoinedWorkerIdentifiers() {
        return this.joinedWorkerIdentifiers;
    }


    addJoinedWorkerIdentifiers(node, i = -1, silent){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.joinedWorkerIdentifiers.push(node);
            index = this.joinedWorkerIdentifiers.length;
        } else {
            this.joinedWorkerIdentifiers.splice(i, 0, node);
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

    removeJoinedWorkerIdentifiers(node, silent){
        const index = this.getIndexOfJoinedWorkerIdentifiers(node);
        this.removeJoinedWorkerIdentifiersByIndex(index);
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

    removeJoinedWorkerIdentifiersByIndex(index, silent){
        this.joinedWorkerIdentifiers.splice(index, 1);
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

    replaceJoinedWorkerIdentifiers(oldChild, newChild, silent){
        const index = this.getIndexOfJoinedWorkerIdentifiers(oldChild);
        this.joinedWorkerIdentifiers[index] = newChild;
    }

    getIndexOfJoinedWorkerIdentifiers(child){
        return _.findIndex(this.joinedWorkerIdentifiers, ['id', child.id]);
    }

    filterJoinedWorkerIdentifiers(predicateFunction){
        return _.filter(this.joinedWorkerIdentifiers, predicateFunction);
    }


    setJoinBody(newValue, silent, title) {
        let oldValue = this.joinBody;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.joinBody = newValue;

        this.joinBody.parent = this;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'joinBody',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getJoinBody() {
        return this.joinBody;
    }



    setTimeOutExpression(newValue, silent, title) {
        let oldValue = this.timeOutExpression;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.timeOutExpression = newValue;

        this.timeOutExpression.parent = this;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'timeOutExpression',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getTimeOutExpression() {
        return this.timeOutExpression;
    }



    setTimeOutVariable(newValue, silent, title) {
        let oldValue = this.timeOutVariable;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.timeOutVariable = newValue;

        this.timeOutVariable.parent = this;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'timeOutVariable',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getTimeOutVariable() {
        return this.timeOutVariable;
    }



    setTimeoutBody(newValue, silent, title) {
        let oldValue = this.timeoutBody;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.timeoutBody = newValue;

        this.timeoutBody.parent = this;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'timeoutBody',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getTimeoutBody() {
        return this.timeoutBody;
    }



    setJoinType(newValue, silent, title) {
        let oldValue = this.joinType;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.joinType = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'joinType',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getJoinType() {
        return this.joinType;
    }



    setJoinCount(newValue, silent, title) {
        let oldValue = this.joinCount;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.joinCount = newValue;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'joinCount',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getJoinCount() {
        return this.joinCount;
    }



    setJoinResultVar(newValue, silent, title) {
        let oldValue = this.joinResultVar;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.joinResultVar = newValue;

        this.joinResultVar.parent = this;

        if(!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'joinResultVar',
                    newValue,
                    oldValue,
                }
            });
        }
    }

    getJoinResultVar() {
        return this.joinResultVar;
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

export default ForkJoinNodeAbstract;
