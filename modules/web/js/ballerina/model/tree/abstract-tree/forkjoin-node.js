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

class ForkjoinNodeAbstract extends Node {


    setWorkers(newValue, title) {
        let oldValue = this.workers;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.workers = newValue;
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

    getWorkers() {
        return this.workers;
    }


    addWorkers(node, i = -1){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.workers.push(node);
            index = this.workers.length;
        } else {
            this.workers.splice(i, 0, node);
        }
        this.trigger('tree-modified', {
            origin: this,
            type: 'child-added',
            title: `Add ${child.kind}`,
            data: {
                child,
                index,
            },
        });
    }


    setJoinType(newValue, title) {
        let oldValue = this.joinType;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.joinType = newValue;
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

    getJoinType() {
        return this.joinType;
    }



    setJoinedWorkerIdentifiers(newValue, title) {
        let oldValue = this.joinedWorkerIdentifiers;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.joinedWorkerIdentifiers = newValue;
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

    getJoinedWorkerIdentifiers() {
        return this.joinedWorkerIdentifiers;
    }


    addJoinedWorkerIdentifiers(node, i = -1){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.joinedWorkerIdentifiers.push(node);
            index = this.joinedWorkerIdentifiers.length;
        } else {
            this.joinedWorkerIdentifiers.splice(i, 0, node);
        }
        this.trigger('tree-modified', {
            origin: this,
            type: 'child-added',
            title: `Add ${child.kind}`,
            data: {
                child,
                index,
            },
        });
    }


    setJoinCount(newValue, title) {
        let oldValue = this.joinCount;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.joinCount = newValue;
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

    getJoinCount() {
        return this.joinCount;
    }



    setJoinBody(newValue, title) {
        let oldValue = this.joinBody;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.joinBody = newValue;
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

    getJoinBody() {
        return this.joinBody;
    }



    setTimeOutExpression(newValue, title) {
        let oldValue = this.timeOutExpression;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.timeOutExpression = newValue;
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

    getTimeOutExpression() {
        return this.timeOutExpression;
    }



    setTimeOutVariable(newValue, title) {
        let oldValue = this.timeOutVariable;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.timeOutVariable = newValue;
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

    getTimeOutVariable() {
        return this.timeOutVariable;
    }



    setTimeoutBody(newValue, title) {
        let oldValue = this.timeoutBody;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.timeoutBody = newValue;
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

    getTimeoutBody() {
        return this.timeoutBody;
    }



    setJoinResultsName(newValue, title) {
        let oldValue = this.joinResultsName;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.joinResultsName = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'joinResultsName',
                newValue,
                oldValue,
            }
        });
    }

    getJoinResultsName() {
        return this.joinResultsName;
    }



    setJoinResultsType(newValue, title) {
        let oldValue = this.joinResultsType;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.joinResultsType = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'joinResultsType',
                newValue,
                oldValue,
            }
        });
    }

    getJoinResultsType() {
        return this.joinResultsType;
    }



    setWS(newValue, title) {
        let oldValue = this.wS;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.wS = newValue;
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

    getWS() {
        return this.wS;
    }



    setKind(newValue, title) {
        let oldValue = this.kind;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.kind = newValue;
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

    getKind() {
        return this.kind;
    }



    setPosition(newValue, title) {
        let oldValue = this.position;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.position = newValue;
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

    getPosition() {
        return this.position;
    }



}

export default ForkjoinNodeAbstract;
